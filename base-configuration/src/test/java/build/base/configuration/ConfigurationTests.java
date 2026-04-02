package build.base.configuration;

import build.base.foundation.Capture;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Configuration}s.
 *
 * @author brian.oliver
 * @since Oct-2024
 */
class ConfigurationTests {

    /**
     * Ensure an empty {@link Configuration} can be created.
     */
    @Test
    void shouldCreateEmptyConfiguration() {
        final var configuration = Configuration.empty();

        assertThat(configuration)
            .isNotNull();

        assertThat(configuration.isEmpty())
            .isTrue();

        assertThat(configuration.stream())
            .isEmpty();
    }

    /**
     * Ensure an enum implementing the {@link Option} interface can be retrieved from a {@link Configuration}.
     */
    @Test
    void shouldAddAndGetEnumOption() {
        final var configuration = ConfigurationBuilder.create()
            .add(Switch.ON)
            .build();

        assertThat(configuration.get(Switch.class))
            .isEqualTo(Switch.ON);

        assertThat(configuration.getOptional(Switch.class))
            .contains(Switch.ON);

        assertThat(configuration.stream())
            .hasSize(1);
    }

    /**
     * Ensure a {@link Default} enum value can be retrieved from a {@link Configuration}.
     */
    @Test
    void shouldGetWithDefaultEnumOption() {
        final var configuration = Configuration.empty();

        assertThat(configuration.get(Color.class))
            .isEqualTo(Color.GREEN);

        assertThat(configuration.isEmpty())
            .isTrue();
    }

    /**
     * Ensure a specified {@link Option} is returned when a requested {@link Option}
     * does not exist in an {@link Configuration}.
     */
    @Test
    void shouldUseSpecifiedDefaultEnumOption() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getOrDefault(Color.class, () -> Color.RED))
            .isEqualTo(Color.RED);

        assertThat(configuration.isEmpty())
            .isTrue();
    }

    /**
     * Ensure {@code null} can be returned when a requested {@link Option} does not exist in a {@link Configuration}.
     */
    @Test
    void shouldReturnNullForDefaultOption() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getOrDefault(Color.class, () -> null))
            .isNull();

        assertThat(configuration.isEmpty())
            .isTrue();

        assertThat(configuration.getOrDefault(Color.class, null))
            .isNull();

        assertThat(configuration.isEmpty())
            .isTrue();
    }

    /**
     * Ensure a specified default {@link Option} is returned when a requested {@link Option} does not exist in a
     * {@link Configuration}.
     */
    @Test
    void shouldReturnSpecifiedDefaultEnumOption() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getOrDefault(Color.class, () -> Color.RED))
            .isEqualTo(Color.RED);

        assertThat(configuration.isEmpty())
            .isTrue();
    }

    /**
     * Ensure that a provided {@link Supplier} is not invoked when an {@link Option} is present.
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldAvoidSupplierWhenNonNull() {
        final var configuration = ConfigurationBuilder.create()
            .add(Color.GREEN)
            .build();

        // establish a mock supplier for the Color.BLUE
        // (so we can track interactions with the supplier)
        final Supplier<Color> blueSupplier = mock(Supplier.class);
        when(blueSupplier.get()).thenReturn(Color.BLUE);

        assertThat(configuration.getOrDefault(Color.class, blueSupplier))
            .isEqualTo(Color.GREEN);

        // ensure the supplier was never used
        verify(blueSupplier, times(0)).get();
    }

    /**
     * Ensure an {@link Option} of a specified type is returned from a {@link Configuration}.
     */
    @Test
    void shouldDeclareStronglyTypedConfiguration() {
        final var configuration = ConfigurationBuilder.create()
            .add(Color.GREEN)
            .build();

        assertThat(configuration.isEmpty())
            .isFalse();

        assertThat(configuration.get(Color.class))
            .isEqualTo(Color.GREEN);
    }

    /**
     * Ensure the {@link Option} collection {@link Class} of an {@link Option} can be determined for
     * an {@link CollectedOption}s.
     */
    @Test
    void shouldDetermineClassOfOptionContainerFromOptionContainerItem() {
        final var argument = Argument.of("hello world");

        assertThat(argument.createCollection())
            .isInstanceOf(List.class);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} will automatically create a collection for and collect
     * a {@link CollectedOption} {@link Option}s.
     */
    @Test
    void shouldCollectASingleCollectedOption() {
        final var argument = Argument.of("hello world");

        final var configuration = ConfigurationBuilder.create()
            .add(argument)
            .build();

        assertThat(configuration.isEmpty())
            .isFalse();

        assertThat(configuration.stream(Argument.class))
            .containsExactly(argument);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} will automatically create a collection for and collect
     * multiple {@link CollectedOption} {@link Option}s.
     */
    @Test
    void shouldCollectMultipleCollectedOptions() {
        final var firstArgument = Argument.of("hello world");
        final var secondArgument = Argument.of("gudday mate");

        final var configuration = ConfigurationBuilder.create()
            .add(firstArgument)
            .add(secondArgument)
            .build();

        assertThat(configuration.isEmpty())
            .isFalse();

        assertThat(configuration.stream(Argument.class))
            .containsExactly(firstArgument, secondArgument);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} will create a copy of {@link CollectedOption} {@link Option}s in a
     * {@link Configuration}.
     */
    @Test
    void shouldCreateCopyOfCollectedOptions() {
        final var firstArgument = Argument.of("hello world");
        final var secondArgument = Argument.of("gudday mate");

        final var configuration = ConfigurationBuilder.create()
            .add(firstArgument)
            .add(secondArgument)
            .build();

        final var otherConfiguration = ConfigurationBuilder.create()
            .include(configuration)
            .build();

        assertThat(otherConfiguration.isEmpty())
            .isFalse();

        assertThat(otherConfiguration.stream(Argument.class))
            .containsExactly(firstArgument, secondArgument);
    }

    /**
     * Ensure composable {@link Option}s are composed by a {@link ConfigurationBuilder}.
     */
    @Test
    void shouldComposeOptions() {
        final var configuration = ConfigurationBuilder.create()
            .add(HeapSize.minimum(100))
            .add(HeapSize.maximum(200))
            .build();

        final HeapSize heapSize = configuration.get(HeapSize.class);

        assertThat(configuration.stream())
            .hasSize(1);

        assertThat(heapSize.getMinimum())
            .contains(100);

        assertThat(heapSize.getMaximum())
            .contains(200);
    }

    /**
     * Ensure a stream of a particular type of {@link Option} can be retrieved from a {@link Configuration}.
     */
    @Test
    void shouldStreamSpecificComposedOptionTypes() {
        final var configuration = ConfigurationBuilder.create()
            .add(HeapSize.minimum(100))
            .add(HeapSize.maximum(200))
            .build();

        // there should only be one HeapSize
        assertThat(configuration.stream(HeapSize.class))
            .hasSize(1);
    }

    /**
     * Ensure a {@link ConfigurationBuilder} will create a copy of {@link MappedOption} {@link Option}s in an
     * {@link Configuration}.
     */
    @Test
    void shouldCreateCopyOfMappedOptions() {
        final var configuration = ConfigurationBuilder.create()
            .add(Attribute.of("AU", "Gudday"))
            .add(Attribute.of("TX", "Howdy"))
            .build();

        final var otherConfiguration = ConfigurationBuilder.create()
            .include(configuration)
            .add(Attribute.of("FR", "Bonjour"))
            .build();

        assertThat(otherConfiguration.get(Attribute.class, "AU").value())
            .isEqualTo("Gudday");

        assertThat(otherConfiguration.get(Attribute.class, "TX").value())
            .isEqualTo("Howdy");

        assertThat(otherConfiguration.get(Attribute.class, "FR").value())
            .isEqualTo("Bonjour");

        assertThat(configuration.get(Attribute.class, "FR"))
            .isNull();

        assertThat(configuration.stream(Attribute.class))
            .hasSize(2);

        assertThat(otherConfiguration.stream(Attribute.class))
            .hasSize(3);
    }

    /**
     * Ensure a {@link Configuration} can retrieve an {@link ValueOption}-based {@link Option}.
     */
    @Test
    void shouldRetrieveAbstractValueBasedOption() {
        final var configuration = ConfigurationBuilder.create()
            .add(Description.of("Cool"))
            .build();

        assertThat(configuration.getValue(Description.class))
            .isEqualTo("Cool");
    }

    /**
     * Ensure that we can retrieve an {@link Optional} of an existing {@link Option}
     * from a {@link Configuration}.
     */
    @Test
    void shouldReturnOptionalWithValue() {
        final var configuration = ConfigurationBuilder.create()
            .add(Color.RED)
            .build();

        assertThat(configuration.getOptional(Color.class))
            .contains(Color.RED);
    }

    /**
     * Ensure that we can retrieve an {@link Optional} of a default {@link Option} from a {@link Configuration}.
     */
    @Test
    void shouldReturnOptionalWithDefaultValue() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getOptional(Color.class))
            .contains(Color.GREEN);
    }

    /**
     * Ensure that a {@link Configuration} returns {@link Optional#empty()} if the {@link Option}
     * of specified type does not exist and a {@link Default} exists on the {@link Option}.
     */
    @Test
    void shouldReturnOptionalWithoutDefaultValue() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getOptionalWithoutDefault(Color.class))
            .isEmpty();
    }

    /**
     * Ensure {@link Optional}s are returned for {@link MappedOption} {@link Option}s.
     */
    @Test
    void shouldReturnOptionalForMappedOption() {
        final var configuration = ConfigurationBuilder.create()
            .add(Attribute.of("Key1", "Val1"))
            .add(Attribute.of("Key2", "Val2"))
            .build();

        assertThat(configuration.getOptional(Attribute.class, "Key3"))
            .isEmpty();

        assertThat(configuration.getOptional(Attribute.class, "Key1")
            .map(Attribute::value))
            .contains("Val1");

        assertThat(configuration.getOptional(Attribute.class, "Key2")
            .map(Attribute::value))
            .contains("Val2");
    }

    /**
     * Ensure that a {@link Configuration} returns {@link Optional#empty()} if the {@link Option}
     * of specified type does not exist and the {@link Option} does not have a default.
     */
    @Test
    void shouldReturnEmptyOptional() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getOptional(Description.class))
            .isEmpty();
    }

    /**
     * Ensure {@link Option}s can be collected by a {@link ConfigurationBuilder#collector()}.
     */
    @Test
    void shouldCollectOptions() {
        // ensure collecting an empty stream produces an empty OptionsByType
        assertThat(Stream.<Option>empty()
            .collect(ConfigurationBuilder.collector())
            .isEmpty())
            .isTrue();

        // some arguments to collect
        final var arguments = new Argument[] { Argument.of("1"), Argument.of("2"), Argument.of("3") };

        // collect the arguments
        final var configuration = Arrays.stream(arguments)
            .collect(ConfigurationBuilder.collector());

        // ensure there's the expected number
        assertThat(configuration.stream())
            .containsExactly(Argument.of("1"), Argument.of("2"), Argument.of("3"));
    }

    /**
     * Ensure a value of {@link ValueOption} is returned.
     */
    @Test
    void shouldReturnValueIfValueOptionExists() {

        final var configuration = ConfigurationBuilder.create()
            .add(Description.of("Foo"))
            .build();

        assertThat(configuration.getValue(Description.class))
            .isEqualTo("Foo");

        assertThat(configuration.getOptionalValue(Description.class))
            .contains("Foo");
    }

    /**
     * Ensure nothing is returned for a non-existent {@link ValueOption}.
     */
    @Test
    void shouldReturnNullValueIfValueOptionDoesNotExist() {
        final var configuration = Configuration.empty();

        assertThat(configuration.getValue(Description.class))
            .isNull();

        assertThat(configuration.getOptionalValue(Description.class))
            .isEmpty();
    }

    /**
     * Ensure {@link Configuration#isPresent(Class)} is satisfied.
     */
    @Test
    void shouldReturnIsPresent() {
        final var configuration = ConfigurationBuilder.create()
            .add(Color.RED)
            .build();

        assertThat(configuration.isPresent(Color.class))
            .isTrue();

        assertThat(configuration.isPresent(Argument.class))
            .isFalse();
    }

    /**
     * Ensure {@link Consumer} invoke when present and not invoked when not present.
     */
    @Test
    void shouldConsumeIfPresent() {
        final var capture = Capture.<String>empty();
        final Consumer<Option> consumer = option -> capture.set(option.toString());

        final var configuration = ConfigurationBuilder.create()
            .add(Color.RED)
            .build();

        configuration.ifPresent(Argument.class, consumer);

        assertThat(capture.isPresent())
            .isFalse();

        configuration.ifPresent(Color.class, consumer);

        assertThat(capture.optional())
            .contains(Color.RED.toString());
    }

    /**
     * Default values should be offered by private no-arg constructors.
     */
    @Test
    void shouldAllowDefaultConstructorCase() {
        assertThat(Configuration.empty()
            .getValue(DefaultConstructorCase.class))
            .isEqualTo(42);
    }

    /**
     * Default values should be offered by private static no-arg methods.
     */
    @Test
    void shouldAllowDefaultMethodCase() {
        assertThat(Configuration.empty()
            .getValue(DefaultMethodCase.class))
            .isEqualTo(42);
    }

    /**
     * Default values should be offered by private static fields.
     */
    @Test
    void shouldAllowDefaultFieldCase() {
        assertThat(Configuration.empty()
            .getValue(DefaultFieldCase.class))
            .isEqualTo(42);
    }

    /**
     * Ensure an interface can provide a {@link Default} {@link Option}.
     */
    @Test
    void shouldDiscoverDefaultValueFromInterface() {
        assertThat(Configuration.empty()
            .get(InterfaceWithDefault.class))
            .isNotNull();
    }

    private static class DefaultConstructorCase
        extends AbstractValueOption<Integer> {

        @Default
        private DefaultConstructorCase() {
            super(42);
        }
    }

    private static class DefaultMethodCase
        extends AbstractValueOption<Integer> {

        private DefaultMethodCase(final int value) {
            super(value);
        }

        @Default
        private static DefaultMethodCase getDefault() {
            return new DefaultMethodCase(42);
        }
    }

    private static class DefaultFieldCase
        extends AbstractValueOption<Integer> {

        @Default
        private static final DefaultFieldCase defaultFieldCase = new DefaultFieldCase(42);

        /**
         * This is necessary to get around checkstyles at the moment.
         */
        @SuppressWarnings("unused")
        public static final DefaultFieldCase publicValue = defaultFieldCase;

        private DefaultFieldCase(final int value) {
            super(value);
        }
    }

    private interface InterfaceWithDefault
        extends Option {

        @Default
        static InterfaceWithDefault autodetect() {
            return new InterfaceWithDefault() {
            };
        }
    }
}
