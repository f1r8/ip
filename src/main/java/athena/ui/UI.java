package athena.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Ui class of the Athena application.
 */
public class Ui {
    private final InputStream inputStream;
    private final PrintStream outputStream;
    private final Scanner sc;

    /**
     * Constructs a Ui object.
     */
    public Ui(InputStream inputStream, PrintStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.sc = new Scanner(inputStream);
    }

    /**
     * Prints the output to the specified outputStream.
     *
     * @param output String to be printed.
     */
    public void println(String output) {
        outputStream.println(output);
    }

    /**
     * Checks if the inputStream has another line.
     *
     * @return true if there is another line, false otherwise.
     */
    public boolean hasNextLine() {
        return sc.hasNextLine();
    }

    /**
     * Gets the next line of the inputStream.
     *
     * @return The next line of the inputStream.
     */
    public String nextLine() {
        return sc.nextLine();
    }
}
