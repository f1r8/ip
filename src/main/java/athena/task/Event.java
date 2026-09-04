package athena.task;

import java.time.LocalDateTime;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

/**
 * Event class for Athena application
 */
public class Event extends Task {

    private final LocalDateTime from;
    private final LocalDateTime to;

    /**
     * Constructs an Event object.
     *
     * @param input String from command line.
     */
    public Event(String input) {
        input = input.replaceAll("/from ", "/");
        input = input.replaceAll("/to ", "/");
        String[] inputs = input.split("/");
        if (inputs.length < 3) {
            throw new AthenaException("Please provide an event with /from and /to times, Your Majesty.");
        }
        this(inputs[0].trim(), inputs[1].trim(), inputs[2].trim());
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
        this.from = DateParser.parse(from);
        this.to = DateParser.parse(to);
    }

    /**
     * Constructs an Event object.
     *
     * @param done true if the Event is completed, false otherwise.
     * @param description Describes the Event object.
     * @param from Date when the Event starts.
     * @param to Date when the Event ends.
     */
    public Event(boolean isDone, String description, String from, String to) {
        super(isDone, description);
        this.from = LocalDateTime.parse(from);
        this.to = LocalDateTime.parse(to);
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[E] {Task} (from: {startDate}, to {endDate})".
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + DateParser.formatOutput(this.from)
                + ", to: " + DateParser.formatOutput(this.to) + ")";
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "E{Separator}{Task}{Separator}{startDate}{Separator}{endDate}".
     */
    @Override
    public String getSaveString() {
        return "E" + Storage.SAVE_SEPARATOR + super.getSaveString() + Storage.SAVE_SEPARATOR
                + this.from + Storage.SAVE_SEPARATOR + this.to;
    }
}
