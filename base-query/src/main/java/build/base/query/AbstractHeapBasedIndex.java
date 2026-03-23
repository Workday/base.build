package build.base.query;

/*-
 * #%L
 * base.build Query
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

import build.base.foundation.Introspection;
import build.base.foundation.Memoizer;
import build.base.foundation.stream.Streamable;
import build.base.foundation.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An {@code abstract} thread-safe heap-based {@link Index}.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
public abstract class AbstractHeapBasedIndex implements Index {

    /**
     * A constant representing a {@code null} object, used to allow indexing of {@code null} values in
     * {@link Object} index.
     */
    private static final Object NULL_OBJECT = new Object();

    /**
     * The {@link Object}s known to the index by {@link Class}.
     */
    private final ConcurrentHashMap<Class<?>, Set<Object>> objectByClass;

    /**
     * The {@link Indexable} {@code public} {@code static} {@code final} {@link Function}s defined by
     * {@link Field}s per known {@link Class}.
     */
    private final Memoizer<Class<?>, Streamable<Field>> indexableFunctionFieldsByClass;

    /**
     * The indexed {@link Object}s by {@link Class}, {@link Indexable} {@link Function}, and extracted value.
     * <p>
     * The {@link Pair} value holds:
     * <ul>
     *   <li>first — a reverse-index mapping each indexed {@link Object} to the value extracted at index time</li>
     *   <li>second — a forward-index mapping each extracted value to the {@link Set} of {@link Object}s indexed with
     *   that value</li>
     * </ul>
     */
    private final ConcurrentHashMap<
        Class<?>,
        ConcurrentHashMap<
            Function<Object, Object>,
            Pair<ConcurrentHashMap<Object, Object>, ConcurrentHashMap<Object, Set<Object>>>>> objectsByClassIndexableFunctionAndValue;

    /**
     * Constructs an empty {@link AbstractHeapBasedIndex}.
     */
    protected AbstractHeapBasedIndex() {
        this.objectByClass = new ConcurrentHashMap<>();
        this.objectsByClassIndexableFunctionAndValue = new ConcurrentHashMap<>();
        this.indexableFunctionFieldsByClass = new Memoizer<>(AbstractHeapBasedIndex::getIndexableFunctionFields);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void index(final Object object) {
        final var objectClass = object.getClass();

        // index the object itself if it is annotated with @Indexable
        if (Introspection.hasDeclaredAnnotation(objectClass, Indexable.class)) {
            this.objectByClass.compute(objectClass, (_, existing) -> {
                final var objects = existing == null
                    ? ConcurrentHashMap.newKeySet()
                    : existing;

                objects.add(object);
                return objects;
            });
        }

        // index the values produced by public static final @Indexable Function fields
        this.indexableFunctionFieldsByClass
            .compute(objectClass)
            .forEach(field -> this.objectsByClassIndexableFunctionAndValue
                .compute(objectClass, (_, existingFunctions) -> {
                    final var functions = existingFunctions == null
                        ? new ConcurrentHashMap<Function<Object, Object>, Pair<ConcurrentHashMap<Object, Object>, ConcurrentHashMap<Object, Set<Object>>>>()
                        : existingFunctions;

                    try {
                        // attempt to obtain the @Indexable value from the Function
                        field.setAccessible(true);
                        final var function = (Function<Object, Object>) field.get(null);

                        functions.compute(function, (_, existingPair) -> {
                            final var pair = existingPair == null
                                ? Pair.of(new ConcurrentHashMap<>(), new ConcurrentHashMap<Object, Set<Object>>())
                                : existingPair;

                            // extract the value from the queryable using the function
                            try {
                                final var value = function.apply(object);

                                final var indexableValue = value == null
                                    ? NULL_OBJECT // use a constant to represent null values
                                    : value;

                                // record the reverse mapping: object → extracted value
                                pair.first().put(object, indexableValue);

                                // record the forward mapping: extracted value → objects
                                pair.second().compute(indexableValue, (_, existingObjects) -> {
                                    final var objects = existingObjects == null
                                        ? ConcurrentHashMap.newKeySet()
                                        : existingObjects;

                                    objects.add(object);
                                    return objects;
                                });

                                return pair;
                            } catch (final Throwable e) {
                                throw new UnsupportedOperationException("Failed to index [" + objectClass.getName() + "] as the function [" + field.getName() + "] failed to extract a value from the object", e);
                            }
                        });
                    } catch (final IllegalAccessException e) {
                        throw new RuntimeException("Failed to index [" + objectClass.getName() + "] as the field [" + field.getName() + "] could not be accessed", e);
                    }

                    return functions;
                }));
    }

    @Override
    public void unindex(final Object object) {
        final var objectClass = object.getClass();

        // unindex the object itself if it is annotated with @Indexable
        if (Introspection.hasDeclaredAnnotation(objectClass, Indexable.class)) {
            this.objectByClass.compute(objectClass, (_, existing) -> {
                if (existing == null) {
                    return null; // nothing to remove
                } else {
                    existing.remove(object);
                    return existing.isEmpty()
                        ? null
                        : existing; // return null if empty
                }
            });
        }

        // unindex the values produced by public static final @Indexable Function fields
        this.indexableFunctionFieldsByClass
            .compute(objectClass)
            .forEach(field -> this.objectsByClassIndexableFunctionAndValue
                .compute(objectClass, (_, existingFunctions) -> {
                    if (existingFunctions == null) {
                        return null; // nothing to remove
                    }
                    try {
                        // attempt to obtain the @Indexable value from the Function
                        field.setAccessible(true);

                        @SuppressWarnings("unchecked") final var function = (Function<Object, Object>) field.get(null);

                        existingFunctions.compute(function, (_, existingPair) -> {
                            if (existingPair == null) {
                                return null; // nothing to remove
                            } else {
                                // look up the indexed value from the reverse map — no function invocation needed
                                final var indexableValue = existingPair.first().remove(object);

                                if (indexableValue != null) {
                                    existingPair.second().compute(indexableValue, (_, existingQueryables) -> {
                                        if (existingQueryables == null) {
                                            return null; // nothing to remove
                                        } else {
                                            existingQueryables.remove(object);

                                            return existingQueryables.isEmpty()
                                                ? null // return null if empty
                                                : existingQueryables;
                                        }
                                    });
                                }

                                return existingPair.second().isEmpty()
                                    ? null // return null if empty
                                    : existingPair;
                            }
                        });
                    } catch (final IllegalAccessException e) {
                        throw new RuntimeException("Failed to unindex [" + objectClass.getName() + "] as the field [" + field.getName() + "] could not be accessed", e);
                    }

                    return existingFunctions.isEmpty()
                        ? null // return null if empty
                        : existingFunctions; // return the functions if not empty
                }));

    }


    @Override
    public <T> void add(final Class<T> valueClass, final T value) {
        if (valueClass != null && value != null) {
            this.objectByClass.compute(valueClass, (_, existing) -> {
                final var objects = existing == null
                    ? ConcurrentHashMap.newKeySet()
                    : existing;

                objects.add(value);
                return objects;
            });
        }
    }

    @Override
    public <T> void remove(final Class<T> valueClass, final T value) {
        if (valueClass != null && value != null) {
            this.objectByClass.compute(valueClass, (_, existing) -> {
                if (existing == null) {
                    return null; // nothing to remove
                } else {
                    existing.remove(value);
                    return existing.isEmpty()
                        ? null
                        : existing; // return null if empty
                }
            });
        }
    }

    /**
     * Obtains an {@link Iterator} over the {@link Object}s of the specified {@link Class} that may be used to
     * match the query should the index not contain a match.
     *
     * @param <T>            the type of {@link Object}
     * @param matchableClass the {@link Class} of {@link Object}
     * @param scope          the {@link Scope} for obtaining {@link Object}s to query
     * @return an {@link Iterator} over the {@link Object}s of the specified {@link Class}
     */
    protected abstract <T> Iterator<T> traverse(Class<T> matchableClass,
                                                Scope scope);

    /**
     * Obtains an {@link Iterator} over {@link Object}s that may be used to match the query should the index not
     * contain a match.
     *
     * @param scope the {@link Scope} for obtaining {@link Object}s to query
     * @return an {@link Iterator} over the {@link Object}s
     */
    protected abstract Iterator<Object> traverse(Scope scope);

    @Override
    public <Q> Match<Q> match(final Class<Q> matchableClass) {
        return new Query<>(this, matchableClass);
    }

    /**
     * Obtains the {@code public static final} {@link Function} {@link Field}s that are annotated as {@link Indexable}
     * for the specified {@link Class}.
     *
     * @param indexableClass the {@link Class} of queryable
     * @return the {@link Streamable} of {@link Field}s that are annotated with {@link Indexable}
     */
    protected static Streamable<Field> getIndexableFunctionFields(final Class<?> indexableClass) {
        return Streamable.of(Introspection.getAllDeclaredFields(indexableClass)
            .filter(field -> field.getAnnotation(Indexable.class) != null
                && Modifier.isPublic(field.getModifiers())
                && Modifier.isStatic(field.getModifiers())
                && Modifier.isFinal(field.getModifiers())
                && Function.class.isAssignableFrom(field.getType())));
    }

    /**
     * An internal {@link Match} implementation.
     *
     * @param <Q> the type of {@link Object} being queried
     */
    private class Query<Q>
        implements Match<Q> {

        /**
         * The {@link AbstractHeapBasedIndex} from which this {@link Query} was created.
         */
        private final AbstractHeapBasedIndex index;

        /**
         * The {@link Class} of the {@link Object} being selected.
         */
        private final Class<Q> objectClass;

        /**
         * The {@link Scope} for querying.
         */
        private Scope scope;

        /**
         * Constructs an {@link Query} for the specified {@link Class} of {@link Object}.
         *
         * @param index       the {@link AbstractHeapBasedIndex} from which this {@link Query} was created
         * @param objectClass the {@link Class} of {@link Object} to select
         */
        Query(final AbstractHeapBasedIndex index,
              final Class<Q> objectClass) {

            this.index = Objects.requireNonNull(index, "The Index must not be null");
            this.objectClass = Objects.requireNonNull(objectClass, "The Object class must not be null");
            this.scope = Scope.Direct;
        }

        @Override
        public Query<Q> scope(final Scope scope) {
            this.scope = scope == null ? Scope.Direct : scope;
            return this;
        }

        /**
         * Obtain the {@link Stream} of {@link Object}s of the specified {@link Class}.
         *
         * @param scope the {@link Scope} for obtaining {@link Object}s to query
         * @return the {@link Stream} of {@link Object}s
         */
        Stream<Q> stream(final Scope scope) {
            // attempt to use the indexed Objects by Class
            final var objects = AbstractHeapBasedIndex.this.objectByClass.get(this.objectClass);

            if (objects != null) {
                return objects.stream()
                    .map(this.objectClass::cast);
            }

            final var unindexedObjectsByClass = this.index.traverse(this.objectClass, scope);

            if (unindexedObjectsByClass != null && unindexedObjectsByClass.hasNext()) {
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize(unindexedObjectsByClass, 0), false);
            }

            final var unindexedObjects = this.index.traverse(scope);

            return unindexedObjects == null || !unindexedObjects.hasNext()
                ? Stream.empty()
                : StreamSupport.stream(Spliterators.spliteratorUnknownSize(unindexedObjects, 0), false)
                .filter(this.objectClass::isInstance)
                .map(this.objectClass::cast);
        }

        @Override
        public <V> Condition<Q, V> where(final Function<? super Q, V> function) {
            return new Where<>(this, function);
        }

        @Override
        public Stream<Q> findAll() {
            return stream(this.scope);
        }
    }


    /**
     * An {@link Condition} implementation to extract values from an {@link Object}.
     *
     * @param <Q> the type of {@link Object}
     * @param <V> the type of value
     */
    private class Where<Q, V>
        implements Condition<Q, V> {

        /**
         * The {@link Query} that created this {@link Where}.
         */
        private final Query<Q> select;

        /**
         * The {@link Function} to extract a value from a {@link Object}.
         */
        private final Function<? super Q, V> function;

        /**
         * Constructs an {@link Where} with the specified function to extract values from the {@link Object}.
         *
         * @param function the extractor {@link Function}
         */
        Where(final Query<Q> select,
              final Function<? super Q, V> function) {

            this.select = Objects.requireNonNull(select, "The Select must not be null");
            this.function = Objects.requireNonNull(function, "The value extractor function must not be null");
        }

        /**
         * Ensures that the value is not {@code null} by replacing it with a constant {@link #NULL_OBJECT} if it is.
         *
         * @param value the value
         * @return the non-{@code null} value, or {@link #NULL_OBJECT} if the value is {@code null}
         */
        @SuppressWarnings("unchecked")
        private V nonNull(final V value) {
            return value == null ? (V) NULL_OBJECT : value;
        }

        @Override
        public IsEqualTo<Q, V> isEqualTo(final V value) {
            return new IsEqualTo<>(this, nonNull(value));
        }

        @Override
        public IsNotEqualTo<Q, V> isNotEqualTo(final V value) {
            return new IsNotEqualTo<>(this, nonNull(value));
        }

        @Override
        public Terminal<Q, Matches<Q, V>> matches(final Predicate<? super V> predicate) {
            return new Matches<>(this, predicate);
        }
    }


    /**
     * A {@link Terminal} implementation for checking if a value is equal to a specified value.
     *
     * @param <Q> the type of {@link Object}
     * @param <V> the type of value
     */
    private class IsEqualTo<Q, V>
        implements Terminal<Q, IsEqualTo<Q, V>> {

        /**
         * The {@link Where} condition defining the possibly {@link Indexable} {@link Function} to extract the value.
         */
        private final Where<Q, V> where;

        /**
         * The non-{@code null} value to compare against.
         */
        private final V value;

        /**
         * The {@link Scope} for querying.
         */
        private Scope scope;

        /**
         * Constructs an {@link IsEqualTo} condition with the specified {@link Where} and value.
         *
         * @param where the {@link Where} condition
         * @param value the value to compare against
         */
        IsEqualTo(final Where<Q, V> where,
                  final V value) {

            this.where = Objects.requireNonNull(where, "The Where must not be null");
            this.value = Objects.requireNonNull(value, "The Value must not be null");
            this.scope = Scope.Direct;
        }

        @Override
        public IsEqualTo<Q, V> scope(final Scope scope) {
            this.scope = scope == null ? Scope.Direct : scope;
            return this;
        }

        @Override
        public Stream<Q> findAll() {
            // first attempt to use the function indexed values
            final var objectsByFunction = AbstractHeapBasedIndex.this
                .objectsByClassIndexableFunctionAndValue.get(this.where.select.objectClass);

            if (objectsByFunction != null) {
                final var pair = objectsByFunction.get(this.where.function);

                if (pair != null) {
                    final var objects = pair.second().get(this.value);
                    return objects == null || objects.isEmpty()
                        ? Stream.empty()
                        : objects.stream()
                        .map(this.where.select.objectClass::cast);
                }
            }

            // failing that, use the objects provided by the query
            return this.where.select.stream(this.scope)
                .filter(queryable -> Objects.equals(this.where.function.apply(queryable), this.value));
        }
    }


    /**
     * A {@link Terminal} implementation for checking if a value is not equal to a specified value.
     *
     * @param <Q> the type of {@link Object}
     * @param <V> the type of value
     */
    private class IsNotEqualTo<Q, V>
        implements Terminal<Q, IsNotEqualTo<Q, V>> {

        /**
         * The {@link Where} condition defining the possibly {@link Indexable} {@link Function} to extract the value.
         */
        private final Where<Q, V> where;

        /**
         * The non-{@code null} value to compare against.
         */
        private final V value;

        /**
         * The {@link Scope} for querying.
         */
        private Scope scope;

        /**
         * Constructs an {@link IsEqualTo} condition with the specified {@link Where} and value.
         *
         * @param where the {@link Where} condition
         * @param value the value to compare against
         */
        IsNotEqualTo(final Where<Q, V> where,
                     final V value) {

            this.where = Objects.requireNonNull(where, "The Where must not be null");
            this.value = Objects.requireNonNull(value, "The Value must not be null");
            this.scope = Scope.Direct;
        }

        @Override
        public IsNotEqualTo<Q, V> scope(final Scope scope) {
            this.scope = scope == null ? Scope.Direct : scope;
            return this;
        }

        @Override
        public Stream<Q> findAll() {
            // first attempt to use the function indexed values
            final var objectsByFunction = AbstractHeapBasedIndex.this
                .objectsByClassIndexableFunctionAndValue.get(this.where.select.objectClass);

            if (objectsByFunction != null) {
                final var pair = objectsByFunction.get(this.where.function);

                if (pair != null) {
                    return pair.second().entrySet()
                        .stream()
                        .filter(entry -> !Objects.equals(entry.getKey(), this.value))
                        .flatMap(entry -> entry.getValue().stream())
                        .map(this.where.select.objectClass::cast);
                }
            }

            // failing that, use the objects provided by the query
            return this.where.select.stream(this.scope)
                .filter(queryable ->
                    !Objects.equals(this.where.nonNull(this.where.function.apply(queryable)), this.value));
        }
    }

    /**
     * A {@link Terminal} implementation for checking if an extracted value matches the specified {@link Predicate}.
     *
     * @param <Q> the type of {@link Object}
     * @param <V> the type of {@link Predicate} value
     */
    private class Matches<Q, V>
        implements Terminal<Q, Matches<Q, V>> {

        /**
         * The {@link Where} condition defining the possibly {@link Indexable} {@link Function} to extract the value.
         */
        private final Where<Q, V> where;

        /**
         * The {@link Predicate} to compare match.
         */
        private final Predicate<? super V> predicate;

        /**
         * The {@link Scope} for querying.
         */
        private Scope scope;

        /**
         * Constructs an {@link Matches} condition with the specified {@link Where} and {@link Predicate}.
         *
         * @param where     the {@link Where} condition
         * @param predicate the {@link Predicate}
         */
        Matches(final Where<Q, V> where,
                final Predicate<? super V> predicate) {

            this.where = Objects.requireNonNull(where, "The Where must not be null");
            this.predicate = Objects.requireNonNull(predicate, "The Predicate must not be null");
            this.scope = Scope.Direct;
        }

        @Override
        public Matches<Q, V> scope(final Scope scope) {
            this.scope = scope == null ? Scope.Direct : scope;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Stream<Q> findAll() {
            // first attempt to use the function indexed values
            final var objectsByFunction = AbstractHeapBasedIndex.this
                .objectsByClassIndexableFunctionAndValue.get(this.where.select.objectClass);

            if (objectsByFunction != null) {
                final var pair = objectsByFunction.get(this.where.function);

                if (pair != null) {
                    return pair.second().entrySet().stream()
                        .filter(entry -> this.predicate
                            .test((V) ((entry.getKey() == NULL_OBJECT)
                                ? null // convert the constant back to null
                                : entry.getKey())))
                        .flatMap(entry -> entry.getValue().stream())
                        .map(this.where.select.objectClass::cast);
                }
            }

            // failing that, use the objects provided by the query
            return this.where.select.stream(this.scope)
                .filter(queryable -> this.predicate.test(this.where.function.apply(queryable)));
        }
    }
}
