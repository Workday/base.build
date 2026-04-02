package build.base.expression;

import build.base.configuration.AbstractValueOption;
import build.base.configuration.ValueOption;
import jakarta.el.ELException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link Processor}s evaluate and replace expressions.
 *
 * @author graeme.campbell
 * @author brian.oliver
 * @since Jan-2019
 */
class ProcessorTests {

    /**
     * Ensure an undefined {@link Variable} is replaced as {@code null}.
     */
    @Test
    void shouldReplaceNonIncludedVariableAsNull() {

        final var processor = Processor.create();

        assertThat(processor.replace("Hello, ${locale}"))
            .isEqualTo("Hello, null");
    }

    /**
     * Ensure a {@link Variable} is included in a {@link Processor} upon construction.
     */
    @Test
    void shouldReplaceVariableIncludedOnConstruction() {

        final var applicationLocale = Variable.of("locale", "world");
        final var processor = Processor.create(applicationLocale);

        assertThat(processor.replace("Hello, ${locale}"))
            .isEqualTo("Hello, world");
    }

    /**
     * Ensure a {@link Variable} included in a {@link ProcessorBuilder} can be replaced.
     */
    @Test
    void shouldReplaceVariableDefinedUsingProcessorBuilder() {

        final var processor = ProcessorBuilder.create()
            .define(Variable.of("locale", "world"))
            .build();

        assertThat(processor.replace("Hello, ${locale}"))
            .isEqualTo("Hello, world");
    }

    /**
     * Ensure a {@link Variable} with a complex (base.property) name can be replaced.
     */
    @Test
    void shouldReplaceVariableWithComplexName() {

        final var applicationLocale = Variable.of("application.locale", "world");
        final var processor = Processor.create(applicationLocale);

        assertThat(processor.replace("Hello, ${application.locale}"))
            .isEqualTo("Hello, world");
    }

    /**
     * Ensure multiple {@link Variable}s which share namespaces don't overwrite each other.
     */
    @Test
    void shouldReplaceVariablesWithSharedNames() {

        final var applicationLocale = Variable.of("application.locale", "world");
        final var applicationCity = Variable.of("application.locale.city", "San Francisco");
        final var processor = ProcessorBuilder.create(applicationCity, applicationLocale)
            .define(Variable.of("application.locale.county", "San Francisco"))
            .build();

        assertThat(processor.replace("Hello, ${application.locale} from ${application.locale.city}"))
            .isEqualTo("Hello, world from San Francisco");

        assertThat(processor.replace("Hello, ${application.locale['county']}"))
            .isEqualTo("Hello, San Francisco");
    }

    /**
     * Ensure the {@link Processor} can replace mathematical and boolean operations on expressions.
     */
    @Test
    void shouldPerformOperationsWithVariables() {

        final var intelligenceScore = Variable.of("character.intelligence", 4);
        final var processor = Processor.create(intelligenceScore);

        assertThat(processor.replace("My Intelligence is the number that comes after ${character.intelligence - 1}"))
            .isEqualTo("My Intelligence is the number that comes after 3");

        assertThat(processor.replace("My Intelligence is greater than 3: ${character.intelligence > 3}"))
            .isEqualTo("My Intelligence is greater than 3: true");
    }

    /**
     * Ensure the {@link Processor} can replace lambdas within expressions.
     */
    @Test
    void shouldReplaceLambdaExpression() {

        final var intelligenceScore = Variable.of("character.intelligence", 4);
        final var processor = Processor.create(intelligenceScore);

        assertThat(processor.replace(
            "Let's count to my Intelligence: ${(x->[x-3, x-2, x-1])(character.intelligence)} ..."))
            .isEqualTo("Let's count to my Intelligence: [1, 2, 3] ...");
    }

    /**
     * Ensure calling methods on objects in expressions updates those objects for use in future expressions.
     */
    @Test
    void shouldCallMethodsOnVariablesAndKeepContext() {

        final var application = Variable.of("application", new HashMap<>());
        final var processor = Processor.create(application);

        assertThat(processor.replace("Let's try a map put: ${application.put(1, 1)}"))
            .isEqualTo("Let's try a map put: null");

        assertThat(processor.replace("Let's try a map get: ${application.get(1)}"))
            .isEqualTo("Let's try a map get: 1");
    }

    /**
     * Ensure {@link Variable}s included in a {@link Processor} post-construction can be replaced.
     */
    @Test
    void shouldReplaceVariablesIncludedAfterConstruction() {

        final var processor = ProcessorBuilder.create()
            .define(Variable.of("jetty.version", "9.24.5"))
            .define(Variable.of("powermock.version", "1.7.2"))
            .define(Variable.of("slf4j.version", "1.28"))
            .build();

        assertThat(processor.replace("${jetty.version}"))
            .isEqualTo("9.24.5");

        assertThat(processor.replace("${powermock.version}"))
            .isEqualTo("1.7.2");

        assertThat(processor.replace("${slf4j.version}"))
            .isEqualTo("1.28");
    }

    /**
     * Ensure {@link PropertyResolver}s can be used to resolve named values.
     */
    @Test
    void shouldEvaluateExpressionUsingPropertyResolvers() {

        final var processor = ProcessorBuilder.create()
            .addPropertyResolver("switch", name -> name.equals("mine"))
            .build();

        assertThat(processor.replace("${switch['mine']}"))
            .isEqualTo("true");

        assertThat(processor.evaluate("switch['mine']", boolean.class))
            .isTrue();

        assertThat(processor.evaluate("!switch['mine']", boolean.class))
            .isFalse();

        assertThat(processor.evaluate("switch['mine'] and switch['yours']", boolean.class))
            .isFalse();

        assertThat(processor.evaluate("switch['yours'] or switch['mine']", boolean.class))
            .isTrue();
    }

    /**
     * Ensure an {@link ELException} is thrown when attempting to parse an unparsable expression.
     */
    @Test
    void shouldFailToParseExpression() {
        final var processor = Processor.create();

        assertThrows(ELException.class, () -> processor.evaluate("${fruit", String.class));
    }

    /**
     * Ensure an {@link ELException} is thrown when attempting to illegally cast an expression to an incompatible type.
     */
    @Test
    void shouldFailToCastExpression() {
        final var processor = Processor.create();

        assertThrows(ELException.class, () -> processor.evaluate("12", boolean.class));
    }

    /**
     * Ensure a {@link String}-based {@link ValueOption} containing a resolvable expression can be resolved and
     * produces a new {@link ValueOption} of the same type but a different instance.
     */
    @Test
    void shouldResolveResolvableStringBasedValueOption() {
        final var processor = ProcessorBuilder.create()
            .define(Variable.of("hostname", "localhost"))
            .build();

        final var unresolvedHostName = HostName.of("${hostname}");

        assertThat(unresolvedHostName.get())
            .isEqualTo("${hostname}");

        final var resolvedHostName = processor.resolve(unresolvedHostName);

        assertThat(resolvedHostName)
            .isNotSameAs(unresolvedHostName);

        assertThat(resolvedHostName)
            .isNotEqualTo(unresolvedHostName);

        assertThat(resolvedHostName.get())
            .isEqualTo("localhost");
    }

    /**
     * Ensure a {@link String}-based {@link ValueOption} which doesn't contain an expression is not resolved and
     * returns the same {@link ValueOption}.
     */
    @Test
    void shouldNotResolveResolvableStringBasedValueOption() {
        final var processor = ProcessorBuilder.create()
            .define(Variable.of("hostname", "localhost"))
            .build();

        final var unresolvedHostName = HostName.of("localhost");

        assertThat(unresolvedHostName.get())
            .isEqualTo("localhost");

        final var resolvedHostName = processor.resolve(unresolvedHostName);

        assertThat(resolvedHostName)
            .isSameAs(unresolvedHostName);

        assertThat(resolvedHostName)
            .isEqualTo(unresolvedHostName);

        assertThat(resolvedHostName.get())
            .isEqualTo("localhost");
    }

    /**
     * A {@link String}-based {@link ValueOption} for testing purposes.
     */
    public static class HostName
        extends AbstractValueOption<String> {

        /**
         * Constructs a {@link HostName}.
         *
         * @param value the non-{@code null} value
         */
        private HostName(final String value) {
            super(value);
        }

        /**
         * Return an instance of {@link HostName} for the provided host string.
         *
         * @param host the host name
         * @return instance of {@link HostName} for the host
         */
        public static HostName of(final String host) {
            return new HostName(host);
        }
    }
}
