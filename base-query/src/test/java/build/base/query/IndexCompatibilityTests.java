package build.base.query;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/**
 * Compatibility tests for {@link Index} implementations.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
public interface IndexCompatibilityTests {

    /**
     * Creates an {@link Index} for testing.
     *
     * @return a new {@link Index}
     */
    Index createIndex();

    /**
     * Ensure an empty {@link Index} can be created and queried.
     */
    @Test
    default void shouldCreateAndQueryAnEmptyIndex() {
        final var index = createIndex();

        assertThat(index.match(Color.class)
            .findFirst())
            .isEmpty();

        assertThat(index.get(Color.class, Color.NAME, "RED"))
            .isEmpty();
    }


    /**
     * Ensure an {@link Indexable} {@link Class} can be indexed and queried.
     */
    @Test
    default void shouldIndexAndQueryIndexableClass() {
        final var index = createIndex();

        index.index(Color.RED);
        index.index(Color.GREEN);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("RED")
            .findFirst())
            .contains(Color.RED);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("GREEN")
            .findFirst())
            .contains(Color.GREEN);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("BLUE")
            .findFirst())
            .isEmpty();
    }


    /**
     * Ensure an {@link Indexable} {@link Function} {@link Field} can be indexed and queried.
     */
    @Test
    default void shouldIndexAndQueryClassWithIndexableField() {
        final var index = createIndex();

        index.index(new Colorful(Color.RED));
        index.index(new Colorful(Color.GREEN));

        assertThat(index.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .contains(new Colorful(Color.RED));

        assertThat(index.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.GREEN)
            .findFirst())
            .contains(new Colorful(Color.GREEN));

        assertThat(index.match(Colorful.class)
            .where(Colorful.COLOR)
            .isEqualTo(Color.BLUE)
            .findFirst())
            .isEmpty();
    }


    /**
     * Ensure an {@link Indexable} {@link Class} can be indexed and queried and then not after being unindexed.
     */
    @Test
    default void shouldIndexAndQueryIndexableClassAndThenNotAfterUnindexing() {
        final var index = createIndex();

        index.index(Color.RED);
        index.index(Color.GREEN);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("RED")
            .findFirst())
            .contains(Color.RED);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("GREEN")
            .findFirst())
            .contains(Color.GREEN);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("BLUE")
            .findFirst())
            .isEmpty();

        index.unindex(Color.RED);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("RED")
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure a specifically added value {@link Object} can be indexed and unindexed.
     */
    @Test
    default void shouldIndexAndUnindexSpecificObjects() {
        final var index = createIndex();

        index.add(Color.class, Color.RED);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("RED")
            .findFirst())
            .contains(Color.RED);

        index.remove(Color.class, Color.RED);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isEqualTo("RED")
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure an object can be unindexed even when its state has changed since being indexed.
     */
    @Test
    default void shouldUnindexObjectAfterStateChange() {
        final var index = createIndex();

        final var colorful = new MutableColorful(Color.RED);
        index.index(colorful);

        assertThat(index.match(MutableColorful.class)
            .where(MutableColorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .contains(colorful);

        // change the state of the object after indexing
        colorful.setColor(Color.GREEN);

        // unindex the object — must succeed without invoking the @Indexable function
        index.unindex(colorful);

        assertThat(index.match(MutableColorful.class)
            .where(MutableColorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .isEmpty();

        assertThat(index.match(MutableColorful.class)
            .where(MutableColorful.COLOR)
            .isEqualTo(Color.GREEN)
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure an object whose {@link Indexable} function returns {@code null} can be indexed and unindexed.
     */
    @Test
    default void shouldIndexAndUnindexObjectWithNullIndexableValue() {
        final var index = createIndex();

        final var colorful = new NullColorful();
        index.index(colorful);

        assertThat(index.match(NullColorful.class)
            .where(NullColorful.COLOR)
            .isEqualTo(null)
            .findFirst())
            .contains(colorful);

        index.unindex(colorful);

        assertThat(index.match(NullColorful.class)
            .where(NullColorful.COLOR)
            .isEqualTo(null)
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure {@code isEqualTo(null)} works on the fallback (non-{@link Indexable}) path.
     *
     * <p>{@link NullFallbackColorful#COLOR} is not {@link Indexable}, so the query bypasses the index
     * and evaluates the predicate against the raw stream. Before the fix, {@code Objects.equals(null, NULL_OBJECT)}
     * returned {@code false}, so the object was never returned.
     */
    @Test
    default void shouldFindObjectWithNullValueOnFallbackPath() {
        final var index = createIndex();

        final var colorful = new NullFallbackColorful();
        index.add(NullFallbackColorful.class, colorful);

        assertThat(index.match(NullFallbackColorful.class)
            .where(NullFallbackColorful.COLOR)
            .isEqualTo(null)
            .findFirst())
            .contains(colorful);
    }

    /**
     * Ensure a {@link Unique} {@link Indexable} field is indexed and queried correctly.
     */
    @Test
    default void shouldIndexAndQueryUniqueIndexableField() {
        final var index = createIndex();

        index.index(new UniqueColorful(Color.RED));
        index.index(new UniqueColorful(Color.GREEN));

        assertThat(index.match(UniqueColorful.class)
            .where(UniqueColorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .contains(new UniqueColorful(Color.RED));

        assertThat(index.match(UniqueColorful.class)
            .where(UniqueColorful.COLOR)
            .isEqualTo(Color.BLUE)
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure a {@link Unique} {@link Indexable} field can be unindexed.
     */
    @Test
    default void shouldUnindexUniqueIndexableField() {
        final var index = createIndex();

        index.index(new UniqueColorful(Color.RED));
        index.unindex(new UniqueColorful(Color.RED));

        assertThat(index.match(UniqueColorful.class)
            .where(UniqueColorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst())
            .isEmpty();
    }

    /**
     * Ensure indexing two objects with the same key on a {@link Unique} field throws {@link IllegalStateException}.
     */
    @Test
    default void shouldRejectDuplicateKeyOnUniqueIndexableField() {
        final var index = createIndex();

        index.index(new UniqueColorful(Color.RED));

        assertThrowsExactly(IllegalStateException.class,
            () -> index.index(new UniqueColorful(Color.RED)));
    }

    /**
     * Ensure {@link Index#add} rejects a {@code null} class or value.
     */
    @Test
    default void shouldRejectNullArgumentsInAdd() {
        final var index = createIndex();

        assertThrowsExactly(NullPointerException.class, () -> index.add(null, Color.RED));
        assertThrowsExactly(NullPointerException.class, () -> index.add(Color.class, null));
    }

    /**
     * Ensure {@link Index#remove} rejects a {@code null} class or value.
     */
    @Test
    default void shouldRejectNullArgumentsInRemove() {
        final var index = createIndex();

        assertThrowsExactly(NullPointerException.class, () -> index.remove(null, Color.RED));
        assertThrowsExactly(NullPointerException.class, () -> index.remove(Color.class, null));
    }

    /**
     * Ensure {@link Index#reindex} reflects value changes after mutation.
     */
    @Test
    default void shouldReflectMutatedValueAfterReindex() {
        final var index = createIndex();

        final var colorful = new MutableColorful(Color.RED);
        index.index(colorful);

        assertThat(index.match(MutableColorful.class).where(MutableColorful.COLOR).isEqualTo(Color.RED).findFirst())
            .contains(colorful);

        colorful.setColor(Color.GREEN);
        index.reindex(colorful);

        assertThat(index.match(MutableColorful.class).where(MutableColorful.COLOR).isEqualTo(Color.RED).findFirst())
            .isEmpty();
        assertThat(index.match(MutableColorful.class).where(MutableColorful.COLOR).isEqualTo(Color.GREEN).findFirst())
            .contains(colorful);
    }

    /**
     * Ensure {@link Match#findAll()} returns instances indexed under subclasses when the query class is a supertype.
     */
    @Test
    default void shouldFindSubclassInstancesWhenQueryingBySupertype() {
        final var index = createIndex();
        final var circle = new Circle("red");
        final var square = new Square("blue");

        index.add(Circle.class, circle);
        index.add(Square.class, square);

        assertThat(index.match(Shape.class)
            .findAll()
            .toList()
        ).containsExactlyInAnyOrder(circle, square);
    }

    /**
     * Ensure {@link Scope#Indexed} also returns subclass instances when querying by supertype.
     */
    @Test
    default void shouldFindSubclassInstancesWithIndexedScopeWhenQueryingBySupertype() {
        final var index = createIndex();
        final var circle = new Circle("red");

        index.add(Circle.class, circle);

        assertThat(index.match(Shape.class)
            .scope(Scope.Indexed)
            .findAll()
            .toList()
        ).containsExactly(circle);
    }

    /**
     * Ensure a non-{@link Indexable} {@code where} predicate falls through to {@code indexedStream()} correctly
     * when querying by supertype with {@link Scope#Indexed}.
     */
    @Test
    default void shouldFilterBySupertypeViaFallbackPredicateWithIndexedScope() {
        final var index = createIndex();
        final var redCircle = new Circle("red");
        final var blueSquare = new Square("blue");

        index.add(Circle.class, redCircle);
        index.add(Square.class, blueSquare);

        assertThat(index.match(Shape.class)
            .scope(Scope.Indexed)
            .where(Shape::color)
            .isEqualTo("red")
            .findAll()
            .toList()
        ).containsExactly(redCircle);
    }

    /**
     * Ensure {@link Condition#isEqualTo} uses the indexed path when the {@link Indexable} function is declared on
     * a supertype and the query class is that supertype.
     */
    @Test
    default void shouldFindBySupertypeUsingIndexedFunctionEquality() {
        final var index = createIndex();
        final var red = new Circle("red");
        final var blue = new Square("blue");

        index.index(red);
        index.index(blue);

        assertThat(index.match(Shape.class).scope(Scope.Indexed).where(Shape.COLOR).isEqualTo("red").findFirst()).contains(red);
        assertThat(index.match(Shape.class).scope(Scope.Indexed).where(Shape.COLOR).isEqualTo("blue").findFirst()).contains(blue);
        assertThat(index.match(Shape.class).scope(Scope.Indexed).where(Shape.COLOR).isEqualTo("green").findFirst()).isEmpty();
    }

    /**
     * Ensure {@link Condition#isNotEqualTo} uses the indexed path when the {@link Indexable} function is declared on
     * a supertype and the query class is that supertype.
     */
    @Test
    default void shouldFindBySupertypeUsingIndexedFunctionInequality() {
        final var index = createIndex();
        final var red = new Circle("red");
        final var blue = new Square("blue");

        index.index(red);
        index.index(blue);

        assertThat(index.match(Shape.class)
            .scope(Scope.Indexed)
            .where(Shape.COLOR)
            .isNotEqualTo("red")
            .findAll()
            .toList()
        ).containsExactly(blue);
    }

    /**
     * Ensure {@link Condition#matches} uses the indexed path when the {@link Indexable} function is declared on
     * a supertype and the query class is that supertype.
     */
    @Test
    default void shouldFindBySupertypeUsingIndexedFunctionPredicate() {
        final var index = createIndex();
        final var red = new Circle("red");
        final var blue = new Square("blue");

        index.index(red);
        index.index(blue);

        assertThat(index.match(Shape.class)
            .scope(Scope.Indexed)
            .where(Shape.COLOR)
            .matches(c -> c.startsWith("r"))
            .findAll()
            .toList()
        ).containsExactly(red);
    }

    /**
     * A simple marker interface for supertype-query tests.
     */
    interface Shape {
        String color();

        @Indexable
        @Unique
        public static final Function<Shape, String> COLOR = Shape::color;
    }

    record Circle(String color) implements Shape {
    }

    record Square(String color) implements Shape {
    }

    /**
     * Ensure {@link Terminal#findAll()} returns all objects that share the same indexed value.
     */
    @Test
    default void shouldReturnAllObjectsWithSameIndexedValue() {
        final var index = createIndex();
        final var red1 = new MutableColorful(Color.RED);
        final var red2 = new MutableColorful(Color.RED);

        index.index(red1);
        index.index(red2);

        assertThat(index.match(MutableColorful.class)
            .where(MutableColorful.COLOR)
            .isEqualTo(Color.RED)
            .findAll()
            .toList()
        ).containsExactlyInAnyOrder(red1, red2);
    }

    /**
     * Ensure {@link Condition#matches(java.util.function.Predicate)} works on the indexed path when the extracted
     * value is {@code null} — indexed path.
     */
    @Test
    default void shouldMatchObjectWithNullValueUsingPredicateOnIndexedPath() {
        final var index = createIndex();
        final var colorful = new NullColorful();
        index.index(colorful);

        assertThat(index.match(NullColorful.class)
            .where(NullColorful.COLOR)
            .matches(c -> c == null)
            .findFirst()
        ).contains(colorful);
        assertThat(index.match(NullColorful.class)
            .where(NullColorful.COLOR)
            .matches(c -> c != null)
            .findFirst()
        ).isEmpty();
    }

    /**
     * Ensure {@link Condition#matches(java.util.function.Predicate)} works on the fallback path when the extracted
     * value is {@code null} — fallback path.
     */
    @Test
    default void shouldMatchObjectWithNullValueUsingPredicateOnFallbackPath() {
        final var index = createIndex();
        final var colorful = new NullFallbackColorful();
        index.add(NullFallbackColorful.class, colorful);

        assertThat(index.match(NullFallbackColorful.class)
            .where(NullFallbackColorful.COLOR)
            .matches(c -> c == null)
            .findFirst()
        ).contains(colorful);
        assertThat(index.match(NullFallbackColorful.class)
            .where(NullFallbackColorful.COLOR)
            .matches(c -> c != null)
            .findFirst()
        ).isEmpty();
    }

    /**
     * Ensure {@link Condition#isNotEqualTo} returns all objects not matching the specified value.
     */
    @Test
    default void shouldReturnObjectsNotEqualToValue() {
        final var index = createIndex();
        index.index(Color.RED);
        index.index(Color.GREEN);
        index.index(Color.BLUE);

        assertThat(index.match(Color.class)
            .where(Color.NAME)
            .isNotEqualTo("RED")
            .findAll()
            .toList()
        ).containsExactlyInAnyOrder(Color.GREEN, Color.BLUE);
    }

    /**
     * Ensure {@link Condition#isNotEqualTo(Object) isNotEqualTo(null)} excludes objects whose indexed value is
     * {@code null} and includes those with a non-{@code null} value (T4, null case).
     */
    @Test
    default void shouldReturnObjectsNotEqualToNull() {
        final var index = createIndex();
        final var withNull = new NullColorful();
        index.index(withNull);

        assertThat(index.match(NullColorful.class)
            .where(NullColorful.COLOR)
            .isNotEqualTo(null)
            .findFirst()
        ).isEmpty();
    }

    /**
     * Ensure concurrent {@link Index#index} and {@link Index#unindex} calls do not throw or corrupt state.
     */
    @Test
    default void shouldHandleConcurrentIndexAndUnindex() throws InterruptedException {
        final var index = createIndex();
        final var latch = new CountDownLatch(1);
        final var errors = new CopyOnWriteArrayList<Throwable>();

        final Runnable task = () -> {
            try {
                latch.await();
                for (int i = 0; i < 50; i++) {
                    final var colorful = new MutableColorful(Color.RED);
                    index.index(colorful);
                    index.unindex(colorful);
                }
            } catch (final Throwable t) {
                errors.add(t);
            }
        };

        final var threads = IntStream.range(0, 8)
            .mapToObj(_ -> new Thread(task))
            .toList();

        threads.forEach(Thread::start);
        latch.countDown();
        for (final var thread : threads) {
            thread.join();
        }

        assertThat(errors).isEmpty();
    }

    /**
     * Ensure the index does not retain stale entries after many index/unindex cycles.
     */
    @Test
    default void shouldCleanUpEntriesAfterManyIndexUnindexCycles() {
        final var index = createIndex();

        for (int i = 0; i < 100; i++) {
            final var colorful = new MutableColorful(Color.RED);
            index.index(colorful);
            index.unindex(colorful);
        }

        assertThat(index.match(MutableColorful.class)
            .findAll()
            .toList()
        ).isEmpty();
        assertThat(index.match(MutableColorful.class)
            .where(MutableColorful.COLOR)
            .isEqualTo(Color.RED)
            .findFirst()
        ).isEmpty();
    }

    /**
     * Ensure a non-{@link Indexable} {@link Object}, which throws an {@link Exception} when indexed, can't be indexed.
     */
    @Test
    default void shouldNotIndexNonIndexableClass() {
        final var index = createIndex();

        assertThrows(UnsupportedOperationException.class,
            () -> index.index(new NotIndexable(Color.RED)));
    }

    /**
     * Ensure that when {@link Index#index} throws, the object is not registered in class-membership — i.e.,
     * {@code objectByClass} is left clean because it is populated only after function-map indexing succeeds.
     */
    @Test
    default void shouldNotRegisterInClassMembershipWhenIndexingFails() {
        final var index = createIndex();
        final var object = new NotIndexable(Color.RED);

        assertThrows(UnsupportedOperationException.class, () -> index.index(object));

        assertThat(index.match(NotIndexable.class)
            .scope(Scope.Indexed)
            .findAll()
            .toList()
        ).isEmpty();
    }

    /**
     * Ensure that a class with no {@link Indexable} annotation and no {@link Indexable} function fields is
     * <em>not</em> registered in class-membership when indexed — only the function-indexed maps are used.
     *
     * <p>Regression for an over-eager fix that added every indexed object to {@code objectByClass} regardless
     * of whether the class opted in via {@link Indexable}.
     */
    @Test
    default void shouldNotRegisterInClassMembershipWhenNoIndexableFieldOrAnnotation() {
        final var index = createIndex();

        index.index(new Plain("hello"));

        assertThat(index.match(Plain.class)
            .scope(Scope.Indexed)
            .findAll()
            .toList()
        ).isEmpty();
    }

    /**
     * Ensure that a class with an {@link Indexable} function field (but no type-level {@link Indexable} annotation)
     * <em>is</em> registered in class-membership when indexed.
     *
     * <p>This is the complement of {@link #shouldNotRegisterInClassMembershipWhenNoIndexableFieldOrAnnotation}:
     * a function-field {@link Indexable} is sufficient to opt the type in to class-membership queries.
     */
    @Test
    default void shouldRegisterInClassMembershipWhenFunctionFieldIsIndexable() {
        final var index = createIndex();

        final var colorful = new Colorful(Color.RED);
        index.index(colorful);

        assertThat(index.match(Colorful.class)
            .scope(Scope.Indexed)
            .findAll()
            .toList()
        ).containsExactly(colorful);
    }

    // ---- reindexDynamic

    /**
     * Ensure {@link Index#reindexDynamic} is a no-op for objects that have only stable (non-{@link Dynamic})
     * {@link Indexable} functions — their index entries must be unchanged.
     */
    @Test
    default void shouldLeaveStableEntriesUnchangedOnReindexDynamic() {
        final var index = createIndex();

        final var colorful = new Colorful(Color.RED);
        index.index(colorful);

        assertThat(index.match(Colorful.class).where(Colorful.COLOR).isEqualTo(Color.RED).findFirst())
            .contains(colorful);

        index.reindexDynamic(colorful);

        assertThat(index.match(Colorful.class).where(Colorful.COLOR).isEqualTo(Color.RED).findFirst())
            .contains(colorful);
    }

    /**
     * Ensure {@link Index#reindexDynamic} updates the forward map for a {@link Dynamic} {@link Indexable} function
     * after the object's state changes.
     */
    @Test
    default void shouldReflectMutatedValueAfterReindexDynamic() {
        final var index = createIndex();

        final var colorful = new DynamicColorful(Color.RED);
        index.index(colorful);

        assertThat(index.match(DynamicColorful.class).where(DynamicColorful.COLOR).isEqualTo(Color.RED).findFirst())
            .contains(colorful);

        colorful.setColor(Color.GREEN);
        index.reindexDynamic(colorful);

        assertThat(index.match(DynamicColorful.class).where(DynamicColorful.COLOR).isEqualTo(Color.RED).findFirst())
            .isEmpty();
        assertThat(index.match(DynamicColorful.class).where(DynamicColorful.COLOR).isEqualTo(Color.GREEN).findFirst())
            .contains(colorful);
    }

    /**
     * Ensure {@link Index#reindexDynamic} updates only the {@link Dynamic} entry and leaves the stable entry
     * untouched when an object has both stable and {@link Dynamic} {@link Indexable} functions.
     */
    @Test
    default void shouldUpdateOnlyDynamicEntryAndLeaveStableEntryUnchangedOnReindexDynamic() {
        final var index = createIndex();

        final var colorful = new StableAndDynamicColorful("alpha", Color.RED);
        index.index(colorful);

        assertThat(index.match(StableAndDynamicColorful.class).where(StableAndDynamicColorful.NAME).isEqualTo("alpha").findFirst())
            .contains(colorful);
        assertThat(index.match(StableAndDynamicColorful.class).where(StableAndDynamicColorful.COLOR).isEqualTo(Color.RED).findFirst())
            .contains(colorful);

        colorful.setColor(Color.BLUE);
        index.reindexDynamic(colorful);

        assertThat(index.match(StableAndDynamicColorful.class).where(StableAndDynamicColorful.NAME).isEqualTo("alpha").findFirst())
            .contains(colorful);
        assertThat(index.match(StableAndDynamicColorful.class).where(StableAndDynamicColorful.COLOR).isEqualTo(Color.RED).findFirst())
            .isEmpty();
        assertThat(index.match(StableAndDynamicColorful.class).where(StableAndDynamicColorful.COLOR).isEqualTo(Color.BLUE).findFirst())
            .contains(colorful);
    }

    /**
     * Ensure {@link Index#reindexDynamic} correctly updates the unique map when a {@link Dynamic} {@link Unique}
     * {@link Indexable} function's value changes.
     */
    @Test
    default void shouldUpdateUniqueMapOnReindexDynamicForDynamicUniqueFunction() {
        final var index = createIndex();

        final var colorful = new DynamicUniqueColorful("key-A");
        index.index(colorful);

        assertThat(index.match(DynamicUniqueColorful.class).where(DynamicUniqueColorful.ID).isEqualTo("key-A").findFirst())
            .contains(colorful);

        colorful.setId("key-B");
        index.reindexDynamic(colorful);

        assertThat(index.match(DynamicUniqueColorful.class).where(DynamicUniqueColorful.ID).isEqualTo("key-A").findFirst())
            .isEmpty();
        assertThat(index.match(DynamicUniqueColorful.class).where(DynamicUniqueColorful.ID).isEqualTo("key-B").findFirst())
            .contains(colorful);
    }

    /**
     * Demonstrates the difference between a plain {@link Indexable} function and one annotated with
     * {@code @Indexable(each = true)}.
     *
     * <p>Both fields on {@link ScalarVsEach} return a {@link List}{@code <Color>}, but they are indexed differently:
     * <ul>
     *   <li>{@code COLORS_SCALAR} — the whole {@link List} is the key; {@code isEqualTo(list)} finds the object and
     *       {@code contains(element)} returns nothing because no individual element is a key.</li>
     *   <li>{@code COLORS_EACH} — each {@link Color} is an individual key; {@code contains(element)} finds the object
     *       and {@code isEqualTo(list)} returns nothing because the whole list is not a key.</li>
     * </ul>
     */
    @Test
    default void shouldDistinguishScalarIndexingFromEachIndexing() {
        final var index = createIndex();
        final var object = new ScalarVsEach(Color.RED, Color.GREEN);
        index.index(object);

        // scalar: the whole list is the key
        assertThat(index.match(ScalarVsEach.class)
            .where(ScalarVsEach.COLORS_SCALAR)
            .isEqualTo(List.of(Color.RED, Color.GREEN))
            .findAll())
            .containsExactly(object);

        assertThat(index.match(ScalarVsEach.class)
            .where(ScalarVsEach.COLORS_SCALAR)
            .contains(Color.RED)
            .findAll())
            .isEmpty();

        // each: individual elements are keys
        assertThat(index.match(ScalarVsEach.class)
            .where(ScalarVsEach.COLORS_EACH)
            .contains(Color.RED)
            .findAll())
            .containsExactly(object);

        assertThat(index.match(ScalarVsEach.class)
            .where(ScalarVsEach.COLORS_EACH)
            .isEqualTo(List.of(Color.RED, Color.GREEN))
            .findAll())
            .isEmpty();
    }

    /**
     * Ensure objects with a multi-valued {@link Indexable} function can be queried with {@code contains}.
     */
    @Test
    default void shouldIndexAndQueryClassWithMultiValuedIndexableField() {
        final var index = createIndex();

        final var redGreen = new MultiColored(Color.RED, Color.GREEN);
        final var greenBlue = new MultiColored(Color.GREEN, Color.BLUE);
        index.index(redGreen);
        index.index(greenBlue);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .contains(Color.RED)
            .findAll())
            .containsExactly(redGreen);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .contains(Color.GREEN)
            .findAll())
            .containsExactlyInAnyOrder(redGreen, greenBlue);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .contains(Color.BLUE)
            .findAll())
            .containsExactly(greenBlue);
    }

    /**
     * Ensure objects with a multi-valued {@link Indexable} function can be unindexed cleanly.
     */
    @Test
    default void shouldUnindexObjectWithMultiValuedIndexableField() {
        final var index = createIndex();

        final var redGreen = new MultiColored(Color.RED, Color.GREEN);
        final var greenBlue = new MultiColored(Color.GREEN, Color.BLUE);
        index.index(redGreen);
        index.index(greenBlue);

        index.unindex(redGreen);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .contains(Color.RED)
            .findAll())
            .isEmpty();

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .contains(Color.GREEN)
            .findAll())
            .containsExactly(greenBlue);
    }

    /**
     * Ensure a multi-valued {@link Indexable} function that returns a {@link java.util.Collection} is indexed and
     * queried correctly (exercises the {@code Collection} branch of the indexing path).
     */
    @Test
    default void shouldIndexAndQueryClassWithCollectionMultiValuedField() {
        final var index = createIndex();

        final var redGreen = new MultiColoredWithList(Color.RED, Color.GREEN);
        final var greenBlue = new MultiColoredWithList(Color.GREEN, Color.BLUE);
        index.index(redGreen);
        index.index(greenBlue);

        assertThat(index.match(MultiColoredWithList.class)
            .where(MultiColoredWithList.COLORS)
            .contains(Color.RED)
            .findAll())
            .containsExactly(redGreen);

        assertThat(index.match(MultiColoredWithList.class)
            .where(MultiColoredWithList.COLORS)
            .contains(Color.GREEN)
            .findAll())
            .containsExactlyInAnyOrder(redGreen, greenBlue);
    }

    /**
     * Ensure a multi-valued {@link Indexable} function that returns a plain {@link Iterable} (not a
     * {@link java.util.Collection}) is indexed and queried correctly (exercises the {@code Iterable} branch).
     */
    @Test
    default void shouldIndexAndQueryClassWithIterableMultiValuedField() {
        final var index = createIndex();

        final var redGreen = new MultiColoredWithIterable(Color.RED, Color.GREEN);
        final var greenBlue = new MultiColoredWithIterable(Color.GREEN, Color.BLUE);
        index.index(redGreen);
        index.index(greenBlue);

        assertThat(index.match(MultiColoredWithIterable.class)
            .where(MultiColoredWithIterable.COLORS)
            .contains(Color.RED)
            .findAll())
            .containsExactly(redGreen);

        assertThat(index.match(MultiColoredWithIterable.class)
            .where(MultiColoredWithIterable.COLORS)
            .contains(Color.GREEN)
            .findAll())
            .containsExactlyInAnyOrder(redGreen, greenBlue);
    }

    /**
     * Ensure reindexing a multi-valued object removes the old element keys and adds the new ones.
     */
    @Test
    default void shouldReindexObjectWithMultiValuedIndexableField() {
        final var index = createIndex();

        final var object = new MutableMultiColored(Color.RED, Color.GREEN);
        index.index(object);

        object.setColors(Color.GREEN, Color.BLUE);
        index.reindex(object);

        assertThat(index.match(MutableMultiColored.class)
            .where(MutableMultiColored.COLORS)
            .contains(Color.RED)
            .findAll())
            .isEmpty();

        assertThat(index.match(MutableMultiColored.class)
            .where(MutableMultiColored.COLORS)
            .contains(Color.GREEN)
            .findAll())
            .containsExactly(object);

        assertThat(index.match(MutableMultiColored.class)
            .where(MutableMultiColored.COLORS)
            .contains(Color.BLUE)
            .findAll())
            .containsExactly(object);
    }

    /**
     * Ensure {@code contains} falls back to a linear scan when the function is not {@link Indexable}.
     */
    @Test
    default void shouldContainsFallBackToLinearScanForNonIndexedFunction() {
        final var index = createIndex();

        final var redGreen = new MultiColored(Color.RED, Color.GREEN);
        final var greenBlue = new MultiColored(Color.GREEN, Color.BLUE);
        index.index(redGreen);
        index.index(greenBlue);

        final Function<MultiColored, Stream<Color>> nonIndexed = c -> MultiColored.COLORS.apply(c);

        assertThat(index.match(MultiColored.class)
            .where(nonIndexed)
            .contains(Color.RED)
            .findAll())
            .containsExactly(redGreen);

        assertThat(index.match(MultiColored.class)
            .where(nonIndexed)
            .contains(Color.GREEN)
            .findAll())
            .containsExactlyInAnyOrder(redGreen, greenBlue);
    }

    /**
     * Ensure {@code doesNotContain} returns objects whose multi-valued field does not include the specified element.
     */
    @Test
    default void shouldQueryDoesNotContainWithMultiValuedField() {
        final var index = createIndex();

        final var redGreen = new MultiColored(Color.RED, Color.GREEN);
        final var greenBlue = new MultiColored(Color.GREEN, Color.BLUE);
        final var onlyBlue = new MultiColored(Color.BLUE);
        index.index(redGreen);
        index.index(greenBlue);
        index.index(onlyBlue);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .doesNotContain(Color.RED)
            .findAll())
            .containsExactlyInAnyOrder(greenBlue, onlyBlue);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .doesNotContain(Color.GREEN)
            .findAll())
            .containsExactly(onlyBlue);

        assertThat(index.match(MultiColored.class)
            .where(MultiColored.COLORS)
            .doesNotContain(Color.BLUE)
            .findAll())
            .containsExactly(redGreen);
    }

    /**
     * A simple {@link Enum} for testing.
     */
    @Indexable
    enum Color {

        RED,
        GREEN,
        BLUE;

        /**
         * Defines an {@link Indexable} method to retrieve the name of the color.
         */
        @Indexable
        public static final Function<Color, String> NAME = Color::name;
    }

    /**
     * A simple {@code record} for testing that uses an {@link Indexable} method.
     */
    record Colorful(Color color) {

        /**
         * Defines an {@link Indexable} method to retrieve the {@link Color}.
         */
        @Indexable
        public static final Function<Colorful, Color> COLOR = Colorful::getColor;

        public Color getColor() {
            return this.color;
        }
    }

    /**
     * A mutable class for testing that unindexing succeeds even when state has changed since indexing.
     */
    class MutableColorful {

        /**
         * Defines an {@link Indexable} method to retrieve the {@link Color}.
         */
        @Indexable
        public static final Function<MutableColorful, Color> COLOR = MutableColorful::getColor;

        /**
         * The {@link Color} of this {@link MutableColorful}.
         */
        private Color color;

        /**
         * Constructs a {@link MutableColorful} with the specified {@link Color}.
         *
         * @param color the {@link Color}
         */
        MutableColorful(final Color color) {
            this.color = color;
        }

        /**
         * Obtains the {@link Color} of this {@link MutableColorful}.
         *
         * @return the {@link Color}
         */
        public Color getColor() {
            return this.color;
        }

        /**
         * Sets the {@link Color} of this {@link MutableColorful}.
         *
         * @param color the {@link Color}
         */
        public void setColor(final Color color) {
            this.color = color;
        }
    }

    /**
     * A simple class for testing that an {@link Indexable} function returning {@code null} is handled correctly.
     */
    class NullColorful {

        /**
         * Defines an {@link Indexable} function that always returns {@code null}.
         */
        @Indexable
        public static final Function<NullColorful, Color> COLOR = _ -> null;
    }

    /**
     * A class whose {@link #COLOR} function is intentionally <em>not</em> {@link Indexable}, forcing queries to
     * evaluate against the fallback stream rather than the index.  The function always returns {@code null}.
     */
    record NullFallbackColorful() {

        /**
         * A non-{@link Indexable} function that always returns {@code null}.
         */
        public static final Function<NullFallbackColorful, Color> COLOR = _ -> null;
    }

    /**
     * A plain class with no {@link Indexable} annotation and no {@link Indexable} function fields — used to verify
     * that {@link Index#index} does not register such types in class-membership.
     */
    record Plain(String value) {
    }

    /**
     * A simple {@code record} for testing purposes that is not indexable.
     *
     * @param color the {@link Color}
     */
    record NotIndexable(Color color) {
        public Color color() {
            throw new UnsupportedOperationException("Not indexable!");
        }

        @Indexable
        public static final Function<NotIndexable, Color> COLOR = NotIndexable::color;
    }

    /**
     * A {@code record} whose {@link #COLOR} function is both {@link Indexable} and {@link Unique}.
     *
     * @param color the {@link Color}
     */
    record UniqueColorful(Color color) {

        @Indexable
        @Unique
        public static final Function<UniqueColorful, Color> COLOR = UniqueColorful::color;
    }

    /**
     * A mutable class with a {@link Dynamic} {@link Indexable} function, used to verify that
     * {@link Index#reindexDynamic} updates the index after mutation.
     */
    class DynamicColorful {

        @Indexable
        @Dynamic
        public static final Function<DynamicColorful, Color> COLOR = DynamicColorful::getColor;

        private Color color;

        DynamicColorful(final Color color) {
            this.color = color;
        }

        public Color getColor() {
            return this.color;
        }

        public void setColor(final Color color) {
            this.color = color;
        }
    }

    /**
     * A mutable class with one stable and one {@link Dynamic} {@link Indexable} function.
     */
    class StableAndDynamicColorful {

        @Indexable
        public static final Function<StableAndDynamicColorful, String> NAME = o -> o.name;

        @Indexable
        @Dynamic
        public static final Function<StableAndDynamicColorful, Color> COLOR = o -> o.color;

        private final String name;
        private Color color;

        StableAndDynamicColorful(final String name, final Color color) {
            this.name = name;
            this.color = color;
        }

        public void setColor(final Color color) {
            this.color = color;
        }
    }

    /**
     * A mutable class with a {@link Dynamic} {@link Unique} {@link Indexable} function.
     */
    class DynamicUniqueColorful {

        @Indexable
        @Unique
        @Dynamic
        public static final Function<DynamicUniqueColorful, String> ID = DynamicUniqueColorful::getId;

        private String id;

        DynamicUniqueColorful(final String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }
    }

    /**
     * A class with two {@link Indexable} functions over the same {@link List}{@code <Color>} field, one scalar and
     * one each, used to demonstrate the difference between the two indexing modes.
     */
    class ScalarVsEach {

        /** The whole {@link List} is the index key. */
        @Indexable
        public static final Function<ScalarVsEach, List<Color>> COLORS_SCALAR = o -> o.colors;

        /** Each {@link Color} in the {@link List} is an individual index key. */
        @Indexable(each = true)
        public static final Function<ScalarVsEach, List<Color>> COLORS_EACH = o -> o.colors;

        private final List<Color> colors;

        ScalarVsEach(final Color... colors) {
            this.colors = List.of(colors);
        }
    }

    /**
     * A class with a multi-valued {@link Indexable} function that returns a {@link Stream}.
     */
    class MultiColored {

        @Indexable(each = true)
        public static final Function<MultiColored, Stream<Color>> COLORS = c -> c.colors.stream();

        private final List<Color> colors;

        MultiColored(final Color... colors) {
            this.colors = List.of(colors);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof MultiColored mc && this.colors.equals(mc.colors);
        }

        @Override
        public int hashCode() {
            return this.colors.hashCode();
        }
    }

    /**
     * A class with a multi-valued {@link Indexable} function that returns a {@link List} (exercises the
     * {@link java.util.Collection} indexing branch).
     */
    class MultiColoredWithList {

        @Indexable(each = true)
        public static final Function<MultiColoredWithList, List<Color>> COLORS = c -> c.colors;

        private final List<Color> colors;

        MultiColoredWithList(final Color... colors) {
            this.colors = List.of(colors);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof MultiColoredWithList mc && this.colors.equals(mc.colors);
        }

        @Override
        public int hashCode() {
            return this.colors.hashCode();
        }
    }

    /**
     * A class with a multi-valued {@link Indexable} function that returns a plain {@link Iterable} that is not a
     * {@link java.util.Collection} (exercises the {@code Iterable} indexing branch).
     */
    class MultiColoredWithIterable {

        @Indexable(each = true)
        public static final Function<MultiColoredWithIterable, Iterable<Color>> COLORS =
            c -> () -> c.colors.iterator();

        private final List<Color> colors;

        MultiColoredWithIterable(final Color... colors) {
            this.colors = List.of(colors);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof MultiColoredWithIterable mc && this.colors.equals(mc.colors);
        }

        @Override
        public int hashCode() {
            return this.colors.hashCode();
        }
    }

    /**
     * A mutable class with a multi-valued {@link Indexable} function, used to verify that reindexing after mutation
     * removes the old element keys and adds the new ones.
     */
    class MutableMultiColored {

        @Indexable(each = true)
        public static final Function<MutableMultiColored, Stream<Color>> COLORS = c -> c.colors.stream();

        private List<Color> colors;

        MutableMultiColored(final Color... colors) {
            this.colors = List.of(colors);
        }

        void setColors(final Color... colors) {
            this.colors = List.of(colors);
        }
    }
}
