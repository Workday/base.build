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

/**
 * Tests for {@link VersionRange}.
 */
class VersionRangeTests {

    private static final Version V1_0 = Version.parse("1.0");
    private static final Version V1_2 = Version.parse("1.2");
    private static final Version V1_5 = Version.parse("1.5");
    private static final Version V2_0 = Version.parse("2.0");
    private static final Version V3_0 = Version.parse("3.0");

    // ── Programmatic construction ────────────────────────────────────────────

    @Test
    void exactRangeShouldMatchOnlyThatVersion() {
        final var range = VersionRange.of(V1_5);
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V1_0)).isFalse();
        assertThat(range.contains(V2_0)).isFalse();
    }

    @Test
    void unboundedRangeShouldMatchEverything() {
        final var range = new VersionRange(null, null);
        assertThat(range.contains(V1_0)).isTrue();
        assertThat(range.contains(V3_0)).isTrue();
    }

    @Test
    void lowerBoundedRangeShouldMatchAtAndAbove() {
        final var range = new VersionRange(VersionBound.lower(V1_5, true), null);
        assertThat(range.contains(V1_0)).isFalse();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isTrue();
    }

    @Test
    void upperBoundedRangeShouldMatchAtAndBelow() {
        final var range = new VersionRange(null, VersionBound.upper(V1_5, true));
        assertThat(range.contains(V1_0)).isTrue();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isFalse();
    }

    // ── Parsing ──────────────────────────────────────────────────────────────

    @Test
    void shouldParseBareVersionAsExactRange() {
        final var range = VersionRange.parse("1.5");
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V1_0)).isFalse();
    }

    @Test
    void shouldParseBracketedExactVersion() {
        final var range = VersionRange.parse("[1.5]");
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V1_0)).isFalse();
    }

    @Test
    void shouldParseInclusiveInclusiveRange() {
        final var range = VersionRange.parse("[1.2,2.0]");
        assertThat(range.contains(V1_2)).isTrue();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isTrue();
        assertThat(range.contains(V1_0)).isFalse();
        assertThat(range.contains(V3_0)).isFalse();
    }

    @Test
    void shouldParseInclusiveExclusiveRange() {
        final var range = VersionRange.parse("[1.2,2.0)");
        assertThat(range.contains(V1_2)).isTrue();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isFalse();
        assertThat(range.contains(V1_0)).isFalse();
    }

    @Test
    void shouldParseUnboundedUpperRange() {
        final var range = VersionRange.parse("[1.5,)");
        assertThat(range.contains(V1_0)).isFalse();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V3_0)).isTrue();
    }

    @Test
    void shouldParseUnboundedLowerInclusiveRange() {
        final var range = VersionRange.parse("(,1.5]");
        assertThat(range.contains(V1_0)).isTrue();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isFalse();
    }

    @Test
    void shouldParseUnboundedLowerExclusiveRange() {
        final var range = VersionRange.parse("(,1.5)");
        assertThat(range.contains(V1_0)).isTrue();
        assertThat(range.contains(V1_5)).isFalse();
        assertThat(range.contains(V2_0)).isFalse();
    }

    // ── Exclusive lower bound ────────────────────────────────────────────────

    @Test
    void shouldParseExclusiveLowerInclusiveUpperRange() {
        final var range = VersionRange.parse("(1.2,2.0]");
        assertThat(range.contains(V1_2)).isFalse();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isTrue();
        assertThat(range.contains(V3_0)).isFalse();
    }

    @Test
    void shouldParseExclusiveLowerExclusiveUpperRange() {
        final var range = VersionRange.parse("(1.2,2.0)");
        assertThat(range.contains(V1_2)).isFalse();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V2_0)).isFalse();
    }

    @Test
    void shouldParseExclusiveLowerUnboundedUpperRange() {
        final var range = VersionRange.parse("(1.2,)");
        assertThat(range.contains(V1_2)).isFalse();
        assertThat(range.contains(V1_5)).isTrue();
        assertThat(range.contains(V3_0)).isTrue();
    }

    // ── (,X) upper-bound regression ──────────────────────────────────────────

    @Test
    void strictlyLessThanRangeMustNotMatchVersionAtBound() {
        final var range = VersionRange.parse("(,1.5)");
        assertThat(range.contains(V1_5)).isFalse();
    }

    @Test
    void strictlyLessThanRangeMustNotMatchVersionAboveBound() {
        final var range = VersionRange.parse("(,1.5)");
        assertThat(range.contains(V2_0)).isFalse();
    }

    // ── Invalid input ────────────────────────────────────────────────────────

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> VersionRange.parse(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmpty() {
        assertThatThrownBy(() -> VersionRange.parse(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectDisjointRangeInVersionRange() {
        assertThatThrownBy(() -> VersionRange.parse("(,1.0],[1.2,)"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
