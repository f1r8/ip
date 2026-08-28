package athena.storage;

import athena.task.Task;

import java.util.ArrayList;

public class StorageStub extends Storage {
    public StorageStub() {
        super(null);
    }

    @Override
    public boolean areItemsLoaded(java.util.ArrayList tasks) {
        return true;
    }

    @Override
    public void writeItems(ArrayList<Task> items) {
        // Do Nothing
    }
}
