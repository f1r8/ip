# Athena User Guide

Athena is a **desktop application for managing tasks, optimized for use through a Graphical User Interface(GUI)**, while retaining the benefits of a Command Line Interface (CLI). If you type quickly, Athena can help you manage tasks faster than traditional GUI applications.

- [Quick Start](#quick-start)
- [Features](#features)
  - [Adding a task](#adding-a-task)
    - [Adding a todo task: `todo`](#adding-a-todo-task-todo)
    - [Adding a deadline task: `deadline`](#adding-a-deadline-task-deadline)
    - [Adding an event task: `event`](#adding-an-event-task-event)
  - [Finding a task: `find`](#finding-a-task-find)
  - [Listing all tasks: `list`](#listing-all-tasks-list)
  - [Marking a task as completed: `mark`](#marking-a-task-as-completed-mark)
  - [Unmarking a task as completed: `unmark`](#unmarking-a-task-as-completed-unmark)
  - [Deleting a task: `delete`](#deleting-a-task-delete)
  - [Exiting the program: `bye`](#exiting-the-program-bye)
  - [Saving the data](#saving-the-data)
- [Known Issues](#known-issues)
  - [Correcting input errors](#correcting-input-errors)
  - [Recovering from storage errors](#recovering-from-storage-errors)
- [Additional Features](#additional-features)
  - [Organizing tasks with tags](#organizing-tasks-with-tags)


<img width="604" height="946" alt="image" src="https://github.com/f1r8/ip/blob/master/docs/Ui.png">

## Quick Start
1. Ensure that Java `25` or later is installed on your computer.\
**Mac Users**: Follow the installation guide [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).
2. Download the latest `.jar` file [here](https://github.com/f1r8/ip/releases/download/A-Jar/athena.jar).
3. Copy the file to the folder you want to use as the *home folder* for your Athena.
4. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar Athena.jar`.
5. Click the **Command** button or press Alt + C to view command reference.
6. Type a command in the command box and click Enter to execute it.\
   Here are a series of commands you can try:
   1. `todo Read a book`
   2. `tag 1 #important`
   3. `list`
   4. `mark 1`
   5. `delete 1`
7. Refer to the [Features](#features) section below for details of each command.

## Features

> [!WARNING]
> Parameters must be in the specified order.\
> For example, if the command specifies `event /from START_TIME /to END_TIME`, `event /to END_TIME /from START_TIME` is not accepted.

> [!NOTE]
> Words in `UPPER_CASE` are the parameters to be supplied by the user.
> For example, in `todo DESCRIPTION`, replace `DESCRIPTION` with a value such as `read a book`.

> [!CAUTION]
> Characters such as the vertical pipe `|` are not allowed in this program. They are reserved for storing program data.

### Adding a task

#### Adding a todo task: `todo`
Adds a todo task to the task list.

Format: `todo DESCRIPTION`

Examples:

- `todo read a book`

#### Adding a deadline task: `deadline`
Adds a deadline task to the task list.

Format: `todo DESCRIPTION /by END_TIME`

- `END_TIME` must be in this format: `YYYY-MM-DD HHmm`, for example `2001-09-11 0800` or `2026-09-17 2315`
- `  /by  `, which is `/by` preceded and succeeded with whitespace, cannot appear in the description.

Examples:

- `deadline Edit User Guide /by 2026-09-17 2359`

#### Adding an event task: `event`
Adds an event task to the task list.

Format: `event DESCRIPTION /from START_TIME /to END_TIME`

- `START_TIME` and `END_TIME` must be in this format: `YYYY-MM-DD HHmm`, for example `2001-09-11 0800` or `2026-09-17 2315`
- `  /from  `, which is `/from` preceded and succeeded with whitespace, cannot appear in the description.
- `  /to  ` also cannot appear in the description.

Examples:

- `event summon best friend to finish the work /from 2026-09-17 2358 /to 2026-09-17 2359`

### Finding a task: `find`
Finds a list of tasks that matches the given string.

Format: `find STRING`

- The search is case-insensitive, for example, `duke` matches `dUkE`.
- Tabs and/or multiple whitespaces in the `STRING` are treated as 1 whitespace. for example, `read      a  book` matches `read a book`.
- The search `STRING` must be exact, for example, `ead a bo` matches `read a book` but does not match `read book a`.

Examples:

- `find book` returns `read a book`, `return the books`, `to kill a mockingbook`.
- `find a book` returns `read a book`.

### Listing all tasks: `list`
Shows a list of all tasks.

Format: `list`

### Marking a task as completed: `mark`
Marks the specified task from the list as completed.

Format: `mark INDEX`

- Marks the task at the specified `INDEX`.
- The index refers to the index number shown in the displayed task list
- The index **must be a positive integer** 1,2,3, ...
- No change is made if the task is already completed. No error is displayed.

Examples:

- `list` followed by `mark 1` marks the 1st task in the list as completed.
- `find book` followed by `mark 1` marks the task numbered 1 as completed, **not the 1st task in the list**.

### Unmarking a task as completed: `unmark`
Marks the specified task from the list as not completed.

Format: `unmark INDEX`

- Marks the task at the specified `INDEX`.
- The index refers to the index number shown in the displayed task list
- The index **must be a positive integer** 1,2,3, ...
- No change is made if the task is already not completed. No error is displayed.

Examples:

- `list` followed by `unmark 1` marks the 1st task in the list as not completed.
- `find book` followed by `unmark 1` marks the task numbered 1 as not completed, **not the 1st task in the list**.

### Deleting a task: `delete`
Deletes the specified task from the list.

Format: `delete INDEX`

- Deletes the task at the specified `INDEX`.
- The index refers to the index number shown in the displayed task list
- The index **must be a positive integer** 1,2,3, ...

Examples:
- `list` followed by `delete 2` deletes the 2nd task in the list
- `find book` followed by `delete 2` deletes the task numbered 2, **not the 2nd task in the list**.

### Exiting the program: `bye`
Exits the program.

Format: `bye`

### Saving the data
Athena automatically saves data after every command. You do not need to save manually.

## Known Issues

### Correcting input errors

Commands accept leading and trailing spaces, repeated spaces, and tabs. Command names are
case-insensitive. `list` and `bye` take no additional arguments. Task numbers must refer to the
current full list; use `list` before marking, deleting, or tagging a task.

Use real calendar dates in `yyyy-MM-dd HHmm` format, with years from 0001 to 9999 and times
from 0000 to 2359. For example, `2024-02-29 1200` is valid but `2026-02-29 1200` is not.
Deadlines require one `/by` parameter. Events require one `/from` followed by one `/to`,
and the end must be strictly later than the start. Put whitespace before date parameters.

Task descriptions cannot be blank or contain `|` or control characters. Other punctuation,
including slashes in text such as `Read chapter 1/2`, is allowed. Spaces are normalized.
Tasks with the same type, description, and dates are duplicates, ignoring description case,
completion status, and tags. Change the details or use the existing task instead.

### Recovering from storage errors

Athena creates `data/athena.txt` and missing parent folders when first saving a task. A failed
save leaves the task list unchanged and displays an error instead of a success confirmation.
Check folder and file permissions, available disk space, and whether the storage path is a folder,
then retry the command. Saving uses atomic file replacement; the filesystem must support it.

<img width="272" height="146" alt="image" src="https://github.com/user-attachments/assets/4035da74-4475-478d-b981-346791731616" />

If saved data is unreadable or malformed, Athena reports the problem and stops startup without
overwriting the file. Back up the file, correct the damaged record or restore a known good copy,
then restart Athena. Saved files must be UTF-8; Windows and Unix line endings are accepted.
Duplicate saved tasks and events with invalid date ranges must be corrected before startup.

## Additional Features

### Organizing tasks with tags

Tags are single words. Athena matches them without regard to letter case while keeping the spelling from the
first time each tag is added. Each tag starts with `#` and then contains one or more letters, numbers,
underscores, or hyphens. Tags appear alphabetically after the task's description, deadline, or event times.

Add one or more tags to an existing task with its displayed task number:

```text
tag 1 #work #urgent
```

Remove one or more tags in the same way:

```text
untag 1 #work #urgent
```

Find tasks carrying all specified exact tags with `findtag`. The search is case-insensitive:

```text
findtag #work #urgent
```

Repeating a tag that is already present, or removing a tag that is absent, leaves the task unchanged and
does not rewrite the data file.
