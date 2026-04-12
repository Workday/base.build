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

import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueExpressionImplTests {

    private ExpressionFactory factory;
    private StandardELContext context;

    @BeforeEach
    void setUp() {
        this.factory = new ExpressionFactoryImpl();
        this.context = new StandardELContext(this.factory);
    }

    @Test
    void shouldEvaluateLiteralExpression() {
        final var expr = this.factory.createValueExpression(this.context, "${'hello'}", String.class);
        final Object result = expr.getValue(this.context);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldEvaluateArithmeticExpression() {
        final var expr = this.factory.createValueExpression(this.context, "${1 + 2}", Integer.class);
        final Object result = expr.getValue(this.context);
        assertThat(result).isEqualTo(3);
    }

    @Test
    void shouldCoerceResultToExpectedType() {
        final var expr = this.factory.createValueExpression(this.context, "42", String.class);
        // '42' without quotes is a numeric literal, but expectedType is String — coerce
        final Object result = expr.getValue(this.context);
        assertThat(result).isEqualTo("42");
    }

    @Test
    void shouldResolveVariableFromContext() {
        this.context.getVariableMapper().setVariable("greeting",
            this.factory.createValueExpression("hello", String.class));
        final var expr = this.factory.createValueExpression(this.context, "${greeting}", String.class);
        final Object result = expr.getValue(this.context);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldReturnExpectedType() {
        final var expr = this.factory.createValueExpression(this.context, "'42'", Integer.class);
        assertThat(expr.getExpectedType()).isEqualTo(Integer.class);
    }

    @Test
    void shouldReturnExpressionString() {
        final var expr = this.factory.createValueExpression(this.context, "${name}", String.class);
        assertThat(expr.getExpressionString()).isEqualTo("${name}");
    }

    @Test
    void shouldIndicateLiteralTextForStringExpression() {
        // A plain string with no ${ } is parsed as a LiteralNode
        final var expr = this.factory.createValueExpression(this.context, "hello world", String.class);
        assertThat(expr.isLiteralText()).isTrue();
    }

    @Test
    void shouldIndicateNonLiteralTextForELExpression() {
        final var expr = this.factory.createValueExpression(this.context, "${name}", String.class);
        assertThat(expr.isLiteralText()).isFalse();
    }

    @Test
    void shouldIndicateReadOnlyForLiteral() {
        final var expr = this.factory.createValueExpression(this.context, "'hello'", String.class);
        assertThat(expr.isReadOnly(this.context)).isTrue();
    }

    @Test
    void shouldIndicateWritableForIdentifier() {
        final var expr = this.factory.createValueExpression(this.context, "${myVar}", Object.class);
        assertThat(expr.isReadOnly(this.context)).isFalse();
    }

    @Test
    void shouldSupportEqualityBySameExpressionAndType() {
        final var expr1 = this.factory.createValueExpression(this.context, "${name}", String.class);
        final var expr2 = this.factory.createValueExpression(this.context, "${name}", String.class);
        assertThat(expr1).isEqualTo(expr2);
    }

    @Test
    void shouldNotEqualExpressionWithDifferentType() {
        final var expr1 = this.factory.createValueExpression(this.context, "${name}", String.class);
        final var expr2 = this.factory.createValueExpression(this.context, "${name}", Integer.class);
        assertThat(expr1).isNotEqualTo(expr2);
    }

    @Test
    void shouldSupportConsistentHashCode() {
        final var expr1 = this.factory.createValueExpression(this.context, "${name}", String.class);
        final var expr2 = this.factory.createValueExpression(this.context, "${name}", String.class);
        assertThat(expr1.hashCode()).isEqualTo(expr2.hashCode());
    }
}
