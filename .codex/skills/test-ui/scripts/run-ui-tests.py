#!/usr/bin/env python3
"""Run command-driven athena.ui.UI tests described in a Markdown test plan."""
from __future__ import annotations

import argparse
import re
import subprocess
import sys
import textwrap
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path


CASE_RE = re.compile(r"^##\s+(?:test\s+case|test)\s*(?:\d+)?\s*:\s*(.+?)\s*$", re.I)
FIELD_RE = re.compile(
    r"^\s*(?:[-*]\s*)?(aim|command|input|expected\s+output|expected\s+exit\s+code)\s*:\s*(.*)$",
    re.I,
)
FENCE_RE = re.compile(r"^\s*(?P<fence>`{3,}|~{3,})(?:[^`]*)?\s*$")


@dataclass
class TestCase:
    name: str
    aim: str
    command: str
    input_text: str
    expected_output: str
    expected_exit_code: int = 0


@dataclass
class CaseResult:
    case: TestCase
    actual_output: str
    exit_code: int | None
    passed: bool
    failure_reason: str = ""


def normalize_output(value: str) -> str:
    """Ignore platform line endings and one final newline, but nothing else."""
    return value.replace("\r\n", "\n").replace("\r", "\n").rstrip("\n")


def read_block(lines: list[str], index: int) -> tuple[str, int]:
    """Read a fenced Markdown block beginning at index, returning value and next index."""
    if index >= len(lines):
        return "", index
    match = FENCE_RE.match(lines[index])
    if not match:
        return "", index
    fence = match.group("fence")
    content: list[str] = []
    index += 1
    while index < len(lines):
        if lines[index].strip() == fence:
            return textwrap.dedent("\n".join(content)), index + 1
        content.append(lines[index])
        index += 1
    raise ValueError("unterminated fenced block in test plan")


def parse_plan(path: Path) -> list[TestCase]:
    lines = path.read_text(encoding="utf-8").splitlines()
    cases: list[dict[str, object]] = []
    current: dict[str, object] | None = None
    index = 0
    while index < len(lines):
        case_match = CASE_RE.match(lines[index])
        if case_match:
            current = {"name": case_match.group(1)}
            cases.append(current)
            index += 1
            continue
        field_match = FIELD_RE.match(lines[index]) if current is not None else None
        if not field_match:
            index += 1
            continue
        field = re.sub(r"\s+", " ", field_match.group(1).strip().lower())
        value = field_match.group(2)
        if not value:
            lookahead = index + 1
            while lookahead < len(lines) and not lines[lookahead].strip():
                lookahead += 1
            if lookahead < len(lines) and FENCE_RE.match(lines[lookahead]):
                value, index = read_block(lines, lookahead)
                current[field] = value
                continue
        current[field] = value.strip()
        index += 1

    if not cases:
        raise ValueError(f"no test cases found in {path}")

    parsed: list[TestCase] = []
    required = ("aim", "command", "input", "expected output")
    for number, raw in enumerate(cases, 1):
        missing = [field for field in required if field not in raw]
        if missing:
            name = raw.get("name", f"test case {number}")
            raise ValueError(f"{name}: missing field(s): {', '.join(missing)}")
        try:
            exit_code = int(str(raw.get("expected exit code", "0")))
        except ValueError as exc:
            raise ValueError(f"{raw['name']}: expected exit code must be an integer") from exc
        parsed.append(
            TestCase(
                name=str(raw["name"]),
                aim=str(raw["aim"]),
                command=str(raw["command"]),
                input_text=str(raw["input"]),
                expected_output=str(raw["expected output"]),
                expected_exit_code=exit_code,
            )
        )
    return parsed


def run_case(case: TestCase, cwd: Path, timeout: int) -> CaseResult:
    try:
        completed = subprocess.run(
            case.command,
            cwd=cwd,
            input=case.input_text,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
            shell=True,
            timeout=timeout,
        )
    except subprocess.TimeoutExpired as exc:
        partial = exc.stdout or ""
        if isinstance(partial, bytes):
            partial = partial.decode("utf-8", errors="replace")
        output = str(partial) + f"\n[process timed out after {timeout} seconds]"
        return CaseResult(case, output, None, False, "process timed out")
    actual = completed.stdout or ""
    output_matches = normalize_output(actual) == normalize_output(case.expected_output)
    exit_matches = completed.returncode == case.expected_exit_code
    if not output_matches:
        reason = "console output mismatch"
    elif not exit_matches:
        reason = f"exit code mismatch: expected {case.expected_exit_code}, got {completed.returncode}"
    else:
        reason = ""
    return CaseResult(case, actual, completed.returncode, output_matches and exit_matches, reason)


def fence(value: str) -> str:
    return f"````text\n{value}\n````"


def render_session(plan_path: Path, started: str, results: list[CaseResult], status: str) -> str:
    lines = [
        "# athena.ui.UI test session",
        "",
        f"- Plan: `{plan_path}`",
        f"- Started: {started}",
        f"- Status: **{status}**",
        "",
    ]
    for number, result in enumerate(results, 1):
        case = result.case
        lines.extend(
            [
                f"## Test case {number}: {case.name}",
                "",
                f"- Aim: {case.aim}",
                f"- Command: `{case.command}`",
                f"- Exit code: `{result.exit_code if result.exit_code is not None else 'timeout'}` "
                f"(expected `{case.expected_exit_code}`)",
                "",
                "### Console input",
                "",
                fence(case.input_text),
                "",
                "### Actual console output",
                "",
                fence(result.actual_output),
                "",
                "### Expected console output",
                "",
                fence(case.expected_output),
                "",
                f"### Result: {'PASS' if result.passed else 'FAIL'}",
                "",
            ]
        )
        if not result.passed:
            lines.extend([f"Failure reason: {result.failure_reason}", ""])
    return "\n".join(lines)


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description="Run athena.ui.UI tests from a Markdown plan.")
    parser.add_argument("--plan", default="test/ui-test-plan.md", help="Markdown test plan")
    parser.add_argument("--session", default="test/ui-test-session.md", help="session log destination")
    parser.add_argument("--cwd", default=".", help="working directory for commands")
    parser.add_argument("--timeout", type=int, default=30, help="per-test timeout in seconds")
    args = parser.parse_args(argv)

    plan_path = Path(args.plan).expanduser().resolve()
    session_path = Path(args.session).expanduser().resolve()
    cwd = Path(args.cwd).expanduser().resolve()
    try:
        cases = parse_plan(plan_path)
    except (OSError, ValueError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 2

    started = datetime.now().astimezone().isoformat(timespec="seconds")
    results: list[CaseResult] = []
    for case in cases:
        result = run_case(case, cwd, args.timeout)
        results.append(result)
        if not result.passed:
            break

    status = "PASSED" if len(results) == len(cases) and all(r.passed for r in results) else "FAILED"
    session = render_session(plan_path, started, results, status)
    session_path.parent.mkdir(parents=True, exist_ok=True)
    session_path.write_text(session, encoding="utf-8")
    print(session)
    if status == "FAILED":
        failed = next(result for result in results if not result.passed)
        print(
            f"Test session stopped after '{failed.case.name}': {failed.failure_reason}.\n"
            "Actual output and expected output are shown above.",
            file=sys.stderr,
        )
        return 1
    print(f"All {len(results)} test case(s) passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
