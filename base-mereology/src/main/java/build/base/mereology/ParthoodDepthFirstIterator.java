package build.base.mereology;

/*-
 * #%L
 * base.build Mereology
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

import build.base.foundation.Capture;
import build.base.foundation.iterator.Iterators;
import build.base.foundation.predicate.Predicates;
import build.base.foundation.tuple.Pair;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * An internal depth-first {@link Iterator} for <i>parts</i> of {@link Composite}s that are assignable to a specified
 * {@link Class}, inclusive of the {@link Composite} itself, providing access to {@link Entity} relationships.
 *
 * @param <T> the type of {@link Object}s to be returned as {@link Entity}-based {@link Iterator} elements
 * @author brian.oliver
 * @since Sep-2025
 */
class ParthoodDepthFirstIterator<T>
    implements Iterator<Entity<T>> {

    /**
     * The {@link Class} of element over which iteration is being performed.
     */
    private final Class<T> elementClass;

    /**
     * The stack of {@link Composite}s and their <i>direct</i> <i>part</i> {@link Iterator}s remaining to
     * be processed.
     */
    private final Deque<Pair<Composite, Iterator<?>>> stack;

    /**
     * The {@link Capture}d next {@link Entity} element to return.
     */
    private final Capture<Entity<T>> nextElement;

    /**
     * The {@link Predicate} to exclude {@link Composite}s during traversal.
     */
    private final Predicate<? super Composite> exclude;

    /**
     * Constructs a {@link DepthFirstIterator} <i>inclusively</i> commencing at the specified {@link Composite},
     * returning only <i>parts</i> that are assignable to the provided {@link Class}.
     *
     * @param composite    the {@link Composite} from which to commence the depth-first traversal
     * @param elementClass the {@link Class} of element to return
     * @param reflexive    {@code true} if the {@link Composite} itself should be included in the iteration,
     *                     otherwise {@code false}
     * @param exclude      the {@link Predicate} to exclude {@link Composite}s during traversal
     */
    ParthoodDepthFirstIterator(final Composite composite,
                               final Class<T> elementClass,
                               final boolean reflexive,
                               final Predicate<? super Composite> exclude) {

        this.elementClass = elementClass;
        this.stack = new LinkedList<>();

        this.exclude = exclude == null
            ? Predicates.never()
            : exclude;

        this.nextElement = reflexive && this.elementClass.isInstance(composite) && !this.exclude.test(composite)
            ? Capture.of(Entity.boundary(this.elementClass.cast(composite)))
            : Capture.empty();

        if (!this.exclude.test(composite)) {
            // the composite.iterator(T) will return the direct parts of the composite assignable to T
            // these may or may not include other composites with are assignable to T
            // however there may be other direct parts that are not assignable to T, that we also have to traverse
            // which we should only return once
            this.stack.push(Pair.of(composite,
                Iterators.distinct(
                    Iterators.concat(composite.iterator(Composite.class), composite.iterator(this.elementClass)))));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasNext() {
        while (this.nextElement.isEmpty() && !this.stack.isEmpty()) {
            final var current = this.stack.peek();
            if (current.second().hasNext()) {
                final var next = current.second().next();

                final var exclude = next instanceof Composite composite
                    && this.exclude.test(composite);

                if (this.elementClass.isInstance(next) && !exclude) {
                    this.nextElement.set(Entity.of((T) next, this.stack.stream().map(Pair::first)));
                }

                if (next instanceof Composite composite && !exclude) {
                    this.stack.push(Pair.of(composite,
                        Iterators.distinct(
                            Iterators.concat(composite.iterator(Composite.class), composite.iterator(this.elementClass)))));
                }
            } else {
                this.stack.pop();
            }
        }

        return this.nextElement.isPresent();
    }

    @Override
    public Entity<T> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements to iterate over");
        }

        return this.nextElement.consume();
    }
}
