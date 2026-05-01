package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JsonBooleanTests {

    @Test
    void ofTrueReturnsTrueConstant() {
        assertThat(JsonBoolean.of(true)).isSameAs(JsonBoolean.TRUE);
    }

    @Test
    void ofFalseReturnsFalseConstant() {
        assertThat(JsonBoolean.of(false)).isSameAs(JsonBoolean.FALSE);
    }

    @Test
    void valueIsCorrect() {
        assertThat(JsonBoolean.TRUE.value()).isTrue();
        assertThat(JsonBoolean.FALSE.value()).isFalse();
    }

    @Test
    void asBooleanReturnsSelf() {
        assertThat(JsonBoolean.TRUE.asBoolean()).isSameAs(JsonBoolean.TRUE);
    }

    @Test
    void equalityByValue() {
        assertThat(JsonBoolean.of(true)).isEqualTo(JsonBoolean.TRUE);
        assertThat(JsonBoolean.of(false)).isEqualTo(JsonBoolean.FALSE);
        assertThat(JsonBoolean.TRUE).isNotEqualTo(JsonBoolean.FALSE);
    }

    @Test
    void otherAsXThrow() {
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonBoolean.TRUE::asObject);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonBoolean.TRUE::asArray);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonBoolean.TRUE::asString);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonBoolean.TRUE::asNumber);
    }
}
