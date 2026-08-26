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
    public static final String PATH = "./data/athena.txt";

    public static final String SEPARATOR = " | ";

    public static final String NEWLINE = System.lineSeparator();

    public static Path getPath() {
        return Paths.get(PATH);
    }

    public static void write(String content) {
        ensureFileExists();
        try {
            Files.writeString(getPath(), content, StandardOpenOption.APPEND);
        }
        catch (IOException e){
            throw new AthenaException("Unable to append to file");
        }
    }

    public static void ensureFileExists() {
        File file = new File(PATH);
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

    public static void overWrite(String content) {
        ensureFileExists();
        try {
            Files.writeString(getPath(), content);
        }
        catch (IOException e){
            throw new AthenaException("Something went wrong overwriting the file");
        }
    }

    public static String read() {
        try {
            String content = Files.readString(getPath());
            return content;
        } catch(IOException e) {
            return "";
        }
    }

    public static void writeItems(ArrayList<Task> items) {
        String content = "";
        for (Task item : items) {
            content += item.toSaveString() + NEWLINE;
        }
        overWrite(content);
    }

    public static boolean loadItems(ArrayList<Task> items) {
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
                throw new AthenaException("athena.storage.Save File Corrupted by this line: " + line);
            }
        }
        return true;
    }

}
