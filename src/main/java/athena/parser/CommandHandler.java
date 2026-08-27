package athena.parser;

import athena.Athena;
import athena.storage.Storage;
import athena.ui.Ui;
import athena.exception.AthenaException;
import athena.task.*;

import java.util.function.Function;

/**
 * Handles Commands for Athena application.
 */
public class CommandHandler {

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
        UNKNOWN("");

        private final String keyword;

        Command(String keyword) {
            this.keyword = keyword;
        }

        private static Command from(String keyword) {
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
    public boolean handleCommand(String nextLine) {
        nextLine = nextLine.trim();
        String[] commandParts = nextLine.split("\\s+", 2);
        Command command = Command.from(commandParts[0]);
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
        default:
            ui.println(UNKNOWN_COMMAND_MESSAGE);
            break;
        }
        return false;
    }

    /**
     * Helper for handling mark or unmark.
     *
     * @param arguments Specifies the index of the task to mark or unmark.
     * @param markAsDone true for mark, false for unmark.
     */
    private void handleMarkCommand(String arguments, boolean markAsDone) {
        int idx;
        try {
            idx = Integer.parseInt(arguments.trim());
            ui.println(markAsDone ? taskList.get(idx - 1).markDone() : taskList.get(idx - 1).unmarkDone());
        }
        catch (NumberFormatException e) {
            ui.println("Which task shall I mark, Your Majesty?");
            return;
        } catch (IndexOutOfBoundsException e) {
            ui.println("Your Majesty, there aren't that many tasks in the list.");
            return;
        }
        ui.println("  " + taskList.get(idx - 1));
        storage.writeItems(taskList.getItems());
    }

    private void handleAddCommand(String arguments,
                                         Function<String, Task> taskFactory) {
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
        int idx;
        Task task;
        try {
            idx = Integer.parseInt(arguments.trim());
            task = taskList.remove(idx - 1);
            storage.writeItems(taskList.getItems());
        }
        catch (NumberFormatException e) {
            ui.println("Which task shall I remove, Your Majesty?");
            return;
        }
        catch (IndexOutOfBoundsException e) {
            ui.println("Your Majesty, there aren't that many tasks in the list.");
            return;
        }
        ui.println("As you wish, Your Majesty. I've removed this task:");
        ui.println("  " + task);
        ui.println("You now have " + taskList.size() + " tasks in the list, Your Majesty.");
    }

    /**
     *
     * @return exit message represented as a string
     */
    private static String getExitMessage() {
        String ans = Athena.UNDERSCORES + "\n";
        ans += "Farewell, Your Majesty. I hope to serve you again soon!\n";
        ans += Athena.UNDERSCORES;
        return ans;
    }
    private static final String UNKNOWN_COMMAND_MESSAGE = "*Athena blinks her eyes, unsure of what you want, "
            + "tilting \nher head slightly as the meaning of your words slips just \nout of reach.*";
}
