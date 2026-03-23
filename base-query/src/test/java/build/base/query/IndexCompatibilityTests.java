package build.base.query;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
