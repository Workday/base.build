package build.base.expression.compat;

/*-
 * #%L
 * base.build Expression
 * %%
 * Copyright (C) 2025 Workday Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.configuration.AbstractValueOption;
import build.base.configuration.ValueOption;
import jakarta.el.ELException;
import org.junit.jupiter.api.Disabled;
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

    @Test
    void shouldReplaceNonIncludedVariableAsNull() {
        final var processor = Processor.create();
        assertThat(processor.replace("Hello, ${locale}"))
            .isEqualTo("Hello, null");
    }

    @Test
    void shouldReplaceVariableIncludedOnConstruction() {
        final var applicationLocale = Variable.of("locale", "world");
        final var processor = Processor.create(applicationLocale);
        assertThat(processor.replace("Hello, ${locale}"))
            .isEqualTo("Hello, world");
    }

    @Test
    void shouldReplaceVariableDefinedUsingProcessorBuilder() {
        final var processor = ProcessorBuilder.create()
            .define(Variable.of("locale", "world"))
            .build();
        assertThat(processor.replace("Hello, ${locale}"))
            .isEqualTo("Hello, world");
    }

    @Test
    void shouldReplaceVariableWithComplexName() {
        final var applicationLocale = Variable.of("application.locale", "world");
        final var processor = Processor.create(applicationLocale);
        assertThat(processor.replace("Hello, ${application.locale}"))
            .isEqualTo("Hello, world");
    }

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

    @Test
    void shouldPerformOperationsWithVariables() {
        final var intelligenceScore = Variable.of("character.intelligence", 4);
        final var processor = Processor.create(intelligenceScore);

        assertThat(processor.replace("My Intelligence is the number that comes after ${character.intelligence - 1}"))
            .isEqualTo("My Intelligence is the number that comes after 3");

        assertThat(processor.replace("My Intelligence is greater than 3: ${character.intelligence > 3}"))
            .isEqualTo("My Intelligence is greater than 3: true");
    }

    @Disabled("EL collection literal syntax ([e1, e2, e3]) is not yet implemented in ELParser")
    @Test
    void shouldReplaceLambdaExpression() {
        final var intelligenceScore = Variable.of("character.intelligence", 4);
        final var processor = Processor.create(intelligenceScore);

        assertThat(processor.replace(
            "Let's count to my Intelligence: ${(x->[x-3, x-2, x-1])(character.intelligence)} ..."))
            .isEqualTo("Let's count to my Intelligence: [1, 2, 3] ...");
    }

    @Test
    void shouldCallMethodsOnVariablesAndKeepContext() {
        final var application = Variable.of("application", new HashMap<>());
        final var processor = Processor.create(application);

        assertThat(processor.replace("Let's try a map put: ${application.put(1, 1)}"))
            .isEqualTo("Let's try a map put: null");

        assertThat(processor.replace("Let's try a map get: ${application.get(1)}"))
            .isEqualTo("Let's try a map get: 1");
    }

    @Test
    void shouldReplaceVariablesIncludedAfterConstruction() {
        final var processor = ProcessorBuilder.create()
            .define(Variable.of("jetty.version", "9.24.5"))
            .define(Variable.of("powermock.version", "1.7.2"))
            .define(Variable.of("slf4j.version", "1.28"))
            .build();

        assertThat(processor.replace("${jetty.version}")).isEqualTo("9.24.5");
        assertThat(processor.replace("${powermock.version}")).isEqualTo("1.7.2");
        assertThat(processor.replace("${slf4j.version}")).isEqualTo("1.28");
    }

    @Test
    void shouldEvaluateExpressionUsingPropertyResolvers() {
        final var processor = ProcessorBuilder.create()
            .addPropertyResolver("switch", name -> name.equals("mine"))
            .build();

        assertThat(processor.replace("${switch['mine']}")).isEqualTo("true");
        assertThat(processor.evaluate("switch['mine']", boolean.class)).isTrue();
        assertThat(processor.evaluate("!switch['mine']", boolean.class)).isFalse();
        assertThat(processor.evaluate("switch['mine'] and switch['yours']", boolean.class)).isFalse();
        assertThat(processor.evaluate("switch['yours'] or switch['mine']", boolean.class)).isTrue();
    }

    @Test
    void shouldFailToParseExpression() {
        final var processor = Processor.create();
        assertThrows(ELException.class, () -> processor.evaluate("${fruit", String.class));
    }

    @Test
    void shouldFailToCastExpression() {
        final var processor = Processor.create();
        assertThrows(ELException.class, () -> processor.evaluate("12", boolean.class));
    }

    @Test
    void shouldResolveResolvableStringBasedValueOption() {
        final var processor = ProcessorBuilder.create()
            .define(Variable.of("hostname", "localhost"))
            .build();

        final var unresolvedHostName = HostName.of("${hostname}");
        assertThat(unresolvedHostName.get()).isEqualTo("${hostname}");

        final var resolvedHostName = processor.resolve(unresolvedHostName);
        assertThat(resolvedHostName).isNotSameAs(unresolvedHostName);
        assertThat(resolvedHostName).isNotEqualTo(unresolvedHostName);
        assertThat(resolvedHostName.get()).isEqualTo("localhost");
    }

    @Test
    void shouldNotResolveResolvableStringBasedValueOption() {
        final var processor = ProcessorBuilder.create()
            .define(Variable.of("hostname", "localhost"))
            .build();

        final var unresolvedHostName = HostName.of("localhost");
        assertThat(unresolvedHostName.get()).isEqualTo("localhost");

        final var resolvedHostName = processor.resolve(unresolvedHostName);
        assertThat(resolvedHostName).isSameAs(unresolvedHostName);
        assertThat(resolvedHostName).isEqualTo(unresolvedHostName);
        assertThat(resolvedHostName.get()).isEqualTo("localhost");
    }

    public static class HostName
        extends AbstractValueOption<String> {

        private HostName(final String value) {
            super(value);
        }

        public static HostName of(final String host) {
            return new HostName(host);
        }
    }
}
