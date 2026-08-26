package athena.task;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Save;

import java.time.LocalDateTime;

public class Event extends Task {

    protected LocalDateTime from;
    protected LocalDateTime to;

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

    public Event(boolean done, String description, String from, String to) {
        super(done, description);
        this.from = LocalDateTime.parse(from);
        this.to = LocalDateTime.parse(to);
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + DateParser.formatOutput(this.from) + ", to: " + DateParser.formatOutput(this.to)  + ")";
    }

    @Override
    public String toSaveString(){
        return "E" + Save.SEPARATOR + super.toSaveString() + Save.SEPARATOR +  this.from + Save.SEPARATOR  + this.to;
    }
}
