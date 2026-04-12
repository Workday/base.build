package build.base.expression.parser;

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

import build.base.expression.ast.BinaryOpNode;
import build.base.expression.ast.CompositeNode;
import build.base.expression.ast.ConditionalNode;
import build.base.expression.ast.IdentifierNode;
import build.base.expression.ast.LambdaNode;
import build.base.expression.ast.LiteralNode;
import build.base.expression.ast.MethodCallNode;
import build.base.expression.ast.PropertyAccessNode;
import build.base.expression.ast.UnaryOpNode;
import jakarta.el.ELException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ELParserTests {

    @Test
    void shouldParseLiteralString() {
        assertThat(ELParser.parseExpression("'hello'"))
            .isEqualTo(new LiteralNode("hello"));
    }

    @Test
    void shouldParseLiteralInteger() {
        assertThat(ELParser.parseExpression("42"))
            .isEqualTo(new LiteralNode(42L));
    }

    @Test
    void shouldParseLiteralTrue() {
        assertThat(ELParser.parseExpression("true"))
            .isEqualTo(new LiteralNode(Boolean.TRUE));
    }

    @Test
    void shouldParseLiteralFalse() {
        assertThat(ELParser.parseExpression("false"))
            .isEqualTo(new LiteralNode(Boolean.FALSE));
    }

    @Test
    void shouldParseLiteralNull() {
        assertThat(ELParser.parseExpression("null"))
            .isEqualTo(new LiteralNode(null));
    }

    @Test
    void shouldParseIdentifier() {
        assertThat(ELParser.parseExpression("myVar"))
            .isEqualTo(new IdentifierNode("myVar"));
    }

    @Test
    void shouldParseAddition() {
        assertThat(ELParser.parseExpression("1 + 2"))
            .isEqualTo(new BinaryOpNode(new LiteralNode(1L), "+", new LiteralNode(2L)));
    }

    @Test
    void shouldParseDotAccess() {
        assertThat(ELParser.parseExpression("foo.bar"))
            .isEqualTo(new PropertyAccessNode(
                new IdentifierNode("foo"), new LiteralNode("bar"), false));
    }

    @Test
    void shouldParseBracketAccess() {
        assertThat(ELParser.parseExpression("foo['bar']"))
            .isEqualTo(new PropertyAccessNode(
                new IdentifierNode("foo"), new LiteralNode("bar"), true));
    }

    @Test
    void shouldParseMethodCall() {
        assertThat(ELParser.parseExpression("foo.size()"))
            .isEqualTo(new MethodCallNode(new IdentifierNode("foo"), "size", List.of()));
    }

    @Test
    void shouldParseConditional() {
        assertThat(ELParser.parseExpression("a ? b : c"))
            .isEqualTo(new ConditionalNode(
                new IdentifierNode("a"),
                new IdentifierNode("b"),
                new IdentifierNode("c")));
    }

    @Test
    void shouldParseLambda() {
        assertThat(ELParser.parseExpression("x -> x + 1"))
            .isEqualTo(new LambdaNode(
                List.of("x"),
                new BinaryOpNode(new IdentifierNode("x"), "+", new LiteralNode(1L))));
    }

    @Test
    void shouldParseCompositeString() {
        assertThat(ELParser.parseComposite("Hello ${name}!"))
            .isEqualTo(new CompositeNode(List.of(
                new LiteralNode("Hello "),
                new IdentifierNode("name"),
                new LiteralNode("!"))));
    }

    @Test
    void shouldParseDollarExpression() {
        assertThat(ELParser.parseFull("${name}"))
            .isEqualTo(new IdentifierNode("name"));
    }

    @Test
    void shouldThrowOnUnclosedExpression() {
        assertThatThrownBy(() -> ELParser.parseFull("${name"))
            .isInstanceOf(ELException.class);
    }

    @Test
    void shouldRespectOperatorPrecedence() {
        // 1 + 2 * 3 should parse as 1 + (2 * 3)
        assertThat(ELParser.parseExpression("1 + 2 * 3"))
            .isEqualTo(new BinaryOpNode(
                new LiteralNode(1L),
                "+",
                new BinaryOpNode(new LiteralNode(2L), "*", new LiteralNode(3L))));
    }

    @Test
    void shouldParseUnaryNot() {
        assertThat(ELParser.parseExpression("!true"))
            .isEqualTo(new UnaryOpNode("!", new LiteralNode(Boolean.TRUE)));
    }

    @Test
    void shouldParseKeywordOperator() {
        assertThat(ELParser.parseExpression("a eq b"))
            .isEqualTo(new BinaryOpNode(new IdentifierNode("a"), "==", new IdentifierNode("b")));
    }
}
