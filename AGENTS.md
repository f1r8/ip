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

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.

## Code-update verification

After every update to application code, tests, build configuration, or test tooling:

1. From the repository root, review `test/ui-test-plan.md`. Add or revise cases when the change affects behavior, inputs, expected output, prerequisites, or commands. Preserve the plan unchanged only when its coverage and expectations remain accurate.
2. Invoke the project skill `$test-ui`. If project-skill discovery is unavailable, load `.codex/skills/test-ui/SKILL.md` directly and follow it; do not substitute an ad hoc UI run.
3. Let the skill build the current JAR and run the plan. Do not use its `--skip-build` option for this verification.
4. Show `test/ui-test-session.md`, including every executed case's command, console input, actual output, expected output, exit code, and result.
5. On the first failure, stop immediately and report the actual and expected outputs. Do not execute later cases, change an expectation merely to match an unintended result, or claim unexecuted cases passed.
