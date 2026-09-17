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
        println(tasks.isEmpty() ? "Your list awaits its first task. Try todo Read a book."
                : "Your tasks, Your Majesty.");
        showTasks(tasks, tasks);
    }

    @Override
    public void showMatchingTasks(List<Task> tasks, List<Task> allTasks) {
        println(tasks.isEmpty() ? "No matching tasks. Try another keyword or use list to see all tasks."
                : "Here are your matching tasks.");
        showTasks(tasks, allTasks);
    }

    @Override
    public void showTaskAdded(Task task, int taskCount) {
        println("As you wish. I've added this task. " + getTaskCount(taskCount));
        showTask(task);
    }

    @Override
    public void showTaskDeleted(Task task, int taskCount) {
        println("Certainly. I've removed this task. " + getTaskCount(taskCount));
        showTask(task);
    }

    @Override
    public void showTaskStatusChanged(Task task, boolean isMarked) {
        println(isMarked ? "Well done, Your Majesty. This task is complete."
                : "Certainly. This task is back on your to-do list.");
        showTask(task);
    }

    @Override
    public void showTaskTagsChanged(Task task, boolean isAdded) {
        println(isAdded ? "I've added the tags to this task." : "I've removed the tags from this task.");
        showTask(task);
    }

    @Override
    public void showNoTagChanges(Task task) {
        println("These tags need no changes.");
        showTask(task);
    }

    private void showTask(Task task) {
        tasks = List.of(TaskView.from(task, 0));
    }

    private void showTasks(List<Task> tasks, List<Task> allTasks) {
        List<TaskView> views = new ArrayList<>();
        for (Task task : tasks) {
            views.add(TaskView.from(task, allTasks.indexOf(task) + 1));
        }
        this.tasks = List.copyOf(views);
    }

    private String getTaskCount(int taskCount) {
        return taskCount + (taskCount == 1 ? " task" : " tasks") + " in your list.";
    }
}
