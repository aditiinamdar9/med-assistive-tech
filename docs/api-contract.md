# Contract between Android and Python

One endpoint that matters. If you change this, change
`ai-service/app/schemas.py` and `android-app/.../net/dto/` in the same pull request.

## POST /recommend

Header: `X-App-Token: <the shared token>`

Request:

```json
{
  "user_text": "Opening jars and holding a pen makes my hands ache.",
  "catalog": [
    {
      "id": "grip-001",
      "name": "Weighted utility grips",
      "category": "physical",
      "tags": "grip-weakness,arthritis,hands",
      "description": "Chunky rubber handles that slide onto forks and pens."
    }
  ],
  "user_context": {
    "age": 34,
    "height_feet": 5,
    "height_inches": 6,
    "weight_lbs": 150,
    "biological_sex": "Female",
    "activity_level": "Moderately active"
  }
}
```

`user_context` is optional and the app omits it entirely when the user has no
profile data set (not sent as an empty object). It deliberately never
includes blood pressure or heart rate - those stay on the device. The model
is instructed to use it only to judge physical fit (size, weight limits,
grip size), never to guess at a condition. If you add a field here that
should also stay off-device, don't add it to `UserContext` in `schemas.py` or
`UserContextDto.java` - keep it out of both.

Response:

```json
{
  "recommendations": [
    { "id": "grip-001", "why": "These slide onto things you already own and give you something thicker to hold." }
  ],
  "disclaimer": "These are product suggestions, not medical advice."
}
```

An empty `recommendations` list is a valid, successful response. The app shows a
"nothing matched" message, not an error.

Note the naming mismatch: Python uses `user_text` (snake_case), Java uses
`userText` (camelCase). The `@SerializedName("user_text")` annotation in
`RecommendRequest.java` bridges them. If you add a snake_case field, it needs
the same annotation or it will silently arrive as null.

## GET /health

Returns `{"status": "ok"}`. No token needed. Use it to tell "service is down"
apart from "service failed on this request".

## Error codes

| Code | Meaning | What the app does |
|---|---|---|
| 401 | Token missing or wrong | Bug in your config — check both files match |
| 422 | Request did not match schema | Bug on the Android side |
| 502 | The model call failed | Show "try again in a minute" |
