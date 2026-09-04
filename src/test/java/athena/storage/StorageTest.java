package athena.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;

class StorageTest {
    @Test
    void writeItems_listOfTasks_addedToStorage(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        Storage storage = new Storage(path.toString());
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Todo("end the git commit subject line with a period"));
        tasks.add(new Deadline("Do not separate subject from body with a blank line", "2001-09-11 0846"));
        tasks.add(new Event("Ensure each line of the body exceeds 72 characters",
                "2001-09-11 0846", "2026-08-26 2154"));
        storage.writeItems(tasks);

        String expected = "";
        for (Task task : tasks) {
            expected = expected + task.getSaveString() + Storage.SAVE_NEWLINE;
        }
        assertEquals(expected, Files.readString(path));
    }

    @Test
    void writeItems_fileDoesntExist_createsFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        Storage storage = new Storage(path.toString());
        assertFalse(Files.exists(path));

        storage.writeItems(new ArrayList<>());
        assertTrue(Files.exists(path));
    }

    @Test
    void writeItems_fileHasContent_overwritesFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        Storage storage = new Storage(path.toString());
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Todo("Do not use bullet points in git commit body"));
        storage.writeItems(tasks);
        assertEquals(tasks.get(0).getSaveString() + Storage.SAVE_NEWLINE, Files.readString(path));

        storage.writeItems(new ArrayList<>());
        assertEquals("", Files.readString(path));
    }
}
