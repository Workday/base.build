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

/**
 * Tests for {@link VersionBound}.
 */
class VersionBoundTests {

    private static final Version V1_0 = Version.parse("1.0");
    private static final Version V1_5 = Version.parse("1.5");
    private static final Version V2_0 = Version.parse("2.0");

    // ── Lower inclusive (>=) ─────────────────────────────────────────────────

    @Test
    void lowerInclusiveShouldAcceptVersionAtBound() {
        assertThat(VersionBound.lower(V1_5, true).test(V1_5)).isTrue();
    }

    @Test
    void lowerInclusiveShouldAcceptVersionAboveBound() {
        assertThat(VersionBound.lower(V1_5, true).test(V2_0)).isTrue();
    }

    @Test
    void lowerInclusiveShouldRejectVersionBelowBound() {
        assertThat(VersionBound.lower(V1_5, true).test(V1_0)).isFalse();
    }

    // ── Lower exclusive (>) ──────────────────────────────────────────────────

    @Test
    void lowerExclusiveShouldRejectVersionAtBound() {
        assertThat(VersionBound.lower(V1_5, false).test(V1_5)).isFalse();
    }

    @Test
    void lowerExclusiveShouldAcceptVersionAboveBound() {
        assertThat(VersionBound.lower(V1_5, false).test(V2_0)).isTrue();
    }

    // ── Upper inclusive (<=) ─────────────────────────────────────────────────

    @Test
    void upperInclusiveShouldAcceptVersionAtBound() {
        assertThat(VersionBound.upper(V1_5, true).test(V1_5)).isTrue();
    }

    @Test
    void upperInclusiveShouldAcceptVersionBelowBound() {
        assertThat(VersionBound.upper(V1_5, true).test(V1_0)).isTrue();
    }

    @Test
    void upperInclusiveShouldRejectVersionAboveBound() {
        assertThat(VersionBound.upper(V1_5, true).test(V2_0)).isFalse();
    }

    // ── Upper exclusive (<) ──────────────────────────────────────────────────

    @Test
    void upperExclusiveShouldRejectVersionAtBound() {
        assertThat(VersionBound.upper(V1_5, false).test(V1_5)).isFalse();
    }

    @Test
    void upperExclusiveShouldRejectVersionAboveBound() {
        assertThat(VersionBound.upper(V1_5, false).test(V2_0)).isFalse();
    }

    @Test
    void upperExclusiveShouldAcceptVersionBelowBound() {
        assertThat(VersionBound.upper(V1_5, false).test(V1_0)).isTrue();
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    @Test
    void shouldExposeVersion() {
        assertThat(VersionBound.lower(V1_5, true).version()).isEqualTo(V1_5);
    }

    @Test
    void shouldReportIsLowerAndIsUpper() {
        assertThat(VersionBound.lower(V1_5, true).isLower()).isTrue();
        assertThat(VersionBound.lower(V1_5, true).isUpper()).isFalse();
        assertThat(VersionBound.upper(V1_5, true).isUpper()).isTrue();
        assertThat(VersionBound.upper(V1_5, true).isLower()).isFalse();
    }

    @Test
    void shouldReportInclusivity() {
        assertThat(VersionBound.lower(V1_5, true).isInclusive()).isTrue();
        assertThat(VersionBound.lower(V1_5, false).isInclusive()).isFalse();
    }
}
