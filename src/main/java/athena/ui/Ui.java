package athena.ui;

import athena.task.Task;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Reads console input and displays Athena's user-facing messages.
 */
public class Ui {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = """
                _  _____ _   _ _____ _   _    _   \s
               / \\|_   _| | | | ____| \\ | |  / \\  \s
              / _ \\ | | | |_| |  _| |  \\| | / _ \\ \s
             / ___ \\| | |  _  | |___| |\\  |/ ___ \\\s
            /_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\""";

    private final PrintStream outputStream;
    private final Scanner scanner;

    /**
     * Constructs a Ui object.
     *
     * @param inputStream Console input to read.
     * @param outputStream Console output to write.
     */
    public Ui(InputStream inputStream, PrintStream outputStream) {
        this.outputStream = outputStream;
        this.scanner = new Scanner(inputStream);
    }

    /**
     * Prints the output to the specified outputStream.
     *
     * @param lines Strings to be printed.
     */
    public void println(String... lines) {
        for (String line : lines) {
            outputStream.println(line);
        }
    }

    /**
     * Checks if the inputStream has another line.
     *
     * @return true if there is another line, false otherwise.
     */
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    /**
     * Gets the next line of the inputStream.
     *
     * @return The next line of the inputStream.
     */
    public String readNextLine() {
        return scanner.nextLine();
    }

    /**
     * Shows whether the saved tasks were loaded successfully.
     *
     * @param isLoaded true if tasks were loaded, false if no data file was found.
     * @param path path of the data file.
     */
    public void showLoadingStatus(boolean isLoaded, String path) {
        println(isLoaded
                ? "Items successfully loaded."
                : "File not found at: " + path);
    }

    /**
     * Shows Athena's banner and greeting when the application starts.
     */
    public void showWelcome() {
        println(DIVIDER);
        println(BANNER);
        println("Hello, Your Majesty! I'm Athena.");
        println("How may I assist you, Your Majesty?");
        println(DIVIDER);
    }

    /**
     * Shows a divider between commands and their responses.
     */
    public void showDivider() {
        println(DIVIDER);
    }

    /**
     * Shows Athena's farewell message.
     */
    public void showGoodbye() {
        println(DIVIDER);
        println("Farewell, Your Majesty. I hope to serve you again soon!");
        println(DIVIDER);
    }

    /**
     * Shows all tasks in their current order.
     *
     * @param tasks tasks to display.
     */
    public void showTaskList(List<Task> tasks) {
        println("Your Majesty, here are the tasks in your list:");
        showNumberedTasks(tasks);
    }

    /**
     * Shows the tasks matching a search command.
     *
     * @param tasks matching tasks to display.
     */
    public void showMatchingTasks(List<Task> tasks) {
        println("Your Majesty, here are the matching tasks in your list:");
        showNumberedTasks(tasks);
    }

    /**
     * Prompts the user to provide a keyword for a find command.
     */
    public void showMissingFindKeyword() {
        println("What shall I search for, Your Majesty?");
    }

    /**
     * Shows the response for an unrecognized command.
     */
    public void showUnknownCommand() {
        println("*Athena blinks her eyes, unsure of what you want, tilting "
                + "\nher head slightly as the meaning of your words slips just "
                + "\nout of reach.*");
    }

    /**
     * Shows the result of marking or unmarking a task.
     *
     * @param task task whose status changed.
     * @param isMarked true if the task was marked done, false if it was unmarked.
     */
    public void showTaskStatusChanged(Task task, boolean isMarked) {
        println(isMarked
                ? "Excellent, Your Majesty! I've marked this task as done:"
                : "Certainly, Your Majesty. I've marked this task as not done yet:");
        showTask(task);
    }

    /**
     * Prompts the user to provide a task number for a mark command.
     */
    public void showMissingMarkIndex() {
        println("Which task shall I mark, Your Majesty?");
    }

    /**
     * Shows that a requested task number is outside the task list.
     */
    public void showInvalidTaskIndex() {
        println("Your Majesty, there aren't that many tasks in the list.");
    }

    /**
     * Shows an application error to the user.
     *
     * @param message explanation of the error.
     */
    public void showError(String message) {
        println(message);
    }

    /**
     * Shows a newly added task and the updated task count.
     *
     * @param task task that was added.
     * @param taskCount number of tasks after the addition.
     */
    public void showTaskAdded(Task task, int taskCount) {
        println("As you command, Your Majesty. I've added this task:");
        showTask(task);
        showTaskCount(taskCount);
    }

    /**
     * Shows a deleted task and the updated task count.
     *
     * @param task task that was deleted.
     * @param taskCount number of tasks after the deletion.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        println("As you wish, Your Majesty. I've removed this task:");
        showTask(task);
        showTaskCount(taskCount);
    }

    /**
     * Prompts the user to provide a task number for a delete command.
     */
    public void showMissingDeleteIndex() {
        println("Which task shall I remove, Your Majesty?");
    }

    private void showNumberedTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            println(i + 1 + ". " + tasks.get(i));
        }
    }

    private void showTask(Task task) {
        println("  " + task);
    }

    private void showTaskCount(int taskCount) {
        println("You now have " + taskCount + " tasks in the list, Your Majesty.");
    }

    private void println(String output) {
        outputStream.println(output);
    }
}
