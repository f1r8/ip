package athena.task;

import athena.exception.AthenaException;
import athena.storage.Save;

public class Todo extends Task {

    public Todo(String description) {
        if (description == "") {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(description);
    }

    public Todo(boolean done, String description) {
        if (description == "") {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(done, description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }

    @Override
    public String toSaveString(){
        return "T" + Save.SEPARATOR + super.toSaveString();
    }
}
