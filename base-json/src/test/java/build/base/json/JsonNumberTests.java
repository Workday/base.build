package build.base.json;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonNumberTests {

    @Test
    void ofRejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> JsonNumber.of(null));
    }

    @Test
    void integerRoundTrips() {
        assertThat(JsonNumber.of(42).toNumber()).isEqualTo(42);
    }

    @Test
    void longRoundTrips() {
        assertThat(JsonNumber.of(Long.MAX_VALUE).toNumber()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void doubleRoundTrips() {
        assertThat(JsonNumber.of(3.14).toNumber()).isEqualTo(3.14);
    }

    @Test
    void bigIntegerRoundTrips() {
        var big = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO);
        assertThat(JsonNumber.of(big).toNumber()).isEqualTo(big);
    }

    @Test
    void bigDecimalRoundTrips() {
        var bd = new BigDecimal("123456789.987654321");
        assertThat(JsonNumber.of(bd).toNumber()).isEqualTo(bd);
    }

    @Test
    void equalityByRuntimeType() {
        assertThat(JsonNumber.of(1)).isNotEqualTo(JsonNumber.of(1L));
        assertThat(JsonNumber.of(BigDecimal.valueOf(1))).isNotEqualTo(JsonNumber.of(1));
    }

    @Test
    void doubleNaNIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> JsonNumber.of(Double.NaN));
    }

    @Test
    void doubleInfinityIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> JsonNumber.of(Double.POSITIVE_INFINITY));
        assertThatIllegalArgumentException().isThrownBy(() -> JsonNumber.of(Double.NEGATIVE_INFINITY));
    }

    @Test
    void floatNaNIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> JsonNumber.of(Float.NaN));
    }

    @Test
    void floatInfinityIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> JsonNumber.of(Float.POSITIVE_INFINITY));
        assertThatIllegalArgumentException().isThrownBy(() -> JsonNumber.of(Float.NEGATIVE_INFINITY));
    }

    @Test
    void asNumberReturnsSelf() {
        var n = JsonNumber.of(1);
        assertThat(n.asNumber()).isSameAs(n);
    }

    @Test
    void otherAsXThrow() {
        var n = JsonNumber.of(1);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(n::asObject);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(n::asArray);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(n::asString);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(n::asBoolean);
    }
}
