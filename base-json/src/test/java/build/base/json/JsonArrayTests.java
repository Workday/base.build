package build.base.json;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonArrayTests {

    @Test
    void ofDefensivelyCopiesInput() {
        var mutable = new ArrayList<JsonValue>();
        mutable.add(JsonString.of("a"));
        var arr = JsonArray.of(mutable);
        mutable.add(JsonString.of("b"));
        assertThat(arr.values()).hasSize(1);
    }

    @Test
    void elementAtValidIndexReturnsValue() {
        var arr = JsonArray.of(List.of(JsonNumber.of(7)));
        assertThat(arr.element(0)).isEqualTo(JsonNumber.of(7));
    }

    @Test
    void elementAtNegativeIndexThrows() {
        var arr = JsonArray.of(List.of(JsonNull.INSTANCE));
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(() -> arr.element(-1));
    }

    @Test
    void elementAtOutOfBoundsIndexThrows() {
        var arr = JsonArray.of(List.of(JsonNull.INSTANCE));
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(() -> arr.element(1));
    }

    @Test
    void nullElementInListThrows() {
        var mutable = new ArrayList<JsonValue>();
        mutable.add(null);
        assertThatNullPointerException().isThrownBy(() -> JsonArray.of(mutable));
    }

    @Test
    void asArrayReturnsSelf() {
        var arr = JsonArray.of(List.of());
        assertThat(arr.asArray()).isSameAs(arr);
    }

    @Test
    void otherAsXThrow() {
        var arr = JsonArray.of(List.of());
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(arr::asObject);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(arr::asString);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(arr::asNumber);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(arr::asBoolean);
    }

    @Test
    void equalityIsStructural() {
        var a = JsonArray.of(List.of(JsonString.of("x")));
        var b = JsonArray.of(List.of(JsonString.of("x")));
        assertThat(a).isEqualTo(b);
    }
}
