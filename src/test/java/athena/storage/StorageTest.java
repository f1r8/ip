package athena.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import athena.exception.AthenaException;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;

/**
 * Tests file creation, reading, writing, and task restoration.
 */
class StorageTest {
    @Test
    void write_newAndExistingFile_contentAppended(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("nested").resolve("athena.txt");
        Storage storage = new Storage(path.toString());

        storage.write("first");
        storage.write(" second");

        assertEquals("first second", Files.readString(path));
    }

    @Test
    void write_pathIsDirectory_exceptionThrown(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.toString());

        AthenaException exception = assertThrows(AthenaException.class, () ->
                storage.write("content"));

        assertEquals("Unable to append to file", exception.getMessage());
    }

    @Test
    void ensureFileExists_nestedPath_directoriesAndFileCreated(@TempDir Path tempDir) {
        Path path = tempDir.resolve("nested").resolve("data").resolve("athena.txt");
        Storage storage = new Storage(path.toString());

        storage.ensureFileExists();

        assertTrue(Files.isRegularFile(path));
    }

    @Test
    void ensureFileExists_parentIsFile_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path parentFile = Files.createFile(tempDir.resolve("parent.txt"));
        Storage storage = new Storage(parentFile.resolve("athena.txt").toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::ensureFileExists);

        assertEquals("Fatal Error. Data File cannot be created.", exception.getMessage());
    }

    @Test
    void overwrite_existingContent_contentReplaced(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "old content");
        Storage storage = new Storage(path.toString());

        storage.overwrite("new content");

        assertEquals("new content", Files.readString(path));
    }

    @Test
    void overwrite_pathIsDirectory_exceptionThrown(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.toString());

        AthenaException exception = assertThrows(AthenaException.class, () ->
                storage.overwrite("content"));

        assertEquals("Something went wrong overwriting the file", exception.getMessage());
    }

    @Test
    void read_existingFile_contentReturned(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "saved content");
        Storage storage = new Storage(path.toString());

        assertEquals("saved content", storage.read());
    }

    @Test
    void read_missingFile_emptyStringReturned(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.resolve("missing.txt").toString());

        assertEquals("", storage.read());
    }

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
    void writeItems_fileDoesNotExist_createsFile(@TempDir Path tempDir) throws IOException {
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
        assertEquals(tasks.getFirst().getSaveString() + Storage.SAVE_NEWLINE, Files.readString(path));

        storage.writeItems(new ArrayList<>());
        assertEquals("", Files.readString(path));
    }

    @Test
    void areItemsLoaded_missingFile_returnsFalseAndLeavesListEmpty(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.resolve("missing.txt").toString());
        ArrayList<Task> tasks = new ArrayList<>();

        assertFalse(storage.areItemsLoaded(tasks));
        assertTrue(tasks.isEmpty());
    }

    @Test
    void areItemsLoaded_allTaskTypes_tasksRestored(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String savedTasks = "T | 0 | Read book" + Storage.SAVE_NEWLINE
                + "D | 1 | Submit report | 2026-12-31T23:59" + Storage.SAVE_NEWLINE
                + "E | 0 | Team meeting | 2026-12-30T14:00 | 2026-12-30T15:00"
                + Storage.SAVE_NEWLINE;
        Files.writeString(path, savedTasks);
        Storage storage = new Storage(path.toString());
        ArrayList<Task> tasks = new ArrayList<>();

        assertTrue(storage.areItemsLoaded(tasks));

        assertEquals(3, tasks.size());
        assertInstanceOf(Todo.class, tasks.get(0));
        assertEquals("[T][ ] Read book", tasks.get(0).toString());
        assertInstanceOf(Deadline.class, tasks.get(1));
        assertEquals("[D][X] Submit report (by: Dec 31, 2026, 23:59)", tasks.get(1).toString());
        assertInstanceOf(Event.class, tasks.get(2));
        assertEquals("[E][ ] Team meeting (from: Dec 30, 2026, 14:00, "
                + "to: Dec 30, 2026, 15:00)", tasks.get(2).toString());
    }

    @Test
    void areItemsLoaded_corruptedLine_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "not a valid saved task");
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, () ->
                storage.areItemsLoaded(new ArrayList<>()));

        assertEquals("Storage File Corrupted by this line: not a valid saved task",
                exception.getMessage());
    }

    @Test
    void areItemsLoaded_invalidStatus_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "T | X | Read book");
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, () ->
                storage.areItemsLoaded(new ArrayList<>()));

        assertEquals("Error converting save string to num: X", exception.getMessage());
    }
}
