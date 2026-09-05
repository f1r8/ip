@AGENTS.md

For every Java code change or review, invoke and follow `$seedu-java-coding-standard`; all production and test Java code must comply with it.

For every commit, invoke and follow `$seedu-git-standard`. Use it as well whenever creating, proposing, or reviewing commit messages or branch names.

After every application-code, test, build-configuration, or test-tooling update, follow the code-update verification workflow in `AGENTS.md`: review or update `test/ui-test-plan.md`, invoke `$test-ui` with a current build, and stop on the first build or UI-test failure. Only after all UI cases pass, run `checkstyleMain` and `checkstyleTest`. Show `test/ui-test-session.md` and report the Checkstyle result.
