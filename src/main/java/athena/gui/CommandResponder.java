package athena.gui;

/**
 * Processes commands entered through the graphical user interface.
 */
@FunctionalInterface
public interface CommandResponder {
    /**
     * Returns the response produced for a user command.
     *
     * @param input User command to process.
     * @return Response containing the message and resulting application state.
     */
    CommandResponse getResponse(String input);
}
