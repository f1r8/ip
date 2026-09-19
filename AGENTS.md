# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: Intermediate — completed several coursework projects and assignments involving Java/OOP; comfortable writing and debugging moderately complex programs, but still building experience with larger codebases and advanced design patterns.
* IDE and level of expertise: IntelliJ IDEA — comfortable using it for everyday development (writing, running, debugging code, navigating projects); familiar with common features but not deeply expert in advanced tooling (e.g. refactoring tools, profilers, plugin ecosystem).

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Java coding standard

Invoke and follow the project skill `$seedu-java-coding-standard` whenever creating, editing, refactoring, or reviewing Java code. All Java code in this project, including test code, must comply with that skill.

## Git

Invoke and follow the project skill `$seedu-git-standard` for every commit. Also invoke it whenever creating, proposing, or reviewing a commit message, or creating, renaming, proposing, or reviewing a branch name. Every commit message and branch name must comply with that skill.

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.

## Code-update verification

After every update to application code, tests, build configuration, or test tooling:

1. From the repository root, review `test/ui-test-plan.md`. Add or revise cases when the change affects behavior, inputs, expected output, prerequisites, or commands. Preserve the plan unchanged only when its coverage and expectations remain accurate.
2. Invoke the project skill `$test-ui`. If project-skill discovery is unavailable, load `.codex/skills/test-ui/SKILL.md` directly and follow it; do not substitute an ad hoc UI run.
3. Let the skill build the current JAR and run the plan. Do not use its `--skip-build` option for this verification.
4. On the first build or UI-test failure, stop immediately and show `test/ui-test-session.md`. For a UI-test failure, report the actual and expected outputs. Do not execute later cases, change an expectation merely to match an unintended result, or claim unexecuted cases passed.
5. Only after every UI test case passes, run Checkstyle from the repository root:
   * On Windows: `gradlew.bat --console=plain checkstyleMain checkstyleTest`
   * On Unix: `./gradlew --console=plain checkstyleMain checkstyleTest`
6. If Checkstyle fails, stop and report its command and violations. Code-update verification passes only when both the UI test plan and Checkstyle pass.
7. Show `test/ui-test-session.md`, including every executed case's command, console input, actual output, expected output, exit code, and result, and report the Checkstyle result.

## Windows sandbox build recovery

Verified on 2026-09-18 with Java 25: a restricted build failed in `compileJava`
with `AccessDeniedException` for the cached `javafx-controls-17.0.7-linux.jar`.
It also printed missing-package and missing-symbol errors for project classes.
Rerunning the same build and UI plan outside the restricted sandbox, using the
same workspace cache and a fresh Gradle process, passed all 12 CLI cases without
source, dependency, cache, or permission changes.

When this specific access failure recurs:

1. Stop the failed verification run and show `test/ui-test-session.md`. Inspect
   the final build exception before treating the earlier compiler messages as
   source defects. Do not run later tests or Checkstyle after a failed build.
2. Use Java 25 and set the workspace cache in each new PowerShell process:

   ```powershell
   $env:GRADLE_USER_HOME = [System.IO.Path]::GetFullPath((Join-Path (Get-Location) '../.gradle'))
   $env:GRADLE_OPTS = "$env:GRADLE_OPTS -Dorg.gradle.daemon=false"
   ```

3. Request approved execution outside the restricted sandbox for a fresh
   verification attempt. Run the existing `test-ui` runner from `ip/`, including
   its build step. If the workspace virtual environment reports access denied,
   the bundled interpreter worked in this environment:

   ```powershell
   & "$env:USERPROFILE/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe" .codex/skills/test-ui/scripts/run-ui-tests.py
   ```

4. After the CLI plan passes, run the GUI/unit tests with
   `gradlew.bat --console=plain --no-daemon test --fail-fast`, then Checkstyle
   with `gradlew.bat --console=plain --no-daemon checkstyleMain checkstyleTest`
   only if the tests pass. Use the same approved execution context and cache.

This recovery permits a new attempt after addressing the environment failure;
it does not permit continuing a failed run or bypassing approval. Do not delete
the cache, change dependency versions, or broaden filesystem permissions to
work around this error. Investigate and report any different failure separately.

## Windows sandbox JUnit cleanup recovery

Verified on 2026-09-19 with Java 25.0.4: the restricted GUI/unit test run failed in
`AthenaTest.getResponse_savedTasks_loadsExistingState()` with
`JUnitException: Failed to close extension context`. Its cause was
`AccessDeniedException` for a `C:\Users\jiang\AppData\Local\Temp\junit-*`
directory during `WindowsPath.toRealPath` in JUnit cleanup.
Rerunning the same test command outside the restricted sandbox, with the existing
workspace cache and a fresh Gradle process, passed all 250 tests with no failures
or skips. No source, dependency, cache, or filesystem-permission changes were needed.

When this specific cleanup failure recurs:

1. Stop the failed run, record the expected successful cleanup and actual exception
   in `test/ui-test-session.md`, and show that record. Do not run Checkstyle yet.
2. If all CLI cases already passed for the unchanged application and resources,
   retain that result. Otherwise, first run the existing `test-ui` skill with its
   build step and stop on any failure.
3. Verify Java 25 and set `GRADLE_USER_HOME` to the workspace cache as described
   above. Request approved execution outside the restricted sandbox for a fresh
   `gradlew.bat --console=plain --no-daemon test --fail-fast` attempt. Keep the
   existing test automation that does not move the user's mouse pointer.
4. Only after the CLI cases and GUI/unit tests pass, run
   `gradlew.bat --console=plain --no-daemon checkstyleMain checkstyleTest` in the
   same approved execution context. Record the retry and Checkstyle results in
   `test/ui-test-session.md`, preserving the initial failure record.

If the approved retry still fails, stop and investigate the new evidence. Do not
disable JUnit cleanup, skip failing tests, delete caches, or broaden filesystem
permissions to make verification pass. This recovery applies to the cleanup
failure described here; investigate different failures separately.
