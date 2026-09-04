package athena;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import athena.exception.AthenaException;
import athena.parser.CommandHandler;
import athena.storage.Storage;
import athena.task.TaskList;
import athena.ui.Ui;

/**
 * Orchestrator for the Athena application.
 */
public class Athena {
    /** Path where application data is stored */
    public static final String PATH = "./data/athena.txt";
    /** Cosmetic String for printing */
    public static final String UNDERSCORES = "____________________________________________________________";

    private CommandHandler commandHandler;

    private ByteArrayOutputStream outputBuffer;

    public Athena() {
        Storage storage = new Storage(PATH);

        outputBuffer = new ByteArrayOutputStream();
        PrintStream guiOut = new PrintStream(outputBuffer);
        Ui ui = new Ui(System.in, guiOut);

        TaskList taskList = new TaskList();
        commandHandler = new CommandHandler(storage, ui, taskList);

        storage.areItemsLoaded(taskList.getItems());
    }

    /**
     * Entry point of Athena application.
     * Initializes Storage, UI, TaskList and CommandHandler.
     * Contains messages to print when entering and leaving the application.
     *
     * @param args Unused command-line argument.
     */
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

    public String getResponse(String input) {
        outputBuffer.reset();
        if (commandHandler.isExitCommand(input)) {
            throw new AthenaException("Exiting...");
        }
        return outputBuffer.toString();
    }
}
