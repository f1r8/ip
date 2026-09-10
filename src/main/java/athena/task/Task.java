package athena.task;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import athena.exception.AthenaException;
import athena.storage.Storage;

/**
 * Represents the common state and behavior of an Athena task.
 */
public abstract class Task {
    private static final String SAVE_STATUS_DONE = "1";
    private static final String SAVE_STATUS_NOT_DONE = "0";

    private final String description;
    private final Set<Tag> tags;
    private boolean isDone;

    /**
     * Constructs an incomplete task with the specified description.
     *
     * @param description Description of the task.
     */
    public Task(String description) {
        this(false, description, List.of());
    }

    /**
     * Constructs a Task object.
     *
     * @param isDone {@code true} if the task is complete, {@code false} otherwise.
     * @param description Description of the Task object.
     */
    public Task(boolean isDone, String description) {
        this(isDone, description, List.of());
    }

    /**
     * Constructs a Task object with saved tags.
     *
     * @param isDone {@code true} if the task is complete, {@code false} otherwise.
     * @param description Description of the Task object.
     * @param tags Saved tags to restore.
     */
    protected Task(boolean isDone, String description, List<Tag> tags) {
        if (description.isEmpty()) {
            throw new AthenaException("Task description cannot be empty");
        }
        this.isDone = isDone;
        this.description = description;
        this.tags = new TreeSet<>(tags);
    }

    /**
     * Marks a Task object as done.
     */
    public void markDone() {
        this.isDone = true;
    }

    /**
     * Unmarks a Task object as not done.
     */
    public void unmarkDone() {
        this.isDone = false;
    }

    /**
     * Gets the status icon of whether the task is done.
     *
     * @return String of the status icon.
     */
    public String getStatusIcon() {
        return this.isDone ? "X" : " ";
    }

    /**
     * Returns the tags in normalized alphabetical order without exposing mutable task state.
     *
     * @return Immutable snapshot of the tags.
     */
    public List<Tag> getTags() {
        return List.copyOf(tags);
    }

    /**
     * Adds a tag unless a case-insensitive equivalent is already present.
     *
     * @param tag Tag to add.
     * @return {@code true} if the task changed, {@code false} otherwise.
     */
    public boolean addTag(Tag tag) {
        return tags.add(tag);
    }

    /**
     * Removes a tag using case-insensitive tag identity.
     *
     * @param tag Tag to remove.
     * @return {@code true} if the task changed, {@code false} otherwise.
     */
    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }

    /**
     * Returns whether the task has the specified tag, ignoring case.
     *
     * @param tag Tag to find.
     * @return {@code true} if the task has the tag, {@code false} otherwise.
     */
    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * {@inheritDoc}
     *
     * @return The Task with any tags after its subtype-specific information.
     */
    @Override
    public final String toString() {
        String tagString = tags.stream()
                .map(Tag::toString)
                .collect(Collectors.joining(" "));
        return tagString.isEmpty()
                ? getDisplayStringWithoutTags()
                : getDisplayStringWithoutTags() + " " + tagString;
    }

    /**
     * Returns the persisted completion status.
     *
     * @return Save-file representation of the completion status.
     */
    public String getSaveStatus() {
        return this.isDone ? SAVE_STATUS_DONE : SAVE_STATUS_NOT_DONE;
    }

    /**
     * Returns whether a saved status represents a completed task.
     *
     * @param status Saved completion status.
     * @return {@code true} if the task is complete, {@code false} otherwise.
     */
    public static boolean isDoneFromStatus(String status) {
        if (status.equals(SAVE_STATUS_DONE)) {
            return true;
        } else if (status.equals(SAVE_STATUS_NOT_DONE)) {
            return false;
        }
        throw new AthenaException("Error converting save string to num: " + status);
    }

    /**
     * Generates the storage String for the Task object.
     *
     * @return Storage String.
     */
    public final String getSaveString() {
        String tagString = tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(","));
        return tagString.isEmpty()
                ? getSaveStringWithoutTags()
                : getSaveStringWithoutTags() + Storage.SAVE_SEPARATOR + tagString;
    }

    /**
     * Generates the display string before tags are appended.
     *
     * @return Display string containing common task information.
     */
    protected String getDisplayStringWithoutTags() {
        return "[" + this.getStatusIcon() + "] " + this.description;
    }

    /**
     * Generates the storage string before tags are appended.
     *
     * @return Storage string containing common task fields.
     */
    protected String getSaveStringWithoutTags() {
        return getSaveStatus() + Storage.SAVE_SEPARATOR + this.description;
    }
}
