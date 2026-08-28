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

public class Storage {
    public static final String SAVE_SEPARATOR = " | ";

    public static final String SAVE_NEWLINE = System.lineSeparator();

    private String path;

    public Storage(String path) {
        this.path = path;
    }

    private Path getPath() {
        return Paths.get(path);
    }

    public void write(String content) {
        ensureFileExists();
        try {
            Files.writeString(getPath(), content, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new AthenaException("Unable to append to file");
        }
    }

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

    public void overwrite(String content) {
        ensureFileExists();
        try {
            Files.writeString(getPath(), content);
        } catch (IOException e) {
            throw new AthenaException("Something went wrong overwriting the file");
        }
    }

    public String read() {
        try {
            String content = Files.readString(getPath());
            return content;
        } catch (IOException e) {
            return "";
        }
    }

    public void writeItems(ArrayList<Task> items) {
        String content = "";
        for (Task item : items) {
            content += item.getSaveString() + SAVE_NEWLINE;
        }
        overwrite(content);
    }

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
