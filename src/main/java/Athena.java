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
    public static void main(String[] args) {
        int count = 0;
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
            String nextLine = sc.nextLine();
            if (nextLine.trim().equalsIgnoreCase("bye")) {
                System.out.println(getExitMessage());
                break;
            }
            else if (nextLine.trim().equalsIgnoreCase("list")) {
                System.out.println(UNDERSCORES);
                for (int i = 0; i < count; i++) {
                    System.out.println(i + 1 + ". " + items[i]);
                }
                System.out.println(UNDERSCORES);
            }
            else if (nextLine.trim().toLowerCase().startsWith("mark")) {
                handleMarkCommand(nextLine, true);
            }
            else if (nextLine.trim().toLowerCase().startsWith("unmark")) {
                handleMarkCommand(nextLine, false);
            }
            else {
                items[count++] = new Task(nextLine);
                System.out.println(UNDERSCORES);
                System.out.println("added: " + nextLine);
                System.out.println(UNDERSCORES);
            }
        }
    }

    /**
     * Helper for handling mark
     * @param line
     * @param markAsDone
     */
    private static void handleMarkCommand(String line, boolean markAsDone) {
        int idx = Integer.parseInt(line.trim().split(" ")[1]);
        System.out.println(UNDERSCORES);
        System.out.println(markAsDone ? items[idx - 1].markDone() : items[idx - 1].unmarkDone());
        System.out.println(items[idx - 1]);
        System.out.println(UNDERSCORES);
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
