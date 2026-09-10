package athena.task;

import java.time.LocalDateTime;
import java.util.List;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

/**
 * Represents an Athena task scheduled between two dates and times.
 */
public class Event extends Task {
    private static final String START_DELIMITER = "/from ";
    private static final String END_DELIMITER = "/to ";
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
        int startDelimiterIndex = input.indexOf(START_DELIMITER);
        if (startDelimiterIndex < 0) {
            throw new AthenaException(MISSING_DETAILS_MESSAGE);
        }

        int endDelimiterIndex = input.indexOf(END_DELIMITER,
                startDelimiterIndex + START_DELIMITER.length());
        if (endDelimiterIndex < 0) {
            throw new AthenaException(MISSING_DETAILS_MESSAGE);
        }

        String description = input.substring(0, startDelimiterIndex).trim();
        String from = input.substring(startDelimiterIndex + START_DELIMITER.length(),
                endDelimiterIndex).trim();
        String to = input.substring(endDelimiterIndex + END_DELIMITER.length()).trim();
        this(description, from, to);
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
