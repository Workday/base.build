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
 * Tests for {@link Multigraph} and {@link MultiEdge}.
 *
 * @author reed.von.redwitz
 * @since Apr-2026
 */
class MultigraphTests {

    // -------------------------------------------------------------------------
    // Basic structure
    // -------------------------------------------------------------------------

    @Test
    void emptyMultigraphShouldBeEmpty() {
        final var graph = Multigraph.<String>directed().build();
        assertThat(graph.isEmpty()).isTrue();
        assertThat(graph.vertices()).isEmpty();
        assertThat(graph.edges()).isEmpty();
        assertThat(graph.isDirected()).isTrue();
    }

    @Test
    void addedVerticesShouldBePresent() {
        final var graph = Multigraph.<String>directed()
            .addVertex("A")
            .addVertex("B")
            .build();

        assertThat(graph.vertices()).containsExactly("A", "B");
        assertThat(graph.contains("A")).isTrue();
        assertThat(graph.contains("Z")).isFalse();
    }

    // -------------------------------------------------------------------------
    // Parallel edges — the core multigraph feature
    // -------------------------------------------------------------------------

    @Test
    void parallelEdgesShouldBeDistinctObjects() {
        final var builder = Multigraph.<String>directed();
        final MultiEdge<String> e1 = builder.addEdge("A", "B");
        final MultiEdge<String> e2 = builder.addEdge("A", "B");
        final var graph = builder.build();

        assertThat(e1).isNotSameAs(e2);
        assertThat(graph.edgeCount()).isEqualTo(2);
        assertThat(graph.multiplicity("A", "B")).isEqualTo(2);
        assertThat(graph.edgesConnecting("A", "B")).hasSize(2);
        assertThat(graph.edgesConnecting("A", "B")).containsExactly(e1, e2);
    }

    @Test
    void hasEdgeShouldReturnTrueWhenAnyParallelEdgeExists() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        final var graph = builder.build();

        assertThat(graph.hasEdge("A", "B")).isTrue();
        assertThat(graph.hasEdge("B", "A")).isFalse();
    }

    @Test
    void successorsShouldReturnDistinctVerticesDespiteParallelEdges() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");
        builder.addEdge("A", "C");
        final var graph = builder.build();

        // successors returns distinct vertices, not one per edge
        assertThat(graph.successors("A")).containsExactlyInAnyOrder("B", "C");
    }

    @Test
    void predecessorsShouldReturnDistinctVerticesDespiteParallelEdges() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "C");
        builder.addEdge("A", "C");
        builder.addEdge("B", "C");
        final var graph = builder.build();

        assertThat(graph.predecessors("C")).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void edgeCountShouldCountEachParallelEdgeSeparately() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");
        final var graph = builder.build();

        assertThat(graph.edgeCount()).isEqualTo(3);
        assertThat(graph.multiplicity("A", "B")).isEqualTo(3);
        assertThat(graph.multiplicity("B", "A")).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Undirected multigraph
    // -------------------------------------------------------------------------

    @Test
    void undirectedParallelEdgeShouldBeNavigableInBothDirections() {
        final var builder = Multigraph.<String>undirected();
        builder.addEdge("A", "B");
        final var graph = builder.build();

        assertThat(graph.hasEdge("A", "B")).isTrue();
        assertThat(graph.hasEdge("B", "A")).isTrue();
        assertThat(graph.successors("A")).containsExactly("B");
        assertThat(graph.successors("B")).containsExactly("A");
        // undirected: only 1 logical edge in the edges() list
        assertThat(graph.edgeCount()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // MultiEdge identity
    // -------------------------------------------------------------------------

    @Test
    void multiEdgeShouldHaveReferenceIdentity() {
        final var e1 = new MultiEdge<>("A", "B");
        final var e2 = new MultiEdge<>("A", "B");

        // Same endpoints, different objects — must NOT be equal
        assertThat(e1).isNotEqualTo(e2);
        assertThat(e1).isEqualTo(e1);
    }

    @Test
    void multiEdgeSelfLoopShouldBeDetected() {
        final var e = new MultiEdge<>("A", "A");
        assertThat(e.isSelfLoop()).isTrue();

        final var e2 = new MultiEdge<>("A", "B");
        assertThat(e2.isSelfLoop()).isFalse();
    }

    @Test
    void toEdgeShouldProduceValueTypeEdge() {
        final var me = new MultiEdge<>("X", "Y");
        final var e = me.toEdge();

        assertThat(e.from()).isEqualTo("X");
        assertThat(e.to()).isEqualTo("Y");
        assertThat(e).isEqualTo(new Edge<>("X", "Y"));
    }

    // -------------------------------------------------------------------------
    // toSimpleGraph
    // -------------------------------------------------------------------------

    @Test
    void toSimpleGraphShouldCollapseParallelEdges() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");
        builder.addEdge("B", "C");
        final var graph = builder.build();

        final var simple = graph.toSimpleGraph();
        assertThat(simple.edgeCount()).isEqualTo(2);
        assertThat(simple.hasEdge("A", "B")).isTrue();
        assertThat(simple.hasEdge("B", "C")).isTrue();
    }

    // -------------------------------------------------------------------------
    // Algorithms via VertexGraph — the payoff of the shared interface
    // -------------------------------------------------------------------------

    @Test
    void bfsShouldWorkOnMultigraph() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");  // parallel edge — should not affect traversal
        builder.addEdge("B", "C");
        final var graph = builder.build();

        final var result = GraphTraversal.breadthFirstSearch(graph, "A");
        assertThat(result).containsExactly("A", "B", "C");
    }

    @Test
    void topologicalSortShouldWorkOnMultigraph() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");  // parallel
        builder.addEdge("B", "C");
        final var graph = builder.build();

        final var sorted = GraphOrdering.topologicalSort(graph);
        assertThat(sorted.indexOf("C")).isLessThan(sorted.indexOf("B"));
        assertThat(sorted.indexOf("B")).isLessThan(sorted.indexOf("A"));
    }

    @Test
    void cycleDetectionShouldWorkOnMultigraph() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("B", "A");  // cycle
        final var graph = builder.build();

        assertThat(GraphCycles.hasCycle(graph)).isTrue();
    }

    @Test
    void sccShouldWorkOnMultigraph() {
        final var builder = Multigraph.<String>directed();
        builder.addEdge("A", "B");
        builder.addEdge("A", "B");  // parallel
        builder.addEdge("B", "C");
        builder.addEdge("C", "A");  // cycle: A-B-C-A
        final var graph = builder.build();

        final var sccs = GraphConnectivity.stronglyConnectedComponents(graph);
        assertThat(sccs).hasSize(1);
        assertThat(sccs.getFirst().vertices()).containsExactlyInAnyOrder("A", "B", "C");
    }

    // -------------------------------------------------------------------------
    // Null guards
    // -------------------------------------------------------------------------

    @Test
    void addVertexShouldRejectNull() {
        assertThatThrownBy(() -> Multigraph.<String>directed().addVertex(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addEdgeShouldRejectNullVertices() {
        assertThatThrownBy(() -> Multigraph.<String>directed().addEdge(null, "B"))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Multigraph.<String>directed().addEdge("A", null))
            .isInstanceOf(NullPointerException.class);
    }
}
