package build.base.json;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonWriterTests {

    @Test
    void stringCompact() {
        assertThat(JsonString.of("hello").toJsonString()).isEqualTo("\"hello\"");
    }

    @Test
    void stringEscapesSpecialCharacters() {
        assertThat(JsonString.of("a\"b\\c\nd\te\rf\bf\fg").toJsonString())
            .isEqualTo("\"a\\\"b\\\\c\\nd\\te\\rf\\bf\\fg\"");
    }

    @Test
    void stringEscapesControlCharacters() {
        assertThat(JsonString.of("\u0001\u001f").toJsonString())
            .isEqualTo("\"\\u0001\\u001f\"");
    }

    @Test
    void numberInteger() {
        assertThat(JsonNumber.of(42).toJsonString()).isEqualTo("42");
    }

    @Test
    void numberLong() {
        assertThat(JsonNumber.of(Long.MAX_VALUE).toJsonString())
            .isEqualTo("9223372036854775807");
    }

    @Test
    void numberDouble() {
        assertThat(JsonNumber.of(1.5).toJsonString()).isEqualTo("1.5");
    }

    @Test
    void numberBigDecimalPreservesRepresentation() {
        assertThat(JsonNumber.of(new BigDecimal("1E+2")).toJsonString()).isEqualTo("1E+2");
        assertThat(JsonNumber.of(new BigDecimal("123456789.987654321")).toJsonString())
            .isEqualTo("123456789.987654321");
    }

    @Test
    void numberBigInteger() {
        final var big = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO);
        assertThat(JsonNumber.of(big).toJsonString()).isEqualTo(big.toString());
    }

    @Test
    void booleanTrue() {
        assertThat(JsonBoolean.TRUE.toJsonString()).isEqualTo("true");
    }

    @Test
    void booleanFalse() {
        assertThat(JsonBoolean.FALSE.toJsonString()).isEqualTo("false");
    }

    @Test
    void nullValue() {
        assertThat(JsonNull.INSTANCE.toJsonString()).isEqualTo("null");
    }

    @Test
    void emptyObject() {
        assertThat(JsonObject.of(Map.of()).toJsonString()).isEqualTo("{}");
    }

    @Test
    void emptyArray() {
        assertThat(JsonArray.of(List.of()).toJsonString()).isEqualTo("[]");
    }

    @Test
    void simpleObjectCompact() {
        final var obj = JsonObject.of(Map.of("x", JsonNumber.of(1)));
        assertThat(obj.toJsonString()).isEqualTo("{\"x\":1}");
    }

    @Test
    void simpleArrayCompact() {
        final var arr = JsonArray.of(List.of(JsonNumber.of(1), JsonBoolean.TRUE, JsonNull.INSTANCE));
        assertThat(arr.toJsonString()).isEqualTo("[1,true,null]");
    }

    @Test
    void prettyArray() {
        final var arr = JsonArray.of(List.of(JsonNumber.of(1), JsonNumber.of(2)));
        assertThat(arr.toJsonString(JsonFormat.PRETTY)).isEqualTo("[\n  1,\n  2\n]");
    }

    @Test
    void prettyObject() {
        final var obj = JsonObject.of(Map.of("a", JsonString.of("b")));
        assertThat(obj.toJsonString(JsonFormat.PRETTY)).isEqualTo("{\n  \"a\": \"b\"\n}");
    }

    @Test
    void toStringDelegatesToToJsonString() {
        assertThat(JsonString.of("hi").toString()).isEqualTo("\"hi\"");
        assertThat(JsonNumber.of(7).toString()).isEqualTo("7");
        assertThat(JsonBoolean.TRUE.toString()).isEqualTo("true");
        assertThat(JsonNull.INSTANCE.toString()).isEqualTo("null");
        assertThat(JsonArray.of(List.of()).toString()).isEqualTo("[]");
        assertThat(JsonObject.of(Map.of()).toString()).isEqualTo("{}");
    }
}
