package athena.gui;

import athena.Athena;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A GUI for Athena using FXML.
 */
public class Main extends Application {

    private Athena athena = new Athena();

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane anchorPane = fxmlLoader.load();
            Scene scene = new Scene(anchorPane);
            stage.setScene(scene);
            stage.setTitle("Athena");
            stage.setResizable(false);
            stage.setMinHeight(600.0);
            stage.setMinWidth(400.0);
            fxmlLoader.<MainWindow>getController().setAthena(athena);  // inject the Athena instance
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
