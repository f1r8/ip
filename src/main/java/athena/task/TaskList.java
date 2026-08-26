package athena.task;

import java.util.ArrayList;

public class TaskList {
    private ArrayList<Task> items = new ArrayList<>();

    public ArrayList<Task> getItems() {
        return items;
    }

    public int size() {
        return items.size();
    }
    public Task get(int index) {
        return items.get(index);
    }
    public void add(Task task) {
        items.add(task);
    }
    public Task remove(int index) {
        return items.remove(index);
    }
}
