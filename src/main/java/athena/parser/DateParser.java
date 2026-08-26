package athena.parser;

import athena.exception.AthenaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParser {
    public static DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, HH:mm");

    public static String formatOutput(LocalDateTime date){
        return outputFormatter.format(date);
    }

    public static LocalDateTime parse(String input) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm");
        try {
            return LocalDateTime.parse(input, formatter);
        }
        catch (DateTimeParseException e) {
            throw new AthenaException("Invalid date format, please use 'yyyy-MM-dd HHmm' (e.g. 2001-09-11 1911)");
        }
    }
}
