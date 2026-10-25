from pydantic import BaseModel, Field


class CatalogItem(BaseModel):
    id: str
    name: str
    category: str = ""
    tags: str = ""
    description: str = ""


class UserContext(BaseModel):
    """Optional, non-clinical profile details - never blood pressure or heart
    rate. Used only to judge physical fit (size/weight limits, grip size),
    never to guess at a condition - see SYSTEM_PROMPT's rules on this."""
    age: int | None = None
    height_feet: int | None = None
    height_inches: int | None = None
    weight_lbs: float | None = None
    biological_sex: str = ""
    activity_level: str = ""


class RecommendRequest(BaseModel):
    """What the Android app sends us."""
    user_text: str = Field(min_length=1, max_length=2000)
    catalog: list[CatalogItem] = Field(min_length=1, max_length=500)
    user_context: UserContext | None = None


class Recommendation(BaseModel):
    id: str
    why: str


class RecommendResponse(BaseModel):
    recommendations: list[Recommendation]
    disclaimer: str = (
        "These are product suggestions, not medical advice. "
        "Nothing here is a diagnosis."
    )
