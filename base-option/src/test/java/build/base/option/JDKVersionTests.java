package build.base.option;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link JDKVersion}.
 *
 * @author brian.oliver
 * @author andrew.wilson
 * @since Nov-2019
 */
class JDKVersionTests {

    /**
     * Obtain the parameter types.
     *
     * @return the parameter types
     * @see #shouldParseJDKVersion for parameter types
     */
    static Stream<Arguments> jdkVersions() {
        return Stream.of(
            // Ensure legacy (pre Java 9) version numbers can be parsed.
            jdkVersion("1.9.0", "9", 9, 0, 0, null, null, true),
            jdkVersion("1.7.0-ea-b19", "7-ea+19", 7, 0, 0, 19, "ea", false),
            jdkVersion("1.9.0-b100", "9+100", 9, 0, 0, 100, null, true),
            jdkVersion("1.9.0_5-b20", "9.0.5+20", 9, 0, 5, 20, null, true),
            jdkVersion("1.9.0_11-b12", "9.0.11+12", 9, 0, 11, 12, null, true),
            jdkVersion("1.9.1_31-b08", "9.1.31+8", 9, 1, 31, 8, null, true),

            // Ensure modern (Java 9 and beyond) version numbers can be parsed.
            jdkVersion("9", "9", 9, 0, 0, null, null, true),
            jdkVersion("9-ea", "9-ea", 9, 0, 0, null, "ea", true),
            jdkVersion("9+100", "9+100", 9, 0, 0, 100, null, true),
            jdkVersion("9.0.1", "9.0.1", 9, 0, 1, null, null, true),
            jdkVersion("9.0.1+20", "9.0.1+20", 9, 0, 1, 20, null, true),
            jdkVersion("9.1.2", "9.1.2", 9, 1, 2, null, null, true),
            jdkVersion("9.1.2+1", "9.1.2+1", 9, 1, 2, 1, null, true),
            jdkVersion("25.0.4.1", "25.0.4.1", 25, 0, 4, null, null, true),
            jdkVersion("9.1.3+15", "9.1.3+15", 9, 1, 3, 15, null, true));
    }

    /**
     * Strongly typed helper method for building arguments.
     *
     * @param versionString   the version string
     * @param versionToString the toString of the version
     * @param major           major version
     * @param minor           minor version
     * @param security        security number
     * @param build           build number (or null)
     * @param pre             pre string (or null)
     * @param modular         is the version modular
     */
    private static Arguments jdkVersion(final String versionString,
                                        final String versionToString,
                                        final int major,
                                        final int minor,
                                        final int security,
                                        final Integer build,
                                        final String pre,
                                        final boolean modular) {

        return Arguments.of(versionString, versionToString, major, minor, security, build, pre, modular);
    }

    /**
     * Check correct parsing of java version.
     *
     * @param versionString   the version string
     * @param versionToString the toString of the version
     * @param major           major version
     * @param minor           minor version
     * @param security        security number
     * @param build           build number (or null)
     * @param pre             pre string (or null)
     * @param modular         is the version modular
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("jdkVersions")
    void shouldParseJDKVersion(final String versionString,
                               final String versionToString,
                               final int major,
                               final int minor,
                               final int security,
                               final Integer build,
                               final String pre,
                               final boolean modular) {

        final JDKVersion version = JDKVersion.of(versionString);

        assertThat(version.major())
            .isEqualTo(major);

        assertThat(version.minor())
            .isEqualTo(minor);

        assertThat(version.security())
            .isEqualTo(security);

        if (pre != null) {
            assertThat(version.pre())
                .contains(pre);
        }

        if (build != null) {
            assertThat(version.build())
                .contains(build);
        }

        assertThat(version.optional())
            .isEmpty();

        assertThat(version.toString())
            .isEqualTo(versionToString);

        assertThat(version.isModular())
            .isEqualTo(modular);
    }

    /**
     * Ensure the current version of Java can be parsed.
     */
    @Test
    void shouldParseCurrentJDKVersion() {
        final JDKVersion current = JDKVersion.current();

        assertThat(current)
            .isNotNull();
    }

    /**
     * Ensure {@link JDKVersion} numbers can be compared.
     */
    @Test
    void shouldCompareVersionNumbers() {

        assertThat(JDKVersion.of("1.2"))
            .isEqualTo(JDKVersion.of("2"));

        assertThat(JDKVersion.of("1.2"))
            .isEqualTo(JDKVersion.of("2.0"));

        assertThat(JDKVersion.of("1.2"))
            .isEqualTo(JDKVersion.of("2.0.0"));

        assertThat(JDKVersion.of("1.2"))
            .isLessThan(JDKVersion.of("1.2.1"));

        // a shorter version compares equal to the same version zero-padded, but below one with a
        // positive trailing segment (e.g. an Azul $PATCH respin)
        assertThat(JDKVersion.of("25.0.4"))
            .isEqualByComparingTo(JDKVersion.of("25.0.4.0"));

        assertThat(JDKVersion.of("25.0.4"))
            .isLessThan(JDKVersion.of("25.0.4.1"));

        assertThat(JDKVersion.of("25.0.4.1"))
            .isGreaterThan(JDKVersion.of("25.0.4"));

        assertThat(JDKVersion.of("1.2"))
            .isLessThan(JDKVersion.of("1.3"));

        assertThat(JDKVersion.of("9.1.3+15"))
            .isLessThan(JDKVersion.of("9.1.3+16"));

        assertThat(JDKVersion.of("9.1.3+15"))
            .isLessThan(JDKVersion.of("9.1.4"));

        assertThat(JDKVersion.of("9.0.0"))
            .isLessThan(JDKVersion.of("10"));
    }

    /**
     * Ensure the optional (additional build) information is exposed.
     */
    @Test
    void shouldExposeOptionalInformation() {
        assertThat(JDKVersion.of("9.0.1+20-internal").optional())
            .contains("internal");

        assertThat(JDKVersion.of("9.0.1+20").optional())
            .isEmpty();
    }

    /**
     * Ensure {@link JDKVersion#version()} exposes the dotted version-number elements only, without
     * the build number, and irrespective of the input scheme.
     */
    @Test
    void shouldExposeVersionNumberElements() {
        assertThat(JDKVersion.of("9.0.1+20").version())
            .containsExactly(9, 0, 1);

        assertThat(JDKVersion.of("1.9.0-b100").version())
            .containsExactly(9);

        assertThat(JDKVersion.of("25.0.4.1").version())
            .containsExactly(25, 0, 4, 1);

        assertThat(JDKVersion.of("1.8.0_292-b10").version())
            .containsExactly(8, 0, 292);
    }

    /**
     * Ensure {@link JDKVersion#get()} echoes the raw input while {@link JDKVersion#toString()}
     * returns the normalized, modern-scheme form.
     */
    @Test
    void shouldRetainRawVersionSeparatelyFromToString() {
        final JDKVersion version = JDKVersion.of("1.8.0_292-b10");

        assertThat(version.get())
            .isEqualTo("1.8.0_292-b10");

        assertThat(version.toString())
            .isEqualTo("8.0.292+10");
    }

    /**
     * Ensure {@link JDKVersion#equals(Object)} and {@link JDKVersion#hashCode()} stay consistent,
     * including when two instances differ only in the optional (additional build) information or in
     * trailing zero elements.
     */
    @Test
    void shouldKeepEqualsAndHashCodeConsistent() {
        final JDKVersion padded = JDKVersion.of("25.0.4.0");
        final JDKVersion unpadded = JDKVersion.of("25.0.4");

        assertThat(padded)
            .isEqualTo(unpadded)
            .hasSameHashCodeAs(unpadded);

        final JDKVersion withOptional = JDKVersion.of("9.0.1+20-internal");
        final JDKVersion withoutOptional = JDKVersion.of("9.0.1+20");

        assertThat(withOptional)
            .isNotEqualTo(withoutOptional);
    }

    /**
     * Ensure invalid, blank, and {@code null} version strings are rejected with
     * {@link IllegalArgumentException}.
     */
    @Test
    void shouldRejectInvalidVersionStrings() {
        assertThatThrownBy(() -> JDKVersion.of((String) null))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> JDKVersion.of("   "))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> JDKVersion.of("not-a-version"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Ensure a {@link JDKVersion} can be created using just a major version number.
     */
    @Test
    void shouldCreateJDKVersionsFromMajorNumber() {
        assertThat(JDKVersion.of(8))
            .isEqualTo(JDKVersion.of("1.8.0"));

        assertThat(JDKVersion.of(9))
            .isEqualTo(JDKVersion.of("9.0.0"));
    }

    /**
     * Ensure a {@link JDKVersion} can be determined from a compiled {@link Class}.
     */
    @Test
    void shouldDetermineJDKVersionFromCompiledClass() {
        final JDKVersion current = JDKVersion.of(JDKVersion.current().major());

        final JDKVersion detected = JDKVersion.of(JDKVersionTests.class)
            .orElseThrow(() -> new RuntimeException("Failed to determine the JDKVersion"));

        assertTrue(current.major() >= detected.major());
    }
}
