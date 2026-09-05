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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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
        assertNotNull(MainWindow.class.getResource("/images/DaUser.png"));
        assertNotNull(MainWindow.class.getResource("/images/DaAthena.png"));
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
        assertDialog(dialogContainer, 0, "todo Read book", Pos.TOP_RIGHT, Label.class, ImageView.class);
        assertDialog(dialogContainer, 1, "Response: todo Read book", Pos.TOP_LEFT, ImageView.class, Label.class);

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
        assertDialog(dialogContainer, 1, "Unknown command", Pos.TOP_LEFT, ImageView.class, Label.class);

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

    private void assertDialog(VBox dialogContainer, int index, String expectedText, Pos expectedAlignment,
            Class<?> firstNodeType, Class<?> secondNodeType) {
        DialogBox dialogBox = assertInstanceOf(DialogBox.class, dialogContainer.getChildren().get(index));

        assertEquals(expectedAlignment, dialogBox.getAlignment());
        assertInstanceOf(firstNodeType, dialogBox.getChildren().get(0));
        assertInstanceOf(secondNodeType, dialogBox.getChildren().get(1));

        Label label = dialogBox.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .findFirst()
                .orElseThrow();
        ImageView imageView = dialogBox.getChildren().stream()
                .filter(ImageView.class::isInstance)
                .map(ImageView.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals(expectedText, label.getText());
        assertNotNull(imageView.getImage());
    }
}
