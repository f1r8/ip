# Athena User Guide

## Correcting input errors

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

## Recovering from storage errors

Athena creates `data/athena.txt` and missing parent folders when first saving a task. A failed
save leaves the task list unchanged and displays an error instead of a success confirmation.
Check folder and file permissions, available disk space, and whether the storage path is a folder,
then retry the command. Saving uses atomic file replacement; the filesystem must support it.

If saved data is unreadable or malformed, Athena reports the problem and stops startup without
overwriting the file. Back up the file, correct the damaged record or restore a known good copy,
then restart Athena. Saved files must be UTF-8; Windows and Unix line endings are accepted.
Duplicate saved tasks and events with invalid date ranges must be corrected before startup.

## Organizing tasks with tags

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
