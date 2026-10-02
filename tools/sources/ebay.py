"""
eBay Browse API source.

Needs an app from developer.ebay.com (free). Put the credentials in .env:

    EBAY_CLIENT_ID=...
    EBAY_CLIENT_SECRET=...

Free tier is 5,000 Browse calls per day, which is far more than this
pipeline will ever use - each search term costs one call.
"""

import base64
import logging
import os

import requests

from .base import RawProduct

log = logging.getLogger(__name__)

TOKEN_URL = "https://api.ebay.com/identity/v1/oauth2/token"
SEARCH_URL = "https://api.ebay.com/buy/browse/v1/item_summary/search"


class EbaySource:
    name = "ebay"

    def __init__(self):
        self.client_id = os.getenv("EBAY_CLIENT_ID", "")
        self.client_secret = os.getenv("EBAY_CLIENT_SECRET", "")
        if not self.client_id or not self.client_secret:
            raise RuntimeError(
                "eBay credentials missing. Set EBAY_CLIENT_ID and EBAY_CLIENT_SECRET "
                "in .env, or run with --source seedfile instead."
            )
        self._token = None

    def _get_token(self) -> str:
        if self._token:
            return self._token

        creds = base64.b64encode(
            f"{self.client_id}:{self.client_secret}".encode()
        ).decode()

        response = requests.post(
            TOKEN_URL,
            headers={
                "Authorization": f"Basic {creds}",
                "Content-Type": "application/x-www-form-urlencoded",
            },
            data={
                "grant_type": "client_credentials",
                "scope": "https://api.ebay.com/oauth/api_scope",
            },
            timeout=20,
        )
        response.raise_for_status()
        self._token = response.json()["access_token"]
        return self._token

    def fetch(self, query: str, limit: int = 10) -> list[RawProduct]:
        response = requests.get(
            SEARCH_URL,
            headers={
                "Authorization": f"Bearer {self._get_token()}",
                "X-EBAY-C-MARKETPLACE-ID": "EBAY_US",
            },
            params={"q": query, "limit": min(limit, 50), "filter": "conditions:{NEW}"},
            timeout=30,
        )
        response.raise_for_status()

        products = []
        for item in response.json().get("itemSummaries", []):
            price = item.get("price", {})
            amount = _num(price.get("value"))
            products.append(RawProduct(
                external_id=item.get("itemId", ""),
                title=item.get("title", ""),
                source_name=self.name,
                raw_description=item.get("shortDescription", ""),
                price_low=amount,
                price_high=amount,
                image_url=item.get("image", {}).get("imageUrl", ""),
                buy_url=item.get("itemWebUrl", ""),
                brand=item.get("brand", ""),
                extra={"search_term": query},
            ))
        return products


def _num(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None
