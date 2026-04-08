package build.base.commandline;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CommandLineParser} environment variable fallback
 * ({@link CommandLineParser#registerEnvVar(String, String)} and
 * {@link CommandLineParser#setEnvironmentVariableResolver(java.util.function.Function)}).
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class CommandLineParserEnvVarTests {

    /**
     * Ensure an env var value is used when no command-line argument is provided.
     */
    @Test
    void shouldUseEnvVarWhenNoCommandLineArgProvided() {
        final var parser = new CommandLineParser()
            .add(CommandLineParserTests.OneArgumentOption.class)
            .setEnvironmentVariableResolver(name -> "ENABLED".equals(name) ? Optional.of("true") : Optional.empty())
            .registerEnvVar("ENABLED", "-enabled");

        assertThat(parser.parse().build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(true));
    }

    /**
     * Ensure command-line arguments take precedence over env var values.
     */
    @Test
    void commandLineArgShouldOverrideEnvVar() {
        final var parser = new CommandLineParser()
            .add(CommandLineParserTests.OneArgumentOption.class)
            .setEnvironmentVariableResolver(name -> "ENABLED".equals(name) ? Optional.of("true") : Optional.empty())
            .registerEnvVar("ENABLED", "-enabled");

        assertThat(parser.parse("-enabled", "false").build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(false));
    }

    /**
     * Ensure a zero-parameter option is activated when its env var is set.
     */
    @Test
    void shouldActivateZeroParamOptionFromEnvVar() {
        final var parser = new CommandLineParser()
            .add(CommandLineParserTests.ZeroArgumentOption.class)
            .setEnvironmentVariableResolver(name -> "USE_DEFAULT".equals(name) ? Optional.of("") : Optional.empty())
            .registerEnvVar("USE_DEFAULT", "-default");

        assertThat(parser.parse().build()
            .get(CommandLineParserTests.ZeroArgumentOption.class))
            .isEqualTo(CommandLineParserTests.ZeroArgumentOption.autodetect());
    }

    /**
     * Ensure that setting the resolver to {@code null} disables env var resolution entirely.
     */
    @Test
    void shouldDisableEnvVarResolutionWhenResolverIsNull() {
        final var parser = new CommandLineParser()
            .add(CommandLineParserTests.OneArgumentOption.class)
            .setEnvironmentVariableResolver(null)
            .registerEnvVar("ENABLED", "-enabled");

        assertThat(parser.parse().build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isNull();
    }
}
