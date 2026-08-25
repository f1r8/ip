public class Event extends Task {

    public static String fromToSeparator = "-";

    protected String from;
    protected String to;

    public Event(String input) {
        input = input.replaceAll("/from ", "/");
        input = input.replaceAll("/to ", "/");
        String[] inputs = input.split("/");
        if (inputs.length < 3) {
            throw new AthenaException("Please provide an event with /from and /to times, Your Majesty.");
        }
        this(inputs[0].trim(), inputs[1].trim(), inputs[2].trim());
    }

    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }
    public Event(boolean done, String description, String fromAndTo) {
        super(done, description);
        String[] fromToArray = fromAndTo.split(fromToSeparator);
        if (fromToArray.length < 3) {
            throw new AthenaException("Please provide an event with /from and /to times.");
        }
        this.from = fromToArray[0].trim();
        this.to = fromToArray[1].trim();
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + ", to: " + to + ")";
    }

    @Override
    public String toSaveString(){
        return "E" + Save.SEPARATOR + super.toSaveString() + Save.SEPARATOR +  this.from + fromToSeparator  + this.to;
    }
}
