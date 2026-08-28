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
    public static final String PATH = "./data/athena.txt";

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

        ui.showLoadingStatus(storage.areItemsLoaded(taskList.getItems()), PATH);
        ui.showWelcome();
        while (ui.hasNextLine()) {
            ui.showDivider();
            if (commandHandler.isExitCommand(ui.nextLine())) {
                return;
            }
            ui.showDivider();
        }
    }


}
