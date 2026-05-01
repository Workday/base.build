package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonAssertionExceptionTests {

    @Test
    void notAIncludesExpectedAndActualType() {
        var ex = JsonAssertionException.notA("object", JsonNull.INSTANCE);
        assertThat(ex.getMessage())
            .contains("object")
            .contains("JsonNullImpl");
    }

    @Test
    void missingMemberIncludesName() {
        var ex = JsonAssertionException.missingMember("foo");
        assertThat(ex.getMessage()).contains("foo");
    }

    @Test
    void missingElementIncludesIndex() {
        var ex = JsonAssertionException.missingElement(3);
        assertThat(ex.getMessage()).contains("3");
    }
}
