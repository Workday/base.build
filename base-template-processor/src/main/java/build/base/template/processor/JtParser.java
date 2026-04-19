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

import build.base.parsing.Filter;
import build.base.parsing.Scanner;

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

    static ParsedTemplate parse(final List<String> lines,
                                final String sourceFile) {
        final List<String> headerLines = new ArrayList<>();
        final List<String> bodyLines = new ArrayList<>();
        boolean inBody = false;

        for (final String line : lines) {
            final String trimmed = line.trim();
            if (!inBody) {
                if (!trimmed.isEmpty()) {
                    headerLines.add(trimmed);
                }
                if (trimmed.startsWith("template ")) {
                    inBody = true;
                }
            } else if (!trimmed.equals("}")) {
                bodyLines.add(line);
            }
        }

        String packageName = "";
        final List<String> imports = new ArrayList<>();
        String outType = null;
        String className = null;
        String params = null;

        try (var scanner = new Scanner(String.join("\n", headerLines))) {
            scanner.register(Filter.WHITESPACE);
            while (scanner.hasNext()) {
                if (scanner.follows("package")) {
                    scanner.skip("package");
                    packageName = scanner.consume(QUALIFIED_NAME);
                    scanner.skip(";");
                } else if (scanner.follows("import")) {
                    scanner.skip("import");
                    imports.add("import " + scanner.consume(IMPORT_BODY));
                    scanner.skip(";");
                } else if (scanner.follows("template")) {
                    scanner.skip("template");
                    outType = scanner.consume(QUALIFIED_NAME);
                    className = scanner.consume(QUALIFIED_NAME);
                    params = consumeParams(scanner);
                    break;
                } else {
                    break;
                }
            }
        } catch (final Exception e) {
            throw new JtParseException(sourceFile + ": " + e.getMessage());
        }

        if (className == null) {
            throw new JtParseException(sourceFile + ": missing template declaration");
        }

        final List<BodyNode> body = new ArrayList<>();
        for (final String line : bodyLines) {
            parseBodyLine(line, body);
        }

        return new ParsedTemplate(packageName, imports, outType, className, params, body);
    }

    private static String consumeParams(final Scanner scanner) {
        scanner.consume("(");
        final var sb = new StringBuilder();
        int depth = 1;
        while (depth > 0) {
            final String ch = scanner.consume(1);
            if ("(".equals(ch)) {
                depth++;
                sb.append(ch);
            } else if (")".equals(ch)) {
                depth--;
                if (depth > 0) {
                    sb.append(ch);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static void parseBodyLine(final String line,
                                      final List<BodyNode> body) {
        final String trimmed = line.trim();
        if (trimmed.startsWith("@include ")) {
            body.add(new BodyNode.Include(trimmed.substring("@include ".length()).trim()));
            return;
        }
        if (trimmed.startsWith("@")) {
            body.add(new BodyNode.CodeLine(trimmed.substring(1).trim()));
            return;
        }
        parseTextLine(line + "\n", body);
    }

    static void parseTextLine(final String line,
                              final List<BodyNode> body) {
        try (var scanner = new Scanner(line)) {
            while (scanner.hasNext()) {
                if (scanner.follows("#{")) {
                    body.add(new BodyNode.Expression(consumeExpression(scanner)));
                } else {
                    final String raw = scanner.consumeUntil("#{");
                    if (!raw.isEmpty()) {
                        body.add(new BodyNode.RawText(raw));
                    }
                }
            }
        } catch (final JtParseException e) {
            throw e;
        } catch (final Exception e) {
            throw new JtParseException("Unclosed expression in: " + line.trim());
        }
    }

    private static String consumeExpression(final Scanner scanner) {
        scanner.consume("#{");
        final var sb = new StringBuilder();
        int depth = 1;
        boolean inString = false;
        char stringChar = 0;
        boolean escape = false;
        while (depth > 0) {
            final char c = scanner.consume(1).charAt(0);
            if (escape) {
                escape = false;
                sb.append(c);
                continue;
            }
            if (inString) {
                sb.append(c);
                if (c == '\\') {
                    escape = true;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                sb.append(c);
            } else if (c == '{') {
                depth++;
                sb.append(c);
            } else if (c == '}') {
                depth--;
                if (depth > 0) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
