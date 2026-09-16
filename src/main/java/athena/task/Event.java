package athena.task;

import java.time.LocalDateTime;
import java.util.List;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.parser.DateTaskInput;
import athena.storage.Storage;

/**
 * Represents an Athena task scheduled between two dates and times.
 */
public class Event extends Task {
    private static final String MISSING_DETAILS_MESSAGE =
            "Please provide an event with /from and /to times, Your Majesty.";

    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    /**
     * Constructs an Event object.
     *
     * @param input String from command line.
     */
    public Event(String input) {
        String[] parts = DateTaskInput.parse(input, MISSING_DETAILS_MESSAGE, "from", "to");
        this(parts[0], parts[1], parts[2]);
    }

    /**
     * Constructs an Event object.
     *
     * @param description Describes the Event object.
     * @param from Date when the Event starts.
     * @param to Date when the Event ends.
     */
    public Event(String description, String from, String to) {
        super(description);
        this.startDateTime = DateParser.parse(from);
        this.endDateTime = DateParser.parse(to);
        validateDateOrder();
    }

    /**
     * Constructs an Event object.
     *
     * @param isDone {@code true} if the event is completed, {@code false} otherwise.
     * @param description Describes the Event object.
     * @param from Date when the Event starts.
     * @param to Date when the Event ends.
     */
    public Event(boolean isDone, String description, String from, String to) {
        this(isDone, description, from, to, List.of());
    }

    /**
     * Constructs an Event object with saved tags.
     *
     * @param isDone {@code true} if the event is completed, {@code false} otherwise.
     * @param description Describes the Event object.
     * @param from Date when the Event starts.
     * @param to Date when the Event ends.
     * @param tags Saved tags to restore.
     */
    public Event(boolean isDone, String description, String from, String to, List<Tag> tags) {
        super(isDone, description, tags);
        this.startDateTime = LocalDateTime.parse(from);
        this.endDateTime = LocalDateTime.parse(to);
        validateDateOrder();
    }

    private void validateDateOrder() {
        if (!startDateTime.isBefore(endDateTime)) {
            throw new AthenaException("An event must end after it starts, Your Majesty.");
        }
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[E] {Task} (from: {startDate}, to {endDate})".
     */
    @Override
    protected String getDisplayStringWithoutTags() {
        return "[E]" + super.getDisplayStringWithoutTags()
                + " (from: " + DateParser.formatOutput(this.startDateTime)
                + ", to: " + DateParser.formatOutput(this.endDateTime) + ")";
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "E{Separator}{Task}{Separator}{startDate}{Separator}{endDate}".
     */
    @Override
    protected String getSaveStringWithoutTags() {
        return "E" + Storage.SAVE_SEPARATOR + super.getSaveStringWithoutTags() + Storage.SAVE_SEPARATOR
                + this.startDateTime + Storage.SAVE_SEPARATOR + this.endDateTime;
    }
}
