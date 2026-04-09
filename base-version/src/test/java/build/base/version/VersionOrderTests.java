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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link VersionOrder}.
 */
class VersionOrderTests {

    // ── NATURAL ──────────────────────────────────────────────────────────────

    @Test
    void naturalShouldOrderVersionsNumerically() {
        assertThat(VersionOrder.NATURAL.compare(Version.parse("1.9"), Version.parse("1.10"))).isNegative();
    }

    @Test
    void naturalShouldOrderStringElementsLexicographically() {
        // milestone > beta under lexicographic order (m > b) — this is the known gap MAVEN fixes
        assertThat(VersionOrder.NATURAL.compare(Version.parse("1.0-milestone"), Version.parse("1.0-beta"))).isPositive();
    }

    // ── MAVEN: qualifier chain ────────────────────────────────────────────────

    @Test
    void mavenShouldOrderAlphaBeforeBeta() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-alpha"), Version.parse("1.0-beta"))).isNegative();
    }

    @Test
    void mavenShouldOrderBetaBeforeMilestone() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-beta"), Version.parse("1.0-milestone"))).isNegative();
    }

    @Test
    void mavenShouldOrderMilestoneBeforeRc() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-milestone"), Version.parse("1.0-rc"))).isNegative();
    }

    @Test
    void mavenShouldOrderRcBeforeSnapshot() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-rc"), Version.parse("1.0-SNAPSHOT"))).isNegative();
    }

    @Test
    void mavenShouldOrderSnapshotBeforeRelease() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-SNAPSHOT"), Version.parse("1.0"))).isNegative();
    }

    @Test
    void mavenShouldSortFullQualifierChainCorrectly() {
        final var versions = List.of(
            Version.parse("1.0"),
            Version.parse("1.0-SNAPSHOT"),
            Version.parse("1.0-rc"),
            Version.parse("1.0-milestone"),
            Version.parse("1.0-beta"),
            Version.parse("1.0-alpha"));
        assertThat(versions.stream().sorted(VersionOrder.MAVEN).toList())
            .containsExactly(
                Version.parse("1.0-alpha"),
                Version.parse("1.0-beta"),
                Version.parse("1.0-milestone"),
                Version.parse("1.0-rc"),
                Version.parse("1.0-SNAPSHOT"),
                Version.parse("1.0"));
    }

    // ── MAVEN: aliases ───────────────────────────────────────────────────────

    @Test
    void mavenShouldTreatAAsAlpha() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-a"), Version.parse("1.0-beta"))).isNegative();
    }

    @Test
    void mavenShouldTreatBAndBetaAsEquivalent() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-b"), Version.parse("1.0-beta"))).isZero();
    }

    @Test
    void mavenShouldTreatMAsMilestone() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-m"), Version.parse("1.0-rc"))).isNegative();
    }

    @Test
    void mavenShouldTreatCrAsRc() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-cr"), Version.parse("1.0-SNAPSHOT"))).isNegative();
    }

    // ── MAVEN: case insensitivity ─────────────────────────────────────────────

    @Test
    void mavenShouldBeCaseInsensitive() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-ALPHA"), Version.parse("1.0-beta"))).isNegative();
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-Alpha"), Version.parse("1.0-BETA"))).isNegative();
    }

    // ── MAVEN: numeric prerelease elements ────────────────────────────────────

    @Test
    void mavenShouldOrderNumericPrereleaseElementsNumerically() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-alpha.1"), Version.parse("1.0-alpha.2"))).isNegative();
    }

    @Test
    void mavenShouldOrderAlpha1BeforeAlpha2WithTransitionSyntax() {
        // "alpha1" parses via alpha/digit transition to [String("alpha"), Integer(1)]
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-alpha1"), Version.parse("1.0-alpha2"))).isNegative();
    }

    // ── MAVEN: unknown qualifiers ─────────────────────────────────────────────

    @Test
    void mavenShouldOrderUnknownQualifiersAfterKnownOnes() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-SNAPSHOT"), Version.parse("1.0-custom"))).isNegative();
    }

    @Test
    void mavenShouldOrderUnknownQualifiersLexicographically() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.0-bar"), Version.parse("1.0-foo"))).isNegative();
    }

    // ── MAVEN: sequence and structural rules still apply ──────────────────────

    @Test
    void mavenShouldOrderSequenceNumerically() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("1.9"), Version.parse("1.10"))).isNegative();
    }

    @Test
    void mavenShouldOrderHigherSequenceBeforeLowerRegardlessOfQualifier() {
        assertThat(VersionOrder.MAVEN.compare(Version.parse("2.0-alpha"), Version.parse("1.9"))).isPositive();
    }

    // ── SEMVER ───────────────────────────────────────────────────────────────

    @Test
    void semverShouldTreatVersionsDifferingOnlyInBuildAsEqual() {
        // SemVer 2.0.0 §10: build metadata MUST be ignored when determining version precedence
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.0+1"), Version.parse("1.0+2"))).isZero();
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.0.0+build.1"), Version.parse("1.0.0+build.2"))).isZero();
    }

    @Test
    void semverShouldTreatVersionWithAndWithoutBuildAsEqual() {
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.0+build"), Version.parse("1.0"))).isZero();
    }

    @Test
    void semverShouldStillOrderPrereleaseBeforeRelease() {
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.0-alpha"), Version.parse("1.0"))).isNegative();
    }

    @Test
    void semverShouldStillOrderPrereleaseElementsLexicographically() {
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.0-alpha"), Version.parse("1.0-beta"))).isNegative();
    }

    @Test
    void semverShouldStillOrderSequencesNumerically() {
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.9"), Version.parse("1.10"))).isNegative();
    }

    @Test
    void semverShouldOrderNumericPrereleaseBeforeAlphanumeric() {
        // SemVer §11.4.1/11.4.3: numeric identifiers have lower precedence than alphanumeric
        // "1" (Integer(1)) vs "alpha" (String): toString comparison "1" < "a" in ASCII
        assertThat(VersionOrder.SEMVER.compare(Version.parse("1.0-1"), Version.parse("1.0-alpha"))).isNegative();
    }
}
