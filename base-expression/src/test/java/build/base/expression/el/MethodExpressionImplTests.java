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

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MethodExpressionImplTests {

    private ExpressionFactory factory;
    private StandardELContext context;

    @BeforeEach
    void setUp() {
        this.factory = new ExpressionFactoryImpl();
        this.context = new StandardELContext(this.factory);
    }

    @Test
    void shouldReturnExpressionString() {
        final var expr = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        assertThat(expr.getExpressionString()).isEqualTo("${list.size()}");
    }

    @Test
    void shouldAlwaysReturnFalseForLiteralText() {
        final var expr = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        assertThat(expr.isLiteralText()).isFalse();
    }

    @Test
    void shouldInvokeMethodOnBean() {
        this.context.getVariableMapper().setVariable("list",
            this.factory.createValueExpression(List.of("a", "b", "c"), List.class));
        final var expr = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        final Object result = expr.invoke(this.context, null);
        assertThat(result).isEqualTo(3);
    }

    @Test
    void shouldProvideMethodInfo() {
        this.context.getVariableMapper().setVariable("list",
            this.factory.createValueExpression(List.of(), List.class));
        final var expr = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        final var info = expr.getMethodInfo(this.context);
        assertThat(info.getName()).isEqualTo("size");
        assertThat(info.getReturnType()).isEqualTo(Integer.class);
    }

    @Test
    void shouldThrowWhenGettingMethodInfoForNonMethodExpression() {
        final var expr = this.factory.createMethodExpression(
            this.context, "${name}", String.class, new Class<?>[0]);
        assertThatThrownBy(() -> expr.getMethodInfo(this.context))
            .isInstanceOf(ELException.class);
    }

    @Test
    void shouldSupportEqualityBySameExpressionString() {
        final var expr1 = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        final var expr2 = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        assertThat(expr1).isEqualTo(expr2);
    }

    @Test
    void shouldNotEqualExpressionWithDifferentString() {
        final var expr1 = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        final var expr2 = this.factory.createMethodExpression(
            this.context, "${list.isEmpty()}", Boolean.class, new Class<?>[0]);
        assertThat(expr1).isNotEqualTo(expr2);
    }

    @Test
    void shouldSupportConsistentHashCode() {
        final var expr1 = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        final var expr2 = this.factory.createMethodExpression(
            this.context, "${list.size()}", Integer.class, new Class<?>[0]);
        assertThat(expr1.hashCode()).isEqualTo(expr2.hashCode());
    }
}
