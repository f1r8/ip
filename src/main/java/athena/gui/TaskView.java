package athena.gui;

import java.util.List;

import athena.parser.DateParser;
import athena.task.Deadline;
import athena.task.Event;
import athena.task.Tag;
import athena.task.Task;

/**
 * Contains an immutable snapshot of the task details shown in one conversation reply.
 *
 * @param number Display number, or zero for an unnumbered confirmation.
 * @param description Task description without console formatting.
 * @param isDone Whether the task was complete when the reply was created.
 * @param type Human-readable task type.
 * @param schedule Formatted date and time details, or an empty string for a todo.
 * @param tags Display tags in their task order.
 */
public record TaskView(int number, String description, boolean isDone, String type, String schedule,
        List<String> tags) {
    /**
     * Copies the tags so later task changes cannot alter a previous reply.
     */
    public TaskView {
        tags = List.copyOf(tags);
    }

    /**
     * Copies a task's fields into a GUI snapshot without parsing its console representation.
     */
    public static TaskView from(Task task, int number) {
        String type = "Todo";
        String schedule = "";
        if (task instanceof Deadline deadline) {
            type = "Deadline";
            schedule = "Due " + DateParser.formatOutput(deadline.getDeadline());
        } else if (task instanceof Event event) {
            type = "Event";
            schedule = "From " + DateParser.formatOutput(event.getStartDateTime())
                    + "\nTo " + DateParser.formatOutput(event.getEndDateTime());
        }
        return new TaskView(number, task.getDescription(), task.isDone(), type, schedule,
                task.getTags().stream().map(Tag::toString).toList());
    }
}
