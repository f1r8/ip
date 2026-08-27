package athena;

import athena.parser.CommandHandler;
import athena.storage.Storage;
import athena.task.TaskList;
import athena.ui.Ui;

/**
 * Orchestrator for the Athena application.
 */
public class Athena {
    /** Path where application data is stored */
    public static final String path = "./data/athena.txt";
    /** Cosmetic String for printing */
    public static final String UNDERSCORES = "____________________________________________________________";

    /**
     * Entry point of Athena application.
     * Initializes Storage, UI, TaskList and CommandHandler.
     * Contains messages to print when entering and leaving the application.
     *
     * @param args Unused command-line argument.
     */
    public static void main(String[] args) {
        Storage storage = new Storage(path);
        Ui ui = new Ui(System.in, System.out);
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
