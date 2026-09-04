package athena.storage;

import java.util.ArrayList;

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
     * Always reports that items are loaded successfully, without reading any file.
     *
     * @param tasks The list to check (ignored).
     * @return {@code true} always.
     */
    @Override
    public boolean areItemsLoaded(ArrayList<Task> tasks) {
        return true;
    }

    /**
     * Does nothing, so no items are actually written to any file.
     *
     * @param items The list of items to write (ignored).
     */
    @Override
    public void writeItems(ArrayList<Task> items) {
        //Do nothing
    }
}
