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
import java.util.Locale;
import java.util.function.Function;

/**
 * Parses and executes commands for the Athena application.
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
     * Handles an Athena command and returns the resulting application state.
     *
     * @param inputLine User input read by the UI.
     * @return Result indicating whether Athena should continue or exit.
     */
    public CommandResult handleCommand(String inputLine) {
        inputLine = inputLine.trim();
        String[] commandParts = inputLine.split("\\s+", 2);
        Command command = Command.search(commandParts[0]);
        String arguments = commandParts.length > 1 ? commandParts[1] : "";

        switch (command) {
            case BYE:
                ui.showGoodbye();
                return CommandResult.EXIT;
            case LIST:
                ui.showTaskList(taskList.getTasks());
                break;
            case MARK:
                handleMarkCommand(arguments, true);
                break;
            case UNMARK:
                handleMarkCommand(arguments, false);
                break;
            case TODO:
                createAndAddTask(arguments, Todo::new);
                break;
            case DEADLINE:
                createAndAddTask(arguments, Deadline::new);
                break;
            case EVENT:
                createAndAddTask(arguments, Event::new);
                break;
            case DELETE:
                handleDeleteCommand(arguments);
                break;
            case FIND:
                handleFindCommand(arguments);
                break;
            default:
                ui.showUnknownCommand();
                break;
        }
        return CommandResult.CONTINUE;
    }

    /**
     * Helper for handling mark/unmark.
     *
     * @param arguments String arguments for handling mark/unmark.
     * @param shouldMarkAsDone true if task should be marked, false if unmarked.
     */
    private void handleMarkCommand(String arguments, boolean shouldMarkAsDone) {
        try {
            int index = Integer.parseInt(arguments.trim());
            Task task = taskList.get(index - 1);
            if (shouldMarkAsDone) {
                task.markDone();
            } else {
                task.unmarkDone();
            }
            ui.showTaskStatusChanged(task, shouldMarkAsDone);
            storage.writeItems(taskList.getTasks());
        } catch (NumberFormatException e) {
            ui.showMissingMarkIndex();
        } catch (IndexOutOfBoundsException e) {
            ui.showInvalidTaskIndex();
        }
    }

    private void createAndAddTask(String arguments, Function<String, Task> taskFactory) {
        try {
            Task task = taskFactory.apply(arguments);
            taskList.add(task);
            handleTaskCommand(task);
            storage.writeItems(taskList.getTasks());
        } catch (AthenaException e) {
            ui.showError(e.getMessage());
        }
    }

    private void handleTaskCommand(Task task) {
        ui.showTaskAdded(task, taskList.size());
    }

    private void handleDeleteCommand(String arguments) {
        try {
            int index = Integer.parseInt(arguments.trim());
            Task task = taskList.remove(index - 1);
            storage.writeItems(taskList.getTasks());
            ui.showTaskDeleted(task, taskList.size());
        } catch (NumberFormatException e) {
            ui.showMissingDeleteIndex();
        } catch (IndexOutOfBoundsException e) {
            ui.showInvalidTaskIndex();
        }
    }

    private void handleFindCommand(String arguments) {
        if (arguments.isBlank()) {
            ui.showMissingFindKeyword();
            return;
        }

        String normalizedKeyword = arguments.toLowerCase(Locale.ROOT);
        List<Task> matchingTasks = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            String normalizedTask = task.toString().toLowerCase(Locale.ROOT);
            if (normalizedTask.contains(normalizedKeyword)) {
                matchingTasks.add(task);
            }
        }
        ui.showMatchingTasks(matchingTasks);
    }
}
