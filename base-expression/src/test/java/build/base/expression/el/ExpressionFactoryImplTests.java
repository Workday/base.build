package build.base.expression.el;

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

import jakarta.el.ELProcessor;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionFactoryImplTests {

    private final ExpressionFactory factory = new ExpressionFactoryImpl();

    @Test
    void shouldCoerceStringToInteger() {
        assertThat(factory.coerceToType("42", Integer.class)).isEqualTo(42);
    }

    @Test
    void shouldCoerceNullToEmptyStringPerSpec() {
        // EL spec 1.23: null coerces to "" for String
        assertThat(factory.coerceToType(null, String.class)).isEqualTo("");
    }

    @Test
    void shouldCreateValueExpressionFromLiteral() {
        final var expr = factory.createValueExpression("hello", String.class);
        final Object result = expr.getValue(null);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldDiscoverViaServiceLoader() {
        final ExpressionFactory discovered = ExpressionFactory.newInstance();
        assertThat(discovered).isInstanceOf(ExpressionFactoryImpl.class);
    }

    @Test
    void shouldEvaluateArithmeticExpression() {
        final var ctx = new StandardELContext(factory);
        final var expr = factory.createValueExpression(ctx, "${1 + 2}", Long.class);
        final Object result = expr.getValue(ctx);
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void shouldEvaluateViaELProcessor() {
        final var processor = new ELProcessor();
        final Object result = processor.eval("1 + 1");
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void shouldResolveDefinedBean() {
        final var processor = new ELProcessor();
        processor.defineBean("name", "World");
        final Object result = processor.eval("name");
        assertThat(result).isEqualTo("World");
    }

    @Test
    void shouldEvaluateConditional() {
        final var processor = new ELProcessor();
        final String result = processor.getValue("true ? 'yes' : 'no'", String.class);
        assertThat(result).isEqualTo("yes");
    }

    @Test
    void shouldEvaluateStringLiteral() {
        final var processor = new ELProcessor();
        final String result = processor.getValue("'hello'", String.class);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldEvaluateBooleanComparison() {
        final var processor = new ELProcessor();
        processor.defineBean("x", 5);
        final Boolean result = processor.getValue("x > 3", Boolean.class);
        assertThat(result).isTrue();
    }
}
