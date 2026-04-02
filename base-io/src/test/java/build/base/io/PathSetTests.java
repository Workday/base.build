package build.base.io;

import build.base.foundation.predicate.Predicates;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PathSet}s.
 *
 * @author brian.oliver
 * @since Jan-2021
 */
class PathSetTests {

    /**
     * Ensure empty {@link PathSet}s are empty.
     */
    @Test
    void shouldCreateEmptyPathSet() {
        final var pathSet = PathSet.empty();

        assertThat(pathSet)
            .isEmpty();

        assertThat(pathSet.size())
            .isEqualTo(0);

        assertThat(pathSet.isEmpty())
            .isTrue();

        assertThat(pathSet.stream())
            .isEmpty();

        assertThat(pathSet)
            .isSameAs(PathSet.empty());

        assertThat(pathSet)
            .isEqualTo(PathSet.empty());

        assertThat(PathSet.empty())
            .isEqualTo(pathSet);
    }

    /**
     * Ensure an empty {@link PathSetBuilder} creates an empty {@link PathSet}.
     */
    @Test
    void shouldBuildEmptyPathSet() {
        final var pathSet = PathSetBuilder
            .create()
            .build();

        assertThat(pathSet)
            .isEmpty();

        assertThat(pathSet)
            .isEmpty();

        assertThat(pathSet.size())
            .isEqualTo(0);

        assertThat(pathSet.isEmpty())
            .isTrue();

        assertThat(pathSet.stream())
            .isEmpty();

        assertThat(pathSet)
            .isEqualTo(PathSet.empty());

        assertThat(PathSet.empty())
            .isEqualTo(pathSet);
    }

    /**
     * Ensure a {@link PathSetBuilder} builds a single {@link Path} {@link PathSet}.
     */
    @Test
    void shouldBuildSinglePathPathSet() {
        final var path = Paths.get("/usr/local/bin");
        final var builder = PathSetBuilder.create(path);

        // attempt to add the same path again (a few different ways!)
        builder.add(path);
        builder.addAll(path);
        builder.addAll(Stream.of(path));
        builder.addAll(builder);

        final PathSet pathSet = builder.build();

        // there should be only one (entry)
        assertThat(pathSet.size())
            .isEqualTo(1);

        assertThat(pathSet)
            .isNotEmpty();

        assertThat(pathSet.isEmpty())
            .isFalse();

        assertThat(pathSet.stream())
            .hasSize(1);

        assertThat(pathSet.stream().findFirst())
            .contains(path);

        assertThat(pathSet)
            .isNotEqualTo(PathSet.empty());

        assertThat(PathSet.empty())
            .isNotEqualTo(pathSet);
    }

    /**
     * Ensure a {@link PathSetBuilder} builds a multi-{@link Path} {@link PathSet}.
     */
    @Test
    void shouldBuildMultiPathPathSet() {
        final var first = Paths.get("/usr/local/bin");
        final var second = Paths.get("/usr/bin");
        final var third = Paths.get("/home");

        final var builder = PathSetBuilder.create(first);

        // attempt to add the same paths (a few different ways!)
        builder.add(first)
            .add(second)
            .add(third);

        builder.addAll(first, second, third);
        builder.addAll(Stream.of(first, second, third));
        builder.addAll(builder);

        final var pathSet = builder.build();

        // there should be three entries
        assertThat(pathSet.size())
            .isEqualTo(3);

        assertThat(pathSet.isEmpty())
            .isFalse();

        assertThat(pathSet.stream())
            .hasSize(3);

        assertThat(pathSet.stream()
            .findFirst())
            .contains(first);

        assertThat(pathSet.stream()
            .skip(1)
            .findFirst())
            .contains(second);

        assertThat(pathSet.stream()
            .skip(2)
            .findFirst())
            .contains(third);

        assertThat(pathSet)
            .isNotEqualTo(PathSet.empty());

        assertThat(PathSet.empty())
            .isNotEqualTo(pathSet);
    }

    /**
     * Ensure {@link Path}s can be removed from a {@link PathSetBuilder}.
     */
    @Test
    void shouldRemovePathsFromBuilder() {
        final var first = Paths.get("/usr/local/bin");
        final var second = Paths.get("/usr/bin");
        final var third = Paths.get("/home");

        final var builder = PathSetBuilder.create(first, second, third);

        builder.removeIf(path -> path.toString().contains("bin"));

        final var pathSet = builder.build();

        // there should be only one (entry)
        assertThat(pathSet.size())
            .isEqualTo(1);

        assertThat(pathSet.isEmpty())
            .isFalse();

        assertThat(pathSet.stream())
            .hasSize(1);

        assertThat(pathSet.stream()
            .findFirst())
            .contains(third);

        // ensure all paths can be removed
        final var empty = builder
            .removeIf(Predicates.always())
            .build();

        assertThat(empty)
            .isEmpty();

        assertThat(empty.isEmpty())
            .isTrue();

        assertThat(pathSet)
            .isNotEqualTo(PathSet.empty());

        assertThat(PathSet.empty())
            .isNotEqualTo(pathSet);
    }

    /**
     * Ensure a {@link Stream} of {@link Path}s can be collected into a {@link PathSet}.
     */
    @Test
    void shouldCollectPathsIntoAPathSet() {
        final var first = Paths.get("/usr/local/bin");
        final var second = Paths.get("/usr/bin");
        final var third = Paths.get("/home");

        final var list = Stream.of(first, second, third)
            .toList();

        final var pathSet = list.stream().collect(PathSet.collector());

        // there should be three entries
        assertThat(pathSet.size())
            .isEqualTo(3);

        assertThat(pathSet.isEmpty())
            .isFalse();

        assertThat(pathSet.stream())
            .hasSize(3);

        assertThat(pathSet.stream()
            .findFirst())
            .contains(first);

        assertThat(pathSet.stream()
            .skip(1)
            .findFirst())
            .contains(second);

        assertThat(pathSet.stream()
            .skip(2)
            .findFirst())
            .contains(third);

        assertThat(pathSet)
            .isNotEqualTo(PathSet.empty());

        assertThat(PathSet.empty())
            .isNotEqualTo(pathSet);
    }

    /**
     * Ensure {@link PathSet}s can be compared.
     */
    @Test
    void shouldComparePathSetsWithEquals() {
        final var first = Paths.get("/usr/local/bin");
        final var second = Paths.get("/usr/bin");
        final var third = Paths.get("/home");

        final var pathSet1 = PathSet.of(first, second, third);
        final var pathSet2 = PathSet.of(first, second, third);

        assertThat(pathSet1)
            .isEqualTo(pathSet2);

        assertThat(pathSet1.hashCode())
            .isEqualTo(pathSet2.hashCode());
    }
}
