package athena.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import athena.exception.AthenaException;

/**
 * Tests date parsing and display formatting used by dated tasks.
 */
class DateParserTest {

    @TestFactory
    Stream<DynamicTest> parse_invalidBoundaries_reportsDateGuidance() {
        return Arrays.asList(null, "", "0000-01-01 0000", "2026-00-01 1200", "2026-01-00 1200",
                "2026-01-32 1200", "1900-02-29 1200", "2026-02-29 1200", "2026-04-31 1200",
                "2026-01-01 2400", "2026-01-01 1260", "2026-1-01 1200", "2026-01-1 1200",
                "2026-01-01 120", "2026-01-01 12:00", "2026-01-01T1200", "2026-01-01 1200 extra")
                .stream().map(input -> DynamicTest.dynamicTest("Input: " + input, () -> {
                    AthenaException exception = assertThrows(AthenaException.class, () -> DateParser.parse(input));
                    assertEquals("Please use 'yyyy-MM-dd HHmm' for the date and time, Your Majesty "
                            + "(e.g. 2026-12-31 2359).", exception.getMessage());
                }));
    }

    @Test
    void parse_supportedBoundariesAndWhitespace_returnsExactTime() {
        assertEquals(LocalDateTime.of(1, 1, 1, 0, 0), DateParser.parse("0001-01-01 0000"));
        assertEquals(LocalDateTime.of(9999, 12, 31, 23, 59), DateParser.parse("9999-12-31 2359"));
        assertEquals(LocalDateTime.of(2000, 2, 29, 0, 0), DateParser.parse("  2000-02-29\t  0000  "));
    }

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

        assertEquals("Please use 'yyyy-MM-dd HHmm' for the date and time, Your Majesty "
                + "(e.g. 2026-12-31 2359).", exception.getMessage());
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
