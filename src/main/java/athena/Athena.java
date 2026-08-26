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
    public static final String path = "./data/athena.txt";
    public static final String UNDERSCORES = "____________________________________________________________";

    public static void main(String[] args) {
        Storage storage = new Storage(path);
        UI ui = new UI(System.in, System.out);
        TaskList taskList = new TaskList();
        CommandHandler commandHandler = new CommandHandler(storage, ui, taskList);

        boolean saved = storage.loadItems(taskList.getItems());
        if (saved) {
            ui.println("Items successfully loaded.");
        } else {
            ui.println("File not found at: " + path);
        }
        String name = "Athena";
        String banner = "    _  _____ _   _ _____ _   _    _    \n"
                + "   / \\|_   _| | | | ____| \\ | |  / \\   \n"
                + "  / _ \\ | | | |_| |  _| |  \\| | / _ \\  \n"
                + " / ___ \\| | |  _  | |___| |\\  |/ ___ \\ \n"
                + "/_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\";

        ui.println(UNDERSCORES);
        ui.println(banner);
        ui.println("Hello, Your Majesty! I'm " + name + ".");
        ui.println("How may I assist you, Your Majesty?");
        ui.println(UNDERSCORES);
        while (ui.hasNextLine()) {
            ui.println(UNDERSCORES);
            if (commandHandler.handleCommand(ui.nextLine())) {
                return;
            }
            ui.println(UNDERSCORES);
        }
    }


}
