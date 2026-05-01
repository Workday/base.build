package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JsonNullTests {

    @Test
    void instanceIsSingleton() {
        assertThat(JsonNull.INSTANCE).isSameAs(JsonNull.INSTANCE);
    }

    @Test
    void equalityIsStructural() {
        assertThat(JsonNull.INSTANCE).isEqualTo(new JsonNullImpl());
    }

    @Test
    void allAsXThrow() {
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonNull.INSTANCE::asObject);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonNull.INSTANCE::asArray);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonNull.INSTANCE::asString);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonNull.INSTANCE::asNumber);
        assertThatExceptionOfType(JsonAssertionException.class).isThrownBy(JsonNull.INSTANCE::asBoolean);
    }
}
