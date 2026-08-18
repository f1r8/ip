# UI test plan

Record every command-driven UI test here before running the test session. Use one test case per command, in execution order. Include the aim, exact standard input, and exact combined console output expected from the program. Record prerequisites such as the build command, runtime version, working directory, and environment variables near the top of this file.

The `test-ui` skill runs this plan with `scripts/run-ui-tests.py`. It normalizes only line endings and the final newline when comparing output; all other characters must match. It stops at the first failed, timed-out, or unexpectedly terminated command.

## Prerequisites

- Working directory: repository root
- Build/runtime requirements: describe them here
- Build command, if needed: describe it here
- Default per-test timeout: 30 seconds

## Test cases

Add cases using this format:

```markdown
## Test case 1: Describe the behavior

- Aim: State what this case verifies.
- Command:
  ```text
  command to run
  ```
- Input:
  ```text
  exact input sent to the program
  ```
- Expected output:
  ```text
  exact combined stdout and stderr
  ```
- Expected exit code: 0
```
