package athena.storage;

import athena.exception.AthenaException;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Task;
import athena.task.Todo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Storage class for the Athena application.
 */
public class Storage {
    private String path;

    /** Separator for storing things in a single line */
    public static final String SEPARATOR = " | ";

    /** Fetches the lineSeparator of the current Operating System */
    public static final String NEWLINE = System.lineSeparator();

    /**
     * Constructor for a Storage object.
     *
     * @param path Path of the file on the system used for storing or retrieving data.
     */
    public Storage(String path) {
        this.path = path;
    }

    private Path getPath() {
        return Paths.get(path);
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
        }
        catch (IOException e){
            throw new AthenaException("Unable to append to file");
        }
    }

    /**
     * Ensure that the file exists.
     * If it doesn't exist, attempts to create the relevant directories and the file itself.
     */
    public void ensureFileExists() {
        File file = new File(path);
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
        }
        catch (IOException e){
            throw new AthenaException("Something went wrong overwriting the file");
        }
    }

    /**
     * Attempts to read from the storage file.
     *
     * @return the content in the file or an empty string if the file does not exist.
     */
    public String read() {
        try {
            String content = Files.readString(getPath());
            return content;
        } catch(IOException e) {
            return "";
        }
    }

    /**
     * Preprocesses tasks to Strings.
     * Passes the Strings to {@link overWrite(String)} to be written to storage.
     *
     * @param items Tasks to be written.
     */
    public void writeItems(ArrayList<Task> items) {
        String content = "";
        for (Task item : items) {
            content += item.toSaveString() + NEWLINE;
        }
        overwrite(content);
    }

    /**
     * Loads tasks from storage.
     * Uses {@link #read()} to get Strings from storage.
     *
     * @param items Tasks read from storage.
     * @return true if items are loaded, false otherwise.
     */
    public boolean loadItems(ArrayList<Task> items) {
        String input = read();
        if (input.equals("")) return false;
        String[] lines = input.split(NEWLINE);
        for (String line : lines) {
            String[] itemArray = line.split(Pattern.quote(SEPARATOR));
            if (itemArray.length == 3) {
                items.add(new Todo(Task.getStatusFromString(itemArray[1]), itemArray[2]));
            } else if (itemArray.length == 4) {
                items.add(new Deadline(Task.getStatusFromString(itemArray[1]), itemArray[2], itemArray[3]));
            } else if (itemArray.length == 5) {
                items.add(new Event(Task.getStatusFromString(itemArray[1]), itemArray[2], itemArray[3], itemArray[4]));
            }
            else {
                throw new AthenaException("Storage File Corrupted by this line: " + line);
            }
        }
        return true;
    }

}
