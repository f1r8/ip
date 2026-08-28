package athena.task;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

import java.time.LocalDateTime;

public class Event extends Task {

    private final LocalDateTime from;
    private final LocalDateTime to;

    public Event(String input) {
        input = input.replaceAll("/from ", "/");
        input = input.replaceAll("/to ", "/");
        String[] inputs = input.split("/");
        if (inputs.length < 3) {
            throw new AthenaException("Please provide an event with /from and /to times, Your Majesty.");
        }
        this(inputs[0].trim(), inputs[1].trim(), inputs[2].trim());
    }

    public Event(String description, String from, String to) {
        super(description);
        this.from = DateParser.parse(from);
        this.to = DateParser.parse(to);
    }

    public Event(boolean isDone, String description, String from, String to) {
        super(isDone, description);
        this.from = LocalDateTime.parse(from);
        this.to = LocalDateTime.parse(to);
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + DateParser.formatOutput(this.from)
                + ", to: " + DateParser.formatOutput(this.to) + ")";
    }

    @Override
    public String getSaveString() {
        return "E" + Storage.SAVE_SEPARATOR + super.getSaveString() + Storage.SAVE_SEPARATOR
                + this.from + Storage.SAVE_SEPARATOR + this.to;
    }
}
