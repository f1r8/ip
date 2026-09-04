package athena;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import athena.exception.AthenaException;
import athena.parser.CommandHandler;
import athena.storage.Storage;
import athena.task.TaskList;
import athena.ui.Ui;

/**
 * Starts and coordinates the Athena application.
 */
public class Athena {
    /** Path where application data is stored */
    public static final String DATA_FILE_PATH = "./data/athena.txt";

    /**
     * Constructs an Athena application orchestrator.
     */
    public Athena() {
    }

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
     * Starts the Athena application and processes commands until the user exits.
     *
     * @param args Command-line arguments; currently unused.
     */
    public static void main(String[] args) {
        Storage storage = new Storage(DATA_FILE_PATH);
        Ui ui = new Ui(System.in, System.out);
        TaskList taskList = new TaskList();
        CommandHandler commandHandler = new CommandHandler(storage, ui, taskList);

        ui.showLoadingStatus(storage.areItemsLoaded(taskList.getItems()), DATA_FILE_PATH);
        ui.showWelcome();
        while (ui.hasNextLine()) {
            ui.showDivider();
            if (commandHandler.isExitCommand(ui.readNextLine())) {
                return;
            }
            ui.showDivider();
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
