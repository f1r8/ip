package athena.task;

import athena.exception.AthenaException;
import athena.storage.Storage;

/**
 * Task class for Athena application.
 */
public abstract class Task {
    private final String name;
    private boolean isDone;

    public Task(String name) {
        this(false, name);
    }

    /**
     * Constructs a Task object.
     *
     * @param isDone true if the task is complete, false otherwise.
     * @param name Description of the Task object.
     */
    public Task(Boolean isDone, String name) {
        if (name.isEmpty()) {
            throw new AthenaException("Task name cannot be empty");
        }
        this.isDone = isDone;
        this.name = name;
    }

    /**
     * Sets a Task object as done or not done.
     *
     * @param isDone true if the task is complete, false otherwise.
     */
    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    /**
     * Marks a Task object as done.
     */
    public void markDone() {
        this.setDone(true);
    }

    /**
     * Unmarks a Task object as not done.
     */
    public void unmarkDone() {
        this.setDone(false);
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
     * Gets the Storage variant of the status icon.
     *
     * @return String of the storage status icon.
     */
    public String getStoreStatusIcon() {
        return this.isDone ? "1" : "0";
    }

    /**
     * Get the status from the storage status icon.
     *
     * @param status String of the storage status icon.
     * @return true if the task is complete, false otherwise.
     */
    public static boolean isDoneFromStatus(String status) {
        if (status.equals("1")) {
            return true;
        } else if (status.equals("0")) {
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
        return getStoreStatusIcon() + Storage.SAVE_SEPARATOR + this.name;
    }

}
