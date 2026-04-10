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
 * Tests for {@link WeightedGraph} construction and structural queries.
 *
 * @author reed.von.redwitz
 * @since Apr-2026
 */
class WeightedGraphTests {

    @Test
    void emptyWeightedGraphShouldBeEmpty() {
        final var graph = WeightedGraph.<String, Integer>directed().build();
        assertThat(graph.isEmpty()).isTrue();
        assertThat(graph.vertices()).isEmpty();
        assertThat(graph.edges()).isEmpty();
        assertThat(graph.vertexCount()).isEqualTo(0);
        assertThat(graph.edgeCount()).isEqualTo(0);
    }

    @Test
    void addedVerticesShouldBePresent() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addVertex("A")
            .addVertex("B")
            .build();

        assertThat(graph.vertices()).containsExactly("A", "B");
        assertThat(graph.contains("A")).isTrue();
        assertThat(graph.contains("Z")).isFalse();
    }

    @Test
    void edgeShouldStoreWeight() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 7)
            .build();

        assertThat(graph.hasEdge("A", "B")).isTrue();
        assertThat(graph.weight("A", "B")).hasValue(7);
        assertThat(graph.edge("A", "B")).hasValueSatisfying(e -> {
            assertThat(e.from()).isEqualTo("A");
            assertThat(e.to()).isEqualTo("B");
            assertThat(e.weight()).isEqualTo(7);
        });
    }

    @Test
    void missingEdgeShouldReturnEmpty() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 5)
            .build();

        assertThat(graph.weight("B", "A")).isEmpty();
        assertThat(graph.edge("B", "A")).isEmpty();
        assertThat(graph.hasEdge("B", "A")).isFalse();
    }

    @Test
    void laterWeightShouldOverridePrevious() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 3)
            .addEdge("A", "B", 99)
            .build();

        assertThat(graph.weight("A", "B")).hasValue(99);
        assertThat(graph.edgeCount()).isEqualTo(1);
    }

    @Test
    void undirectedWeightedEdgeShouldBeNavigableInBothDirections() {
        final var graph = WeightedGraph.<String, Integer>undirected()
            .addEdge("A", "B", 4)
            .build();

        assertThat(graph.hasEdge("A", "B")).isTrue();
        assertThat(graph.hasEdge("B", "A")).isTrue();
        assertThat(graph.weight("A", "B")).hasValue(4);
        assertThat(graph.weight("B", "A")).hasValue(4);
    }

    @Test
    void addEdgeViaWeightedEdgeRecordShouldWork() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge(new WeightedEdge<>("X", "Y", 12))
            .build();

        assertThat(graph.hasEdge("X", "Y")).isTrue();
        assertThat(graph.weight("X", "Y")).hasValue(12);
    }

    @Test
    void successorsShouldListOutgoingVertices() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 1)
            .addEdge("A", "C", 2)
            .build();

        assertThat(graph.successors("A")).containsExactlyInAnyOrder("B", "C");
        assertThat(graph.successors("B")).isEmpty();
    }

    @Test
    void predecessorsShouldListIncomingVertices() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 1)
            .addEdge("A", "C", 2)
            .addEdge("B", "C", 3)
            .build();

        assertThat(graph.predecessors("A")).isEmpty();
        assertThat(graph.predecessors("B")).containsExactly("A");
        assertThat(graph.predecessors("C")).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void toUnweightedGraphShouldPreserveStructure() {
        final var weighted = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 1)
            .addEdge("B", "C", 2)
            .build();

        final var unweighted = weighted.toUnweightedGraph();
        assertThat(unweighted.vertices()).containsExactly("A", "B", "C");
        assertThat(unweighted.hasEdge("A", "B")).isTrue();
        assertThat(unweighted.hasEdge("B", "C")).isTrue();
        assertThat(unweighted.isDirected()).isTrue();
    }

    @Test
    void addVertexShouldRejectNull() {
        assertThatThrownBy(() -> WeightedGraph.<String, Integer>directed().addVertex(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addEdgeShouldRejectNullWeight() {
        assertThatThrownBy(() -> WeightedGraph.<String, Integer>directed().addEdge("A", "B", null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringShouldDescribeGraph() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 5)
            .build();
        assertThat(graph.toString()).contains("directed=true", "vertices=2", "edges=1");
    }
}
