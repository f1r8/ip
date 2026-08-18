public class Todo extends Task {

    public Todo(String description) {
        if (description == "") {
            throw new AthenaException("Please provide a todo description, Your Majesty.");
        }
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
