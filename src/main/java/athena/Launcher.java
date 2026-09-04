package athena;

import javafx.application.Application;

/**
 * A launcher class to work around classpath issues when running JavaFX
 * directly from the Main class (see JavaFX tutorial Part 1).
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
