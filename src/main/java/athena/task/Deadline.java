package athena.task;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

import java.time.LocalDateTime;

/**
 * Deadline class of the Athena application.
 */
public class Deadline extends Task {

    private final LocalDateTime by;

    /**
     * Constructs a Deadline object.
     *
     * @param input String from command line.
     */
    public Deadline(String input) {
        input = input.replaceAll("/by ", "/");
        String[] inputs = input.split("/");
        if (inputs.length < 2) {
            throw new AthenaException("Please provide a deadline and /by date, Your Majesty.");
        }
        this(inputs[0].trim(), inputs[1].trim());
    }

    /**
     * Constructs a Deadline object.
     *
     * @param description Describes the Deadline object.
     * @param by The time that the Deadline object is due by.
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = DateParser.parse(by);
    }

    /**
     * Constructs a Deadline object.
     *
     * @param isDone
     * @param description
     * @param by
     */
    public Deadline(boolean isDone, String description, String by) {
        super(isDone, description);
        this.by = LocalDateTime.parse(by);
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[D] {Task} (by: {Date})".
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + DateParser.formatOutput(this.by) + ")";
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "D{Separator}{Task}{Separator}{Date}".
     */
    @Override
    public String getSaveString() {
        return "D" + Storage.SAVE_SEPARATOR + super.getSaveString() + Storage.SAVE_SEPARATOR + this.by;
    }
}
