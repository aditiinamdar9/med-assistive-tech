#!/usr/bin/env python3
"""
Manual test harness for the /recommend endpoint - not part of the app.

Loads the real catalog the Android app ships, lets you type a problem the
way a user would, and prints back whatever the AI service recommends.

Run `uvicorn app.main:app --port 8001` first, then:
    python chat_cli.py
"""

import json
import os
import sys
from pathlib import Path

import httpx
from dotenv import load_dotenv

load_dotenv()

CATALOG_PATH = Path("../android-app/app/src/main/assets/catalog.json")
SERVICE_URL = "http://localhost:8001/recommend"
APP_TOKEN = os.getenv("APP_TOKEN", "change-me-before-you-ship")


def main() -> int:
    if not CATALOG_PATH.exists():
        print(f"Can't find catalog at {CATALOG_PATH}", file=sys.stderr)
        return 1
    catalog = json.loads(CATALOG_PATH.read_text())

    print(f"Loaded {len(catalog)} products. Describe what's hard for you (blank line to quit).\n")

    while True:
        try:
            user_text = input("> ").strip()
        except (EOFError, KeyboardInterrupt):
            print()
            return 0

        if not user_text:
            return 0

        try:
            response = httpx.post(
                SERVICE_URL,
                headers={"X-App-Token": APP_TOKEN},
                json={"user_text": user_text, "catalog": catalog},
                timeout=30,
            )
        except httpx.ConnectError:
            print("Can't reach the service - is uvicorn running on port 8001?\n")
            continue

        if response.status_code != 200:
            print(f"[{response.status_code}] {response.text}\n")
            continue

        data = response.json()
        picks = data["recommendations"]
        if not picks:
            print("(no matches)\n")
            continue

        by_id = {item["id"]: item for item in catalog}
        for pick in picks:
            name = by_id.get(pick["id"], {}).get("name", pick["id"])
            print(f"- {name}: {pick['why']}")
        print(f"\n{data['disclaimer']}\n")

    return 0


if __name__ == "__main__":
    sys.exit(main())
