package athena.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Verifies command submission, error recovery, and conversation layout in the GUI.
 */
@ExtendWith(ApplicationExtension.class)
class MainWindowTest {
    private static final String INVALID_DEADLINE = "deadline Submit report /by";
    private static final String DEADLINE_EXAMPLE = "deadline Submit report /by 2026-12-31 2359";
    private static final String DEADLINE_ERROR = "Please provide a deadline and /by date, Your Majesty.";
    private static final PseudoClass INVALID_INPUT = PseudoClass.getPseudoClass("invalid-input");

    private final List<String> receivedInputs = new ArrayList<>();

    private Stage stage;

    @Start
    void start(Stage stage) throws IOException {
        Font.loadFont(Main.class.getResourceAsStream("/fonts/static/Inter_18pt-Regular.ttf"), 16);
        Font.loadFont(Main.class.getResourceAsStream("/fonts/static/Inter_18pt-Bold.ttf"), 16);

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        AnchorPane root = fxmlLoader.load();
        MainWindow mainWindow = fxmlLoader.getController();
        mainWindow.setCommandResponder(this::getResponse);
        mainWindow.setExitHandler(stage::close);

        stage.setScene(new Scene(root));
        stage.show();
        this.stage = stage;
    }

    @Test
    void startup_fxmlLoaded_controlsAndResourcesAvailable(FxRobot robot) {
        assertTrue(stage.isShowing());
        assertEquals("Send", robot.lookup("#sendButton").queryAs(Button.class).getText());
        assertNotNull(robot.lookup("#userInput").queryAs(TextField.class));
        assertNotNull(robot.lookup("#dialogContainer").queryAs(VBox.class));
        assertNotNull(MainWindow.class.getResource("/view/OwlAvatar.fxml"));
        assertErrorPanelVisibility(robot, false);
    }

    @Test
    void submitCommand_buttonAndEnter_addsDialogsAndClearsInput(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);

        robot.interact(() -> userInput.setText("todo Read book"));
        Button sendButton = robot.lookup("#sendButton").queryAs(Button.class);
        robot.interact(sendButton::fire);

        assertEquals("", userInput.getText());
        assertEquals(2, dialogContainer.getChildren().size());
        assertDialog(robot, dialogContainer, 0, "todo Read book", Pos.TOP_RIGHT);
        assertDialog(robot, dialogContainer, 1, "Response: todo Read book", Pos.TOP_LEFT);

        robot.interact(() -> userInput.setText("list"));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertEquals("", userInput.getText());
        assertEquals(4, dialogContainer.getChildren().size());
        assertEquals(List.of("todo Read book", "list"), receivedInputs);
    }

    @Test
    void submitCommand_invalidInput_preservesCommandAndShowsActionableError(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);
        VBox errorPanel = robot.lookup("#errorPanel").queryAs(VBox.class);
        String originalInput = "  " + INVALID_DEADLINE + "  ";

        robot.interact(() -> userInput.setText(originalInput));
        fireButton(robot, "#sendButton");
        robot.interact(errorPanel::applyCss);

        assertErrorPanelVisibility(robot, true);
        assertEquals(originalInput, userInput.getText());
        assertEquals(List.of(originalInput), receivedInputs);
        assertTrue(userInput.isFocused());
        assertEquals(originalInput.length(), userInput.getCaretPosition());
        assertTrue(userInput.getPseudoClassStates().contains(INVALID_INPUT));
        assertEquals(2, dialogContainer.getChildren().size());
        assertDialog(robot, dialogContainer, 0, originalInput, Pos.TOP_RIGHT);
        Label failedStatus = assertInstanceOf(Label.class, dialogContainer.getChildren().get(1));
        assertEquals("Command not completed", failedStatus.getText());
        assertTrue(failedStatus.getStyleClass().contains("failed-command-status"));
        assertEquals("Command needs attention",
                robot.lookup("#errorHeading").queryAs(Label.class).getText());
        assertEquals(DEADLINE_ERROR, robot.lookup("#errorExplanation").queryAs(Label.class).getText());
        assertFalse(robot.lookup("#errorHint").queryAs(Label.class).getText().isBlank());
        assertEquals(DEADLINE_EXAMPLE, robot.lookup("#errorExample").queryAs(Label.class).getText());
        assertFalse(robot.lookup("#inputStatus").queryAs(Label.class).getText().isBlank());
        assertFalse(errorPanel.getBackground().getFills().isEmpty());
        Color panelColor = assertInstanceOf(Color.class,
                errorPanel.getBackground().getFills().get(0).getFill());
        assertNotEquals(Color.WHITE, panelColor);
        assertTrue(panelColor.getOpacity() > 0.0);
    }

    @Test
    void submitCommand_repeatedFailuresThenCorrection_recoversWithoutLosingInput(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);
        Label inputStatus = robot.lookup("#inputStatus").queryAs(Label.class);

        robot.interact(() -> userInput.setText(INVALID_DEADLINE));
        fireButton(robot, "#sendButton");
        String failedStatus = inputStatus.getText();
        String retryInput = "deadline Submit report /by not-a-date";

        robot.interact(() -> userInput.setText(retryInput));

        assertErrorPanelVisibility(robot, true);
        assertNotEquals(failedStatus, inputStatus.getText());
        assertEquals(retryInput, userInput.getText());

        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertErrorPanelVisibility(robot, true);
        assertEquals(retryInput, userInput.getText());
        assertTrue(userInput.isFocused());
        assertEquals(retryInput.length(), userInput.getCaretPosition());
        assertEquals(4, dialogContainer.getChildren().size());
        assertDialog(robot, dialogContainer, 2, retryInput, Pos.TOP_RIGHT);
        assertInstanceOf(Label.class, dialogContainer.getChildren().get(3));

        robot.interact(() -> userInput.setText(DEADLINE_EXAMPLE));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertErrorPanelVisibility(robot, false);
        assertEquals("", userInput.getText());
        assertFalse(userInput.getPseudoClassStates().contains(INVALID_INPUT));
        assertTrue(userInput.isFocused());
        assertEquals(6, dialogContainer.getChildren().size());
        assertDialog(robot, dialogContainer, 4, DEADLINE_EXAMPLE, Pos.TOP_RIGHT);
        assertDialog(robot, dialogContainer, 5, "Response: " + DEADLINE_EXAMPLE, Pos.TOP_LEFT);
        assertEquals(List.of(INVALID_DEADLINE, retryInput, DEADLINE_EXAMPLE), receivedInputs);
    }

    @Test
    void useExample_unknownCommand_fillsInputWithoutSubmitting(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);

        robot.interact(() -> userInput.setText("dance"));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertErrorPanelVisibility(robot, true);
        assertEquals("dance", userInput.getText());
        assertEquals("Unknown command", robot.lookup("#errorExplanation").queryAs(Label.class).getText());
        assertFalse(robot.lookup("#errorHint").queryAs(Label.class).getText().isBlank());
        String example = robot.lookup("#errorExample").queryAs(Label.class).getText();
        assertFalse(example.isBlank());

        scrollToExample(robot);
        fireButton(robot, "#useExampleButton");

        assertEquals(example, userInput.getText());
        assertEquals(List.of("dance"), receivedInputs);
        assertEquals(2, dialogContainer.getChildren().size());
        assertErrorPanelVisibility(robot, true);
        assertTrue(userInput.isFocused());
        assertEquals(example.length(), userInput.getCaretPosition());
    }

    @Test
    void useExample_deadlineWithEditedDescription_preservesDescriptionWithoutSubmitting(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);

        robot.interact(() -> userInput.setText(INVALID_DEADLINE));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));
        robot.interact(() -> userInput.setText("deadline Send the revised report /by tomorrow"));
        scrollToExample(robot);
        fireButton(robot, "#useExampleButton");

        String correctedInput = "deadline Send the revised report /by 2026-12-31 2359";
        assertEquals(correctedInput, userInput.getText());
        assertEquals(List.of(INVALID_DEADLINE), receivedInputs);
        assertErrorPanelVisibility(robot, true);
        assertTrue(userInput.isFocused());
        assertEquals(correctedInput.length(), userInput.getCaretPosition());
    }

    @Test
    void errorPanel_narrowWindow_fitsAboveCommandInput(FxRobot robot) throws IOException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        ScrollPane errorScroll = robot.lookup("#errorScroll").queryAs(ScrollPane.class);
        Button sendButton = robot.lookup("#sendButton").queryAs(Button.class);

        robot.interact(() -> {
            stage.setWidth(400.0 + stage.getWidth() - stage.getScene().getWidth());
            userInput.setText(INVALID_DEADLINE);
            userInput.fireEvent(new ActionEvent());
        });
        robot.interact(() -> {
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
        });

        saveScreenshot(robot, Path.of("build", "reports", "gui", "error-panel.png"));

        Bounds panelBounds = errorScroll.localToScene(errorScroll.getLayoutBounds());
        Bounds inputBounds = userInput.localToScene(userInput.getLayoutBounds());
        Bounds sendBounds = sendButton.localToScene(sendButton.getLayoutBounds());
        assertEquals(400.0, stage.getScene().getWidth(), 1.0);
        assertTrue(panelBounds.getWidth() > 0.0);
        assertTrue(panelBounds.getHeight() > 0.0);
        assertTrue(panelBounds.getMinX() >= 0.0);
        assertTrue(panelBounds.getMaxX() <= stage.getScene().getWidth());
        assertTrue(panelBounds.getMaxY() <= inputBounds.getMinY());
        assertTrue(panelBounds.getMaxY() <= sendBounds.getMinY());
        assertTrue(inputBounds.getMaxY() <= stage.getScene().getHeight());
        assertTrue(sendBounds.getMaxX() <= stage.getScene().getWidth());
    }

    @Test
    void errorPanel_shortWindowAndLongCommand_keepsInputVisibleAndAllowsScrolling(FxRobot robot)
            throws IOException, InterruptedException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        ScrollPane errorScroll = robot.lookup("#errorScroll").queryAs(ScrollPane.class);
        VBox errorPanel = robot.lookup("#errorPanel").queryAs(VBox.class);
        Button sendButton = robot.lookup("#sendButton").queryAs(Button.class);
        Button useExampleButton = robot.lookup("#useExampleButton").queryAs(Button.class);
        String longCommand = "deadline " + "Review the detailed project report ".repeat(30) + "/by";

        robot.interact(() -> {
            stage.setWidth(400.0 + stage.getWidth() - stage.getScene().getWidth());
            stage.setHeight(400.0 + stage.getHeight() - stage.getScene().getHeight());
            userInput.setText(longCommand);
            userInput.fireEvent(new ActionEvent());
        });
        awaitLayoutPulse(robot);

        AtomicReference<Bounds> initialContentBounds = new AtomicReference<>();
        robot.interact(() -> {
            Bounds panelBounds = errorScroll.localToScene(errorScroll.getLayoutBounds());
            Bounds inputBounds = userInput.localToScene(userInput.getLayoutBounds());
            Bounds sendBounds = sendButton.localToScene(sendButton.getLayoutBounds());
            String layoutDetails = "panel=" + panelBounds + ", input=" + inputBounds + ", send=" + sendBounds;
            assertEquals(400.0, stage.getScene().getWidth(), 1.0);
            assertEquals(400.0, stage.getScene().getHeight(), 1.0);
            assertEquals(longCommand, userInput.getText());
            assertErrorPanelVisibility(robot, true);
            assertTrue(errorScroll.isFitToWidth());
            assertTrue(panelBounds.getMinY() >= 0.0, layoutDetails);
            assertTrue(panelBounds.getMaxY() <= inputBounds.getMinY(), layoutDetails);
            assertTrue(panelBounds.getMaxY() <= sendBounds.getMinY(), layoutDetails);
            assertTrue(inputBounds.getMaxY() <= stage.getScene().getHeight(), layoutDetails);
            assertTrue(sendBounds.getMaxY() <= stage.getScene().getHeight(), layoutDetails);
            assertTrue(errorPanel.getLayoutBounds().getHeight() > errorScroll.getViewportBounds().getHeight(),
                    "Expected overflowing content: content=" + errorPanel.getLayoutBounds()
                            + ", viewport=" + errorScroll.getViewportBounds());
            initialContentBounds.set(errorPanel.localToScene(errorPanel.getLayoutBounds()));
            errorScroll.setVvalue(errorScroll.getVmax());
        });
        awaitLayoutPulse(robot);

        robot.interact(() -> {
            Node viewport = errorScroll.lookup(".viewport");
            assertNotNull(viewport, "Expected the error ScrollPane's viewport after layout");
            Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
            Bounds contentBounds = errorPanel.localToScene(errorPanel.getLayoutBounds());
            Bounds buttonBounds = useExampleButton.localToScene(useExampleButton.getLayoutBounds());
            String scrollDetails = "viewport=" + viewportBounds + ", button=" + buttonBounds
                    + ", content=" + contentBounds + ", initialContent=" + initialContentBounds.get()
                    + ", vvalue=" + errorScroll.getVvalue();
            assertTrue(viewportBounds.getHeight() > 0.0,
                    "Expected a nonempty error viewport: " + scrollDetails);
            assertEquals(errorScroll.getVmax(), errorScroll.getVvalue(), 0.0001, scrollDetails);
            assertTrue(contentBounds.getMinY() < initialContentBounds.get().getMinY(), scrollDetails);
            assertTrue(buttonBounds.getMinY() >= viewportBounds.getMinY(),
                    "Expected button top inside viewport: " + scrollDetails);
            assertTrue(buttonBounds.getMaxY() <= viewportBounds.getMaxY(),
                    "Expected button bottom inside viewport: " + scrollDetails);
        });
        saveScreenshot(robot, Path.of("build", "reports", "gui", "error-panel-short-window.png"));
    }

    @Test
    void submitCommand_errorThenBye_closesWindow(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);

        robot.interact(() -> userInput.setText("dance"));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertTrue(stage.isShowing());
        assertErrorPanelVisibility(robot, true);

        robot.interact(() -> userInput.setText("bye"));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertFalse(stage.isShowing());
        assertEquals(List.of("dance", "bye"), receivedInputs);
    }

    @Test
    void taskRows_narrowWindow_wrapsDetailsAndKeepsStatusesAligned(FxRobot robot)
            throws IOException, InterruptedException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        robot.interact(() -> {
            setSceneSize(400.0, 600.0);
            userInput.setText("structured-list");
            userInput.fireEvent(new ActionEvent());
        });
        awaitLayoutPulse(robot);

        robot.interact(() -> {
            List<Label> descriptions = robot.lookup(".task-description").queryAllAs(Label.class).stream().toList();
            List<Label> statuses = robot.lookup(".task-status").queryAllAs(Label.class).stream().toList();
            assertEquals(2, descriptions.size());
            assertEquals(2, statuses.size());
            double statusX = statuses.getFirst().localToScene(statuses.getFirst().getLayoutBounds()).getMinX();
            for (Label status : statuses) {
                Bounds bounds = status.localToScene(status.getLayoutBounds());
                assertEquals(statusX, bounds.getMinX(), 1.0);
                assertTrue(bounds.getMaxX() < stage.getScene().getWidth());
                assertTrue(status.getWidth() >= status.prefWidth(-1) - 1.0,
                        "Completion status must be fully readable");
            }
            for (Label description : descriptions) {
                Bounds bounds = description.localToScene(description.getLayoutBounds());
                assertTrue(description.isWrapText());
                assertTrue(bounds.getMaxX() <= statusX);
                assertTrue(description.getHeight() >= description.prefHeight(description.getWidth()) - 1.0,
                        "Task description must wrap instead of truncating");
            }
            for (String selector : List.of(".task-schedule", ".task-tags")) {
                for (Label label : robot.lookup(selector).queryAllAs(Label.class)) {
                    assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()) - 1.0,
                            "Task details must remain fully readable");
                }
            }
            for (String selector : List.of(".task-type", ".task-tags")) {
                for (Label label : robot.lookup(selector).queryAllAs(Label.class)) {
                    assertInstanceOf(SVGPath.class, label.getGraphic());
                    assertFalse(label.getText().isBlank());
                }
            }
        });
        saveScreenshot(robot, Path.of("build", "reports", "gui", "task-rows.png"));
    }

    @Test
    void conversation_newReplyWhileReadingEarlier_preservesPosition(FxRobot robot) throws InterruptedException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        ScrollPane scrollPane = robot.lookup("#scrollPane").queryAs(ScrollPane.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);
        populateConversation(robot);
        awaitLayoutPulse(robot);

        for (String input : List.of("todo A newly added task", "dance")) {
            robot.interact(() -> scrollPane.setVvalue(scrollPane.getVmin()
                    + 0.4 * (scrollPane.getVmax() - scrollPane.getVmin())));
            awaitLayoutPulse(robot);
            AtomicReference<Double> previousPosition = new AtomicReference<>();
            Node earlierReply = dialogContainer.getChildren().get(6);
            robot.interact(() -> {
                double scrollableHeight = scrollPane.getContent().getLayoutBounds().getHeight()
                        - scrollPane.getViewportBounds().getHeight();
                assertTrue(scrollableHeight > 200.0, "Expected a conversation longer than its viewport");
                previousPosition.set(earlierReply.localToScene(earlierReply.getLayoutBounds()).getMinY());
                userInput.setText(input);
                userInput.fireEvent(new ActionEvent());
            });
            awaitLayoutPulse(robot);

            robot.interact(() -> {
                double currentPosition = earlierReply.localToScene(earlierReply.getLayoutBounds()).getMinY();
                assertEquals(previousPosition.get(), currentPosition, 2.0,
                        "Expected the earlier reply to stay in place after " + input);
                assertTrue(scrollPane.getVvalue() < scrollPane.getVmax(),
                        "Expected the reader to remain away from the conversation bottom");
            });
        }
    }

    @Test
    void conversation_newReplyNearBottom_followsLatestReply(FxRobot robot) throws InterruptedException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        ScrollPane scrollPane = robot.lookup("#scrollPane").queryAs(ScrollPane.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);
        populateConversation(robot);
        awaitLayoutPulse(robot);

        robot.interact(() -> {
            double scrollableHeight = scrollPane.getContent().getLayoutBounds().getHeight()
                    - scrollPane.getViewportBounds().getHeight();
            assertTrue(scrollableHeight > 200.0, "Expected a conversation longer than its viewport");
            double fraction = 1.0 - 16.0 / scrollableHeight;
            scrollPane.setVvalue(scrollPane.getVmin()
                    + fraction * (scrollPane.getVmax() - scrollPane.getVmin()));
        });
        awaitLayoutPulse(robot);
        robot.interact(() -> {
            userInput.setText("todo The latest task");
            userInput.fireEvent(new ActionEvent());
        });
        awaitLayoutPulse(robot);

        robot.interact(() -> {
            Node viewport = scrollPane.lookup(".viewport");
            Node latestReply = dialogContainer.getChildren().get(dialogContainer.getChildren().size() - 1);
            Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
            Bounds replyBounds = latestReply.localToScene(latestReply.getLayoutBounds());
            String layoutDetails = "reply=" + replyBounds + ", viewport=" + viewportBounds;
            assertEquals(scrollPane.getVmax(), scrollPane.getVvalue(), 0.0001);
            assertTrue(replyBounds.getMinY() >= viewportBounds.getMinY() - 1.0,
                    "Expected latest reply top in viewport: " + layoutDetails);
            assertTrue(replyBounds.getMaxY() <= viewportBounds.getMaxY() + 1.0,
                    "Expected latest reply bottom in viewport: " + layoutDetails);
        });
    }

    @Test
    void taskRows_windowResizeAndEnlargedText_reflowsWithinViewport(FxRobot robot)
            throws InterruptedException, IOException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);
        robot.interact(() -> {
            setSceneSize(400.0, 400.0);
            userInput.setText("structured-list");
            userInput.fireEvent(new ActionEvent());
        });
        awaitLayoutPulse(robot);
        AtomicReference<Double> narrowHeight = new AtomicReference<>();
        robot.interact(() -> {
            assertConversationFits(robot);
            assertEquals(2, dialogContainer.lookupAll(".task-description").size());
            assertEquals(2, dialogContainer.lookupAll(".task-schedule").size());
            narrowHeight.set(dialogContainer.getHeight());
        });

        robot.interact(() -> setSceneSize(720.0, 600.0));
        awaitLayoutPulse(robot);
        robot.interact(() -> {
            assertConversationFits(robot);
            assertTrue(dialogContainer.getHeight() < narrowHeight.get(),
                    "Expected fewer wrapped lines at 720 px: narrow=" + narrowHeight.get()
                            + ", wide=" + dialogContainer.getHeight());
        });
        saveScreenshot(robot, Path.of("build", "reports", "gui", "task-rows-wide.png"));

        robot.interact(() -> {
            setSceneSize(400.0, 400.0);
            for (Node node : dialogContainer.lookupAll(".label")) {
                node.setStyle("-fx-font-size: 22px;");
            }
            userInput.setStyle("-fx-font-size: 22px;");
            robot.lookup("#sendButton").queryAs(Button.class).setStyle("-fx-font-size: 22px;");
        });
        awaitLayoutPulse(robot);
        robot.interact(() -> {
            assertConversationFits(robot);
            assertTrue(dialogContainer.getHeight() > narrowHeight.get(),
                    "Expected the narrow conversation to grow for enlarged text");
            ScrollPane scrollPane = robot.lookup("#scrollPane").queryAs(ScrollPane.class);
            scrollPane.setVvalue(scrollPane.getVmin());
        });
        awaitLayoutPulse(robot);
        saveScreenshot(robot, Path.of("build", "reports", "gui", "responsive-enlarged-text.png"));
    }

    /**
     * Adds enough messages to test scrolling without controlling the system mouse pointer.
     */
    private void populateConversation(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        robot.interact(() -> {
            for (int i = 0; i < 18; i++) {
                userInput.setText("todo Read chapter " + i);
                userInput.fireEvent(new ActionEvent());
            }
        });
    }

    /**
     * Resizes the content area while allowing for native window decorations.
     */
    private void setSceneSize(double width, double height) {
        stage.setWidth(width + stage.getWidth() - stage.getScene().getWidth());
        stage.setHeight(height + stage.getHeight() - stage.getScene().getHeight());
    }

    /**
     * Checks wrapping labels and the pinned editor on the JavaFX application thread.
     */
    private void assertConversationFits(FxRobot robot) {
        ScrollPane scrollPane = robot.lookup("#scrollPane").queryAs(ScrollPane.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        Button sendButton = robot.lookup("#sendButton").queryAs(Button.class);
        Node viewport = scrollPane.lookup(".viewport");
        Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
        Bounds inputBounds = userInput.localToScene(userInput.getLayoutBounds());
        Bounds sendBounds = sendButton.localToScene(sendButton.getLayoutBounds());
        String layoutDetails = "viewport=" + viewportBounds + ", input=" + inputBounds + ", send=" + sendBounds;
        assertTrue(viewportBounds.getHeight() > 0.0, layoutDetails);
        assertTrue(inputBounds.getMinY() >= viewportBounds.getMaxY() - 1.0, layoutDetails);
        assertTrue(inputBounds.getWidth() > 0.0, layoutDetails);
        assertTrue(inputBounds.getMaxY() <= stage.getScene().getHeight() + 1.0, layoutDetails);
        assertTrue(sendBounds.getMaxX() <= stage.getScene().getWidth() + 1.0, layoutDetails);
        assertTrue(sendBounds.getMaxY() <= stage.getScene().getHeight() + 1.0, layoutDetails);
        assertTrue(sendButton.getWidth() >= sendButton.prefWidth(-1) - 1.0,
                "Expected the complete Send label to fit: " + layoutDetails);

        for (Node node : dialogContainer.lookupAll(".label")) {
            Label label = assertInstanceOf(Label.class, node);
            if (!label.isVisible() || !label.isManaged()) {
                continue;
            }
            Bounds labelBounds = label.localToScene(label.getLayoutBounds());
            String labelDetails = "label=" + label.getText() + ", bounds=" + labelBounds
                    + ", viewport=" + viewportBounds;
            assertTrue(labelBounds.getMinX() >= viewportBounds.getMinX() - 1.0, labelDetails);
            assertTrue(labelBounds.getMaxX() <= viewportBounds.getMaxX() + 1.0, labelDetails);
            assertTrue(label.getHeight() + 1.0 >= label.prefHeight(label.getWidth()),
                    "Expected unclipped label height: " + labelDetails);
        }
    }

    /**
     * Returns predictable responses without parsing commands or accessing task storage.
     */
    private CommandResponse getResponse(String input) {
        receivedInputs.add(input);
        String command = input.strip();
        if (command.equals("bye")) {
            return new CommandResponse("", true, false);
        }
        if (command.equals("dance")) {
            return new CommandResponse("\n  Unknown command  \n", false, true);
        }
        if (command.equals("structured-list")) {
            return new CommandResponse("Your tasks, Your Majesty.", false, false, List.of(
                    new TaskView(1, "Read the full project brief before preparing the final report", false,
                            "Deadline", "Due Dec 31, 2026, 23:59", List.of("#school", "#writing")),
                    new TaskView(2, "Discuss the report", true, "Event",
                            "From Dec 30, 2026, 14:00\nTo Dec 30, 2026, 15:00", List.of())));
        }
        if (command.startsWith("deadline ") && !command.endsWith("/by 2026-12-31 2359")) {
            return new CommandResponse("\n  " + DEADLINE_ERROR + "  \n", false, true);
        }
        return new CommandResponse("Response: " + input, false, false);
    }

    /**
     * Checks that the error panel and editing status take up space only when shown.
     */
    private void assertErrorPanelVisibility(FxRobot robot, boolean isVisible) {
        VBox errorPanel = robot.lookup("#errorPanel").queryAs(VBox.class);
        ScrollPane errorScroll = robot.lookup("#errorScroll").queryAs(ScrollPane.class);
        Label inputStatus = robot.lookup("#inputStatus").queryAs(Label.class);
        assertEquals(isVisible, errorPanel.isVisible());
        assertEquals(isVisible, errorPanel.isManaged());
        assertEquals(isVisible, errorScroll.isVisible());
        assertEquals(isVisible, errorScroll.isManaged());
        assertEquals(isVisible, inputStatus.isVisible());
        assertEquals(isVisible, inputStatus.isManaged());
    }

    /**
     * Saves the rendered JavaFX window for visual review without comparing it to a reference image.
     */
    private void saveScreenshot(FxRobot robot, Path target) throws IOException {
        int width = (int) Math.ceil(stage.getScene().getWidth());
        int height = (int) Math.ceil(stage.getScene().getHeight());
        WritableImage snapshot = new WritableImage(width, height);
        robot.interact(() -> stage.getScene().getRoot().snapshot(null, snapshot));

        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        Files.createDirectories(target.getParent());
        ImageIO.write(image, "png", target.toFile());
    }

    /**
     * Waits for a post-layout pulse so bounds reflect queued resize and scrolling updates.
     */
    private void awaitLayoutPulse(FxRobot robot) throws InterruptedException {
        CountDownLatch layoutComplete = new CountDownLatch(1);
        Runnable listener = layoutComplete::countDown;
        robot.interact(() -> {
            stage.getScene().addPostLayoutPulseListener(listener);
            Platform.requestNextPulse();
        });
        try {
            assertTrue(layoutComplete.await(5, TimeUnit.SECONDS), "Timed out waiting for JavaFX layout");
        } finally {
            robot.interact(() -> stage.getScene().removePostLayoutPulseListener(listener));
        }
    }

    /**
     * Focuses and activates a button on the JavaFX thread without controlling the system mouse.
     */
    private void fireButton(FxRobot robot, String selector) {
        Button button = robot.lookup(selector).queryAs(Button.class);
        robot.interact(() -> {
            button.requestFocus();
            button.fire();
        });
    }

    /**
     * Makes the example action reachable when the correction panel needs vertical scrolling.
     */
    private void scrollToExample(FxRobot robot) {
        ScrollPane errorScroll = robot.lookup("#errorScroll").queryAs(ScrollPane.class);
        robot.interact(() -> {
            errorScroll.setVvalue(errorScroll.getVmax());
            stage.getScene().getRoot().layout();
        });
    }

    /**
     * Checks a conversation bubble's text, alignment, avatar, and matching tail appearance.
     */
    private void assertDialog(FxRobot robot, VBox dialogContainer, int index, String expectedText,
            Pos expectedAlignment) {
        DialogBox dialogBox = assertInstanceOf(DialogBox.class, dialogContainer.getChildren().get(index));

        assertEquals(expectedAlignment, dialogBox.getAlignment());
        boolean isAthenaDialog = expectedAlignment == Pos.TOP_LEFT;
        assertEquals(isAthenaDialog ? 2 : 1, dialogBox.getChildren().size());
        VBox messageContent = assertInstanceOf(VBox.class,
                dialogBox.getChildren().get(isAthenaDialog ? 1 : 0));
        assertEquals(expectedAlignment, messageContent.getAlignment());
        Label timestamp = assertInstanceOf(Label.class, messageContent.getChildren().getLast());
        assertTrue(timestamp.getText().matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"));
        assertNotNull(timestamp.getTooltip());
        assertEquals("Message time: " + timestamp.getTooltip().getText(), timestamp.getAccessibleText());

        if (isAthenaDialog) {
            Pane avatar = assertInstanceOf(Pane.class, dialogBox.getChildren().get(0));
            assertEquals("Athena owl", avatar.getAccessibleText());
            assertFalse(avatar.getChildren().isEmpty());
        } else {
            assertTrue(dialogBox.lookupAll(".owl-avatar").isEmpty());
        }

        StackPane bubbleContainer = messageContent.getChildren().stream()
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .findFirst()
                .orElseThrow();
        VBox bubbleContent = bubbleContainer.getChildren().stream()
                .filter(VBox.class::isInstance)
                .map(VBox.class::cast)
                .findFirst()
                .orElseThrow();
        Label label = assertInstanceOf(Label.class, bubbleContent.getChildren().getFirst());
        Pane tail = assertInstanceOf(Pane.class, bubbleContainer.getChildren().stream()
                .filter(node -> "tail".equals(node.getId()))
                .findFirst()
                .orElseThrow());
        Region tailFill = assertInstanceOf(Region.class, tail.getChildren().get(0));
        SVGPath tailOutline = assertInstanceOf(SVGPath.class, tail.getChildren().get(1));

        robot.interact(() -> {
            dialogBox.applyCss();
            dialogBox.layout();
            Bounds timestampBounds = timestamp.localToScene(timestamp.getLayoutBounds());
            Bounds bubbleBounds = bubbleContainer.localToScene(bubbleContainer.getLayoutBounds());
            assertTrue(timestampBounds.getMinY() >= bubbleBounds.getMaxY(),
                    "Expected the timestamp below the message bubble");
        });
        Insets labelMargin = StackPane.getMargin(bubbleContent);

        assertEquals(expectedText, label.getText());
        assertFalse(bubbleContent.getBackground().getFills().isEmpty());
        assertEquals(isAthenaDialog ? Pos.BOTTOM_LEFT : Pos.BOTTOM_RIGHT, bubbleContainer.getAlignment());
        assertEquals(isAthenaDialog ? 11.0 : 47.0, labelMargin.getLeft());
        assertEquals(isAthenaDialog ? 47.0 : 11.0, labelMargin.getRight());
        assertTrue(tailFill.getStyleClass().contains("bubble"));
        assertEquals(bubbleContent.getBackground().getFills().get(0).getFill(),
                tailFill.getBackground().getFills().get(0).getFill());
        assertNotNull(tailFill.getShape());
        assertEquals(Color.TRANSPARENT, tailOutline.getFill());
        assertEquals(Color.BLACK, tailOutline.getStroke());
        assertEquals("M 3 0 C 4 6 8 9 14 9 C 11 10 8 11 3 12", tailOutline.getContent());
        assertEquals(tail, bubbleContainer.getChildren().get(bubbleContainer.getChildren().size() - 1));
        assertEquals(isAthenaDialog ? -1.0 : 1.0, tail.getScaleX());
        assertEquals(14.0, tail.getPrefWidth());
        assertEquals(12.0, tail.getPrefHeight());
    }
}
