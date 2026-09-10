package athena.parser;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import athena.exception.AthenaException;

/**
 * Parses and formats dates for the Athena application.
 */
public class DateParser {
    /** Required date and time pattern for application input */
    private static final String INPUT_PATTERN = "yyyy-MM-dd HHmm";

    /** Sample date and time pattern for showing an example */
    private static final String SAMPLE_INPUT_FORMAT = DateTimeFormatter.ofPattern(INPUT_PATTERN).format(
            LocalDateTime.of(2001, Month.SEPTEMBER, 11, 19, 11));

    /** Date and time formatter for application input */
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern(INPUT_PATTERN);

    /** Default date and time pattern for application output */
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy, HH:mm");

    /**
     * Prevents instantiation of this utility class.
     */
    private DateParser() {
    }

    /**
     * Formats a date and time for display.
     *
     * @param date Date and time to format.
     * @return Date and time formatted with the default output format.
     */
    public static String formatOutput(LocalDateTime date) {
        return OUTPUT_FORMATTER.format(date);
    }

    /**
     * Parses a date and time in the application's command format.
     *
     * @param input Date and time in `yyyy-MM-dd HHmm` format.
     * @return Parsed date and time.
     * @throws AthenaException If the input does not match the required format.
     */
    public static LocalDateTime parse(String input) {
        try {
            return LocalDateTime.parse(input, INPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new AthenaException("Invalid date format, please use "
                    + "'" + INPUT_PATTERN + "' (e.g. " + SAMPLE_INPUT_FORMAT + ")");
        }
    }
}
