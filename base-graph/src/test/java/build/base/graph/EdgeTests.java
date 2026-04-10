package build.base.graph;

/*-
 * #%L
 * base.build Graph
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
 * Tests for {@link Edge} and {@link WeightedEdge}.
 *
 * @author reed.von.redwitz
 * @since Apr-2026
 */
class EdgeTests {

    // -------------------------------------------------------------------------
    // Edge
    // -------------------------------------------------------------------------

    @Test
    void edgeShouldStoreFromAndTo() {
        final var edge = new Edge<>("A", "B");
        assertThat(edge.from()).isEqualTo("A");
        assertThat(edge.to()).isEqualTo("B");
    }

    @Test
    void edgeShouldRejectNullFrom() {
        assertThatThrownBy(() -> new Edge<>(null, "B"))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void edgeShouldRejectNullTo() {
        assertThatThrownBy(() -> new Edge<>("A", null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void edgeShouldReverse() {
        final var edge = new Edge<>("A", "B");
        final var reversed = edge.reversed();
        assertThat(reversed.from()).isEqualTo("B");
        assertThat(reversed.to()).isEqualTo("A");
    }

    @Test
    void edgeSelfLoopShouldBeDetected() {
        assertThat(new Edge<>("A", "A").isSelfLoop()).isTrue();
        assertThat(new Edge<>("A", "B").isSelfLoop()).isFalse();
    }

    @Test
    void edgeShouldSupportValueEquality() {
        final var e1 = new Edge<>("A", "B");
        final var e2 = new Edge<>("A", "B");
        final var e3 = new Edge<>("B", "A");
        assertThat(e1).isEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
    }

    // -------------------------------------------------------------------------
    // WeightedEdge
    // -------------------------------------------------------------------------

    @Test
    void weightedEdgeShouldStoreFields() {
        final var edge = new WeightedEdge<>("A", "B", 5);
        assertThat(edge.from()).isEqualTo("A");
        assertThat(edge.to()).isEqualTo("B");
        assertThat(edge.weight()).isEqualTo(5);
    }

    @Test
    void weightedEdgeShouldRejectNullWeight() {
        assertThatThrownBy(() -> new WeightedEdge<>("A", "B", null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void weightedEdgeShouldReverse() {
        final var edge = new WeightedEdge<>("A", "B", 10);
        final var reversed = edge.reversed();
        assertThat(reversed.from()).isEqualTo("B");
        assertThat(reversed.to()).isEqualTo("A");
        assertThat(reversed.weight()).isEqualTo(10);
    }

    @Test
    void weightedEdgeShouldConvertToEdge() {
        final var weighted = new WeightedEdge<>("X", "Y", 42);
        final var edge = weighted.toEdge();
        assertThat(edge).isEqualTo(new Edge<>("X", "Y"));
    }

    @Test
    void weightedEdgeSelfLoopShouldBeDetected() {
        assertThat(new WeightedEdge<>("A", "A", 1).isSelfLoop()).isTrue();
        assertThat(new WeightedEdge<>("A", "B", 1).isSelfLoop()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Path
    // -------------------------------------------------------------------------

    @Test
    void pathShouldBuildFromVertices() {
        final var path = Path.of(java.util.List.of("A", "B", "C"));
        assertThat(path.start()).isEqualTo("A");
        assertThat(path.end()).isEqualTo("C");
        assertThat(path.length()).isEqualTo(2);
        assertThat(path.edges()).containsExactly(new Edge<>("A", "B"), new Edge<>("B", "C"));
    }

    @Test
    void pathShouldBuildFromSingleVertex() {
        final var path = Path.of("A");
        assertThat(path.start()).isEqualTo("A");
        assertThat(path.end()).isEqualTo("A");
        assertThat(path.length()).isEqualTo(0);
        assertThat(path.edges()).isEmpty();
    }

    @Test
    void pathShouldRejectEmptyVertexList() {
        assertThatThrownBy(() -> Path.of(java.util.List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pathShouldRejectMismatchedEdgeCount() {
        assertThatThrownBy(() -> new Path<>(java.util.List.of("A", "B"), java.util.List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------------------------------------------------------
    // Component
    // -------------------------------------------------------------------------

    @Test
    void componentShouldStoreVertices() {
        final var component = new Component<>(java.util.Set.of("A", "B", "C"));
        assertThat(component.vertices()).containsExactlyInAnyOrder("A", "B", "C");
        assertThat(component.size()).isEqualTo(3);
    }

    @Test
    void componentShouldRejectEmptySet() {
        assertThatThrownBy(() -> new Component<>(java.util.Set.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void trivialComponentShouldBeDetected() {
        assertThat(new Component<>(java.util.Set.of("A")).isTrivial()).isTrue();
        assertThat(new Component<>(java.util.Set.of("A", "B")).isTrivial()).isFalse();
        assertThat(new Component<>(java.util.Set.of("A", "B")).isNonTrivial()).isTrue();
    }

    @Test
    void componentShouldCheckContainment() {
        final var component = new Component<>(java.util.Set.of("A", "B"));
        assertThat(component.contains("A")).isTrue();
        assertThat(component.contains("C")).isFalse();
    }
}
