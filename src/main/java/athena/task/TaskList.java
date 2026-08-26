package athena.task;

import java.util.ArrayList;

public class TaskList {
    private static final ArrayList<Task> items = new ArrayList<>();

    public static ArrayList<Task> getItems() {
        return items;
    }

    public static int size() {
        return items.size();
    }
    public static Task get(int index) {
        return items.get(index);
    }
    public static void add(Task task) {
        items.add(task);
    }
    public static Task remove(int index) {
        return items.remove(index);
    }
}
