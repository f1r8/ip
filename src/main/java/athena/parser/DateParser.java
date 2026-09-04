package athena.parser;

import athena.exception.AthenaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Parses and formats dates for the Athena application.
 */
public class DateParser {
    /** Default date and time pattern for application output */
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy, HH:mm");

    /**
     * Constructs a date parser.
     */
    public DateParser() {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
        try {
            return LocalDateTime.parse(input, formatter);
        } catch (DateTimeParseException e) {
            throw new AthenaException("Invalid date format, please use "
                    + "'yyyy-MM-dd HHmm' (e.g. 2001-09-11 1911)");
        }
    }
}
