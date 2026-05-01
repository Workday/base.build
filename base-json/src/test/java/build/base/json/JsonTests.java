package build.base.json;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JsonTests {

    // --- parse(String) ---

    @Test
    void parseNullThrows() {
        assertThatNullPointerException().isThrownBy(() -> Json.parse((String) null));
    }

    @Test
    void parseString() {
        assertThat(Json.parse("\"hello\"")).isEqualTo(JsonString.of("hello"));
    }

    @Test
    void parseStringWithEscapes() {
        assertThat(Json.parse("\"a\\\"b\\\\c\\nd\"")).isEqualTo(JsonString.of("a\"b\\c\nd"));
    }

    @Test
    void parseStringWithUnicodeEscape() {
        assertThat(Json.parse("\"\\u0041\"")).isEqualTo(JsonString.of("A"));
    }

    @Test
    void parseIntegerAsLong() {
        final var result = Json.parse("42");
        assertThat(result).isInstanceOf(JsonNumber.class);
        assertThat(((JsonNumber) result).toNumber()).isEqualTo(42L);
    }

    @Test
    void parseLargeIntegerAsBigInteger() {
        final var big = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO);
        final var result = Json.parse(big.toString());
        assertThat(((JsonNumber) result).toNumber()).isEqualTo(big);
    }

    @Test
    void parseDecimalAsBigDecimal() {
        final var result = Json.parse("3.14");
        assertThat(((JsonNumber) result).toNumber()).isEqualTo(new BigDecimal("3.14"));
    }

    @Test
    void parseExponentAsBigDecimal() {
        final var result = Json.parse("1E+2");
        assertThat(((JsonNumber) result).toNumber()).isEqualTo(new BigDecimal("1E+2"));
    }

    @Test
    void parseNegativeNumber() {
        assertThat(((JsonNumber) Json.parse("-7")).toNumber()).isEqualTo(-7L);
    }

    @Test
    void parseTrue() {
        assertThat(Json.parse("true")).isSameAs(JsonBoolean.TRUE);
    }

    @Test
    void parseFalse() {
        assertThat(Json.parse("false")).isSameAs(JsonBoolean.FALSE);
    }

    @Test
    void parseNull() {
        assertThat(Json.parse("null")).isSameAs(JsonNull.INSTANCE);
    }

    @Test
    void parseEmptyObject() {
        assertThat(Json.parse("{}")).isEqualTo(JsonObject.of(Map.of()));
    }

    @Test
    void parseObjectWithMembers() {
        final var result = Json.parse("{\"a\":1,\"b\":true}").asObject();
        assertThat(result.get("a")).isEqualTo(JsonNumber.of(1L));
        assertThat(result.get("b")).isSameAs(JsonBoolean.TRUE);
    }

    @Test
    void parseNestedObject() {
        final var result = Json.parse("{\"x\":{\"y\":\"z\"}}").asObject();
        assertThat(result.get("x").asObject().get("y")).isEqualTo(JsonString.of("z"));
    }

    @Test
    void parseEmptyArray() {
        assertThat(Json.parse("[]")).isEqualTo(JsonArray.of(List.of()));
    }

    @Test
    void parseArrayWithElements() {
        final var result = Json.parse("[1,\"two\",null]").asArray();
        assertThat(result.element(0)).isEqualTo(JsonNumber.of(1L));
        assertThat(result.element(1)).isEqualTo(JsonString.of("two"));
        assertThat(result.element(2)).isSameAs(JsonNull.INSTANCE);
    }

    @Test
    void parseIgnoresWhitespace() {
        final var result = Json.parse("  {  \"k\"  :  1  }  ").asObject();
        assertThat(result.get("k")).isEqualTo(JsonNumber.of(1L));
    }

    @Test
    void parseReader() {
        assertThat(Json.parse(new StringReader("true"))).isSameAs(JsonBoolean.TRUE);
    }

    // --- parse errors ---

    @Test
    void parseEmptyInputThrows() {
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> Json.parse(""));
    }

    @Test
    void parseTrailingContentThrows() {
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> Json.parse("true false"));
    }

    @Test
    void parseMalformedObjectThrows() {
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> Json.parse("{\"a\":}"));
    }

    @Test
    void parseUnterminatedStringThrows() {
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> Json.parse("\"unclosed"));
    }

    @Test
    void parseErrorCarriesLineAndColumn() {
        final var ex = org.assertj.core.api.Assertions
            .catchThrowableOfType(JsonParseException.class, () -> Json.parse("\n\nbad"));
        assertThat(ex.line()).isEqualTo(3);
    }

    @Test
    void parseErrorCarriesPath() {
        final var ex = org.assertj.core.api.Assertions
            .catchThrowableOfType(JsonParseException.class, () -> Json.parse("{\"a\":{}}"));
        // no error in this case; test a nested failure
        final var ex2 = org.assertj.core.api.Assertions
            .catchThrowableOfType(JsonParseException.class, () -> Json.parse("{\"users\":[{\"age\":}]}"));
        assertThat(ex2.path()).isEqualTo("/users/0/age");
    }

    // --- round-trip ---

    @Test
    void roundTripString() {
        final var v = JsonString.of("hello");
        assertThat(Json.parse(v.toJsonString())).isEqualTo(v);
    }

    @Test
    void roundTripObject() {
        final var v = JsonObject.of(Map.of("x", JsonNumber.of(1L), "y", JsonBoolean.TRUE));
        assertThat(Json.parse(v.toJsonString())).isEqualTo(v);
    }

    @Test
    void roundTripArray() {
        final var v = JsonArray.of(List.of(JsonString.of("a"), JsonNull.INSTANCE));
        assertThat(Json.parse(v.toJsonString())).isEqualTo(v);
    }

    // --- Json.of ---

    @Test
    void ofNullGivesJsonNull() {
        assertThat(Json.of(null)).isSameAs(JsonNull.INSTANCE);
    }

    @Test
    void ofStringGivesJsonString() {
        assertThat(Json.of("hi")).isEqualTo(JsonString.of("hi"));
    }

    @Test
    void ofNumberGivesJsonNumber() {
        assertThat(Json.of(42)).isEqualTo(JsonNumber.of(42));
    }

    @Test
    void ofBooleanGivesJsonBoolean() {
        assertThat(Json.of(true)).isSameAs(JsonBoolean.TRUE);
    }

    @Test
    void ofUnsupportedTypeThrows() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Json.of(List.of()));
    }

    // --- Json.array / Json.object ---

    @Test
    void arrayVarargs() {
        assertThat(Json.array(JsonBoolean.TRUE, JsonNull.INSTANCE))
            .isEqualTo(JsonArray.of(List.of(JsonBoolean.TRUE, JsonNull.INSTANCE)));
    }

    @Test
    void arrayIterable() {
        assertThat(Json.array(List.of(JsonString.of("x"))))
            .isEqualTo(JsonArray.of(List.of(JsonString.of("x"))));
    }

    @Test
    void objectDelegate() {
        assertThat(Json.object(Map.of("k", JsonNull.INSTANCE)))
            .isEqualTo(JsonObject.of(Map.of("k", JsonNull.INSTANCE)));
    }

}
