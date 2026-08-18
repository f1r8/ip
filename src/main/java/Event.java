public class Event extends Task {

    protected String from;
    protected String to;

    public Event(String input) {
        input = input.replaceAll("/from ", "/");
        input = input.replaceAll("/to ", "/");
        String[] inputs = input.split("/");
        this(inputs[0].trim(), inputs[1].trim(), inputs[2].trim());
    }

    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + ", to: " + to + ")";
    }
}
