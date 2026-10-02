#!/usr/bin/env python3
"""
Check catalog products against FDA device recalls.

    python check_recalls.py
    python check_recalls.py --since 2023-01-01

Uses the openFDA enforcement endpoint. No API key needed - a key only raises
the rate limit (1,000 requests/day without, 120,000 with). Set OPENFDA_API_KEY
in .env if you have one.

IMPORTANT, read before trusting the output:

This does keyword matching on product descriptions. It is a screening tool,
not an authoritative answer.

  - A hit does NOT mean your product is recalled. It means FDA recalled
    something whose description contains similar words. Different brand,
    different model, probably unrelated. A human must read it.
  - No hits does NOT mean your product is safe. Most consumer assistive aids
    are not FDA-regulated devices at all, so they can never appear here.

Treat hits as "go read this" and never as something to show a user directly.
"""

import argparse
import json
import logging
import os
import sys
import time
from datetime import date
from pathlib import Path
from urllib.parse import quote

import requests
from dotenv import load_dotenv

load_dotenv()
logging.basicConfig(level=logging.INFO, format="%(levelname)s  %(message)s")
log = logging.getLogger("recalls")

CATALOG = Path("../android-app/app/src/main/assets/catalog.json")
REPORT = Path("recall_report.md")
ENDPOINT = "https://api.fda.gov/device/enforcement.json"

# Words that match half the catalog if you search them. Dropped from queries.
STOPWORDS = {
    "the", "and", "for", "with", "your", "you", "set", "pack", "kit", "pcs",
    "new", "pro", "plus", "premium", "adjustable", "portable", "large", "small",
    "inch", "size", "color", "black", "white", "blue", "red",
}


def search_terms(product: dict) -> str:
    """Pull the most distinctive words out of a product name."""
    words = [
        w.strip(".,()-").lower()
        for w in product.get("name", "").split()
        if len(w) > 3 and w.strip(".,()-").lower() not in STOPWORDS
    ]
    return " ".join(words[:4])


def query_fda(terms: str, since: str | None) -> list[dict]:
    if not terms:
        return []

    search = f'product_description:"{terms}"'
    if since:
        search += f"+AND+recall_initiation_date:[{since.replace('-', '')}+TO+99991231]"

    params = {"search": search, "limit": 5}
    key = os.getenv("OPENFDA_API_KEY")
    if key:
        params["api_key"] = key

    try:
        response = requests.get(ENDPOINT, params=params, timeout=20)
    except requests.RequestException:
        log.warning("Request failed for %r", terms)
        return []

    # openFDA returns 404 for "no matches found". That is a normal result.
    if response.status_code == 404:
        return []
    if response.status_code == 429:
        log.error("Rate limited. Get a free key at open.fda.gov/apis/authentication")
        return []
    if not response.ok:
        log.warning("openFDA returned %d for %r", response.status_code, terms)
        return []

    return response.json().get("results", [])


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--since", help="only recalls initiated after this date (YYYY-MM-DD)")
    parser.add_argument("--delay", type=float, default=0.3, help="seconds between requests")
    args = parser.parse_args()

    if not CATALOG.exists():
        log.error("No catalog at %s", CATALOG)
        return 1

    with CATALOG.open(encoding="utf-8") as f:
        products = json.load(f)

    log.info("Checking %d products against FDA enforcement reports", len(products))

    findings = []
    for product in products:
        terms = search_terms(product)
        results = query_fda(terms, args.since)
        if results:
            log.warning("%d possible match(es) for %s", len(results), product["id"])
            findings.append((product, terms, results))
        time.sleep(args.delay)

    write_report(findings, len(products), args.since)
    log.info("%d products flagged -> %s", len(findings), REPORT)
    if findings:
        log.warning("These are keyword matches, not confirmed recalls. Read them.")
    return 0


def write_report(findings, total, since) -> None:
    lines = [
        "# FDA recall screening",
        "",
        f"Run on {date.today().isoformat()} against {total} products.",
        f"Date filter: recalls since {since}." if since else "Date filter: none.",
        "",
        "**These are keyword matches on FDA product descriptions, not confirmed",
        "recalls of your products.** A match usually means FDA recalled a different",
        "item with similar words in its description. Read each one and decide.",
        "",
        "Equally: no match does not mean safe. Most consumer assistive aids are not",
        "FDA-regulated devices and never appear in this database at all.",
        "",
    ]

    if not findings:
        lines += ["## No matches", "", "Nothing in the catalog matched a recall record."]
    else:
        for product, terms, results in findings:
            lines += [
                f"## {product['name']}",
                "",
                f"- Catalog id: `{product['id']}`",
                f"- Searched for: `{terms}`",
                "",
            ]
            for r in results:
                lines += [
                    f"**{r.get('recalling_firm', 'Unknown firm')}** "
                    f"({r.get('recall_initiation_date', 'date unknown')}, "
                    f"class {r.get('classification', '?')})",
                    "",
                    f"- Product: {r.get('product_description', '')[:300]}",
                    f"- Reason: {r.get('reason_for_recall', '')[:300]}",
                    f"- Status: {r.get('status', 'unknown')}",
                    "",
                ]

    REPORT.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    sys.exit(main())
