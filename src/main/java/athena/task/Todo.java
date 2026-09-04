package athena.task;

import athena.exception.AthenaException;
import athena.storage.Storage;

/**
 * Represents an Athena task without an associated date or time.
 */
public class Todo extends Task {

    /**
     * Constructs a Todo object.
     *
     * @param description Command-line input used to construct the todo.
     */
    public Todo(String description) {
        if (description.isEmpty()) {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(description);
    }

    /**
     * Constructs a Todo object.
     *
     * @param isDone true if the todo is done, false otherwise.
     * @param description Describes the Todo object.
     */
    public Todo(boolean isDone, String description) {
        if (description.isEmpty()) {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(isDone, description);
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[T] {Task}".
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "T{Separator}{Task}".
     */
    @Override
    public String getSaveString() {
        return "T" + Storage.SAVE_SEPARATOR + super.getSaveString();
    }
}
