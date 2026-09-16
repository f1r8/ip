package athena.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import athena.parser.CommandHandler;
import athena.parser.CommandResult;
import athena.storage.StorageStub;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Tag;
import athena.task.TaskList;
import athena.task.Todo;

/**
 * Verifies that GUI replies preserve task details without depending on console formatting.
 */
class GuiUiTest {
    private ByteArrayOutputStream output;
    private GuiUi ui;
    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        ui = new GuiUi(new PrintStream(output));
        commandHandler = new CommandHandler(new StorageStub(), ui, new TaskList());
    }

    @Test
    void taskView_datedTasks_preservesTypeDatesAndLiteralDescription() {
        TaskView deadline = TaskView.from(new Deadline("Review (by: literal) [X] #text", "2026-12-31 2359"), 2);
        assertEquals(2, deadline.number());
        assertEquals("Review (by: literal) [X] #text", deadline.description());
        assertEquals("Deadline", deadline.type());
        assertEquals("Due Dec 31, 2026, 23:59", deadline.schedule());

        TaskView event = TaskView.from(new Event("Meeting", "2026-12-30 1400", "2026-12-31 1500"), 3);
        assertEquals("Event", event.type());
        assertEquals("From Dec 30, 2026, 14:00\nTo Dec 31, 2026, 15:00", event.schedule());
    }

    @Test
    void taskView_laterTaskChanges_doesNotAlterHistoricalReply() {
        Todo task = new Todo("Read book");
        task.addTag(new Tag("#school"));
        task.addTag(new Tag("#Fun"));
        TaskView snapshot = TaskView.from(task, 1);

        task.markDone();
        task.removeTag(new Tag("#Fun"));

        assertFalse(snapshot.isDone());
        assertEquals("Todo", snapshot.type());
        assertEquals("", snapshot.schedule());
        assertEquals(List.of("#Fun", "#school"), snapshot.tags());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.tags().clear());
    }

    @Test
    void response_mutableSourceList_keepsIndependentTaskSnapshot() {
        List<TaskView> tasks = new ArrayList<>(List.of(TaskView.from(new Todo("Read book"), 1)));
        CommandResponse response = new CommandResponse("Your tasks, Your Majesty.", false, false, tasks);
        tasks.clear();

        assertEquals(1, response.tasks().size());
        assertThrows(UnsupportedOperationException.class, () -> response.tasks().clear());
    }

    @Test
    void commands_taskLifecycle_returnsConciseCaptionsAndUpdatedDetails() {
        CommandResponse added = respond("todo Read book");
        assertEquals("As you wish. I've added this task. 1 task in your list.", added.message());
        assertEquals(0, added.tasks().getFirst().number());
        assertEquals("Read book", added.tasks().getFirst().description());
        assertFalse(added.tasks().getFirst().isDone());

        CommandResponse marked = respond("mark 1");
        assertEquals("Well done, Your Majesty. This task is complete.", marked.message());
        assertTrue(marked.tasks().getFirst().isDone());
        assertFalse(added.tasks().getFirst().isDone());

        assertEquals("Certainly. This task is back on your to-do list.", respond("unmark 1").message());
        assertEquals(List.of("#book"), respond("tag 1 #book").tasks().getFirst().tags());
        assertEquals("These tags need no changes.", respond("tag 1 #book").message());
        assertEquals(List.of(), respond("untag 1 #book").tasks().getFirst().tags());
        assertEquals("Certainly. I've removed this task. 0 tasks in your list.",
                respond("delete 1").message());
        assertEquals("Your list awaits its first task. Try todo Read a book.", respond("list").message());
    }

    @Test
    void commands_listAndSearch_preservesOrderAndClearsRowsAfterAnError() {
        respond("todo Read book");
        respond("deadline Submit report /by 2026-12-31 2359");
        respond("tag 2 #work");

        CommandResponse listed = respond("list");
        assertEquals(List.of(1, 2), listed.tasks().stream().map(TaskView::number).toList());
        assertEquals(List.of("Read book", "Submit report"),
                listed.tasks().stream().map(TaskView::description).toList());
        CommandResponse found = respond("find report");
        assertEquals("Here are your matching tasks.", found.message());
        assertEquals(List.of(listed.tasks().get(1).description()),
                found.tasks().stream().map(TaskView::description).toList());
        assertEquals(1, found.tasks().getFirst().number());
        assertEquals(found.tasks(), respond("findtag #work").tasks());

        CommandResponse failed = respond("deadline Bad date /by tomorrow");
        assertTrue(failed.isError());
        assertTrue(failed.tasks().isEmpty());
        assertTrue(failed.message().contains("yyyy-MM-dd HHmm"));
        assertEquals("No matching tasks. Try another keyword or use list to see all tasks.",
                respond("find missing").message());
    }

    private CommandResponse respond(String command) {
        output.reset();
        ui.clearTasks();
        CommandResult result = commandHandler.handleCommand(command);
        return new CommandResponse(output.toString().strip(), result == CommandResult.EXIT,
                result == CommandResult.ERROR, ui.getTasks());
    }
}
