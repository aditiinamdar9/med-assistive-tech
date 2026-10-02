#!/usr/bin/env python3
"""
Turn raw product data into catalog entries a person can understand.

    python enrich_catalog.py --source seedfile
    python enrich_catalog.py --source ebay --terms terms.txt --per-term 5

What it does, per product:
  1. Ask Claude to write a plain-English "this could help you when..." line
     plus difficulty tags, from the retailer title and whatever else we have.
  2. Ask it to rate its own confidence, and to refuse outright when the title
     is too vague to tell what the thing is.
  3. Merge into catalog.json - but ONLY low-risk fields, and never over a
     description a human has already approved.
  4. Write review_queue.csv with everything that needs human eyes.

Nothing reaches the app until you look at the review queue and commit.
"""

import argparse
import json
import logging
import sys
from datetime import date
from pathlib import Path

from anthropic import Anthropic
from dotenv import load_dotenv

from sources.base import RawProduct
from sources.seedfile import SeedFileSource

load_dotenv()
logging.basicConfig(level=logging.INFO, format="%(levelname)s  %(message)s")
log = logging.getLogger("enrich")

CATALOG = Path("../android-app/app/src/main/assets/catalog.json")
REVIEW_QUEUE = Path("review_queue.csv")
MODEL = "claude-sonnet-4-5"

SYSTEM = """You write product descriptions for an app that helps people find \
assistive products - things that make daily tasks easier for people with \
physical difficulties or mental health conditions.

You will be given raw retailer data for one product. Write:

1. `description` - 1-2 sentences at about a 6th-grade reading level saying what \
the thing IS and what task it makes easier. Everyday words only. No medical or \
clinical terms. No marketing language, no "revolutionary" or "premium".

2. `tags` - comma-separated difficulties this helps with, drawn from this list \
where they fit: grip-weakness, low-vision, hearing, reach, bending, mobility, \
balance, fatigue, tremor, one-handed, memory, routine, focus, sensory-overload, \
anxiety, sleep, communication. Add a new tag only if nothing here fits.

3. `category` - "physical" or "mental".

4. `confidence` - "high", "medium" or "low". Be honest. Use low when the title \
is vague, when you are guessing what the product is, or when you cannot tell \
what difficulty it addresses.

5. `concerns` - a short note if anything is wrong with this product for this \
app. Say so if it looks like a prescription or clinical device, if it makes \
medical claims, if it seems to be a different product than the search suggested, \
or if it looks unsafe or like a scam. Empty string if nothing is wrong.

Never describe a product as treating, curing, preventing or managing any \
condition. You describe what a task becomes easier. That is all.

If the raw data is too thin to describe the product honestly, set confidence to \
"low" and say so in concerns rather than inventing details.

Respond with JSON only, no fences:
{"description": "...", "tags": "...", "category": "...", "confidence": "...", "concerns": "..."}
"""


def describe(client: Anthropic, product: RawProduct) -> dict | None:
    prompt = (
        f"Title: {product.title}\n"
        f"Brand: {product.brand or 'unknown'}\n"
        f"Retailer description: {product.raw_description or 'none provided'}\n"
        f"Price seen: {product.price_low}-{product.price_high}\n"
        f"Source: {product.source_name}\n"
        f"Search term that found it: {product.extra.get('search_term', 'n/a')}"
    )

    try:
        response = client.messages.create(
            model=MODEL,
            max_tokens=600,
            system=SYSTEM,
            messages=[{"role": "user", "content": prompt}],
        )
    except Exception:
        log.exception("Model call failed for %s", product.title[:50])
        return None

    text = "".join(b.text for b in response.content if b.type == "text").strip()
    if text.startswith("```"):
        text = text.split("```")[1]
        if text.startswith("json"):
            text = text[4:]

    try:
        return json.loads(text.strip())
    except json.JSONDecodeError:
        log.warning("Unparseable response for %s: %s", product.title[:40], text[:120])
        return None


def load_catalog() -> dict:
    if not CATALOG.exists():
        return {}
    with CATALOG.open(encoding="utf-8") as f:
        return {item["id"]: item for item in json.load(f)}


def save_catalog(by_id: dict) -> None:
    CATALOG.parent.mkdir(parents=True, exist_ok=True)
    with CATALOG.open("w", encoding="utf-8") as f:
        json.dump(sorted(by_id.values(), key=lambda p: p["id"]), f, indent=2)
        f.write("\n")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", default="seedfile", choices=["seedfile", "ebay"])
    parser.add_argument("--terms", help="file of search terms, one per line (ebay only)")
    parser.add_argument("--per-term", type=int, default=5)
    parser.add_argument("--dry-run", action="store_true",
                        help="print what would change, write nothing")
    args = parser.parse_args()

    if args.source == "ebay":
        from sources.ebay import EbaySource
        source = EbaySource()
        if not args.terms:
            log.error("--terms is required with --source ebay")
            return 1
        terms = [t.strip() for t in Path(args.terms).read_text().splitlines() if t.strip()]
        raw = []
        for term in terms:
            log.info("Searching: %s", term)
            raw.extend(source.fetch(term, args.per_term))
    else:
        source = SeedFileSource()
        raw = source.fetch()

    log.info("Fetched %d products", len(raw))

    client = Anthropic()
    catalog = load_catalog()
    review_rows = []
    added = updated = skipped = 0

    for product in raw:
        pid = product.external_id
        existing = catalog.get(pid, {})

        # A description a human signed off on is never overwritten.
        if existing.get("reviewed"):
            log.info("Keeping reviewed entry: %s", pid)
            # Prices are safe to refresh even on reviewed entries.
            if product.price_low is not None:
                existing["priceLow"] = product.price_low
                existing["priceHigh"] = product.price_high
                existing["priceCheckedOn"] = date.today().isoformat()
                updated += 1
            continue

        result = describe(client, product)
        if result is None:
            skipped += 1
            continue

        entry = {
            "id": pid,
            "name": product.title[:80],
            "category": result.get("category", "physical"),
            "tags": result.get("tags", ""),
            "description": result.get("description", ""),
            "priceLow": product.price_low,
            "priceHigh": product.price_high,
            "priceCheckedOn": date.today().isoformat() if product.price_low else "",
            "buyUrl": product.buy_url,
            "imageUrl": product.image_url,
            "source": product.source_name,
            "reviewed": False,
        }

        confidence = result.get("confidence", "low")
        concerns = result.get("concerns", "").strip()

        if confidence != "high" or concerns:
            review_rows.append({
                "id": pid,
                "name": product.title[:60],
                "confidence": confidence,
                "concerns": concerns or "-",
                "description": result.get("description", ""),
                "tags": result.get("tags", ""),
            })

        if pid in catalog:
            updated += 1
        else:
            added += 1
        catalog[pid] = entry

    if args.dry_run:
        log.info("DRY RUN - nothing written")
    else:
        save_catalog(catalog)
        write_review_queue(review_rows)

    log.info("Added %d, updated %d, skipped %d", added, updated, skipped)
    log.info("%d entries need review -> %s", len(review_rows), REVIEW_QUEUE)
    if review_rows:
        log.warning("Read the review queue BEFORE you commit catalog.json.")
    return 0


def write_review_queue(rows: list[dict]) -> None:
    import csv
    with REVIEW_QUEUE.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(
            f, fieldnames=["id", "name", "confidence", "concerns", "description", "tags"])
        writer.writeheader()
        writer.writerows(rows)


if __name__ == "__main__":
    sys.exit(main())
