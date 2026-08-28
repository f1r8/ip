package athena.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Ui {
    private final PrintStream outputStream;
    private final Scanner scanner;

    public Ui(InputStream inputStream, PrintStream outputStream) {
        this.outputStream = outputStream;
        this.scanner = new Scanner(inputStream);
    }

    public void println(String output) {
        outputStream.println(output);
    }

    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    public String nextLine() {
        return scanner.nextLine();
    }
}
