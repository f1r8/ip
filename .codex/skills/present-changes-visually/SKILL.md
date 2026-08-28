---
name: present-changes-visually
description: Generate a self-contained, GitHub-style split-view HTML page that visually presents changes in a Git repository. Use when asked to show, review, share, or inspect code changes visually; compare revisions, branches, commits, or the worktree; or create an HTML diff.
---

# Present Changes Visually

Generate one interactive HTML page containing every changed file as a side-by-side before/after diff. The page folds long unchanged runs, highlights changed words within modified lines, lets readers filter files, and includes collapsed panels for unchanged files.

## Generate the page

1. Treat the current repository as the target unless the user identifies another repository.
2. Use `HEAD` as the before point and `WORKTREE` as the after point unless the user specifies comparison points. `WORKTREE` includes staged, unstaged, and untracked files, excluding ignored files.
3. Write to `_temp/visual-diff.html` unless the user supplies an output path.
4. Run the bundled generator from the target repository root with an available Python 3 interpreter:

   ```text
   python .codex/skills/present-changes-visually/scripts/generate-split-view-diff.py . HEAD WORKTREE _temp/visual-diff.html
   ```

   On systems where `python` is unavailable, use the repository's configured Python 3 executable, such as `.venv/Scripts/python.exe` on Windows or `python3` on Unix-like systems. Replace `HEAD`, `WORKTREE`, and the output path with the requested comparison points and destination. Comparison points may be any Git commit-ish, including a commit SHA, tag, branch, or `HEAD~N`.
5. Confirm the command succeeds, the output file exists, and the generator summary reports the expected changed-file count. Report the absolute output path.

## Review and sharing

- Do not open a browser automatically. Open or render the generated HTML only when the user asks for a visual review or preview.
- Keep the generated page self-contained. The bundled generator uses only Python's standard library; optional syntax highlighting is loaded from a CDN by the page when network access is available.
- Expect binary, unreadable, or oversized files to appear as file-level notes rather than line-by-line diffs.
- Do not add generated HTML to a commit unless the user explicitly asks for it.

## Resource

Use `scripts/generate-split-view-diff.py` as the standard-library-only generator. It supports staged, unstaged, and untracked worktree changes, rename detection, folded unchanged runs, word-level highlighting, and collapsed unchanged-file panels.
