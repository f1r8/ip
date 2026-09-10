package athena.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import athena.exception.AthenaException;

/**
 * Tests tag validation, identity, and display spelling.
 */
class TagTest {

    @Test
    void equality_differentCase_equalWithMatchingHashCode() {
        Tag first = new Tag("#Work");
        Tag second = new Tag("#wOrK");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals("#Work", first.toString());
        assertEquals("#wOrK", second.toString());
        assertNotEquals(first, new Tag("#Personal"));
    }

    @Test
    void constructor_supportedCharacters_tagCreated() {
        assertEquals("#Alpha_2-test", new Tag("#Alpha_2-test").toString());
    }

    @Test
    void constructor_invalidPattern_exceptionThrown() {
        List<String> invalidNames = new ArrayList<>();
        invalidNames.add(null);
        invalidNames.addAll(List.of("", "Work", "#", "#two words", "#bad!", "#two,tags", "#save|field"));

        for (String invalidName : invalidNames) {
            AthenaException exception = assertThrows(AthenaException.class, () -> new Tag(invalidName));
            assertEquals("Each tag must start with # and contain at least one letter, number, underscore, "
                    + "or hyphen, Your Majesty.", exception.getMessage());
        }
    }

    @Test
    void compareTo_mixedCaseTags_sortedByNormalizedIdentity() {
        List<Tag> tags = new ArrayList<>(List.of(new Tag("#zeta"), new Tag("#Alpha")));

        Collections.sort(tags);

        assertEquals(List.of("#Alpha", "#zeta"), tags.stream().map(Tag::toString).toList());
    }
}
