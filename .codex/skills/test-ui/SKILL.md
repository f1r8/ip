---
name: test-ui
description: Run command-driven athena.ui.Ui or console tests from a project test plan, compare each program's combined console output with its expected output, stop immediately on the first failure, and record the console input, output, and result. Use when a user provides lists of commands and expected outputs or asks to exercise a CLI/athena.ui.Ui program reproducibly.
---

# Test athena.ui.Ui

Run the project's command-driven athena.ui.Ui test plan and preserve a readable test-session record.

## Prepare the plan

1. Work from the repository root.
2. Create or update `test/ui-test-plan.md` before executing anything. Keep one `## Test case N: <name>` section per command so a list of commands becomes an ordered list of test cases.
3. Give every test case all of these fields:
   - `Aim`: the behavior being verified.
   - `Command`: one shell command to execute.
   - `Input`: the exact text sent to standard input; use an empty fenced block when no input is required.
   - `Expected output`: the exact combined console output expected from the command.
   - Optionally, `Expected exit code`; it defaults to `0`.
4. Record relevant prerequisites in the plan, such as the build command, Java/runtime version, working-directory assumptions, and any environment variables. Do not invent expected output; ask for it when the user has not supplied it and it cannot be established from a trusted requirement.

Use this shape for each case:

```markdown
## Test case 1: Reject an unknown command

- Aim: Verify that an invalid command produces the documented error.
- Command:
  ```text
  java -cp out/production/ip duke.Main
  ```
- Input:
  ```text
  invalid-command
  ```
- Expected output:
  ```text
  I don't know what that means :-/
  ```
- Expected exit code: 0
```

## Run the plan

Run the bundled standard-library-only runner from the repository root:

```text
python .codex/skills/test-ui/scripts/run-ui-tests.py --plan test/ui-test-plan.md --session test/ui-test-session.md
```

Use the repository's configured Python 3 interpreter when `python` is unavailable, such as `.venv/Scripts/python.exe` on Windows or `python3` on Unix-like systems.

The runner executes test cases in plan order. For each case it sends the recorded input to the command, combines stdout and stderr as the console output, compares the result with the expected output, and checks the expected exit code. It normalizes only line endings and the final newline; all other characters must match.

## Fail-fast and reporting

- Stop the session immediately when a command fails, times out, returns an unexpected exit code, or produces unexpected output. Do not run later cases.
- Preserve every completed case and the failing case in `test/ui-test-session.md`.
- After the runner finishes, show the session transcript, including each command, console input, actual console output, and result.
- On failure, explicitly report the actual output and expected output from the failing case, along with the command and exit status. Do not replace the actual output with a summary.
- Do not claim unrun cases passed.

## Resource

Use `scripts/run-ui-tests.py` for deterministic plan parsing, command execution, exact output checking, fail-fast behavior, and session logging. It requires only Python's standard library.
