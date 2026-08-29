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
java -jar "{repo}/build/libs/athena.jar"
```

- Default per-test timeout: 30 seconds
- Build timeout: 180 seconds

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
event Team meeting /from 2026-12-30 1400
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
Please provide an event with /from and /to times, Your Majesty.
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
*Athena blinks her eyes, unsure of what you want, tilting
her head slightly as the meaning of your words slips just
out of reach.*
____________________________________________________________
```

Expected exit code: 0

## Test case 6: Find matching tasks

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
