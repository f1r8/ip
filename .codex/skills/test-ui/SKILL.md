---
name: test-ui
description: Build Athena and run its command-line UI test cases from test/ui-test-plan.md, comparing console output, stopping at the first failure, and recording the full session. Use after code changes or when given UI commands and expected outputs for this project.
---

# Test Athena's UI

Use the repository's Markdown test plan as the durable source of UI acceptance cases. The current Codex workspace may open one directory above the Git repository; locate the directory containing both `build.gradle` and `test/ui-test-plan.md` and treat it as the repository root.

## Maintain the plan

Before running tests, review `test/ui-test-plan.md` against the code change and the user's requested behavior.

- Translate each supplied command/expected-output pair into an ordered `## Test case N: <name>` section. Normally use one fresh Athena process per case; put multiple input lines in one case only when the aim depends on a stateful command sequence.
- Every case must contain `Aim`, `Inputs`, `Expected output`, and optionally `Expected exit code` (default `0`).
- Keep the plan-level build and program commands accurate. The runner supports `{repo}` in commands so the JAR can be launched from an isolated working directory.
- Preserve useful regression cases. Update inputs or expected output only when the intended behavior changed; do not copy unexpected actual output into the plan merely to make a failure pass.
- Derive expected output from the user's requirement, an existing accepted test, or clearly intended code behavior. Ask the user if the expected behavior is genuinely ambiguous.

The runner accepts `Input` and `Inputs` as equivalent field names. A test case may include its own `Command`; otherwise it uses the plan-level `Program command`.

## Run the plan

From the repository root, verify that `java --version` reports Java 25, then run:

```text
python .codex/skills/test-ui/scripts/run-ui-tests.py
```

On this Windows workspace, `python` may not be on `PATH`; use the existing workspace interpreter instead:

```text
..\.venv\Scripts\python.exe .codex\skills\test-ui\scripts\run-ui-tests.py
```

The runner builds the current JAR once, then launches every case in a new temporary working directory. This prevents `data/athena.txt` from changing the initial output or overwriting the user's saved tasks. It combines stdout and stderr, compares output after normalizing line endings, invisible trailing whitespace, and final blank lines, and checks the exit code.

Do not use `--skip-build` for post-code-update verification. It is only for diagnosing the runner after an equivalent current build has already succeeded.

## Fail fast and report

- Stop immediately on a build error, timeout, unexpected exit code, or output mismatch. Never run or claim success for later cases after a failure.
- The runner writes `test/ui-test-session.md`. Show that record after every run; it contains the build output and, for each executed case, the command, console input, actual output, expected output, exit code, and result.
- On a failed case, report its command, failure reason, actual output, and expected output verbatim from the session record.
- A parsing error is a failed test setup. Correct the plan rather than bypassing it.

## Resource

Use `scripts/run-ui-tests.py` for plan parsing, a current Gradle build, isolated execution, output comparison, fail-fast behavior, and transcript generation. It requires only Python's standard library.
