package athena.gui;

import java.io.IOException;

import athena.Athena;
import athena.exception.AthenaException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * A GUI for Athena using FXML.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Athena athena;
        try {
            athena = new Athena();
        } catch (AthenaException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Athena could not open the data file");
            alert.setHeaderText("Your saved file has not been changed.");
            alert.setContentText(e.getMessage() + "\nRepair the file or its permissions and restart Athena.");
            alert.showAndWait();
            Platform.exit();
            return;
        }
        Font.loadFont(getClass().getResourceAsStream("/fonts/static/Inter_18pt-Regular.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fonts/static/Inter_18pt-Bold.ttf"), 16);

        AnchorPane anchorPane;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            anchorPane = fxmlLoader.load();
            fxmlLoader.<MainWindow>getController().setCommandResponder(athena);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load main window layout", e);
        }

        Scene scene = new Scene(anchorPane);
        stage.setScene(scene);
        stage.setTitle("Athena");
        stage.setResizable(true);
        stage.setMinHeight(400.0);
        stage.setMinWidth(400.0);
        stage.show();
    }
}
