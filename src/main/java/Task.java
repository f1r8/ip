/**
 * This class is used to create Task Objects
 */
public class Task {
    private String name;
    private boolean done;
    public Task(String name) {
        this.done = false;
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

    public String toString() {
        return "[" + this.getStatusIcon() + "] " + this.name;
    }

    public String getCreateMsg() {
        return "As you command, Your Majesty. I've added this task:";
    }

}
