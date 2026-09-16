package athena.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Verifies first-use guidance and command-reference access without system mouse or keyboard input.
 */
@ExtendWith(ApplicationExtension.class)
class OnboardingTest {
    private final List<String> receivedInputs = new ArrayList<>();

    private Stage stage;

    @Start
    void start(Stage stage) throws IOException {
        Font.loadFont(Main.class.getResourceAsStream("/fonts/static/Inter_18pt-Regular.ttf"), 16);
        Font.loadFont(Main.class.getResourceAsStream("/fonts/static/Inter_18pt-Bold.ttf"), 16);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        AnchorPane root = loader.load();
        MainWindow controller = loader.getController();
        controller.setCommandResponder(input -> {
            receivedInputs.add(input);
            return new CommandResponse(input.equals("dance") ? "Unknown command" : "Response: " + input,
                    false, input.equals("dance"));
        });
        controller.setExitHandler(stage::close);
        stage.setScene(new Scene(root));
        stage.show();
        this.stage = stage;
    }

    @Test
    void startup_showsExamplesWithoutSubmittingCommands(FxRobot robot) throws InterruptedException, IOException {
        awaitLayoutPulse(robot, stage);
        VBox welcomePanel = robot.lookup("#welcomePanel").queryAs(VBox.class);
        Label examples = robot.lookup("#welcomeExamples").queryAs(Label.class);
        Button commandsButton = robot.lookup("#commandsButton").queryAs(Button.class);

        assertTrue(welcomePanel.isVisible());
        assertTrue(welcomePanel.isManaged());
        assertTrue(robot.lookup("#identityHeader").query().isVisible());
        assertEquals("Athena", robot.lookup(".identity-title").queryAs(Label.class).getText());
        assertEquals("Your task adviser", robot.lookup(".identity-subtitle").queryAs(Label.class).getText());
        assertEquals("todo Read a book\nlist", examples.getText());
        assertTrue(commandsButton.isFocusTraversable());
        assertTrue(commandsButton.isMnemonicParsing());
        assertEquals("Commands", commandsButton.getAccessibleText());
        assertTrue(robot.lookup("#userInput").queryAs(TextField.class).isFocused(),
                "Expected the command editor to receive typing immediately after startup");
        assertTrue(receivedInputs.isEmpty());
        assertTrue(robot.lookup("#dialogContainer").queryAs(VBox.class).getChildren().isEmpty());
        saveScreenshot(robot, stage, Path.of("build", "reports", "gui", "welcome.png"));
    }

    @Test
    void submitCommand_firstSuccess_removesWelcomeAndItsLayoutSpace(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox welcomePanel = robot.lookup("#welcomePanel").queryAs(VBox.class);
        robot.interact(() -> {
            userInput.setText("list");
            userInput.fireEvent(new ActionEvent());
        });

        assertFalse(welcomePanel.isVisible());
        assertFalse(welcomePanel.isManaged());
        assertTrue(robot.lookup("#identityHeader").query().isVisible());
        assertTrue(robot.lookup("#identityHeader").query().isManaged());
        assertEquals(List.of("list"), receivedInputs);
        assertEquals(2, robot.lookup("#dialogContainer").queryAs(VBox.class).getChildren().size());
    }

    @Test
    void commands_keyboardActivation_preservesDraftAndReturnsFocus(FxRobot robot) throws InterruptedException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        Button commandsButton = robot.lookup("#commandsButton").queryAs(Button.class);
        String draft = "todo Read a book";
        robot.interact(() -> {
            userInput.setText(draft);
            commandsButton.requestFocus();
            commandsButton.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, " ", " ", KeyCode.SPACE,
                    false, false, false, false));
            commandsButton.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, " ", " ", KeyCode.SPACE,
                    false, false, false, false));
        });

        Stage guideStage = findGuideStage(robot);
        assertTrue(guideStage.isShowing());
        assertSame(stage, guideStage.getOwner());
        assertEquals(Modality.WINDOW_MODAL, guideStage.getModality());
        assertTrue(guideStage.isResizable());
        assertEquals(draft, userInput.getText());
        assertTrue(receivedInputs.isEmpty());

        robot.interact(() -> guideStage.getScene().getRoot().fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, false)));
        awaitLayoutPulse(robot, stage);

        assertFalse(guideStage.isShowing());
        assertEquals(draft, userInput.getText());
        assertTrue(userInput.isFocused());
        assertTrue(receivedInputs.isEmpty());
    }

    @Test
    void commands_activeError_preservesCorrectionAndReusesGuideWindow(FxRobot robot)
            throws InterruptedException {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox errorPanel = robot.lookup("#errorPanel").queryAs(VBox.class);
        VBox welcomePanel = robot.lookup("#welcomePanel").queryAs(VBox.class);
        Button commandsButton = robot.lookup("#commandsButton").queryAs(Button.class);
        ScrollPane conversation = robot.lookup("#scrollPane").queryAs(ScrollPane.class);
        robot.interact(() -> {
            userInput.setText("dance");
            userInput.fireEvent(new ActionEvent());
        });
        awaitLayoutPulse(robot, stage);
        AtomicReference<Double> initialPosition = new AtomicReference<>();
        robot.interact(() -> initialPosition.set(conversation.getVvalue()));
        String errorExplanation = robot.lookup("#errorExplanation").queryAs(Label.class).getText();
        robot.interact(commandsButton::fire);
        Stage firstGuide = findGuideStage(robot);
        closeGuide(robot, firstGuide);
        robot.interact(commandsButton::fire);
        Stage reopenedGuide = findGuideStage(robot);
        assertSame(firstGuide, reopenedGuide);
        closeGuide(robot, reopenedGuide);

        assertFalse(welcomePanel.isVisible());
        assertFalse(welcomePanel.isManaged());
        assertTrue(errorPanel.isVisible());
        assertEquals(errorExplanation, robot.lookup("#errorExplanation").queryAs(Label.class).getText());
        assertEquals("dance", userInput.getText());
        robot.interact(() -> assertEquals(initialPosition.get(), conversation.getVvalue(), 0.0001));
        assertEquals(List.of("dance"), receivedInputs);
        assertEquals(2, robot.lookup("#dialogContainer").queryAs(VBox.class).getChildren().size());
    }

    @Test
    void commands_smallWindow_wrapsAndScrollsToLastCommand(FxRobot robot)
            throws InterruptedException, IOException {
        Button commandsButton = robot.lookup("#commandsButton").queryAs(Button.class);
        robot.interact(commandsButton::fire);
        Stage guideStage = findGuideStage(robot);
        ScrollPane scroll = (ScrollPane) guideStage.getScene().lookup("#commandsScroll");
        VBox guide = (VBox) guideStage.getScene().lookup("#commandsGuide");
        robot.interact(() -> {
            guideStage.setWidth(360);
            guideStage.setHeight(360);
        });
        awaitLayoutPulse(robot, guideStage);

        robot.interact(() -> {
            assertTrue(scroll.getViewportBounds().getHeight() > 0);
            assertTrue(guide.getHeight() > scroll.getViewportBounds().getHeight());
            assertTrue(guide.getWidth() <= scroll.getViewportBounds().getWidth() + 1);
            scroll.setVvalue(scroll.getVmax());
        });
        awaitLayoutPulse(robot, guideStage);

        robot.interact(() -> {
            Node viewport = scroll.lookup(".viewport");
            assertNotNull(viewport);
            Bounds viewportBounds = viewport.localToScene(viewport.getLayoutBounds());
            Label lastCommand = guide.lookupAll(".guide-example").stream()
                    .map(Label.class::cast)
                    .filter(label -> label.getText().equals("bye"))
                    .findFirst()
                    .orElseThrow();
            Bounds commandBounds = lastCommand.localToScene(lastCommand.getLayoutBounds());
            assertTrue(commandBounds.getMinY() >= viewportBounds.getMinY() - 1);
            assertTrue(commandBounds.getMaxY() <= viewportBounds.getMaxY() + 1);
            Button closeButton = (Button) guideStage.getScene().lookup("#closeCommandsButton");
            Bounds buttonBounds = closeButton.localToScene(closeButton.getLayoutBounds());
            assertTrue(buttonBounds.getMaxY() <= guideStage.getScene().getHeight() + 1);
        });
        saveScreenshot(robot, guideStage, Path.of("build", "reports", "gui", "commands-small-window.png"));
        closeGuide(robot, guideStage);
    }

    /**
     * Finds the owned command-reference window on the JavaFX application thread.
     */
    private Stage findGuideStage(FxRobot robot) {
        AtomicReference<Stage> guide = new AtomicReference<>();
        robot.interact(() -> guide.set(Window.getWindows().stream()
                .filter(window -> window.getScene() != null
                        && window.getScene().lookup("#commandsDialog") != null)
                .map(Stage.class::cast)
                .findFirst()
                .orElseThrow()));
        return guide.get();
    }

    /**
     * Activates the normal Close action without controlling the system mouse pointer.
     */
    private void closeGuide(FxRobot robot, Stage guide) {
        robot.interact(() -> ((Button) guide.getScene().lookup("#closeCommandsButton")).fire());
    }

    /**
     * Waits for a layout pulse after a window resize or a scroll-position change.
     */
    private void awaitLayoutPulse(FxRobot robot, Stage target) throws InterruptedException {
        CountDownLatch layoutComplete = new CountDownLatch(1);
        Runnable listener = layoutComplete::countDown;
        robot.interact(() -> {
            target.getScene().addPostLayoutPulseListener(listener);
            Platform.requestNextPulse();
        });
        try {
            assertTrue(layoutComplete.await(5, TimeUnit.SECONDS), "Timed out waiting for JavaFX layout");
        } finally {
            robot.interact(() -> target.getScene().removePostLayoutPulseListener(listener));
        }
    }

    /**
     * Saves a scene snapshot for visual review without capturing the system desktop.
     */
    private void saveScreenshot(FxRobot robot, Stage targetStage, Path target) throws IOException {
        int width = (int) Math.ceil(targetStage.getScene().getWidth());
        int height = (int) Math.ceil(targetStage.getScene().getHeight());
        WritableImage snapshot = new WritableImage(width, height);
        robot.interact(() -> targetStage.getScene().getRoot().snapshot(null, snapshot));
        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), pixels, 0, width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        Files.createDirectories(target.getParent());
        ImageIO.write(image, "png", target.toFile());
    }
}
