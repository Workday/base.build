package build.base.expression.eval;

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

import jakarta.el.ELException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeCoercionTests {

    // --- null coercions ---

    @Test
    void shouldCoerceNullToNullForArbitraryObject() {
        final Object result = TypeCoercion.coerce(null, Object.class);
        assertThat(result).isNull();
    }

    @Test
    void shouldCoerceNullToEmptyStringPerSpec() {
        // EL spec 1.23: null coerces to "" for String
        final String result = TypeCoercion.coerce(null, String.class);
        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldCoerceNullToFalseForBoxedBoolean() {
        final Boolean result = TypeCoercion.coerce(null, Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    void shouldCoerceNullToZeroForPrimitiveInt() {
        final Integer result = TypeCoercion.coerce(null, int.class);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void shouldCoerceNullToZeroForPrimitiveLong() {
        final Long result = TypeCoercion.coerce(null, long.class);
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void shouldCoerceNullToZeroForPrimitiveDouble() {
        final Double result = TypeCoercion.coerce(null, double.class);
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void shouldCoerceNullToFalseForPrimitiveBoolean() {
        final Boolean result = TypeCoercion.coerce(null, boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    void shouldCoerceNullToNullReferenceForInteger() {
        final Integer result = TypeCoercion.coerce(null, Integer.class);
        assertThat(result).isNull();
    }

    // --- String coercions ---

    @Test
    void shouldCoerceStringToInteger() {
        final Integer result = TypeCoercion.coerce("42", Integer.class);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void shouldCoerceStringToLong() {
        final Long result = TypeCoercion.coerce("100", Long.class);
        assertThat(result).isEqualTo(100L);
    }

    @Test
    void shouldCoerceStringToDouble() {
        final Double result = TypeCoercion.coerce("3.14", Double.class);
        assertThat(result).isEqualTo(3.14);
    }

    @Test
    void shouldCoerceStringToBoolean() {
        final Boolean trueResult = TypeCoercion.coerce("true", Boolean.class);
        assertThat(trueResult).isTrue();
        final Boolean falseResult = TypeCoercion.coerce("false", Boolean.class);
        assertThat(falseResult).isFalse();
    }

    @Test
    void shouldCoerceEmptyStringToZeroForInteger() {
        // EL spec: empty string coerces to 0 for numeric types
        final Integer result = TypeCoercion.coerce("", Integer.class);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void shouldCoerceEmptyStringToFalseForBoolean() {
        final Boolean result = TypeCoercion.coerce("", Boolean.class);
        assertThat(result).isFalse();
    }

    // --- Number-to-Number coercions ---

    @Test
    void shouldCoerceLongToInteger() {
        final Integer result = TypeCoercion.coerce(42L, Integer.class);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void shouldCoerceIntegerToLong() {
        final Long result = TypeCoercion.coerce(7, Long.class);
        assertThat(result).isEqualTo(7L);
    }

    @Test
    void shouldCoerceIntegerToDouble() {
        final Double result = TypeCoercion.coerce(3, Double.class);
        assertThat(result).isEqualTo(3.0);
    }

    @Test
    void shouldCoerceBigIntegerToLong() {
        final Long result = TypeCoercion.coerce(BigInteger.valueOf(99), Long.class);
        assertThat(result).isEqualTo(99L);
    }

    @Test
    void shouldCoerceLongToBigDecimal() {
        final BigDecimal result = TypeCoercion.coerce(5L, BigDecimal.class);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    // --- toString coercions ---

    @Test
    void shouldCoerceIntegerToString() {
        final String result = TypeCoercion.coerce(42, String.class);
        assertThat(result).isEqualTo("42");
    }

    @Test
    void shouldCoerceBooleanToString() {
        final String result = TypeCoercion.coerce(true, String.class);
        assertThat(result).isEqualTo("true");
    }

    // --- Identity ---

    @Test
    void shouldReturnSameInstanceWhenAlreadyTargetType() {
        final String value = "already a string";
        final String result = TypeCoercion.coerce(value, String.class);
        assertThat(result).isSameAs(value);
    }

    // --- Enum coercion ---

    @Test
    void shouldCoerceStringToEnum() {
        final Thread.State result = TypeCoercion.coerce("RUNNABLE", Thread.State.class);
        assertThat(result).isEqualTo(Thread.State.RUNNABLE);
    }

    @Test
    void shouldThrowOnInvalidEnumName() {
        assertThatThrownBy(() -> TypeCoercion.coerce("NOT_A_STATE", Thread.State.class))
            .isInstanceOf(ELException.class);
    }

    // --- BigDecimal and BigInteger ---

    @Test
    void shouldCoerceStringToBigDecimal() {
        final BigDecimal result = TypeCoercion.coerce("12.34", BigDecimal.class);
        assertThat(result).isEqualByComparingTo(new BigDecimal("12.34"));
    }

    @Test
    void shouldCoerceStringToBigInteger() {
        final BigInteger result = TypeCoercion.coerce("999", BigInteger.class);
        assertThat(result).isEqualTo(BigInteger.valueOf(999));
    }

    // --- Error cases ---

    @Test
    void shouldThrowWhenCoercingUnrelatedTypes() {
        assertThatThrownBy(() -> TypeCoercion.coerce(new Object(), Integer.class))
            .isInstanceOf(ELException.class);
    }
}
