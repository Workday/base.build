package build.base.naming;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link UniqueNameGenerator}s.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
class UniqueNameGeneratorTests {

    /**
     * Ensure an initial unique name is generated when a {@link UniqueNameGenerator} is created.
     */
    @Test
    void shouldGenerateInitialUniqueName() {
        final var generator = new UniqueNameGenerator();

        assertThat(generator.hasNext())
            .isTrue();

        assertThat(generator.next())
            .isNotNull();
    }

    /**
     * Ensure the first two generated names are unique.
     */
    @Test
    void shouldGenerateAdditionalUniqueName() {
        final var generator = new UniqueNameGenerator();
        final var first = generator.next();
        final var second = generator.next();

        assertThat(first)
            .isNotEqualTo(second);
    }

    /**
     * Ensure we can generate a lot of unique names!
     */
    @Test
    void shouldGenerateUniqueNames() {
        final var generator = new UniqueNameGenerator();
        final var generated = new HashSet<String>();

        final var count = Math.min(100000, generator.size());

        for (var i = 0; i < count; i++) {
            final var name = generator.next();
            assertThat(generated.add(name))
                .isTrue();
        }
    }
}
