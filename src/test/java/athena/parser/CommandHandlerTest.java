package athena.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import athena.storage.Storage;
import athena.storage.StorageStub;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;

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
    void isExitCommand_list_returnsFalse() {
        assertFalse(commandHandler.isExitCommand("list"));
    }

    @Test
    void isExitCommand_list_printsNumberedTasks() {
        taskList.add(new Todo("Read book"));
        taskList.add(new Todo("Write report"));

        commandHandler.isExitCommand("list");

        assertTrue(outContent.toString().contains("1. [T][ ] Read book"));
        assertTrue(outContent.toString().contains("2. [T][ ] Write report"));
    }

    @Test
    void isExitCommand_todo_addedTodoTask() {
        commandHandler.isExitCommand("todo Use 1 letter variable names like i,j,k");
        assertEquals(1, taskList.size());
        assertInstanceOf(Todo.class, taskList.get(0));
        assertEquals("[T][ ] Use 1 letter variable names like i,j,k", taskList.get(0).toString());
    }

    @Test
    void isExitCommand_deadline_addedDeadlineTask() {
        commandHandler.isExitCommand("deadline Use import java.util.* to save lines /by 2026-12-31 2359");
        assertEquals(1, taskList.size());
        assertInstanceOf(Deadline.class, taskList.get(0));
        assertEquals("[D][ ] Use import java.util.* to save lines (by: Dec 31, 2026, 23:59)",
                taskList.get(0).toString());
    }

    @Test
    void isExitCommand_event_addedEventTask() {
        commandHandler.isExitCommand("event Write more than 72 chars for git commit message subject"
                + " to give details /from 2026-12-31 2359 /to 9999-12-31 0000");
        assertEquals(1, taskList.size());
        assertInstanceOf(Event.class, taskList.get(0));
        assertEquals("[E][ ] Write more than 72 chars for git commit message subject to give details"
                + " (from: Dec 31, 2026, 23:59, to: Dec 31, 9999, 00:00)", taskList.get(0).toString());
    }

    @Test
    void isExitCommand_delete_deleteTask() {
        commandHandler.isExitCommand("todo start git commit message with lowercase letter");
        commandHandler.isExitCommand("delete 1");
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_unknown_printBlinkEyes() {
        commandHandler.isExitCommand("67 67 67 67");
        assertTrue(outContent.toString().contains("blinks her eyes"));
    }

    @Test
    void isExitCommand_mixedCaseWithWhitespace_commandRecognized() {
        boolean shouldExit = commandHandler.isExitCommand("  ToDo Read book  ");

        assertFalse(shouldExit);
        assertEquals(1, taskList.size());
        assertEquals("[T][ ] Read book", taskList.get(0).toString());
    }

    @Test
    void isExitCommand_mark_markTaskAsDone() {
        commandHandler.isExitCommand("todo Use 1 letter variable names like i,j,k");
        commandHandler.isExitCommand("mark 1");
        assertEquals(1, taskList.size());
        assertEquals("X", taskList.get(0).getStatusIcon());
    }

    @Test
    void isExitCommand_unmark_unmarkTaskAsDone() {
        commandHandler.isExitCommand("todo Use 1 letter variable names like i,j,k");
        commandHandler.isExitCommand("mark 1");
        commandHandler.isExitCommand("unmark 1");
        assertEquals(1, taskList.size());
        assertEquals(" ", taskList.get(0).getStatusIcon());
    }

    @Test
    void isExitCommand_bye_returnsTrue() {
        assertTrue(commandHandler.isExitCommand("bye"));
    }

    @Test
    void isExitCommand_bye_printsFarewell() {
        commandHandler.isExitCommand("bye");
        assertTrue(outContent.toString().contains("Farewell, Your Majesty"));
    }

    @Test
    void isExitCommand_deadlineMissingBy_printErrorMessage() {
        commandHandler.isExitCommand("deadline The Mythical Man-Month");
        assertTrue(outContent.toString().contains("/by"));
    }

    @Test
    void isExitCommand_eventMissingFrom_printErrorMessage() {
        commandHandler.isExitCommand("event Antithesis /to 2001-09-11 0846");
        assertTrue(outContent.toString().contains("/from"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_eventMissingTo_printErrorMessage() {
        commandHandler.isExitCommand("event Antithesis /from 2001-09-11 0846");
        assertTrue(outContent.toString().contains("/to"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_todoMissingDescription_printErrorMessage() {
        commandHandler.isExitCommand("todo");
        assertTrue(outContent.toString().contains("description"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_markOutOfRangeIndex_printErrorMessage() {
        commandHandler.isExitCommand("mark 1");
        assertTrue(outContent.toString().contains("many tasks in the list"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_unmarkOutOfRangeIndex_printErrorMessage() {
        commandHandler.isExitCommand("unmark 2");
        assertTrue(outContent.toString().contains("many tasks in the list"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_deleteOutOfRangeIndex_printErrorMessage() {
        commandHandler.isExitCommand("delete 67");
        assertTrue(outContent.toString().contains("many tasks in the list"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_markMissingIndex_printErrorMessage() {
        commandHandler.isExitCommand("mark");

        assertTrue(outContent.toString().contains("Which task shall I mark"));
    }

    @Test
    void isExitCommand_unmarkNonNumericIndex_printErrorMessage() {
        commandHandler.isExitCommand("unmark first");

        assertTrue(outContent.toString().contains("Which task shall I mark"));
    }

    @Test
    void isExitCommand_deleteMissingIndex_printErrorMessage() {
        commandHandler.isExitCommand("delete");

        assertTrue(outContent.toString().contains("Which task shall I remove"));
    }

    @Test
    void isExitCommand_findMatchingTasks_onlyMatchesPrinted() {
        taskList.add(new Todo("Read project brief"));
        taskList.add(new Todo("Submit Final Report"));
        taskList.add(new Todo("Review REPORT"));

        commandHandler.isExitCommand("find report");

        String output = outContent.toString();
        assertTrue(output.contains("1. [T][ ] Submit Final Report"));
        assertTrue(output.contains("2. [T][ ] Review REPORT"));
        assertFalse(output.contains("Read project brief"));
    }

    @Test
    void isExitCommand_findMissingKeyword_printErrorMessage() {
        taskList.add(new Todo("Read project brief"));

        commandHandler.isExitCommand("find");

        assertEquals("What shall I search for, Your Majesty?"
                + System.lineSeparator(), outContent.toString());
    }

    @Test
    void isExitCommand_findNoMatchingTasks_headingOnlyPrinted() {
        taskList.add(new Todo("Read project brief"));

        commandHandler.isExitCommand("find report");

        assertEquals("Your Majesty, here are the matching tasks in your list:"
                + System.lineSeparator(), outContent.toString());
    }

    @Test
    void isExitCommand_invalidDeadlineDate_printErrorAndDoesNotAddTask() {
        commandHandler.isExitCommand("deadline Submit report /by 31-12-2026 23:59");

        assertTrue(outContent.toString().contains("Invalid date format"));
        assertEquals(0, taskList.size());
    }

    @Test
    void isExitCommand_invalidEventDate_printErrorAndDoesNotAddTask() {
        commandHandler.isExitCommand("event Team meeting /from invalid /to 2026-12-30 1500");

        assertTrue(outContent.toString().contains("Invalid date format"));
        assertEquals(0, taskList.size());
    }
}
