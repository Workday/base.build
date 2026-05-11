package build.base.query;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HeapBasedIndex}es.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
public class HeapBasedIndexTests implements IndexCompatibilityTests {

    @Override
    public Index createIndex() {
        return new HeapBasedIndex();
    }

    /**
     * Ensure a {@link Scope} set on a {@link Match} is inherited by the terminal so that
     * {@code traverse(Class, Scope)} receives the match-level scope rather than the default {@link Scope#Direct}.
     * The terminal's own {@link Terminal#scope} override must still take precedence.
     */
    @Test
    void shouldPropagateMatchScopeToTerminal() {
        final var capturedScope = new AtomicReference<Scope>();
        final Index index = getIndex(capturedScope);

        // match-level scope propagates to terminal
        index.match(String.class)
            .scope(Scope.BreadthFirst)
            .where(s -> s)
            .isEqualTo("hello")
            .findAll()
            .toList();

        assertThat(capturedScope.get()).isEqualTo(Scope.BreadthFirst);

        // terminal-level scope overrides match-level scope
        index.match(String.class)
            .scope(Scope.BreadthFirst)
            .where(s -> s)
            .isEqualTo("hello")
            .scope(Scope.DepthFirst)
            .findAll()
            .toList();

        assertThat(capturedScope.get()).isEqualTo(Scope.DepthFirst);
    }

    /**
     * Ensure that {@link Scope#Direct}, {@link Scope#BreadthFirst}, and {@link Scope#DepthFirst} are additive:
     * results from both the index and {@code traverse} are returned, deduplicated by identity.
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldConcatenateIndexedAndTraversedObjectsForNonIndexedScopes() {
        final var indexedOnly = "indexed-only";
        final var traversedOnly = "traversed-only";
        final var shared = "shared";

        final Index index = new AbstractHeapBasedIndex() {
            @Override
            protected <T> Iterator<T> traverse(final Class<T> cls, final Scope scope) {
                if (cls == String.class) {
                    return (Iterator<T>) List.of(shared, traversedOnly).iterator();
                }
                return Collections.emptyIterator();
            }

            @Override
            protected Iterator<Object> traverse(final Scope scope) {
                return Collections.emptyIterator();
            }
        };

        index.add(String.class, indexedOnly);
        index.add(String.class, shared);

        for (final var scope : List.of(Scope.Direct, Scope.BreadthFirst, Scope.DepthFirst)) {
            assertThat(index.match(String.class).scope(scope).findAll().toList())
                .as("scope %s should be additive", scope)
                .containsExactlyInAnyOrder(indexedOnly, shared, traversedOnly);
        }
    }

    /**
     * Ensure that {@link Scope#Indexed} short-circuits without invoking {@code traverse}.
     */
    @Test
    void shouldNotTraverseWhenScopeIsIndexed() {
        final var capturedScope = new AtomicReference<Scope>();
        final Index index = getIndex(capturedScope);

        index.add(String.class, "hello");

        index.match(String.class).scope(Scope.Indexed).findAll().toList();

        assertThat(capturedScope.get()).isNull();
    }

    private static Index getIndex(final AtomicReference<Scope> capturedScope) {
        return new AbstractHeapBasedIndex() {
            @Override
            protected <T> Iterator<T> traverse(final Class<T> cls, final Scope scope) {
                capturedScope.set(scope);
                return Collections.emptyIterator();
            }

            @Override
            protected Iterator<Object> traverse(final Scope scope) {
                return Collections.emptyIterator();
            }
        };
    }
}
