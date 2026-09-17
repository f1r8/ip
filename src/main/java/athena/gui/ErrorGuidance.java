package athena.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import athena.exception.AthenaException;
import athena.task.Deadline;

/**
 * Supplies courteous repair advice and editable examples for failed commands.
 *
 * @param hint Advice explaining how to repair the command.
 * @param example A command illustrating the accepted syntax.
 * @param isDeadline Whether the example can preserve an edited deadline description.
 */
record ErrorGuidance(String hint, String example, boolean isDeadline) {
    private static final String EXAMPLE_DATE = "2026-12-31 2359";
    private static final Pattern DATE_PARAMETER = Pattern.compile("(?<!\\S)/(by|from|to)(?=\\s|$)");
    private static final String INDEX_HINT = "Use list to inspect your task numbers. "
            + "This example assumes task 1 exists. ";

    /**
     * Returns advice for the command keyword without executing the command.
     *
     * @param input The failed command.
     * @return Advice and an example appropriate to the command.
     */
    static ErrorGuidance forInput(String input) {
        return forInput(input, List.of());
    }

    /**
     * Returns repair guidance using current task names to avoid an already-taken event example.
     */
    static ErrorGuidance forInput(String input, List<String> taskDescriptions) {
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
                    createEventExample(taskDescriptions), false);
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
     * Uses the first available numbered name when the default event name is already taken.
     */
    private static String createEventExample(List<String> taskDescriptions) {
        Set<String> existingNames = new HashSet<>();
        for (String description : taskDescriptions) {
            existingNames.add(description.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT));
        }
        String description = "Team meeting";
        int suffix = 1;
        while (existingNames.contains(description.toLowerCase(Locale.ROOT))) {
            description = "Team meeting " + suffix;
            suffix++;
        }
        return "event " + description + " /from 2026-12-31 1400 /to 2026-12-31 1500";
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
     * Retains the description before the first date parameter recognized by the parser.
     * Supplies a default description when the retained text cannot form a valid deadline.
     */
    private static String createDeadlineExample(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String description = parts.length > 1 ? parts[1] : "";
        Matcher matcher = DATE_PARAMETER.matcher(description);
        if (matcher.find()) {
            description = description.substring(0, matcher.start()).trim();
        }
        try {
            new Deadline(description + " /by " + EXAMPLE_DATE);
        } catch (AthenaException exception) {
            description = "Submit report";
        }
        return "deadline " + description + " /by " + EXAMPLE_DATE;
    }
}
