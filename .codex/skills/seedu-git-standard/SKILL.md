---
name: seedu-git-standard
description: Apply and review Athena commit messages and branch names against the SE-EDU Git conventions. Use whenever creating, proposing, or reviewing a commit or commit message, or creating, renaming, proposing, or reviewing a branch in this project.
---

# SE-EDU Git Standard

Follow the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html) for every commit message and branch name in this project. Treat the rules below as project requirements.

## Commit-message subject

- Write a clear, meaningful subject.
- Aim for at most 50 characters; never exceed 72 characters.
- Use the imperative mood, as in `Add README.md`, not `Added README.md` or `Adding README.md`.
- Capitalize the first letter of the action text.
- Do not end the subject with a period.
- When useful, prefix the action with `<scope>:` or `<category>:`, such as `Person class: Remove static imports` or `chore: Update release date`.

## Commit-message body

Include a body for every non-trivial commit.

- Separate the body from the subject with a blank line.
- Wrap body lines at 72 characters and use blank lines between paragraphs.
- Use bullets when they communicate a list more clearly than prose.
- Explain what changed and why it changed; leave implementation mechanics to the diff.
- Give enough context for a reviewer to judge the change without first reading the diff.
- Describe the existing situation in the present tense, explain why it needs to change, describe the change in the imperative mood, and explain why that approach was chosen. Add other relevant context only when useful.
- Avoid redundant qualifiers such as `currently` and `originally`, and avoid repeating information already captured by code comments in the same commit.
- Split the work into finer-grained commits when a clear body becomes excessively long.

## Branch names

- Use a meaningful kebab-case name made from relevant keywords, such as `refactor-ui-tests`.
- For an issue-related branch, use `issueNumber-some-keywords-from-issue-title`, such as `1234-ui-freeze-error`.

## Workflow

1. Inspect the actual staged changes before creating or reviewing a commit message. If drafting ahead of staging, inspect the intended change instead.
2. Draft or revise the subject and, for a non-trivial change, the body so they satisfy every applicable rule above.
3. Recheck subject length, body wrapping, mood, capitalization, punctuation, and the explanation of what and why before committing.
4. Validate proposed or existing branch names against the branch-name rules before creating or renaming a branch.

Invoking this skill does not authorize a commit or push. Do not commit or push unless the user explicitly requests it.
