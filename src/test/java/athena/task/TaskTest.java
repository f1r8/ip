package athena.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import athena.exception.AthenaException;

/**
 * Tests the common state and storage behavior inherited by every task type.
 */
class TaskTest {

    /**
     * Concrete task used to exercise the base class without subtype formatting.
     */
    private static class TestTask extends Task {
        TestTask(String description) {
            super(description);
        }

        TestTask(boolean isDone, String description) {
            super(isDone, description);
        }
    }

    @Test
    void constructor_description_startsIncomplete() {
        Task task = new TestTask("Read book");

        assertEquals(" ", task.getStatusIcon());
        assertEquals("0", task.getSaveStatus());
        assertEquals("[ ] Read book", task.toString());
        assertEquals("0 | Read book", task.getSaveString());
    }

    @Test
    void constructor_completedTask_restoresCompletedState() {
        Task task = new TestTask(true, "Read book");

        assertEquals("X", task.getStatusIcon());
        assertEquals("1", task.getSaveStatus());
        assertEquals("[X] Read book", task.toString());
        assertEquals("1 | Read book", task.getSaveString());
    }

    @Test
    void constructors_emptyDescription_exceptionThrown() {
        AthenaException incompleteTaskException = assertThrows(AthenaException.class, () ->
                new TestTask(""));
        AthenaException completedTaskException = assertThrows(AthenaException.class, () ->
                new TestTask(true, ""));

        assertEquals("Task description cannot be empty", incompleteTaskException.getMessage());
        assertEquals("Task description cannot be empty", completedTaskException.getMessage());
    }

    @Test
    void markDone_incompleteTask_taskCompleted() {
        Task task = new TestTask("Read book");

        task.markDone();

        assertEquals("X", task.getStatusIcon());
    }

    @Test
    void unmarkDone_completedTask_taskIncomplete() {
        Task task = new TestTask(true, "Read book");

        task.unmarkDone();

        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    void tags_caseInsensitive_sortedAndFirstCurrentSpellingPreserved() {
        Task task = new TestTask("Read book");

        assertTrue(task.addTag(new Tag("#Work")));
        assertFalse(task.addTag(new Tag("#work")));
        assertTrue(task.addTag(new Tag("#Urgent")));

        assertEquals(List.of("#Urgent", "#Work"), task.getTags().stream().map(Tag::toString).toList());
        assertEquals("[ ] Read book #Urgent #Work", task.toString());
        assertEquals("0 | Read book | #Urgent,#Work", task.getSaveString());
        assertTrue(task.hasTag(new Tag("#wOrK")));
        assertTrue(task.removeTag(new Tag("#WORK")));
        assertFalse(task.removeTag(new Tag("#work")));
        assertTrue(task.addTag(new Tag("#wOrK")));
        assertEquals("[ ] Read book #Urgent #wOrK", task.toString());

        task.removeTag(new Tag("#urgent"));
        task.removeTag(new Tag("#work"));
        assertEquals("0 | Read book", task.getSaveString());
    }

    @Test
    void isDoneFromStatus_validStatus_booleanReturned() {
        assertTrue(Task.isDoneFromStatus("1"));
        assertFalse(Task.isDoneFromStatus("0"));
    }

    @Test
    void isDoneFromStatus_unknownStatus_exceptionThrown() {
        AthenaException exception = assertThrows(AthenaException.class, () ->
                Task.isDoneFromStatus("X"));

        assertEquals("Error converting save string to num: X", exception.getMessage());
    }
}
