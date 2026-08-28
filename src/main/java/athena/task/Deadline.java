package athena.task;

import athena.exception.AthenaException;
import athena.parser.DateParser;
import athena.storage.Storage;

import java.time.LocalDateTime;

public class Deadline extends Task {

    private final LocalDateTime by;

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

    public Deadline(boolean isDone, String description, String by) {
        super(isDone, description);
        this.by = LocalDateTime.parse(by);
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + DateParser.formatOutput(this.by) + ")";
    }

    @Override
    public String getSaveString() {
        return "D" + Storage.SAVE_SEPARATOR + super.getSaveString() + Storage.SAVE_SEPARATOR + this.by;
    }
}
