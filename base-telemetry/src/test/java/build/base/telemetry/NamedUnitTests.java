package build.base.telemetry;

/*-
 * #%L
 * base.build Telemetry
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link NamedUnit}.
 *
 * @author brian.oliver
 * @since Feb-2026
 */
class NamedUnitTests {

    /**
     * Ensure a {@link NamedUnit} can be created with distinct singular and plural names.
     */
    @Test
    void shouldCreateNamedUnitWithSingularAndPluralNames() {
        final var unit = NamedUnit.of("item", "items");

        assertThat(unit.singular())
            .isEqualTo("item");

        assertThat(unit.plural())
            .isEqualTo("items");
    }

    /**
     * Ensure a {@link NamedUnit} can be created with the same singular and plural name.
     */
    @Test
    void shouldCreateNamedUnitWithSameNameForSingularAndPlural() {
        final var unit = NamedUnit.of("MB");

        assertThat(unit.singular())
            .isEqualTo("MB");

        assertThat(unit.plural())
            .isEqualTo("MB");
    }

    /**
     * Ensure a {@link NamedUnit} returns the singular name when the count is one.
     */
    @Test
    void shouldReturnSingularNameWhenCountIsOne() {
        final var unit = NamedUnit.of("item", "items");

        assertThat(unit.nameFor(1))
            .isEqualTo("item");
    }

    /**
     * Ensure a {@link NamedUnit} returns the plural name when the count is greater than one.
     */
    @Test
    void shouldReturnPluralNameWhenCountIsGreaterThanOne() {
        final var unit = NamedUnit.of("item", "items");

        assertThat(unit.nameFor(2))
            .isEqualTo("items");
    }

    /**
     * Ensure a {@link NamedUnit} returns the plural name when the count is zero.
     */
    @Test
    void shouldReturnPluralNameWhenCountIsZero() {
        final var unit = NamedUnit.of("item", "items");

        assertThat(unit.nameFor(0))
            .isEqualTo("items");
    }

    /**
     * Ensure a {@link NamedUnit} with no name is not empty when created with {@link NamedUnit#of(String, String)}.
     */
    @Test
    void shouldNotBeEmptyWhenCreatedWithNames() {
        final var unit = NamedUnit.of("item", "items");

        assertThat(unit.isEmpty())
            .isFalse();
    }

    /**
     * Ensure {@link NamedUnit#none()} is empty.
     */
    @Test
    void shouldBeEmptyWhenNone() {
        assertThat(NamedUnit.none().isEmpty())
            .isTrue();
    }

    /**
     * Ensure {@link NamedUnit#none()} returns an empty {@link String} from {@link NamedUnit#nameFor(int)}.
     */
    @Test
    void shouldReturnEmptyStringFromNameForWhenNone() {
        assertThat(NamedUnit.none().nameFor(1))
            .isEmpty();

        assertThat(NamedUnit.none().nameFor(5))
            .isEmpty();
    }

    /**
     * Ensure {@link NamedUnit#none()} always returns the same instance.
     */
    @Test
    void shouldAlwaysReturnTheSameInstanceForNone() {
        assertThat(NamedUnit.none())
            .isSameAs(NamedUnit.none());
    }

    /**
     * Ensure a {@link NullPointerException} is thrown when the singular name is {@code null}.
     */
    @Test
    void shouldThrowWhenSingularNameIsNull() {
        assertThatNullPointerException()
            .isThrownBy(() -> NamedUnit.of(null, "items"));
    }

    /**
     * Ensure a {@link NullPointerException} is thrown when the plural name is {@code null}.
     */
    @Test
    void shouldThrowWhenPluralNameIsNull() {
        assertThatNullPointerException()
            .isThrownBy(() -> NamedUnit.of("item", null));
    }

    /**
     * Ensure an {@link IllegalArgumentException} is thrown when the singular name is blank.
     */
    @Test
    void shouldThrowWhenSingularNameIsBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> NamedUnit.of("  ", "items"));
    }

    /**
     * Ensure an {@link IllegalArgumentException} is thrown when the plural name is blank.
     */
    @Test
    void shouldThrowWhenPluralNameIsBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> NamedUnit.of("item", "  "));
    }
}
