package athena.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Represents a dialog box containing a message and an optional display picture.
 */
public class DialogBox extends HBox {
    @FXML
    private StackPane bubbleContainer;
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;
    @FXML
    private Pane tail;
    @FXML
    private Region tailFill;

    private DialogBox(String text) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
    }

    /**
     * Displays the supplied image using a centered square crop.
     */
    private void setDisplayPicture(Image image) {
        displayPicture.setImage(image);

        double size = Math.min(image.getWidth(), image.getHeight());
        double x = (image.getWidth() - size) / 2;
        double y = (image.getHeight() - size) / 2;
        displayPicture.setViewport(new Rectangle2D(x, y, size, size));
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        ObservableList<Node> observableNodes = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(observableNodes);
        getChildren().setAll(observableNodes);
        setAlignment(Pos.TOP_LEFT);
        bubbleContainer.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setMargin(dialog, new Insets(0, 47, 0, 11));
        tail.setScaleX(-1);
        dialog.getStyleClass().add("reply-label");
        tailFill.getStyleClass().add("reply-label");
    }

    /**
     * Returns a right-aligned dialog box for a user message.
     */
    public static DialogBox getUserDialog(String text) {
        var dialogBox = new DialogBox(text);
        dialogBox.getChildren().remove(dialogBox.displayPicture);
        return dialogBox;
    }

    /**
     * Returns a left-aligned dialog box for an Athena message.
     */
    public static DialogBox getAthenaDialog(String text, Image img) {
        var dialogBox = new DialogBox(text);
        dialogBox.setDisplayPicture(img);
        dialogBox.flip();
        return dialogBox;
    }
}
