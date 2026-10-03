# Catalog tools

Two scripts that run on your laptop or in CI. Neither is part of the Android
app, and neither runs when a user taps anything.

## Setup

```bash
cd tools
python3 -m venv .venv && source .venv/bin/activate
python -m pip install -r requirements.txt
cp .env.template .env       # then add ONE api key
```

### Which model provider

The pipeline works with either Anthropic or OpenAI. Put one key in `.env`:

```
ANTHROPIC_API_KEY=sk-ant-...     # or
OPENAI_API_KEY=sk-proj-...
```

Whichever is present gets used. If both are, set `LLM_PROVIDER=openai` to
choose. Override the model with `LLM_MODEL=` — defaults are
`claude-sonnet-4-5` and `gpt-4o-mini`.

All of this lives in `llm.py`. The rest of the pipeline does not know which
provider is in use, so switching costs you one line in `.env`.

## enrich_catalog.py

Takes raw product data, has Claude write the plain-English description and
tags, writes `catalog.json`, and flags anything questionable for you.

```bash
python enrich_catalog.py --source seedfile              # works now, no credentials
python enrich_catalog.py --source seedfile --dry-run    # see it without writing
python enrich_catalog.py --source ebay --terms terms.example.txt --per-term 5
```

### The `reviewed` flag

Every entry gets `"reviewed": false`. When you have read a description and you
are happy with it, change it to `true` by hand. After that the script will never
overwrite that description again — it only refreshes the price.

This is what stops a re-run from silently undoing your edits. It matters more
than it sounds like it does.

### The review queue

Anything the model rated below high confidence, or raised a concern about, lands
in `review_queue.csv`. Open it in a spreadsheet before you commit.

The model is told to flag products that look prescription-only or clinical, that
make medical claims, that do not match the search term, or that look unsafe. It
will not catch everything. The queue is where you catch the rest.

### Adding a different source

Subclass `ProductSource` in `sources/`, implement `fetch()`, return
`RawProduct` objects. Nothing else changes.

## check_recalls.py

Screens the catalog against FDA device recalls via the openFDA enforcement
endpoint.

```bash
python check_recalls.py
python check_recalls.py --since 2023-01-01
```

Writes `recall_report.md`.

**Read the warnings in that file.** This does keyword matching on FDA product
descriptions. A hit usually means FDA recalled a *different* product with
similar words — not yours. And no hit does not mean safe, because most consumer
assistive aids are not FDA-regulated devices and never appear in that database.

Never show this output to a user. It is a prompt for a human to go read
something, nothing more.

No API key needed. openFDA allows 1,000 requests per day without one and
120,000 with. Get a free key at open.fda.gov/apis/authentication and put it in
`.env` as `OPENFDA_API_KEY` if you start hitting limits.

## The workflow

1. Add search terms or seed rows
2. Run `enrich_catalog.py`
3. Open `review_queue.csv`, fix descriptions, mark good ones `"reviewed": true`
4. Run `check_recalls.py`, read `recall_report.md`
5. Commit `catalog.json`
6. Prices refresh weekly in CI; you review the pull request

Step 3 is the one that makes the app good. The script gets you a first draft at
scale — it does not get you a finished catalog.
