/**
 *
 * Provides major functions for the chatbot.
 *
 * @author f1r8
 */
public class Athena {
    public static void main(String[] args) {
        String name = "Athena";
        String banner = "    _  _____ _   _ _____ _   _    _    \n"
                + "   / \\|_   _| | | | ____| \\ | |  / \\   \n"
                + "  / _ \\ | | | |_| |  _| |  \\| | / _ \\  \n"
                + " / ___ \\| | |  _  | |___| |\\  |/ ___ \\ \n"
                + "/_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\";

        String underscores = "____________________________________________________________";
        System.out.println(underscores);
        System.out.println(banner);
        System.out.println("Hello! I'm " + name + ".");
        System.out.println("What can I do for you?");
        System.out.println(underscores);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(underscores);
    }
}
