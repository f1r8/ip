package athena.task;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

import java.time.LocalDateTime;

public class Deadline extends Task {

    protected LocalDateTime by;

    public Deadline(String input) {
        input = input.replaceAll("/by ", "/");
        String[] inputs = input.split("/");
        if (inputs.length < 2) {
            throw new AthenaException("Please provide a deadline and /by date, Your Majesty.");
        }
        this(inputs[0].trim(), inputs[1].trim());
    }

    public Deadline(String description, String by) {
        super(description);
        this.by = DateParser.parse(by);
    }

    public Deadline(boolean done, String description, String by) {
        super(done, description);
        this.by = LocalDateTime.parse(by);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + DateParser.formatOutput(this.by) + ")";
    }

    @Override
    public String toSaveString(){
        return "D" + Storage.SEPARATOR + super.toSaveString() + Storage.SEPARATOR + this.by;
    }
}
