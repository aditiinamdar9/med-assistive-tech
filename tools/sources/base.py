"""
A product source hands back raw products. Nothing more.

Write a new source by subclassing ProductSource and implementing fetch().
enrich_catalog.py does not care where products come from, so swapping eBay
for Amazon, Walmart or a spreadsheet touches only this folder.
"""

from dataclasses import dataclass, field
from typing import Protocol


@dataclass
class RawProduct:
    """A product as the source gave it to us, before any AI touches it."""

    external_id: str          # the source's own id
    title: str                # usually retailer marketing copy
    source_name: str          # "ebay", "seedfile", etc.
    raw_description: str = ""
    price_low: float | None = None
    price_high: float | None = None
    image_url: str = ""
    buy_url: str = ""
    brand: str = ""
    extra: dict = field(default_factory=dict)


class ProductSource(Protocol):
    name: str

    def fetch(self, query: str, limit: int) -> list[RawProduct]:
        """Return products matching a search term."""
        ...
