package build.base.mereology;

import build.base.assertion.IteratorAssert;
import build.base.foundation.stream.Streams;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Composite}s.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
class CompositeTests {

    /**
     * Ensure an empty {@link Composite} can be created.
     */
    @Test
    void shouldCreateEmptyComposite() {
        final var composite = Composite.empty();

        assertThat(composite.parts())
            .isEmpty();

        assertThat(composite.composition())
            .isEmpty();

        assertThat(composite.traverse())
            .isEmpty();

        assertThat(composite.traverse(String.class))
            .isEmpty();

        assertThat(composite.parts(String.class))
            .isEmpty();

        assertThat(composite.composition(String.class))
            .isEmpty();

        assertThat(composite
            .traverse()
            .hierarchical())
            .isEmpty();

        assertThat(composite.traverse(String.class)
            .hierarchical())
            .isEmpty();
    }

    /**
     * Ensure a {@link Composite} with a single part can be created.
     */
    @Test
    void shouldCreateSinglePartComposite() {
        final var message = "Hello World";
        final var composite = Composites.of(message);

        assertThat(composite.parts())
            .containsExactly(message);

        assertThat(composite.parts(String.class))
            .containsExactly(message);

        assertThat(composite.parts(Integer.class))
            .isEmpty();

        assertThat(composite.composition())
            .containsExactly(message);

        assertThat(composite.composition(String.class))
            .containsExactly(message);

        assertThat(composite.composition(Integer.class))
            .isEmpty();

        assertThat(composite.traverse())
            .containsExactly(message);

        assertThat(composite
            .traverse(String.class))
            .containsExactly(message);

        assertThat(composite.traverse()
            .reflexive(true))
            .containsExactly(composite, message);

        assertThat(composite.traverse()
            .reflexive(true)
            .strategy(Strategy.DepthFirst))
            .containsExactly(composite, message);

        assertThat(composite.traverse()
            .filter(String.class::isInstance))
            .containsExactly(message);

        composite.traverse()
            .hierarchical()
            .stream()
            .forEach(parthood -> {
                assertThat(parthood.object())
                    .isSameAs(message);

                assertThat(parthood.composite()
                    .orElseThrow())
                    .isSameAs(composite);
            });
    }

    /**
     * Ensure a {@link Composite} with multiple parts of the same type can be created.
     */
    @Test
    void shouldCreateMonogeneousPartComposite() {
        final var composite = Composites.of("Hello", "World");

        assertThat(composite.parts())
            .containsExactly("Hello", "World");

        assertThat(composite.parts(String.class))
            .containsExactly("Hello", "World");

        assertThat(composite.parts(Integer.class))
            .isEmpty();

        assertThat(composite.composition())
            .containsExactly("Hello", "World");

        assertThat(composite.composition(String.class))
            .containsExactly("Hello", "World");

        assertThat(composite.composition(Integer.class))
            .isEmpty();

        assertThat(composite.traverse())
            .containsExactly("Hello", "World");

        assertThat(composite.traverse(String.class))
            .containsExactly("Hello", "World");

        assertThat(composite.traverse(String.class)
            .filter(string -> string.contains("e")))
            .containsExactly("Hello");

        assertThat(composite.traverse(String.class)
            .filter(string -> string.contains("d")))
            .containsExactly("World");

        assertThat(composite.traverse()
            .reflexive(true))
            .containsExactly(composite, "Hello", "World");

        assertThat(composite.traverse(String.class)
            .reflexive(true))
            .containsExactly("Hello", "World");

        assertThat(composite.traverse(String.class)
            .filter(string -> string.contains("e"))
            .hierarchical()
            .stream()
            .findFirst()
            .orElseThrow())
            .matches(parthood -> parthood.object().equals("Hello")
                && parthood.composite()
                .orElseThrow()
                .equals(composite));

        assertThat(composite.traverse(String.class)
            .filter(string -> string.contains("W"))
            .hierarchical()
            .stream()
            .findFirst()
            .orElseThrow())
            .matches(parthood -> parthood.object().equals("World")
                && parthood.composite()
                .orElseThrow()
                .equals(composite));

        IteratorAssert.assertThat(
                composite.traverse(String.class)
                    .strategy(Strategy.Direct)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals("Hello"))
            .then().matches(parthood -> parthood.object().equals("World"))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse(String.class)
                    .strategy(Strategy.DepthFirst)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals("Hello"))
            .then().matches(parthood -> parthood.object().equals("World"))
            .isTrue();
    }

    /**
     * Ensure a {@link Composite} with multiple parts of different types can be created.
     */
    @Test
    void shouldCreateHeterogeneousPartComposite() {
        final var composite = Composites.of("Hello", 42);

        assertThat(composite.parts())
            .containsExactly("Hello", 42);

        assertThat(composite.parts(String.class))
            .containsExactly("Hello");

        assertThat(composite.parts(Integer.class))
            .containsExactly(42);

        assertThat(composite.composition())
            .containsExactly("Hello", 42);

        assertThat(composite.composition(String.class))
            .containsExactly("Hello");

        assertThat(composite.composition(Integer.class))
            .containsExactly(42);

        assertThat(composite.traverse())
            .containsExactly("Hello", 42);

        assertThat(composite.traverse(String.class))
            .containsExactly("Hello");

        assertThat(composite.traverse(Integer.class))
            .containsExactly(42);

        assertThat(composite.traverse()
            .reflexive(true))
            .containsExactly(composite, "Hello", 42);

        assertThat(composite.traverse(String.class)
            .reflexive(true))
            .containsExactly("Hello");

        assertThat(composite.traverse(Integer.class)
            .reflexive(true))
            .containsExactly(42);

        assertThat(composite.traverse()
            .filter(String.class::isInstance))
            .containsExactly("Hello");

        assertThat(composite.traverse()
            .filter(Integer.class::isInstance))
            .containsExactly(42);

        assertThat(composite.traverse()
            .filter(Object.class::isInstance))
            .containsExactly("Hello", 42);

        assertThat(composite.traverse()
            .reflexive(true)
            .filter(Composite.class::isInstance))
            .containsExactly(composite);

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.Direct)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals("Hello"))
            .then().matches(parthood -> parthood.object().equals(42))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.DepthFirst)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals("Hello"))
            .then().matches(parthood -> parthood.object().equals(42))
            .isTrue();
    }


    /**
     * Ensure a {@link Composite} with nested parts can be created.
     */
    @Test
    void shouldCreateNestedComposite() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Awesome!";

        final var composite = Composites.of(part1, part2, part3);

        assertThat(composite.parts())
            .containsExactly(part1, part2, part3);

        assertThat(composite.parts(String.class))
            .containsExactly(part3);

        assertThat(composite.parts(Composite.class))
            .containsExactly(part1, part2);

        assertThat(composite.composition())
            .containsExactly(part1, "Hello", part2, "World", part3);

        assertThat(composite.composition(String.class))
            .containsExactly("Hello", "World", part3);

        assertThat(composite.composition(Composite.class))
            .containsExactly(part1, part2);

        assertThat(composite.traverse()
            .reflexive(true)
            .strategy(Strategy.DepthFirst))
            .containsExactly(composite, part1, "Hello", part2, "World", part3);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst))
            .containsExactly(part1, "Hello", part2, "World", part3);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .exclude(c -> c == part2))
            .containsExactly(part1, "Hello", part3);


        assertThat(composite.traverse()
            .filter(String.class::isInstance)
            .strategy(Strategy.DepthFirst))
            .containsExactly("Hello", "World", part3);

        assertThat(composite.traverse()
            .filter(String.class)
            .strategy(Strategy.DepthFirst))
            .containsExactly("Hello", "World", part3);

        assertThat(composite.traverse()
            .reflexive(true)
            .strategy(Strategy.BreadthFirst))
            .containsExactly(composite, part1, part2, part3, "Hello", "World");

        assertThat(composite.traverse()
            .strategy(Strategy.BreadthFirst))
            .containsExactly(part1, part2, part3, "Hello", "World");

        assertThat(composite.traverse()
            .strategy(Strategy.BreadthFirst)
            .exclude(c -> c == part2))
            .containsExactly(part1, part3, "Hello");

        assertThat(composite.traverse()
            .filter(String.class::isInstance)
            .strategy(Strategy.BreadthFirst))
            .containsExactly(part3, "Hello", "World");

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.Direct)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals(part1))
            .then().matches(parthood -> parthood.object().equals(part2))
            .then().matches(parthood -> parthood.object().equals(part3))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.Direct)
                    .exclude(c -> c == part2)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals(part1))
            .then().matches(parthood -> parthood.object().equals(part3))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.DepthFirst)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals(part1))
            .then().matches(parthood -> parthood.object().equals("Hello"))
            .then().matches(parthood -> parthood.object().equals(part2))
            .then().matches(parthood -> parthood.object().equals("World"))
            .then().matches(parthood -> parthood.object().equals(part3))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.DepthFirst)
                    .exclude(c -> c == part2)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals(part1))
            .then().matches(parthood -> parthood.object().equals("Hello"))
            .then().matches(parthood -> parthood.object().equals(part3))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.BreadthFirst)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals(part1)
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite)))
            .then().matches(parthood -> parthood.object().equals(part2)
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite)))
            .then().matches(parthood -> parthood.object().equals(part3)
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite)))
            .then().matches(parthood -> parthood.object().equals("Hello")
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite, part1)))
            .then().matches(parthood -> parthood.object().equals("World")
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite, part2)))
            .isTrue();

        IteratorAssert.assertThat(
                composite.traverse()
                    .strategy(Strategy.BreadthFirst)
                    .exclude(c -> c == part2)
                    .hierarchical())
            .starts()
            .then().matches(parthood -> parthood.object().equals(part1)
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite)))
            .then().matches(parthood -> parthood.object().equals(part3)
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite)))
            .then().matches(parthood -> parthood.object().equals("Hello")
                && Streams.equals(parthood.hierarchy().stream(), Stream.of(composite, part1)))
            .isTrue();
    }

    /**
     * Ensure a {@link Composite} traversal can be aborted when a predicate is satisfied.
     */
    @Test
    void shouldAbortTraversalWhenPredicateMatches() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Stop";
        final var part4 = "Continue";

        final var composite = Composites.of(part1, part2, part3, part4);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .abort(element -> element.equals("Stop")))
            .containsExactly(part1, "Hello", part2, "World");

        assertThat(composite.traverse()
            .strategy(Strategy.BreadthFirst)
            .abort(element -> element.equals("Stop")))
            .containsExactly(part1, part2);
    }

    /**
     * Ensure a {@link Composite} traversal returns all elements when abort predicate never matches.
     */
    @Test
    void shouldNotAbortTraversalWhenPredicateNeverMatches() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Awesome!";

        final var composite = Composites.of(part1, part2, part3);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .abort(element -> element.equals("NonExistent")))
            .containsExactly(part1, "Hello", part2, "World", part3);
    }

    /**
     * Ensure a {@link Composite} traversal can be aborted immediately on first element.
     */
    @Test
    void shouldAbortTraversalImmediatelyOnFirstElement() {
        final var part1 = Composites.of("Stop");
        final var part2 = Composites.of("World");

        final var composite = Composites.of(part1, part2);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .abort(element -> element instanceof Composite))
            .isEmpty();
    }

    /**
     * Ensure a {@link Composite} traversal with filtering and abort works correctly.
     */
    @Test
    void shouldAbortFilteredTraversal() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Stop";
        final var part4 = 42;

        final var composite = Composites.of(part1, part2, part3, part4);

        assertThat(composite.traverse(String.class)
            .strategy(Strategy.DepthFirst)
            .abort(s -> s.equals("Stop")))
            .containsExactly("Hello", "World");
    }

    /**
     * Ensure a {@link Composite} traversal with null abort predicate works normally.
     */
    @Test
    void shouldHandleNullAbortPredicate() {
        final var part1 = Composites.of("Hello");
        final var part2 = "World";

        final var composite = Composites.of(part1, part2);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .abort(null))
            .containsExactly(part1, "Hello", part2);
    }

    /**
     * Ensure a {@link Composite} hierarchical traversal can be aborted when a predicate is satisfied.
     */
    @Test
    void shouldAbortHierarchicalTraversalWhenPredicateMatches() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Stop";
        final var part4 = "Continue";

        final var composite = Composites.of(part1, part2, part3, part4);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .abort(parthood -> parthood.object().equals("Stop"))
            .stream()
            .map(Entity::object))
            .containsExactly(part1, "Hello", part2, "World");

        assertThat(composite.traverse()
            .strategy(Strategy.BreadthFirst)
            .hierarchical()
            .abort(parthood -> parthood.object().equals("Stop"))
            .stream()
            .map(Entity::object))
            .containsExactly(part1, part2);
    }

    /**
     * Ensure a {@link Composite} hierarchical traversal can be aborted based on composite hierarchy.
     */
    @Test
    void shouldAbortHierarchicalTraversalBasedOnComposite() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Continue";

        final var composite = Composites.of(part1, part2, part3);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .abort(parthood -> parthood.object().equals("World"))
            .stream()
            .map(Entity::object)
            .peek(System.out::println))
            .containsExactly(part1, "Hello", part2);
    }

    /**
     * Ensure a {@link Composite} hierarchical traversal returns all elements when abort predicate never matches.
     */
    @Test
    void shouldNotAbortHierarchicalTraversalWhenPredicateNeverMatches() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Awesome!";

        final var composite = Composites.of(part1, part2, part3);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .abort(parthood -> parthood.object().equals("NonExistent"))
            .stream()
            .map(Entity::object))
            .containsExactly(part1, "Hello", part2, "World", part3);
    }

    /**
     * Ensure a {@link Composite} hierarchical traversal can be aborted immediately on first element.
     */
    @Test
    void shouldAbortHierarchicalTraversalImmediatelyOnFirstElement() {
        final var part1 = Composites.of("Stop");
        final var part2 = Composites.of("World");

        final var composite = Composites.of(part1, part2);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .abort(parthood -> parthood.object() instanceof Composite)
            .stream()
            .map(Entity::object))
            .isEmpty();
    }

    /**
     * Ensure a {@link Composite} hierarchical traversal with null abort predicate works normally.
     */
    @Test
    void shouldHandleNullAbortPredicateInHierarchicalTraversal() {
        final var part1 = Composites.of("Hello");
        final var part2 = "World";

        final var composite = Composites.of(part1, part2);

        assertThat(composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .abort(null)
            .stream()
            .map(Entity::object))
            .containsExactly(part1, "Hello", part2);
    }

    /**
     * Ensure hierarchical abort can be chained and replaced.
     */
    @Test
    void shouldChainHierarchicalAbortPredicates() {
        final var part1 = Composites.of("Hello");
        final var part2 = Composites.of("World");
        final var part3 = "Stop";

        final var composite = Composites.of(part1, part2, part3);

        final var hierarchical = composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .abort(parthood -> parthood.object().equals("Never"))
            .abort(parthood -> parthood.object().equals("Stop"));

        assertThat(hierarchical.stream()
            .map(Entity::object))
            .containsExactly(part1, "Hello", part2, "World");
    }

    /**
     * Ensure the composite relationships in {@link Entity}s returned by hierarchical traversal are correct.
     */
    @Test
    void shouldReturnCorrectCompositeInParthoodHierarchy() {
        final var innerPart1 = "Hello";
        final var innerPart2 = "World";
        final var part1 = Composites.of(innerPart1);
        final var part2 = Composites.of(innerPart2);
        final var part3 = "Direct";

        final var composite = Composites.of(part1, part2, part3);

        final var parthoods = composite.traverse()
            .strategy(Strategy.DepthFirst)
            .hierarchical()
            .stream()
            .toList();

        // Verify each parthood has the correct composite relationship
        assertThat(parthoods)
            .hasSize(5)
            .satisfies(list -> {
                // part1 should be directly in composite
                assertThat(list.get(0).object()).isSameAs(part1);
                assertThat(list.get(0).composite()).contains(composite);

                // "Hello" should be directly in part1
                assertThat(list.get(1).object()).isEqualTo("Hello");
                assertThat(list.get(1).composite()).contains(part1);

                // part2 should be directly in composite
                assertThat(list.get(2).object()).isSameAs(part2);
                assertThat(list.get(2).composite()).contains(composite);

                // "World" should be directly in part2
                assertThat(list.get(3).object()).isEqualTo("World");
                assertThat(list.get(3).composite()).contains(part2);

                // "Direct" should be directly in composite
                assertThat(list.get(4).object()).isEqualTo("Direct");
                assertThat(list.get(4).composite()).contains(composite);
            });
    }

    /**
     * Ensure the hierarchy in {@link Entity}s shows the complete path from root to element.
     */
    @Test
    void shouldReturnCorrectHierarchyInParthood() {
        final var deepest = "Deep";
        final var level2 = Composites.of(deepest);
        final var level1 = Composites.of(level2);
        final var root = Composites.of(level1, "Sibling");

        final var parthoods = root.traverse()
            .strategy(Strategy.DepthFirst)
            .reflexive(true)
            .hierarchical()
            .stream()
            .toList();

        // Find the deepest element and verify its hierarchy
        final var deepestParthood = parthoods.stream()
            .filter(p -> p.object().equals("Deep"))
            .findFirst()
            .orElseThrow();

        assertThat(deepestParthood.hierarchy().stream())
            .containsExactly(root, level1, level2);

        assertThat(deepestParthood.distance())
            .isEqualTo(3);

        // Verify level2 composite has correct hierarchy
        final var level2Parthood = parthoods.stream()
            .filter(p -> p.object() == level2)
            .findFirst()
            .orElseThrow();

        assertThat(level2Parthood.hierarchy().stream())
            .containsExactly(root, level1);

        assertThat(level2Parthood.distance())
            .isEqualTo(2);
    }
}
