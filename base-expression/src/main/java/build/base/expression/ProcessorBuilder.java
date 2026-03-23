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

import build.base.foundation.Arrays;
import build.base.foundation.Strings;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.function.Function;

/**
 * A builder of {@link Processor}s.
 *
 * @author brian.oliver
 * @since Dec-2024
 */
public final class ProcessorBuilder {

    /**
     * The {@link Resolvable}s defined for the {@link ProcessorBuilder}.
     */
    private final LinkedHashSet<Resolvable<?>> resolvables;

    /**
     * The {@link PropertyResolver}s defined for the {@link ProcessorBuilder}.
     */
    private final LinkedHashMap<String, PropertyResolver<?>> propertyResolvers;

    /**
     * Constructs a new {@link ProcessorBuilder}.
     */
    ProcessorBuilder() {
        this.resolvables = new LinkedHashSet<>();
        this.propertyResolvers = new LinkedHashMap<>();
    }

    /**
     * Includes the provided {@link Resolvable} value in the context used to evaluate
     * {@link jakarta.el.ValueExpression}s.
     *
     * @param resolvable the {@link Resolvable} to be included in the evaluation context
     * @return this {@link ProcessorBuilder} to permit fluent-method invocation
     */
    public ProcessorBuilder include(final Resolvable<?> resolvable) {
        if (resolvable != null) {
            this.resolvables.add(resolvable);
        }

        return this;
    }

    /**
     * Includes the definition of the specified {@link Variable} to evaluate {@link jakarta.el.ValueExpression}s.
     *
     * @param variable the {@link Variable} to be defined in the evaluation context
     * @return this {@link ProcessorBuilder} to permit fluent-method invocation
     */
    public ProcessorBuilder define(final Variable variable) {
        return include(variable);
    }

    /**
     * Adds a {@link Function} to resolve <i>property values</i> of a specifically named bean occurring in a
     * {@link jakarta.el.ValueExpression}.
     * <p>
     * For example, the {@link Function} to <i>property values</i> in the bean named {@code 'mymap'} the
     * Jakarta Expression {@code mymap['some_property']} or {@code mymap.some_property}, could be defined by:
     * {@code processor.addPropertyResolver("mymap", propertyName -> ...);}
     *
     * @param beanName the bean name
     * @param resolver the {@link PropertyResolver} to resolve a property value
     * @return this {@link ProcessorBuilder} to permit fluent-method invocation
     */
    public ProcessorBuilder addPropertyResolver(final String beanName,
                                                final PropertyResolver<?> resolver) {

        if (!Strings.isEmpty(beanName) && resolver != null) {
            this.propertyResolvers.put(beanName, resolver);
        }

        return this;
    }

    /**
     * Builds a new immutable {@link Processor} based on the {@link ProcessorBuilder}.
     *
     * @return a new immutable {@link Processor}
     */
    public Processor build() {
        final var processor = new DefaultProcessor();

        this.resolvables.stream()
            .sorted(Comparator.comparing(Resolvable::name))
            .forEach(processor::include);

        this.propertyResolvers
            .forEach(processor::addPropertyResolver);

        return processor;
    }

    /**
     * Creates a {@link ProcessorBuilder} with the provided {@link Resolvable}s in the context.
     *
     * @param resolvables the {@link Resolvable}s to be included in the context
     * @return a new {@link ProcessorBuilder} with the provided {@link Resolvable}s included
     */
    public static ProcessorBuilder create(final Resolvable<?>... resolvables) {
        final var builder = new ProcessorBuilder();

        Arrays.stream(resolvables)
            .filter(Objects::nonNull)
            .forEach(builder::include);

        return builder;
    }
}
