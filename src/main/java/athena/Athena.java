package athena;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import athena.exception.AthenaException;
import athena.parser.CommandHandler;
import athena.parser.CommandResult;
import athena.storage.Storage;
import athena.task.TaskList;
import athena.ui.Ui;

/**
 * Starts and coordinates the Athena application.
 */
public class Athena {
    /** Path where application data is stored */
    public static final String DATA_FILE_PATH = "./data/athena.txt";

    private CommandHandler commandHandler;

    private ByteArrayOutputStream outputBuffer;

    /**
     * Constructs an Athena application orchestrator.
     */
    public Athena() {
        Storage storage = new Storage(DATA_FILE_PATH);

        outputBuffer = new ByteArrayOutputStream();
        PrintStream guiOut = new PrintStream(outputBuffer);
        Ui ui = new Ui(System.in, guiOut);

        TaskList taskList = new TaskList();
        commandHandler = new CommandHandler(storage, ui, taskList);

        storage.areItemsLoaded(taskList.getTasks());
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

        ui.showLoadingStatus(storage.areItemsLoaded(taskList.getTasks()), DATA_FILE_PATH);
        ui.showWelcome();
        while (ui.hasNextLine()) {
            ui.showDivider();
            if (commandHandler.handleCommand(ui.readNextLine()) == CommandResult.EXIT) {
                return;
            }
            ui.showDivider();
        }
    }

    public String getResponse(String input) {
        outputBuffer.reset();
        if (commandHandler.handleCommand(input) == CommandResult.EXIT) {
            throw new AthenaException("Exiting...");
        }
        return outputBuffer.toString();
    }
}
