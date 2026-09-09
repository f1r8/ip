package athena.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

import athena.exception.AthenaException;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;

/**
 * Persists Athena tasks in a local text file.
 */
public class Storage {
    /** Separator between fields in a saved task */
    public static final String SAVE_SEPARATOR = " | ";

    /** Line separator used between saved tasks */
    public static final String SAVE_NEWLINE = System.lineSeparator();

    private static final int SAVE_STATUS_INDEX = 1;
    private static final int SAVE_DESCRIPTION_INDEX = 2;
    private static final int SAVE_DEADLINE_INDEX = 3;
    private static final int SAVE_EVENT_START_INDEX = 3;
    private static final int SAVE_EVENT_END_INDEX = 4;
    private static final int SAVE_TODO_FIELD_COUNT = 3;
    private static final int SAVE_DEADLINE_FIELD_COUNT = 4;
    private static final int SAVE_EVENT_FIELD_COUNT = 5;

    private final String filePath;

    private boolean loadSuccessful;

    /**
     * Constructs storage backed by the file at the specified path.
     *
     * @param path Path of the file on the system used for storing or retrieving data.
     */
    public Storage(String path) {
        this.filePath = path;
    }

    private Path getPath() {
        return Paths.get(filePath);
    }

    /**
     * Appends to the file.
     *
     * @param content Content to be written.
     */
    public void write(String content) {
        ensureFileExists();
        try {
            Files.writeString(getPath(), content, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new AthenaException("Unable to append to file");
        }
    }

    /**
     * Ensures that the storage file and its parent directories exist.
     */
    public void ensureFileExists() {
        File file = new File(filePath);
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new AthenaException("Fatal Error. Data File cannot be created.");
        }
    }

    /**
     * Overwrites the content in the storage file.
     *
     * @param content Content to be written to storage.
     */
    public void overwrite(String content) {
        ensureFileExists();
        try {
            Files.writeString(getPath(), content);
        } catch (IOException e) {
            throw new AthenaException("Something went wrong overwriting the file");
        }
    }

    /**
     * Reads the storage file if it exists.
     *
     * @return the content in the file or an empty string if the file does not exist.
     */
    public String read() {
        try {
            String content = Files.readString(getPath());
            return content;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Serializes tasks and writes them to storage.
     *
     * @param tasks Tasks to be written.
     */
    public void saveTasks(List<Task> tasks) {
        String content = "";
        for (Task task : tasks) {
            content += task.getSaveString() + SAVE_NEWLINE;
        }
        overwrite(content);
    }

    /**
     * Loads tasks from storage into the supplied list.
     *
     * @param tasks Tasks read from storage.
     */
    public void loadTasks(List<Task> tasks) {
        String input = read();
        if (input.isEmpty()) {
            loadSuccessful = false;
            return;
        }

        String[] lines = input.split(SAVE_NEWLINE);
        for (String line : lines) {
            tasks.add(parseTask(line));
        }
        loadSuccessful = true;
        return;
    }

    /**
     * Checks if {@link loadTasks} was successful.
     *
     * @return {@code true} if tasks are loaded, {@code false} otherwise.
     */
    public boolean wasLoadSuccessful() {
        return loadSuccessful;
    }

    private static Task parseTask(String line) {
        String[] items = line.split(Pattern.quote(SAVE_SEPARATOR));
        if (items.length == SAVE_TODO_FIELD_COUNT) {
            return new Todo(Task.isDoneFromStatus(items[SAVE_STATUS_INDEX]),
                    items[SAVE_DESCRIPTION_INDEX]);
        } else if (items.length == SAVE_DEADLINE_FIELD_COUNT) {
            return new Deadline(Task.isDoneFromStatus(items[SAVE_STATUS_INDEX]),
                    items[SAVE_DESCRIPTION_INDEX],
                    items[SAVE_DEADLINE_INDEX]);
        } else if (items.length == SAVE_EVENT_FIELD_COUNT) {
            return new Event(Task.isDoneFromStatus(items[SAVE_STATUS_INDEX]),
                    items[SAVE_DESCRIPTION_INDEX],
                    items[SAVE_EVENT_START_INDEX],
                    items[SAVE_EVENT_END_INDEX]);
        }
        throw new AthenaException("Storage File Corrupted by this line: " + line);
    }
}
