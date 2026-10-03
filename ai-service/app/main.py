import logging
import secrets

from fastapi import Depends, FastAPI, Header, HTTPException

from .config import settings
from .recommender import recommend
from .schemas import RecommendRequest, RecommendResponse

logging.basicConfig(level=logging.INFO)

app = FastAPI(
    title="Aid Finder AI service",
    description="Called by the Android app. Holds the Anthropic key.",
    version="0.1.0",
)


def require_token(x_app_token: str = Header(default="")):
    """
    Once this service is on the internet, anyone who finds the URL can spend
    your API credits. This is a low bar, not real security - the token ships
    inside the APK and a determined person can extract it. It stops casual
    abuse. Add rate limiting before you let strangers use the app.
    """
    if not secrets.compare_digest(x_app_token, settings.app_token):
        raise HTTPException(status_code=401, detail="Bad or missing app token.")


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/recommend", response_model=RecommendResponse, dependencies=[Depends(require_token)])
def recommend_endpoint(req: RecommendRequest):
    try:
        return RecommendResponse(recommendations=recommend(req))
    except Exception:
        logging.exception("Recommendation failed")
        raise HTTPException(status_code=502, detail="Recommendation engine failed.")
