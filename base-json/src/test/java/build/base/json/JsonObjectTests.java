package build.base.json;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonObjectTests {

    @Test
    void ofDefensivelyCopiesInput() {
        var mutable = new HashMap<String, JsonValue>();
        mutable.put("a", JsonString.of("hello"));
        var obj = JsonObject.of(mutable);
        mutable.put("b", JsonString.of("world"));
        assertThat(obj.members()).containsOnlyKeys("a");
    }

    @Test
    void getPresentMemberReturnsValue() {
        var obj = JsonObject.of(Map.of("x", JsonNumber.of(1)));
        assertThat(obj.get("x")).isEqualTo(JsonNumber.of(1));
    }

    @Test
    void getAbsentMemberThrows() {
        var obj = JsonObject.of(Map.of());
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(() -> obj.get("missing"));
    }

    @Test
    void getNullNameThrows() {
        var obj = JsonObject.of(Map.of());
        assertThatNullPointerException().isThrownBy(() -> obj.get(null));
    }

    @Test
    void nullValueInMapThrows() {
        var mutable = new HashMap<String, JsonValue>();
        mutable.put("a", null);
        assertThatNullPointerException().isThrownBy(() -> JsonObject.of(mutable));
    }

    @Test
    void asObjectReturnsSelf() {
        var obj = JsonObject.of(Map.of());
        assertThat(obj.asObject()).isSameAs(obj);
    }

    @Test
    void otherAsXThrow() {
        var obj = JsonObject.of(Map.of());
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(obj::asArray);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(obj::asString);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(obj::asNumber);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(obj::asBoolean);
    }

    @Test
    void equalityIsStructural() {
        var a = JsonObject.of(Map.of("k", JsonString.of("v")));
        var b = JsonObject.of(Map.of("k", JsonString.of("v")));
        assertThat(a).isEqualTo(b);
    }
}
