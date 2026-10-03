"""Tests that spend no API credits. Run with: pytest"""

from app.recommender import _parse


def test_parses_plain_json():
    raw = '{"recommendations": [{"id": "grip-001", "why": "Easier to hold."}]}'
    assert _parse(raw)[0]["id"] == "grip-001"


def test_parses_fenced_json():
    raw = '```json\n{"recommendations": [{"id": "vis-001", "why": "Bigger print."}]}\n```'
    assert _parse(raw)[0]["id"] == "vis-001"


def test_bad_json_returns_empty():
    assert _parse("sorry, I cannot help with that") == []
