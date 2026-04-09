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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link VersionConstraint}.
 */
class VersionConstraintTests {

    private static final Version V0_9  = Version.parse("0.9");
    private static final Version V1_0  = Version.parse("1.0");
    private static final Version V1_1  = Version.parse("1.1");
    private static final Version V1_2  = Version.parse("1.2");
    private static final Version V1_5  = Version.parse("1.5");
    private static final Version V2_0  = Version.parse("2.0");
    private static final Version V3_0  = Version.parse("3.0");

    // ── Programmatic construction ────────────────────────────────────────────

    @Test
    void shouldContainVersionInAnyMatchingRange() {
        final var constraint = new VersionConstraint(List.of(
            VersionRange.parse("(,1.0]"),
            VersionRange.parse("[1.2,)")));
        assertThat(constraint.contains(V0_9)).isTrue();
        assertThat(constraint.contains(V1_0)).isTrue();
        assertThat(constraint.contains(V1_1)).isFalse();
        assertThat(constraint.contains(V1_5)).isTrue();
    }

    @Test
    void shouldExposeRangeList() {
        final var r1 = VersionRange.parse("[1.0,2.0)");
        final var r2 = VersionRange.parse("[3.0,)");
        final var constraint = new VersionConstraint(List.of(r1, r2));
        assertThat(constraint.ranges()).containsExactly(r1, r2);
    }

    // ── Single-range forms ───────────────────────────────────────────────────

    @Test
    void shouldParseBareVersionAsExactConstraint() {
        final var c = VersionConstraint.parse("1.0");
        assertThat(c.contains(V1_0)).isTrue();
        assertThat(c.contains(V1_1)).isFalse();
    }

    @Test
    void shouldParseBracketedExactVersion() {
        assertThat(VersionConstraint.parse("[1.0]").contains(V1_0)).isTrue();
        assertThat(VersionConstraint.parse("[1.0]").contains(V1_1)).isFalse();
    }

    @Test
    void shouldParseInclusiveExclusiveRange() {
        final var c = VersionConstraint.parse("[1.0,2.0)");
        assertThat(c.contains(V1_0)).isTrue();
        assertThat(c.contains(V1_5)).isTrue();
        assertThat(c.contains(V2_0)).isFalse();
    }

    @Test
    void shouldParseUnboundedUpperRange() {
        final var c = VersionConstraint.parse("[1.5,)");
        assertThat(c.contains(V1_0)).isFalse();
        assertThat(c.contains(V1_5)).isTrue();
        assertThat(c.contains(V3_0)).isTrue();
    }

    @Test
    void shouldParseUnboundedLowerInclusiveRange() {
        final var c = VersionConstraint.parse("(,1.5]");
        assertThat(c.contains(V1_0)).isTrue();
        assertThat(c.contains(V1_5)).isTrue();
        assertThat(c.contains(V2_0)).isFalse();
    }

    @Test
    void shouldParseUnboundedLowerExclusiveRange() {
        final var c = VersionConstraint.parse("(,1.5)");
        assertThat(c.contains(V1_0)).isTrue();
        assertThat(c.contains(V1_5)).isFalse();
        assertThat(c.contains(V2_0)).isFalse();
    }

    // ── Disjoint forms ───────────────────────────────────────────────────────

    @Test
    void shouldParseDisjointInclusiveRange() {
        // (,1.0],[1.2,) — v <= 1.0 or v >= 1.2
        final var c = VersionConstraint.parse("(,1.0],[1.2,)");
        assertThat(c.contains(V0_9)).isTrue();
        assertThat(c.contains(V1_0)).isTrue();
        assertThat(c.contains(V1_1)).isFalse();
        assertThat(c.contains(V1_2)).isTrue();
        assertThat(c.contains(V3_0)).isTrue();
        assertThat(c.ranges()).hasSize(2);
    }

    @Test
    void shouldParseDisjointExclusiveRange() {
        // (,1.1),(1.1,) — v != 1.1
        final var c = VersionConstraint.parse("(,1.1),(1.1,)");
        assertThat(c.contains(V1_0)).isTrue();
        assertThat(c.contains(V1_1)).isFalse();
        assertThat(c.contains(V1_2)).isTrue();
        assertThat(c.ranges()).hasSize(2);
    }

    // ── Exclusive lower bound ────────────────────────────────────────────────

    @Test
    void shouldParseExclusiveLowerBoundedConstraint() {
        final var c = VersionConstraint.parse("(1.2,2.0]");
        assertThat(c.contains(V1_2)).isFalse();
        assertThat(c.contains(V1_5)).isTrue();
        assertThat(c.contains(V2_0)).isTrue();
        assertThat(c.contains(V3_0)).isFalse();
    }

    // ── (,X) upper-bound regression ──────────────────────────────────────────

    @Test
    void strictlyLessThanConstraintMustNotMatchVersionAtBound() {
        assertThat(VersionConstraint.parse("(,1.1)").contains(V1_1)).isFalse();
    }

    @Test
    void strictlyLessThanConstraintMustNotMatchVersionAboveBound() {
        assertThat(VersionConstraint.parse("(,1.1)").contains(V2_0)).isFalse();
    }

    // ── Invalid input ────────────────────────────────────────────────────────

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> VersionConstraint.parse(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmpty() {
        assertThatThrownBy(() -> VersionConstraint.parse(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmptyRangesList() {
        assertThatThrownBy(() -> new VersionConstraint(List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
