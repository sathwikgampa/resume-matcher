# Data Science Methodology

This document walks through **every** data science concept used in this project, with a small worked example for each — including why each choice was made over the ML/AI alternative, and why match scores land at the specific percentages they do.

---

## 1. Tokenization (Turning Text into Data)

**What it is:** Breaking a sentence into individual words (tokens) that can be counted and compared.

**Example:**
```
Input:  "We need a Senior Java Developer!"
Output: ["we", "need", "a", "senior", "java", "developer"]
```
Lowercasing and stripping punctuation first ensures `"Java"`, `"java,"`, and `"JAVA"` are all treated as the same token — otherwise a computer would count them as three unrelated words.

**Why not an NLP library (spaCy, NLTK)?** Those libraries do the same thing (and more, like grammar tagging), but require downloading and loading external packages. Basic tokenization is just string splitting — Java's built-in `.toLowerCase()`, `.replaceAll()`, and `.split()` handle it in three lines with zero installs.

---

## 2. Stopword Removal (Filtering Noise)

**What it is:** Removing common words that carry no distinguishing signal — words that appear in almost every sentence regardless of topic.

**Example:**
```
Before: ["we", "need", "a", "senior", "java", "developer"]
After:  ["senior", "java", "developer"]
```
`"we"`, `"need"`, and `"a"` are stripped because they'd appear in a job posting for a chef, a nurse, or a developer equally — they don't help distinguish one job/resume from another.

**Why this matters for scoring:** Without stopword removal, two completely unrelated documents (a tech job and a finance resume) would still show artificially high similarity, because they both contain "the," "and," "with," etc. Removing them forces the score to reflect actual subject-matter overlap.

---

## 3. Term Frequency (TF) Vectors — Turning Words into Numbers

**What it is:** Counting how many times each surviving keyword appears, turning a sentence into a `HashMap<String, Integer>`.

**Example:**
```
Text: "java developer java backend developer"
TF Vector: {java: 2, developer: 2, backend: 1}
```

This is called a **bag-of-words** model — grammar and word order are discarded entirely. `"developer java"` and `"java developer"` produce the identical vector. That's a real limitation (explained in Section 8), but it's what makes the math simple and fully traceable.

---

## 4. The Shared Vector Space (Master Keyword Set)

**What it is:** To compare two documents mathematically, both need to exist on the same "map." The master set is the union of every unique keyword from both documents — each keyword becomes one axis/dimension.

**Example:**
```
Job posting tokens:  {java, developer, spring}
Resume tokens:        {java, developer, sql}

Master set (shared axes): {java, developer, spring, sql}

Job vector:    java=1, developer=1, spring=1, sql=0
Resume vector: java=1, developer=1, spring=0, sql=1
```
Notice both vectors now have a value (even if zero) for every axis — that's what makes them comparable as vectors in the same space.

---

## 5. Cosine Similarity — The Core Formula

**What it is:** A way to measure how similar two vectors are by calculating the **angle** between them, ignoring their length/magnitude.

**Formula:**
```
similarity = (A · B) / (‖A‖ × ‖B‖)
```

**Worked example**, using the vectors from Section 4:
```
Job vector A:    [java=1, developer=1, spring=1, sql=0]
Resume vector B: [java=1, developer=1, spring=0, sql=1]

Dot product (A·B):
  = (1×1) + (1×1) + (1×0) + (0×1)
  = 1 + 1 + 0 + 0
  = 2

Magnitude of A: sqrt(1² + 1² + 1² + 0²) = sqrt(3) ≈ 1.732
Magnitude of B: sqrt(1² + 1² + 0² + 1²) = sqrt(3) ≈ 1.732

Similarity = 2 / (1.732 × 1.732) = 2 / 3 ≈ 0.667 → 66.7%
```

**Why cosine similarity instead of just counting shared words?** Raw overlap count (here, "2 words matched") doesn't tell you if that's a *good* match or not — 2 shared words out of 4 total is very different from 2 shared words out of 400. Cosine similarity automatically accounts for the total size of each document by dividing out the magnitude, so a short resume isn't unfairly penalized (or a long one unfairly boosted) just because of its length.

---

## 6. Why Data Science / Statistical Methods Instead of Machine Learning Models

This is worth being explicit about, since it's the central design decision of the whole project.

| Question | Statistical approach (used here) | ML/AI approach (not used) |
|---|---|---|
| How is similarity measured? | Cosine similarity — a fixed geometric formula | A trained neural network predicts a similarity score |
| Can you explain any single result? | Yes — every number traces back to a word count | Often no — "the model just learned it" from millions of parameters |
| What if the score seems wrong? | You can pull up the exact dot product and magnitude and check the math by hand | You'd need to inspect model weights or retrain — not practical to explain in an interview |
| Setup cost | Zero — pure Java, runs instantly | Requires downloading a pretrained model (often gigabytes), a Python environment, or an API key |
| Consistency | Same input always produces the exact same output | Some models (especially LLMs) can produce slightly different outputs on repeated runs |

**Concrete example of the difference:** If you ask an LLM "how well does this resume match this job," it might answer "72% — strong alignment" with no way to verify that number. If you ask *this* system the same question, you get: "72.3% — because these 9 keywords overlapped out of these 14 total, and here's the exact multiplication." One is a black box; the other is a math problem you can check with a calculator.

This isn't a claim that statistical methods are *better* than ML — modern embedding models genuinely capture meaning (synonyms, context) that word-counting can't. The point of this project is that the classical method is **fully transparent and cheap**, which is valuable for a use case (resume screening) where explainability and auditability matter.

---

## 7. Why Not TF-IDF (a Reasonable Next Step, Explained)

**TF-IDF** (Term Frequency–Inverse Document Frequency) is a step up from raw counting: it down-weights common words and up-weights rare, distinguishing words — even after stopword removal.

**Example of the problem TF-IDF solves:**
```
Job posting keywords (after stopword removal): "experience", "team", "kubernetes"
```
`"experience"` and `"team"` appear in *almost every* job posting ever written, while `"kubernetes"` appears in far fewer. Raw TF treats all three as equally important (each counted once). TF-IDF would recognize `"kubernetes"` as a much stronger, more distinguishing signal and weight it higher.

**Why this project doesn't use it:** TF-IDF's "inverse document frequency" weight needs to be calculated across *many* documents (a whole corpus of job postings) to know which words are actually rare versus common. Comparing just one resume against one job posting doesn't give enough data to compute a meaningful IDF weight. It's a legitimate next upgrade if you had, say, 100 job postings to compare against — worth mentioning as a stated future improvement, not a missing feature.

---

## 8. Why Scores Land at ~40–70% for Genuine Matches, Not Higher

This is the question people ask most often, so here's the direct answer with the actual numbers from this project's test run.

**Job posting:**
`hiring, senior, java, developer, strong, experience, spring, boot, rest, apis, sql, databases, microservices, architecture, familiarity, docker, kubernetes, plus` (18 unique keywords)

**Alice's resume:**
`backend, developer, years, building, rest, apis, using, spring, boot, working, sql, databases, docker, containers` (14 unique keywords)

**Shared keywords (8):** developer, rest, apis, spring, boot, sql, databases, docker

**Resulting score: 50.40%**

Why not 100%, given 8 solid technical matches? Because cosine similarity measures the *entire* vector, not just the overlap:
- The job posting has 10 keywords Alice's resume doesn't repeat: `hiring, senior, java, strong, experience, microservices, architecture, familiarity, kubernetes, plus`
- Alice's resume has 6 keywords the job posting doesn't contain: `backend, years, building, using, working, containers`

Each of those 16 non-overlapping keywords pulls the vector angle further apart, even though the 8 shared ones represent a genuinely strong technical match. **A score in the 40–70% range with most of the hard skills overlapping is a strong, credible result** — this is standard behavior for bag-of-words cosine similarity between two differently-worded documents describing the same underlying reality (a job vs. a person's experience).

A score close to 95–100% would actually be a red flag — it would mean the resume nearly echoes the job posting's own phrasing word-for-word, which reads as keyword-stuffing rather than genuine experience.

---

## 9. Known Statistical Limitations (Stated Honestly)

| Limitation | Example | Why it happens |
|---|---|---|
| No synonym awareness | "JS" and "JavaScript" scored as unrelated | Each token is compared as an exact string, not a concept |
| No stemming | "manage," "managed," "managing" counted as 3 separate keywords | No grammatical root-word reduction is applied |
| No implied-skill detection | A resume listing "Spring Boot" doesn't register a match for "java" in the job posting | The system only counts literal token overlap, never infers related technologies |
| Sensitive to phrasing style | Two resumes with identical actual experience but different wording can score differently | Cosine similarity operates on word choice, not underlying meaning |

These aren't hidden flaws — they're the direct, explainable tradeoff of choosing a transparent, zero-dependency statistical method over a heavier NLP/ML pipeline that would understand meaning but sacrifice auditability.