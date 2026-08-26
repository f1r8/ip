package athena;

import athena.parser.CommandHandler;
import athena.storage.Storage;
import athena.task.TaskList;
import athena.ui.UI;

import java.util.Scanner;

/**
 *
 * Provides major functions for the chatbot.
 *
 * @author f1r8
 */
public class Athena {
    public static final String UNDERSCORES = "____________________________________________________________";
    public static void main(String[] args) {
        boolean saved = Storage.loadItems(TaskList.getItems());
        if (saved) {
            UI.println("Items successfully loaded.");
        } else {
            UI.println("File not found at: " + athena.storage.Storage.PATH);
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
                if (CommandHandler.handleCommand(sc.nextLine())) {
                    return;
                }
                UI.println(UNDERSCORES);
            }
        }
    }


}
