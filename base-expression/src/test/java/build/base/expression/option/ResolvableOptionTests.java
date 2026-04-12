package build.base.expression.option;

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

import build.base.configuration.Option;
import build.base.expression.compat.Processor;
import build.base.expression.compat.Variable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResolvableOptionTests {

    record HostName(String value) implements Option, ResolvableOption<HostName> {
        static HostName of(final String value) {
            return new HostName(value);
        }

        @Override
        public HostName resolve(final Processor processor) {
            return HostName.of(processor.replace(this.value));
        }
    }

    @Test
    void shouldResolveDollarExpressionInOptionValue() {
        final var processor = Processor.create(Variable.of("env", "production"));
        final var option = new HostName("${env}.example.com");
        final HostName resolved = option.resolve(processor);
        assertThat(resolved.value()).isEqualTo("production.example.com");
    }

    @Test
    void shouldReturnStaticValueWhenNoExpression() {
        final var processor = Processor.create();
        final var option = new HostName("static.example.com");
        final HostName resolved = option.resolve(processor);
        assertThat(resolved.value()).isEqualTo("static.example.com");
    }

    @Test
    void shouldResolveMultipleExpressionsInValue() {
        final var processor = Processor.create(Variable.of("host", "myapp"), Variable.of("tld", "io"));
        final var option = new HostName("${host}.example.${tld}");
        final HostName resolved = option.resolve(processor);
        assertThat(resolved.value()).isEqualTo("myapp.example.io");
    }

    @Test
    void shouldReturnNewInstanceOnResolve() {
        final var processor = Processor.create(Variable.of("val", "resolved"));
        final var original = new HostName("${val}");
        final HostName resolved = original.resolve(processor);
        assertThat(resolved).isNotSameAs(original);
        assertThat(resolved.value()).isEqualTo("resolved");
    }
}
