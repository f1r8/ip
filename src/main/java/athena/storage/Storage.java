package athena.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import athena.exception.AthenaException;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Tag;
import athena.task.Task;
import athena.task.TaskList;
import athena.task.Todo;

/**
 * Persists Athena tasks in a local text file.
 */
public class Storage {
    /**
     * Separator between fields in a saved task
     */
    public static final String SAVE_SEPARATOR = " | ";

    /**
     * Line separator used between saved tasks
     */
    public static final String SAVE_NEWLINE = System.lineSeparator();

    private static final int SAVE_TYPE_INDEX = 0;
    private static final int SAVE_STATUS_INDEX = 1;
    private static final int SAVE_DESCRIPTION_INDEX = 2;
    private static final int SAVE_DEADLINE_INDEX = 3;
    private static final int SAVE_EVENT_START_INDEX = 3;
    private static final int SAVE_EVENT_END_INDEX = 4;
    private static final int SAVE_TODO_FIELD_COUNT = 3;
    private static final int SAVE_DEADLINE_FIELD_COUNT = 4;
    private static final int SAVE_EVENT_FIELD_COUNT = 5;
    private static final int SAVE_TAG_FIELD_OFFSET = 1;
    private static final String SAVE_TODO_TYPE = "T";
    private static final String SAVE_DEADLINE_TYPE = "D";
    private static final String SAVE_EVENT_TYPE = "E";

    private final String filePath;

    private boolean wasLoadSuccessful;

    /**
     * Constructs storage backed by the file at the specified path.
     *
     * @param path Path of the file on the system used for storing or retrieving data.
     */
    public Storage(String path) {
        this.filePath = path;
    }

    private Path getPath() {
        try {
            return Paths.get(filePath);
        } catch (InvalidPathException e) {
            throw new AthenaException("The data file path is invalid. Check the storage location.");
        }
    }

    /**
     * Ensures that the storage file and its parent directories exist.
     */
    public void ensureFileExists() {
        try {
            Path path = getPath().toAbsolutePath();
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            if (!Files.isRegularFile(path)) {
                throw new IOException("Storage path is not a file");
            }
        } catch (IOException | SecurityException e) {
            throw new AthenaException("Cannot create the data file. Check its location and permissions.");
        }
    }

    /**
     * Overwrites the content in the storage file.
     *
     * @param content Content to be written to storage.
     */
    public void overwrite(String content) {
        Path temporaryFile = null;
        try {
            Path path = getPath().toAbsolutePath();
            Files.createDirectories(path.getParent());
            if (Files.exists(path) && (!Files.isRegularFile(path) || !Files.isWritable(path))) {
                throw new IOException("Storage file is not writable");
            }
            temporaryFile = Files.createTempFile(path.getParent(), ".athena-", ".tmp");
            Files.writeString(temporaryFile, content);
            Files.move(temporaryFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | SecurityException e) {
            throw new AthenaException("Cannot save tasks. No changes were applied. "
                    + "Check the data file location, permissions, and free disk space, then try again.");
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException | SecurityException e) {
                    //An unused temporary file must not turn a completed save into a failed command
                }
            }
        }
    }

    /**
     * Reads the storage file if it exists.
     *
     * @return the content in the file or an empty string if the file does not exist.
     * @throws AthenaException If the storage file exists but cannot be read.
     */
    public String read() {
        Path path = getPath();
        if (Files.notExists(path)) {
            return "";
        }

        try {
            return Files.readString(path);
        } catch (IOException | SecurityException e) {
            throw new AthenaException("Cannot read the data file. Check its location, permissions, and encoding.");
        }
    }

    /**
     * Serializes tasks and writes them to storage.
     *
     * @param tasks Tasks to be written.
     */
    public void saveTasks(List<Task> tasks) {
        assert tasks != null : "tasks cannot be null";
        String content = tasks.stream()
                .map(task -> task.getSaveString() + SAVE_NEWLINE)
                .collect(Collectors.joining());
        overwrite(content);
    }

    /**
     * Loads tasks from storage.
     *
     * @return Tasks read from storage.
     */
    public List<Task> loadTasks() {
        wasLoadSuccessful = false;
        String input = read();
        if (input.isEmpty()) {
            wasLoadSuccessful = Files.exists(getPath());
            return new ArrayList<>();
        }

        TaskList loadedTasks = new TaskList();
        for (String line : input.lines().toList()) {
            try {
                loadedTasks.add(parseTask(line));
            } catch (DateTimeParseException e) {
                throw new AthenaException("Storage File Corrupted by this line: " + line);
            }
        }
        List<Task> tasks = loadedTasks.getTasks();
        wasLoadSuccessful = true;
        return tasks;
    }

    /**
     * Checks if {@link loadTasks} was successful.
     *
     * @return {@code true} if tasks are loaded, {@code false} otherwise.
     */
    public boolean wasLoadSuccessful() {
        return wasLoadSuccessful;
    }

    private static Task parseTask(String line) {
        String[] items = line.split(Pattern.quote(SAVE_SEPARATOR), -1);
        String type = items[SAVE_TYPE_INDEX];

        switch (type) {
            case SAVE_TODO_TYPE:
                if (hasSupportedFieldCount(items, SAVE_TODO_FIELD_COUNT)) {
                    return new Todo(Task.isDoneFromStatus(items[SAVE_STATUS_INDEX]),
                            items[SAVE_DESCRIPTION_INDEX],
                            parseTags(items, SAVE_TODO_FIELD_COUNT));
                }
                break;
            case SAVE_DEADLINE_TYPE:
                if (hasSupportedFieldCount(items, SAVE_DEADLINE_FIELD_COUNT)) {
                    return new Deadline(Task.isDoneFromStatus(items[SAVE_STATUS_INDEX]),
                            items[SAVE_DESCRIPTION_INDEX],
                            items[SAVE_DEADLINE_INDEX],
                            parseTags(items, SAVE_DEADLINE_FIELD_COUNT));
                }
                break;
            case SAVE_EVENT_TYPE:
                if (hasSupportedFieldCount(items, SAVE_EVENT_FIELD_COUNT)) {
                    return new Event(Task.isDoneFromStatus(items[SAVE_STATUS_INDEX]),
                            items[SAVE_DESCRIPTION_INDEX],
                            items[SAVE_EVENT_START_INDEX],
                            items[SAVE_EVENT_END_INDEX],
                            parseTags(items, SAVE_EVENT_FIELD_COUNT));
                }
                break;
            default:
                break;
        }
        throw new AthenaException("Storage File Corrupted by this line: " + line);
    }

    private static boolean hasSupportedFieldCount(String[] items, int legacyFieldCount) {
        return items.length == legacyFieldCount
                || items.length == legacyFieldCount + SAVE_TAG_FIELD_OFFSET;
    }

    private static List<Tag> parseTags(String[] items, int legacyFieldCount) {
        if (items.length == legacyFieldCount) {
            return List.of();
        }

        try {
            List<Tag> tags = Arrays.stream(items[legacyFieldCount].split(",", -1))
                    .map(Tag::new)
                    .toList();
            Set<Tag> uniqueTags = new HashSet<>(tags);
            if (uniqueTags.size() != tags.size()) {
                throw new AthenaException("Duplicate saved tag");
            }
            return tags;
        } catch (AthenaException e) {
            throw new AthenaException("Storage File Corrupted by this line: "
                    + String.join(SAVE_SEPARATOR, items));
        }
    }
}
