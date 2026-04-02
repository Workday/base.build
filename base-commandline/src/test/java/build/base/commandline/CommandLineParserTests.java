package build.base.commandline;

import build.base.configuration.AbstractValueOption;
import build.base.configuration.Configuration;
import build.base.configuration.Option;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the {@link CommandLineParser} and the {@link CommandLine} interface/annotations.
 *
 * @author brian.oliver
 * @author spencer.firestone
 * @since Sep-2020
 */
class CommandLineParserTests {

    /**
     * Ensure {@link Option}s that take no parameters may be parsed.
     */
    @Test
    void shouldParseZeroParameterOption() {
        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class);

        assertThat(parser.parse("-default").build()
            .get(ZeroArgumentOption.class))
            .isEqualTo(ZeroArgumentOption.autodetect());
    }

    /**
     * Ensure {@link Option}s that take one parameter may be parsed.
     */
    @Test
    void shouldParseOneParameterOption() {
        final var parser = new CommandLineParser()
            .add(OneArgumentOption.class);

        assertThat(parser.parse("-enabled=true").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(parser.parse("-enabled", "true").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(parser.parse("-enabled=false").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));

        assertThat(parser.parse("-enabled", "false").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));
    }

    /**
     * Ensure {@link Option}s that define alternative names can be parsed.
     */
    @Test
    void shouldParseUsingAlternativeOptionName() {
        final var parser = new CommandLineParser()
            .add(OneArgumentOption.class);

        assertThat(parser.parse("-e=true").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(parser.parse("-e", "true").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(parser.parse("-e=false").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));

        assertThat(parser.parse("-e", "false").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));
    }

    /**
     * Ensure {@link Option}s that take two parameter may be parsed.
     */
    @Test
    void shouldParseTwoParameterOption() {
        final var parser = new CommandLineParser()
            .add(TwoArgumentOption.class);

        assertThat(parser.parse("-add=1", "2").build()
            .get(TwoArgumentOption.class))
            .isEqualTo(TwoArgumentOption.of(1, 2));

        assertThat(parser.parse("-add", "1", "2").build()
            .get(TwoArgumentOption.class))
            .isEqualTo(TwoArgumentOption.of(1, 2));
    }

    /**
     * Ensure multiple {@link Option}s can be parsed.
     */
    @Test
    void shouldParseMultipleOptions() {
        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class)
            .add(OneArgumentOption.class)
            .add(TwoArgumentOption.class);

        final var configuration = parser.parse(
                "-add", "1", "2",
                "-default",
                "-enabled", "true")
            .build();

        assertThat(configuration.get(ZeroArgumentOption.class))
            .isEqualTo(ZeroArgumentOption.autodetect());

        assertThat(configuration.get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(configuration.get(TwoArgumentOption.class))
            .isEqualTo(TwoArgumentOption.of(1, 2));
    }

    /**
     * Ensure custom {@link Option}s that take no parameters may be parsed.
     */
    @Test
    void shouldParseCustomZeroParameterOption()
        throws NoSuchMethodException {

        final var parser = new CommandLineParser()
            .add("-default", ZeroArgumentOption.class, "autodetect");

        assertThat(parser.parse("-default").build()
            .get(ZeroArgumentOption.class))
            .isEqualTo(ZeroArgumentOption.autodetect());
    }

    /**
     * Ensure custom {@link Option}s that take one parameter may be parsed.
     */
    @Test
    void shouldParseCustomOneParameterOption()
        throws NoSuchMethodException {

        final var parser = new CommandLineParser()
            .add("-enabled", OneArgumentOption.class, "of", boolean.class);

        assertThat(parser.parse("-enabled=true").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(parser.parse("-enabled", "true").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(true));

        assertThat(parser.parse("-enabled=false").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));

        assertThat(parser.parse("-enabled", "false").build()
            .get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));
    }

    /**
     * Ensure custom {@link Option}s that take two parameter may be parsed.
     */
    @Test
    void shouldParseCustomTwoParameterOption()
        throws NoSuchMethodException {

        final var parser = new CommandLineParser()
            .add("-add", TwoArgumentOption.class, "of", int.class, int.class);

        assertThat(parser.parse("-add=1", "2").build()
            .get(TwoArgumentOption.class))
            .isEqualTo(TwoArgumentOption.of(1, 2));

        assertThat(parser.parse("-add", "1", "2").build()
            .get(TwoArgumentOption.class))
            .isEqualTo(TwoArgumentOption.of(1, 2));
    }

    /**
     * Ensure {@link CommandLine.Argument}s are parsed when there's no options.
     */
    @Test
    void shouldParseCommandLineArgumentsWithoutOptions() {
        final var parser = new CommandLineParser()
            .setUnknownOptionConsumer(CommandLineParser.CAPTURE_UNKNOWN_OPTIONS_AS_ARGUMENTS);

        final var arguments = new String[] { "hello", "world" };

        final var configuration = parser.parse(arguments).build();

        final var parsed = configuration.stream(CommandLine.Argument.class)
            .map(CommandLine.Argument::get)
            .toArray(String[]::new);

        assertThat(arguments)
            .containsExactly(parsed);
    }

    /**
     * Ensure {@link CommandLine.Argument}s are parsed when there are {@link Option}s.
     */
    @Test
    void shouldParseCommandLineArgumentsWithOptions() {
        final var parser = new CommandLineParser()
            .setUnknownOptionConsumer(CommandLineParser.CAPTURE_UNKNOWN_OPTIONS_AS_ARGUMENTS)
            .add(ZeroArgumentOption.class)
            .add(OneArgumentOption.class);

        final var arguments = new String[] { "-enabled", "no", "-default", "hello", "world" };

        final Configuration configuration = parser.parse(arguments).build();

        assertThat(configuration.get(ZeroArgumentOption.class))
            .isEqualTo(ZeroArgumentOption.autodetect());

        assertThat(configuration.get(OneArgumentOption.class))
            .isEqualTo(OneArgumentOption.of(false));

        final var parsed = configuration.stream(CommandLine.Argument.class)
            .map(CommandLine.Argument::get)
            .toArray(String[]::new);

        assertThat(parsed)
            .containsExactly("hello", "world");
    }

    /**
     * Ensure {@link CommandLine.Argument}s are not parsed when they aren't allowed.
     */
    @Test
    void shouldNotParseCommandLineArguments() {
        final var parser = new CommandLineParser();

        final var arguments = new String[] { "hello", "world" };

        assertThrows(CommandLineParser.HelpException.class, () -> parser.parse(arguments));
    }

    /**
     * Ensure {@link CommandLine} {@link Option}s can be created and converted back into a string stream and then back
     * to a {@link CommandLine}.
     */
    @Test
    void shouldConvertStringsToCommandLineAndBack() {
        final var parser = new CommandLineParser()
            .add(CommandLineOption.class);

        final var arguments = new String[] { "--value", "5.5" };

        // Create a commandLine
        final var commandLineOption = parser.parse(arguments).build()
            .get(CommandLineOption.class);

        assertThat(commandLineOption.get())
            .isEqualTo(5.5);

        // Extract the arguments from the CommandLineOption
        final var commandLineOptionArguments = commandLineOption.arguments().toList();

        assertThat(commandLineOptionArguments)
            .containsExactly("-v", "5.5");

        // use those arguments to create a new CommandLineOption and assert that they are equal
        final var newCommandLineOption = parser.parse(commandLineOptionArguments.toArray(new String[0]))
            .build()
            .get(CommandLineOption.class);

        assertThat(commandLineOption)
            .isEqualTo(newCommandLineOption);
    }

    /**
     * Ensure a {@link CommandLineParser} argument that is a key/value pair in the format "x=y" is parsed correctly.
     */
    @Test
    void shouldTreatEqualsAsADelimiter() {
        final var key = "key";
        final var value = "value";

        final var parser = new CommandLineParser()
            .add(KeyValuePairOption.class);

        final var separatedArguments = new String[] { "--key-value", key, value };
        final var combinedArguments = new String[] { "--key-value", String.format("%s=%s", key, value) };
        final var oneArgument = new String[] { String.format("--key-value=%s=%s", key, value) };

        final var combinedOption = parser.parse(combinedArguments).build()
            .get(KeyValuePairOption.class);

        final var separatedOption = parser.parse(separatedArguments).build()
            .get(KeyValuePairOption.class);

        final KeyValuePairOption oneArgOption = parser.parse(oneArgument).build()
            .get(KeyValuePairOption.class);

        assertThat(combinedOption.key)
            .isEqualTo(key);

        assertThat(combinedOption.value)
            .isEqualTo(value);

        assertThat(combinedOption)
            .isEqualTo(separatedOption);

        assertThat(combinedOption)
            .isEqualTo(oneArgOption);
    }

    /**
     * Tests that help output should be generated.
     */
    @Test
    void shouldGenerateHelp() {
        final CommandLineParser parser = new CommandLineParser()
            .setHelpUsageProgramName("GenericProgram")
            .add(ZeroArgumentOption.class)
            .add(OneArgumentOption.class)
            .add(TwoArgumentOption.class)
            .add(KeyValuePairOption.class)
            .add(CommandLineOption.class);

        final var shortArg = new String[] { "-h" };
        final var longArg = new String[] { "--help" };
        final var multipleArgs = new String[] { "--value", "5.5", "-enabled", "no", "-h", "-default" };

        assertThrows(CommandLineParser.HelpException.class, () -> parser.parse(shortArg));
        assertThrows(CommandLineParser.HelpException.class, () -> parser.parse(longArg));

        final var helpException = assertThrows(CommandLineParser.HelpException.class,
            () -> parser.parse(multipleArgs));

        System.out.println(helpException.getMessage());
    }

    /**
     * Test that zero argument options can be anywhere in the argument list.
     */
    @Test
    void shouldAllowMultipleZeroArgumentOptions() {
        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class)
            .add(ZeroArgumentOption2.class)
            .add(OneArgumentOption.class);

        final var args = new String[] { "-default2", "-default", "-e", "no" };

        final var configuration = parser.parse(args).build();

        assertThat(configuration.get(ZeroArgumentOption.class))
            .isNotNull();

        assertThat(configuration.get(ZeroArgumentOption2.class))
            .isNotNull();

        assertThat(configuration.get(OneArgumentOption.class))
            .isNotNull();
    }

    /**
     * Tests that {@link CommandLineParser#setSuppressHelp(boolean)} suppresses help requests and ignore the help options.
     */
    @Test
    void shouldIgnoreHelp() {
        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class)
            .setSuppressHelp(true);

        final var args = new String[] { "--help", "-h", "-default" };

        assertThat(parser.parse(args).build()
            .get(ZeroArgumentOption.class))
            .isNotNull();
    }

    /**
     * Tests that the {@link CommandLineParser} handles missing arguments.
     */
    @Test
    void shouldHandleMissingArgument() {
        final var parser = new CommandLineParser()
            .add(OneArgumentOption.class);

        final var args = new String[] { "-e" };

        final var helpException =
            assertThrows(CommandLineParser.HelpException.class, () -> parser.parse(args));

        System.out.println(helpException.getMessage());
    }

    /**
     * Tests that any prefixes for an option that uses the reserved {@link CommandLineParser} help arguments is ignored
     * by the parser.
     */
    @Test
    void shouldIgnoreHelpPrefixedOptions() {
        final var parser = new CommandLineParser()
            .add(HelpPrefixedOption.class)
            .setSuppressHelp(true);

        final var helpArgs = new String[] { "--help" };

        assertThat(parser.parse(helpArgs).build()
            .get(HelpPrefixedOption.class))
            .isNull();

        final var helpAndNonHelpArgs = new String[] { "--help", "--not-help-prefix", "foo" };
        assertThat(parser.parse(helpAndNonHelpArgs).build()
            .get(HelpPrefixedOption.class))
            .isNotNull();
    }

    /**
     * Tests that use of {@link CommandLineParser#IGNORE_UNKNOWN_OPTIONS} causes the parser to ignore
     * unknown arguments that would cause it to fail parsing with a HelpException otherwise.
     */
    @Test
    void shouldIgnoreUnknownArguments() {
        final var args = new String[] { "-this-will-not-work", "-default", "--this-will-also-not-work" };

        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class);

        assertThrows(CommandLineParser.HelpException.class, () -> parser.parse(args));

        final var ignoreUnknownParser = new CommandLineParser()
            .add(ZeroArgumentOption.class)
            .setUnknownOptionConsumer(CommandLineParser.IGNORE_UNKNOWN_OPTIONS);

        assertThat(ignoreUnknownParser.parse(args).build()
            .get(ZeroArgumentOption.class))
            .isNotNull();
    }

    /**
     * Tests that a {@link CommandLineParser} support {@link Option}s with multiple public static constructor methods.
     */
    @Test
    void shouldSupportMultipleStaticConstructorMethods() {
        final var parser = new CommandLineParser().add(MultipleMethodOption.class);

        assertThat(parser.parse("--multi-method").build()
            .getValue(MultipleMethodOption.class))
            .isEqualTo("default");

        assertThat(parser.parse("-mm").build()
            .getValue(MultipleMethodOption.class))
            .isEqualTo("default");

        assertThat(parser.parse("--multi-method-value", "foo").build()
            .getValue(MultipleMethodOption.class))
            .isEqualTo("foo");

        assertThat(parser.parse("-mmv", "bar").build()
            .getValue(MultipleMethodOption.class))
            .isEqualTo("bar");

        assertThat(parser.parse("--multi-method-concat", "foo", "bar").build()
            .getValue(MultipleMethodOption.class))
            .isEqualTo("foobar");

        assertThat(parser.parse("-mmc", "baz", "feh").build()
            .getValue(MultipleMethodOption.class))
            .isEqualTo("bazfeh");
    }

    /**
     * Tests that a {@link CommandLineParser} should not fail upon adding a class that has no
     * {@link CommandLine.Prefix}es.
     */
    @Test
    void shouldNotFailWhenNoPrefixOptionsAreIncluded() {
        final var parser = new CommandLineParser()
            .add(NoPrefixOption.class);

        assertThat(parser.parse().stream())
            .isEmpty();
    }

    /**
     * Tests that the {@link CommandLineParser} can handle {@link Option}s with conflicting arguments.
     */
    @Test
    void shouldHandleConflictingArgumentOptions() {
        final CommandLineParser parser = new CommandLineParser()
            .add(ConflictingPrefixOption1.class)
            .add(ConflictingPrefixOption2.class)
            .add(ConflictingPrefixOption3.class);

        final CommandLineParser.HelpException helpException = assertThrows(
            CommandLineParser.HelpException.class, () -> parser.parse(CommandLineParser.HELP_ARGUMENT_SHORT));
        System.out.println(helpException.getMessage());

        // ensure that all three options can still be parsed by their unambiguous arguments
        final var configuration = parser.parse("-cpo1", "-cpo2", "-cpo3").build();

        assertThat(configuration.getValue(ConflictingPrefixOption1.class))
            .isEqualTo("1");

        assertThat(configuration.getValue(ConflictingPrefixOption2.class))
            .isEqualTo("2");

        assertThat(configuration.getValue(ConflictingPrefixOption3.class))
            .isEqualTo("3");

        // ambiguous options should fail with an IllegalArgumentException
        final var ambiguousException = assertThrows(IllegalArgumentException.class, () -> parser.parse("-cpo"));

        // use the --option-class override to disambiguate
        final var disambiguatedOptions = parser.parse(
                CommandLineParser.OPTION_CLASS_OVERRIDE, ConflictingPrefixOption1.class.getName(), "-cpo",
                CommandLineParser.OPTION_CLASS_OVERRIDE, ConflictingPrefixOption2.class.getName(), "-cpo",
                CommandLineParser.OPTION_CLASS_OVERRIDE, ConflictingPrefixOption3.class.getName(), "-cpo")
            .build();

        assertThat(disambiguatedOptions.getValue(ConflictingPrefixOption1.class))
            .isEqualTo("1");

        assertThat(disambiguatedOptions.getValue(ConflictingPrefixOption2.class))
            .isEqualTo("2");

        assertThat(disambiguatedOptions.getValue(ConflictingPrefixOption3.class))
            .isEqualTo("3");

        // should fail on an invalid disambiguation
        final var illegalDisambiguationException =
            assertThrows(IllegalArgumentException.class, () -> parser.parse(
                CommandLineParser.OPTION_CLASS_OVERRIDE, ZeroArgumentOption.class.getName(), "-cpo"));
        System.out.println(illegalDisambiguationException.toString());
    }

    /**
     * Tests that the {@link CommandLineParser#OPTION_CLASS_OVERRIDE} works for unambiguous options.
     */
    @Test
    void shouldAllowDisambiguationOfUnambiguousOptions() {
        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class);

        final var arguments = new String[] {
            CommandLineParser.OPTION_CLASS_OVERRIDE, ZeroArgumentOption.class.getName(), "-default"
        };

        assertThat(parser.parse(arguments).build()
            .getValue(ZeroArgumentOption.class))
            .isTrue();
    }

    /**
     * Tests that the {@link CommandLineParser#OPTION_CLASS_OVERRIDE} is always respected, even if the argument itself
     * would have been unambiguous.
     */
    @Test
    void shouldFailUnambiguousArgumentsIfInvalidOverrideIsUsed() {
        final var parser = new CommandLineParser()
            .add(ZeroArgumentOption.class);

        final var illegalDisambiguationException =
            assertThrows(IllegalArgumentException.class, () -> parser.parse(
                CommandLineParser.OPTION_CLASS_OVERRIDE, ZeroArgumentOption2.class.getName(), "-default"));
        System.out.println(illegalDisambiguationException.toString());
    }

    /**
     * A simple zero argument {@link Option}.
     */
    public static class ZeroArgumentOption
        extends AbstractValueOption<Boolean> {

        private ZeroArgumentOption() {
            super(true);
        }

        @CommandLine.Prefix("-default")
        public static ZeroArgumentOption autodetect() {
            return new ZeroArgumentOption();
        }
    }

    /**
     * Another simple zero argument {@link Option}.
     */
    private static class ZeroArgumentOption2
        extends AbstractValueOption<Boolean> {

        private ZeroArgumentOption2() {
            super(true);
        }

        @CommandLine.Prefix("-default2")
        public static ZeroArgumentOption2 autodetect() {
            return new ZeroArgumentOption2();
        }
    }

    /**
     * A simple one argument {@link Option}.
     */
    public static class OneArgumentOption
        extends AbstractValueOption<Boolean> {

        private OneArgumentOption(final boolean value) {
            super(value);
        }

        @CommandLine.Prefix("-enabled")
        @CommandLine.Prefix("-e")
        public static OneArgumentOption of(final boolean value) {
            return new OneArgumentOption(value);
        }
    }

    /**
     * A simple two argument {@link Option}.
     */
    public static class TwoArgumentOption
        extends AbstractValueOption<Integer> {

        private TwoArgumentOption(final int value) {
            super(value);
        }

        @CommandLine.Prefix("-add")
        @CommandLine.Description("Adds two ints")
        public static TwoArgumentOption of(final int first, final int second) {
            return new TwoArgumentOption(first + second);
        }
    }

    /**
     * An option that stores a key/value pair.
     */
    public static class KeyValuePairOption
        implements Option {

        private final String key;
        private final String value;

        private KeyValuePairOption(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @CommandLine.Prefix("--key-value")
        public static KeyValuePairOption of(final String key, final String value) {
            return new KeyValuePairOption(key, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final KeyValuePairOption that = (KeyValuePairOption) o;
            return Objects.equals(this.key, that.key) &&
                Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.value);
        }
    }

    /**
     * A {@link CommandLine} {@link Option}.
     */
    public static class CommandLineOption
        extends AbstractValueOption<Double>
        implements CommandLine {

        private CommandLineOption(final double value) {
            super(value);
        }

        @CommandLine.Prefix("-v")
        @CommandLine.Prefix("--value")
        @CommandLine.Description("Sets a double")
        public static CommandLineOption of(final double value) {
            return new CommandLineOption(value);
        }

        @Override
        public Stream<String> arguments() {
            return Stream.of("-v", String.valueOf(get()));
        }
    }

    /**
     * An {@link Option} that uses the reserved {@link CommandLineParser} help arguments as prefixes.
     */
    private static class HelpPrefixedOption
        extends AbstractValueOption<String> {

        private HelpPrefixedOption(final String value) {
            super(value);
        }

        @CommandLine.Prefix(CommandLineParser.HELP_ARGUMENT_LONG)
        @CommandLine.Prefix(CommandLineParser.HELP_ARGUMENT_SHORT)
        @CommandLine.Prefix("--not-help-prefix")
        public static HelpPrefixedOption of(final String value) {
            return new HelpPrefixedOption(value);
        }
    }

    /**
     * An {@link Option} that has multiple static public constructor methods.
     */
    private static class MultipleMethodOption
        extends AbstractValueOption<String> {

        private MultipleMethodOption(final String value) {
            super(value);
        }

        @CommandLine.Prefix("--multi-method")
        @CommandLine.Prefix("-mm")
        public static MultipleMethodOption autodetect() {
            return new MultipleMethodOption("default");
        }

        @CommandLine.Prefix("--multi-method-value")
        @CommandLine.Prefix("-mmv")
        public static MultipleMethodOption of(final String value) {
            return new MultipleMethodOption(value);
        }

        @CommandLine.Prefix("--multi-method-concat")
        @CommandLine.Prefix("-mmc")
        public static MultipleMethodOption of(final String value1, final String value2) {
            return new MultipleMethodOption(value1 + value2);
        }
    }

    /**
     * An {@link Option} that has no {@link CommandLine.Prefix}es.
     */
    private static class NoPrefixOption
        extends AbstractValueOption<String> {

        private NoPrefixOption(final String value) {
            super(value);
        }
    }

    /**
     * An {@link Option} that will have conflicts.
     */
    private static class ConflictingPrefixOption1
        extends AbstractValueOption<String> {

        private ConflictingPrefixOption1() {
            super("1");
        }

        @CommandLine.Prefix("-cpo")
        @CommandLine.Prefix("-cpo1")
        public static ConflictingPrefixOption1 autodetect() {
            return new ConflictingPrefixOption1();
        }
    }

    /**
     * An {@link Option} that will have conflicts.
     */
    private static class ConflictingPrefixOption2
        extends AbstractValueOption<String> {

        private ConflictingPrefixOption2() {
            super("2");
        }

        @CommandLine.Prefix("-cpo")
        @CommandLine.Prefix("-cpo2")
        public static ConflictingPrefixOption2 autodetect() {
            return new ConflictingPrefixOption2();
        }
    }

    /**
     * An {@link Option} that will have conflicts.
     */
    private static class ConflictingPrefixOption3
        extends AbstractValueOption<String> {

        private ConflictingPrefixOption3() {
            super("3");
        }

        @CommandLine.Prefix("-cpo")
        @CommandLine.Prefix("-cpo3")
        public static ConflictingPrefixOption3 autodetect() {
            return new ConflictingPrefixOption3();
        }
    }
}
