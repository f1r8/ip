package athena.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest {

    @Test
    void handleCommand_bye_returnsTrue() {
        assertTrue(CommandHandler.handleCommand("bye"));

    }

    @Test
    void handleCommand_list_returnsFalse() {
        assertFalse(CommandHandler.handleCommand("list"));

    }

    @Test
    void getExitMessage_returnsFarewell() {
        assertTrue(CommandHandler.getExitMessage().contains("Farewell, Your Majesty"));
    }
}