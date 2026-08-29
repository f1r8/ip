---
name: seedu-java-coding-standard
description: Apply and review Athena Java code against the SE-EDU basic and intermediate Java coding standard. Use whenever creating, editing, refactoring, or reviewing Java source or test code in this project.
---

# SE-EDU Java Coding Standard

Apply the SE-EDU basic and intermediate Java rules to every Java file in this project, including tests.

Before working on Java code, read [references/rules.md](references/rules.md) completely. Treat it as the project checklist. When the checklist does not cover a topic, follow the Google Java Style Guide linked there. Project-specific conventions in the checklist override that fallback; direct SE-EDU rules still take precedence.

## Workflow

1. Identify every Java file affected by the requested change.
2. Write code that follows the checklist while preserving the user's intended behavior.
3. Review the affected files for naming, layout, imports, statement structure, and comments before verification.
4. When asked for repository-wide conformance, audit every Java source and test file, not only files already being changed.

Do not make unrelated behavior or API changes solely to achieve stylistic uniformity. Use the repository's required verification workflow after code changes.
