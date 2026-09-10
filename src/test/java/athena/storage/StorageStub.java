package athena.storage;

import java.util.List;

import athena.task.Task;

/**
 * A stub for {@link Storage} that overrides file I/O behaviour for testing purposes.
 * Loading always reports success, and writing is a no-op, so tests using this stub
 * do not touch the file system.
 */
public class StorageStub extends Storage {
    /**
     * Constructs a {@code StorageStub} with no backing file.
     */
    public StorageStub() {
        super(null);
    }

    /**
     * Returns no tasks without accessing the file system.
     *
     * @return An empty task list.
     */
    @Override
    public List<Task> loadTasks() {
        return List.of();
    }

    /**
     * Always reports that items are loaded successfully.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean wasLoadSuccessful() {
        return true;
    }

    /**
     * Does nothing, so no tasks are written to any file.
     *
     * @param tasks Tasks that would be saved (ignored).
     */
    @Override
    public void saveTasks(List<Task> tasks) {
        //Do nothing
    }
}
