package athena.task;

import java.time.LocalDateTime;
import java.util.List;

import athena.parser.DateParser;
import athena.parser.DateTaskInput;
import athena.storage.Storage;

/**
 * Represents an Athena task that must be completed by a specific date and time.
 */
public class Deadline extends Task {
    private final LocalDateTime deadline;

    /**
     * Constructs a Deadline object.
     *
     * @param input String from command line.
     */
    public Deadline(String input) {
        String[] parts = DateTaskInput.parse(input,
                "Please provide a deadline and /by date, Your Majesty.", "by");
        this(parts[0], parts[1]);
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
        this(isDone, description, by, List.of());
    }

    /**
     * Constructs a Deadline object with saved tags.
     *
     * @param isDone {@code true} if the deadline is complete, {@code false} otherwise.
     * @param description Description of the deadline.
     * @param by Saved deadline date and time.
     * @param tags Saved tags to restore.
     */
    public Deadline(boolean isDone, String description, String by, List<Tag> tags) {
        super(isDone, description, tags);
        this.deadline = LocalDateTime.parse(by);
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[D] {Task} (by: {Date})".
     */
    @Override
    protected String getDisplayStringWithoutTags() {
        return "[D]" + super.getDisplayStringWithoutTags()
                + " (by: " + DateParser.formatOutput(this.deadline) + ")";
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "D{Separator}{Task}{Separator}{Date}".
     */
    @Override
    protected String getSaveStringWithoutTags() {
        return "D" + Storage.SAVE_SEPARATOR + super.getSaveStringWithoutTags()
                + Storage.SAVE_SEPARATOR + this.deadline;
    }
}
