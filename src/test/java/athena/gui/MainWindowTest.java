package athena.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import athena.exception.AthenaException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
class MainWindowTest {
    private final List<String> receivedInputs = new ArrayList<>();

    private Stage stage;

    @Start
    void start(Stage stage) throws IOException {
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
        assertNotNull(MainWindow.class.getResource("/images/DaAthena.jpg"));
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
    void errorAndExit_invalidThenBye_errorDisplayedBeforeWindowCloses(FxRobot robot) {
        TextField userInput = robot.lookup("#userInput").queryAs(TextField.class);
        VBox dialogContainer = robot.lookup("#dialogContainer").queryAs(VBox.class);

        robot.interact(() -> userInput.setText("dance"));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertTrue(stage.isShowing());
        assertDialog(robot, dialogContainer, 1, "Unknown command", Pos.TOP_LEFT);

        robot.interact(() -> userInput.setText("bye"));
        robot.interact(() -> userInput.fireEvent(new ActionEvent()));

        assertFalse(stage.isShowing());
        assertEquals(List.of("dance", "bye"), receivedInputs);
    }

    private String getResponse(String input) {
        receivedInputs.add(input);
        if (input.equals("bye")) {
            throw new AthenaException("Exiting...");
        }
        if (input.equals("dance")) {
            return "Unknown command";
        }
        return "Response: " + input;
    }

    private void assertDialog(FxRobot robot, VBox dialogContainer, int index, String expectedText,
            Pos expectedAlignment) {
        DialogBox dialogBox = assertInstanceOf(DialogBox.class, dialogContainer.getChildren().get(index));

        assertEquals(expectedAlignment, dialogBox.getAlignment());
        boolean isAthenaDialog = expectedAlignment == Pos.TOP_LEFT;
        assertEquals(isAthenaDialog ? 2 : 1, dialogBox.getChildren().size());
        assertInstanceOf(StackPane.class, dialogBox.getChildren().get(isAthenaDialog ? 1 : 0));

        if (isAthenaDialog) {
            ImageView imageView = assertInstanceOf(ImageView.class, dialogBox.getChildren().get(0));
            assertNotNull(imageView.getImage());
        } else {
            assertFalse(dialogBox.getChildren().stream().anyMatch(ImageView.class::isInstance));
        }

        StackPane bubbleContainer = dialogBox.getChildren().stream()
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .findFirst()
                .orElseThrow();
        Label label = bubbleContainer.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .findFirst()
                .orElseThrow();
        Pane tail = assertInstanceOf(Pane.class, bubbleContainer.getChildren().stream()
                .filter(node -> "tail".equals(node.getId()))
                .findFirst()
                .orElseThrow());
        Region tailFill = assertInstanceOf(Region.class, tail.getChildren().get(0));
        SVGPath tailOutline = assertInstanceOf(SVGPath.class, tail.getChildren().get(1));

        robot.interact(dialogBox::applyCss);
        Insets labelMargin = StackPane.getMargin(label);

        assertEquals(expectedText, label.getText());
        assertFalse(label.getBackground().getFills().isEmpty());
        assertEquals(isAthenaDialog ? Pos.BOTTOM_LEFT : Pos.BOTTOM_RIGHT, bubbleContainer.getAlignment());
        assertEquals(isAthenaDialog ? 11.0 : 47.0, labelMargin.getLeft());
        assertEquals(isAthenaDialog ? 47.0 : 11.0, labelMargin.getRight());
        assertTrue(tailFill.getStyleClass().contains("bubble"));
        assertEquals(label.getBackground().getFills().get(0).getFill(),
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
