#!/usr/bin/env python3
"""Build Athena and run fail-fast console tests from a Markdown plan."""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path


CASE_RE = re.compile(r"^##\s+Test\s+case\s+\d+\s*:\s*(.+?)\s*$", re.I)
FIELD_RE = re.compile(
    r"^\s*(?:[-*]\s*)?(aim|command|inputs?|expected\s+output|expected\s+exit\s+code)\s*:\s*(.*)$",
    re.I,
)
PLAN_FIELD_RE = re.compile(
    r"^\s*[-*]\s*(build\s+command(?:\s*\((?:windows|unix)\))?|program\s+command)\s*:\s*(.*)$",
    re.I,
)
FENCE_RE = re.compile(r"^(?P<indent>\s*)(?P<fence>`{3,}|~{3,})(?P<rest>.*)$")


@dataclass
class TestCase:
    name: str
    aim: str
    command: str
    input_text: str
    expected_output: str
    expected_exit_code: int = 0


@dataclass
class TestPlan:
    build_command: str
    program_command: str
    cases: list[TestCase]


@dataclass
class ProcessResult:
    command: str
    actual_output: str
    exit_code: int | None
    failure_reason: str = ""

    @property
    def passed(self) -> bool:
        return not self.failure_reason


@dataclass
class CaseResult:
    case: TestCase
    process: ProcessResult

    @property
    def passed(self) -> bool:
        return self.process.passed


def canonical_field(value: str) -> str:
    """Normalize a Markdown field name used by the plan schema."""
    field = re.sub(r"\s+", " ", value.strip().lower())
    return "input" if field in ("input", "inputs") else field


def normalize_output(value: str) -> str:
    """Ignore line endings, invisible trailing whitespace, and final newlines."""
    normalized = value.replace("\r\n", "\n").replace("\r", "\n")
    return "\n".join(line.rstrip() for line in normalized.split("\n")).rstrip("\n")


def read_block(lines: list[str], index: int) -> tuple[str, int]:
    """Read one fenced Markdown block and return its contents and next index."""
    if index >= len(lines):
        return "", index
    match = FENCE_RE.match(lines[index])
    if not match:
        return "", index
    fence = match.group("fence")
    indent = match.group("indent")
    content: list[str] = []
    index += 1
    while index < len(lines):
        closing = FENCE_RE.match(lines[index])
        if (
            closing
            and closing.group("fence")[0] == fence[0]
            and len(closing.group("fence")) >= len(fence)
            and not closing.group("rest").strip()
        ):
            return "\n".join(content), index + 1
        line = lines[index]
        if indent and line.startswith(indent):
            line = line[len(indent) :]
        content.append(line)
        index += 1
    raise ValueError("unterminated fenced block in test plan")


def read_field_value(lines: list[str], index: int, inline_value: str) -> tuple[str, int]:
    """Read an inline value or the fenced block following a field."""
    if inline_value:
        return inline_value.strip(), index + 1
    lookahead = index + 1
    while lookahead < len(lines) and not lines[lookahead].strip():
        lookahead += 1
    if lookahead < len(lines) and FENCE_RE.match(lines[lookahead]):
        return read_block(lines, lookahead)
    return "", index + 1


def select_build_command(fields: dict[str, str]) -> str:
    """Select the platform-specific build command, with a generic fallback."""
    platform = "windows" if os.name == "nt" else "unix"
    return fields.get(f"build command ({platform})", fields.get("build command", ""))


def parse_plan(path: Path) -> TestPlan:
    """Parse the global program configuration and ordered test cases."""
    lines = path.read_text(encoding="utf-8").splitlines()
    plan_fields: dict[str, str] = {}
    cases: list[dict[str, str]] = []
    current: dict[str, str] | None = None
    index = 0
    while index < len(lines):
        case_match = CASE_RE.match(lines[index])
        if case_match:
            current = {"name": case_match.group(1)}
            cases.append(current)
            index += 1
            continue

        field_match = FIELD_RE.match(lines[index]) if current is not None else None
        if field_match:
            field = canonical_field(field_match.group(1))
            value, index = read_field_value(lines, index, field_match.group(2))
            current[field] = value
            continue

        plan_match = PLAN_FIELD_RE.match(lines[index]) if current is None else None
        if plan_match:
            field = canonical_field(plan_match.group(1))
            value, index = read_field_value(lines, index, plan_match.group(2))
            plan_fields[field] = value
            continue

        # Skip examples and explanatory text inside unrelated Markdown fences.
        if FENCE_RE.match(lines[index]):
            _, index = read_block(lines, index)
            continue
        index += 1

    if not cases:
        raise ValueError(f"no real '## Test case N: ...' sections found in {path}")

    program_command = plan_fields.get("program command", "")
    parsed: list[TestCase] = []
    required = ("aim", "input", "expected output")
    for number, raw in enumerate(cases, 1):
        missing = [field for field in required if field not in raw]
        command = raw.get("command", program_command)
        if not command:
            missing.append("command (or plan-level program command)")
        if missing:
            name = raw.get("name", f"test case {number}")
            raise ValueError(f"{name}: missing field(s): {', '.join(missing)}")
        try:
            exit_code = int(raw.get("expected exit code", "0"))
        except ValueError as exc:
            raise ValueError(f"{raw['name']}: expected exit code must be an integer") from exc
        parsed.append(
            TestCase(
                name=raw["name"],
                aim=raw["aim"],
                command=command,
                input_text=raw["input"],
                expected_output=raw["expected output"],
                expected_exit_code=exit_code,
            )
        )
    return TestPlan(select_build_command(plan_fields), program_command, parsed)


def resolve_command(command: str, repo_root: Path, case_dir: Path | None = None) -> str:
    """Expand the small set of path placeholders supported by the plan."""
    resolved = command.replace("{repo}", repo_root.as_posix())
    if case_dir is not None:
        resolved = resolved.replace("{case_dir}", case_dir.as_posix())
    return resolved


def run_process(command: str, cwd: Path, timeout: int, input_text: str = "") -> ProcessResult:
    """Run a command with combined stdout/stderr and a bounded timeout."""
    try:
        completed = subprocess.run(
            command,
            cwd=cwd,
            input=input_text,
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
        return ProcessResult(command, output, None, "process timed out")
    return ProcessResult(command, completed.stdout or "", completed.returncode)


def run_case(case: TestCase, repo_root: Path, timeout: int) -> CaseResult:
    """Run one Athena process in a clean directory so saved tasks cannot leak."""
    with tempfile.TemporaryDirectory(prefix="athena-ui-test-") as temporary_directory:
        case_dir = Path(temporary_directory)
        command = resolve_command(case.command, repo_root, case_dir)
        process = run_process(command, case_dir, timeout, case.input_text)
    if process.failure_reason:
        pass
    elif process.exit_code != case.expected_exit_code:
        actual = "timeout" if process.exit_code is None else str(process.exit_code)
        process.failure_reason = f"exit code mismatch: expected {case.expected_exit_code}, got {actual}"
    elif normalize_output(process.actual_output) != normalize_output(case.expected_output):
        process.failure_reason = "console output mismatch"
    return CaseResult(case, process)


def fence(value: str) -> str:
    """Render text without colliding with ordinary triple-backtick content."""
    return f"````text\n{value}\n````"


def render_session(
    plan_path: Path,
    started: str,
    status: str,
    build: ProcessResult | None,
    results: list[CaseResult],
    error: str = "",
) -> str:
    """Create the complete, human-readable console test transcript."""
    lines = [
        "# Athena UI test session",
        "",
        f"- Plan: `{plan_path}`",
        f"- Started: {started}",
        f"- Status: **{status}**",
        "",
    ]
    if error:
        lines.extend(["## Plan error", "", error, ""])
    if build is not None:
        lines.extend(
            [
                "## Build",
                "",
                f"- Command: `{build.command}`",
                f"- Exit code: `{build.exit_code if build.exit_code is not None else 'timeout'}`",
                "",
                "### Console output",
                "",
                fence(build.actual_output),
                "",
                f"### Result: {'PASS' if build.passed else 'FAIL'}",
                "",
            ]
        )
        if not build.passed:
            lines.extend([f"Failure reason: {build.failure_reason}", ""])
    for number, result in enumerate(results, 1):
        case = result.case
        process = result.process
        lines.extend(
            [
                f"## Test case {number}: {case.name}",
                "",
                f"- Aim: {case.aim}",
                f"- Command: `{process.command}`",
                "- Working directory: isolated temporary directory",
                f"- Exit code: `{process.exit_code if process.exit_code is not None else 'timeout'}` "
                f"(expected `{case.expected_exit_code}`)",
                "",
                "### Console input",
                "",
                fence(case.input_text),
                "",
                "### Actual console output",
                "",
                fence(process.actual_output),
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
            lines.extend([f"Failure reason: {process.failure_reason}", ""])
    return "\n".join(lines)


def write_session(path: Path, content: str) -> None:
    """Write the transcript to its stable project location."""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description="Run Athena UI tests from a Markdown plan.")
    parser.add_argument("--plan", default="test/ui-test-plan.md", help="Markdown test plan")
    parser.add_argument("--session", default="test/ui-test-session.md", help="session log destination")
    parser.add_argument("--cwd", default=".", help="Athena repository root")
    parser.add_argument("--timeout", type=int, default=30, help="per-test timeout in seconds")
    parser.add_argument("--build-timeout", type=int, default=180, help="build timeout in seconds")
    parser.add_argument("--skip-build", action="store_true", help="run an already-built program")
    args = parser.parse_args(argv)

    repo_root = Path(args.cwd).expanduser().resolve()
    plan_path = Path(args.plan).expanduser()
    if not plan_path.is_absolute():
        plan_path = repo_root / plan_path
    session_path = Path(args.session).expanduser()
    if not session_path.is_absolute():
        session_path = repo_root / session_path
    started = datetime.now().astimezone().isoformat(timespec="seconds")

    try:
        plan = parse_plan(plan_path)
    except (OSError, ValueError) as exc:
        session = render_session(plan_path, started, "FAILED", None, [], str(exc))
        write_session(session_path, session)
        print(session)
        return 2

    build: ProcessResult | None = None
    if plan.build_command and not args.skip_build:
        build_command = resolve_command(plan.build_command, repo_root)
        build = run_process(build_command, repo_root, args.build_timeout)
        if build.exit_code != 0:
            actual = "timeout" if build.exit_code is None else str(build.exit_code)
            build.failure_reason = f"build failed: expected exit code 0, got {actual}"
            session = render_session(plan_path, started, "FAILED", build, [])
            write_session(session_path, session)
            print(session)
            print("Test session stopped because the build failed.", file=sys.stderr)
            return 1

    results: list[CaseResult] = []
    for case in plan.cases:
        result = run_case(case, repo_root, args.timeout)
        results.append(result)
        if not result.passed:
            break

    status = "PASSED" if len(results) == len(plan.cases) and all(r.passed for r in results) else "FAILED"
    session = render_session(plan_path, started, status, build, results)
    write_session(session_path, session)
    print(session)
    if status == "FAILED":
        failed = next(result for result in results if not result.passed)
        print(
            f"Test session stopped after '{failed.case.name}': {failed.process.failure_reason}.\n"
            "Actual output and expected output are shown above.",
            file=sys.stderr,
        )
        return 1
    print(f"All {len(results)} test case(s) passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
