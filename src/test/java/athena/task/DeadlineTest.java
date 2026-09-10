package athena.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

import athena.exception.AthenaException;

/**
 * Tests deadline parsing, restoration, validation, and formatting.
 */
class DeadlineTest {

    @Test
    void constructor_commandInput_deadlineParsedAndTrimmed() {
        Deadline deadline = new Deadline("  Submit report /by 2026-12-31 2359  ");

        assertEquals("[D][ ] Submit report (by: Dec 31, 2026, 23:59)", deadline.toString());
        assertEquals("D | 0 | Submit report | 2026-12-31T23:59", deadline.getSaveString());
    }

    @Test
    void constructor_descriptionAndDate_deadlineCreated() {
        Deadline deadline = new Deadline("Submit report", "2026-12-31 2359");

        assertEquals("[D][ ] Submit report (by: Dec 31, 2026, 23:59)", deadline.toString());
    }

    @Test
    void constructor_descriptionContainsSlash_descriptionPreserved() {
        Deadline deadline = new Deadline("Read chapter 1/2 /by 2026-12-31 2359");

        assertEquals("[D][ ] Read chapter 1/2 (by: Dec 31, 2026, 23:59)", deadline.toString());
    }

    @Test
    void constructor_savedCompletedDeadline_completedDeadlineRestored() {
        Deadline deadline = new Deadline(true, "Submit report", "2026-12-31T23:59");

        assertEquals("[D][X] Submit report (by: Dec 31, 2026, 23:59)", deadline.toString());
        assertEquals("D | 1 | Submit report | 2026-12-31T23:59", deadline.getSaveString());
    }

    @Test
    void taggedDeadline_displayAndStorage_tagsFollowDeadline() {
        Deadline deadline = new Deadline("Submit report", "2026-12-31 2359");
        deadline.addTag(new Tag("#Work"));

        assertEquals("[D][ ] Submit report (by: Dec 31, 2026, 23:59) #Work",
                deadline.toString());
        assertEquals("D | 0 | Submit report | 2026-12-31T23:59 | #Work",
                deadline.getSaveString());
    }

    @Test
    void constructor_missingByDate_exceptionThrown() {
        AthenaException exception = assertThrows(AthenaException.class, () ->
                new Deadline("Submit report"));

        assertEquals("Please provide a deadline and /by date, Your Majesty.", exception.getMessage());
    }

    @Test
    void constructor_emptyDescription_exceptionThrown() {
        assertThrows(AthenaException.class, () -> new Deadline("", "2026-12-31 2359"));
    }

    @Test
    void constructor_invalidCommandDate_exceptionThrown() {
        assertThrows(AthenaException.class, () ->
                new Deadline("Submit report", "31-12-2026 23:59"));
    }

    @Test
    void constructor_invalidSavedDate_exceptionThrown() {
        assertThrows(DateTimeParseException.class, () ->
                new Deadline(false, "Submit report", "31-12-2026 23:59"));
    }
}
