"""
One function that talks to whichever model provider you have a key for.

Set exactly one of these in tools/.env:

    ANTHROPIC_API_KEY=sk-ant-...
    OPENAI_API_KEY=sk-proj-...

If both are present, ANTHROPIC wins unless you set LLM_PROVIDER=openai.

Optionally override the model:

    LLM_MODEL=gpt-4o-mini

Nothing else in the pipeline knows or cares which provider is in use.
"""

import json
import logging
import os

log = logging.getLogger(__name__)

DEFAULT_MODELS = {
    "anthropic": "claude-sonnet-4-5",
    "openai": "gpt-4o-mini",
}


def _pick_provider() -> str:
    explicit = os.getenv("LLM_PROVIDER", "").strip().lower()
    if explicit in DEFAULT_MODELS:
        return explicit

    if os.getenv("ANTHROPIC_API_KEY"):
        return "anthropic"
    if os.getenv("OPENAI_API_KEY"):
        return "openai"

    raise RuntimeError(
        "No API key found. Put one of these in tools/.env:\n"
        "  ANTHROPIC_API_KEY=sk-ant-...\n"
        "  OPENAI_API_KEY=sk-proj-..."
    )


class LLM:
    """Ask a model a question, get JSON back. Provider-agnostic."""

    def __init__(self):
        self.provider = _pick_provider()
        self.model = os.getenv("LLM_MODEL") or DEFAULT_MODELS[self.provider]

        if self.provider == "anthropic":
            from anthropic import Anthropic
            self._client = Anthropic()
        else:
            from openai import OpenAI
            self._client = OpenAI()

        log.info("Using %s / %s", self.provider, self.model)

    def ask_json(self, system: str, user: str, max_tokens: int = 600) -> dict | None:
        """Returns a parsed dict, or None if the call or the parse failed."""
        try:
            raw = (self._ask_anthropic if self.provider == "anthropic"
                   else self._ask_openai)(system, user, max_tokens)
        except Exception:
            log.exception("%s call failed", self.provider)
            return None

        return _parse_json(raw)

    def _ask_anthropic(self, system: str, user: str, max_tokens: int) -> str:
        response = self._client.messages.create(
            model=self.model,
            max_tokens=max_tokens,
            system=system,
            messages=[{"role": "user", "content": user}],
        )
        return "".join(b.text for b in response.content if b.type == "text")

    def _ask_openai(self, system: str, user: str, max_tokens: int) -> str:
        response = self._client.chat.completions.create(
            model=self.model,
            max_tokens=max_tokens,
            # Guarantees syntactically valid JSON. The word "JSON" must appear
            # in one of the messages or the API rejects the request - our system
            # prompt ends with a JSON shape, so that is covered.
            response_format={"type": "json_object"},
            messages=[
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
        )
        return response.choices[0].message.content or ""


def _parse_json(raw: str) -> dict | None:
    """Models sometimes wrap JSON in fences even when told not to."""
    text = (raw or "").strip()
    if text.startswith("```"):
        parts = text.split("```")
        text = parts[1] if len(parts) > 1 else text
        if text.startswith("json"):
            text = text[4:]

    try:
        result = json.loads(text.strip())
        return result if isinstance(result, dict) else None
    except json.JSONDecodeError:
        log.warning("Model returned unparseable JSON: %s", text[:150])
        return None
