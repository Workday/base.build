package build.base.expression.eval;

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

import build.base.expression.el.ExpressionFactoryImpl;
import build.base.expression.parser.ELParser;
import jakarta.el.ELContext;
import jakarta.el.StandardELContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NodeEvaluatorTests {

    private ELContext context;

    @BeforeEach
    void setUp() {
        this.context = new StandardELContext(new ExpressionFactoryImpl());
    }

    // --- Literals ---

    @Test
    void shouldEvaluateLiteralString() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("'hello'"), this.context);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldEvaluateLiteralInteger() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("42"), this.context);
        assertThat(result).isEqualTo(42L);
    }

    @Test
    void shouldEvaluateLiteralTrue() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("true"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateLiteralNull() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("null"), this.context);
        assertThat(result).isNull();
    }

    // --- Arithmetic ---

    @Test
    void shouldEvaluateAddition() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("1 + 2"), this.context);
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void shouldEvaluateSubtraction() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("10 - 3"), this.context);
        assertThat(result).isEqualTo(7L);
    }

    @Test
    void shouldEvaluateMultiplication() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("4 * 5"), this.context);
        assertThat(result).isEqualTo(20L);
    }

    @Test
    void shouldEvaluateDivision() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("10 / 4"), this.context);
        assertThat(result).isEqualTo(2.5);
    }

    @Test
    void shouldEvaluateModulo() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("10 % 3"), this.context);
        assertThat(result).isEqualTo(1L);
    }

    @Test
    void shouldEvaluateUnaryNegate() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("-5"), this.context);
        assertThat(result).isEqualTo(-5L);
    }

    // --- Comparison ---

    @Test
    void shouldEvaluateGreaterThan() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("3 > 2"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateLessThan() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("1 < 2"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateEquals() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("5 == 5"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateNotEquals() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("3 != 5"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    // --- Logical ---

    @Test
    void shouldEvaluateLogicalAnd() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("true && false"), this.context);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    @Test
    void shouldEvaluateLogicalOr() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("false || true"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldShortCircuitAndOnFalse() {
        // Left is false — right should not be evaluated (no error from invalid identifier)
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("false && true"), this.context);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    @Test
    void shouldShortCircuitOrOnTrue() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("true || false"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateUnaryNot() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("!true"), this.context);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    // --- Conditional ---

    @Test
    void shouldEvaluateConditionalTrue() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("true ? 'yes' : 'no'"), this.context);
        assertThat(result).isEqualTo("yes");
    }

    @Test
    void shouldEvaluateConditionalFalse() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("false ? 'yes' : 'no'"), this.context);
        assertThat(result).isEqualTo("no");
    }

    // --- Empty operator ---

    @Test
    void shouldEvaluateEmptyOnNull() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("empty null"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateEmptyOnEmptyString() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("empty ''"), this.context);
        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    void shouldEvaluateEmptyOnNonEmptyString() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("empty 'hello'"), this.context);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    // --- Variable resolution ---

    @Test
    void shouldResolveVariableFromContext() {
        final var factory = new ExpressionFactoryImpl();
        this.context.getVariableMapper().setVariable("greeting",
            factory.createValueExpression("hello", String.class));
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("greeting"), this.context);
        assertThat(result).isEqualTo("hello");
    }

    // --- Property access ---

    @Test
    void shouldAccessMapEntry() {
        final var factory = new ExpressionFactoryImpl();
        this.context.getVariableMapper().setVariable("data",
            factory.createValueExpression(Map.of("key", "value"), Map.class));
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("data['key']"), this.context);
        assertThat(result).isEqualTo("value");
    }

    @Test
    void shouldAccessListByIndex() {
        final var factory = new ExpressionFactoryImpl();
        this.context.getVariableMapper().setVariable("items",
            factory.createValueExpression(List.of("a", "b", "c"), List.class));
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("items[1]"), this.context);
        assertThat(result).isEqualTo("b");
    }

    // --- Composite ---

    @Test
    void shouldEvaluateCompositeNode() {
        final var factory = new ExpressionFactoryImpl();
        this.context.getVariableMapper().setVariable("name",
            factory.createValueExpression("World", String.class));
        final var node = ELParser.parseComposite("Hello ${name}!");
        final Object result = NodeEvaluator.evaluate(node, this.context);
        assertThat(result).isEqualTo("Hello World!");
    }

    // --- Semicolon sequence ---

    @Test
    void shouldEvaluateSemicolonSequenceReturningRight() {
        final Object result = NodeEvaluator.evaluate(ELParser.parseExpression("1; 2"), this.context);
        assertThat(result).isEqualTo(2L);
    }
}
