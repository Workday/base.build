package build.base.foundation;

import build.base.foundation.stream.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for Topological Sort.
 *
 * @author andrew.wilson
 * @since Mar-2021
 */
class TopologicalSortTests {

    /**
     * This checks a graph with tight dependencies to check the path functionality.
     */
    @Test
    void shouldAllowATightDependency() {
        final List<SimpleGraphNode> graph = Arrays.asList(
            new SimpleGraphNode(0), new SimpleGraphNode(1), new SimpleGraphNode(2), new SimpleGraphNode(3));
        graph.get(0).addDependency(graph.get(1));
        graph.get(0).addDependency(graph.get(2));
        graph.get(0).addDependency(graph.get(3));
        graph.get(1).addDependency(graph.get(2));
        graph.get(2).addDependency(graph.get(3));
        graph.get(1).addDependency(graph.get(3));
        assertEquals(Arrays.asList(0, 1, 2, 3),
            Streams.topologicalSort(graph.stream(), SimpleGraphNode::getDependencies, null, Streams.SortOrder.UNCHANGED)
                .map(SimpleGraphNode.class::cast)
                .map(SimpleGraphNode::getId)
                .collect(Collectors.toList()));
    }

    /**
     * Allow either the dependent function to be null (and behave more efficiently if it is).
     */
    @Test
    void shouldAllowNullDependentFunction() {
        final List<SimpleGraphNode> graph = Arrays.asList(new SimpleGraphNode(0), new SimpleGraphNode(1));
        assertEquals(Arrays.asList(0, 1),
            Streams.topologicalSort(graph.stream(), SimpleGraphNode::getDependencies, null, Streams.SortOrder.LAST)
                .map(SimpleGraphNode.class::cast)
                .map(SimpleGraphNode::getId)
                .collect(Collectors.toList()));
    }

    /**
     * Allow the dependency function to be null.
     */
    @Test
    void shouldAllowNullDependencyFunction() {
        final List<SimpleGraphNode> graph = Arrays.asList(new SimpleGraphNode(0), new SimpleGraphNode(1));
        assertEquals(Arrays.asList(0, 1),
            Streams.topologicalSort(graph.stream(), null, SimpleGraphNode::getDependents, Streams.SortOrder.FIRST)
                .map(SimpleGraphNode.class::cast)
                .map(SimpleGraphNode::getId)
                .collect(Collectors.toList()));
    }

    /**
     * Should detect a cycle in the graph.
     */
    @Test
    void shouldDetectCycle() {
        final List<SimpleGraphNode> graph =
            Arrays.asList(new SimpleGraphNode(0), new SimpleGraphNode(1), new SimpleGraphNode(2));
        graph.get(0).addDependency(graph.get(1));
        graph.get(1).addDependency(graph.get(2));
        graph.get(2).addDependency(graph.get(0));
        assertEquals(
            "Cycle detected : SimpleGraphNode{id=0, name='null', layer=0} : [SimpleGraphNode{id=0, name='null',"
                + " layer=0}, SimpleGraphNode{id=1, name='null', layer=0}, SimpleGraphNode{id=2, name='null', layer=0}]",
            Assertions.assertThrows(IllegalArgumentException.class, () -> Streams.topologicalSort(graph.stream(),
                SimpleGraphNode::getDependencies, SimpleGraphNode::getDependents,
                Streams.SortOrder.UNCHANGED)).getMessage());
    }

    /**
     * We report the correct ordered cycle, this allows better debugging.
     */
    @Test
    void shouldReportCorrectPath() {
        final SimpleGraphNode seed = new SimpleGraphNode(0);
        final List<SimpleGraphNode> graph = Stream.iterate(seed, g -> new SimpleGraphNode(g.id + 1))
            .limit(10)
            .toList();

        seed.addDependency(graph.get(1));
        seed.addDependency(graph.get(2));
        seed.addDependency(graph.get(3));

        graph.get(1).addDependency(graph.get(4));
        graph.get(2).addDependency(graph.get(5));
        graph.get(3).addDependency(graph.get(6));

        graph.get(6).addDependency(seed);  // create a cycle.

        assertEquals(
            "Cycle detected : SimpleGraphNode{id=0, name='null', layer=0} : [SimpleGraphNode{id=0, name='null',"
                + " layer=0}, SimpleGraphNode{id=3, name='null', layer=0}, SimpleGraphNode{id=6, name='null', layer=0}]",
            Assertions.assertThrows(IllegalArgumentException.class, () ->
                Streams.topologicalSort(graph.stream(), SimpleGraphNode::getDependencies, null,
                        Streams.SortOrder.UNCHANGED)
                    .map(SimpleGraphNode.class::cast)
                    .map(SimpleGraphNode::getId)
                    .collect(Collectors.toList())).getMessage());
    }

    /**
     * This is what we will often do, have a linear list, we should effectively reverse this list.
     * 9 -> 8 -> 7 -> 6 -> 5 -> 4 -> 3 -> 2 -> 1 -> 0
     */
    @Test
    void shouldCheckLinearGraph() {
        final List<SimpleGraphNode> graph = Stream.iterate(new SimpleGraphNode(9), i -> {
                final SimpleGraphNode node = new SimpleGraphNode((i.id) - 1);
                node.addDependency(i);
                return node;
            }).limit(10)
            .toList();

        assertEquals(Stream.iterate(0, i -> i + 1).limit(10).collect(Collectors.toList()),
            Streams.topologicalSort(
                    graph.stream(), SimpleGraphNode::getDependencies, SimpleGraphNode::getDependents,
                    Streams.SortOrder.LAST)
                .map(SimpleGraphNode.class::cast)
                .map(SimpleGraphNode::getId)
                .collect(Collectors.toList()));
    }

    @Test
    void shouldGetDressedProperlyIndependentNodesFirst() {
        // watch -> socks -> undershorts -> pants -> shirt -> tie -> belt -> jacket -> shoes
        getDressed(Streams.SortOrder.FIRST, Arrays.asList(0, 8, 7, 6, 2, 3, 4, 5, 1));
    }

    @Test
    void shouldGetDressedProperlyIndependentNodesLast() {
        // socks -> undershorts -> pants -> shirt -> tie -> belt -> jacket -> shoes -> watch
        getDressed(Streams.SortOrder.LAST, Arrays.asList(8, 7, 6, 2, 3, 4, 5, 1, 0));
    }

    @Test
    void shouldGetDressedProperlyIndependentNodesNone() {
        // socks -> undershorts -> pants -> shirt -> tie -> belt -> jacket -> shoes -> watch
        getDressed(Streams.SortOrder.UNCHANGED, Arrays.asList(8, 7, 6, 2, 3, 4, 5, 1, 0));
    }

    /**
     * This is the classic topological sort on page 613 of the MIT Algo book 3rd edition.
     * Put your clothes on in the correct order.
     *
     * @param sortOrder      the sort order for unconnected nodes
     * @param expectedResult the expected result
     */
    private void getDressed(final Streams.SortOrder sortOrder, final List<Integer> expectedResult) {

        final List<SimpleGraphNode> graph = Arrays.asList(
            new SimpleGraphNode(0, "watch"),
            new SimpleGraphNode(1, "shoes"),
            new SimpleGraphNode(2, "shirt"),
            new SimpleGraphNode(3, "tie"),
            new SimpleGraphNode(4, "belt"),
            new SimpleGraphNode(5, "jacket"),
            new SimpleGraphNode(6, "pants"),
            new SimpleGraphNode(7, "undershorts"),
            new SimpleGraphNode(8, "socks"));

        graph.get(1).addDependent(graph.get(8)); // socks -> shoes
        graph.get(1).addDependent(graph.get(7)); // undershorts -> shoes
        graph.get(7).addDependency(graph.get(6)); // undershorts -> pants
        graph.get(6).addDependency(graph.get(4)); // pants -> belt
        graph.get(1).addDependent(graph.get(6)); // pants -> shoes
        graph.get(2).addDependency(graph.get(4)); // shirt -> belt
        graph.get(2).addDependency(graph.get(3)); // shirt -> tie
        graph.get(3).addDependency(graph.get(5)); // tie -> jacket
        graph.get(4).addDependency(graph.get(5)); // belt -> jacket

        assertEquals(expectedResult, Streams.topologicalSort(
                graph.stream(), SimpleGraphNode::getDependencies, SimpleGraphNode::getDependents, sortOrder)
            .map(SimpleGraphNode.class::cast)
            .map(SimpleGraphNode::getId)
            .collect(Collectors.toList()));
    }

    @Test
    void shouldPutIndependentNodesFirst() {
        checkDependencies(Streams.SortOrder.FIRST, Arrays.asList(0, 3, 6, 5, 4, 1, 2));
    }

    @Test
    void shouldPutIndependentNodesLast() {
        checkDependencies(Streams.SortOrder.LAST, Arrays.asList(5, 4, 1, 2, 0, 3, 6));
    }

    @Test
    void shouldPutIndependentNodesNone() {
        checkDependencies(Streams.SortOrder.UNCHANGED, Arrays.asList(6, 5, 4, 3, 1, 2, 0));
    }

    /**
     * In case things with no dependencies should go at the end, this is different from
     * a classical topological sort where {@link Streams.SortOrder} doesn't matter.
     *
     * @param sortOrder      the sort order for unconnected nodes
     * @param expectedResult the expected result
     */
    private void checkDependencies(final Streams.SortOrder sortOrder, final List<Integer> expectedResult) {
        final List<SimpleGraphNode> graph = Arrays.asList(
            new SimpleGraphNode(0, "no-depends0"),
            new SimpleGraphNode(1, "depends1"),
            new SimpleGraphNode(2, "depends2"),
            new SimpleGraphNode(3, "no-depends3"),
            new SimpleGraphNode(4, "depends4"),
            new SimpleGraphNode(5, "depends5"),
            new SimpleGraphNode(6, "no-depends6"));

        graph.get(1).addDependency(graph.get(2));
        graph.get(4).addDependent(graph.get(5));

        // dependent nodes should go first, non-dependent nodes last
        assertEquals(expectedResult, Streams.topologicalSort(
                graph.stream(), SimpleGraphNode::getDependencies, SimpleGraphNode::getDependents, sortOrder)
            .map(SimpleGraphNode.class::cast)
            .map(SimpleGraphNode::getId)
            .collect(Collectors.toList()));
    }

    /**
     * Generate random graphs and check that nodes are placed before their dependencies and sort order is observed.
     */
    @RepeatedTest(10)
    void shouldHandleRandomGraph() {
        final List<SimpleGraphNode> graph = generateRandomDag(8, 12, 8, 12, 20, 50);

        // shuffle the graph
        java.util.Collections.shuffle(graph);

        // choose a random sort order
        final Streams.SortOrder order = Math.random() > 0.66
            ? Streams.SortOrder.FIRST
            : Math.random() > 0.5
                ? Streams.SortOrder.LAST
                : Streams.SortOrder.UNCHANGED;

        final boolean first = order.equals(Streams.SortOrder.FIRST);
        final boolean last = order.equals(Streams.SortOrder.LAST);
        boolean flipped = false;

        final List<SimpleGraphNode> result = Streams.topologicalSort(graph.stream(), SimpleGraphNode::getDependencies,
            SimpleGraphNode::getDependents, order).collect(Collectors.toList());

        for (int i = 0; i < result.size(); i++) {
            final SimpleGraphNode node = result.get(i);
            final int nodeOffset = i;

            // check the dependencies
            node.getDependencies().forEach(dep ->
                assertTrue(result.indexOf(dep) > nodeOffset, () -> "Dependency Issue : " + displayGraph(result)));

            // check the dependents
            node.getDependents().forEach(dep ->
                assertTrue(result.indexOf(dep) < nodeOffset, () -> "Dependent Issue : " + displayGraph(result)));

            if (flipped && ((first && !node.connected) || (last && node.connected))) {
                fail(() -> "Streams.SortOrder issue : " + displayGraph(result));
            }

            // update the flip flag
            flipped = first == node.connected;
        }
    }

    /**
     * Use <a href="https://dreampuf.github.io/GraphvizOnline">GraphViz</a> to visualize this representation of the
     * graph.
     *
     * @param result the topological sort graph
     * @return a {@link String} representation of the graph
     */
    private String displayGraph(final List<SimpleGraphNode> result) {
        final StringBuffer buffer = new StringBuffer("Graph, go to graphviz.org : \ndigraph {\n");
        for (int i = 0; i < result.size(); i++) {
            result.get(i).graphVizRender(buffer, i);
        }
        return buffer.toString();
    }

    /**
     * Generate a random dag.
     *
     * @param minPerRank     the minimum per rank (width)
     * @param maxPerRank     the maximum per rank (width)
     * @param minRanks       the minimum number of ranks (depth)
     * @param maxRanks       the maximum number of ranks (depth)
     * @param edgePercentage the percentage (between 0 and 100) of edge creation
     * @return a {@link List} of {@link SimpleGraphNode}
     */
    private List<SimpleGraphNode> generateRandomDag(final int minPerRank,
                                                    final int maxPerRank,
                                                    final int minRanks,
                                                    final int maxRanks,
                                                    final int edgePercentage,
                                                    final int directionPercentage) {
        final List<SimpleGraphNode> result = new ArrayList<>();
        int nodes = 0;

        for (int rank = 0; rank < (int) (minRanks + (Math.random() % (maxRanks - minRanks + 1))); rank++) {
            final int newNodes = (int) (minPerRank + (Math.random() % (maxPerRank - minPerRank + 1)));

            for (int k = 0; k < newNodes; k++) {
                final SimpleGraphNode node = new SimpleGraphNode(k + nodes, rank, null);
                result.add(node);
                for (int j = 0; j < nodes; j++) {
                    if ((Math.random() * 100) < edgePercentage) {
                        if ((Math.random() * 100) < directionPercentage) {
                            result.get(j).addDependency(node);
                        }
                        else {
                            node.addDependent(result.get(j));
                        }
                    }
                }
            }

            nodes += newNodes;
        }

        return result;
    }

    /**
     * A simple graph node implementation.
     */
    private static class SimpleGraphNode {

        /**
         * Id of the node.
         */
        private final int id;

        /**
         * Layer of the node (can be 0).
         */
        private final int layer;

        /**
         * Name of the node, can be null.
         */
        private final String name;

        /**
         * Dependencies of the node.
         */
        private final List<SimpleGraphNode> dependencies = new ArrayList<>();

        /**
         * Dependents of the node.
         */
        private final List<SimpleGraphNode> dependents = new ArrayList<>();

        /**
         * Is this node connected to the graph.
         */
        private boolean connected;

        private SimpleGraphNode(final int id) {
            this(id, null);
        }

        private SimpleGraphNode(final int id, final String name) {
            this.id = id;
            this.name = name;
            this.layer = 0;
        }

        private SimpleGraphNode(final int id, final int layer, final String name) {
            this.id = id;
            this.layer = layer;
            this.name = name;
        }

        private int getId() {
            return this.id;
        }

        private void addDependency(final SimpleGraphNode edge) {
            this.dependencies.add(edge);
            this.connected = true;
            edge.connected = true;
        }

        private Stream<SimpleGraphNode> getDependencies() {
            return this.dependencies.stream();
        }

        private void addDependent(final SimpleGraphNode edge) {
            edge.connected = true;
            this.connected = true;
            this.dependents.add(edge);
        }

        private Stream<SimpleGraphNode> getDependents() {
            return this.dependents.stream();
        }

        /**
         * Render the sorted graph for viewing at http://graphviz.org
         *
         * @param buffer the {@link StringBuffer}
         * @param order  the topological sorted order of this node
         * @return a {@link String} representation of the graph
         */
        private String graphVizRender(final StringBuffer buffer, final int order) {
            buffer.append("    " + this.id).append(" [label=\"")
                .append("id:").append(this.id)
                .append(" layer:").append(this.layer)
                .append(" order:").append(order)
                .append("\"]\n");
            this.dependencies.forEach(d -> buffer.append(this.id).append("->").append(d.id).append("\n"));
            this.dependents.forEach(d -> buffer.append(d.id).append("->").append(this.id).append("\n"));
            return buffer.toString();
        }

        @Override
        public String toString() {
            return "SimpleGraphNode{id=" + this.id + ", name='" + this.name + "', layer=" + this.layer + "}";
        }
    }
}
