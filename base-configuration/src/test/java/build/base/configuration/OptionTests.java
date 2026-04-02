package build.base.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Option}s.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
class OptionTests {

    /**
     * Ensure the expected discriminator classes are returned by an {@link Option}.
     */
    @Test
    void shouldDetermineTheClassDiscriminator() {

        // the discriminator class of Option is Option
        assertThat(Option.getDiscriminatorClass(Option.class).getName())
            .isEqualTo(Option.class.getName());

        // the discriminator class of a Mapped is Option
        assertThat(Option.getDiscriminatorClass(MappedOption.class).getName())
            .isEqualTo(MappedOption.class.getName());

        // the discriminator class of a Collected is Option
        assertThat(Option.getDiscriminatorClass(CollectedOption.class).getName())
            .isEqualTo(CollectedOption.class.getName());

        // the discriminator class of a non-@Discriminator enum class of Option is the enum itself
        assertThat(Option.getDiscriminatorClass(Color.class).getName())
            .isEqualTo(Color.class.getName());

        // the discriminator class of a non-@Discriminator subclass of Option is the class itself
        assertThat(Option.getDiscriminatorClass(Launch.class).getName())
            .isEqualTo(Launch.class.getName());

        // the discriminator class of a non-@Discriminator sub-class of Option is the class itself
        assertThat(Option.getDiscriminatorClass(Network.class).getName())
            .isEqualTo(Network.class.getName());

        // the discriminator class of a @Discriminator subclass of Option is the class itself
        assertThat(Option.getDiscriminatorClass(Discovery.class).getName())
            .isEqualTo(Discovery.class.getName());

        // the discriminator class of a @Discriminator subclass of Option is the annotated discriminator class
        assertThat(Option.getDiscriminatorClass(FileBasedDiscovery.class).getName())
            .isEqualTo(Discovery.class.getName());
    }

    /**
     * Ensure {@link Option} can be extracted from a {@link Class}
     */
    @Test
    void shouldIncludeOptionsFromClass() {

        final var options1 = ConfigurationBuilder.create()
            .include(ClassWithMethodReturningOption.class)
            .build();

        assertThat(options1.isPresent(OptionForTest.class))
            .isTrue();

        final var options2 = ConfigurationBuilder.create()
            .include(ClassWithPackageVisibleMethodReturningOption.class)
            .build();

        assertThat(options2.isPresent(OptionForTest.class))
            .isFalse();

        final var options3 = ConfigurationBuilder.create()
            .include(ClassWithNonstaticMethodReturningOption.class)
            .build();

        assertThat(options3.isPresent(OptionForTest.class))
            .isFalse();

        final var options4 = ConfigurationBuilder.create()
            .include(ClassWithMethodNotReturningOption.class)
            .build();

        assertThat(options4.isPresent(OptionForTest.class))
            .isFalse();

        final var options5 = ConfigurationBuilder.create()
            .include(ClassWithOneArgMethodReturningOption.class)
            .build();

        assertThat(options5.isPresent(OptionForTest.class))
            .isFalse();

        assertThrows(IllegalArgumentException.class,
            () -> ConfigurationBuilder.create()
                .include(ClassWithMethodReturningOptionThatThrows.class)
                .build());
    }

    private static class OptionForTest
        extends AbstractValueOption<Integer> {

        OptionForTest() {
            super(42);
        }
    }

    private static class ClassWithMethodReturningOption {

        @SuppressWarnings("unused")
        public static OptionForTest option() {
            return new OptionForTest();
        }
    }

    private static class ClassWithPackageVisibleMethodReturningOption {

        @SuppressWarnings("unused")
        static OptionForTest option() {
            throw new RuntimeException(); // should not be invoked
        }
    }

    private static class ClassWithNonstaticMethodReturningOption {

        @SuppressWarnings("unused")
        public OptionForTest option() {
            throw new RuntimeException(); // should not be invoked
        }
    }

    private static class ClassWithMethodNotReturningOption {

        @SuppressWarnings("unused")
        public static Object notAnOption() {
            throw new RuntimeException(); // should not be invoked
        }
    }

    private static class ClassWithOneArgMethodReturningOption {

        @SuppressWarnings("unused")
        public static OptionForTest option(final int i) {
            throw new RuntimeException(); // should not be invoked
        }
    }

    private static class ClassWithMethodReturningOptionThatThrows {

        @SuppressWarnings("unused")
        public static OptionForTest option() {
            throw new NullPointerException(); // should be wrapped in IllegalArgumentException
        }
    }
}
