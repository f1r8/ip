package athena.task;

import athena.exception.AthenaException;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests event parsing, restoration, validation, and formatting.
 */
class EventTest {

    @Test
    void constructor_commandInput_eventParsedAndTrimmed() {
        Event event = new Event("  Team meeting /from 2026-12-30 1400 /to 2026-12-30 1500  ");

        assertEquals("[E][ ] Team meeting (from: Dec 30, 2026, 14:00, "
                + "to: Dec 30, 2026, 15:00)", event.toString());
        assertEquals("E | 0 | Team meeting | 2026-12-30T14:00 | 2026-12-30T15:00",
                event.getSaveString());
    }

    @Test
    void constructor_descriptionAndDates_eventCreated() {
        Event event = new Event("Team meeting", "2026-12-30 1400", "2026-12-30 1500");

        assertEquals("[E][ ] Team meeting (from: Dec 30, 2026, 14:00, "
                + "to: Dec 30, 2026, 15:00)", event.toString());
    }

    @Test
    void constructor_savedCompletedEvent_completedEventRestored() {
        Event event = new Event(true, "Team meeting", "2026-12-30T14:00", "2026-12-30T15:00");

        assertEquals("[E][X] Team meeting (from: Dec 30, 2026, 14:00, "
                + "to: Dec 30, 2026, 15:00)", event.toString());
        assertEquals("E | 1 | Team meeting | 2026-12-30T14:00 | 2026-12-30T15:00",
                event.getSaveString());
    }

    @Test
    void constructor_missingFromDate_exceptionThrown() {
        AthenaException exception = assertThrows(AthenaException.class,
                () -> new Event("Team meeting /to 2026-12-30 1500"));

        assertEquals("Please provide an event with /from and /to times, Your Majesty.",
                exception.getMessage());
    }

    @Test
    void constructor_missingToDate_exceptionThrown() {
        assertThrows(AthenaException.class,
                () -> new Event("Team meeting /from 2026-12-30 1400"));
    }

    @Test
    void constructor_emptyDescription_exceptionThrown() {
        assertThrows(AthenaException.class,
                () -> new Event("", "2026-12-30 1400", "2026-12-30 1500"));
    }

    @Test
    void constructor_invalidStartDate_exceptionThrown() {
        assertThrows(AthenaException.class,
                () -> new Event("Team meeting", "30-12-2026 14:00", "2026-12-30 1500"));
    }

    @Test
    void constructor_invalidEndDate_exceptionThrown() {
        assertThrows(AthenaException.class,
                () -> new Event("Team meeting", "2026-12-30 1400", "30-12-2026 15:00"));
    }

    @Test
    void constructor_invalidSavedDate_exceptionThrown() {
        assertThrows(DateTimeParseException.class,
                () -> new Event(false, "Team meeting", "invalid", "2026-12-30T15:00"));
    }
}
