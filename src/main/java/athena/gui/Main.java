package athena.gui;

import java.io.IOException;

import athena.Athena;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * A GUI for Athena using FXML.
 */
public class Main extends Application {

    private Athena athena = new Athena();

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/static/Inter_18pt-Regular.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fonts/static/Inter_18pt-Bold.ttf"), 16);

        AnchorPane anchorPane;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            anchorPane = fxmlLoader.load();
            fxmlLoader.<MainWindow>getController().setCommandResponder(athena);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(anchorPane);
        stage.setScene(scene);
        stage.setTitle("Athena");
        stage.setResizable(true);
        stage.setMinHeight(200.0);
        stage.setMinWidth(400.0);
        stage.show();
    }
}
