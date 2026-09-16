package athena.gui;

import java.util.Locale;

/**
 * Supplies courteous repair advice and editable examples for failed commands.
 *
 * @param hint Advice explaining how to repair the command.
 * @param example A command illustrating the accepted syntax.
 * @param isDeadline Whether the example can preserve an edited deadline description.
 */
record ErrorGuidance(String hint, String example, boolean isDeadline) {
    private static final String EXAMPLE_DATE = "2026-12-31 2359";
    private static final String INDEX_HINT = "Use list to inspect your task numbers. "
            + "This example assumes task 1 exists. ";

    /**
     * Returns advice for the command keyword without executing the command.
     *
     * @param input The failed command.
     * @return Advice and an example appropriate to the command.
     */
    static ErrorGuidance forInput(String input) {
        String command = input.trim().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        return switch (command) {
            case "todo" -> new ErrorGuidance(
                    "Please tell me what you wish to do after todo.",
                    "todo Read book", false);
            case "deadline" -> new ErrorGuidance(
                    "Give me a task followed by /by, a date in YYYY-MM-DD format, "
                            + "and a four-digit time in HHmm format. You may change the sample date before sending.",
                    createDeadlineExample(input), true);
            case "event" -> new ErrorGuidance(
                    "Give me an event followed by /from and /to. "
                            + "Each needs a date in YYYY-MM-DD format and a four-digit time in HHmm format.",
                    "event Team meeting /from 2026-12-31 1400 /to 2026-12-31 1500", false);
            case "mark" -> new ErrorGuidance(INDEX_HINT + "Choose the task you wish to mark complete.",
                    "mark 1", false);
            case "unmark" -> new ErrorGuidance(INDEX_HINT + "Choose the task you wish to mark incomplete.",
                    "unmark 1", false);
            case "delete" -> new ErrorGuidance(INDEX_HINT + "Choose the task you wish me to remove.",
                    "delete 1", false);
            case "find" -> new ErrorGuidance(
                    "Place a word or phrase after find, and I shall search your tasks.",
                    "find report", false);
            case "tag" -> new ErrorGuidance(INDEX_HINT + "Follow its number with #tags containing "
                    + "letters, numbers, underscores, or hyphens.", "tag 1 #work", false);
            case "untag" -> new ErrorGuidance(INDEX_HINT + "Follow its number with the #tags you wish me to remove.",
                    "untag 1 #work", false);
            case "findtag" -> new ErrorGuidance(
                    "Follow findtag with #tags containing letters, numbers, underscores, "
                            + "or hyphens. I shall look for tasks bearing all those tags.",
                    "findtag #work", false);
            default -> new ErrorGuidance(
                    "Open Commands for the full reference, or begin with the example below.",
                    "todo Read book", false);
        };
    }

    /**
     * Returns a draft example, retaining edits to the deadline task description.
     * A different command keyword is left untouched so an old repair cannot replace unrelated work.
     *
     * @param currentInput The command currently being edited.
     * @return An example to place in the input field without submitting it.
     */
    String getExampleFor(String currentInput) {
        if (!isDeadline) {
            return example;
        }
        String command = currentInput.trim().split("\\s+", 2)[0];
        if (!command.equalsIgnoreCase("deadline")) {
            return currentInput;
        }
        return createDeadlineExample(currentInput);
    }

    /**
     * Replaces the deadline date using the parser's literal, case-sensitive /by delimiter.
     * A trailing /by with no date is also treated as an unfinished delimiter.
     */
    private static String createDeadlineExample(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String description = parts.length > 1 ? parts[1] : "";
        int delimiterIndex = description.indexOf("/by ");
        if (delimiterIndex >= 0) {
            description = description.substring(0, delimiterIndex).trim();
        } else if (description.endsWith("/by")) {
            description = description.substring(0, description.length() - "/by".length()).trim();
        }
        if (description.isBlank()) {
            description = "Submit report";
        }
        return "deadline " + description + " /by " + EXAMPLE_DATE;
    }
}
