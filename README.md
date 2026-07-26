# Resume Matcher & Job Scraper

An end-to-end resume-to-job matching pipeline built entirely in Java — no external libraries, no ML frameworks, no API keys. It scrapes a live job posting (or accepts pasted text), extracts keywords, converts resumes and job descriptions into numerical vectors, and scores their similarity using cosine similarity.

## Why this exists

Most resume matchers either cost money (API-based AI scoring) or hide their logic behind a black box. This project does the opposite: every step — from tokenization to the final percentage score — is plain, auditable Java code you can read top to bottom in a few minutes.

## Features

- **Live job scraping**: pulls text directly from a job posting URL
- **Keyword extraction**: strips noise (stopwords, punctuation, formatting) down to meaningful terms
- **Cosine similarity scoring**: mathematically compares a resume against a job description, 0–100%
- **Key Strengths / Critical Gaps breakdown**: shows exactly which keywords matched and which are missing
- **Zero dependencies**: runs on the JDK alone — no Maven, no Gradle, no third-party packages

## Requirements

- Java 11 or higher

## Project Structure

```
src/
├── TextProcessor.java     # Tokenization, stopword removal, normalization
├── SimilarityEngine.java  # Frequency vectors, cosine similarity, keyword breakdown
├── WebScraper.java        # Fetches and cleans job posting HTML
└── Main.java              # Wires everything together, prints ranked results
```

## How to Run

1. Clone the repo:
   ```bash
   git clone https://github.com/YOUR_USERNAME/resume-matcher-java.git
   cd resume-matcher-java
   ```
2. Open in IntelliJ IDEA (or compile manually):
   ```bash
   javac src/*.java -d out
   java -cp out Main
   ```
3. To test against a **live job posting**, open `Main.java`, set:
   ```java
   boolean useLiveScraping = true;
   String jobUrl = "https://boards.greenhouse.io/company/jobs/12345";
   ```
   Static, non-JS-rendered job boards (Greenhouse, Lever) scrape reliably. Sites like LinkedIn actively block non-browser requests and are not supported — and scraping them would violate their Terms of Service.

## Sample Output

```
Candidate                 Matches    Score
---------------------------------------------
Alice - Java Engineer     8          50.40
Bob - Graphic Designer    0          0.00
Carol - Finance Analyst   0          0.00

=== Detailed Breakdown ===

Alice - Java Engineer
  Score: 50.40%
  Key Strengths: [apis, boot, databases, developer, docker, rest, spring, sql]
  Critical Gaps: [architecture, experience, kubernetes, microservices, senior, ...]
```

## Architecture Overview

| Standard AI/ML Concept | What This Project Uses Instead |
|---|---|
| LLM / Transformer parsing | Native regex + string tokenization |
| NLP libraries (spaCy, NLTK) | Hardcoded `HashSet` stopword filtering |
| Embeddings / CountVectorizer | `HashMap<String, Integer>` frequency vectors |
| Neural similarity scoring | Cosine similarity via `java.lang.Math` |

For the reasoning behind these choices — including why scores land where they do — see [`DATA_SCIENCE_METHODOLOGY.md`](./DATA_SCIENCE_METHODOLOGY.md).

## Known Limitations

- **Exact string matching only**: "Java" and "java developer" match, but doesn't understand synonyms (e.g. "JS" vs "JavaScript") or implied skills
- **No semantic understanding**: this is a keyword-overlap tool, not a language-understanding system
- **JS-rendered pages won't scrape**: only static HTML is captured; content injected by JavaScript after page load is invisible to this scraper

## License

MIT