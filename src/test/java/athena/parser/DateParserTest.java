package athena.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import athena.exception.AthenaException;

/**
 * Tests date parsing and display formatting used by dated tasks.
 */
class DateParserTest {

    @Test
    void parse_validDateTime_localDateTimeReturned() {
        LocalDateTime result = DateParser.parse("2026-12-31 2359");

        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59), result);
    }

    @Test
    void parse_leapDay_localDateTimeReturned() {
        LocalDateTime result = DateParser.parse("2024-02-29 0000");

        assertEquals(LocalDateTime.of(2024, 2, 29, 0, 0), result);
    }

    @Test
    void parse_wrongFormat_exceptionThrown() {
        AthenaException exception = assertThrows(AthenaException.class, () ->
                DateParser.parse("31-12-2026 23:59"));

        assertEquals("Invalid date format, please use "
                + "'yyyy-MM-dd HHmm' (e.g. 2001-09-11 1911)", exception.getMessage());
    }

    @Test
    void parse_impossibleDate_exceptionThrown() {
        assertThrows(AthenaException.class, () -> DateParser.parse("2025-13-01 1200"));
    }

    @Test
    void formatOutput_dateTime_readableDateReturned() {
        LocalDateTime date = LocalDateTime.of(2026, 12, 31, 23, 59);

        assertEquals("Dec 31, 2026, 23:59", DateParser.formatOutput(date));
    }
}
