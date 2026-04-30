package build.base.template.processor;

/*-
 * #%L
 * base.build Template Processor
 * %%
 * Copyright (C) 2026 Workday, Inc.
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

import build.base.parsing.AbstractParser;
import build.base.parsing.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class JtParser {

    private static final Pattern QUALIFIED_NAME =
        Pattern.compile("[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*");

    // Matches the body of an import statement: optional "static", qualified name, optional ".*"
    private static final Pattern IMPORT_BODY =
        Pattern.compile("(static\\s+)?[a-zA-Z_$][\\w$]*(\\.[a-zA-Z_$][\\w$]*)*(\\.\\*)?");

    private JtParser() {
    }

    static ParsedTemplate parse(final String content, final String sourceFile) {
        return new JtFileParser(content, sourceFile).run();
    }

    private static void parseBodyLine(final String line, final List<BodyNode> body) {
        final String trimmed = line.trim();
        if (trimmed.startsWith("@include ")) {
            body.add(new BodyNode.Include(trimmed.substring("@include ".length()).trim()));
            return;
        }
        if (trimmed.startsWith("@")) {
            body.add(new BodyNode.CodeLine(trimmed.substring(1).trim()));
            return;
        }
        body.addAll(new TextLineParser(line + "\n").run());
    }

    /** Test-facing entry point: parse a single text line, appending the resulting nodes to {@code body}. */
    static void parseTextLine(final String line, final List<BodyNode> body) {
        body.addAll(new TextLineParser(line).run());
    }

    /**
     * Parses the full content of a {@code .jt} file.
     * <p>
     * No whitespace filter is registered — whitespace is significant in the body and is skipped manually
     * between header tokens.
     */
    private static final class JtFileParser
        extends AbstractParser<ParsedTemplate> {

        private final String sourceFile;

        JtFileParser(final String content, final String sourceFile) {
            super(content);
            this.sourceFile = sourceFile;
        }

        @Override
        protected void registerFilters(final build.base.parsing.Scanner s) {
            // No filters — whitespace is significant in the body.
        }

        @Override
        protected ParsedTemplate parse() {
            String packageName = "";
            final List<String> imports = new ArrayList<>();

            skip();
            if (followsKeyword("package")) {
                consumeKeyword("package");
                skip();
                packageName = scanner.consume(QUALIFIED_NAME);
                skip();
                expect(";");
            }

            skip();
            while (followsKeyword("import")) {
                consumeKeyword("import");
                skip();
                imports.add("import " + scanner.consume(IMPORT_BODY));
                skip();
                expect(";");
                skip();
            }

            if (!followsKeyword("template")) {
                throw new JtParseException(sourceFile + ": missing template declaration");
            }
            consumeKeyword("template");
            skip();
            final String outType = scanner.consume(QUALIFIED_NAME);
            skip();
            final String className = scanner.consume(QUALIFIED_NAME);
            skip();
            final String params = scanner.consumeBalanced('(', ')');

            // Drain the remainder of the template declaration line (e.g. " {")
            while (scanner.hasNext() && scanner.peekChar() != '\n') {
                scanner.consumeChar();
            }
            if (scanner.follows('\n')) {
                scanner.consumeChar();
            }

            // Parse body lines until @end
            final List<BodyNode> body = new ArrayList<>();
            while (scanner.hasNext()) {
                final String line = scanner.consumeUntil("\n");
                if (scanner.follows('\n')) {
                    scanner.consumeChar();
                }
                if (line.trim().equals("@end")) {
                    break;
                }
                parseBodyLine(line, body);
            }

            // Drain any content after @end so AbstractParser's full-consumption check passes
            while (scanner.hasNext()) {
                scanner.consumeChar();
            }

            return new ParsedTemplate(packageName, imports, outType, className, params, body);
        }

        /** Skips whitespace (including newlines) between header tokens. */
        private void skip() {
            scanner.skipWhile(c -> Character.isWhitespace((char) c));
        }

        @Override
        protected RuntimeException translate(final ParseException cause) {
            return new JtParseException(sourceFile + ": " + cause.getMessage());
        }
    }

    /**
     * Parses a single line of the template body, alternating literal text with {@code #{...}} expression
     * interpolations.
     */
    private static final class TextLineParser
        extends AbstractParser<List<BodyNode>> {

        TextLineParser(final String input) {
            super(input);
        }

        @Override
        protected void registerFilters(final build.base.parsing.Scanner s) {
            // No filters — whitespace and other characters in literal template text must be preserved verbatim.
        }

        @Override
        protected List<BodyNode> parse() {
            final List<BodyNode> nodes = new ArrayList<>();
            while (scanner.hasNext()) {
                if (scanner.follows("#{")) {
                    scanner.consume("#");
                    nodes.add(new BodyNode.Expression(scanner.consumeBalanced('{', '}')));
                } else {
                    final String raw = scanner.consumeUntil("#{");
                    if (!raw.isEmpty()) {
                        nodes.add(new BodyNode.RawText(raw));
                    }
                }
            }
            return nodes;
        }

        @Override
        protected RuntimeException translate(final ParseException cause) {
            return new JtParseException("Unclosed expression: " + cause.getMessage());
        }
    }
}
