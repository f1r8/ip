package athena.task;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import athena.exception.AthenaException;

/**
 * Represents a case-insensitive tag while preserving its original spelling.
 */
public final class Tag implements Comparable<Tag> {
    private static final String INVALID_TAG_MESSAGE =
            "Each tag must start with # and contain at least one letter, number, underscore, or hyphen, "
                    + "Your Majesty.";
    private static final Pattern VALID_TAG_PATTERN = Pattern.compile("#[A-Za-z0-9_-]+");

    private final String name;
    private final String normalizedName;

    /**
     * Constructs a tag with the specified display spelling.
     *
     * @param name Tag name.
     */
    public Tag(String name) {
        if (name == null || !VALID_TAG_PATTERN.matcher(name).matches()) {
            throw new AthenaException(INVALID_TAG_MESSAGE);
        }
        this.name = name;
        this.normalizedName = name.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the original spelling of this tag.
     *
     * @return Original tag name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this tag has the same name as another tag, ignoring case.
     *
     * @param other Object to compare with this tag.
     * @return {@code true} if both objects are tags with the same case-insensitive name.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag otherTag)) {
            return false;
        }
        return normalizedName.equals(otherTag.normalizedName);
    }

    /**
     * Returns a case-insensitive hash code for this tag.
     *
     * @return Hash code based on the normalized tag name.
     */
    @Override
    public int hashCode() {
        return Objects.hash(normalizedName);
    }

    /**
     * Compares tags by their case-insensitive identity.
     *
     * @param other Tag to compare with this tag.
     * @return Negative, zero, or positive according to normalized alphabetical order.
     */
    @Override
    public int compareTo(Tag other) {
        return normalizedName.compareTo(other.normalizedName);
    }

    /**
     * Returns the original spelling of this tag.
     *
     * @return Original tag name.
     */
    @Override
    public String toString() {
        return name;
    }
}
