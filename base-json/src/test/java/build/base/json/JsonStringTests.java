package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonStringTests {

    @Test
    void ofRejectsNull() {
        assertThatNullPointerException().isThrownBy(() -> JsonString.of(null));
    }

    @Test
    void valueRoundTrips() {
        assertThat(JsonString.of("hello").value()).isEqualTo("hello");
    }

    @Test
    void asStringReturnsSelf() {
        var s = JsonString.of("x");
        assertThat(s.asString()).isSameAs(s);
    }

    @Test
    void otherAsXThrow() {
        var s = JsonString.of("x");
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(s::asObject);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(s::asArray);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(s::asNumber);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(s::asBoolean);
    }

    @Test
    void equalityIsStructural() {
        assertThat(JsonString.of("hello")).isEqualTo(JsonString.of("hello"));
        assertThat(JsonString.of("hello")).isNotEqualTo(JsonString.of("world"));
    }
}
