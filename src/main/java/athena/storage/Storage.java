package athena.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

    private final String filePath;

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
        } catch (Exception e) {
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
     * @param items Tasks to be written.
     */
    public void writeItems(ArrayList<Task> items) {
        String content = "";
        for (Task item : items) {
            content += item.getSaveString() + SAVE_NEWLINE;
        }
        overwrite(content);
    }

    /**
     * Loads tasks from storage into the supplied list.
     *
     * @param tasks Tasks read from storage.
     * @return true if items are loaded, false otherwise.
     */
    public boolean areItemsLoaded(ArrayList<Task> tasks) {
        String input = read();
        if (input.equals("")) {
            return false;
        }

        String[] lines = input.split(SAVE_NEWLINE);
        for (String line : lines) {
            String[] items = line.split(Pattern.quote(SAVE_SEPARATOR));
            if (items.length == 3) {
                tasks.add(new Todo(Task.isDoneFromStatus(items[1]), items[2]));
            } else if (items.length == 4) {
                tasks.add(new Deadline(Task.isDoneFromStatus(items[1]), items[2], items[3]));
            } else if (items.length == 5) {
                tasks.add(new Event(Task.isDoneFromStatus(items[1]), items[2], items[3], items[4]));
            } else {
                throw new AthenaException("Storage File Corrupted by this line: " + line);
            }
        }
        return true;
    }
}
