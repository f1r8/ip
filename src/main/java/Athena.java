import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Function;

/**
 *
 * Provides major functions for the chatbot.
 *
 * @author f1r8
 */
public class Athena {
    private static final String UNDERSCORES = "____________________________________________________________";
    private static final String UNKNOWN_COMMAND_MESSAGE = "*Athena blinks her eyes, unsure of what you want, "
            + "tilting her head slightly as the meaning of your words slips just out of reach.*";

    public static void main(String[] args) {
        boolean saved = Save.loadItems(TaskList.getItems());
        if (saved) {
            UI.println("Items successfully loaded.");
        } else {
            UI.println("File not found at: " + Save.PATH);
        }
        String name = "Athena";
        String banner = "    _  _____ _   _ _____ _   _    _    \n"
                + "   / \\|_   _| | | | ____| \\ | |  / \\   \n"
                + "  / _ \\ | | | |_| |  _| |  \\| | / _ \\  \n"
                + " / ___ \\| | |  _  | |___| |\\  |/ ___ \\ \n"
                + "/_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\";

        UI.println(UNDERSCORES);
        UI.println(banner);
        UI.println("Hello, Your Majesty! I'm " + name + ".");
        UI.println("How may I assist you, Your Majesty?");
        UI.println(UNDERSCORES);
        try (Scanner sc = new Scanner(UI.IN)) {
            while (sc.hasNextLine()) {
                UI.println(UNDERSCORES);
                String nextLine = sc.nextLine().trim();
                String[] commandParts = nextLine.split("\\s+", 2);
                Command command = Command.from(commandParts[0]);
                String arguments = commandParts.length > 1 ? commandParts[1] : "";

                switch (command) {
                case BYE:
                    UI.println(getExitMessage());
                    return;
                case LIST:
                    UI.println("Your Majesty, here are the tasks in your list:");
                    for (int i = 0; i < TaskList.size(); i++) {
                        UI.println(i + 1 + ". " + TaskList.get(i));
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
                    UI.println(UNKNOWN_COMMAND_MESSAGE);
                    break;
                }
                UI.println(UNDERSCORES);
            }
        }
    }

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
     * Helper for handling mark
     * @param arguments
     * @param markAsDone
     */
    private static void handleMarkCommand(String arguments, boolean markAsDone) {
        int idx;
        try {
            idx = Integer.parseInt(arguments.trim());
        }
        catch (NumberFormatException e) {
            UI.println("Which task shall I mark, Your Majesty?");
            return;
        }
        UI.println(markAsDone ? TaskList.get(idx - 1).markDone() : TaskList.get(idx - 1).unmarkDone());
        UI.println("  " + TaskList.get(idx - 1));
        Save.writeItems(TaskList.getItems());
    }

    private static void handleAddCommand(String arguments,
                                         Function<String, Task> taskFactory) {
        try {
            Task task = taskFactory.apply(arguments);
            TaskList.add(task);
            handleTaskCommand(task);
            Save.writeItems(TaskList.getItems());
        } catch (AthenaException e) {
            UI.println(e.getMessage());
        }
    }

    private static void handleTaskCommand(Task task) {
        UI.println(task.getCreateMsg());
        UI.println("  " + task);
        UI.println("You now have " + TaskList.size() + " tasks in the list, Your Majesty.");
    }

    private static void handleDeleteCommand(String arguments) {
        int idx;
        Task task;
        try {
            idx = Integer.parseInt(arguments.trim());
            task = TaskList.remove(idx - 1);
            Save.writeItems(TaskList.getItems());
        }
        catch (NumberFormatException e) {
            UI.println("Which task shall I remove, Your Majesty?");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            UI.println("Your Majesty, there aren't that many tasks in the list.");
            return;
        }
        UI.println("As you wish, Your Majesty. I've removed this task:");
        UI.println("  " + task);
        UI.println("You now have " + TaskList.size() + " tasks in the list, Your Majesty.");
    }

    /**
     *
     * @return exit message represented as a string
     */
    public static String getExitMessage() {
        String ans = UNDERSCORES + "\n";
        ans += "Farewell, Your Majesty. I hope to serve you again soon!\n";
        ans += UNDERSCORES;
        return ans;
    }
}
