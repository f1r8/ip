package athena.task;

import java.util.ArrayList;

/**
 * TaskList class of the Athena application.
 */
public class TaskList {
    private ArrayList<Task> tasks = new ArrayList<>();

    /**
     * Retrieves the items from the TaskList wrapper.
     *
     * @return The ArrayList of Tasks.
     */
    public ArrayList<Task> getItems() {
        return tasks;
    }

    /**
     * Gets the size of the TaskList object.
     *
     * @return The size.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Gets the Task at the specified index.
     *
     * @param index Index of the task to retrieve.
     * @return The task retrieved at the index.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a Task object to the TaskList.
     *
     * @param task Task to be added.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes a Task at the specified index.
     *
     * @param index Index of the Task to be removed.
     * @return The removed Task.
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }
}
