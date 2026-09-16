package athena.gui;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Represents a dialog box containing a message and an optional display picture.
 */
public class DialogBox extends HBox {
    private static final DateTimeFormatter MESSAGE_TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter MESSAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH);

    @FXML
    private VBox messageContent;
    @FXML
    private Label timestamp;
    @FXML
    private StackPane bubbleContainer;
    @FXML
    private VBox bubbleContent;
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
            throw new IllegalStateException("Failed to load dialog box layout", e);
        }

        dialog.setText(text);
        LocalDateTime createdAt = LocalDateTime.now();
        timestamp.setText(createdAt.format(MESSAGE_TIME));
        String fullTimestamp = createdAt.format(MESSAGE_DATE_TIME);
        timestamp.setTooltip(new Tooltip(fullTimestamp));
        timestamp.setAccessibleText("Message time: " + fullTimestamp);
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
        messageContent.setAlignment(Pos.TOP_LEFT);
        bubbleContainer.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setMargin(bubbleContent, new Insets(0, 47, 0, 11));
        tail.setScaleX(-1);
        bubbleContent.getStyleClass().add("reply-label");
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

    /**
     * Returns an Athena reply with aligned task details when the command supplies them.
     */
    public static DialogBox getAthenaDialog(CommandResponse response, Image img) {
        DialogBox dialogBox = getAthenaDialog(response.message().strip(), img);
        if (!response.tasks().isEmpty()) {
            dialogBox.bubbleContent.setMaxWidth(Double.MAX_VALUE);
            dialogBox.bubbleContent.getChildren().add(dialogBox.createTaskRows(response));
        }
        return dialogBox;
    }

    /**
     * Aligns numbers, task details, and completion states without constraining wrapped descriptions.
     */
    private GridPane createTaskRows(CommandResponse response) {
        GridPane rows = new GridPane();
        rows.getStyleClass().add("task-rows");
        rows.setHgap(6);
        rows.setVgap(8);
        rows.setMinWidth(0);

        ColumnConstraints numberColumn = new ColumnConstraints();
        ColumnConstraints detailsColumn = new ColumnConstraints();
        detailsColumn.setHgrow(Priority.ALWAYS);
        detailsColumn.setMinWidth(0);
        ColumnConstraints statusColumn = new ColumnConstraints();
        rows.getColumnConstraints().addAll(numberColumn, detailsColumn, statusColumn);

        for (int i = 0; i < response.tasks().size(); i++) {
            TaskView task = response.tasks().get(i);
            Label number = createTaskLabel(task.number() == 0 ? "" : task.number() + ".", "task-number");
            Label status = createTaskLabel(task.isDone() ? "Done" : "To do", "task-status");
            number.setMinWidth(Region.USE_PREF_SIZE);
            status.setMinWidth(Region.USE_PREF_SIZE);
            if (task.isDone()) {
                status.getStyleClass().add("task-done");
            }
            rows.add(number, 0, i);
            rows.add(createTaskDetails(task), 1, i);
            rows.add(status, 2, i);
            GridPane.setValignment(number, VPos.TOP);
            GridPane.setValignment(status, VPos.TOP);
        }
        return rows;
    }

    /**
     * Places dates and tags below a task name so each field remains readable in a narrow reply.
     */
    private VBox createTaskDetails(TaskView task) {
        VBox details = new VBox(2);
        details.setMinWidth(0);
        details.getChildren().add(createTaskLabel(task.description(), "task-description"));
        details.getChildren().add(createTaskLabel(task.type(), "task-type"));
        if (!task.schedule().isEmpty()) {
            details.getChildren().add(createTaskLabel(task.schedule(), "task-schedule"));
        }
        if (!task.tags().isEmpty()) {
            details.getChildren().add(createTaskLabel(String.join(" ", task.tags()), "task-tags"));
        }
        return details;
    }

    private Label createTaskLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
