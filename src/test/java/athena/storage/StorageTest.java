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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import athena.exception.AthenaException;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Tag;
import athena.task.Task;
import athena.task.Todo;

/**
 * Tests file creation, reading, writing, and task restoration.
 */
class StorageTest {
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

        assertEquals("Cannot create the data file. Check its location and permissions.", exception.getMessage());
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

        assertEquals("Cannot save tasks. No changes were applied. "
                + "Check the data file location, permissions, and free disk space, then try again.",
                exception.getMessage());
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
    void read_pathIsDirectory_exceptionThrown(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::read);

        assertEquals("Cannot read the data file. Check its location, permissions, and encoding.",
                exception.getMessage());
    }

    @Test
    void saveTasks_listOfTasks_writtenToStorage(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        Storage storage = new Storage(path.toString());
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Todo("end the git commit subject line with a period"));
        tasks.add(new Deadline("Do not separate subject from body with a blank line", "2001-09-11 0846"));
        tasks.add(new Event("Ensure each line of the body exceeds 72 characters",
                "2001-09-11 0846", "2026-08-26 2154"));
        storage.saveTasks(tasks);

        String expected = "";
        for (Task task : tasks) {
            expected = expected + task.getSaveString() + Storage.SAVE_NEWLINE;
        }
        assertEquals(expected, Files.readString(path));
    }

    @Test
    void saveTasks_fileDoesNotExist_createsFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        Storage storage = new Storage(path.toString());
        assertFalse(Files.exists(path));

        storage.saveTasks(new ArrayList<>());
        assertTrue(Files.exists(path));
    }

    @Test
    void saveTasks_fileHasContent_overwritesFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("duke.txt");
        Storage storage = new Storage(path.toString());
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new Todo("Do not use bullet points in git commit body"));
        storage.saveTasks(tasks);
        assertEquals(tasks.getFirst().getSaveString() + Storage.SAVE_NEWLINE, Files.readString(path));

        storage.saveTasks(new ArrayList<>());
        assertEquals("", Files.readString(path));
    }

    @Test
    void saveTasks_taggedAndUntaggedTasks_onlyTaggedRecordHasTagField(@TempDir Path tempDir)
            throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Storage storage = new Storage(path.toString());
        Todo untaggedTodo = new Todo("Read book");
        Todo taggedTodo = new Todo("Write report");
        taggedTodo.addTag(new Tag("#Work"));

        storage.saveTasks(List.of(untaggedTodo, taggedTodo));

        assertEquals("T | 0 | Read book" + Storage.SAVE_NEWLINE
                + "T | 0 | Write report | #Work" + Storage.SAVE_NEWLINE,
                Files.readString(path));
    }

    @Test
    void loadTasks_missingFile_returnsFalseAndLeavesListEmpty(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.resolve("missing.txt").toString());
        List<Task> tasks = storage.loadTasks();

        assertFalse(storage.wasLoadSuccessful());
        assertTrue(tasks.isEmpty());
    }

    @Test
    void loadTasks_allTaskTypes_tasksRestored(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String savedTasks = "T | 0 | Read book" + Storage.SAVE_NEWLINE
                + "D | 1 | Submit report | 2026-12-31T23:59" + Storage.SAVE_NEWLINE
                + "E | 0 | Team meeting | 2026-12-30T14:00 | 2026-12-30T15:00"
                + Storage.SAVE_NEWLINE;
        Files.writeString(path, savedTasks);
        Storage storage = new Storage(path.toString());
        List<Task> tasks = storage.loadTasks();

        assertTrue(storage.wasLoadSuccessful());

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
    void loadTasks_legacyAndTaggedRecords_bothShapesRestored(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String savedTasks = "T | 0 | Legacy todo" + Storage.SAVE_NEWLINE
                + "D | 1 | Tagged deadline | 2026-12-31T23:59 | #zeta,#Alpha"
                + Storage.SAVE_NEWLINE;
        Files.writeString(path, savedTasks);
        Storage storage = new Storage(path.toString());

        List<Task> tasks = storage.loadTasks();

        assertEquals(savedTasks, Files.readString(path));
        assertEquals("[T][ ] Legacy todo", tasks.get(0).toString());
        assertEquals("T | 0 | Legacy todo", tasks.get(0).getSaveString());
        assertEquals("[D][X] Tagged deadline (by: Dec 31, 2026, 23:59) #Alpha #zeta",
                tasks.get(1).toString());
        assertEquals("D | 1 | Tagged deadline | 2026-12-31T23:59 | #Alpha,#zeta",
                tasks.get(1).getSaveString());

        storage.saveTasks(tasks);

        assertEquals("T | 0 | Legacy todo" + Storage.SAVE_NEWLINE
                + "D | 1 | Tagged deadline | 2026-12-31T23:59 | #Alpha,#zeta"
                + Storage.SAVE_NEWLINE, Files.readString(path));
    }

    @Test
    void loadTasks_malformedTagField_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String malformedLine = "T | 0 | Read book | #Work,,#Urgent";
        Files.writeString(path, malformedLine);
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Storage File Corrupted by this line: " + malformedLine, exception.getMessage());
    }

    @Test
    void loadTasks_invalidSavedTag_exceptionIdentifiesCorruptedLine(@TempDir Path tempDir)
            throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String malformedLine = "T | 0 | Read book | #valid,#bad!";
        Files.writeString(path, malformedLine);
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Storage File Corrupted by this line: " + malformedLine, exception.getMessage());
    }

    @Test
    void loadTasks_duplicateSavedTags_exceptionIdentifiesCorruptedLine(@TempDir Path tempDir)
            throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String malformedLine = "T | 0 | Read book | #fun,#FUN";
        Files.writeString(path, malformedLine);
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Storage File Corrupted by this line: " + malformedLine, exception.getMessage());
    }

    @Test
    void loadTasks_emptyTagField_exceptionIdentifiesCorruptedLine(@TempDir Path tempDir)
            throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String malformedLine = "T | 0 | Read book | ";
        Files.writeString(path, malformedLine);
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Storage File Corrupted by this line: " + malformedLine, exception.getMessage());
    }

    @Test
    void loadTasks_corruptedLine_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "not a valid saved task");
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Storage File Corrupted by this line: not a valid saved task",
                exception.getMessage());
    }

    @Test
    void loadTasks_unknownTaskType_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "X | 0 | Read book");
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Storage File Corrupted by this line: X | 0 | Read book",
                exception.getMessage());
    }

    @Test
    void loadTasks_invalidStatus_exceptionThrown(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "T | X | Read book");
        Storage storage = new Storage(path.toString());

        AthenaException exception = assertThrows(AthenaException.class, storage::loadTasks);

        assertEquals("Error converting save string to num: X", exception.getMessage());
    }

    @Test
    void loadTasks_invalidDatesAndDuplicateTasks_rejectedWithoutChangingFile(@TempDir Path tempDir)
            throws IOException {
        Path path = tempDir.resolve("athena.txt");
        List<String> contents = List.of(
                "D | 0 | Report | 2026-02-30T12:00",
                "E | 0 | Meeting | 2026-01-01T13:00 | 2026-01-01T12:00",
                "E | 0 | Meeting | 2026-01-01T12:00 | 2026-01-01T12:00",
                "\n", "T | 0 |    ", "T | 0 | Read book\nT | 1 | READ BOOK",
                "T | 0 | Read book\n\nT | 0 | Second task");
        for (String content : contents) {
            Files.writeString(path, content);
            Storage storage = new Storage(path.toString());
            assertThrows(AthenaException.class, storage::loadTasks, content);
            assertFalse(storage.wasLoadSuccessful());
            assertEquals(content, Files.readString(path));
        }
    }

    @Test
    void loadTasks_mixedLineEndings_restoresEveryTask(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        Files.writeString(path, "T | 0 | First\nT | 1 | Second\r\nT | 0 | Third\r");

        assertEquals(3, new Storage(path.toString()).loadTasks().size());
    }

    @Test
    void read_directoryOrInvalidEncoding_reportsError(@TempDir Path tempDir) throws IOException {
        assertThrows(AthenaException.class, () -> new Storage(tempDir.toString()).loadTasks());
        Path path = tempDir.resolve("athena.txt");
        byte[] invalidUtf8 = {(byte) 0xc3, (byte) 0x28};
        Files.write(path, invalidUtf8);
        assertThrows(AthenaException.class, () -> new Storage(path.toString()).loadTasks());
    }

    @Test
    void saveTasks_parentIsFile_preservesExistingContent(@TempDir Path tempDir) throws IOException {
        Path parent = tempDir.resolve("data");
        Files.writeString(parent, "Keep this content");
        Storage storage = new Storage(parent.resolve("athena.txt").toString());

        assertThrows(AthenaException.class, () -> storage.saveTasks(List.of(new Todo("Read book"))));
        assertEquals("Keep this content", Files.readString(parent));
    }

    @Test
    void saveTasks_targetIsDirectory_preservesExistingContent(@TempDir Path tempDir) throws IOException {
        Path target = Files.createDirectory(tempDir.resolve("athena.txt"));
        Path child = target.resolve("keep.txt");
        Files.writeString(child, "Keep this content");

        assertThrows(AthenaException.class, () ->
                new Storage(target.toString()).saveTasks(List.of(new Todo("Read book"))));
        assertEquals("Keep this content", Files.readString(child));
    }

    @Test
    void saveTasks_nestedMissingDirectories_createsReloadableFile(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.resolve("nested/data/athena.txt").toString());
        storage.saveTasks(List.of(new Todo("Read book")));

        assertEquals("[T][ ] Read book", storage.loadTasks().getFirst().toString());
    }

    @Test
    void loadTasks_emptyExistingFile_reportsSuccessfulLoad(@TempDir Path tempDir) throws IOException {
        Path path = Files.createFile(tempDir.resolve("athena.txt"));
        Storage storage = new Storage(path.toString());

        assertTrue(storage.loadTasks().isEmpty());
        assertTrue(storage.wasLoadSuccessful());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void saveTasks_readOnlyFile_preservesDataAndCanRetry(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("athena.txt");
        String original = "T | 0 | Read book";
        Files.writeString(path, original);
        Storage storage = new Storage(path.toString());
        Files.setAttribute(path, "dos:readonly", true);
        try {
            assertThrows(AthenaException.class, () -> storage.saveTasks(List.of(new Todo("New task"))));
            assertEquals(original, Files.readString(path));
        } finally {
            Files.setAttribute(path, "dos:readonly", false);
        }

        storage.saveTasks(List.of(new Todo("New task")));
        assertEquals("[T][ ] New task", storage.loadTasks().getFirst().toString());
    }
}
