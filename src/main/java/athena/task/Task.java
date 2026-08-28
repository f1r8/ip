package athena.task;

import athena.exception.AthenaException;
import athena.storage.Storage;

/**
 * Task class for Athena application.
 */
public abstract class Task {
    private String name;
    private boolean done;
    public Task(String name) {
        this(false, name);
    }

    /**
     * Constructs a Task object.
     *
     * @param done true if the task is complete, false otherwise.
     * @param name Description of the Task object.
     */
    public Task(Boolean done, String name) {
        if (name.equals("")) {
            throw new AthenaException("Task name cannot be empty");
        }
        this.done = done;
        this.name = name;
    }

    /**
     * Sets a Task object as done or not done.
     *
     * @param done true if the task is complete, false otherwise.
     */
    public void setDone(boolean done) {
        this.done = done;
    }

    /**
     * Marks a Task object as done.
     *
     * @return The String to be printed to command line.
     */
    public String markDone() {
        this.setDone(true);
        return "Excellent, Your Majesty! I've marked this task as done:";
    }

    /**
     * Unmarks a Task object as not done.
     *
     * @return The String to be printed to command line.
     */
    public String unmarkDone() {
        this.setDone(false);
        return "Certainly, Your Majesty. I've marked this task as not done yet:";
    }

    /**
     * Gets the status icon of whether the task is done.
     *
     * @return String of the status icon.
     */
    public String getStatusIcon() {
        return this.done ? "X" : " ";
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
        return this.done ? "1" : "0";
    }

    /**
     * Get the status from the storage status icon.
     *
     * @param status String of the storage status icon.
     * @return true if the task is complete, false otherwise.
     */
    public static boolean getStatusFromString(String status) {
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
    public String toSaveString() {
        return getStoreStatusIcon() + Storage.SEPARATOR + this.name;
    }

    /**
     * Retrieves the message on task creation.
     *
     * @return Task creation message.
     */
    public static String getCreateMsg() {
        return "As you command, Your Majesty. I've added this task:";
    }

}
