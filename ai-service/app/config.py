from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Reads from environment variables or a .env file."""

    anthropic_api_key: str = ""
    model: str = "claude-sonnet-4-5"
    max_tokens: int = 1000
    max_picks: int = 3

    # Shared secret the Android app sends in the X-App-Token header.
    # Must match APP_TOKEN in app/build.gradle.
    app_token: str = "change-me-before-you-ship"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
