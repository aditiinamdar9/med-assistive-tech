"""
Reads products from a CSV you maintain by hand.

This source works right now with no credentials, so you can run the whole
pipeline today and see what it produces. The CSV only needs the bare facts -
the AI writes the plain-English description and tags from them.
"""

import csv
from pathlib import Path

from .base import RawProduct


class SeedFileSource:
    name = "seedfile"

    def __init__(self, path: str = "seeds.csv"):
        self.path = Path(path)

    def fetch(self, query: str = "", limit: int = 1000) -> list[RawProduct]:
        if not self.path.exists():
            raise FileNotFoundError(f"No seed file at {self.path}")

        products = []
        with self.path.open(newline="", encoding="utf-8") as f:
            for row in csv.DictReader(f):
                if not row.get("title"):
                    continue
                products.append(RawProduct(
                    external_id=row.get("id") or row["title"].lower().replace(" ", "-")[:24],
                    title=row["title"],
                    source_name=self.name,
                    raw_description=row.get("notes", ""),
                    price_low=_num(row.get("price_low")),
                    price_high=_num(row.get("price_high")),
                    buy_url=row.get("buy_url", ""),
                    brand=row.get("brand", ""),
                ))
        return products[:limit]


def _num(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None
