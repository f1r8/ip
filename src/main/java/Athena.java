import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * Provides major functions for the chatbot.
 *
 * @author f1r8
 */
public class Athena {
    private static final String UNDERSCORES = "____________________________________________________________";
    private static Task[] items = new Task[100];
    private static int count = 0;

    public static void main(String[] args) {
        String name = "Athena";
        String banner = "    _  _____ _   _ _____ _   _    _    \n"
                + "   / \\|_   _| | | | ____| \\ | |  / \\   \n"
                + "  / _ \\ | | | |_| |  _| |  \\| | / _ \\  \n"
                + " / ___ \\| | |  _  | |___| |\\  |/ ___ \\ \n"
                + "/_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\";

        System.out.println(UNDERSCORES);
        System.out.println(banner);
        System.out.println("Hello! I'm " + name + ".");
        System.out.println("What can I do for you?");
        System.out.println(UNDERSCORES);
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            System.out.println(UNDERSCORES);
            String nextLine = sc.nextLine();
            if (nextLine.trim().equalsIgnoreCase("bye")) {
                System.out.println(getExitMessage());
                break;
            }
            else if (nextLine.trim().equalsIgnoreCase("list")) {
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < count; i++) {
                    System.out.println(i + 1 + ". " + items[i]);
                }
            }
            else if (nextLine.trim().toLowerCase().startsWith("mark")) {
                handleMarkCommand(nextLine, true);
            }
            else if (nextLine.trim().toLowerCase().startsWith("unmark")) {
                handleMarkCommand(nextLine, false);
            }
            else if (nextLine.trim().toLowerCase().startsWith("todo")) {
                try {
                    items[count++] = new Todo(nextLine.replace("todo",""));
                    handleTaskCommand();
                } catch (AthenaException e) {
                    System.out.println(e.getMessage());
                }
            }
            else if (nextLine.trim().toLowerCase().startsWith("deadline")) {
                try{
                    items[count++] = new Deadline(nextLine.replace("deadline",""));
                    handleTaskCommand();
                } catch (AthenaException e) {
                    System.out.println(e.getMessage());
                }
            }
            else if (nextLine.trim().toLowerCase().startsWith("event")) {
                try{
                    items[count++] = new Event(nextLine.replace("event",""));
                    handleTaskCommand();
                } catch (AthenaException e) {
                    System.out.println(e.getMessage());
                }

            }
            else {
                System.out.println("What are you trying to do? 😵‍💫");
            }
            System.out.println(UNDERSCORES);
        }
    }

    /**
     * Helper for handling mark
     * @param line
     * @param markAsDone
     */
    private static void handleMarkCommand(String line, boolean markAsDone) {
        int idx;
        try {
            idx = Integer.parseInt(line.trim().split(" ")[1]);
        }
        catch (NumberFormatException e) {
            System.out.println("What are you trying to mark?");
            return;
        }
        System.out.println(markAsDone ? items[idx - 1].markDone() : items[idx - 1].unmarkDone());
        System.out.println(items[idx - 1]);
    }

    private static void handleTaskCommand() {
        System.out.println(items[count-1].getCreateMsg());
        System.out.println("  " + items[count-1]);
        System.out.println("Now you have " + count + " tasks in the list.");
    }

    /**
     *
     * @return exit message represented as a string
     */
    public static String getExitMessage() {
        String ans = UNDERSCORES + "\n";
        ans += "Bye. Hope to see you again soon!\n";
        ans += UNDERSCORES;
        return ans;
    }
}
