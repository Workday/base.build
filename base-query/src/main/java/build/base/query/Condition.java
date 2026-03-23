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

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Provides a mechanism to specify a condition for filtering {@link Object}s of a specific {@link Class} being queried
 * based on values extracted from the {@link Object}s.
 *
 * @param <Q> the type of {@link Class} of {@link Object} being queried
 * @param <V> the type of value
 */
public interface Condition<Q, V> {

    /**
     * Specifies a value that must successfully compare with the extracted value using
     * {@link Objects#equals(Object, Object)}.
     *
     * @param value the value that must successfully compare
     * @return a {@link Terminal} that can be used to obtain the results
     */
    Terminal<Q, ?> isEqualTo(V value);

    /**
     * Specifies a value that must fail to compare with the extracted value using
     * {@link Objects#equals(Object, Object)}.
     *
     * @param value the value that must fail to compare
     * @return a {@link Terminal} that can be used to obtain the results
     */
    Terminal<Q, ?> isNotEqualTo(V value);

    /**
     * Specifies a {@link Predicate} that must successfully match the extracted value.
     *
     * @param predicate the {@link Predicate} that must match
     * @return a {@link Terminal} that can be used to obtain the results
     */
    Terminal<Q, ?> matches(Predicate<? super V> predicate);

    /**
     * Specifies a {@link Predicate} that must fail to match the extracted value.
     *
     * @param predicate the {@link Predicate} that must fail to match
     * @return a {@link Terminal} that can be used to obtain the results
     */
    default Terminal<Q, ?> doesNotMatch(Predicate<? super V> predicate) {
        return matches(predicate.negate());
    }
}
