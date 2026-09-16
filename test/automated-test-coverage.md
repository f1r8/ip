# Automated test scope

JUnit tests cover the following behavior without system mouse or keyboard input:

| Area | Coverage |
| --- | --- |
| Dates and delimiters | Strict dates, leap centuries, year/time limits, whitespace, missing values, duplicate/reordered parameters, delimiter-like description text |
| Tasks and tags | Constructors, completion, display/serialization, validation, case-insensitive identity, scheduled-time differences, tag ordering, immutable collection snapshots |
| Task lists | Copying, ordering, addition/removal, invalid indexes, duplicate rejection without mutation |
| Commands | Every command, invalid input, duplicate tasks, search, tag no-ops, persistence, rollback after failed saves |
| Storage | Every record type with/without tags, Unicode round trips, malformed fields/status/dates/tags, duplicate records, missing/empty files, failed reads/writes, invalid paths, reload after repair, successful-save temporary-file cleanup |
| Console UI | Input, captions, errors, task lists, confirmations, welcome and goodbye |
| Athena orchestration | Real process startup, EOF, stopping at bye, corrupt-file recovery guidance, existing saved tasks, success/error/exit response flags, message/row reset |
| Presentation data | Task snapshots, response list copies, correction guidance and examples, without starting JavaFX |

The existing JavaFX event-based tests remain in the suite. New tests focus on logic that does not
require a graphical environment. Process tests use temporary working directories, never personal
`data/athena.txt` files. Storage tests also use JUnit temporary directories.

This is a behavior inventory, not a measured line/branch coverage percentage. Private utility
constructors, compiler-generated record/enum methods, unreachable defensive switch branches,
and operating-system failures such as disk exhaustion or failure to remove a temporary file are
not separately forced through artificial production hooks.

## Manual checks

Use the GUI acceptance section of `ui-test-plan.md` to review visual appearance, clipping, hover
states, native keyboard behavior, and focus on supported desktop platforms. Automated layout
assertions and saved screenshots supplement these checks; they do not prove visual quality or
native accessibility behavior. No additional manual GUI result is implied by passing the new JUnit tests.
