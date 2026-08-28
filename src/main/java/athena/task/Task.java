package athena.task;

import athena.exception.AthenaException;
import athena.storage.Storage;

/**
 * This class is used to create athena.task.Task Objects
 */
public abstract class Task {
    private final String name;
    private boolean isDone;

    public Task(String name) {
        this(false, name);
    }

    public Task(Boolean isDone, String name) {
        if (name.isEmpty()) {
            throw new AthenaException("Task name cannot be empty");
        }
        this.isDone = isDone;
        this.name = name;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    public String markDone() {
        this.setDone(true);
        return "Excellent, Your Majesty! I've marked this task as done:";
    }

    public String unmarkDone() {
        this.setDone(false);
        return "Certainly, Your Majesty. I've marked this task as not done yet:";
    }

    public String getStatusIcon() {
        return this.isDone ? "X" : " ";
    }

    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.name;
    }

    public String getStoreStatusIcon() {
        return this.isDone ? "1" : "0";
    }

    public static boolean isDoneFromStatus(String status) {
        if (status.equals("1")) {
            return true;
        } else if (status.equals("0")) {
            return false;
        }
        throw new AthenaException("Error converting save string to num: " + status);
    }

    public String getSaveString() {
        return getStoreStatusIcon() + Storage.SAVE_SEPARATOR + this.name;
    }

    public String getCreateMsg() {
        return "As you command, Your Majesty. I've added this task:";
    }

}
