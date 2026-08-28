package athena.parser;

import athena.exception.AthenaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parse Dates for Athena application.
 */
public class DateParser {
    /** Default date and time pattern for the application output */
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy, HH:mm");

    /**
     * Converts LocalDateTime objects into Strings.
     *
     * @return Date and time formatted with the default format.
     */
    public static String formatOutput(LocalDateTime date) {
        return OUTPUT_FORMATTER.format(date);
    }

    /**
     * Converts Strings into LocalDateTime objects.
     *
     * @return LocalDateTime objects.
     */
    public static LocalDateTime parse(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
        try {
            return LocalDateTime.parse(input, formatter);
        } catch (DateTimeParseException e) {
            throw new AthenaException("Invalid date format, please use "
                    + "'yyyy-MM-dd HHmm' (e.g. 2001-09-11 1911)");
        }
    }
}
