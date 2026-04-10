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
 * Tests for {@link Graph} construction and structural queries.
 *
 * @author reed.von.redwitz
 * @since Apr-2026
 */
class GraphTests {

    // -------------------------------------------------------------------------
    // Empty graph
    // -------------------------------------------------------------------------

    @Test
    void emptyDirectedGraphShouldHaveNoVerticesOrEdges() {
        final var graph = Graph.<String>directed().build();
        assertThat(graph.vertices()).isEmpty();
        assertThat(graph.edges()).isEmpty();
        assertThat(graph.vertexCount()).isEqualTo(0);
        assertThat(graph.edgeCount()).isEqualTo(0);
        assertThat(graph.isEmpty()).isTrue();
        assertThat(graph.isDirected()).isTrue();
    }

    @Test
    void emptyUndirectedGraphShouldBeEmpty() {
        final var graph = Graph.<String>undirected().build();
        assertThat(graph.isEmpty()).isTrue();
        assertThat(graph.isDirected()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Vertices
    // -------------------------------------------------------------------------

    @Test
    void addedVerticesShouldBePresent() {
        final var graph = Graph.<String>directed()
            .addVertex("A")
            .addVertex("B")
            .addVertex("C")
            .build();

        assertThat(graph.vertices()).containsExactly("A", "B", "C");
        assertThat(graph.vertexCount()).isEqualTo(3);
        assertThat(graph.contains("A")).isTrue();
        assertThat(graph.contains("Z")).isFalse();
    }

    @Test
    void duplicateVerticesShouldBeIgnored() {
        final var graph = Graph.<String>directed()
            .addVertex("A")
            .addVertex("A")
            .build();

        assertThat(graph.vertexCount()).isEqualTo(1);
    }

    @Test
    void addVertexShouldRejectNull() {
        assertThatThrownBy(() -> Graph.<String>directed().addVertex(null))
            .isInstanceOf(NullPointerException.class);
    }

    // -------------------------------------------------------------------------
    // Directed edges
    // -------------------------------------------------------------------------

    @Test
    void addedEdgesShouldCreateVerticesImplicitly() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .addEdge("B", "C")
            .build();

        assertThat(graph.vertices()).containsExactly("A", "B", "C");
        assertThat(graph.edgeCount()).isEqualTo(2);
    }

    @Test
    void directedGraphShouldHaveCorrectSuccessorsAndPredecessors() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .addEdge("A", "C")
            .addEdge("B", "C")
            .build();

        assertThat(graph.successors("A")).containsExactlyInAnyOrder("B", "C");
        assertThat(graph.successors("B")).containsExactly("C");
        assertThat(graph.successors("C")).isEmpty();

        assertThat(graph.predecessors("A")).isEmpty();
        assertThat(graph.predecessors("B")).containsExactly("A");
        assertThat(graph.predecessors("C")).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void directedGraphShouldCorrectlyReportEdgeExistence() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .build();

        assertThat(graph.hasEdge("A", "B")).isTrue();
        assertThat(graph.hasEdge("B", "A")).isFalse();
    }

    @Test
    void addEdgeViaEdgeRecordShouldWork() {
        final var graph = Graph.<String>directed()
            .addEdge(new Edge<>("X", "Y"))
            .build();

        assertThat(graph.hasEdge("X", "Y")).isTrue();
        assertThat(graph.edgeCount()).isEqualTo(1);
    }

    @Test
    void duplicateEdgesShouldBeIgnored() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .addEdge("A", "B")
            .build();

        assertThat(graph.edgeCount()).isEqualTo(1);
    }

    @Test
    void selfLoopShouldBeSupported() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "A")
            .build();

        assertThat(graph.hasEdge("A", "A")).isTrue();
        assertThat(graph.successors("A")).containsExactly("A");
        assertThat(graph.predecessors("A")).containsExactly("A");
    }

    // -------------------------------------------------------------------------
    // Undirected edges
    // -------------------------------------------------------------------------

    @Test
    void undirectedEdgeShouldBeNavigableInBothDirections() {
        final var graph = Graph.<String>undirected()
            .addEdge("A", "B")
            .build();

        assertThat(graph.hasEdge("A", "B")).isTrue();
        assertThat(graph.hasEdge("B", "A")).isTrue();
        assertThat(graph.successors("A")).containsExactly("B");
        assertThat(graph.successors("B")).containsExactly("A");
    }

    @Test
    void undirectedGraphEdgeCountShouldCountLogicalEdges() {
        final var graph = Graph.<String>undirected()
            .addEdge("A", "B")
            .addEdge("B", "C")
            .build();

        // 2 undirected edges, not 4 directed entries
        assertThat(graph.edgeCount()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // Disconnected components
    // -------------------------------------------------------------------------

    @Test
    void disconnectedGraphShouldContainIsolatedVertices() {
        final var graph = Graph.<String>directed()
            .addVertex("A")
            .addVertex("B")
            .addEdge("C", "D")
            .build();

        assertThat(graph.vertexCount()).isEqualTo(4);
        assertThat(graph.successors("A")).isEmpty();
        assertThat(graph.predecessors("B")).isEmpty();
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Test
    void toStringShouldDescribeGraph() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .build();

        assertThat(graph.toString()).contains("directed=true", "vertices=2", "edges=1");
    }
}
