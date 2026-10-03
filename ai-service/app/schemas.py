from pydantic import BaseModel, Field


class CatalogItem(BaseModel):
    id: str
    name: str
    category: str = ""
    tags: str = ""
    description: str = ""


class RecommendRequest(BaseModel):
    """What the Android app sends us."""
    user_text: str = Field(min_length=1, max_length=2000)
    catalog: list[CatalogItem] = Field(min_length=1, max_length=500)


class Recommendation(BaseModel):
    id: str
    why: str


class RecommendResponse(BaseModel):
    recommendations: list[Recommendation]
    disclaimer: str = (
        "These are product suggestions, not medical advice. "
        "Nothing here is a diagnosis."
    )
