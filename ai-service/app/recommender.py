"""
All OpenAI API contact lives here.

Design rule: the model chooses ids from a catalog we hand it, and we validate
every id it returns against that same catalog before replying. The Android app
validates a second time against its own copy. Two checks, because a fabricated
product reaching a user is the worst thing this app could do.
"""

import json
import logging

from openai import APIError, OpenAI

from .config import settings
from .prompts import SYSTEM_PROMPT
from .schemas import CatalogItem, Recommendation, RecommendRequest, UserContext

log = logging.getLogger(__name__)
_client: OpenAI | None = None


def _get_client() -> OpenAI:
    """Built on first use, not at import time - importing this module (e.g.
    to test _parse) should not require a real API key to be set."""
    global _client
    if _client is None:
        _client = OpenAI(api_key=settings.openai_api_key)
    return _client


def _catalog_block(catalog: list[CatalogItem]) -> str:
    return "\n".join(
        f"- id={c.id} | {c.name} | {c.category} | tags: {c.tags} | {c.description}"
        for c in catalog
    )


def _context_block(context: UserContext | None) -> str:
    """Empty string if there's nothing to say - never pad the prompt with
    "Age: unknown" noise. Deliberately excludes blood pressure and heart
    rate - see UserContext's docstring."""
    if context is None:
        return ""

    parts = []
    if context.age:
        parts.append(f"Age: {context.age}")
    if context.height_feet or context.height_inches:
        parts.append(f"Height: {context.height_feet or 0} ft {context.height_inches or 0} in")
    if context.weight_lbs:
        parts.append(f"Weight: {context.weight_lbs} lbs")
    if context.biological_sex:
        parts.append(f"Biological sex: {context.biological_sex}")
    if context.activity_level:
        parts.append(f"Activity level: {context.activity_level}")

    if not parts:
        return ""
    return "\n\nThe person's physical details (use only for physical fit):\n" + "\n".join(parts)


def _parse(raw: str) -> list[dict]:
    """Models sometimes wrap JSON in fences even when told not to. Strip them."""
    text = raw.strip()
    if text.startswith("```"):
        text = text.split("```")[1]
        if text.startswith("json"):
            text = text[4:]
    try:
        return json.loads(text.strip()).get("recommendations", [])
    except (json.JSONDecodeError, AttributeError):
        log.warning("Model did not return valid JSON: %s", raw[:200])
        return []


def recommend(req: RecommendRequest) -> list[Recommendation]:
    valid_ids = {c.id for c in req.catalog}

    user_message = (
        f"Catalog:\n{_catalog_block(req.catalog)}\n\n"
        f"The person said:\n{req.user_text}"
        f"{_context_block(req.user_context)}"
    )

    try:
        response = _get_client().chat.completions.create(
            model=settings.model,
            max_tokens=settings.max_tokens,
            # Guarantees syntactically valid JSON. The word "JSON" must appear
            # in one of the messages or the API rejects the request - our
            # system prompt says "Respond with JSON only", so that's covered.
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT.format(max_picks=settings.max_picks)},
                {"role": "user", "content": user_message},
            ],
        )
    except APIError:
        log.exception("OpenAI API call failed")
        raise

    raw_text = response.choices[0].message.content or ""

    picks: list[Recommendation] = []
    for item in _parse(raw_text):
        pid = item.get("id")
        if pid in valid_ids:
            picks.append(Recommendation(id=pid, why=item.get("why", "")))
        else:
            log.warning("Dropped unknown product id from model: %r", pid)

    return picks[: settings.max_picks]
