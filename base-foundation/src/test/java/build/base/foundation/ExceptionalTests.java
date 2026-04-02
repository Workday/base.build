package build.base.foundation;

import build.base.foundation.predicate.Predicates;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link Exceptional}s.
 *
 * @author brian.oliver
 * @since Nov-2021
 */
class ExceptionalTests {

    /**
     * Ensure an {@link Exceptional#empty()} can be created.
     */
    @Test
    void shouldCreateEmptyExceptional() {
        final var exceptional = Exceptional.<String>empty();

        assertThat(exceptional.isEmpty())
            .isTrue();

        assertThat(exceptional.isPresent())
            .isFalse();

        assertThat(exceptional.orElse("World"))
            .isEqualTo("World");

        assertThat(exceptional.orElseGet(() -> "World"))
            .isEqualTo("World");

        assertThrows(NoSuchElementException.class, exceptional::orElseThrow);

        assertThat(exceptional.isException())
            .isFalse();

        assertThat(exceptional.exception())
            .isEmpty();

        assertThat(exceptional.optional())
            .isEmpty();

        assertThat(exceptional.filter(Predicates.always()))
            .isEmpty();

        assertThat(exceptional.map(Function.identity()))
            .isEmpty();

        assertThat(exceptional.flatMap(__ -> Exceptional.of(42)))
            .isEmpty();

        assertThat(exceptional
            .otherwise(() -> Exceptional.of("Hello"))
            .orElseThrow())
            .isEqualTo("Hello");
    }

    /**
     * Ensure an {@link Exceptional} for a {@link Exception} can be created.
     */
    @Test
    void shouldCreateExceptionExceptional() {
        final var exception = new RuntimeException("This is exception, but won't be thrown!");
        final var exceptional = Exceptional.<String>ofException(exception);

        assertThat(exceptional.isEmpty())
            .isFalse();

        assertThat(exceptional.isException())
            .isTrue();

        assertThat(exceptional.isPresent())
            .isFalse();

        assertThat(exceptional.orElse("World"))
            .isEqualTo("World");

        assertThat(exceptional.orElseGet(() -> "World"))
            .isEqualTo("World");

        assertThrows(NoSuchElementException.class, exceptional::orElseThrow);

        assertThat(exceptional.isException())
            .isTrue();

        assertThat(exceptional.exception())
            .isPresent();

        assertThat(exceptional.exception()
            .orElseThrow(() -> new NoSuchElementException("Expected a Exception")))
            .isSameAs(exception);

        assertThat(exceptional.exception().get())
            .isSameAs(exception);

        assertThat(exceptional.optional())
            .isEmpty();

        assertThat(exceptional
            .filter(Predicates.always())
            .isEmpty())
            .isFalse();

        assertThat(exceptional
            .filter(Predicates.always())
            .isPresent())
            .isFalse();

        assertThat(exceptional
            .filter(Predicates.always())
            .isException())
            .isTrue();

        assertThat(exceptional
            .map(Function.identity())
            .isEmpty())
            .isFalse();

        assertThat(exceptional
            .map(Function.identity())
            .isPresent())
            .isFalse();

        assertThat(exceptional
            .map(Function.identity())
            .isException())
            .isTrue();

        assertThat(exceptional
            .map(Function.identity())
            .exception()
            .orElseThrow())
            .isSameAs(exception);

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .isEmpty())
            .isFalse();

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .isPresent())
            .isFalse();

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .isException())
            .isTrue();

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .exception()
            .orElseThrow())
            .isSameAs(exception);

        assertThat(exceptional
            .otherwise(() -> Exceptional.of("Hello"))
            .orElseThrow())
            .isEqualTo("Hello");
    }

    /**
     * Ensure an {@link Exceptional} for a value may be created.
     */
    @Test
    void shouldCreateValueBasedExceptional() {
        final var exceptional = Exceptional.of("Hello");

        assertThat(exceptional.isEmpty())
            .isFalse();

        assertThat(exceptional.isPresent())
            .isTrue();

        assertThat(exceptional.orElseThrow())
            .isEqualTo("Hello");

        assertThat(exceptional.orElse("World"))
            .isEqualTo("Hello");

        assertThat(exceptional.orElseGet(() -> "World"))
            .isEqualTo("Hello");

        assertThat(exceptional.orElseThrow())
            .isEqualTo("Hello");

        assertThat(exceptional.isException())
            .isFalse();

        assertThat(exceptional.exception())
            .isNotNull();

        assertThat(exceptional.exception().isPresent())
            .isFalse();

        assertThat(exceptional.exception())
            .isEqualTo(Optional.empty());

        assertThat(exceptional.optional())
            .isNotNull();

        assertThat(exceptional.optional())
            .isPresent();

        assertThat(exceptional.optional().get())
            .isEqualTo("Hello");

        assertThat(exceptional
            .map(Function.identity())
            .isEmpty())
            .isFalse();

        assertThat(exceptional
            .map(Function.identity())
            .orElseThrow())
            .isEqualTo("Hello");

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .isEmpty())
            .isFalse();

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .orElseThrow())
            .isEqualTo(42);

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .isException())
            .isFalse();

        assertThat(exceptional
            .flatMap(__ -> Exceptional.of(42))
            .exception())
            .isEqualTo(Optional.empty());

        assertThat(exceptional
            .otherwise(() -> Exceptional.of("World"))
            .orElseThrow())
            .isEqualTo("Hello");
    }

    /**
     * Verify that {@link Exceptional#ifPresent} runs the supplied action if a value is present
     */
    @Test
    void shouldRunIfPresent() {
        final var set = Capture.<Boolean>empty();

        Exceptional.empty()
            .ifPresent(x -> set.set(true));

        assertThat(set.isEmpty())
            .isTrue();

        Exceptional.ofException(new NullPointerException())
            .ifPresent(x -> set.set(true));

        assertThat(set.isEmpty())
            .isTrue();

        Exceptional.of(42)
            .ifPresent(x -> set.set(true));

        assertThat(set.isPresent())
            .isTrue();

        final var exceptional = Exceptional.of(42)
            .ifPresent(x -> {
                throw new RuntimeException();
            });

        assertThat(exceptional.isException())
            .isTrue();
    }

    /**
     * Verify that {@link Exceptional#ifPresentOrElse} runs the supplied action if a value is present, otherwise the else action
     */
    @Test
    void shouldRunIfPresentOrElse() {
        final var value = Capture.<Boolean>empty();

        Exceptional.empty()
            .ifPresentOrElse(x -> value.set(true), () -> value.set(false));

        assertThat(value.orElseThrow())
            .isFalse();

        value.clear();

        Exceptional.ofException(new NullPointerException())
            .ifPresentOrElse(x -> value.set(true), () -> value.set(false));

        assertThat(value.orElseThrow())
            .isFalse();

        value.clear();

        Exceptional.of(42)
            .ifPresentOrElse(x -> value.set(true), () -> value.set(false));

        assertThat(value.orElseThrow())
            .isTrue();

        final Exceptional<?> e = Exceptional.of(42)
            .ifPresentOrElse(x -> {
                    throw new RuntimeException();
                },
                () -> {
                    throw new RuntimeException();
                });

        assertThat(e.isException())
            .isTrue();

        final var e2 = Exceptional.empty()
            .ifPresentOrElse(x -> {
                throw new RuntimeException();
            }, () -> {
                throw new RuntimeException();
            });

        assertThat(e2.isException())
            .isTrue();

        final var e3 = Exceptional.empty()
            .ifPresentOrElse(x -> {
                throw new RuntimeException();
            }, () -> {
                throw new RuntimeException();
            });

        assertThat(e3.isException())
            .isTrue();
    }

    /**
     * Verify {@link Exceptional#filter(Predicate)} by predicate.
     */
    @Test
    void shouldFilterByPredicate() {
        assertThat(Exceptional.of(42)
            .filter(n -> n == 42)
            .isPresent())
            .isTrue();

        assertThat(Exceptional.of(42)
            .filter(n -> n == 0)
            .isEmpty())
            .isTrue();

        assertThat(Exceptional.empty()
            .filter(n -> true)
            .isEmpty())
            .isTrue();

        assertThat(Exceptional.ofException(new Exception())
            .filter(n -> true)
            .isException())
            .isTrue();

        assertThat(Exceptional.of(42)
            .filter(n -> {
                throw new RuntimeException();
            })
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#map(Function)} by function.
     */
    @Test
    void shouldMapByFunction() {
        assertThat(Exceptional.empty()
            .map(n -> n)
            .isEmpty())
            .isTrue();

        assertThat(Exceptional.of(42)
            .map(n -> n - 1)
            .orElseThrow())
            .isEqualTo(41);

        assertThat(Exceptional.of(42)
            .map(n -> null)
            .isEmpty())
            .isTrue();

        assertThat(Exceptional.ofException(new Exception())
            .map(n -> n)
            .isException())
            .isTrue();

        assertThat(Exceptional.of(42)
            .map(n -> {
                throw new RuntimeException();
            }).isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#peek} is invoked.
     */
    @Test
    void shouldPeekByConsumer() {
        final var value = Capture.<Integer>empty();

        assertThat(Exceptional.of(42)
            .peek(value::set)
            .orElseThrow())
            .isEqualTo(42);

        assertThat(value.orElseThrow())
            .isEqualTo(42);

        final var set = Capture.<Boolean>empty();
        assertThat(Exceptional.empty()
            .peek(_ -> set.set(true))
            .isEmpty())
            .isTrue();

        assertThat(set.isEmpty())
            .isTrue();

        assertThat(Exceptional
            .ofException(new Exception())
            .peek(n -> set.set(true))
            .isException())
            .isTrue();

        assertThat(set.isEmpty())
            .isTrue();

        assertThat(Exceptional
            .of(42)
            .peek(n -> {
                throw new RuntimeException();
            })
            .isException())
            .isTrue();
    }

    /**
     * Ensure an {@link Exceptional} can be transformed into an equivalent future.
     */
    @Test
    void shouldTransformToFuture() {
        assertThat(Exceptional.of(42)
            .future()
            .join())
            .isEqualTo(42);

        assertThat(Exceptional.empty()
            .future()
            .join())
            .isNull();

        assertThat(Exceptional.ofException(new Exception())
            .future()
            .isCompletedExceptionally())
            .isTrue();
    }

    /**
     * Ensure that an {@link Exceptional#stream()} streams present values.
     */
    @Test
    void shouldStreamExceptional() {
        assertThat(Exceptional.of(42)
            .stream()
            .findFirst()
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional.empty()
            .stream())
            .isEmpty();

        assertThat(Exceptional.ofException(new Exception())
            .stream())
            .isEmpty();
    }

    /**
     * Ensure {@link Exceptional#orElseThrow} rethrows the exception if present, otherwise returns the value.
     */
    @Test
    void shouldOrElseThrow() {
        assertThat(Exceptional.of(42)
            .orElseThrow(IllegalStateException::new))
            .isEqualTo(42);

        assertThrows(IllegalStateException.class,
            () -> Exceptional.empty().orElseThrow(IllegalStateException::new));

        assertThrows(IllegalStateException.class,
            () -> Exceptional.ofException(new Exception()).orElseThrow(IllegalStateException::new));
    }

    /**
     * Ensure {@link Exceptional#orElseRethrow} rethrows the exception if present, otherwise returns the value.
     */
    @Test
    void shouldOrElseRethrow() {
        assertThat(Exceptional.of(42)
            .orElseRethrow(IllegalStateException::new))
            .isEqualTo(42);

        assertThrows(IllegalStateException.class,
            () -> Exceptional.empty().orElseRethrow(IllegalStateException::new));

        assertThrows(IllegalStateException.class,
            () -> Exceptional.ofException(new Exception()).orElseRethrow(IllegalStateException::new));

        final var e = new Exception();
        try {
            Exceptional.ofException(e)
                .orElseRethrow(IllegalStateException::new);

            fail();
        } catch (final IllegalStateException e2) {
            assertThat(e2.getCause())
                .isSameAs(e);
        }
    }

    /**
     * Ensure equality of {@link Exceptional}s.
     */
    @Test
    void shouldCompareWithEquality() {
        assertThat(Exceptional.empty())
            .isEqualTo(Exceptional.ofNullable(null));

        assertThat(Exceptional.of(42))
            .isEqualTo(Exceptional.of(42));

        final var e = new Exception();
        assertThat(Exceptional.ofException(e))
            .isEqualTo(Exceptional.ofException(e));

        assertThat(Exceptional.of(0))
            .isNotEqualTo(Exceptional.of(42));

        assertThat(Exceptional.empty())
            .isNotEqualTo(Exceptional.of(42));

        assertThat(Exceptional.empty())
            .isNotEqualTo(Exceptional.ofException(new Exception()));

        assertThat(Exceptional.of(42))
            .isNotEqualTo(Exceptional.ofException(new Exception()));
    }

    /**
     * Ensure {@link Exceptional}s can be created from {@link Optional}s.
     */
    @Test
    void shouldCreateExceptionalsFromOptionals() {
        assertThat(Exceptional
            .ofOptional(Optional.of(42))
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional
            .ofOptional(Optional.empty()))
            .isEmpty();

        assertThat(Exceptional
            .ofOptional(Optional.of(42))
            .orElseThrow())
            .isEqualTo(42);
    }

    /**
     * Ensure {@link Exceptional}s can be created from {@link CompletableFuture}s.
     */
    @Test
    void shouldCreateExceptionalsFromFutures() {
        assertThat(Exceptional
            .ofFuture(CompletableFuture.completedFuture(42))
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional
            .ofFuture(CompletableFuture.completedFuture(null))
            .isEmpty())
            .isTrue();

        assertThat(Exceptional
            .ofFuture(CompletableFutures.completeExceptionally(new Exception()))
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#call} produces {@link Exceptional}s.
     */
    @Test
    void shouldCallExceptionals()
        throws Exception {

        assertThat(Exceptional
            .call(() -> 42)
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional
            .call(() -> {
                throw new IllegalStateException();
            })
            .isException())
            .isTrue();

        assertThat(Exceptional
            .callable(() -> 42)
            .call()
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional
            .callable(() -> {
                throw new Exception();
            })
            .call()
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#run} produces {@link Exceptional}s.
     */
    @Test
    void shouldRunExceptionals() {
        assertThat(Exceptional
            .run(() -> {
            })
            .isEmpty())
            .isTrue();

        assertThat(Exceptional
            .run(() -> {
                throw new IllegalStateException();
            })
            .isException())
            .isTrue();

        assertThat(Exceptional
            .runnable(() -> {
            }).get()
            .isEmpty())
            .isTrue();

        assertThat(Exceptional
            .runnable(() -> {
                throw new RuntimeException();
            })
            .get()
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#consume} produces {@link Exceptional}s.
     */
    @Test
    void shouldConsumeExceptionals() {
        assertThat(Exceptional
            .consume(x -> {
            }, 42)
            .isEmpty())
            .isTrue();

        assertThat(Exceptional
            .consume(x -> {
                throw new IllegalStateException();
            }, 42)
            .isException())
            .isTrue();

        assertThat(Exceptional
            .consumer(n -> {
            })
            .apply(42)
            .isEmpty())
            .isTrue();

        assertThat(Exceptional
            .consumer(n -> {
                throw new RuntimeException();
            })
            .apply(42)
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#supply} produces {@link Exceptional}s.
     */
    @Test
    void shouldSupplyExceptionals() {
        assertThat(Exceptional
            .supply(() -> 42)
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional
            .supply(() -> {
                throw new IllegalStateException();
            })
            .isException())
            .isTrue();

        assertThat(Exceptional
            .supplier(() -> 42)
            .get()
            .orElseThrow())
            .isEqualTo(42);

        assertThat(Exceptional
            .supplier(() -> {
                throw new RuntimeException();
            })
            .get()
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#apply} produces {@link Exceptional}s.
     */
    @Test
    void shouldApplyExceptionals() {
        assertThat(Exceptional
            .apply(n -> n + 1, 42)
            .orElseThrow())
            .isEqualTo(43);

        assertThat(Exceptional
            .apply(n -> {
                throw new IllegalStateException();
            }, 42)
            .isException())
            .isTrue();

        assertThat(Exceptional
            .function((Integer n) -> n + 1)
            .apply(42)
            .orElseThrow())
            .isEqualTo(43);

        assertThat(Exceptional
            .function(n -> {
                throw new RuntimeException();
            })
            .apply(42)
            .isException())
            .isTrue();
    }

    /**
     * Ensure {@link Exceptional#orElseThrow()} includes the causing {@link Exception}.
     */
    @Test
    void shouldSeeCausingExceptionInExceptional() {
        try {
            Exceptional.ofException(new UnsupportedOperationException("Not supported mate"))
                .orElseThrow();

            fail("Expected to throw");
        } catch (final NoSuchElementException e) {
            assertThat(e.getCause().getMessage())
                .isEqualTo("Not supported mate");
        }
    }
}
