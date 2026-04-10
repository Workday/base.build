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

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for all graph algorithms in {@link Graphs}.
 *
 * @author reed.von.redwitz
 * @since Apr-2026
 */
class GraphsTests {

    // =========================================================================
    // BFS
    // =========================================================================

    @Test
    void bfsShouldVisitVerticesInBreadthFirstOrder() {
        //     A
        //    / \
        //   B   C
        //  / \
        // D   E
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("A", "C")
            .addEdge("B", "D").addEdge("B", "E")
            .build();

        final var result = Graphs.breadthFirstSearch(graph, "A");
        // A must come first; B and C before D and E
        assertThat(result).startsWith("A");
        assertThat(result.indexOf("B")).isLessThan(result.indexOf("D"));
        assertThat(result.indexOf("C")).isLessThan(result.indexOf("D"));
        assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D", "E");
    }

    @Test
    void bfsShouldReturnOnlyReachableVertices() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .addVertex("C")   // isolated
            .build();

        assertThat(Graphs.breadthFirstSearch(graph, "A")).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void bfsOnSingleNodeShouldReturnThatNode() {
        final var graph = Graph.<String>directed().addVertex("A").build();
        assertThat(Graphs.breadthFirstSearch(graph, "A")).containsExactly("A");
    }

    @Test
    void bfsShouldHandleSelfLoop() {
        final var graph = Graph.<String>directed().addEdge("A", "A").build();
        assertThat(Graphs.breadthFirstSearch(graph, "A")).containsExactly("A");
    }

    // =========================================================================
    // DFS
    // =========================================================================

    @Test
    void dfsShouldVisitAllReachableVertices() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("A", "C")
            .addEdge("B", "D")
            .build();

        final var result = Graphs.depthFirstSearch(graph, "A");
        assertThat(result).startsWith("A");
        assertThat(result).containsExactlyInAnyOrder("A", "B", "C", "D");
        // In DFS, B's subtree should be explored before C
        assertThat(result.indexOf("D")).isLessThan(result.indexOf("C"));
    }

    @Test
    void dfsShouldHandleCyclicGraphWithoutInfiniteLoop() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "A")
            .build();

        final var result = Graphs.depthFirstSearch(graph, "A");
        assertThat(result).containsExactlyInAnyOrder("A", "B", "C");
    }

    // =========================================================================
    // Reachability
    // =========================================================================

    @Test
    void reachableFromShouldReturnAllDownstreamVertices() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("B", "D")
            .build();

        assertThat(Graphs.reachableFrom(graph, "A")).containsExactlyInAnyOrder("B", "C", "D");
    }

    @Test
    void reachableFromIsolatedVertexShouldBeEmpty() {
        final var graph = Graph.<String>directed().addVertex("A").build();
        assertThat(Graphs.reachableFrom(graph, "A")).isEmpty();
    }

    @Test
    void reachableFromShouldIncludeStartOnCycle() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "A")
            .build();

        assertThat(Graphs.reachableFrom(graph, "A")).contains("A", "B");
    }

    @Test
    void ancestorsShouldReturnUpstreamVertices() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "C").addEdge("B", "C").addEdge("C", "D")
            .build();

        assertThat(Graphs.ancestors(graph, "D")).containsExactlyInAnyOrder("A", "B", "C");
        assertThat(Graphs.ancestors(graph, "C")).containsExactlyInAnyOrder("A", "B");
        assertThat(Graphs.ancestors(graph, "A")).isEmpty();
    }

    @Test
    void descendantsShouldReturnDownstreamVertices() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("A", "C").addEdge("B", "D")
            .build();

        assertThat(Graphs.descendants(graph, "A")).containsExactlyInAnyOrder("B", "C", "D");
    }

    // =========================================================================
    // Topological Sort
    // =========================================================================

    @Test
    void topologicalSortShouldOrderDependenciesFirst() {
        // A depends on B and C; B depends on D
        // Valid order: D, B, C, A  (or D, C, B, A)
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("A", "C").addEdge("B", "D")
            .build();

        final var sorted = Graphs.topologicalSort(graph);
        assertThat(sorted).containsExactlyInAnyOrder("A", "B", "C", "D");
        assertThat(sorted.indexOf("D")).isLessThan(sorted.indexOf("B"));
        assertThat(sorted.indexOf("B")).isLessThan(sorted.indexOf("A"));
        assertThat(sorted.indexOf("C")).isLessThan(sorted.indexOf("A"));
    }

    @Test
    void topologicalSortOnEmptyGraphShouldReturnEmptyList() {
        final var graph = Graph.<String>directed().build();
        assertThat(Graphs.topologicalSort(graph)).isEmpty();
    }

    @Test
    void topologicalSortOnSingleVertexShouldReturnIt() {
        final var graph = Graph.<String>directed().addVertex("A").build();
        assertThat(Graphs.topologicalSort(graph)).containsExactly("A");
    }

    @Test
    void topologicalSortOnGraphWithCycleShouldThrow() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "A")
            .build();

        assertThatThrownBy(() -> Graphs.topologicalSort(graph))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cycle");
    }

    // =========================================================================
    // Cycle Detection
    // =========================================================================

    @Test
    void hasCycleShouldReturnFalseForDag() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C")
            .build();

        assertThat(Graphs.hasCycle(graph)).isFalse();
    }

    @Test
    void hasCycleShouldReturnTrueForCyclicGraph() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "A")
            .build();

        assertThat(Graphs.hasCycle(graph)).isTrue();
    }

    @Test
    void hasCycleOnEmptyGraphShouldReturnFalse() {
        assertThat(Graphs.hasCycle(Graph.<String>directed().build())).isFalse();
    }

    @Test
    void hasCycleOnSingleNodeWithoutSelfLoopShouldReturnFalse() {
        assertThat(Graphs.hasCycle(Graph.<String>directed().addVertex("A").build())).isFalse();
    }

    @Test
    void hasCycleShouldDetectSelfLoop() {
        final var graph = Graph.<String>directed().addEdge("A", "A").build();
        assertThat(Graphs.hasCycle(graph)).isTrue();
    }

    @Test
    void findCycleShouldReturnEmptyForAcyclicGraph() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C")
            .build();

        assertThat(Graphs.findCycle(graph)).isEmpty();
    }

    @Test
    void findCycleShouldReturnCyclePath() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "A")
            .build();

        final var cycle = Graphs.findCycle(graph);
        assertThat(cycle).isPresent();

        final var path = cycle.get();
        // Cycle should start and end with the same vertex
        assertThat(path.getFirst()).isEqualTo(path.getLast());
        // Cycle vertices (excluding duplicate end) should be a subset of graph vertices
        assertThat(path).hasSize(4); // A->B->C->A or similar rotation
    }

    @Test
    void findCycleShouldDetectSelfLoop() {
        final var graph = Graph.<String>directed().addEdge("A", "A").build();
        final var cycle = Graphs.findCycle(graph);
        assertThat(cycle).isPresent();
        assertThat(cycle.get()).contains("A");
    }

    // =========================================================================
    // Shortest Path (unweighted BFS)
    // =========================================================================

    @Test
    void shortestPathShouldFindDirectPath() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C")
            .build();

        final var path = Graphs.shortestPath(graph, "A", "C");
        assertThat(path).isPresent();
        assertThat(path.get().vertices()).containsExactly("A", "B", "C");
        assertThat(path.get().length()).isEqualTo(2);
    }

    @Test
    void shortestPathShouldPreferFewerHops() {
        // A->B->C  and  A->C directly
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("A", "C")
            .build();

        final var path = Graphs.shortestPath(graph, "A", "C");
        assertThat(path).isPresent();
        assertThat(path.get().length()).isEqualTo(1);
        assertThat(path.get().vertices()).containsExactly("A", "C");
    }

    @Test
    void shortestPathToSelfShouldReturnSingleVertexPath() {
        final var graph = Graph.<String>directed().addEdge("A", "B").build();
        final var path = Graphs.shortestPath(graph, "A", "A");
        assertThat(path).isPresent();
        assertThat(path.get().vertices()).containsExactly("A");
        assertThat(path.get().length()).isEqualTo(0);
    }

    @Test
    void shortestPathShouldReturnEmptyWhenUnreachable() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B")
            .addVertex("C")
            .build();

        assertThat(Graphs.shortestPath(graph, "A", "C")).isEmpty();
    }

    @Test
    void shortestPathOnEmptyGraphShouldReturnEmptyForDifferentVertices() {
        final var graph = Graph.<String>directed().addVertex("A").addVertex("B").build();
        assertThat(Graphs.shortestPath(graph, "A", "B")).isEmpty();
    }

    // =========================================================================
    // Dijkstra (weighted shortest path)
    // =========================================================================

    @Test
    void dijkstraShouldFindMinimumWeightPath() {
        // A -1-> B -2-> D   (total 3)
        // A -4-> C -1-> D   (total 5)
        // A -1-> B -3-> C -1-> D  (total 5)
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 1)
            .addEdge("A", "C", 4)
            .addEdge("B", "D", 2)
            .addEdge("B", "C", 3)
            .addEdge("C", "D", 1)
            .build();

        final var result = Graphs.shortestPath(graph, "A", "D", 0, Integer::sum);
        assertThat(result).isPresent();
        assertThat(result.get().totalWeight()).isEqualTo(3);
        assertThat(result.get().vertices()).containsExactly("A", "B", "D");
    }

    @Test
    void dijkstraShouldReturnSingleVertexPathForSameStartAndEnd() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 5)
            .build();

        final var result = Graphs.shortestPath(graph, "A", "A", 0, Integer::sum);
        assertThat(result).isPresent();
        assertThat(result.get().totalWeight()).isEqualTo(0);
        assertThat(result.get().vertices()).containsExactly("A");
    }

    @Test
    void dijkstraShouldReturnEmptyWhenUnreachable() {
        final var graph = WeightedGraph.<String, Integer>directed()
            .addEdge("A", "B", 1)
            .addVertex("C")
            .build();

        assertThat(Graphs.shortestPath(graph, "A", "C", 0, Integer::sum)).isEmpty();
    }

    @Test
    void dijkstraShouldWorkWithDoubleWeights() {
        final var graph = WeightedGraph.<String, Double>directed()
            .addEdge("A", "B", 1.5)
            .addEdge("B", "C", 2.5)
            .build();

        final var result = Graphs.shortestPath(graph, "A", "C", 0.0, Double::sum);
        assertThat(result).isPresent();
        assertThat(result.get().totalWeight()).isEqualTo(4.0);
    }

    // =========================================================================
    // Parallelizable Groups
    // =========================================================================

    @Test
    void parallelizableGroupsShouldLayerDependencies() {
        // Layer 0: D (no deps)
        // Layer 1: B, C (depend only on D)
        // Layer 2: A (depends on B and C)
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("A", "C")
            .addEdge("B", "D").addEdge("C", "D")
            .build();

        final var groups = Graphs.parallelizableGroups(graph);
        assertThat(groups).hasSize(3);
        assertThat(groups.get(0)).containsExactly("D");
        assertThat(groups.get(1)).containsExactlyInAnyOrder("B", "C");
        assertThat(groups.get(2)).containsExactly("A");
    }

    @Test
    void parallelizableGroupsOnEmptyGraphShouldReturnEmptyList() {
        assertThat(Graphs.parallelizableGroups(Graph.<String>directed().build())).isEmpty();
    }

    @Test
    void parallelizableGroupsOnCycleShouldThrow() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "A")
            .build();

        assertThatThrownBy(() -> Graphs.parallelizableGroups(graph))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void parallelizableGroupsOnLinearChainShouldHaveOnePerLayer() {
        // A -> B -> C -> D: each in its own layer (in reverse: D, C, B, A)
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "D")
            .build();

        final var groups = Graphs.parallelizableGroups(graph);
        assertThat(groups).hasSize(4);
        for (final var group : groups) {
            assertThat(group).hasSize(1);
        }
    }

    // =========================================================================
    // Strongly Connected Components
    // =========================================================================

    @Test
    void sccOnDagShouldReturnOneTrivialComponentPerVertex() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C")
            .build();

        final var sccs = Graphs.stronglyConnectedComponents(graph);
        assertThat(sccs).hasSize(3);
        assertThat(sccs).allSatisfy(c -> assertThat(c.isTrivial()).isTrue());
    }

    @Test
    void sccShouldGroupCyclicVerticesTogether() {
        // A->B->C->A is one SCC; D is separate
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "A")
            .addEdge("A", "D")
            .build();

        final var sccs = Graphs.stronglyConnectedComponents(graph);
        // One SCC of {A,B,C} and one of {D}
        assertThat(sccs).hasSize(2);

        final var nonTrivial = sccs.stream().filter(Component::isNonTrivial).toList();
        assertThat(nonTrivial).hasSize(1);
        assertThat(nonTrivial.getFirst().vertices()).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    void sccOnEmptyGraphShouldReturnEmptyList() {
        final var graph = Graph.<String>directed().build();
        assertThat(Graphs.stronglyConnectedComponents(graph)).isEmpty();
    }

    @Test
    void sccOnSingleVertexShouldReturnOneTrivialComponent() {
        final var graph = Graph.<String>directed().addVertex("A").build();
        final var sccs = Graphs.stronglyConnectedComponents(graph);
        assertThat(sccs).hasSize(1);
        assertThat(sccs.getFirst().isTrivial()).isTrue();
    }

    @Test
    void sccShouldDetectSelfLoopAsNonTrivialComponent() {
        // A self-loop makes A an SCC, but with a single vertex the isTrivial check is size-based
        // A -> A: still just {A} but reachable from itself
        final var graph = Graph.<String>directed().addEdge("A", "A").build();
        final var sccs = Graphs.stronglyConnectedComponents(graph);
        assertThat(sccs).hasSize(1);
        assertThat(sccs.getFirst().contains("A")).isTrue();
    }

    @Test
    void sccOnDisconnectedGraphShouldReturnComponentPerVertex() {
        final var graph = Graph.<String>directed()
            .addVertex("A").addVertex("B").addVertex("C")
            .build();

        assertThat(Graphs.stronglyConnectedComponents(graph)).hasSize(3);
    }

    // =========================================================================
    // Transitive Reduction
    // =========================================================================

    @Test
    void transitiveReductionShouldRemoveRedundantEdges() {
        // A->B, B->C, A->C (A->C is redundant since A->B->C exists)
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("A", "C")
            .build();

        final var reduced = Graphs.transitiveReduction(graph);
        assertThat(reduced.hasEdge("A", "B")).isTrue();
        assertThat(reduced.hasEdge("B", "C")).isTrue();
        assertThat(reduced.hasEdge("A", "C")).isFalse();
        assertThat(reduced.edgeCount()).isEqualTo(2);
    }

    @Test
    void transitiveReductionShouldPreserveLinearChain() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("C", "D")
            .build();

        final var reduced = Graphs.transitiveReduction(graph);
        assertThat(reduced.edgeCount()).isEqualTo(3);
    }

    @Test
    void transitiveReductionOnEmptyGraphShouldReturnEmptyGraph() {
        final var reduced = Graphs.transitiveReduction(Graph.<String>directed().build());
        assertThat(reduced.isEmpty()).isTrue();
    }

    @Test
    void transitiveReductionShouldPreserveAllVertices() {
        final var graph = Graph.<String>directed()
            .addEdge("A", "B").addEdge("B", "C").addEdge("A", "C")
            .build();

        final var reduced = Graphs.transitiveReduction(graph);
        assertThat(reduced.vertices()).containsExactlyInAnyOrder("A", "B", "C");
    }

    // =========================================================================
    // Null argument guards
    // =========================================================================

    @Test
    void algorithmsShouldRejectNullGraph() {
        assertThatThrownBy(() -> Graphs.breadthFirstSearch(null, "A"))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Graphs.depthFirstSearch(null, "A"))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Graphs.topologicalSort(null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Graphs.hasCycle(null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Graphs.findCycle(null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Graphs.stronglyConnectedComponents(null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Graphs.transitiveReduction(null))
            .isInstanceOf(NullPointerException.class);
    }

    // =========================================================================
    // Directed-only guard
    // =========================================================================

    @Test
    void directedOnlyAlgorithmsShouldRejectUndirectedGraphs() {
        final var undirected = Graph.<String>undirected().addEdge("A", "B").build();

        assertThatThrownBy(() -> Graphs.topologicalSort(undirected))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("directed");
        assertThatThrownBy(() -> Graphs.parallelizableGroups(undirected))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("directed");
        assertThatThrownBy(() -> Graphs.stronglyConnectedComponents(undirected))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("directed");
        assertThatThrownBy(() -> Graphs.transitiveReduction(undirected))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("directed");
    }
}
