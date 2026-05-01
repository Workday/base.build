package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonObjectBuilderTests {

    @Test
    void emptyBuildProducesEmptyObject() {
        var obj = JsonObject.builder().build();
        assertThat(obj.members()).isEmpty();
    }

    @Test
    void putStringWrapsAsJsonString() {
        var obj = JsonObject.builder().put("k", "v").build();
        assertThat(obj.get("k")).isEqualTo(JsonString.of("v"));
    }

    @Test
    void putNumberWrapsAsJsonNumber() {
        var obj = JsonObject.builder().put("n", 42).build();
        assertThat(obj.get("n")).isEqualTo(JsonNumber.of(42));
    }

    @Test
    void putBooleanWrapsAsJsonBoolean() {
        var obj = JsonObject.builder().put("b", true).build();
        assertThat(obj.get("b")).isEqualTo(JsonBoolean.TRUE);
    }

    @Test
    void putNullAddsJsonNull() {
        var obj = JsonObject.builder().putNull("x").build();
        assertThat(obj.get("x")).isEqualTo(JsonNull.INSTANCE);
    }

    @Test
    void putJsonValueStoresDirectly() {
        var value = JsonString.of("direct");
        var obj = JsonObject.builder().put("k", value).build();
        assertThat(obj.get("k")).isSameAs(value);
    }

    @Test
    void laterPutOverwritesEarlier() {
        var obj = JsonObject.builder().put("k", "first").put("k", "second").build();
        assertThat(obj.get("k")).isEqualTo(JsonString.of("second"));
    }

    @Test
    void nestedBuilderProducesNestedObject() {
        var obj = JsonObject.builder()
            .put("outer", JsonObject.builder()
                .put("inner", "value")
                .build())
            .build();
        assertThat(obj.get("outer").asObject().get("inner")).isEqualTo(JsonString.of("value"));
    }

    @Test
    void buildIsImmutable() {
        var builder = JsonObject.builder().put("k", "v");
        var first = builder.build();
        var second = builder.put("k2", "v2").build();
        assertThat(first.members()).containsOnlyKeys("k");
        assertThat(second.members()).containsOnlyKeys("k", "k2");
    }

    @Test
    void nullKeyThrows() {
        assertThatNullPointerException().isThrownBy(() -> JsonObject.builder().put(null, "v"));
    }

    @Test
    void nullStringValueThrows() {
        assertThatNullPointerException().isThrownBy(() -> JsonObject.builder().put("k", (String) null));
    }

    @Test
    void nullJsonValueThrows() {
        assertThatNullPointerException().isThrownBy(() -> JsonObject.builder().put("k", (JsonValue) null));
    }

    @Test
    void insertionOrderPreserved() {
        var obj = JsonObject.builder()
            .put("z", "1")
            .put("a", "2")
            .put("m", "3")
            .build();
        assertThat(obj.members().keySet()).containsExactly("z", "a", "m");
    }

    @Test
    void toJsonStringRoundTrips() {
        var obj = JsonObject.builder().put("x", 1).build();
        assertThat(Json.parse(obj.toJsonString()).toJsonString()).isEqualTo(obj.toJsonString());
    }
}
