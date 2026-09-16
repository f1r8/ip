package athena.gui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import athena.task.Task;
import athena.ui.Ui;

/**
 * Presents concise GUI captions and task snapshots while preserving the console's error guidance.
 */
public class GuiUi extends Ui {
    private List<TaskView> tasks = List.of();

    /**
     * Constructs a GUI presenter using the supplied command output stream.
     */
    public GuiUi(PrintStream outputStream) {
        super(System.in, outputStream);
    }

    public List<TaskView> getTasks() {
        return tasks;
    }

    /**
     * Clears the previous command's task details before processing another command.
     */
    public void clearTasks() {
        tasks = List.of();
    }

    @Override
    public void showTaskList(List<Task> tasks) {
        println(tasks.isEmpty() ? "Your list is empty, Your Majesty." : "Your tasks, Your Majesty.");
        showTasks(tasks);
    }

    @Override
    public void showMatchingTasks(List<Task> tasks) {
        println(tasks.isEmpty() ? "No matching tasks, Your Majesty." : "Matching tasks, Your Majesty.");
        showTasks(tasks);
    }

    @Override
    public void showTaskAdded(Task task, int taskCount) {
        println("Added, Your Majesty. " + getTaskCount(taskCount));
        showTask(task);
    }

    @Override
    public void showTaskDeleted(Task task, int taskCount) {
        println("Removed, Your Majesty. " + getTaskCount(taskCount));
        showTask(task);
    }

    @Override
    public void showTaskStatusChanged(Task task, boolean isMarked) {
        println(isMarked ? "Marked done, Your Majesty." : "Marked to do, Your Majesty.");
        showTask(task);
    }

    @Override
    public void showTaskTagsChanged(Task task, boolean isAdded) {
        println(isAdded ? "Tags added, Your Majesty." : "Tags removed, Your Majesty.");
        showTask(task);
    }

    @Override
    public void showNoTagChanges(Task task) {
        println("Tags unchanged, Your Majesty.");
        showTask(task);
    }

    private void showTask(Task task) {
        tasks = List.of(TaskView.from(task, 0));
    }

    private void showTasks(List<Task> tasks) {
        List<TaskView> views = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            views.add(TaskView.from(tasks.get(i), i + 1));
        }
        this.tasks = List.copyOf(views);
    }

    private String getTaskCount(int taskCount) {
        return taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list.";
    }
}
