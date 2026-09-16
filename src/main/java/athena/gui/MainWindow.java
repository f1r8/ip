package athena.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    private static final PseudoClass INVALID_INPUT = PseudoClass.getPseudoClass("invalid-input");

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane errorScroll;
    @FXML
    private VBox windowLayout;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;
    @FXML
    private VBox errorPanel;
    @FXML
    private Label errorExplanation;
    @FXML
    private Label errorHint;
    @FXML
    private Label errorExample;
    @FXML
    private Button useExampleButton;
    @FXML
    private Label inputStatus;

    private CommandResponder commandResponder;
    private Runnable exitHandler = Platform::exit;
    private ErrorGuidance errorGuidance;

    private Image athenaImage = new Image(this.getClass().getResourceAsStream("/images/DaAthena.jpg"));

    /**
     * Keeps the newest reply visible and preserves correction guidance while editing.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        errorPanel.managedProperty().bind(errorPanel.visibleProperty());
        inputStatus.managedProperty().bind(inputStatus.visibleProperty());
        errorScroll.visibleProperty().bind(errorPanel.visibleProperty());
        errorScroll.managedProperty().bind(errorPanel.visibleProperty());
        errorScroll.maxHeightProperty().bind(
                Bindings.min(320, Bindings.max(80, windowLayout.heightProperty().subtract(150))));
        inputStatus.setLabelFor(userInput);
        userInput.textProperty().addListener((observable, oldInput, newInput) -> {
            if (errorPanel.isVisible()) {
                inputStatus.setText("I await your revised command, Your Majesty.");
                userInput.pseudoClassStateChanged(INVALID_INPUT, false);
            }
        });
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
     * Displays the command result, preserving failed input beside its correction guidance.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        CommandResponse response = commandResponder.getResponse(input);
        if (response.shouldExit()) {
            exitHandler.run();
            return;
        }

        dialogContainer.getChildren().add(DialogBox.getUserDialog(input));
        if (response.isError()) {
            showError(input, response.message());
            return;
        }

        dialogContainer.getChildren().add(DialogBox.getAthenaDialog(response.message(), athenaImage));
        errorPanel.setVisible(false);
        inputStatus.setVisible(false);
        userInput.pseudoClassStateChanged(INVALID_INPUT, false);
        userInput.setAccessibleHelp(null);
        errorGuidance = null;
        userInput.clear();
        userInput.requestFocus();
    }

    /**
     * Presents a failed command with an explanation and an example beside the input.
     */
    private void showError(String input, String explanation) {
        Label failureStatus = new Label("Not fulfilled, Your Majesty.");
        failureStatus.getStyleClass().add("failed-command-status");
        failureStatus.setWrapText(true);
        failureStatus.setMaxWidth(Double.MAX_VALUE);
        dialogContainer.getChildren().add(failureStatus);

        errorGuidance = ErrorGuidance.forInput(input);
        errorExplanation.setText(explanation.strip());
        errorHint.setText(errorGuidance.hint());
        errorExample.setText(errorGuidance.example());
        useExampleButton.setText(errorGuidance.isDeadline()
                ? "Use this date and time" : "Place this example below");
        inputStatus.setText("I have kept your command for correction, Your Majesty.");
        errorPanel.setVisible(true);
        errorScroll.setVvalue(0);
        inputStatus.setVisible(true);
        userInput.pseudoClassStateChanged(INVALID_INPUT, true);
        userInput.setAccessibleHelp("Command error. " + explanation.strip() + " " + errorGuidance.hint()
                + " Valid example: " + errorGuidance.example());
        focusInput();
    }

    /**
     * Places an example in the editor without executing it or discarding an edited deadline description.
     */
    @FXML
    private void useExample() {
        if (errorGuidance == null) {
            return;
        }
        String correction = errorGuidance.getExampleFor(userInput.getText());
        userInput.setText(correction);
        inputStatus.setText("Please review these details before sending, Your Majesty.");
        userInput.pseudoClassStateChanged(INVALID_INPUT, false);
        focusInput();
    }

    /**
     * Returns keyboard focus to the end of the preserved command.
     */
    private void focusInput() {
        userInput.requestFocus();
        userInput.positionCaret(userInput.getLength());
    }
}
