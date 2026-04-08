package build.base.commandline;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CommandLineParser} help section grouping ({@link CommandLineParser#setHelpSection(String)}).
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class CommandLineParserSectionTests {

    /**
     * Ensure {@link CommandLineParser#setHelpSection(String)} causes options to render under named headings
     * in the correct order.
     */
    @Test
    void shouldRenderSectionHeadersInHelp() {
        final List<String> helpOutput = new ArrayList<>();

        final var parser = new CommandLineParser()
            .setHelpUsageProgramName("spin")
            .setHelpHandler(helpOutput::add)
            .setHelpSection("Global options")
            .add(CommandLineParserTests.ZeroArgumentOption.class)
            .setHelpSection("Task options")
            .add(CommandLineParserTests.OneArgumentOption.class);

        parser.parse("--help");

        assertThat(helpOutput).hasSize(1);
        final var help = helpOutput.getFirst();
        assertThat(help).contains("Global options");
        assertThat(help).contains("Task options");
        assertThat(help).containsSubsequence("Global options", "-default", "Task options", "-enabled");
    }

    /**
     * Ensure that options registered under sections are still parsed correctly — sections are purely
     * a help-rendering concern and must not affect parse behaviour.
     */
    @Test
    void sectionsShouldNotAffectParsing() {
        final var parser = new CommandLineParser()
            .setHelpSection("Global options")
            .add(CommandLineParserTests.ZeroArgumentOption.class)
            .setHelpSection("Task options")
            .add(CommandLineParserTests.OneArgumentOption.class);

        final var configuration = parser.parse("-default", "-enabled", "true").build();

        assertThat(configuration.get(CommandLineParserTests.ZeroArgumentOption.class))
            .isEqualTo(CommandLineParserTests.ZeroArgumentOption.autodetect());

        assertThat(configuration.get(CommandLineParserTests.OneArgumentOption.class))
            .isEqualTo(CommandLineParserTests.OneArgumentOption.of(true));
    }

    /**
     * Ensure options registered without a section render without a section header (backward compatibility).
     */
    @Test
    void shouldRenderUnsectionedOptionsWithoutHeader() {
        final List<String> helpOutput = new ArrayList<>();

        final var parser = new CommandLineParser()
            .setHelpHandler(helpOutput::add)
            .add(CommandLineParserTests.ZeroArgumentOption.class);

        parser.parse("--help");

        assertThat(helpOutput).hasSize(1);
        assertThat(helpOutput.getFirst()).doesNotContain("Global options");
    }

    /**
     * Ensure {@link CommandLineParser#setHelpSection(String)} with {@code null} resets to the unsectioned group:
     * the subsequently registered option appears in help but is not nested under any section heading.
     */
    @Test
    void shouldResetSectionToDefaultWhenNullPassed() {
        final List<String> helpOutput = new ArrayList<>();

        final var parser = new CommandLineParser()
            .setHelpHandler(helpOutput::add)
            .setHelpSection("My section")
            .add(CommandLineParserTests.ZeroArgumentOption.class)
            .setHelpSection(null)
            .add(CommandLineParserTests.OneArgumentOption.class);

        parser.parse("--help");

        assertThat(helpOutput).hasSize(1);
        final var help = helpOutput.getFirst();

        assertThat(help).contains("My section");
        assertThat(help).contains("-enabled");
        assertThat(help).containsSubsequence("My section", "-default", "-enabled");

        final int mySectionIndex = help.indexOf("My section");
        final int enabledIndex = help.indexOf("-enabled");
        final int defaultIndex = help.indexOf("-default");

        assertThat(defaultIndex).isGreaterThan(mySectionIndex);
        assertThat(enabledIndex).isGreaterThan(defaultIndex);
    }
}
