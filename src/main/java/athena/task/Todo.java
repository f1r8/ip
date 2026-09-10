package athena.task;

import java.util.List;

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
        this(false, description);
    }

    /**
     * Constructs a Todo object.
     *
     * @param isDone {@code true} if the todo is done, {@code false} otherwise.
     * @param description Describes the Todo object.
     */
    public Todo(boolean isDone, String description) {
        this(isDone, description, List.of());
    }

    /**
     * Constructs a Todo object with saved tags.
     *
     * @param isDone {@code true} if the todo is done, {@code false} otherwise.
     * @param description Describes the Todo object.
     * @param tags Saved tags to restore.
     */
    public Todo(boolean isDone, String description, List<Tag> tags) {
        if (description.isEmpty()) {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(isDone, description, tags);
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "[T] {Task}".
     */
    @Override
    protected String getDisplayStringWithoutTags() {
        return "[T]" + super.getDisplayStringWithoutTags();
    }

    /**
     * {@inheritDoc}
     *
     * @return The task in the format "T{Separator}{Task}".
     */
    @Override
    protected String getSaveStringWithoutTags() {
        return "T" + Storage.SAVE_SEPARATOR + super.getSaveStringWithoutTags();
    }
}
