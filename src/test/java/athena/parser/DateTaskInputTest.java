package athena.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import athena.exception.AthenaException;

/**
 * Tests delimiter boundaries and validation independently of date conversion.
 */
class DateTaskInputTest {
    private static final String MISSING_MESSAGE = "Missing task details";

    @Test
    void parse_whitespace_stripsEachPart() {
        assertArrayEquals(new String[]{"Meeting", "start", "end"},
                DateTaskInput.parse("  Meeting\t/from\tstart  /to end  ", MISSING_MESSAGE, "from", "to"));
    }

    @Test
    void parse_delimiterLikeDescription_preservesLiteralText() {
        assertArrayEquals(new String[]{"Read /bypass and path/by", "date"},
                DateTaskInput.parse("Read /bypass and path/by /by date", MISSING_MESSAGE, "by"));
    }

    @Test
    void parse_emptyDescription_leavesDescriptionValidationToTask() {
        assertArrayEquals(new String[]{"", "date"},
                DateTaskInput.parse("/by date", MISSING_MESSAGE, "by"));
    }

    @TestFactory
    Stream<DynamicTest> parse_missingDeadlineParts_reportsSuppliedMessage() {
        return Arrays.asList(null, "", "Read book", "Read/by date", "Read /bypass date", "Read /BY date",
                "Read /by", "Read /by   ").stream().map(input -> DynamicTest.dynamicTest("Input: " + input, () -> {
                    AthenaException exception = assertThrows(AthenaException.class, () ->
                            DateTaskInput.parse(input, MISSING_MESSAGE, "by"));
                    assertEquals(MISSING_MESSAGE, exception.getMessage());
                }));
    }

    @TestFactory
    Stream<DynamicTest> parse_missingEventValues_reportsSuppliedMessage() {
        return List.of("Meeting /from start", "Meeting /to end", "Meeting /from /to end",
                "Meeting /from start /to").stream().map(input -> DynamicTest.dynamicTest(input, () -> {
                    AthenaException exception = assertThrows(AthenaException.class, () ->
                            DateTaskInput.parse(input, MISSING_MESSAGE, "from", "to"));
                    assertEquals(MISSING_MESSAGE, exception.getMessage());
                }));
    }

    @TestFactory
    Stream<DynamicTest> parse_misplacedEventParameters_reportsOrderError() {
        return List.of("Meeting /to end /from start", "Meeting /from start /from again /to end",
                "Meeting /from start /to end /to again", "Meeting /by date /from start /to end")
                .stream().map(input -> DynamicTest.dynamicTest(input, () -> {
                    AthenaException exception = assertThrows(AthenaException.class, () ->
                            DateTaskInput.parse(input, MISSING_MESSAGE, "from", "to"));
                    assertEquals("Use each date parameter once and in the correct order, Your Majesty.",
                            exception.getMessage());
                }));
    }
}
