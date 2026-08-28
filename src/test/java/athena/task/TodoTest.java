package athena.task;

import athena.exception.AthenaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests todo construction, validation, display, and storage formatting.
 */
class TodoTest {

    @Test
    void constructor_description_incompleteTodoCreated() {
        Todo todo = new Todo("Read book");

        assertEquals("[T][ ] Read book", todo.toString());
        assertEquals("T | 0 | Read book", todo.getSaveString());
    }

    @Test
    void constructor_savedCompletedTodo_completedTodoRestored() {
        Todo todo = new Todo(true, "Read book");

        assertEquals("[T][X] Read book", todo.toString());
        assertEquals("T | 1 | Read book", todo.getSaveString());
    }

    @Test
    void constructors_emptyDescription_exceptionThrown() {
        AthenaException newTodoException = assertThrows(AthenaException.class,
                () -> new Todo(""));
        AthenaException savedTodoException = assertThrows(AthenaException.class,
                () -> new Todo(true, ""));

        assertEquals("Please provide a todo description, Your Majesty.", newTodoException.getMessage());
        assertEquals("Please provide a todo description, Your Majesty.", savedTodoException.getMessage());
    }
}
