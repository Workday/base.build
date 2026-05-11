package build.base.mereology;

import build.base.assertion.IteratorAssert;
import build.base.foundation.stream.Streams;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
     * Ensure {@link Composite#composition()} does not hang or recurse infinitely when the composite graph contains a self-loop.
     */
    @Test
    void shouldHandleSelfLoopCycleInComposition() {
        final var parts = new ArrayList<>();
        final var composite = Composites.of(parts);
        parts.add("Hello");
        parts.add(composite); // cycle: composite contains itself

        // composite is emitted as an element (it is a part of itself) but not recursed into again
        assertThat(composite.composition()).containsExactly(composite, "Hello");
    }

    /**
     * Ensure {@link Composite#composition()} does not hang or recurse infinitely when the composite graph contains a mutual cycle.
     */
    @Test
    void shouldHandleMutualCycleInComposition() {
        final var partsA = new ArrayList<>();
        final var partsB = new ArrayList<>();
        final var a = Composites.of(partsA);
        final var b = Composites.of(partsB);
        partsA.add("Hello");
        partsA.add(b);   // A → B
        partsB.add("World");
        partsB.add(a);   // B → A (cycle)

        // a is emitted as an element of b (cycle back-edge), but not recursed into again
        assertThat(a.composition()).containsExactly(b, a, "World", "Hello");
    }

    /**
     * Ensure {@link Entity#distance()} returns 1 for an {@link Entity} created with a single {@link Composite},
     * and that repeated calls to {@link Entity#hierarchy()} return the same content.
     */
    @Test
    void shouldReturnDistanceOfOneForSingleCompositeEntity() {
        final var composite = Composites.of("part");
        final var entity = Entity.of("object", composite);

        assertThat(entity.distance()).isEqualTo(1);
        assertThat(entity.composite()).contains(composite);
        assertThat(entity.hierarchy().stream()).containsExactly(composite);

        // repeated calls must return the same results
        assertThat(entity.distance()).isEqualTo(1);
        assertThat(entity.hierarchy().stream()).containsExactly(composite);
    }

    /**
     * Ensure {@link Entity#distance()} equals the hierarchy depth for an {@link Entity} created from a
     * {@link Stream}, and that {@link Entity#composite()} and {@link Entity#hierarchy()} return consistent
     * results across repeated calls (no stream-reuse hazard).
     */
    @Test
    void shouldReturnCorrectDistanceAndHierarchyFromStreamFactory() {
        final var root = Composites.of("a");
        final var mid = Composites.of("b");
        final var leaf = Composites.of("c");

        // stream is innermost-first: leaf → mid → root
        final var entity = Entity.of("object", Stream.of(leaf, mid, root));

        assertThat(entity.distance()).isEqualTo(3);
        assertThat(entity.composite()).contains(leaf);

        // hierarchy() must be boundary-first: root → mid → leaf
        assertThat(entity.hierarchy().stream()).containsExactly(root, mid, leaf);

        // repeated calls must return the same results (guards against stream-reuse regression)
        assertThat(entity.distance()).isEqualTo(3);
        assertThat(entity.composite()).contains(leaf);
        assertThat(entity.hierarchy().stream()).containsExactly(root, mid, leaf);
    }

    /**
     * Ensure a {@link Strategy#Direct} {@link Hierarchical} traversal produces the same elements on repeated
     * calls to {@link Hierarchical#stream()} — guards against the iterator being created eagerly and exhausted
     * on first use.
     */
    @Test
    void shouldReturnConsistentResultsOnRepeatedDirectHierarchicalStream() {
        final var composite = Composites.of("Hello", "World");
        final var hierarchical = composite.traverse()
            .strategy(Strategy.Direct)
            .hierarchical();

        assertThat(hierarchical.stream().map(Entity::object)).containsExactly("Hello", "World");
        assertThat(hierarchical.stream().map(Entity::object)).containsExactly("Hello", "World");
    }

    /**
     * Ensure a reflexive {@link Strategy#Direct} {@link Hierarchical} traversal also produces consistent results
     * on repeated calls.
     */
    @Test
    void shouldReturnConsistentResultsOnRepeatedReflexiveDirectHierarchicalStream() {
        final var composite = Composites.of("Hello");
        final var hierarchical = composite.traverse()
            .strategy(Strategy.Direct)
            .reflexive(true)
            .hierarchical();

        assertThat(hierarchical.stream().map(Entity::object)).containsExactly(composite, "Hello");
        assertThat(hierarchical.stream().map(Entity::object)).containsExactly(composite, "Hello");
    }

    /**
     * Ensure {@link Traversal#filter(Class)} throws when a {@link Predicate} filter has already been set,
     * preventing the silent drop of the predicate.
     */
    @Test
    void shouldThrowWhenFilterByClassCalledAfterFilterByPredicate() {
        final var composite = Composites.of("Hello", 42);

        assertThatThrownBy(() -> composite.traverse()
            .filter(e -> e instanceof String)
            .filter(String.class))
            .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Ensure {@link Traversal#filter(Class)} throws when an abort predicate has already been set.
     */
    @Test
    void shouldThrowWhenFilterByClassCalledAfterAbort() {
        final var composite = Composites.of("Hello", 42);

        assertThatThrownBy(() -> composite.traverse()
            .abort(e -> false)
            .filter(String.class))
            .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Ensure {@link Entity#isAtom()} is structural: a non-{@link Composite} is always an atom,
     * a non-empty {@link Composite} is not, and an empty {@link Composite} is.
     */
    @Test
    void shouldDetermineAtomStructurally() {
        assertThat(Entity.boundary("hello").isAtom()).isTrue();
        assertThat(Entity.boundary(Composites.of("part")).isAtom()).isFalse();
        assertThat(Entity.boundary(Composite.empty()).isAtom()).isTrue();
    }

    /**
     * Ensure a {@link Composite} reachable via two paths in a diamond DAG is visited only once.
     */
    @Test
    void shouldVisitSharedCompositeOnceInDiamondDag() {
        final var shared = Composites.of("deep");
        final var left = Composites.of(shared);
        final var right = Composites.of(shared);
        final var root = Composites.of(left, right);

        assertThat(root.composition(Composite.class))
            .containsExactlyInAnyOrder(left, right, shared, shared);

        assertThat(root.composition(String.class))
            .containsExactly("deep");
    }

    /**
     * Ensure {@link Composites#verify} returns an empty list when every reachable {@link Composite} has parts.
     */
    @Test
    void shouldFindNoSuspectCompositesWhenAllHaveParts() {
        final var leaf = Composites.of("Hello");
        final var root = Composites.of(leaf, "World");

        assertThat(Composites.verify(root, c -> true)).isEmpty();
    }

    /**
     * Ensure {@link Composites#verify} flags a reachable {@link Composite} that exposes no parts.
     */
    @Test
    void shouldFindEmptyNestedCompositeAsSuspect() {
        final var empty = Composite.empty();
        final var root = Composites.of(empty, "Hello");

        assertThat(Composites.verify(root, c -> true))
            .containsExactly(empty);
    }

    /**
     * Ensure {@link Composites#verify} respects the predicate: composites rejected by it are not reported.
     */
    @Test
    void shouldRespectSuspectPredicateInVerify() {
        final var empty = Composite.empty();
        final var root = Composites.of(empty, "Hello");

        assertThat(Composites.verify(root, c -> c != empty)).isEmpty();
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
