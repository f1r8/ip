package athena.parser;

import athena.storage.Storage;
import athena.storage.StorageStub;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandHandlerTest {

    private CommandHandler commandHandler;
    private ByteArrayOutputStream outContent;
    private TaskList taskList;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(outContent);
        InputStream testIn = new ByteArrayInputStream("".getBytes());
        Ui ui = new Ui(testIn, testOut);
        Storage storage = new StorageStub();
        taskList = new TaskList();
        commandHandler = new CommandHandler(storage, ui, taskList);
    }

    @Test
    void handleCommand_list_returnsContinue() {
        assertEquals(CommandResult.CONTINUE, commandHandler.handleCommand("list"));
    }

    @Test
    void handleCommand_list_printsNumberedTasks() {
        taskList.add(new Todo("Read book"));
        taskList.add(new Todo("Write report"));

        commandHandler.handleCommand("list");

        assertTrue(outContent.toString().contains("1. [T][ ] Read book"));
        assertTrue(outContent.toString().contains("2. [T][ ] Write report"));
    }

    @Test
    void handleCommand_todo_addedTodoTask() {
        commandHandler.handleCommand("todo Use 1 letter variable names like i,j,k");
        assertEquals(1, taskList.size());
        assertInstanceOf(Todo.class, taskList.get(0));
        assertEquals("[T][ ] Use 1 letter variable names like i,j,k", taskList.get(0).toString());
    }

    @Test
    void handleCommand_deadline_addedDeadlineTask() {
        commandHandler.handleCommand("deadline Use import java.util.* to save lines /by 2026-12-31 2359");
        assertEquals(1, taskList.size());
        assertInstanceOf(Deadline.class, taskList.get(0));
        assertEquals("[D][ ] Use import java.util.* to save lines (by: Dec 31, 2026, 23:59)",
                taskList.get(0).toString());
    }

    @Test
    void handleCommand_event_addedEventTask() {
        commandHandler.handleCommand("event Write more than 72 chars for git commit message subject"
                + " to give details /from 2026-12-31 2359 /to 9999-12-31 0000");
        assertEquals(1, taskList.size());
        assertInstanceOf(Event.class, taskList.get(0));
        assertEquals("[E][ ] Write more than 72 chars for git commit message subject to give details"
                + " (from: Dec 31, 2026, 23:59, to: Dec 31, 9999, 00:00)", taskList.get(0).toString());
    }

    @Test
    void handleCommand_delete_deleteTask() {
        commandHandler.handleCommand("todo start git commit message with lowercase letter");
        commandHandler.handleCommand("delete 1");
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_unknown_printBlinkEyes() {
        commandHandler.handleCommand("67 67 67 67");
        assertTrue(outContent.toString().contains("blinks her eyes"));
    }

    @Test
    void handleCommand_mixedCaseWithWhitespace_commandRecognized() {
        CommandResult commandResult = commandHandler.handleCommand("  ToDo Read book  ");

        assertEquals(CommandResult.CONTINUE, commandResult);
        assertEquals(1, taskList.size());
        assertEquals("[T][ ] Read book", taskList.get(0).toString());
    }

    @Test
    void handleCommand_mark_markTaskAsDone() {
        commandHandler.handleCommand("todo Use 1 letter variable names like i,j,k");
        commandHandler.handleCommand("mark 1");
        assertEquals(1, taskList.size());
        assertEquals("X", taskList.get(0).getStatusIcon());
    }

    @Test
    void handleCommand_unmark_unmarkTaskAsDone() {
        commandHandler.handleCommand("todo Use 1 letter variable names like i,j,k");
        commandHandler.handleCommand("mark 1");
        commandHandler.handleCommand("unmark 1");
        assertEquals(1, taskList.size());
        assertEquals(" ", taskList.get(0).getStatusIcon());
    }

    @Test
    void handleCommand_bye_returnsExit() {
        assertEquals(CommandResult.EXIT, commandHandler.handleCommand("bye"));
    }

    @Test
    void handleCommand_bye_printsFarewell() {
        commandHandler.handleCommand("bye");
        assertTrue(outContent.toString().contains("Farewell, Your Majesty"));
    }

    @Test
    void handleCommand_deadlineMissingBy_printErrorMessage() {
        commandHandler.handleCommand("deadline The Mythical Man-Month");
        assertTrue(outContent.toString().contains("/by"));
    }

    @Test
    void handleCommand_eventMissingFrom_printErrorMessage() {
        commandHandler.handleCommand("event Antithesis /to 2001-09-11 0846");
        assertTrue(outContent.toString().contains("/from"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_eventMissingTo_printErrorMessage() {
        commandHandler.handleCommand("event Antithesis /from 2001-09-11 0846");
        assertTrue(outContent.toString().contains("/to"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_todoMissingDescription_printErrorMessage() {
        commandHandler.handleCommand("todo");
        assertTrue(outContent.toString().contains("description"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_markOutOfRangeIndex_printErrorMessage() {
        commandHandler.handleCommand("mark 1");
        assertTrue(outContent.toString().contains("many tasks in the list"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_unmarkOutOfRangeIndex_printErrorMessage() {
        commandHandler.handleCommand("unmark 2");
        assertTrue(outContent.toString().contains("many tasks in the list"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_deleteOutOfRangeIndex_printErrorMessage() {
        commandHandler.handleCommand("delete 67");
        assertTrue(outContent.toString().contains("many tasks in the list"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_markMissingIndex_printErrorMessage() {
        commandHandler.handleCommand("mark");

        assertTrue(outContent.toString().contains("Which task shall I mark"));
    }

    @Test
    void handleCommand_unmarkNonNumericIndex_printErrorMessage() {
        commandHandler.handleCommand("unmark first");

        assertTrue(outContent.toString().contains("Which task shall I mark"));
    }

    @Test
    void handleCommand_deleteMissingIndex_printErrorMessage() {
        commandHandler.handleCommand("delete");

        assertTrue(outContent.toString().contains("Which task shall I remove"));
    }

    @Test
    void handleCommand_findMatchingTasks_onlyMatchesPrinted() {
        taskList.add(new Todo("Read project brief"));
        taskList.add(new Todo("Submit Final Report"));
        taskList.add(new Todo("Review REPORT"));

        commandHandler.handleCommand("find report");

        String output = outContent.toString();
        assertTrue(output.contains("1. [T][ ] Submit Final Report"));
        assertTrue(output.contains("2. [T][ ] Review REPORT"));
        assertFalse(output.contains("Read project brief"));
    }

    @Test
    void handleCommand_findMissingKeyword_printErrorMessage() {
        taskList.add(new Todo("Read project brief"));

        commandHandler.handleCommand("find");

        assertEquals("What shall I search for, Your Majesty?"
                + System.lineSeparator(), outContent.toString());
    }

    @Test
    void handleCommand_findNoMatchingTasks_headingOnlyPrinted() {
        taskList.add(new Todo("Read project brief"));

        commandHandler.handleCommand("find report");

        assertEquals("Your Majesty, here are the matching tasks in your list:"
                + System.lineSeparator(), outContent.toString());
    }

    @Test
    void handleCommand_invalidDeadlineDate_printErrorAndDoesNotAddTask() {
        commandHandler.handleCommand("deadline Submit report /by 31-12-2026 23:59");

        assertTrue(outContent.toString().contains("Invalid date format"));
        assertEquals(0, taskList.size());
    }

    @Test
    void handleCommand_invalidEventDate_printErrorAndDoesNotAddTask() {
        commandHandler.handleCommand("event Team meeting /from invalid /to 2026-12-30 1500");

        assertTrue(outContent.toString().contains("Invalid date format"));
        assertEquals(0, taskList.size());
    }
}
