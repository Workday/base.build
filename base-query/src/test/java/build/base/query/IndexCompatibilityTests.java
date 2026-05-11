package build.base.query;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.IntStream;

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

        assertThat(index.match(Shape.class).findAll().toList())
            .containsExactlyInAnyOrder(circle, square);
    }

    /**
     * Ensure {@link Scope#Indexed} also returns subclass instances when querying by supertype.
     */
    @Test
    default void shouldFindSubclassInstancesWithIndexedScopeWhenQueryingBySupertype() {
        final var index = createIndex();
        final var circle = new Circle("red");

        index.add(Circle.class, circle);

        assertThat(index.match(Shape.class).scope(Scope.Indexed).findAll().toList())
            .containsExactly(circle);
    }

    /**
     * A simple marker interface for supertype-query tests.
     */
    interface Shape {
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
}
