package build.base.commandline;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the {@link Command} fluent builder.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class CommandTests {

    /**
     * Ensure {@link Command#create(String)} produces a {@link CommandLineParser} that parses an annotated option class.
     */
    @Test
    void commandBuilderShouldParseAnnotatedOption() {
        final var parser = Command.create("test")
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .build();

        assertThat(parser.parse("-default").build()
            .get(CommandLineParserTests.ZeroArgumentOption.class))
            .isEqualTo(CommandLineParserTests.ZeroArgumentOption.autodetect());
    }

    /**
     * Ensure {@link Command#option(String, Class, String, Class[])} registers an explicit factory method.
     */
    @Test
    void commandBuilderShouldRegisterExplicitOption() {
        final var parser = Command.create("test")
            .option("-enabled", CommandLineParserTests.OneArgumentOption.class, "of", boolean.class)
            .build();

        assertThat(parser.parse("-enabled", "true").build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(true));
    }

    /**
     * Ensure {@link Command#option(String, Class, String, Class[])} wraps {@link NoSuchMethodException} as
     * {@link IllegalArgumentException} when the method does not exist.
     */
    @Test
    void commandBuilderShouldWrapNoSuchMethodException() {
        final var command = Command.create("test")
            .option("-enabled", CommandLineParserTests.OneArgumentOption.class, "nonExistentMethod", boolean.class);

        assertThrows(IllegalArgumentException.class, command::build);
    }

    /**
     * Ensure {@link Command#argument(String)} and {@link Command#helpHandler(java.util.function.Consumer)}
     * are forwarded to the parser.
     */
    @Test
    void commandBuilderShouldForwardArgumentAndHelpHandler() {
        final List<String> helpOutput = new ArrayList<>();

        final var parser = Command.create("spin")
            .argument("<task>...")
            .helpHandler(helpOutput::add)
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .build();

        parser.parse("--help");

        assertThat(helpOutput).hasSize(1);
        assertThat(helpOutput.getFirst()).contains("Usage: spin [options] <task>...");
    }

    /**
     * Ensure {@link Command#unknownOption(CommandLineParser.UnknownOptionConsumer)} is forwarded to the parser.
     */
    @Test
    void commandBuilderShouldForwardUnknownOptionConsumer() {
        final var parser = Command.create("test")
            .unknownOption(CommandLineParser.IGNORE_UNKNOWN_OPTIONS)
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .build();

        assertThat(parser.parse("-default", "--unknown").build()
            .get(CommandLineParserTests.ZeroArgumentOption.class))
            .isNotNull();
    }

    /**
     * Ensure {@link Command#section(String)} groups options under named headings in help output.
     */
    @Test
    void commandBuilderShouldGroupOptionsIntoSections() {
        final List<String> helpOutput = new ArrayList<>();

        final var parser = Command.create("spin")
            .helpHandler(helpOutput::add)
            .section("Global options")
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .section("Task options")
            .option(CommandLineParserTests.OneArgumentOption.class)
            .build();

        parser.parse("--help");

        assertThat(helpOutput).hasSize(1);
        assertThat(helpOutput.getFirst())
            .containsSubsequence("Global options", "-default", "Task options", "-enabled");
    }

    /**
     * Ensure {@link Command#envVar(String, String)} and {@link Command#envVarResolver(java.util.function.Function)}
     * wire up correctly via the builder.
     */
    @Test
    void commandBuilderEnvVarShouldInjectValue() {
        final var parser = Command.create("test")
            .option(CommandLineParserTests.OneArgumentOption.class)
            .envVar("MY_ENABLED", "-enabled")
            .envVarResolver(name -> "MY_ENABLED".equals(name) ? Optional.of("false") : Optional.empty())
            .build();

        assertThat(parser.parse().build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(false));
    }

    /**
     * Ensure {@link Command#command(String, String)} causes the command to appear under a {@code Commands:}
     * section in help output.
     */
    @Test
    void commandBuilderShouldRegisterCommandsInHelp() {
        final List<String> helpOutput = new ArrayList<>();

        Command.create("spin")
            .helpHandler(helpOutput::add)
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .command("compile", "Compiles sources")
            .command("test", "Runs tests")
            .build()
            .parse("--help");

        assertThat(helpOutput).hasSize(1);
        final var help = helpOutput.getFirst();
        assertThat(help).contains("Commands:");
        assertThat(help).contains("compile");
        assertThat(help).contains("Compiles sources");
        assertThat(help).contains("test");
    }

    /**
     * Ensure {@link Command#category(String, String)} causes the category to appear under a {@code Categories:}
     * section in help output.
     */
    @Test
    void commandBuilderShouldRegisterCategoriesInHelp() {
        final List<String> helpOutput = new ArrayList<>();

        Command.create("spin")
            .helpHandler(helpOutput::add)
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .category("build", "Runs compile + test + package")
            .build()
            .parse("--help");

        assertThat(helpOutput).hasSize(1);
        final var help = helpOutput.getFirst();
        assertThat(help).contains("Categories:");
        assertThat(help).contains("build");
        assertThat(help).contains("Runs compile + test + package");
    }

    /**
     * Ensure {@link Command#option(List, Class, String, String, Class[])} registers all prefixes so the option
     * can be parsed via any of them.
     */
    @Test
    void commandBuilderShouldRegisterMultiPrefixOption() {
        final var parser = Command.create("test")
            .option(List.of("--working-dir", "-w"),
                CommandLineParserTests.OneArgumentOption.class,
                "of",
                "Working directory",
                boolean.class)
            .build();

        assertThat(parser.parse("--working-dir", "true").build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(true));

        assertThat(parser.parse("-w", "false").build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(false));
    }

    /**
     * Ensure {@link Command#option(List, Class, String, String, Class[])} wraps {@link NoSuchMethodException} as
     * {@link IllegalArgumentException} when the method does not exist.
     */
    @Test
    void commandBuilderShouldWrapNoSuchMethodExceptionForMultiPrefixOption() {
        final var command = Command.create("test")
            .option(List.of("--working-dir", "-w"),
                CommandLineParserTests.OneArgumentOption.class,
                "nonExistentMethod",
                "desc",
                boolean.class);

        assertThrows(IllegalArgumentException.class, command::build);
    }

    /**
     * Ensure {@link Command#commandsSectionName(String)} and {@link Command#categoriesSectionName(String)}
     * replace the default headings in help output.
     */
    @Test
    void commandBuilderShouldUseCustomSectionNames() {
        final List<String> helpOutput = new ArrayList<>();

        Command.create("spin")
            .helpHandler(helpOutput::add)
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .command("compile", "Compiles sources")
            .commandsSectionName("Tasks:")
            .category("build", "Runs everything")
            .categoriesSectionName("Aliases:")
            .build()
            .parse("--help");

        assertThat(helpOutput).hasSize(1);
        final var help = helpOutput.getFirst();
        assertThat(help).contains("Tasks:");
        assertThat(help).doesNotContain("Commands:");
        assertThat(help).contains("Aliases:");
        assertThat(help).doesNotContain("Categories:");
    }

    /**
     * Ensure {@link Command#positionalArgument(java.util.function.Consumer)} routes non-hyphen args to the consumer.
     */
    @Test
    void commandBuilderShouldForwardPositionalArgumentConsumer() {
        final List<String> positionals = new ArrayList<>();

        Command.create("spin")
            .option(CommandLineParserTests.ZeroArgumentOption.class)
            .positionalArgument(positionals::add)
            .build()
            .parse("-default", "compile", "test");

        assertThat(positionals).containsExactly("compile", "test");
    }

    /**
     * Ensure command-line arg overrides env var when wired through the {@link Command} builder.
     */
    @Test
    void commandBuilderCommandLineShouldOverrideEnvVar() {
        final var parser = Command.create("test")
            .option(CommandLineParserTests.OneArgumentOption.class)
            .envVar("MY_ENABLED", "-enabled")
            .envVarResolver(name -> "MY_ENABLED".equals(name) ? Optional.of("false") : Optional.empty())
            .build();

        assertThat(parser.parse("-enabled", "true").build()
            .get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(true));
    }
}
