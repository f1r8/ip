package athena.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import athena.storage.StorageStub;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Tag;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;

class CommandHandlerTest {

    private CommandHandler commandHandler;
    private ByteArrayOutputStream outContent;
    private StorageStub storage;
    private TaskList taskList;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(outContent);
        InputStream testIn = new ByteArrayInputStream("".getBytes());
        Ui ui = new Ui(testIn, testOut);
        storage = new StorageStub();
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
    void handleCommand_todoWithInlineTagText_literalDescriptionPreserved() {
        commandHandler.handleCommand("todo Discuss /tag #fun");

        assertEquals(1, taskList.size());
        assertEquals("[T][ ] Discuss /tag #fun", taskList.get(0).toString());
        assertEquals(List.of(), taskList.get(0).getTags());
        assertEquals(1, storage.getSaveCount());
    }

    @Test
    void handleCommand_tagDuplicatesAndExistingTag_onlyNewTagsAddedAndSavedOnce() {
        Todo todo = new Todo("Read book");
        todo.addTag(new Tag("#Work"));
        taskList.add(todo);

        commandHandler.handleCommand("tag 1 #work #Urgent #urgent");

        assertEquals("[T][ ] Read book #Urgent #Work", taskList.get(0).toString());
        assertEquals(1, storage.getSaveCount());
        assertEquals("As you command, Your Majesty. I've added the tags to this task:"
                + System.lineSeparator()
                + "  [T][ ] Read book #Urgent #Work" + System.lineSeparator(), outContent.toString());

        outContent.reset();
        commandHandler.handleCommand("tag 1 #WORK #urgent");

        assertEquals("Your Majesty, no tags changed for this task:" + System.lineSeparator()
                + "  [T][ ] Read book #Urgent #Work" + System.lineSeparator(), outContent.toString());
        assertEquals(1, storage.getSaveCount());
    }

    @Test
    void handleCommand_untagCaseVariants_tagRemovedAndMissingTagDoesNotSave() {
        Todo todo = new Todo("Read book");
        todo.addTag(new Tag("#Work"));
        todo.addTag(new Tag("#Urgent"));
        taskList.add(todo);

        commandHandler.handleCommand("untag 1 #work #Missing #missing");

        assertEquals("[T][ ] Read book #Urgent", taskList.get(0).toString());
        assertEquals(1, storage.getSaveCount());

        outContent.reset();
        commandHandler.handleCommand("untag 1 #WORK");

        assertEquals("Your Majesty, no tags changed for this task:" + System.lineSeparator()
                + "  [T][ ] Read book #Urgent" + System.lineSeparator(), outContent.toString());
        assertEquals(1, storage.getSaveCount());
    }

    @Test
    void handleCommand_tagInvalidLaterValue_noTagsAddedOrSaved() {
        taskList.add(new Todo("Read book"));

        commandHandler.handleCommand("tag 1 #Valid invalid");

        assertEquals(List.of(), taskList.get(0).getTags());
        assertEquals(0, storage.getSaveCount());
        assertEquals("Each tag must start with # and contain at least one letter, number, underscore, "
                + "or hyphen, Your Majesty." + System.lineSeparator(), outContent.toString());
    }

    @Test
    void handleCommand_untagInvalidLaterValue_noTagsRemovedOrSaved() {
        Todo todo = new Todo("Read book");
        todo.addTag(new Tag("#Work"));
        taskList.add(todo);

        commandHandler.handleCommand("untag 1 #Work invalid");

        assertEquals("[T][ ] Read book #Work", taskList.get(0).toString());
        assertEquals(0, storage.getSaveCount());
    }

    @Test
    void handleCommand_tagValidationErrors_resolvedInRequiredOrder() {
        taskList.add(new Todo("Read book"));

        commandHandler.handleCommand("tag");
        commandHandler.handleCommand("tag first #fun");
        commandHandler.handleCommand("tag 1");
        commandHandler.handleCommand("tag 9 invalid");
        commandHandler.handleCommand("tag 9 #fun");

        assertEquals("Which task shall I tag, Your Majesty?" + System.lineSeparator()
                + "Which task shall I tag, Your Majesty?" + System.lineSeparator()
                + "Which tags shall I add, Your Majesty?" + System.lineSeparator()
                + "Each tag must start with # and contain at least one letter, number, underscore, "
                + "or hyphen, Your Majesty." + System.lineSeparator()
                + "Your Majesty, there aren't that many tasks in the list." + System.lineSeparator(),
                outContent.toString());
        assertEquals(0, storage.getSaveCount());
    }

    @Test
    void handleCommand_untagValidationErrors_resolvedInRequiredOrder() {
        taskList.add(new Todo("Read book"));

        commandHandler.handleCommand("untag");
        commandHandler.handleCommand("untag first #fun");
        commandHandler.handleCommand("untag 1");
        commandHandler.handleCommand("untag 9 invalid");
        commandHandler.handleCommand("untag 9 #fun");

        assertEquals("Which task shall I untag, Your Majesty?" + System.lineSeparator()
                + "Which task shall I untag, Your Majesty?" + System.lineSeparator()
                + "Which tags shall I remove, Your Majesty?" + System.lineSeparator()
                + "Each tag must start with # and contain at least one letter, number, underscore, "
                + "or hyphen, Your Majesty." + System.lineSeparator()
                + "Your Majesty, there aren't that many tasks in the list." + System.lineSeparator(),
                outContent.toString());
        assertEquals(0, storage.getSaveCount());
    }

    @Test
    void handleCommand_findTagMultipleRequirements_exactCaseInsensitiveAndMatchesPrinted() {
        Todo firstMatch = new Todo("Prepare slides");
        firstMatch.addTag(new Tag("#Fun"));
        firstMatch.addTag(new Tag("#school"));
        taskList.add(firstMatch);

        Todo partialMatch = new Todo("Write report");
        partialMatch.addTag(new Tag("#fun"));
        taskList.add(partialMatch);

        Todo secondMatch = new Todo("Review notes");
        secondMatch.addTag(new Tag("#SCHOOL"));
        secondMatch.addTag(new Tag("#fun"));
        taskList.add(secondMatch);

        commandHandler.handleCommand("findtag #FUN #school #fun");

        String output = outContent.toString();
        assertTrue(output.contains("1. [T][ ] Prepare slides #Fun #school"));
        assertTrue(output.contains("2. [T][ ] Review notes #fun #SCHOOL"));
        assertFalse(output.contains("Write report"));
        assertEquals(0, storage.getSaveCount());
    }

    @Test
    void handleCommand_findTagMissingOrInvalidTags_errorPrintedWithoutSaving() {
        commandHandler.handleCommand("findtag");
        commandHandler.handleCommand("findtag invalid");

        assertEquals("Which tags shall I search for, Your Majesty?" + System.lineSeparator()
                + "Each tag must start with # and contain at least one letter, number, underscore, "
                + "or hyphen, Your Majesty." + System.lineSeparator(), outContent.toString());
        assertEquals(0, storage.getSaveCount());
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
