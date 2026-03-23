package build.base.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationBuilder}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class ConfigurationBuilderTests {

    /**
     * Ensure an empty {@link ConfigurationBuilder} can be created.
     */
    @Test
    void shouldCreateEmptyConfigurationBuilder() {
        final var builder = ConfigurationBuilder.create();

        assertThat(builder.isEmpty())
            .isTrue();

        assertThat(builder.isPresent(Switch.class))
            .isFalse();
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can be created with some default {@link Option}s.
     */
    @Test
    void shouldCreateConfigurationBuilderWithOptions() {
        final var builder = ConfigurationBuilder.create(Switch.ON);

        assertThat(builder.isEmpty())
            .isFalse();

        assertThat(builder.stream())
            .hasSize(1);

        assertThat(builder.stream(Switch.class))
            .containsExactly(Switch.ON);

        assertThat(builder.isPresent(Switch.class))
            .isTrue();
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can be created several {@link Option}s of the same {@link Class},
     * the last {@link Option} being used.
     */
    @Test
    void shouldCreateConfigurationBuilderWithMultipleOptionsOfTheSameClass() {
        final var builder = ConfigurationBuilder.create(Switch.ON, Switch.OFF);

        assertThat(builder.isEmpty())
            .isFalse();

        assertThat(builder.stream())
            .hasSize(1);

        assertThat(builder.stream(Switch.class))
            .containsExactly(Switch.OFF);

        assertThat(builder.isPresent(Switch.class))
            .isTrue();
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can add an {@link Option} when not present.
     */
    @Test
    void shouldAddOptionIfNotPresent() {
        final var builder = ConfigurationBuilder.create();

        builder.addIfNotPresent(Switch.class, Switch.ON);

        assertThat(builder.get(Switch.class))
            .isEqualTo(Switch.ON);

        builder.addIfNotPresent(Switch.class, Switch.OFF);

        assertThat(builder.get(Switch.class))
            .isEqualTo(Switch.ON);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can compute an {@link Option} when not present.
     */
    @Test
    void shouldComputeOptionIfNotPresent() {
        final var builder = ConfigurationBuilder.create();

        builder.computeIfNotPresent(Switch.class, () -> Switch.ON);

        assertThat(builder.get(Switch.class))
            .isEqualTo(Switch.ON);

        builder.computeIfNotPresent(Switch.class, () -> Switch.OFF);

        assertThat(builder.get(Switch.class))
            .isEqualTo(Switch.ON);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can remove an {@link Option}.
     */
    @Test
    void shouldRemoveOption() {
        final var builder = ConfigurationBuilder.create(Switch.ON);

        builder.remove(Switch.ON);

        assertThat(builder.isPresent(Switch.class))
            .isFalse();
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can compute and replace an existing {@link Option}.
     */
    @Test
    void shouldComputeOptionIfPresent() {
        final var builder = ConfigurationBuilder.create(Switch.ON);

        builder.compute(Switch.class, _ -> Switch.OFF);

        assertThat(builder.isPresent(Switch.class))
            .isTrue();

        assertThat(builder.get(Switch.class))
            .isEqualTo(Switch.OFF);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} can compute an {@link Option} when not present.
     */
    @Test
    void shouldComputeOptionWhenNotPresent() {
        final var builder = ConfigurationBuilder.create();

        builder.compute(Switch.class, _ -> Switch.ON);

        assertThat(builder.isPresent(Switch.class))
            .isTrue();

        assertThat(builder.get(Switch.class))
            .isEqualTo(Switch.ON);
    }
}
