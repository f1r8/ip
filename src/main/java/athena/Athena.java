package athena;

import athena.parser.CommandHandler;
import athena.storage.Storage;
import athena.task.TaskList;
import athena.ui.Ui;

/**
 *
 * Provides major functions for the chatbot.
 *
 * @author f1r8
 */
public class Athena {
    public static final String PATH = "./data/athena.txt";
    public static final String UNDERSCORES = "____________________________________________________________";

    public static void main(String[] args) {
        Storage storage = new Storage(PATH);
        Ui ui = new Ui(System.in, System.out);
        TaskList taskList = new TaskList();
        CommandHandler commandHandler = new CommandHandler(storage, ui, taskList);

        boolean isLoaded = storage.areItemsLoaded(taskList.getItems());
        if (isLoaded) {
            ui.println("Items successfully loaded.");
        } else {
            ui.println("File not found at: " + PATH);
        }
        String name = "Athena";
        String banner = """
                    _  _____ _   _ _____ _   _    _   \s
                   / \\|_   _| | | | ____| \\ | |  / \\  \s
                  / _ \\ | | | |_| |  _| |  \\| | / _ \\ \s
                 / ___ \\| | |  _  | |___| |\\  |/ ___ \\\s
                /_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\""";

        ui.println(UNDERSCORES);
        ui.println(banner);
        ui.println("Hello, Your Majesty! I'm " + name + ".");
        ui.println("How may I assist you, Your Majesty?");
        ui.println(UNDERSCORES);
        while (ui.hasNextLine()) {
            ui.println(UNDERSCORES);
            if (commandHandler.isExitCommand(ui.nextLine())) {
                return;
            }
            ui.println(UNDERSCORES);
        }
    }


}
