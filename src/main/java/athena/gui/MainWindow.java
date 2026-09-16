package athena.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    private static final PseudoClass INVALID_INPUT = PseudoClass.getPseudoClass("invalid-input");
    private static final double AUTO_SCROLL_THRESHOLD = 32.0;
    private static final double ERROR_VIEWPORT_MAX_HEIGHT = 320.0;
    private static final double CONVERSATION_MIN_HEIGHT = 64.0;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane errorScroll;
    @FXML
    private VBox windowLayout;
    @FXML
    private VBox dialogContainer;
    @FXML
    private VBox welcomePanel;
    @FXML
    private VBox composer;
    @FXML
    private HBox composerHeader;
    @FXML
    private HBox commandInputRow;
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
    private CommandsDialog commandsDialog;

    private Image athenaImage = new Image(this.getClass().getResourceAsStream("/images/DaAthena.jpg"));

    /**
     * Keeps correction guidance available while editing.
     */
    @FXML
    public void initialize() {
        welcomePanel.managedProperty().bind(welcomePanel.visibleProperty());
        errorPanel.managedProperty().bind(errorPanel.visibleProperty());
        inputStatus.managedProperty().bind(inputStatus.visibleProperty());
        errorScroll.visibleProperty().bind(errorPanel.visibleProperty());
        errorScroll.managedProperty().bind(errorPanel.visibleProperty());
        errorScroll.maxHeightProperty().bind(Bindings.createDoubleBinding(this::getErrorViewportMaxHeight,
                windowLayout.heightProperty(), composer.widthProperty(), composer.insetsProperty(),
                composer.spacingProperty(), composerHeader.layoutBoundsProperty(),
                commandInputRow.layoutBoundsProperty(), inputStatus.layoutBoundsProperty(),
                inputStatus.managedProperty(), inputStatus.textProperty(), inputStatus.fontProperty(),
                errorScroll.managedProperty()));
        inputStatus.setLabelFor(userInput);
        userInput.textProperty().addListener((observable, oldInput, newInput) -> {
            if (errorPanel.isVisible()) {
                inputStatus.setText("I await your revised command, Your Majesty.");
                userInput.pseudoClassStateChanged(INVALID_INPUT, false);
            }
        });
        Platform.runLater(userInput::requestFocus);
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

        double previousScrollOffset = getConversationScrollOffset();
        boolean shouldFollowConversation = getConversationScrollableHeight() - previousScrollOffset
                <= AUTO_SCROLL_THRESHOLD;
        welcomePanel.setVisible(false);
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input));
        if (response.isError()) {
            showError(input, response.message());
            restoreConversationScroll(previousScrollOffset, shouldFollowConversation);
            return;
        }

        dialogContainer.getChildren().add(DialogBox.getAthenaDialog(response, athenaImage));
        errorPanel.setVisible(false);
        inputStatus.setVisible(false);
        userInput.pseudoClassStateChanged(INVALID_INPUT, false);
        userInput.setAccessibleHelp(null);
        errorGuidance = null;
        userInput.clear();
        userInput.requestFocus();
        restoreConversationScroll(previousScrollOffset, shouldFollowConversation);
    }

    /**
     * Reserves the measured composer controls and a small conversation area before sizing error guidance.
     */
    private double getErrorViewportMaxHeight() {
        Insets insets = composer.getInsets();
        double contentWidth = Math.max(0.0, composer.getWidth() - insets.getLeft() - insets.getRight());
        double controlsHeight = insets.getTop() + insets.getBottom();
        int managedChildren = 0;
        for (Node child : composer.getChildren()) {
            if (!child.isManaged()) {
                continue;
            }
            managedChildren++;
            if (child != errorScroll) {
                controlsHeight += child.prefHeight(contentWidth);
                Insets margin = VBox.getMargin(child);
                if (margin != null) {
                    controlsHeight += margin.getTop() + margin.getBottom();
                }
            }
        }
        controlsHeight += composer.getSpacing() * Math.max(0, managedChildren - 1);
        return Math.min(ERROR_VIEWPORT_MAX_HEIGHT,
                Math.max(0.0, windowLayout.getHeight() - controlsHeight - CONVERSATION_MIN_HEIGHT));
    }

    /**
     * Opens the command reference while keeping the draft and conversation position intact.
     */
    @FXML
    private void showCommands() {
        if (commandsDialog == null) {
            commandsDialog = new CommandsDialog(userInput.getScene().getWindow());
            commandsDialog.setOnHidden(event -> Platform.runLater(userInput::requestFocus));
        }
        if (commandsDialog.isShowing()) {
            commandsDialog.getDialogPane().getScene().getWindow().requestFocus();
            return;
        }
        commandsDialog.show();
    }

    /**
     * Returns the conversation height outside the viewport, including any introductory content.
     */
    private double getConversationScrollableHeight() {
        return Math.max(0.0, scrollPane.getContent().getLayoutBounds().getHeight()
                - scrollPane.getViewportBounds().getHeight());
    }

    /**
     * Returns the distance already scrolled from the start of the conversation in pixels.
     */
    private double getConversationScrollOffset() {
        double valueRange = scrollPane.getVmax() - scrollPane.getVmin();
        if (valueRange <= 0.0) {
            return 0.0;
        }
        double fraction = (scrollPane.getVvalue() - scrollPane.getVmin()) / valueRange;
        return getConversationScrollableHeight() * Math.max(0.0, Math.min(1.0, fraction));
    }

    /**
     * Follows new replies only when the reader was near the bottom; otherwise preserves the reading position.
     */
    private void restoreConversationScroll(double previousScrollOffset, boolean shouldFollowConversation) {
        Platform.runLater(() -> {
            windowLayout.applyCss();
            windowLayout.layout();
            double scrollableHeight = getConversationScrollableHeight();
            if (shouldFollowConversation || scrollableHeight <= 0.0) {
                scrollPane.setVvalue(scrollPane.getVmax());
                return;
            }
            double fraction = Math.max(0.0, Math.min(1.0, previousScrollOffset / scrollableHeight));
            scrollPane.setVvalue(scrollPane.getVmin()
                    + fraction * (scrollPane.getVmax() - scrollPane.getVmin()));
        });
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
