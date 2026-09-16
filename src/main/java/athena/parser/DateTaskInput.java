package athena.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import athena.exception.AthenaException;

/**
 * Splits dated task arguments while rejecting repeated or misplaced date parameters.
 */
public final class DateTaskInput {
    private static final Pattern PARAMETER = Pattern.compile("(?<!\\S)/(by|from|to)(?=\\s|$)");

    private DateTaskInput() {
    }

    /**
     * Extracts a description and exactly one value for each expected parameter in order.
     *
     * @param input Task arguments.
     * @param missingMessage Message for missing parameters or values.
     * @param parameters Expected parameter names in order.
     * @return Description followed by the parameter values.
     */
    public static String[] parse(String input, String missingMessage, String... parameters) {
        if (input == null) {
            throw new AthenaException(missingMessage);
        }
        for (String parameter : parameters) {
            if (!Pattern.compile("(?<!\\S)/" + parameter + "(?=\\s|$)").matcher(input).find()) {
                throw new AthenaException(missingMessage);
            }
        }
        Matcher matcher = PARAMETER.matcher(input);
        String[] parts = new String[parameters.length + 1];
        int count = 0;
        int previousEnd = 0;
        while (matcher.find()) {
            if (count >= parameters.length || !matcher.group(1).equals(parameters[count])) {
                throw new AthenaException("Use each date parameter once and in the correct order, Your Majesty.");
            }
            parts[count] = input.substring(previousEnd, matcher.start()).strip();
            previousEnd = matcher.end();
            count++;
        }
        if (count != parameters.length) {
            throw new AthenaException(missingMessage);
        }
        parts[count] = input.substring(previousEnd).strip();
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new AthenaException(missingMessage);
            }
        }
        return parts;
    }
}
