package athena.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import athena.exception.AthenaException;
import athena.storage.Storage;
import athena.storage.StorageStub;
import athena.task.Tag;
import athena.task.Task;
import athena.task.TaskList;
import athena.task.Todo;
import athena.ui.Ui;

/**
 * Verifies that invalid input and failed saves leave the user's tasks unchanged.
 */
class ErrorHandlingTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Ui ui = new Ui(new ByteArrayInputStream(new byte[0]), new PrintStream(output));

    @Test
    void handleCommand_invalidInputs_noTasksSaved() {
        StorageStub storage = new StorageStub();
        TaskList tasks = new TaskList();
        CommandHandler handler = new CommandHandler(storage, ui, tasks);
        List<String> commands = List.of(
                "list extra", "bye extra", "todo one | two", "todo bad\u0000text",
                "deadline report /by 2026-02-30 1200", "deadline report /by 2026-02-29 1200",
                "deadline report /by 2026-04-31 1200", "deadline report /by 2026-01-01 2400",
                "deadline report /by 0000-01-01 1200", "deadline report /by 2026-01-01 1260",
                "deadline report /by 2026-01-01 1200 /by 2026-01-02 1200",
                "deadline report /by", "deadline report/by 2026-01-01 1200",
                "event meeting /from 2026-01-01 1200 /to 2026-01-01 1200",
                "event meeting /from 2026-01-02 1200 /to 2026-01-01 1200",
                "event meeting /to 2026-01-01 1300 /from 2026-01-01 1200",
                "event meeting /from 2026-01-01 1200 /to 2026-01-01 1300 /to 2026-01-01 1400");

        for (String command : commands) {
            assertEquals(CommandResult.ERROR, handler.handleCommand(command), command);
            assertEquals(0, tasks.size(), command);
            assertEquals(0, storage.getSaveCount(), command);
        }
    }

    @Test
    void handleCommand_extraWhitespaceAndLeapDay_taskAdded() {
        TaskList tasks = new TaskList();
        CommandHandler handler = new CommandHandler(new StorageStub(), ui, tasks);

        assertEquals(CommandResult.CONTINUE,
                handler.handleCommand(" \tdeadline  Submit\treport /by\t2024-02-29   1200  "));
        assertEquals("[D][ ] Submit report (by: Feb 29, 2024, 12:00)", tasks.get(0).toString());
    }

    @Test
    void handleCommand_duplicateDetailsIgnoringStatusTagsCaseAndSpacing_rejected() {
        TaskList tasks = new TaskList();
        StorageStub storage = new StorageStub();
        CommandHandler handler = new CommandHandler(storage, ui, tasks);
        List<String> commands = List.of("todo Read book", "deadline Report /by 2026-01-01 1200",
                "event Meeting /from 2026-01-01 1200 /to 2026-01-01 1300");

        for (String command : commands) {
            assertEquals(CommandResult.CONTINUE, handler.handleCommand(command));
            Task added = tasks.get(tasks.size() - 1);
            added.markDone();
            added.addTag(new Tag("#Work"));
            assertEquals(CommandResult.ERROR, handler.handleCommand(command.replace(" ", "  ")));
        }
        assertEquals(CommandResult.ERROR, handler.handleCommand("todo READ BOOK"));
        assertEquals(3, tasks.size());
        assertEquals(3, storage.getSaveCount());
        assertTrue(output.toString().contains("already exists"));
    }

    @Test
    void handleCommand_saveFailures_restoreAllMutationsWithoutSuccessMessages() {
        Storage storage = new StorageStub() {
            @Override
            public void saveTasks(List<Task> tasks) {
                throw new AthenaException("Cannot save tasks.");
            }
        };
        Todo task = new Todo("Read book");
        task.addTag(new Tag("#Work"));
        TaskList tasks = new TaskList(List.of(task, new Todo("Second task")));
        CommandHandler handler = new CommandHandler(storage, ui, tasks);
        List<String> commands = List.of("todo New task", "deadline Report /by 2026-01-01 1200",
                "event Meeting /from 2026-01-01 1200 /to 2026-01-01 1300",
                "delete 1", "mark 1", "tag 1 #New", "untag 1 #work");
        for (String command : commands) {
            output.reset();
            assertEquals(CommandResult.ERROR, handler.handleCommand(command), command);
            assertEquals(List.of("T | 0 | Read book | #Work", "T | 0 | Second task"),
                    tasks.getTasks().stream().map(Task::getSaveString).toList(), command);
            assertEquals("Cannot save tasks." + System.lineSeparator(), output.toString());
        }
        task.markDone();
        assertEquals(CommandResult.ERROR, handler.handleCommand("unmark 1"));
        assertTrue(task.isDone());
        assertFalse(output.toString().contains("back on your to-do list"));
    }
}
