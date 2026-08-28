package athena.parser;

import athena.exception.AthenaException;
import athena.storage.Storage;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;

import java.util.ArrayList;
import java.util.List;
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
                ui.showGoodbye();
                return true;
            case LIST:
                ui.showTaskList(taskList.getItems());
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
                List<Task> matchingTasks = new ArrayList<>();
                for (int i = 0; i < taskList.size(); i++) {
                    Task task = taskList.get(i);
                    if (task.toString().contains(arguments)) {
                        matchingTasks.add(task);
                    }
                }
                ui.showMatchingTasks(matchingTasks);
                break;
            default:
                ui.showUnknownCommand();
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
            if (shouldMarkAsDone) {
                task.markDone();
            } else {
                task.unmarkDone();
            }
            ui.showTaskStatusChanged(task, shouldMarkAsDone);
            storage.writeItems(taskList.getItems());
        } catch (NumberFormatException e) {
            ui.showMissingMarkIndex();
        } catch (IndexOutOfBoundsException e) {
            ui.showInvalidTaskIndex();
        }
    }

    private void handleAddCommand(String arguments, Function<String, Task> taskFactory) {
        try {
            Task task = taskFactory.apply(arguments);
            taskList.add(task);
            handleTaskCommand(task);
            storage.writeItems(taskList.getItems());
        } catch (AthenaException e) {
            ui.showError(e.getMessage());
        }
    }

    private void handleTaskCommand(Task task) {
        ui.showTaskAdded(task, taskList.size());
    }

    private void handleDeleteCommand(String arguments) {
        try {
            int idx = Integer.parseInt(arguments.trim());
            Task task = taskList.remove(idx - 1);
            storage.writeItems(taskList.getItems());
            ui.showTaskDeleted(task, taskList.size());
        } catch (NumberFormatException e) {
            ui.showMissingDeleteIndex();
        } catch (IndexOutOfBoundsException e) {
            ui.showInvalidTaskIndex();
        }
    }
}
