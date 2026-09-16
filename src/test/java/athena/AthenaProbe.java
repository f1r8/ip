package athena;

import athena.gui.CommandResponse;

/**
 * Exercises response orchestration in an isolated process with its own data directory.
 */
public class AthenaProbe {
    /**
     * Runs each supplied command and prints its structured result for the parent test.
     */
    public static void main(String[] args) {
        Athena athena = new Athena();
        for (String input : args) {
            CommandResponse response = athena.getResponse(input);
            System.out.println("RESULT exit=" + response.shouldExit() + " error=" + response.isError());
            System.out.print(response.message());
            System.out.println("ROWS " + response.tasks());
        }
    }
}
