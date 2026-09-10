# Athena User Guide

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
