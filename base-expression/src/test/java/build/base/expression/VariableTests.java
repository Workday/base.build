package build.base.expression;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests that {@link Variable}s are created properly and that their properties can be accessed.
 *
 * @author graeme.campbell
 * @since Jan-2019
 */
class VariableTests {

    /**
     * Tests that {@link Variable}s can be created and that their getter methods work properly.
     */
    @Test
    void shouldCreateVariableAndAccessItsProperties() {
        final Variable variable = Variable.of("application", "app");

        assertThat(variable.name())
            .isEqualTo("application");

        assertThat(variable.value())
            .isEqualTo("app");
    }
}
