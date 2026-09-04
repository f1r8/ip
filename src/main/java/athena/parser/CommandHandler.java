package athena.parser;

import java.util.function.Function;

import athena.Athena;
import athena.exception.AthenaException;
import athena.storage.Storage;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;

/**
 * Handles Commands for Athena application.
 */
public class CommandHandler {

    private static final String UNKNOWN_COMMAND_MESSAGE = "*Athena blinks her eyes, unsure of what you want, "
            + "tilting \nher head slightly as the meaning of your words slips just \nout of reach.*";

    private final Storage storage;
    private final Ui ui;
    private final TaskList taskList;

    /**
     * Constructs a CommandHandler object.
     *
     * @param storage Storage object for persistent data storage.
     * @param ui Ui object for printing and receiving inputs.
     * @param taskList TaskList object that stores data in memory.
     */
    public CommandHandler(Storage storage, Ui ui, TaskList taskList) {
        this.storage = storage;
        this.ui = ui;
        this.taskList = taskList;
    }

    /**
     * Specifies valid commands.
     */
    private enum Command {
        BYE("bye"),
        LIST("list"),
        MARK("mark"),
        UNMARK("unmark"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event"),
        DELETE("delete"),
        FIND("find"),
        UNKNOWN("");

        private final String keyword;

        Command(String keyword) {
            this.keyword = keyword;
        }

        private static Command search(String keyword) {
            for (Command command : values()) {
                if (command.keyword.equalsIgnoreCase(keyword)) {
                    return command;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Orchestrator for various Athena application commands.
     *
     * @param nextLine takes String as input from Ui.
     * @return true if the user wants to terminate the application.
     */
    public boolean isExitCommand(String nextLine) {
        nextLine = nextLine.trim();
        String[] commandParts = nextLine.split("\\s+", 2);
        Command command = Command.search(commandParts[0]);
        String arguments = commandParts.length > 1 ? commandParts[1] : "";

        switch (command) {
            case BYE:
                ui.println(getExitMessage());
                return true;
            case LIST:
                ui.println("Your Majesty, here are the tasks in your list:");
                for (int i = 0; i < taskList.size(); i++) {
                    ui.println(i + 1 + ". " + taskList.get(i));
                }
                break;
            case MARK:
                handleMarkCommand(arguments, true);
                break;
            case UNMARK:
                handleMarkCommand(arguments, false);
                break;
            case TODO:
                handleAddCommand(arguments, Todo::new);
                break;
            case DEADLINE:
                handleAddCommand(arguments, Deadline::new);
                break;
            case EVENT:
                handleAddCommand(arguments, Event::new);
                break;
            case DELETE:
                handleDeleteCommand(arguments);
                break;
            case FIND:
                ui.println("Your Majesty, here are the matching tasks in your list:");
                int counter = 1;
                for (int i = 0; i < taskList.size(); i++) {
                    String s = taskList.get(i).toString();
                    if (s.contains(arguments)) {
                        ui.println(counter++ + ". " + s);
                    }
                }
                break;
            default:
                ui.println(UNKNOWN_COMMAND_MESSAGE);
                break;
        }
        return false;
    }

    /**
     * Helper for handling mark/unmark.
     *
     * @param arguments String arguments for handling mark/unmark.
     * @param shouldMarkAsDone true if task should be marked, false if unmarked.
     */
    private void handleMarkCommand(String arguments, boolean shouldMarkAsDone) {
        try {
            int idx = Integer.parseInt(arguments.trim());
            Task task = taskList.get(idx - 1);
            ui.println(shouldMarkAsDone
                    ? task.markDone()
                    : task.unmarkDone());
            ui.println("  " + task);
            storage.writeItems(taskList.getItems());
        } catch (NumberFormatException e) {
            ui.println("Which task shall I mark, Your Majesty?");
        } catch (IndexOutOfBoundsException e) {
            ui.println("Your Majesty, there aren't that many tasks in the list.");
        }
    }

    private void handleAddCommand(String arguments, Function<String, Task> taskFactory) {
        try {
            Task task = taskFactory.apply(arguments);
            taskList.add(task);
            handleTaskCommand(task);
            storage.writeItems(taskList.getItems());
        } catch (AthenaException e) {
            ui.println(e.getMessage());
        }
    }

    private void handleTaskCommand(Task task) {
        ui.println(Task.getCreateMsg());
        ui.println("  " + task);
        ui.println("You now have " + taskList.size() + " tasks in the list, Your Majesty.");
    }

    private void handleDeleteCommand(String arguments) {
        try {
            int idx = Integer.parseInt(arguments.trim());
            Task task = taskList.remove(idx - 1);
            storage.writeItems(taskList.getItems());
            ui.println("As you wish, Your Majesty. I've removed this task:");
            ui.println("  " + task);
            ui.println("You now have " + taskList.size() + " tasks in the list, Your Majesty.");
        } catch (NumberFormatException e) {
            ui.println("Which task shall I remove, Your Majesty?");
        } catch (IndexOutOfBoundsException e) {
            ui.println("Your Majesty, there aren't that many tasks in the list.");
        }
    }

    /**
     * @return exit message represented as a string
     */
    private static String getExitMessage() {
        String ans = Athena.UNDERSCORES + "\n";
        ans += "Farewell, Your Majesty. I hope to serve you again soon!\n";
        ans += Athena.UNDERSCORES;
        return ans;
    }
}
