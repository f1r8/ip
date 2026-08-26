package athena.task;

import athena.exception.AthenaException;
import athena.storage.Save;

/**
 * This class is used to create athena.task.Task Objects
 */
public abstract class Task {
    private String name;
    private boolean done;
    public Task(String name) {
        this.done = false;
        this.name = name;
    }

    public Task(Boolean done, String name) {
        this.done = done;
        this.name = name;
    }

    public void setDone(boolean done) {
        this.done = done;
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
        return this.done ? "X" : " ";
    }

    @Override
    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.name;
    }

    public String getStoreStatusIcon() {
        return this.done ? "1" : "0";
    }

    public static boolean getStatusFromString(String status) {
        if (status.equals("1")) {
            return true;
        } else if (status.equals("0")) {
            return false;
        }
        throw new AthenaException("Error converting save string to num: " + status);
    }

    public String toSaveString() {
        return getStoreStatusIcon() + Save.SEPARATOR + this.name;
    }

    public String getCreateMsg() {
        return "As you command, Your Majesty. I've added this task:";
    }

}
