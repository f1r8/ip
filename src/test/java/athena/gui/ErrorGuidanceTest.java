package athena.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import athena.parser.CommandHandler;
import athena.parser.CommandResult;
import athena.storage.StorageStub;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;

class ErrorGuidanceTest {
    @Test
    void forInput_deadline_preservesDescriptionAndReplacesInvalidDate() {
        ErrorGuidance guidance = ErrorGuidance.forInput("deadline Submit final report /by tomorrow");

        assertEquals("deadline Submit final report /by 2026-12-31 2359", guidance.example());
        assertTrue(guidance.isDeadline());
        assertFalse(guidance.hint().contains("Your Majesty"));
    }

    @Test
    void forInput_deadlineMissingDetails_suppliesValidExample() {
        for (String input : List.of("deadline", "deadline /by", "deadline /by invalid")) {
            assertEquals("deadline Submit report /by 2026-12-31 2359", ErrorGuidance.forInput(input).example());
        }
        assertEquals("deadline Read book /by 2026-12-31 2359",
                ErrorGuidance.forInput("deadline Read book /by").example());
    }

    @Test
    void getExampleFor_editedDeadline_preservesLatestDescription() {
        ErrorGuidance guidance = ErrorGuidance.forInput("deadline Old description /by tomorrow");

        assertEquals("deadline Revised  description /by 2026-12-31 2359",
                guidance.getExampleFor("  DeAdLiNe\tRevised  description /by next week  "));
        assertEquals("deadline New description /by 2026-12-31 2359",
                guidance.getExampleFor("deadline New description"));
    }

    @Test
    void getExampleFor_changedCommand_retainsUnrelatedEditsExactly() {
        ErrorGuidance guidance = ErrorGuidance.forInput("deadline Submit report /by tomorrow");

        for (String input : List.of("todo Different task", "  event Meeting  ", "", "   ", "deadlineish")) {
            assertEquals(input, guidance.getExampleFor(input));
        }
    }

    @Test
    void forInput_delimiterLikeText_preservesTextOutsideParserDelimiter() {
        assertEquals("deadline Read /bypass notes /by 2026-12-31 2359",
                ErrorGuidance.forInput("deadline Read /bypass notes /by tomorrow").example());
        assertEquals("deadline Read /BY notes /by 2026-12-31 2359",
                ErrorGuidance.forInput("deadline Read /BY notes").example());
        assertEquals("deadline Read /by 2026-12-31 2359",
                ErrorGuidance.forInput("deadline Read /by notes /by tomorrow").example());
    }

    @Test
    void forInput_indexedCommand_explainsTaskPrerequisite() {
        for (String command : List.of("mark", "unmark", "delete", "tag", "untag")) {
            String hint = ErrorGuidance.forInput(command).hint();
            assertFalse(hint.contains("Your Majesty"));
            assertTrue(hint.contains("list"));
            assertTrue(hint.contains("assumes task 1 exists"));
        }
    }

    @Test
    void forInput_blankOrUnknown_offersRecognizedCommands() {
        for (String input : List.of("", "   ", "unknown something")) {
            ErrorGuidance guidance = ErrorGuidance.forInput(input);
            assertEquals("todo Read book", guidance.example());
            assertFalse(guidance.hint().contains("Your Majesty"));
            assertTrue(guidance.hint().contains("Open Commands"));
            assertFalse(guidance.isDeadline());
        }
    }

    @Test
    void getExampleFor_otherCommand_returnsDisplayedExample() {
        ErrorGuidance guidance = ErrorGuidance.forInput("  ToDo  ");

        assertEquals(guidance.example(), guidance.getExampleFor("todo"));
        assertFalse(guidance.isDeadline());
    }

    @Test
    void forInput_examples_canBeExecutedWithRequiredTaskPresent() {
        for (String command : List.of("todo", "deadline", "event", "mark", "unmark", "delete",
                "find", "tag", "untag", "findtag", "unknown")) {
            TaskList taskList = new TaskList();
            taskList.add(new Todo("Existing task"));
            StorageStub storage = new StorageStub();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Ui ui = new Ui(new ByteArrayInputStream(new byte[0]), new PrintStream(output));
            CommandHandler handler = new CommandHandler(storage, ui, taskList);

            ErrorGuidance guidance = ErrorGuidance.forInput(command);
            String example = guidance.getExampleFor(command);

            assertEquals(1, taskList.size());
            assertEquals(0, storage.getSaveCount());
            assertEquals(CommandResult.CONTINUE, handler.handleCommand(example), example);
        }
    }
}
