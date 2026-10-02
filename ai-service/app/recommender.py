"""
All Anthropic API contact lives here.

Design rule: the model chooses ids from a catalog we hand it, and we validate
every id it returns against that same catalog before replying. The Android app
validates a second time against its own copy. Two checks, because a fabricated
product reaching a user is the worst thing this app could do.
"""

import json
import logging

from anthropic import Anthropic, APIError

from .config import settings
from .prompts import SYSTEM_PROMPT
from .schemas import CatalogItem, Recommendation, RecommendRequest

log = logging.getLogger(__name__)
_client = Anthropic(api_key=settings.anthropic_api_key)


def _catalog_block(catalog: list[CatalogItem]) -> str:
    return "\n".join(
        f"- id={c.id} | {c.name} | {c.category} | tags: {c.tags} | {c.description}"
        for c in catalog
    )


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
    )

    try:
        response = _client.messages.create(
            model=settings.model,
            max_tokens=settings.max_tokens,
            system=SYSTEM_PROMPT.format(max_picks=settings.max_picks),
            messages=[{"role": "user", "content": user_message}],
        )
    except APIError:
        log.exception("Anthropic API call failed")
        raise

    raw_text = "".join(
        block.text for block in response.content if block.type == "text"
    )

    picks: list[Recommendation] = []
    for item in _parse(raw_text):
        pid = item.get("id")
        if pid in valid_ids:
            picks.append(Recommendation(id=pid, why=item.get("why", "")))
        else:
            log.warning("Dropped unknown product id from model: %r", pid)

    return picks[: settings.max_picks]
