import java.util.Scanner;

/**
 *
 * Provides major functions for the chatbot.
 *
 * @author f1r8
 */
public class Athena {
    private static final String UNDERSCORES = "____________________________________________________________";
    public static void main(String[] args) {
        String[] items = new String[100];
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
            if (nextLine.trim().equalsIgnoreCase("list")) {
                System.out.println(UNDERSCORES);
                for (int i = 0; i < count; i++) {
                    System.out.println(i + 1 + ". " + items[i]);
                }
                System.out.println(UNDERSCORES);
            }
            else {
                items[count++] = nextLine;
                System.out.println(UNDERSCORES);
                System.out.println("added: " + nextLine);
                System.out.println(UNDERSCORES);
            }
        }
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
