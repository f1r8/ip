package athena.ui;

import athena.task.Deadline;
import athena.task.Task;
import athena.task.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests console input handling and every user-facing UI response.
 */
class UiTest {
    private static final String DIVIDER = "____________________________________________________________";

    private ByteArrayOutputStream output;
    private Ui ui;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        ui = createUi("");
    }

    @Test
    void inputMethods_multipleLines_linesReadInOrder() {
        ui = createUi("first" + System.lineSeparator() + "second" + System.lineSeparator());

        assertTrue(ui.hasNextLine());
        assertEquals("first", ui.readNextLine());
        assertTrue(ui.hasNextLine());
        assertEquals("second", ui.readNextLine());
        assertFalse(ui.hasNextLine());
    }

    @Test
    void showLoadingStatus_itemsLoaded_successMessagePrinted() {
        ui.showLoadingStatus(true, "ignored-path");

        assertOutput("Items successfully loaded.\n");
    }

    @Test
    void showLoadingStatus_noItems_fileMessagePrinted() {
        ui.showLoadingStatus(false, "./data/athena.txt");

        assertOutput("File not found at: ./data/athena.txt\n");
    }

    @Test
    void showWelcome_bannerAndGreetingPrinted() {
        ui.showWelcome();

        assertOutput(DIVIDER + "\n"
                + "    _  _____ _   _ _____ _   _    _\n"
                + "   / \\|_   _| | | | ____| \\ | |  / \\\n"
                + "  / _ \\ | | | |_| |  _| |  \\| | / _ \\\n"
                + " / ___ \\| | |  _  | |___| |\\  |/ ___ \\\n"
                + "/_/   \\_\\_| |_| |_|_____|_| \\_/_/   \\_\\\n"
                + "Hello, Your Majesty! I'm Athena.\n"
                + "How may I assist you, Your Majesty?\n"
                + DIVIDER + "\n");
    }

    @Test
    void showDivider_dividerPrinted() {
        ui.showDivider();

        assertOutput(DIVIDER + "\n");
    }

    @Test
    void showGoodbye_farewellPrintedBetweenDividers() {
        ui.showGoodbye();

        assertOutput(DIVIDER + "\n"
                + "Farewell, Your Majesty. I hope to serve you again soon!\n"
                + DIVIDER + "\n");
    }

    @Test
    void showTaskList_tasks_numberedTasksPrinted() {
        List<Task> tasks = List.of(
                new Todo("Read book"),
                new Deadline("Submit report", "2026-12-31 2359"));

        ui.showTaskList(tasks);

        assertOutput("Your Majesty, here are the tasks in your list:\n"
                + "1. [T][ ] Read book\n"
                + "2. [D][ ] Submit report (by: Dec 31, 2026, 23:59)\n");
    }

    @Test
    void showTaskList_emptyList_headingOnlyPrinted() {
        ui.showTaskList(List.of());

        assertOutput("Your Majesty, here are the tasks in your list:\n");
    }

    @Test
    void showMatchingTasks_tasks_numberedMatchesPrinted() {
        ui.showMatchingTasks(List.of(new Todo("Read report")));

        assertOutput("Your Majesty, here are the matching tasks in your list:\n"
                + "1. [T][ ] Read report\n");
    }

    @Test
    void showUnknownCommand_unknownCommandMessagePrinted() {
        ui.showUnknownCommand();

        assertOutput("*Athena blinks her eyes, unsure of what you want, tilting\n"
                + "her head slightly as the meaning of your words slips just\n"
                + "out of reach.*\n");
    }

    @Test
    void showTaskStatusChanged_markedTask_markedMessagePrinted() {
        Todo todo = new Todo(true, "Read book");

        ui.showTaskStatusChanged(todo, true);

        assertOutput("Excellent, Your Majesty! I've marked this task as done:\n"
                + "  [T][X] Read book\n");
    }

    @Test
    void showTaskStatusChanged_unmarkedTask_unmarkedMessagePrinted() {
        Todo todo = new Todo("Read book");

        ui.showTaskStatusChanged(todo, false);

        assertOutput("Certainly, Your Majesty. I've marked this task as not done yet:\n"
                + "  [T][ ] Read book\n");
    }

    @Test
    void showMissingMarkIndex_promptPrinted() {
        ui.showMissingMarkIndex();

        assertOutput("Which task shall I mark, Your Majesty?\n");
    }

    @Test
    void showInvalidTaskIndex_errorPrinted() {
        ui.showInvalidTaskIndex();

        assertOutput("Your Majesty, there aren't that many tasks in the list.\n");
    }

    @Test
    void showError_message_messagePrinted() {
        ui.showError("Something went wrong");

        assertOutput("Something went wrong\n");
    }

    @Test
    void showTaskAdded_taskAndCount_additionPrinted() {
        ui.showTaskAdded(new Todo("Read book"), 1);

        assertOutput("As you command, Your Majesty. I've added this task:\n"
                + "  [T][ ] Read book\n"
                + "You now have 1 tasks in the list, Your Majesty.\n");
    }

    @Test
    void showTaskDeleted_taskAndCount_deletionPrinted() {
        ui.showTaskDeleted(new Todo("Read book"), 0);

        assertOutput("As you wish, Your Majesty. I've removed this task:\n"
                + "  [T][ ] Read book\n"
                + "You now have 0 tasks in the list, Your Majesty.\n");
    }

    @Test
    void showMissingDeleteIndex_promptPrinted() {
        ui.showMissingDeleteIndex();

        assertOutput("Which task shall I remove, Your Majesty?\n");
    }

    /**
     * Creates a UI whose output is captured by this test instance.
     *
     * @param input text that the UI should read.
     * @return a UI connected to in-memory streams.
     */
    private Ui createUi(String input) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        PrintStream outputStream = new PrintStream(output, true, StandardCharsets.UTF_8);
        return new Ui(inputStream, outputStream);
    }

    /**
     * Compares output after normalizing platform line endings and invisible trailing spaces.
     *
     * @param expected expected console text using line-feed separators.
     */
    private void assertOutput(String expected) {
        String actual = output.toString(StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .replaceAll("[ \\t]+\\n", "\n");
        assertEquals(expected, actual);
    }
}
