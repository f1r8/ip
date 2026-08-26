package athena.ui;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class UI {
    private final InputStream inputStream;
    private final PrintStream outputStream;
    private final Scanner sc;

    public UI(InputStream inputStream, PrintStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.sc = new Scanner(inputStream);
    }

    public void println(String output) {
        outputStream.println(output);
    }

    public boolean hasNextLine() {
        return sc.hasNextLine();
    }

    public String nextLine() {
        return sc.nextLine();
    }
}
