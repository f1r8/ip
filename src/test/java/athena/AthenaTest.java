package athena;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the application entry point in an isolated working directory.
 */
class AthenaTest {

    @Test
    void main_byeInput_applicationStartsAndExits(@TempDir Path tempDir) throws Exception {
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String mainClassPath = Path.of(Athena.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toString();
        Process process = new ProcessBuilder(javaExecutable, "-cp", mainClassPath, Athena.class.getName())
                .directory(tempDir.toFile())
                .redirectErrorStream(true)
                .start();

        try {
            try (BufferedWriter input = process.outputWriter(StandardCharsets.UTF_8)) {
                input.write("bye");
                input.newLine();
            }

            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Athena should exit after the bye command");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            assertEquals(0, process.exitValue());
            assertTrue(output.contains("Hello, Your Majesty! I'm Athena."));
            assertTrue(output.contains("Farewell, Your Majesty. I hope to serve you again soon!"));
        } finally {
            process.destroyForcibly();
        }
    }
}
