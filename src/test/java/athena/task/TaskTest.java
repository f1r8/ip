package athena.task;

import athena.exception.AthenaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the common state and storage behavior inherited by every task type.
 */
class TaskTest {

    /**
     * Concrete task used to exercise the base class without subtype formatting.
     */
    private static class TestTask extends Task {
        TestTask(String name) {
            super(name);
        }

        TestTask(boolean isDone, String name) {
            super(isDone, name);
        }
    }

    @Test
    void constructor_description_startsIncomplete() {
        Task task = new TestTask("Read book");

        assertEquals(" ", task.getStatusIcon());
        assertEquals("0", task.getStoreStatusIcon());
        assertEquals("[ ] Read book", task.toString());
        assertEquals("0 | Read book", task.getSaveString());
    }

    @Test
    void constructor_completedTask_restoresCompletedState() {
        Task task = new TestTask(true, "Read book");

        assertEquals("X", task.getStatusIcon());
        assertEquals("1", task.getStoreStatusIcon());
        assertEquals("[X] Read book", task.toString());
        assertEquals("1 | Read book", task.getSaveString());
    }

    @Test
    void constructors_emptyDescription_exceptionThrown() {
        assertThrows(AthenaException.class, () -> new TestTask(""));
        assertThrows(AthenaException.class, () -> new TestTask(true, ""));
    }

    @Test
    void setDone_bothValues_statusUpdated() {
        Task task = new TestTask("Read book");

        task.setDone(true);
        assertEquals("X", task.getStatusIcon());

        task.setDone(false);
        assertEquals(" ", task.getStatusIcon());
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
    void isDoneFromStatus_validStatus_booleanReturned() {
        assertTrue(Task.isDoneFromStatus("1"));
        assertFalse(Task.isDoneFromStatus("0"));
    }

    @Test
    void isDoneFromStatus_unknownStatus_exceptionThrown() {
        AthenaException exception = assertThrows(AthenaException.class,
                () -> Task.isDoneFromStatus("X"));

        assertEquals("Error converting save string to num: X", exception.getMessage());
    }
}
