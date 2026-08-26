package athena.storage;

import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {
    Storage storage;

    @TempDir
    Path tempDir;

    @Test
    void writeItems_listOfTasks_addedToStorage(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        storage = new Storage(path.toString());
        ArrayList<Task> taskList = new ArrayList<>();
        taskList.add(new Todo("end the git commit subject line with a period"));
        taskList.add(new Deadline("Do not separate subject from body with a blank line", "2001-09-11 0846"));
        taskList.add(new Event("Ensure each line of the body exceeds 72 characters", "2001-09-11 0846", "2026-08-26 2154"));
        storage.writeItems(taskList);

        String expected = "";
        for (Task task : taskList) {
            expected = expected + task.toSaveString() + Storage.NEWLINE;
        }
        assertEquals(expected, Files.readString(path));
    }

    @Test
    void writeItem_fileDoesntExist_createsFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        storage = new Storage(path.toString());
        assertFalse(Files.exists(path));

        storage.writeItems(new ArrayList<>());
        assertTrue(Files.exists(path));
    }

    @Test
    void writeItem_fileHasContent_overwritesFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        storage = new Storage(path.toString());
        ArrayList<Task> taskList = new ArrayList<>();
        taskList.add(new Todo("Do not use bullet points in git commit body"));
        storage.writeItems(taskList);
        assertEquals(taskList.get(0).toSaveString() + Storage.NEWLINE, Files.readString(path));

        storage.writeItems(new ArrayList<>());
        assertEquals("", Files.readString(path));
    }
}