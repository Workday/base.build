package build.base.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JsonValueTests {

    @Test
    void patternMatchIsExhaustive() {
        List<JsonValue> values = List.of(
            JsonString.of("s"),
            JsonNumber.of(1),
            JsonBoolean.TRUE,
            JsonObject.of(Map.of()),
            JsonArray.of(List.of()),
            JsonNull.INSTANCE
        );
        for (var v : values) {
            var description = switch (v) {
                case JsonString s -> "string";
                case JsonNumber n -> "number";
                case JsonBoolean b -> "boolean";
                case JsonObject o -> "object";
                case JsonArray a -> "array";
                case JsonNull _ -> "null";
            };
            assertThat(description).isNotNull();
        }
    }

    @Test
    void asXErrorMessageIncludesActualType() {
        var ex = assertThatExceptionOfType(JsonAssertionException.class)
            .isThrownBy(() -> JsonString.of("x").asObject())
            .actual();
        assertThat(ex.getMessage()).containsIgnoringCase("string");
    }

    @Test
    void getOnNonObjectThrows() {
        assertThatExceptionOfType(JsonAssertionException.class)
            .isThrownBy(() -> JsonString.of("x").get("name"));
    }

    @Test
    void elementOnNonArrayThrows() {
        assertThatExceptionOfType(JsonAssertionException.class)
            .isThrownBy(() -> JsonString.of("x").element(0));
    }
}
