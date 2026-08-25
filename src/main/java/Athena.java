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
    private static final ArrayList<Task> items = new ArrayList<>();

    public static void main(String[] args) {
        Save.loadItems(items);
        String name = "Athena";
        String banner = "    _  _____ _   _ _____ _   _    _    \n"
                + "   / \\|_   _| | | | ____| \\ | |  / \\   \n"
                + "  / _ \\ | | | |_| |  _| |  \\| | / _ \\  \n"
                + " / ___ \\| | |  _  | |___| |\\  |/ ___ \\ \n"
                + "/_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\";

        System.out.println(UNDERSCORES);
        System.out.println(banner);
        System.out.println("Hello, Your Majesty! I'm " + name + ".");
        System.out.println("How may I assist you, Your Majesty?");
        System.out.println(UNDERSCORES);
        try (Scanner sc = new Scanner(System.in)) {
            while (sc.hasNextLine()) {
                System.out.println(UNDERSCORES);
                String nextLine = sc.nextLine().trim();
                String[] commandParts = nextLine.split("\\s+", 2);
                Command command = Command.from(commandParts[0]);
                String arguments = commandParts.length > 1 ? commandParts[1] : "";

                switch (command) {
                case BYE:
                    System.out.println(getExitMessage());
                    return;
                case LIST:
                    System.out.println("Your Majesty, here are the tasks in your list:");
                    for (int i = 0; i < items.size(); i++) {
                        System.out.println(i + 1 + ". " + items.get(i));
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
                    System.out.println(UNKNOWN_COMMAND_MESSAGE);
                    break;
                }
                System.out.println(UNDERSCORES);
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
     * @param line
     * @param markAsDone
     */
    private static void handleMarkCommand(String arguments, boolean markAsDone) {
        int idx;
        try {
            idx = Integer.parseInt(arguments.trim());
        }
        catch (NumberFormatException e) {
            System.out.println("Which task shall I mark, Your Majesty?");
            return;
        }
        System.out.println(markAsDone ? items.get(idx - 1).markDone() : items.get(idx - 1).unmarkDone());
        System.out.println("  " + items.get(idx - 1));
        Save.writeItems(items);
    }

    private static void handleAddCommand(String arguments,
                                         Function<String, Task> taskFactory) {
        try {
            Task task = taskFactory.apply(arguments);
            items.add(task);
            handleTaskCommand(task);
            Save.writeItems(items);
        } catch (AthenaException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void handleTaskCommand(Task task) {
        System.out.println(task.getCreateMsg());
        System.out.println("  " + task);
        System.out.println("You now have " + items.size() + " tasks in the list, Your Majesty.");
    }

    private static void handleDeleteCommand(String arguments) {
        int idx;
        Task task;
        try {
            idx = Integer.parseInt(arguments.trim());
            task = items.remove(idx - 1);
            Save.writeItems(items);
        }
        catch (NumberFormatException e) {
            System.out.println("Which task shall I remove, Your Majesty?");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Your Majesty, there aren't that many tasks in the list.");
            return;
        }
        System.out.println("As you wish, Your Majesty. I've removed this task:");
        System.out.println("  " + task);
        System.out.println("You now have " + items.size() + " tasks in the list, Your Majesty.");
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
