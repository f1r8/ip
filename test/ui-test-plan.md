# Athena UI test plan

This is the ordered acceptance-test plan for Athena's command-line interface. The `test-ui` skill builds the current JAR, starts each test case in a clean temporary working directory, sends the recorded inputs to standard input, and compares combined stdout/stderr with the expected output. Comparisons ignore platform line endings, trailing whitespace that is invisible in a terminal, and final blank lines.

## Test configuration

- Working directory: a new isolated temporary directory for each case
- Runtime requirement: Java 25
- Build command (Windows):

```text
gradlew.bat --console=plain shadowJar
```

- Build command (Unix):

```text
./gradlew --console=plain shadowJar
```

- Program command:

```text
java -cp "{repo}/build/libs/athena.jar" athena.Athena
```

- Default per-test timeout: 30 seconds
- Build timeout: 180 seconds

### Windows workspace cache setup

In the restricted Windows workspace, set a writable Gradle cache before running the test runner or
any Gradle commands. From `ip/`, run this once in the PowerShell session:

```powershell
$env:GRADLE_USER_HOME = [System.IO.Path]::GetFullPath((Join-Path (Get-Location) '../.gradle'))
```

This uses the existing workspace cache, including the JUnit runtime dependencies. Without this setting,
Java may report `C:\` as its home and
Gradle may try to write to `C:\.gradle`; the user-profile cache may also be outside the sandbox's
writable directories. Keep the setting for the build, GUI/unit tests, and Checkstyle commands.

If compilation reports `AccessDeniedException` from `ZipFileSystemProvider.removeFileSystem` even
though the cached JAR is readable, run verification outside the restricted sandbox with approval.
In this Windows environment, Java's real-path lookup can fail inside the sandbox. A fresh Gradle
process avoids reusing a daemon started under different permissions. To diagnose compilation using
only cached dependencies, run:

```powershell
.\gradlew.bat --console=plain --no-daemon --offline compileTestJava
```

Use `--no-daemon` for subsequent Gradle verification commands in that environment. For the Python
runner's Gradle child process, append `-Dorg.gradle.daemon=false` to `GRADLE_OPTS` for the session.
Do not change dependency versions or broadly relax filesystem permissions to work around this error.

If the workspace virtual environment cannot launch Python, use the bundled interpreter to run the
same `test-ui` runner:

```powershell
& "$env:USERPROFILE\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" .codex/skills/test-ui/scripts/run-ui-tests.py
```

## GUI acceptance checks

GUI tests must not control the system mouse pointer or send system keyboard input. Activate buttons
with `Button.fire()`, submit text fields with JavaFX action events, and update scroll positions on the
JavaFX application thread. When checking focus restoration, focus the button before firing its action.

After every CLI case below passes, run the automated GUI and unit tests from the repository root with
`gradlew.bat --console=plain test --fail-fast` on Windows or `./gradlew --console=plain test --fail-fast`
on Unix. Stop on the first failure. Only after the CLI cases and these tests pass, run
`gradlew.bat --console=plain checkstyleMain checkstyleTest` on Windows or
`./gradlew --console=plain checkstyleMain checkstyleTest` on Unix.

For background styling changes, also inspect the GUI at 400 by 600 px and 720 by 600 px before
running Checkstyle. The conversation should have a uniform pale green-gray background without
wallpaper or repeated graphics. Confirm that message bubbles, the composer, and the tinted error
panel remain visually distinct. Record the visual result and screenshot paths in the session record.

The GUI acceptance checks cover the following behavior:

- User and Athena messages show a compact local `HH:mm` timestamp below the bubble, aligned with
  the corresponding side. Hovering reveals the full date and time. Existing timestamps stay fixed
  when later messages arrive or the window is resized, and remain readable in narrow layouts.

- Startup displays a courteous welcome, `todo Read a book` and `list` examples, and a Commands button.
  The welcome is accurate for both new and saved task lists: startup sends no synthetic command and
  changes no tasks. The first non-exit command removes the welcome and its layout space.
  The command editor has focus at startup so the user can immediately type a command.
- Commands is keyboard focusable and has an Alt+C mnemonic. It opens an owned, resizable command
  reference with wrapped syntax examples for every supported command, date/time format guidance,
  and a reminder to inspect task numbers using `list`. Space activates the focused button and Escape
  closes the guide. Tests use local JavaFX key events rather than system keyboard input.
- Opening and closing Commands preserves a typed draft, active error guidance, and the conversation's
  scroll position. It never calls the command responder. Close returns focus to the editor, and
  reopening the guide reuses the same window. At 360 by 360 px, the guide scrolls to the final `bye`
  example, wraps within its viewport, and keeps Close visible. Successful checks save
  `build/reports/gui/welcome.png` and `build/reports/gui/commands-small-window.png`.
- The error viewport budgets space using the composer's actual controls, wrapped status, padding,
  and spacing, so the Commands row leaves the editor available at short window heights.

- Body text, command input, and Send use a consistent 15 px baseline. The short input prompt fits at
  400 px. Send keeps the same font size in its normal, hover, pressed, and keyboard-focused states.
  Its dark green background has at least 4.5:1 contrast with the white label; the darker placeholder
  remains readable. Keyboard focus is visible without changing control size or shifting text.
- Success replies use concise captions that retain "Your Majesty". Added and removed tasks include the
  updated count. Empty lists and searches explain that no tasks are available or match the request.
- Task lists, search matches, and confirmations show structured task names, textual completion states,
  task types, full dates and times, and tags. Numbers, details, and statuses align across rows. Long names,
  dates, and tags wrap within a 400 px window without truncating or overlapping the status column.
  Saved replies retain their original completion state and tags when subsequent commands change a task.
- Adjacent message containers leave 8 px of vertical padding between them. Existing avatars, speech
  bubbles, and tails remain present. Review `build/reports/gui/task-rows.png` for the compact layout.
- The conversation scrollbar remains reader-controlled. New successful replies and failed-command entries
  preserve the position of earlier messages when the reader is more than 32 px from the bottom, including
  when error guidance reduces the conversation viewport. Replies follow the bottom when the reader is
  within 32 px of it; the automated near-bottom case begins 16 px from the bottom.
- Resizing the content area from 400 by 400 px to 720 by 600 px and back reflows long task names, dates,
  tags, and status columns within the conversation viewport. With conversation labels, command input,
  and Send enlarged to 22 px, labels remain fully readable and the editor stays inside the window.
  The complete Send label must fit without ellipsis; its width is at least its preferred width.
  Review `build/reports/gui/responsive-enlarged-text.png` for the narrow layout with enlarged text.
- A failed command displays a tinted panel beside the editor with a clear error heading, the explanation,
  command-specific advice, and a valid example. A text status identifies the failure without relying on color.
  Athena's advice, explanation, and correction status remain courteous and address "Your Majesty".
- Submitting `  deadline Submit report /by tomorrow  ` retains that exact input, including its whitespace,
  keeps the editor focused, and places the caret at the end so the command can be corrected immediately.
- After editing the retained command to `deadline Revised report /by next week`, choosing
  "Use this date and time" places `deadline Revised report /by 2026-12-31 2359` in the editor and restores
  focus. It does not submit a command, add a task, or append a reply. If the editor has been changed to
  another command, the deadline repair leaves that edited command untouched.
- Other command examples are placed in the editor only when "Place this example below" is chosen.
  They are not executed until the user sends them. The corrected successful command clears the editor,
  hides the panel and correction status, and removes their layout space. The success reply remains visible.
- Invalid `todo`, `deadline`, `event`, `mark`, `unmark`, `delete`, `find`, `tag`, `untag`, `findtag`, blank,
  and unknown commands each show appropriate advice and examples. Examples using task 1 explicitly assume
  that it exists and direct the user to `list` to inspect task numbers. Keywords remain case-insensitive.
- At a window width of 400 px, a window height of 400 px, and at 400 by 400 px, the editor and send control
  remain available. A long deadline description wraps within the error panel; its contents can be scrolled
  when necessary to reach the explanation, example, and repair control without losing access to the editor.
  The short-window scrolling test waits for JavaFX layout pulses, measures bounds on the JavaFX thread,
  and checks that the scroll viewport has positive height and fully contains the repair button after
  scrolling. Failures include measured coordinates. The composer retains space for the error viewport
  and editor while the conversation area shrinks. A successful short-window check saves
  `build/reports/gui/error-panel-short-window.png` for visual review.

## Test case 1: Exit cleanly

Aim: Verify that Athena starts with an empty isolated store and prints its farewell message for `bye`.

Inputs:

```text
bye
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
____________________________________________________________
Farewell, Your Majesty. I hope to serve you again soon!
____________________________________________________________
```

Expected exit code: 0

## Test case 2: Manage a todo through its lifecycle

Aim: Verify adding, listing, marking, unmarking, and deleting a todo in one stateful session.

Inputs:

```text
todo Read the project brief
list
mark 1
unmark 1
delete 1
list
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Read the project brief
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [T][ ] Read the project brief
____________________________________________________________
____________________________________________________________
Excellent, Your Majesty! I've marked this task as done:
  [T][X] Read the project brief
____________________________________________________________
____________________________________________________________
Certainly, Your Majesty. I've marked this task as not done yet:
  [T][ ] Read the project brief
____________________________________________________________
____________________________________________________________
As you wish, Your Majesty. I've removed this task:
  [T][ ] Read the project brief
You now have 0 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
____________________________________________________________
```

Expected exit code: 0

## Test case 3: Add dated tasks

Aim: Verify deadline and event commands parse dates and display their formatted times.

Inputs:

```text
deadline Submit report /by 2026-12-31 2359
event Team meeting /from 2026-12-30 1400 /to 2026-12-30 1500
list
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [D][ ] Submit report (by: Dec 31, 2026, 23:59)
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [E][ ] Team meeting (from: Dec 30, 2026, 14:00, to: Dec 30, 2026, 15:00)
You now have 2 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [D][ ] Submit report (by: Dec 31, 2026, 23:59)
2. [E][ ] Team meeting (from: Dec 30, 2026, 14:00, to: Dec 30, 2026, 15:00)
____________________________________________________________
```

Expected exit code: 0

## Test case 4: Reject incomplete commands

Aim: Verify that missing descriptions, dates, task indexes, and search keywords produce the intended guidance
without terminating the session.

Inputs:

```text
todo
deadline Submit report
deadline /by 2026-12-31 2359
event Team meeting /from 2026-12-30 1400
event /from 2026-12-30 1400 /to 2026-12-30 1500
mark not-a-number
delete 1
find
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
Please provide a todo description, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide a deadline and /by date, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide a task description, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide an event with /from and /to times, Your Majesty.
____________________________________________________________
____________________________________________________________
Please provide a task description, Your Majesty.
____________________________________________________________
____________________________________________________________
Which task shall I mark, Your Majesty?
____________________________________________________________
____________________________________________________________
Your Majesty, there aren't that many tasks in the list.
____________________________________________________________
____________________________________________________________
What shall I search for, Your Majesty?
____________________________________________________________
```

Expected exit code: 0

## Test case 5: Reject an unknown command

Aim: Verify that an unrecognized command produces Athena's unknown-command response.

Inputs:

```text
dance
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
*Athena blinks her eyes, unsure of what you want, tilting her head slightly as the meaning of your words slips just out of reach.*
____________________________________________________________
```

Expected exit code: 0

## Test case 6: Manage tags through their lifecycle

Aim: Verify adding, removing, and finding valid tags while preserving spelling and normalized display order.

Inputs:

```text
todo Prepare briefing
tag 1 #school #Fun
findtag #fun #SCHOOL
untag 1 #FUN
tag 1 #school
list
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Prepare briefing
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added the tags to this task:
  [T][ ] Prepare briefing #Fun #school
____________________________________________________________
____________________________________________________________
Your Majesty, here are the matching tasks in your list:
1. [T][ ] Prepare briefing #Fun #school
____________________________________________________________
____________________________________________________________
As you wish, Your Majesty. I've removed the tags from this task:
  [T][ ] Prepare briefing #school
____________________________________________________________
____________________________________________________________
Your Majesty, no tags changed for this task:
  [T][ ] Prepare briefing #school
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [T][ ] Prepare briefing #school
____________________________________________________________
```

Expected exit code: 0

## Test case 7: Validate tag commands and no-op behavior

Aim: Verify validation order and messages, no-op responses, and that invalid commands leave tags unchanged.

Inputs:

```text
todo Read book
tag first #fun
tag 1
tag 9 invalid
tag 9 #fun
tag 1 #fun
tag 1 #FUN
untag 1 invalid
untag 1 #missing
findtag
findtag invalid
list
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Read book
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Which task shall I tag, Your Majesty?
____________________________________________________________
____________________________________________________________
Which tags shall I add, Your Majesty?
____________________________________________________________
____________________________________________________________
Each tag must start with # and contain at least one letter, number, underscore, or hyphen, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, there aren't that many tasks in the list.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added the tags to this task:
  [T][ ] Read book #fun
____________________________________________________________
____________________________________________________________
Your Majesty, no tags changed for this task:
  [T][ ] Read book #fun
____________________________________________________________
____________________________________________________________
Each tag must start with # and contain at least one letter, number, underscore, or hyphen, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, no tags changed for this task:
  [T][ ] Read book #fun
____________________________________________________________
____________________________________________________________
Which tags shall I search for, Your Majesty?
____________________________________________________________
____________________________________________________________
Each tag must start with # and contain at least one letter, number, underscore, or hyphen, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [T][ ] Read book #fun
____________________________________________________________
```

Expected exit code: 0

## Test case 8: Find matching tasks

Aim: Verify that the find command matches case-insensitively, prints only matching tasks, and numbers the
matches from one.

Inputs:

```text
todo Read the project brief
todo Submit the Final REPORT
find report
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Read the project brief
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [T][ ] Submit the Final REPORT
You now have 2 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the matching tasks in your list:
1. [T][ ] Submit the Final REPORT
____________________________________________________________
```

Expected exit code: 0

## Test case 9: Correct a failed deadline without adding a duplicate

Aim: Verify that an invalid deadline receives in-character date guidance, adds no task, and can be corrected
in the same session so only the successful deadline appears in the list.

Inputs:

```text
deadline Submit report /by tomorrow
deadline Submit report /by 2026-12-31 2359
list
```

Expected output:

```text
File not found at: ./data/athena.txt
____________________________________________________________
    _  _____ _   _ _____ _   _    _
   / \|_   _| | | | ____| \ | |  / \
  / _ \ | | | |_| |  _| |  \| | / _ \
 / ___ \| | |  _  | |___| |\  |/ ___ \
/_/   \_\_| |_| |_|_____|_| \_/_/   \_\
Hello, Your Majesty! I'm Athena.
How may I assist you, Your Majesty?
____________________________________________________________
____________________________________________________________
Please use 'yyyy-MM-dd HHmm' for the date and time, Your Majesty (e.g. 2026-12-31 2359).
____________________________________________________________
____________________________________________________________
As you command, Your Majesty. I've added this task:
  [D][ ] Submit report (by: Dec 31, 2026, 23:59)
You now have 1 tasks in the list, Your Majesty.
____________________________________________________________
____________________________________________________________
Your Majesty, here are the tasks in your list:
1. [D][ ] Submit report (by: Dec 31, 2026, 23:59)
____________________________________________________________
```

Expected exit code: 0
