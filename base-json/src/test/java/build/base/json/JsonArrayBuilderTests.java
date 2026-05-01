package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonArrayBuilderTests {

    @Test
    void emptyBuildProducesEmptyArray() {
        var arr = JsonArray.builder().build();
        assertThat(arr.values()).isEmpty();
    }

    @Test
    void addStringWrapsAsJsonString() {
        var arr = JsonArray.builder().add("hello").build();
        assertThat(arr.values()).containsExactly(JsonString.of("hello"));
    }

    @Test
    void addNumberWrapsAsJsonNumber() {
        var arr = JsonArray.builder().add(99).build();
        assertThat(arr.values()).containsExactly(JsonNumber.of(99));
    }

    @Test
    void addBooleanWrapsAsJsonBoolean() {
        var arr = JsonArray.builder().add(false).build();
        assertThat(arr.values()).containsExactly(JsonBoolean.FALSE);
    }

    @Test
    void addNullAddsJsonNull() {
        var arr = JsonArray.builder().addNull().build();
        assertThat(arr.values()).containsExactly(JsonNull.INSTANCE);
    }

    @Test
    void addJsonValueStoresDirectly() {
        var value = JsonString.of("direct");
        var arr = JsonArray.builder().add(value).build();
        assertThat(arr.values().get(0)).isSameAs(value);
    }

    @Test
    void orderPreserved() {
        var arr = JsonArray.builder().add("a").add("b").add("c").build();
        assertThat(arr.values()).containsExactly(
            JsonString.of("a"), JsonString.of("b"), JsonString.of("c"));
    }

    @Test
    void buildIsImmutable() {
        var builder = JsonArray.builder().add("x");
        var first  = builder.build();
        var second = builder.add("y").build();
        assertThat(first.values()).hasSize(1);
        assertThat(second.values()).hasSize(2);
    }

    @Test
    void nullStringValueThrows() {
        assertThatNullPointerException().isThrownBy(() -> JsonArray.builder().add((String) null));
    }

    @Test
    void nullJsonValueThrows() {
        assertThatNullPointerException().isThrownBy(() -> JsonArray.builder().add((JsonValue) null));
    }

    @Test
    void nestedObjects() {
        var arr = JsonArray.builder()
            .add(JsonObject.builder().put("port", "8080").build())
            .build();
        assertThat(arr.values().get(0).asObject().get("port")).isEqualTo(JsonString.of("8080"));
    }

    @Test
    void toJsonStringRoundTrips() {
        var arr = JsonArray.builder().add("a").add(1).add(true).build();
        assertThat(Json.parse(arr.toJsonString()).toJsonString()).isEqualTo(arr.toJsonString());
    }
}
