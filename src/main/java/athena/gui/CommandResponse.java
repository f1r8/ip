package athena.gui;

import java.util.List;

/**
 * Contains the message and application state produced for a GUI command.
 *
 * @param message Message to display.
 * @param shouldExit {@code true} if the application should exit, {@code false} otherwise.
 * @param isError {@code true} if the command failed and should remain available for editing.
 * @param tasks Immutable task details to display below the response caption.
 */
public record CommandResponse(String message, boolean shouldExit, boolean isError, List<TaskView> tasks) {
    /**
     * Copies task details so conversation replies retain their original state.
     */
    public CommandResponse {
        tasks = List.copyOf(tasks);
    }

    /**
     * Constructs a response without structured task details.
     */
    public CommandResponse(String message, boolean shouldExit, boolean isError) {
        this(message, shouldExit, isError, List.of());
    }
}
