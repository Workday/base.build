package build.base.expression;

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

import build.base.configuration.Configuration;
import build.base.foundation.Strings;
import jakarta.el.ELProcessor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * A default implementation of {@link Processor}.
 *
 * @author graeme.campbell
 * @author brian.oliver
 * @see <a href="https://docs.oracle.com/javaee/7/tutorial/jsf-el.htm">Java Expression Language Documentation</a>
 * @since Jan-2019
 */
public class DefaultProcessor
    implements Processor {

    /**
     * The {@link Pattern} for embedding a {@link jakarta.el.ValueExpression} in a {@link String}.
     */
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(?=([^}]*)})");

    /**
     * The {@link ELProcessor} used to store context and evaluate {@link jakarta.el.ValueExpression}s.
     */
    private final ELProcessor processor;

    /**
     * Constructs the {@link build.base.expression.DefaultProcessor} without any {@link Resolvable}s.
     */
    DefaultProcessor() {
        this.processor = new ELProcessor();

        this.processor.getELManager().getELContext()
            .addELResolver(new DefaultResolver());
    }

    /**
     * Constructs the {@link Processor} with a {@link Configuration} to be included in the context.
     *
     * @param configuration the {@link Configuration} to include in the context
     */
    DefaultProcessor(final Configuration configuration) {
        this();

        if (configuration != null) {
            configuration.stream(Resolvable.class)
                .sorted(Comparator.comparing(Resolvable::name))
                .forEach(this::include);
        }
    }

    /**
     * Constructs the {@link build.base.expression.DefaultProcessor} with the specified {@link Resolvable}s included in the context.
     *
     * @param resolvables the {@link Resolvable}s to be included in the context
     */
    DefaultProcessor(final Resolvable<?>... resolvables) {
        this();

        Arrays.stream(resolvables)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Resolvable::name))
            .forEach(this::include);
    }

    void include(final Resolvable<?> resolvable) {

        final var rawLevels = resolvable.name().split("\\.");
        IntStream.range(1, rawLevels.length)
            .boxed()
            .map(index -> String.join(".", Arrays.copyOfRange(rawLevels, 0, index)))
            .forEach(level -> {
                if (this.processor.getValue(level, Object.class) == null) {
                    this.processor.setValue(level, new HashMap<>());
                }
            });

        this.processor.setValue(resolvable.name(), resolvable.value());
    }

    void addPropertyResolver(final String beanName, final PropertyResolver<?> resolver) {
        if (!Strings.isEmpty(beanName) && resolver != null) {
            this.processor.defineBean(beanName, resolver);
        }
    }

    @Override
    public String replace(final String expression) {
        // Credit goes to justin.head for the regex/matcher design in workflow-config
        final var builder = new StringBuilder();

        final var matcher = PATTERN.matcher(expression);

        int lastMatch = 0;
        while (matcher.find()) {
            final String prior = expression.substring(lastMatch, matcher.start());
            final String subExpression = matcher.group(1);

            builder.append(prior);
            builder.append(this.processor.getValue(subExpression, Object.class));

            lastMatch = matcher.end() + subExpression.length() + 1;
        }

        if (lastMatch < expression.length()) {
            builder.append(expression.substring(lastMatch));
        }

        return builder.toString();
    }

    @Override
    public <T> T evaluate(final String expression, final Class<T> resultClass) {
        return this.processor.getValue(expression, resultClass);
    }
}
