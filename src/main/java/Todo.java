public class Todo extends Task {

    public Todo(String description) {
        if (description == "") {
            throw new AthenaException("todo description cannot be empty");
        }
        super(description);
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
