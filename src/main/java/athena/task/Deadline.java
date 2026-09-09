package athena.task;

import java.time.LocalDateTime;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

/**
 * Represents an Athena task that must be completed by a specific date and time.
 */
public class Deadline extends Task {
    private static final String DATE_DELIMITER = "/by ";

    private final LocalDateTime deadline;

    /**
     * Constructs a Deadline object.
     *
     * @param input String from command line.
     */
    public Deadline(String input) {
        int dateDelimiterIndex = input.indexOf(DATE_DELIMITER);
        if (dateDelimiterIndex < 0) {
            throw new AthenaException("Please provide a deadline and /by date, Your Majesty.");
        }

        String description = input.substring(0, dateDelimiterIndex).trim();
        String by = input.substring(dateDelimiterIndex + DATE_DELIMITER.length()).trim();
        this(description, by);
    }

    /**
     * Constructs a Deadline object.
     *
     * @param description Describes the Deadline object.
     * @param by The time that the Deadline object is due by.
     */
    public Deadline(String description, String by) {
        super(description);
        this.deadline = DateParser.parse(by);
    }

    /**
     * Constructs a Deadline object.
     *
     * @param isDone {@code true} if the deadline is complete, {@code false} otherwise.
     * @param description Description of the deadline.
     * @param by Saved deadline date and time.
     */
    public Deadline(boolean isDone, String description, String by) {
        super(isDone, description);
        this.deadline = LocalDateTime.parse(by);
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[D] {Task} (by: {Date})".
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + DateParser.formatOutput(this.deadline) + ")";
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "D{Separator}{Task}{Separator}{Date}".
     */
    @Override
    public String getSaveString() {
        return "D" + Storage.SAVE_SEPARATOR + super.getSaveString()
                + Storage.SAVE_SEPARATOR + this.deadline;
    }
}
