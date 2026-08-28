package athena.task;

import athena.exception.AthenaException;
import athena.storage.Storage;

public class Todo extends Task {

    public Todo(String description) {
        if (description == "") {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(description);
    }

    public Todo(boolean isDone, String description) {
        if (description == "") {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(isDone, description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }

    @Override
    public String getSaveString() {
        return "T" + Storage.SAVE_SEPARATOR + super.getSaveString();
    }
}
