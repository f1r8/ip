package athena.storage;

import athena.task.Task;

import java.util.ArrayList;

public class StorageStub extends Storage{
    public StorageStub(){
        super(null);
    }
    @Override
    public boolean loadItems(java.util.ArrayList items) {
        return true;
    }

    @Override
    public void writeItems(ArrayList<Task> items) {
        //Do Nothing
    }
}
