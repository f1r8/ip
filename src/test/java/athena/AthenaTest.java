package athena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests real startup, persistence, and response coordination without opening JavaFX.
 */
class AthenaTest {
    @TempDir
    private Path tempDir;

    @Test
    void main_endOfInput_exitsWithoutCreatingSavedData() throws Exception {
        String output = runApplication(Athena.class, "", List.of());
        assertTrue(output.contains("File not found at: ./data/athena.txt"), output);
        assertTrue(output.contains("Hello, Your Majesty! I'm Athena."), output);
        assertFalse(output.contains("Farewell"), output);
        assertFalse(Files.exists(tempDir.resolve("data/athena.txt")));
    }

    @Test
    void main_bye_stopsBeforeLaterCommands() throws Exception {
        String output = runApplication(Athena.class, "todo First\nbye\ntodo Ignored\n", List.of());
        assertTrue(output.contains("Farewell, Your Majesty."), output);
        assertFalse(output.contains("Ignored"), output);
        assertEquals("T | 0 | First" + System.lineSeparator(),
                Files.readString(tempDir.resolve("data/athena.txt")));
    }

    @Test
    void main_corruptStorage_reportsRecoveryAndPreservesFile() throws Exception {
        Path data = Files.createDirectories(tempDir.resolve("data")).resolve("athena.txt");
        Files.writeString(data, "broken saved record");
        String output = runApplication(Athena.class, "todo Ignored\n", List.of());
        assertTrue(output.contains("Cannot open Athena: Storage File Corrupted by this line: broken saved record"),
                output);
        assertTrue(output.contains("Your saved file has not been changed. Repair it and restart Athena."), output);
        assertFalse(output.contains("Hello"), output);
        assertEquals("broken saved record", Files.readString(data));
    }

    @Test
    void getResponse_successErrorAndExit_clearsPreviousMessagesAndRows() throws Exception {
        String output = runApplication(AthenaProbe.class, "", List.of("todo First", "nonsense", "bye"));
        String[] responses = output.split("RESULT ");
        assertEquals(4, responses.length, output);
        assertTrue(responses[1].startsWith("exit=false error=false"), output);
        assertTrue(responses[1].contains("description=First"), output);
        assertTrue(responses[2].startsWith("exit=false error=true"), output);
        assertTrue(responses[2].contains("ROWS []"), output);
        assertFalse(responses[2].contains("First"), output);
        assertTrue(responses[3].startsWith("exit=true error=false"), output);
        assertTrue(responses[3].contains("ROWS []"), output);
        assertTrue(responses[3].contains("Farewell"), output);
    }

    @Test
    void getResponse_savedTasks_loadsExistingState() throws Exception {
        Path data = Files.createDirectories(tempDir.resolve("data")).resolve("athena.txt");
        Files.writeString(data, "T | 1 | Existing | #Work");
        String output = runApplication(AthenaProbe.class, "", List.of("list"));
        assertTrue(output.contains("number=1, description=Existing, isDone=true"), output);
        assertTrue(output.contains("tags=[#Work]"), output);
        assertEquals("T | 1 | Existing | #Work", Files.readString(data));
    }

    /**
     * Runs with a temporary working directory so tests never read or overwrite personal tasks.
     */
    private String runApplication(Class<?> entryPoint, String input, List<String> arguments) throws Exception {
        String classPath = Path.of(Athena.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(AthenaProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-ea", "-cp", classPath, entryPoint.getName()));
        command.addAll(arguments);
        Path outputPath = tempDir.resolve("process-output.txt");
        Process process = new ProcessBuilder(command).directory(tempDir.toFile()).redirectErrorStream(true)
                .redirectOutput(outputPath.toFile()).start();
        try {
            try (var stdin = process.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.UTF_8));
            }
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Athena did not exit within 20 seconds");
            String output = Files.readString(outputPath);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }
}
