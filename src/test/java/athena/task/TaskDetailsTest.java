package athena.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import athena.exception.AthenaException;

/**
 * Tests identity boundaries and ownership of mutable task collections.
 */
class TaskDetailsTest {
    @Test
    void hasSameDetails_typeDescriptionAndDates_determineIdentity() {
        List<Task> tasks = List.of(new Todo("Report"), new Todo("Other"),
                new Deadline("Report", "2026-01-01 1200"), new Deadline("Report", "2026-01-01 1201"),
                new Event("Report", "2026-01-01 1200", "2026-01-01 1300"),
                new Event("Report", "2026-01-01 1201", "2026-01-01 1300"),
                new Event("Report", "2026-01-01 1200", "2026-01-01 1301"));
        for (int i = 0; i < tasks.size(); i++) {
            for (int j = 0; j < tasks.size(); j++) {
                assertEquals(i == j, tasks.get(i).hasSameDetails(tasks.get(j)), i + " versus " + j);
            }
        }
        assertEquals(tasks, new TaskList(tasks).getTasks());
    }

    @Test
    void hasSameDetails_caseStatusAndTags_doNotDistinguishTasks() {
        Task first = new Deadline("  Read   report ", "2026-01-01 1200");
        Task second = new Deadline(true, "READ REPORT", "2026-01-01T12:00", List.of(new Tag("#Work")));
        assertTrue(first.hasSameDetails(second));
        assertTrue(second.hasSameDetails(first));
        assertThrows(AthenaException.class, () -> new TaskList(List.of(first, second)));
        TaskList tasks = new TaskList(List.of(first));
        assertThrows(AthenaException.class, () -> tasks.add(second));
        assertEquals(List.of(first), tasks.getTasks());
    }

    @Test
    void getTags_sourceAndSnapshotChanges_doNotChangeTaskTags() {
        List<Tag> source = new ArrayList<>(List.of(new Tag("#Work")));
        Task task = new Todo(false, "Report", source);
        List<Tag> snapshot = task.getTags();
        source.clear();
        task.addTag(new Tag("#Home"));
        assertEquals(List.of(new Tag("#Work")), snapshot);
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertEquals(List.of(new Tag("#Home"), new Tag("#Work")), task.getTags());
        assertFalse(task.hasTag(new Tag("#Missing")));
    }

    @Test
    void getTasks_laterAddAndRemove_preservesSnapshotOrder() {
        Task first = new Todo("First");
        Task second = new Todo("Second");
        TaskList tasks = new TaskList(List.of(first));
        List<Task> snapshot = tasks.getTasks();
        tasks.add(second);
        tasks.remove(0);
        assertEquals(List.of(first), snapshot);
        assertEquals(List.of(second), tasks.getTasks());
    }

    @TestFactory
    Stream<DynamicTest> constructor_unsafeDescriptions_rejected() {
        return Arrays.asList(null, "", "  ", "A|B", "A\tB", "A\nB", "A\rB", "A\u0000B",
                "A\u007fB", "A\u0085B", "A\u2028B", "A\u2029B").stream()
                .map(input -> DynamicTest.dynamicTest("Input: " + input, () ->
                        assertThrows(AthenaException.class, () -> new Deadline(input, "2026-01-01 1200"))));
    }

    @Test
    void constructor_unicodeDescription_preservesTextAndNormalizesSpaces() {
        assertEquals("Read café 猫 🦉", new Todo("  Read   café 猫 🦉  ").getDescription());
    }
}
