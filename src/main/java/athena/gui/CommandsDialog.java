package athena.gui;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;

/**
 * Presents a scrollable command reference without submitting commands or changing a draft.
 */
class CommandsDialog extends Dialog<Void> {
    /**
     * Constructs a resizable command reference owned by the main application window.
     *
     * @param owner The application window to return to when the reference closes.
     */
    CommandsDialog(Window owner) {
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle("Athena commands");
        setResizable(true);

        VBox guide = new VBox(14);
        guide.setId("commandsGuide");
        guide.getStyleClass().add("commands-guide");
        guide.getChildren().add(createLabel("Your commands, Your Majesty.", "guide-heading"));
        guide.getChildren().add(createLabel("Enter a command in Athena, then press Enter or Send.",
                "guide-description"));
        addCommand(guide, "Add a task", "todo Read a book");
        addCommand(guide, "Add a deadline", "deadline Submit report /by 2026-12-31 2359");
        addCommand(guide, "Add an event", "event Lunch /from 2026-12-31 1200 /to 2026-12-31 1300");
        guide.getChildren().add(createLabel("For deadlines and events, use yyyy-MM-dd HHmm: "
                + "a date and four-digit, 24-hour time. Replace the sample dates as needed.", "guide-description"));
        addCommand(guide, "View and search", "list\nfind report\nfindtag #work");
        addCommand(guide, "Update tasks", "mark 1\nunmark 1\ndelete 1\ntag 1 #work\nuntag 1 #work");
        guide.getChildren().add(createLabel("Your Majesty, use list to check task numbers first. "
                + "The update examples assume task 1 exists. Tags begin with # followed by one or more "
                + "ASCII letters (A-Z, a-z), digits (0-9), underscores, or hyphens. "
                + "findtag matches all supplied tags.", "guide-description"));
        addCommand(guide, "Finish your session", "bye");

        ScrollPane guideScroll = new ScrollPane(guide);
        guideScroll.setId("commandsScroll");
        guideScroll.setFitToWidth(true);
        guideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        guideScroll.setMinSize(0, 0);
        guideScroll.setPrefViewportWidth(380);
        guideScroll.setPrefViewportHeight(350);
        guideScroll.setAccessibleText("Command reference. Scroll to read all commands.");

        getDialogPane().setId("commandsDialog");
        getDialogPane().getStylesheets().add(Main.class.getResource("/css/main.css").toExternalForm());
        getDialogPane().setContent(guideScroll);
        getDialogPane().setMinSize(300, 250);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Button closeButton = (Button) getDialogPane().lookupButton(ButtonType.CLOSE);
        closeButton.setId("closeCommandsButton");
        closeButton.setAccessibleText("Close commands and return to your draft");
    }

    /**
     * Adds a command group with a readable heading and wrapping syntax examples.
     */
    private void addCommand(VBox guide, String heading, String examples) {
        VBox group = new VBox(5);
        group.getChildren().addAll(createLabel(heading, "guide-heading"),
                createLabel(examples, "guide-example"));
        guide.getChildren().add(group);
    }

    /**
     * Creates text that wraps to the guide viewport instead of widening its window.
     */
    private Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMinHeight(Label.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
