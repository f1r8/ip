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
     * Does nothing, so no tasks are loaded.
     *
     * @param tasks Tasks that would be loaded (ignored).
     */
    @Override
    public void loadTasks(List<Task> tasks) {
        return;
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

    public void wasloadFailed() {
    }

    /**
     * Does nothing, so no items are actually written to any file.
     *
     * @param items The list of items to write (ignored).
     */
    @Override
    public void writeItems(List<Task> items) {
        //Do nothing
    }
}
