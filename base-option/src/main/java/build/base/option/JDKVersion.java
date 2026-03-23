package build.base.option;

/*-
 * #%L
 * base.build Option
 * %%
 * Copyright (C) 2025 Workday Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.configuration.Default;
import build.base.configuration.Option;
import build.base.foundation.Strings;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides the ability to parse Java Development Kit (JDK) version numbers as defined by the
 * <a href="https://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html">Legacy Version</a>
 * specification (prior to Java 9) and the new
 * <a href="https://docs.oracle.com/javase/9/docs/api/java/lang/Runtime.Version.html">Modern Version</a>
 * specification (from version 9 onwards), representing them as required by the new
 * <a href="http://openjdk.java.net/jeps/223">Versioning Scheme</a>, including dropping the 1 for legacy versions
 * when encountered.
 *
 * @author brian.oliver
 * @since Nov-2019
 */
public final class JDKVersion
    implements Option, Comparable<JDKVersion> {

    /**
     * The raw version {@link String}.
     */
    private final String rawVersion;

    /**
     * The major version number.
     */
    private final int major;

    /**
     * The minor version number.
     */
    private final int minor;

    /**
     * The security version number.
     */
    private final int security;

    /**
     * The build number, {@code null} if not present.
     */
    private final Integer build;

    /**
     * The pre-release information, {@code null} if not present.
     */
    private final String prerelease;

    /**
     * The version identifier, {@code null} if not present.
     */
    private final String identifier;

    /**
     * The numbers, in order of appearance, in the {@link JDKVersion}.
     */
    private final List<Integer> numbers;

    /**
     * Constructs a {@link JDKVersion}.
     *
     * @param rawVersion the raw version {@link String} from which the {@link JDKVersion} was parsed
     * @param major      the major version
     * @param minor      the minor version
     * @param security   the security version
     * @param build      the {@code null}able build number
     * @param prerelease the {@code null}able pre-release
     * @param numbers    the {@code null}able numbers in the {@link JDKVersion}
     */
    private JDKVersion(final String rawVersion,
                       final int major,
                       final int minor,
                       final int security,
                       final Integer build,
                       final String prerelease,
                       final String identifier,
                       final Stream<Integer> numbers) {

        this.rawVersion = rawVersion;
        this.major = major;
        this.minor = minor;
        this.security = security;
        this.build = build;
        this.prerelease = prerelease;
        this.identifier = identifier;
        this.numbers = numbers.toList();
    }

    /**
     * Obtains the raw version {@link String}.
     *
     * @return the raw version
     */
    public String get() {
        return this.rawVersion;
    }

    /**
     * Obtains the major version number.
     *
     * @return the major version number
     */
    public int major() {
        return this.major;
    }

    /**
     * Obtains the minor version number.
     *
     * @return the minor version number
     */
    public int minor() {
        return this.minor;
    }

    /**
     * Obtains the security version number.
     *
     * @return the security version number
     */
    public int security() {
        return this.security;
    }

    /**
     * Obtains the {@link Optional} build number.
     *
     * @return the {@link Optional} build number
     */
    public Optional<Integer> build() {
        return Optional.ofNullable(this.build);
    }

    /**
     * Obtains the {@link Optional} pre-release information.
     *
     * @return the {@link Optional} pre-release information
     */
    public Optional<String> pre() {
        return Optional.ofNullable(this.prerelease);
    }

    /**
     * Obtains the {@link Optional} identifier information.
     *
     * @return the {@link Optional} identifier information
     */
    public Optional<String> optional() {
        return Optional.ofNullable(this.identifier);
    }

    /**
     * Obtains an unmodifiable {@link List} of the integer numerals contained in the {@link JDKVersion}.
     *
     * @return a {@link List} of {@link Integer} numerals in the {@link JDKVersion}
     */
    public List<Integer> version() {
        return this.numbers;
    }

    /**
     * Determines if the {@link JDKVersion} supports the Module System (ie: Major Version 9+)
     *
     * @return {@code true} if the {@link JDKVersion} is modular, {@code false} otherwise
     */
    public boolean isModular() {
        return this.major >= 9;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final var other = (JDKVersion) object;

        return this.major == other.major &&
            this.minor == other.minor &&
            this.security == other.security &&
            Objects.equals(this.build, other.build) &&
            Objects.equals(this.prerelease, other.prerelease) &&
            Objects.equals(this.identifier, other.identifier) &&
            this.numbers.equals(other.numbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.major, this.minor, this.security, this.build, this.prerelease, this.identifier, this.numbers);
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append(this.major);

        // output the other numbers except the build number when there are positive numbers following
        final var size = this.build == null ? this.numbers.size() : this.numbers.size() - 1;
        for (int i = 1; i < size; i++) {
            final int number = this.numbers.get(i);

            // is a positive number following this number?
            var positiveFollows = false;
            for (int j = i + 1; j < size && !positiveFollows; j++) {
                positiveFollows = this.numbers.get(j) > 0;
            }

            if (number > 0 || positiveFollows) {
                builder.append(".");
                builder.append(number);
            }
        }

        if (this.prerelease != null) {
            builder.append("-");
            builder.append(this.prerelease);
        }

        if (this.build != null) {
            builder.append("+");
            builder.append(this.build);
        }

        if (this.identifier != null) {

            if (this.prerelease == null && this.build == null) {
                builder.append("+");
            }

            builder.append("-");
            builder.append(this.identifier);
        }

        return builder.toString();
    }

    @Override
    public int compareTo(final JDKVersion other) {
        // determine the smallest number of numbers from this and the other version
        final var thisSize = other.numbers.size();
        final var otherSize = this.numbers.size();
        final var size = Math.min(thisSize, otherSize);

        // compare those numbers
        for (int i = 0; i < size; i++) {
            final var thisNumber = this.numbers.get(i);
            final var otherNumber = other.numbers.get(i);
            if (thisNumber != otherNumber) {
                return thisNumber - otherNumber;
            }
        }
        return thisSize - otherSize;
    }

    /**
     * Obtains the {@link JDKVersion} based on the {@code java.version} system property.
     *
     * @return the current {@link JDKVersion}
     */
    @Default
    public static JDKVersion current() {
        return of(System.getProperty("java.version"));
    }

    /**
     * Creates a {@link JDKVersion} for the specified major version number.
     * <p>
     * For example, {@code JDKVersion.of(8)} produces a {@link JDKVersion} with a major version of 8.
     *
     * @param major the major {@link JDKVersion} number
     * @return a {@link JDKVersion}
     */
    public static JDKVersion of(final int major) {

        final var list = new ArrayList<Integer>(3);
        list.add(major);
        list.add(0);
        list.add(0);

        final String rawVersion;
        if (major < 9) {
            // create a legacy version
            rawVersion = "1." + major + ".0";
        }
        else {
            // create a modern version
            rawVersion = major + ".0.0";
        }

        return new JDKVersion(rawVersion, major, 0, 0, null, null, null, list.stream());
    }

    /**
     * Obtains the {@link JDKVersion} by parsing the specified {@link String}.
     *
     * @param version the version
     * @return a {@link JDKVersion}
     * @throws NoSuchElementException should the version be invalid format
     */
    public static JDKVersion of(final String version) {

        if (Strings.isEmpty(version)) {
            throw new IllegalArgumentException("The specified version was empty or null");
        }

        final var rawVersion = version.trim();

        try {
            final var numbers = new ArrayList<Integer>();

            var position = 0;

            final var majorString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                .orElseThrow(() -> new IllegalArgumentException("Expected Major Version Number ([0-9]+)"));

            position += majorString.length();

            int major = Integer.parseInt(majorString);
            int minor = 0;
            int security = 0;
            Integer build = null;
            String prerelease = null;
            String identifier = null;

            if (major == 1) {
                // parse the legacy (non-modular) version specification (prior to version 9)
                // according to https://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html

                if (Strings.follows(rawVersion, position, ".")) {
                    position++;

                    // for legacy versions, the minor version becomes the major version
                    final var legacyMajorString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Major Version Number ([0-9]+)"));

                    position += legacyMajorString.length();
                    major = Integer.parseInt(legacyMajorString);
                }
                numbers.add(major);

                if (Strings.follows(rawVersion, position, ".")) {
                    position++;

                    final var minorString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Minor Version Number ([0-9]+)"));

                    position += minorString.length();
                    minor = Integer.parseInt(minorString);
                }
                numbers.add(minor);

                if (Strings.follows(rawVersion, position, "_")) {
                    position++;

                    final var securityString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Security Number ([0-9]+)"));

                    position += securityString.length();
                    security = Integer.parseInt(securityString);
                }
                numbers.add(security);

                if (Strings.follows(rawVersion, position, "-b")
                    && Strings.follows(rawVersion, position + 2, Character::isDigit)) {

                    position += 2;
                    final var buildString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Build Number ([0-9]+)"));

                    position += buildString.length();
                    build = Integer.parseInt(buildString);
                    numbers.add(build);
                }
                else if (Strings.follows(rawVersion, position, "-")
                    && Strings.follows(rawVersion, position + 1,
                    Character::isLetterOrDigit)) {

                    position += 1;

                    prerelease = Strings.collectWhile(rawVersion, position, Character::isLetterOrDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Prerelease Number ([a-zA-Z0-9]+)"));

                    position += prerelease.length();

                    if (Strings.follows(rawVersion, position, "-b")
                        && Strings.follows(rawVersion, position + 2, Character::isDigit)) {

                        position += 2;
                        final var buildString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                            .orElseThrow(() -> new IllegalArgumentException("Expected Build Number ([0-9]+)"));

                        position += buildString.length();
                        build = Integer.parseInt(buildString);
                        numbers.add(build);
                    }
                }
            }
            else {
                // parse the new version specification (as of version 9)
                // according to https://docs.oracle.com/javase/9/docs/api/java/lang/Runtime.Version.html

                // include the major version as the first number
                numbers.add(major);

                // parse an arbitrary number parse digits
                while (Strings.follows(rawVersion, position, ".")) {
                    position++;

                    final var numberString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Number ([0-9]+)"));

                    position += numberString.length();
                    final var number = Integer.parseInt(numberString);
                    numbers.add(number);
                }

                // the number after the major is the minor version
                if (numbers.size() > 1) {
                    minor = numbers.get(1);
                }
                else {
                    numbers.add(minor);
                }

                // the number after the minor is the security version
                if (numbers.size() > 2) {
                    security = numbers.get(2);
                }
                else {
                    numbers.add(security);
                }

                if (Strings.follows(rawVersion, position, "-")) {
                    position++;

                    prerelease = Strings.collectWhile(rawVersion, position, Character::isLetterOrDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Prerelease Number ([a-zA-Z0-9]+)"));

                    position += prerelease.length();

                    if (Strings.follows(rawVersion, position, "-")) {
                        identifier = Strings
                            .collectWhile(rawVersion, position, c -> Character.isLetterOrDigit(c) || c == '-')
                            .orElseThrow(
                                () -> new IllegalArgumentException("Expected Identifier Number ([-a-zA-Z0-9]+)"));

                        return new JDKVersion(rawVersion, major, minor, security, build, prerelease, identifier,
                            numbers.stream());
                    }
                }

                if (Strings.follows(rawVersion, position, "+")) {
                    position++;

                    final var buildString = Strings.collectWhile(rawVersion, position, Character::isDigit)
                        .orElseThrow(() -> new IllegalArgumentException("Expected Build Number ([0-9]+)"));

                    position += buildString.length();
                    build = Integer.parseInt(buildString);
                    numbers.add(build);

                    if (Strings.follows(rawVersion, position, "-")) {
                        identifier = Strings
                            .collectWhile(rawVersion, position, c -> Character.isLetterOrDigit(c) || c == '-')
                            .orElseThrow(
                                () -> new IllegalArgumentException("Expected Identifier Number ([-a-zA-Z0-9]+)"));
                    }

                    return new JDKVersion(rawVersion, major, minor, security, build, prerelease, identifier,
                        numbers.stream());
                }

                if (Strings.follows(rawVersion, position, "+-")) {
                    position++;

                    identifier = Strings
                        .collectWhile(rawVersion, position, c -> Character.isLetterOrDigit(c) || c == '-')
                        .orElseThrow(
                            () -> new IllegalArgumentException("Expected Identifier Number ([-a-zA-Z0-9]+)"));
                }
            }

            return new JDKVersion(rawVersion, major, minor, security, build, prerelease, identifier, numbers.stream());
        }
        catch (final Exception e) {
            throw new RuntimeException("Failed to parse version: " + rawVersion, e);
        }
    }

    /**
     * Attempts to determine the {@link JDKVersion} of the compiled {@link Class} bytecode according to the
     * <a href="https://en.wikipedia.org/wiki/Java_class_file#General_layout">Bytecode File Format</a>.
     *
     * @param targetClass the target {@link Class} from which to determine the {@link JDKVersion}
     * @return an {@link Optional} {@link JDKVersion} or
     * {@link Optional#empty()} if the {@link JDKVersion} could not be detected
     */
    public static Optional<JDKVersion> of(final Class<?> targetClass) {

        if (targetClass == null) {
            return Optional.empty();
        }

        final String className = targetClass.getName();
        final String classAsPath = className.replace('.', '/') + ".class";
        final InputStream inputStream = targetClass.getClassLoader().getResourceAsStream(classAsPath);

        if (inputStream == null) {
            return Optional.empty();
        }

        try (DataInputStream stream = new DataInputStream(inputStream)) {
            final var magicBytes = stream.readInt();

            // ensure the magicBytes are at the start of the stream
            if (magicBytes != 0xcafebabe) {
                return Optional.empty();
            }

            // we don't care about the minor bytecode version number (it's never used)
            stream.readUnsignedShort();

            // the major version is the bytecode version - 44
            final var major = stream.readUnsignedShort() - 44;

            return Optional.of(JDKVersion.of(major));
        }
        catch (final Exception e) {
            return Optional.empty();
        }
    }
}
