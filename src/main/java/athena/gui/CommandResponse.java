package athena.gui;

/**
 * Contains the message and application state produced for a GUI command.
 *
 * @param message Message to display.
 * @param shouldExit {@code true} if the application should exit, {@code false} otherwise.
 * @param isError {@code true} if the command failed and should remain available for editing.
 */
public record CommandResponse(String message, boolean shouldExit, boolean isError) {
}
