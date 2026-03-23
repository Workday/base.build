package build.base.foundation;

/*-
 * #%L
 * base.build Foundation
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

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * A thread-safe <a href="https://en.wikipedia.org/wiki/Memoization">Memoizer</a> that memoizes the results
 * of {@link Function} invocations to avoid repetitive invocations for the same input parameters, including support for
 * {@code null} inputs and results.
 * <p>
 * This implementation provides a simple way to memoize the results of expensive {@link Function} calls using a
 * {@link ConcurrentWeakHashMap}.  Once a result is computed for {@link Function} invocation with a specific
 * input, subsequent invocations with the same input will return the memoized result without re-invocation of the
 * {@link Function}, assuming the result has not been garbage collected, in which case, the result will be recomputed
 * through re-invocation.
 *
 * @param <T> the type of the input parameter
 * @param <R> the type of the result
 * @author reed.vonredwitz
 * @since Jul-2025
 */
public class Memoizer<T, R> {

    /**
     * The {@link ConcurrentWeakHashMap} used to cache results by input
     */
    private final ConcurrentWeakHashMap<Object, R> cache;

    /**
     * {@link ReentrantLock}s by key to ensure thread-safe re-entrant computation.
     */
    private final ConcurrentHashMap<Object, ReentrantLock> computeLocks;

    /**
     * The {@link Function} for which invocations will be memoized.
     */
    private final Function<T, R> function;

    /**
     * The {@link Object} used as the key for {@code null} inputs and invocation results.
     */
    private static final Object NULL_KEY = new Object();

    /**
     * Constructs a new memoizer with the specified {@link Function}.
     *
     * @param function the {@link Function} to memoize; must not be {@code null}
     * @throws IllegalArgumentException should the {@link Function} be {@code null}
     */
    public Memoizer(final Function<T, R> function) {
        Objects.requireNonNull(function, "The memoizing Function must not be null");
        this.cache = new ConcurrentWeakHashMap<>();
        this.computeLocks = new ConcurrentHashMap<>();
        this.function = function;
    }

    /**
     * Computes the result for the given input, using the memoized result if available.
     * <p>
     * If the result for the given input has already been computed and memoized, this method returns
     * the cached result immediately. Otherwise, it computes the result by invoking the {@link Function},
     * caches it, and returns the computed result.
     * </p>
     * <p>
     * This method is thread-safe and can be called concurrently from multiple threads.
     * </p>
     *
     * @param input the input parameter; may be {@code null}
     * @return the computed or cached result
     */
    public R compute(final T input) {
        final var key = input == null ? NULL_KEY : input;

        var result = this.cache.get(key);

        if (result != null) {
            return result;
        }

        // obtain a lock for computing the value for the key
        final var lock = this.computeLocks.computeIfAbsent(key, k -> new ReentrantLock());

        // the following algorithm allows for re-entrant computation of keys with the same hash code or
        // those causing collisions in the hash when attempting to concurrently update the cache.
        // (while it may seem like replace this code with cache.computeIfAbsent would be a good idea, that method
        // may experience hash collisions and compute the value multiple times)
        try {
            lock.lock();

            result = this.cache.get(key);

            if (result != null) {
                return result;
            }

            result = this.function.apply(input);

            this.cache.put(key, result);

            return result;
        } finally {
            lock.unlock();
            this.computeLocks.remove(key);
        }
    }

    /**
     * Clears all memoized results, forcing subsequent calls to re-compute results.
     * <p>
     * This method is thread-safe and can be called concurrently with {@link #compute(Object)}.
     * </p>
     */
    public void clear() {
        this.cache.clear();
    }

    /**
     * Obtains the approximate number of memoized results.
     *
     * @return the approximate number of memoized results
     */
    public int size() {
        return this.cache.size();
    }

    /**
     * Determines if the {@link Memoizer} contains a result for the given input.
     *
     * @param input the input to check
     * @return {@code true} if the cache contains a result for the input, {@code false} otherwise
     */
    public boolean contains(final T input) {
        final var key = input == null ? NULL_KEY : input;
        return this.cache.containsKey(key);
    }
}
