package athena.task;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the in-memory task collection and its index behavior.
 */
class TaskListTest {

    @Test
    void newTaskList_noTasks() {
        TaskList taskList = new TaskList();

        assertEquals(0, taskList.size());
        assertEquals(List.of(), taskList.getTasks());
    }

    @Test
    void add_tasksAddedInOrder() {
        TaskList taskList = new TaskList();
        Todo first = new Todo("Read book");
        Todo second = new Todo("Write report");

        taskList.add(first);
        taskList.add(second);

        assertEquals(2, taskList.size());
        assertSame(first, taskList.get(0));
        assertSame(second, taskList.get(1));
        assertEquals(List.of(first, second), taskList.getTasks());
    }

    @Test
    void remove_validIndex_taskRemovedAndReturned() {
        TaskList taskList = new TaskList();
        Todo first = new Todo("Read book");
        Todo second = new Todo("Write report");
        taskList.add(first);
        taskList.add(second);

        Task removed = taskList.remove(0);

        assertSame(first, removed);
        assertEquals(1, taskList.size());
        assertSame(second, taskList.get(0));
    }

    @Test
    void get_outOfRangeIndex_exceptionThrown() {
        TaskList taskList = new TaskList();

        assertThrows(IndexOutOfBoundsException.class, () -> taskList.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> taskList.get(-1));
    }

    @Test
    void remove_outOfRangeIndex_exceptionThrown() {
        TaskList taskList = new TaskList();

        assertThrows(IndexOutOfBoundsException.class, () -> taskList.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> taskList.remove(-1));
    }
}
