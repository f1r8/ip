package athena.task;

import athena.exception.AthenaException;
import athena.storage.Storage;

/**
 * Represents the common state and behavior of an Athena task.
 */
public abstract class Task {
    private static final String SAVE_STATUS_DONE = "1";
    private static final String SAVE_STATUS_NOT_DONE = "0";

    private final String name;
    private boolean isDone;

    /**
     * Constructs an incomplete task with the specified description.
     *
     * @param name Description of the task.
     */
    public Task(String name) {
        this(false, name);
    }

    /**
     * Constructs a Task object.
     *
     * @param isDone {@code true} if the task is complete, {@code false} otherwise.
     * @param name Description of the Task object.
     */
    public Task(boolean isDone, String name) {
        if (name.isEmpty()) {
            throw new AthenaException("Task name cannot be empty");
        }
        this.isDone = isDone;
        this.name = name;
    }

    /**
     * Marks a Task object as done.
     */
    public void markDone() {
        this.isDone = true;
    }

    /**
     * Unmarks a Task object as not done.
     */
    public void unmarkDone() {
        this.isDone = false;
    }

    /**
     * Gets the status icon of whether the task is done.
     *
     * @return String of the status icon.
     */
    public String getStatusIcon() {
        return this.isDone ? "X" : " ";
    }

    /**
     * {@inheritDoc}
     *
     * @return The Task in the format "[{StatusIcon}]{Description}".
     */
    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.name;
    }

    /**
     * Returns the persisted completion status.
     *
     * @return Save-file representation of the completion status.
     */
    public String getSaveStatus() {
        return this.isDone ? SAVE_STATUS_DONE : SAVE_STATUS_NOT_DONE;
    }

    /**
     * Returns whether a saved status represents a completed task.
     *
     * @param status Saved completion status.
     * @return {@code true} if the task is complete, {@code false} otherwise.
     */
    public static boolean isDoneFromStatus(String status) {
        if (status.equals(SAVE_STATUS_DONE)) {
            return true;
        } else if (status.equals(SAVE_STATUS_NOT_DONE)) {
            return false;
        }
        throw new AthenaException("Error converting save string to num: " + status);
    }

    /**
     * Generates the storage String for the Task object.
     *
     * @return Storage String.
     */
    public String getSaveString() {
        return getSaveStatus() + Storage.SAVE_SEPARATOR + this.name;
    }
}
