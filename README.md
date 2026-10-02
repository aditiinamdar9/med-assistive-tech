# Aid Finder (Android)

An Android app that takes a plain-English description of what someone finds hard
in daily life and suggests assistive products that might help. No medical
jargon, no diagnosis.

**Java** is the Android app. **Python** is a small separate service that does
one job: talk to the AI API. The app calls it over HTTP.

## How the two halves fit together

```
Android app (Java)  ──HTTPS──►  Python service (FastAPI)  ──►  Anthropic API
      │                              │
      │                              └── holds the API key
      └── holds the catalog (assets/catalog.json)
```

Three rules keep this safe:

1. **The API key lives only in Python.** It is never in the APK. Anything shipped
   inside an Android app can be extracted — assume anything in the app is public.
2. **The app owns the catalog.** Python receives the catalog, returns only ids,
   and the app looks those ids up in its own copy. An id the model invented gets
   dropped and never reaches a user. Both sides check this.
3. **The service checks a token.** Once Python is on the internet, anyone who
   finds the URL can spend your credits. `X-App-Token` is a low bar, not real
   security — see the note in `main.py`.

## Repo layout

```
aid-finder-android/
├── android-app/                      open THIS folder in Android Studio
│   ├── settings.gradle
│   ├── build.gradle
│   └── app/
│       ├── build.gradle              deps, SDK versions, AI_BASE_URL, APP_TOKEN
│       └── src/
│           ├── main/
│           │   ├── AndroidManifest.xml
│           │   ├── assets/catalog.json        the product list
│           │   ├── java/com/aidfinder/
│           │   │   ├── MainActivity.java      the one screen
│           │   │   ├── RecommendationViewModel.java   search + id validation
│           │   │   ├── RecommendationResult.java
│           │   │   ├── catalog/               Product, CatalogRepository
│           │   │   ├── net/                   Retrofit setup + DTOs
│           │   │   └── ui/                    RecyclerView adapter
│           │   └── res/                       layouts, strings, colors
│           └── test/                          JVM tests, no emulator needed
├── ai-service/                       FastAPI service
│   ├── app/
│   │   ├── main.py                   /health, /recommend
│   │   ├── recommender.py            Anthropic call + id validation
│   │   ├── prompts.py                the system prompt
│   │   ├── schemas.py                request/response shapes
│   │   └── config.py
│   └── requirements.txt
├── .github/workflows/ci.yml          builds the APK on every PR
├── .env.example
└── docs/api-contract.md
```

## Getting it running

You need **Android Studio** (Ladybug or newer) and **Python 3.11+**.

### 1. Start Python

```bash
cd ai-service
python -m venv .venv && source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp ../.env.example .env      # then paste your real API key into .env
uvicorn app.main:app --port 8001 --reload
```

Check it: `curl localhost:8001/health` should return `{"status":"ok"}`.

### 2. Generate the Gradle wrapper

This template does not ship the wrapper binary. Once, from `android-app/`:

```bash
gradle wrapper --gradle-version 8.7
```

Or just open the folder in Android Studio and let it offer to do this for you.

### 3. Run the app

Open **`android-app`** in Android Studio (not the repo root). Let it sync, then
run on an emulator.

## The emulator vs. real phone thing

This trips up everyone once, so read this bit.

Inside the Android emulator, `localhost` means *the emulator itself*, not your
computer. The emulator reaches your machine at the special address **10.0.2.2**.
That is why `AI_BASE_URL` is `http://10.0.2.2:8001/`.

A real phone cannot use that address at all. To test on a physical device you
need Python reachable on the network. Two options:

- **Same wifi:** run uvicorn with `--host 0.0.0.0`, find your computer's local IP
  (`ipconfig` / `ifconfig`), and set `AI_BASE_URL` to `http://192.168.x.x:8001/`.
  You will also have to add that IP to `res/xml/network_security_config.xml`,
  because Android blocks plain HTTP by default.
- **Deploy it** (better, and required for the release build): push `ai-service`
  to Render, Railway, or Fly.io. They give you an HTTPS URL. Set your
  `ANTHROPIC_API_KEY` and `APP_TOKEN` as environment variables in their
  dashboard — not in the repo. Then put that URL in the `release` block of
  `app/build.gradle`.

Deploy early. It is a one-hour job the first time and it removes a whole
category of "works on my machine" problems.

## Filling in the catalog

`app/src/main/assets/catalog.json` has five products so the app runs. Replace it
with 25–40 real ones. Build the list in a spreadsheet, then export to JSON.

The fields that matter:

- `tags` — describe the **difficulty**, not the condition: `grip-weakness`,
  `low-vision`, `sensory-overload`, `memory`
- `description` — you write this in normal human English. This is where "no
  medical jargon" actually gets enforced. Not in the prompt.

The AI is only as good as this list. It deserves more of your time than the code.

Keeping the catalog in the app means it works offline for browsing and needs no
database. The tradeoff is that changing a product means shipping an app update.
That is the right trade for a school project; if you outgrow it, move the catalog
to the Python side and have the app fetch it.

## Accessibility

This is an app for people with disabilities, so the app itself has to be usable
by them. The template starts you off: labelled inputs, 48dp touch targets,
16sp+ text, cards that read as one unit in a screen reader, and error messages
announced rather than silently displayed.

Before you show it to anyone, turn on **TalkBack** (Settings → Accessibility) and
use the whole app without looking at the screen. Then bump the system font to its
largest setting and check nothing gets cut off. Both tests take five minutes and
both will find real bugs.

## Working as a team

- Nobody pushes to `main`. Branch, open a pull request, get one review.
- CI runs on every PR, runs the tests, and attaches a debug APK you can download
  and install. Do not merge red.
- Split by boundary: one or two on Android, one on Python, one on the catalog.
  The contract in `docs/api-contract.md` is the handshake — agree on it early and
  change it deliberately.

## Safety notes

The app must not read like a medical service. Keep the disclaimer on screen, keep
the "no diagnosis" rules in the system prompt, and keep "an empty list is a valid
answer" — a model that always finds three products will recommend nonsense rather
than admit nothing fits.

Before you demo this, type something distressing into the box and see what comes
back. Decide as a team what the app should do in that case, and build it
deliberately rather than finding out in front of an audience.
