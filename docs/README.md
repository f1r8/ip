# Athena User Guide

Athena is a **desktop application for managing tasks by typing commands in a Graphical User Interface (GUI)**. You can add todos, deadlines, and events, track completion, and organize tasks with tags.

- [Quick Start](#quick-start)
- [Features](#features)
  - [Command conventions](#command-conventions)
  - [Command summary](#command-summary)
  - [Adding a task](#adding-a-task)
    - [Adding a todo task: `todo`](#adding-a-todo-task-todo)
    - [Adding a deadline task: `deadline`](#adding-a-deadline-task-deadline)
    - [Adding an event task: `event`](#adding-an-event-task-event)
  - [Finding a task: `find`](#finding-a-task-find)
  - [Listing all tasks: `list`](#listing-all-tasks-list)
  - [Marking a task as completed: `mark`](#marking-a-task-as-completed-mark)
  - [Unmarking a task as completed: `unmark`](#unmarking-a-task-as-completed-unmark)
  - [Deleting a task: `delete`](#deleting-a-task-delete)
  - [Organizing tasks with tags](#organizing-tasks-with-tags)
    - [Adding tags: `tag`](#adding-tags-tag)
    - [Removing tags: `untag`](#removing-tags-untag)
    - [Finding tasks by tags: `findtag`](#finding-tasks-by-tags-findtag)
  - [Viewing the command reference](#viewing-the-command-reference)
  - [Exiting the program: `bye`](#exiting-the-program-bye)
  - [Saving the data](#saving-the-data)
- [Troubleshooting](#troubleshooting)
  - [Correcting input errors](#correcting-input-errors)
  - [Recovering from storage errors](#recovering-from-storage-errors)

<img width="604" height="946" alt="Athena conversation showing a deadline being added and task 9 marked Done, with the command box, Commands button, and Send button below." src="https://raw.githubusercontent.com/f1r8/ip/master/docs/Ui.png">

*Figure 1: Adding a deadline and marking it as completed in Athena.*

## Quick Start

1. Ensure that Java `25` is installed on your computer. Run `java -version` in a terminal to check the version used to launch Athena.\
   **Mac Users**: Follow the installation guide [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).
2. Download [athena.jar](https://github.com/f1r8/ip/releases/download/A-Jar/athena.jar).
3. Copy the file to the folder you want to use as the *home folder* for your Athena.
4. Open a terminal, `cd` to that folder, and run `java -jar athena.jar`. Use the exact filename of your downloaded JAR.
5. Click the **Commands** button to view the command reference. On Windows, you can also press Alt + C. Close the reference to return to your command draft.
6. Type a command in the command box and press Enter or click **Send** to execute it.\
   On an empty task list, try these commands one at a time:
   1. `todo Read a book`
   2. `tag 1 #important`
   3. `list`
   4. `mark 1`
   5. `delete 1`
7. Refer to the [Features](#features) section below for details of each command. If you already have saved tasks, run `list` and use the appropriate task number in the examples.

## Features

### Command conventions

- Words in `UPPER_CASE` are parameters to replace with your own values. For example, `todo DESCRIPTION` becomes `todo read a book`.
- Square brackets in command formats indicate optional arguments; do not type the brackets. `...` means you can supply more arguments of the same kind.
- Command names are case-insensitive. Leading and trailing spaces, repeated spaces, and tabs are accepted; repeated spaces and tabs in commands are normalized to a single space.
- Date parameters `/by`, `/from`, and `/to` must be lowercase, separated from surrounding values by whitespace, and supplied exactly once in the specified order.
- `INDEX` is a positive integer identifying a task in the **current full list**. Search results retain these numbers: if the first match is numbered 3, use `mark 3`, not `mark 1`. The same rule applies to `unmark`, `delete`, `tag`, and `untag`.
- Deleting a task renumbers later tasks. Earlier replies are snapshots and do not update when tasks change. Run `list` to check current numbers before updating a task.

### Command summary

Use this table as a quick reference. Follow the [command conventions](#command-conventions) above and select an action for full details.

| Action | Command format |
| --- | --- |
| [Add a todo](#adding-a-todo-task-todo) | `todo DESCRIPTION` |
| [Add a deadline](#adding-a-deadline-task-deadline) | `deadline DESCRIPTION /by END_TIME` |
| [Add an event](#adding-an-event-task-event) | `event DESCRIPTION /from START_TIME /to END_TIME` |
| [Find text](#finding-a-task-find) | `find STRING` |
| [List all tasks](#listing-all-tasks-list) | `list` |
| [Mark completed](#marking-a-task-as-completed-mark) | `mark INDEX` |
| [Mark not completed](#unmarking-a-task-as-completed-unmark) | `unmark INDEX` |
| [Delete a task](#deleting-a-task-delete) | `delete INDEX` |
| [Add tags](#adding-tags-tag) | `tag INDEX TAG [TAG ...]` |
| [Remove tags](#removing-tags-untag) | `untag INDEX TAG [TAG ...]` |
| [Find all specified tags](#finding-tasks-by-tags-findtag) | `findtag TAG [TAG ...]` |
| [Exit Athena](#exiting-the-program-bye) | `bye` |

Dates and times use `yyyy-MM-dd HHmm`; tags start with `#`. Use `list` to check the current task numbers before updating a task.

### Adding a task

New tasks start as not completed. Descriptions must not be blank or contain `|` or control characters. Ordinary punctuation, including slashes in `Read chapter 1/2`, is allowed.

Tasks with the same type, description, and dates are rejected as duplicates. Description case, completion status, and tags do not distinguish duplicates. There is no edit command; to change a description or date, delete the old task and add its replacement, then restore its tags and completion status as needed.

#### Adding a todo task: `todo`

Adds a task without a date or time.

Format: `todo DESCRIPTION`

Example:

- `todo read a book`

#### Adding a deadline task: `deadline`

Adds a task with a due date and time.

Format: `deadline DESCRIPTION /by END_TIME`

- `END_TIME` must be a real calendar date and four-digit, 24-hour time in `yyyy-MM-dd HHmm` format, for example `2026-09-17 2315`.
- Years must be from `0001` to `9999`. Hours must be `00` to `23` and minutes `00` to `59`.
- Standalone `/by`, `/from`, and `/to` tokens cannot appear in the description of a deadline; they are interpreted as date parameters.

Example:

- `deadline Edit User Guide /by 2026-09-17 2359`

#### Adding an event task: `event`

Adds a task with a start and end date and time.

Format: `event DESCRIPTION /from START_TIME /to END_TIME`

- Both times follow the same `yyyy-MM-dd HHmm` format and date limits as deadlines.
- The end must be strictly later than the start. Events can span multiple days.
- Supply `/from` before `/to`; reversing them is not accepted.
- Standalone `/by`, `/from`, and `/to` tokens cannot appear in the description of an event.

Example:

- `event summon best friend to finish the work /from 2026-09-17 2358 /to 2026-09-17 2359`

### Finding a task: `find`

Finds tasks containing the supplied text, including completed tasks.

Format: `find STRING`

- Supply a nonblank search string. Matching is case-insensitive.
- The string must occur as one continuous substring; this is not a search for separate words in any order. For example, `ead a bo` matches `read a book`, but `book read` does not.
- Tabs and repeated spaces in the search are treated as one space: `find read      a  book` matches `read a book`.
- Search includes descriptions, tags, formatted dates, and the task type/status markers used in the text representation (`[T]`, `[D]`, `[E]`, and `[X]` for completed tasks).
- Dates are searched in their displayed form, such as `Sep 17, 2026, 23:59`, rather than the input form `2026-09-17 2359`.
- Results keep their full-list task numbers. Use `list` to see all tasks again.

Examples:

- `find book` matches descriptions such as `read a book`, `return the books`, and `to kill a mockingbook`.
- `find a book` matches `read a book`.
- `find #work` matches tag text including `#workshop`; use `findtag #work` for an exact tag match.

### Listing all tasks: `list`

Shows all tasks, including completed tasks, in list order with their current numbers. Tasks are not sorted by date. The GUI shows each task's type, description, schedule where applicable, tags, and **To do** or **Done** status.

Format: `list`

This command takes no arguments. Use it after reopening Athena to view saved tasks.

### Marking a task as completed: `mark`

Marks the task at `INDEX` in the current full list as completed.

Format: `mark INDEX`

Example: `mark 1` marks task number 1 as completed.

Marking an already completed task keeps it completed and still displays a success confirmation if saving succeeds. Completed tasks remain in the list and search results.

### Unmarking a task as completed: `unmark`

Marks the task at `INDEX` in the current full list as not completed.

Format: `unmark INDEX`

Example: `unmark 1` returns task number 1 to **To do**.

Unmarking an incomplete task keeps it incomplete and still displays a success confirmation if saving succeeds.

### Deleting a task: `delete`

Deletes the task at `INDEX` in the current full list.

Format: `delete INDEX`

Example: `delete 2` deletes task number 2, even if it is the first result of a search.

Deletion is immediate and saved without a confirmation prompt. There is no undo command. Run `list` afterward to check the remaining task numbers.

### Organizing tasks with tags

Tags begin with `#` followed by one or more ASCII letters (`A-Z`, `a-z`), digits (`0-9`), underscores, or hyphens. They cannot contain spaces. Examples include `#work`, `#CS2103`, and `#next-week`.

Matching is case-insensitive. Each task keeps the spelling used when a tag was added and shows tags in case-insensitive alphabetical order. Add tags using `tag`: putting `#work` in a new todo's description only adds description text, not a tag.

#### Adding tags: `tag`

Adds one or more tags to an existing task.

Format: `tag INDEX TAG [TAG ...]`

Example: `tag 1 #work #urgent`

Adding a tag already present leaves that tag unchanged. If none of the tags change, Athena reports that no changes are needed and does not rewrite the data file.

#### Removing tags: `untag`

Removes one or more tags from an existing task.

Format: `untag INDEX TAG [TAG ...]`

Example: `untag 1 #work #urgent`

Removing an absent tag leaves the task unchanged for that tag. If none of the tags change, the data file is not rewritten. At least one tag is required; this command does not implicitly remove all tags.

#### Finding tasks by tags: `findtag`

Finds tasks carrying **all** specified exact tags, ignoring case, including completed tasks.

Format: `findtag TAG [TAG ...]`

Example: `findtag #work #urgent` finds tasks with both tags. `#workshop` does not match `#work`.

Results retain their current full-list task numbers. Use `list` to view all tasks again.

### Viewing the command reference

Click **Commands** (or press Alt + C on Windows) to open the scrollable command reference. Opening and closing it preserves your draft. This is a GUI control, not a typed command.

### Exiting the program: `bye`

Exits the program.

Format: `bye`

This command takes no arguments. Task changes have already been saved when their commands succeed.

### Saving the data

Athena automatically saves successful additions, deletions, completion updates, and tag changes. You do not need to save manually. `list`, `find`, `findtag`, and `bye` do not write the data file; tag commands that make no changes do not write it either.

Tasks, completion status, and tags are stored in `data/athena.txt`, relative to the terminal's working folder when Athena starts. Always launch from the same folder to use the same data. Launching from another folder can show an empty or different task list even when you use the same JAR. Conversation history is not saved.

To back up your tasks, close Athena and copy `data/athena.txt` to a safe location. To restore a backup, close Athena, replace that file with the backup, and restart from the same folder. When moving Athena to another folder, also copy its `data` folder to retain your tasks.

## Troubleshooting

### Correcting input errors

If a command fails, Athena keeps it in the command box and shows an explanation and correction guidance. Edit the command and press Enter or **Send** again. **Use example** places an example in the command box without running it; review its task number, description, and dates before sending. A successful command clears the error guidance and command box.

Check these common causes:

- `list` and `bye` accept no arguments; other commands require the parameters shown above.
- Task numbers must exist in the current full list. Run `list` to check them.
- Use a real calendar date: `2024-02-29 1200` is valid, but `2026-02-29 1200` is not.
- Deadlines require one `/by`; events require one `/from` followed by one `/to`, with the end strictly after the start.
- Descriptions cannot be blank or contain `|` or control characters. Duplicate tasks are rejected even if their completion status or tags differ.

### Recovering from storage errors

Athena creates `data/athena.txt` and missing parent folders when first saving a task. A failed save leaves the task list unchanged and displays an error instead of a success confirmation. Check folder and file permissions, available disk space, and whether the storage path is a folder, then retry the command. Saving uses atomic file replacement; the filesystem must support it.

<img width="272" height="146" alt="Athena startup error reporting a duplicate saved task, confirming the saved file is unchanged, and asking the user to repair the file or its permissions and restart." src="https://github.com/user-attachments/assets/4035da74-4475-478d-b981-346791731616" />

*Figure 2: A duplicate task in the saved data prevents startup without changing the saved file.*

If saved data is unreadable or malformed, Athena reports the problem and stops startup without overwriting the file. Close Athena and back up the file before attempting repairs. Correct the damaged record or restore a known good copy, then restart Athena. Saved files must be UTF-8; Windows and Unix line endings are accepted. Duplicate saved tasks and events with invalid date ranges must be corrected before startup. Prefer restoring a backup to manually editing the file: saved dates use a different format from command input, for example `2026-09-17T23:59`.
