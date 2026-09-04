package athena.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the in-memory collection of Athena tasks.
 */
public class TaskList {
    private final List<Task> tasks = new ArrayList<>();

    /**
     * Constructs an empty task list.
     */
    public TaskList() {
    }

    /**
     * Retrieves the items from the TaskList wrapper.
     *
     * @return The ArrayList of Tasks.
     */
    public List<Task> getTasks() {
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
