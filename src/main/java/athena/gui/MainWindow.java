package athena.gui;

import athena.exception.AthenaException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private CommandResponder commandResponder;
    private Runnable exitHandler = Platform::exit;

    private Image athenaImage = new Image(this.getClass().getResourceAsStream("/images/DaAthena.jpg"));

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /** Sets the component that processes user commands */
    void setCommandResponder(CommandResponder commandResponder) {
        this.commandResponder = commandResponder;
    }

    /** Sets the action used to exit the application */
    void setExitHandler(Runnable exitHandler) {
        this.exitHandler = exitHandler;
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Athena's reply,
     * and then appends them to the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        try {
            String input = userInput.getText();
            String response = commandResponder.getResponse(input);
            dialogContainer.getChildren().addAll(
                    DialogBox.getUserDialog(input),
                    DialogBox.getAthenaDialog(response, athenaImage)
            );
            userInput.clear();
        } catch (AthenaException e) {
            exitHandler.run();
        }
    }
}
