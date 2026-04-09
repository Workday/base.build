package build.base.version;

/*-
 * #%L
 * base.build Version
 * %%
 * Copyright (C) 2026 Workday Inc
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for {@link Version}.
 */
class VersionTests {

    // ── Parsing ──────────────────────────────────────────────────────────────

    @Test
    void shouldParseSimpleNumericVersion() {
        final var v = Version.parse("1.2.3");
        assertThat(v.sequence()).extracting(Version.Element::getValue)
            .containsExactly(1, 2, 3);
        assertThat(v.prerelease()).isEmpty();
        assertThat(v.build()).isEmpty();
    }

    @Test
    void shouldParseVersionWithPrerelease() {
        final var v = Version.parse("1.0-SNAPSHOT");
        assertThat(v.sequence()).extracting(Version.Element::getValue)
            .containsExactly(1, 0);
        assertThat(v.prerelease()).extracting(Version.Element::getValue)
            .containsExactly("SNAPSHOT");
        assertThat(v.build()).isEmpty();
    }

    @Test
    void shouldParseVersionWithBuildMetadata() {
        final var v = Version.parse("1.0+build.42");
        assertThat(v.sequence()).extracting(Version.Element::getValue)
            .containsExactly(1, 0);
        assertThat(v.prerelease()).isEmpty();
        assertThat(v.build()).extracting(Version.Element::getValue)
            .containsExactly("build", 42);
    }

    @Test
    void shouldParseVersionWithPrereleaseAndBuild() {
        final var v = Version.parse("1.0-alpha.1+001");
        assertThat(v.sequence()).extracting(Version.Element::getValue)
            .containsExactly(1, 0);
        assertThat(v.prerelease()).extracting(Version.Element::getValue)
            .containsExactly("alpha", 1);
        assertThat(v.build()).extracting(Version.Element::getValue)
            .containsExactly(1);
    }

    @Test
    void shouldParseAlphaDigitTransitionInSequence() {
        final var v = Version.parse("1.0RC1");
        assertThat(v.sequence()).extracting(Version.Element::getValue)
            .containsExactly(1, 0, "RC", 1);
    }

    @Test
    void shouldReturnRawStringFromGet() {
        assertThat(Version.parse("1.2.3-SNAPSHOT").get()).isEqualTo("1.2.3-SNAPSHOT");
    }

    @Test
    void shouldRejectNullInput() {
        assertThatThrownBy(() -> Version.parse(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmptyInput() {
        assertThatThrownBy(() -> Version.parse(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── tryParse ─────────────────────────────────────────────────────────────

    @Test
    void tryParseShouldReturnPresentForValidVersion() {
        assertThat(Version.tryParse("1.2.3")).isPresent()
            .hasValueSatisfying(v -> assertThat(v.get()).isEqualTo("1.2.3"));
    }

    @Test
    void tryParseShouldReturnEmptyForNull() {
        assertThat(Version.tryParse(null)).isEmpty();
    }

    @Test
    void tryParseShouldReturnEmptyForBlank() {
        assertThat(Version.tryParse("")).isEmpty();
    }

    @Test
    void tryParseShouldReturnEmptyForAlphaOnlySequence() {
        assertThat(Version.tryParse("SNAPSHOT")).isEmpty();
    }

    // ── Comparison ───────────────────────────────────────────────────────────

    @Test
    void shouldOrderNumericElementsNumerically() {
        assertThat(Version.parse("1.9")).isLessThan(Version.parse("1.10"));
    }

    @Test
    void shouldOrderStringElementsLexicographically() {
        assertThat(Version.parse("1.0-alpha")).isLessThan(Version.parse("1.0-beta"));
    }

    @Test
    void shouldOrderVersionWithoutPrereleaseHigherThanWithSameSequence() {
        assertThat(Version.parse("1.0")).isGreaterThan(Version.parse("1.0-SNAPSHOT"));
        assertThat(Version.parse("1.0")).isGreaterThan(Version.parse("1.0-alpha"));
    }

    @Test
    void shouldOrderBySequenceBeforePrerelease() {
        assertThat(Version.parse("2.0-alpha")).isGreaterThan(Version.parse("1.9"));
    }

    @Test
    void shouldTreatTrailingZeroesAsEqual() {
        assertThat(Version.parse("1.0.0")).isEqualByComparingTo(Version.parse("1.0"));
    }

    @Test
    void shouldCompareBuildMetadataLast() {
        assertThat(Version.parse("1.0+1")).isLessThan(Version.parse("1.0+2"));
    }

    @Test
    void shouldRejectPurelyAlphabeticSequence() {
        assertThatThrownBy(() -> Version.parse("SNAPSHOT"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectSequenceStartingWithAlpha() {
        assertThatThrownBy(() -> Version.parse("RC1"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── equals / hashCode ────────────────────────────────────────────────────

    @Test
    void elementHashCodeShouldBeConsistentWithEquals() throws Exception {
        final var ctorInt = Version.Element.class.getDeclaredConstructor(int.class);
        ctorInt.setAccessible(true);
        final var ctorStr = Version.Element.class.getDeclaredConstructor(String.class);
        ctorStr.setAccessible(true);

        final var intElement = (Version.Element) ctorInt.newInstance(1);
        final var strElement = (Version.Element) ctorStr.newInstance("1");

        // mixed-type comparison falls back to toString — these compare as equal
        assertThat(intElement.compareTo(strElement)).isZero();
        // hashCode must be consistent with equals per the Object contract
        assertThat(intElement.hashCode()).isEqualTo(strElement.hashCode());
    }

    @Test
    void shouldBeEqualForStructurallyIdenticalVersions() {
        assertThat(Version.parse("1.0.0")).isEqualTo(Version.parse("1.0"));
    }

    @Test
    void shouldNotBeEqualForDifferentVersions() {
        assertThat(Version.parse("1.0")).isNotEqualTo(Version.parse("1.1"));
    }

    @Test
    void shouldHaveConsistentHashCode() {
        assertThat(Version.parse("1.0.0").hashCode())
            .isEqualTo(Version.parse("1.0").hashCode());
    }
}
