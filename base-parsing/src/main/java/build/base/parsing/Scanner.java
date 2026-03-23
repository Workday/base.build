package build.base.parsing;

/*-
 * #%L
 * base.build Parsing
 * %%
 * Copyright (C) 2025 Workday Inc
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

import build.base.foundation.stream.Streamable;
import build.base.io.LookaheadReader;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text scanner that can filter, match and break input into {@link String}s and specific types of values.
 * <p>
 * {@link Scanner}s may be configured with zero or more {@link Filter}s, that will be used to automatically filter out
 * and ignore text during the scanning process.
 * <p>
 * {@link Scanner}s may also be used and configured with {@link Evaluator}s, allowing matching and evaluation to return
 * specific types of values.
 * <p>
 * As {@link Scanner}s ultimately read input to scan from {@link Reader}s, scanning operations may block waiting for
 * input.  Should the underlying {@link Reader} throw an {@link java.io.IOException}, the {@link Scanner} will assume
 * the end of input has been reached.
 * <p>
 * When a {@link Scanner} throws a {@link ParseException} while matching, the {@link Scanner} will not consume the
 * failed match from the underlying {@link Reader}, thus allowing further attempts to either skip or consume from
 * the {@link Scanner} with some other pattern.
 * <p>
 * When a {@link Scanner} is closed, the underlying {@link Reader} will also be closed.
 *
 * @author brian.oliver
 * @see Filter#WHITESPACE
 * @see Filter#JAVA_SINGLE_LINE_COMMENT
 * @see Filter#JAVA_MULTILINE_COMMENT
 * @see Evaluator
 * @since Aug-2019
 */
public class Scanner
    implements AutoCloseable {

    /**
     * The {@link LookaheadReader} from which content will be read for parsing.
     */
    private final LookaheadReader reader;

    /**
     * The {@link Filter}s for skipping {@link LookaheadReader} content to ignore and not parse.
     */
    private final ArrayList<Filter> filters;

    /**
     * The {@link Evaluator}s for evaluating and converting content into specific {@link Class}es.
     */
    private final HashMap<Class<?>, Evaluator<?>> evaluators;

    /**
     * Constructs a {@link Scanner} using the specified {@link Reader}.
     *
     * @param reader a {@link Reader}
     */
    public Scanner(final Reader reader) {
        Objects.requireNonNull(reader, "The Reader must not be null");

        this.reader = reader instanceof LookaheadReader ? (LookaheadReader) reader : new LookaheadReader(reader);
        this.filters = new ArrayList<>();
        this.evaluators = new HashMap<>();
    }

    /**
     * Constructs a {@link Scanner} for the specified {@link String}.
     *
     * @param string the {@link String}
     */
    public Scanner(final String string) {
        this(new StringReader(string));
    }

    @Override
    public void close()
        throws Exception {
        this.reader.close();
    }

    /**
     * Registers the specified {@link Filter} to be used for filtering content while being scanned.
     *
     * @param filter the {@link Filter}
     * @return the {@link Scanner} to permit fluent-style method calls
     */
    public Scanner register(final Filter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
        return this;
    }

    /**
     * Registers an {@link Evaluator} for a specific {@link Class} of value.
     *
     * @param <T>        the type of value
     * @param valueClass the {@link Class} of value
     * @param evaluator  the {@link Evaluator}
     * @return the {@link Scanner} to permit fluent-style method calls
     */
    public <T> Scanner register(final Class<T> valueClass, final Evaluator<T> evaluator) {
        if (valueClass != null && evaluator != null) {
            this.evaluators.put(valueClass, evaluator);
        }
        return this;
    }

    /**
     * Obtains the current {@link LookaheadReader.Location} for the {@link Scanner}.
     *
     * @return the {@link LookaheadReader.Location}
     */
    public LookaheadReader.Location getLocation() {
        return this.reader.getLocation();
    }

    /**
     * Normalizes a {@link Pattern} for matching with the {@link String}.
     * <p>
     * By default, all {@link Pattern}s are designed to match anywhere in a {@link String}, unless they are explicitly
     * bounded, say to commence matching at the start (^) or end ($) of a {@link String}.  When using a {@link Pattern}
     * for parsing, we need to take this into account, namely if a specified {@link Pattern} is bounded to commence
     * matching at the start of a {@link String}, the current position in the {@link String} being parsed
     * <strong>must be</strong> at the start of the said {@link String}.  Similarly, if the {@link Pattern} is not
     * bound to commence matching at the start of a {@link String}, we must automatically include the bound to
     * commence matching at the start of a {@link String}, because we must match from the <i>start of the current
     * position</i> in the {@link String}.
     * <p>
     * This method performs this transformation, if necessary, to return a {@link Pattern} that is suitable for
     * matching at the current position in the {@link String}.  Should it not be possible to produce a
     * normalized {@link Pattern} or it is determined that the provided {@link Pattern} would never match, an
     * {@link Optional#empty()} is returned.
     *
     * @param pattern the {@link Pattern}
     * @return the {@link Optional}ly normalized {@link Pattern}
     */
    private Optional<Pattern> normalize(final Pattern pattern) {
        if (pattern == null) {
            return Optional.empty();
        }

        // normalize the pattern
        final String regex = pattern.pattern();

        if (regex.startsWith("^") && this.reader.getLocation().getColumn() != 1) {
            return Optional.empty();
        }

        final Pattern normalized = regex.startsWith("^")
            ? pattern
            : Pattern.compile("^" + regex, pattern.flags());

        return Optional.of(normalized);
    }

    /**
     * Attempts to match the {@link Pattern} with the current content in the {@link String} to parse.
     *
     * @param pattern the {@link Pattern}
     * @return the {@link Optional}ly matched {@link String} or {@link Optional#empty()} if the match was unsuccessful
     */
    private Optional<String> match(final Pattern pattern) {
        if (pattern != null && this.reader.available()) {
            final Optional<Pattern> normalized = normalize(pattern);

            if (normalized.isPresent()) {
                final Matcher matcher = normalized.get().matcher(this.reader.peekMaximum());

                if (matcher.find()) {
                    // obtain the match
                    final String match = matcher.group();

                    // skip the matched length
                    this.reader.consume(match.length());

                    return Optional.of(match);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Attempts to match the specified {@link String} with the current content in the {@link String} to parse.
     * <p>
     * Matching is performed using {@link String#equals(Object)}, which means matching is case-sensitive.
     *
     * @param string the {@link String} to match
     * @return the {@link Optional}ly matched {@link String} or {@link Optional#empty()} if the match was unsuccessful
     */
    private Optional<String> match(final String string) {
        if (string != null && !string.isEmpty() && this.reader.available()) {
            if (this.reader.peek(string.length()).equals(string)) {

                // move the column past the match
                this.reader.consume(string.length());

                return Optional.of(string);
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to ensure content is available for parsing, including filtering any content according to the
     * defined {@link Filter}s.
     *
     * @return {@code true} if there is content available for parsing,
     * {@code false} when there is no more content available
     */
    private boolean available() {
        if (!this.filters.isEmpty()) {
            int index = 0;

            while (this.reader.available() && index < this.filters.size()) {

                final Filter filter = this.filters.get(index);
                final LookaheadReader.Location location = this.reader.getLocation();

                filter.accept(this.reader);

                // did the filter consume any content?
                if (location.equals(this.reader.getLocation())) {
                    // the location wasn't changed, so we can proceed to the next filter
                    index++;
                } else {
                    // the location did change, so we have to restart filtering
                    index = 0;
                }
            }
        }

        return this.reader.available();
    }

    /**
     * Skips the specified regular expression {@link Pattern}.  Should the {@link Pattern} not match,
     * nothing happens.
     *
     * @param pattern the regular expression {@link Pattern}
     */
    public void skip(final Pattern pattern) {
        optionallyConsume(pattern);
    }

    /**
     * Skips the specified {@link String}.  Should the {@link String} not match, nothing happens.
     * <p>
     * Matching is performed using {@link String#equals(Object)}, which means matching is case-sensitive.
     *
     * @param string the {@link String} to match
     */
    public void skip(final String string) {
        optionallyConsume(string);
    }

    /**
     * Skips the value matched by the specified {@link Evaluator}.  Should the {@link Evaluator} not match,
     * nothing happens.
     *
     * @param token the {@link Evaluator}
     */
    public void skip(final Evaluator<?> token) {
        optionallyConsume(token);
    }

    /**
     * Skip the specified {@link Class} of value using a known (registered) {@link Evaluator}.  Should the
     * {@link Class} be unknown or the {@link Evaluator} not match, nothing happens.
     *
     * @param valueClass the {@link Class} of value
     * @see #register(Class, Evaluator)
     */
    public void skip(final Class<?> valueClass) {
        final Evaluator<?> evaluator = this.evaluators.get(valueClass);

        if (evaluator != null) {
            optionallyConsume(evaluator);
        }
    }

    /**
     * Consumes the specified number of characters, returned that which was consumed
     *
     * @param count the number of characters to skip
     * @return the consumed characters
     * @throws ParseException when the specified number of characters could not be consumed
     */
    public String consume(final int count)
        throws ParseException {
        final String consumed = this.reader.consume(count);

        if (consumed.length() != count) {
            throw new ParseException(getLocation(),
                "Failed to consume " + count + " character(s)",
                consumed);
        }

        return consumed;
    }

    /**
     * Consumes the specified regular expression {@link Pattern}, returning that which was consumed.
     *
     * @param pattern the regular expression {@link Pattern}
     * @return the matched {@link String}
     * @throws ParseException when the {@link Pattern} does not match, or the {@link Pattern} was {@code null}
     */
    public String consume(final Pattern pattern)
        throws ParseException {

        if (available()) {
            // attempt to match the provided pattern
            final Optional<String> match = match(pattern);

            return match.orElseThrow(
                () -> new ParseException(this.reader.getLocation(), pattern.toString(), this.reader.peekMaximum()));
        } else {
            throw new ParseException(this.reader.getLocation(), pattern.toString(), "(end of input)");
        }
    }

    /**
     * Consumes the specified {@link String}, returning that which was consumed.
     *
     * @param string the {@link String} to match
     * @return the matched {@link String}
     * @throws ParseException when the {@link String} does not match, or the {@link String} was {@code null}
     */
    public String consume(final String string)
        throws ParseException {

        if (available()) {
            // attempt to match the provided pattern
            final Optional<String> match = match(string);

            return match.orElseThrow(
                () -> new ParseException(this.reader.getLocation(), string, this.reader.peekMaximum()));
        } else {
            throw new ParseException(this.reader.getLocation(), string, "(end of input)");
        }
    }

    /**
     * Consumes a specific type of value defined by an {@link Evaluator}.
     *
     * @param <T>       the type of value
     * @param evaluator the {@link Evaluator}
     * @return the value
     * @throws ParseException when the {@link Evaluator} can not produce a value, or the {@link Evaluator} was
     *                        {@code null}
     */
    public <T> T consume(final Evaluator<T> evaluator)
        throws ParseException {

        if (evaluator == null) {
            throw new ParseException(getLocation(), "The specified Evaluator was null", "A non-null Evaluator");
        }

        if (available()) {
            // attempt to match the provided pattern
            if (evaluator.test(this)) {
                try {
                    return evaluator.apply(this);
                } catch (final ParseException e) {
                    throw new ParseException(getLocation(), evaluator.getDescription(), this.reader.peekMaximum());
                } catch (final Exception e) {
                    throw new ParseException(
                        getLocation(),
                        "Failed to evaluate and convert the character sequence into the required type of value",
                        evaluator.getDescription(),
                        e);
                }
            } else {
                throw new ParseException(
                    this.reader.getLocation(),
                    evaluator.getDescription(),
                    this.reader.peekMaximum());
            }
        } else {
            throw new ParseException(this.reader.getLocation(), evaluator.getDescription(), "(end of input)");
        }
    }

    /**
     * Consumes a specific type of value known (registered) with the {@link Scanner}.
     *
     * @param <T>        the type of value
     * @param valueClass the {@link Class} of value
     * @return the value
     * @throws ParseException when the {@link Class} of value can not be matched, or the {@link Class} was
     *                        {@code null}
     * @see #register(Class, Evaluator)
     */
    @SuppressWarnings("unchecked")
    public <T> T consume(final Class<T> valueClass) {
        final Evaluator<T> evaluator = (Evaluator<T>) this.evaluators.get(valueClass);

        if (evaluator != null) {
            return consume(evaluator);
        }

        throw new ParseException(
            this.reader.getLocation(),
            "Unable to determine Evaluator for " + valueClass,
            this.reader.peekMaximum());
    }

    /**
     * Determines if the specified regular expression {@link Pattern} immediately follows (occurs next).
     *
     * @param pattern the regular expression {@link Pattern}
     * @return {@code true} if the {@link Pattern} matches, {@code false} otherwise
     */
    public boolean follows(final Pattern pattern) {
        final Optional<Pattern> normalized = normalize(pattern);

        return normalized.isPresent() && available() && normalized.get().matcher(this.reader.peekMaximum()).find();
    }

    /**
     * Determines if the specified {@link String} immediately follows (occurs next).
     * <p>
     * Matching is performed using {@link String#equals(Object)}, which means matching is case-sensitive.
     *
     * @param string the {@link String} to match
     * @return {@code true} if the pattern matches, {@code false} otherwise
     */
    public boolean follows(final String string) {
        return string != null && available() && this.reader.follows(string);
    }

    /**
     * Determines if the type of value defined by an {@link Evaluator} immediately follows (occurs next).
     *
     * @param evaluator the {@link Evaluator}
     * @return {@code true} if the {@link Evaluator} matches, {@code false} otherwise
     */
    public boolean follows(final Evaluator<?> evaluator) {
        return evaluator != null && evaluator.test(this);
    }

    /**
     * Determines if the type of value known (registered) with the {@link Scanner} immediately follows (occurs next).
     *
     * @param valueClass the {@link Class} of value
     * @return {@code true} if the {@link Class} matches, {@code false} otherwise
     * @see #register(Class, Evaluator)
     */
    public boolean follows(final Class<?> valueClass) {
        final Evaluator<?> evaluator = this.evaluators.get(valueClass);
        return evaluator != null && evaluator.test(this);
    }

    /**
     * Attempts to {@link Optional}ly consume the specified regular expression {@link Pattern},
     * returning that which was consumed as an {@link Optional} or {@link Optional#empty()} if the {@link Pattern}
     * did not immediately follow.
     *
     * @param pattern the regular expression {@link Pattern}
     * @return the {@link Optional}ly matched {@link String}
     */
    public Optional<String> optionallyConsume(final Pattern pattern) {
        return pattern != null && follows(pattern) ? Optional.of(consume(pattern)) : Optional.empty();
    }

    /**
     * Attempts to {@link Optional}ly consume the specified {@link String}, returning that which was consumed as
     * an {@link Optional} or {@link Optional#empty()} if the {@link String} did not immediately follow.
     *
     * @param string the {@link String} to match
     * @return the {@link Optional}ly matched {@link String}
     */
    public Optional<String> optionallyConsume(final String string) {
        return string != null && follows(string) ? Optional.of(consume(string)) : Optional.empty();
    }

    /**
     * Attempts to {@link Optional}ly consume the type of value defined by an {@link Evaluator}.
     *
     * @param <T>       the type of value
     * @param evaluator the {@link Evaluator}
     * @return the {@link Optional}ly matched value
     */
    public <T> Optional<T> optionallyConsume(final Evaluator<T> evaluator) {
        return evaluator != null && follows(evaluator) ? Optional.of(consume(evaluator)) : Optional.empty();
    }

    /**
     * Attempts to {@link Optional}ly consume the type of value known (registered) with the {@link Scanner}.
     *
     * @param <T>        the type of value
     * @param valueClass the {@link Class} of value
     * @return the {@link Optional}ly matched value
     * @see #register(Class, Evaluator)
     */
    public <T> Optional<T> optionallyConsume(final Class<T> valueClass) {
        return follows(valueClass) ? Optional.of(consume(valueClass)) : Optional.empty();
    }

    /**
     * Skips characters one at a time until the specified regular expression {@link Pattern} matches,
     * leaving the matching content unconsumed.  Should the end of input be reached before the {@link Pattern}
     * matches, all remaining content is skipped.
     *
     * @param pattern the regular expression {@link Pattern}
     */
    public void skipUntil(final Pattern pattern) {
        while (available() && !follows(pattern)) {
            this.reader.consume();
        }
    }

    /**
     * Skips characters one at a time until the specified {@link String} matches,
     * leaving the matching content unconsumed.  Should the end of input be reached before the {@link String}
     * matches, all remaining content is skipped.
     * <p>
     * Matching is performed using {@link String#equals(Object)}, which means matching is case-sensitive.
     *
     * @param string the {@link String} to match
     */
    public void skipUntil(final String string) {
        while (available() && !follows(string)) {
            this.reader.consume();
        }
    }

    /**
     * Skips characters one at a time until the type of value defined by the specified {@link Evaluator} matches,
     * leaving the matching content unconsumed.  Should the end of input be reached before the {@link Evaluator}
     * matches, all remaining content is skipped.
     *
     * @param evaluator the {@link Evaluator}
     */
    public void skipUntil(final Evaluator<?> evaluator) {
        while (available() && !follows(evaluator)) {
            this.reader.consume();
        }
    }

    /**
     * Skips characters one at a time until the type of value known (registered) with the {@link Scanner} matches,
     * leaving the matching content unconsumed.  Should the end of input be reached before the type matches,
     * all remaining content is skipped.
     *
     * @param valueClass the {@link Class} of value
     * @see #register(Class, Evaluator)
     */
    public void skipUntil(final Class<?> valueClass) {
        while (available() && !follows(valueClass)) {
            this.reader.consume();
        }
    }

    /**
     * Repeatedly skips the specified regular expression {@link Pattern} while it matches at the current position.
     * Should the {@link Pattern} not match, nothing happens.
     *
     * @param pattern the regular expression {@link Pattern}
     */
    public void skipWhile(final Pattern pattern) {
        while (follows(pattern)) {
            skip(pattern);
        }
    }

    /**
     * Repeatedly skips the specified {@link String} while it matches at the current position.
     * Should the {@link String} not match, nothing happens.
     * <p>
     * Matching is performed using {@link String#equals(Object)}, which means matching is case-sensitive.
     *
     * @param string the {@link String} to match
     */
    public void skipWhile(final String string) {
        while (follows(string)) {
            skip(string);
        }
    }

    /**
     * Repeatedly skips the type of value defined by the specified {@link Evaluator} while it matches at
     * the current position.  Should the {@link Evaluator} not match, nothing happens.
     *
     * @param evaluator the {@link Evaluator}
     */
    public void skipWhile(final Evaluator<?> evaluator) {
        while (follows(evaluator)) {
            skip(evaluator);
        }
    }

    /**
     * Repeatedly skips the type of value known (registered) with the {@link Scanner} while it matches at
     * the current position.  Should the type not match, nothing happens.
     *
     * @param valueClass the {@link Class} of value
     * @see #register(Class, Evaluator)
     */
    public void skipWhile(final Class<?> valueClass) {
        while (follows(valueClass)) {
            skip(valueClass);
        }
    }

    /**
     * Consumes characters one at a time until the specified regular expression {@link Pattern} matches,
     * returning all consumed characters.  The matching content is left unconsumed.
     *
     * @param pattern the regular expression {@link Pattern}
     * @return the consumed characters as a {@link String}
     */
    public String consumeUntil(final Pattern pattern) {
        final var builder = new StringBuilder();

        while (available() && !follows(pattern)) {
            builder.append((char) this.reader.consume());
        }

        return builder.toString();
    }

    /**
     * Consumes characters one at a time until the specified {@link String} matches,
     * returning all consumed characters.  The matching content is left unconsumed.
     * <p>
     * Matching is performed using {@link String#equals(Object)}, which means matching is case-sensitive.
     *
     * @param string the {@link String} to match
     * @return the consumed characters as a {@link String}
     */
    public String consumeUntil(final String string) {
        final var builder = new StringBuilder();

        while (available() && !follows(string)) {
            builder.append((char) this.reader.consume());
        }

        return builder.toString();
    }

    /**
     * Consumes characters one at a time until the type of value defined by the specified {@link Evaluator} matches,
     * returning all consumed characters.  The matching content is left unconsumed.
     *
     * @param evaluator the {@link Evaluator}
     * @return the consumed characters as a {@link String}
     */
    public String consumeUntil(final Evaluator<?> evaluator) {
        final var builder = new StringBuilder();

        while (available() && !follows(evaluator)) {
            builder.append((char) this.reader.consume());
        }

        return builder.toString();
    }

    /**
     * Consumes characters one at a time until the type of value known (registered) with the {@link Scanner} matches,
     * returning all consumed characters.  The matching content is left unconsumed.
     *
     * @param valueClass the {@link Class} of value
     * @return the consumed characters as a {@link String}
     * @see #register(Class, Evaluator)
     */
    public String consumeUntil(final Class<?> valueClass) {
        final var builder = new StringBuilder();

        while (available() && !follows(valueClass)) {
            builder.append((char) this.reader.consume());
        }

        return builder.toString();
    }

    /**
     * Repeatedly consumes the specified regular expression {@link Pattern} while it matches at the current position,
     * returning all consumed characters.
     *
     * @param pattern the regular expression {@link Pattern}
     * @return the consumed characters as a {@link String}
     */
    public String consumeWhile(final Pattern pattern) {
        final var builder = new StringBuilder();

        while (follows(pattern)) {
            builder.append(consume(pattern));
        }

        return builder.toString();
    }

    /**
     * Repeatedly consumes the type of value defined by the specified {@link Evaluator} while it matches at
     * the current position, returning all consumed values as a {@link Streamable}.
     *
     * @param <T>       the type of value
     * @param evaluator the {@link Evaluator}
     * @return a {@link Streamable} of consumed values
     */
    public <T> Streamable<T> consumeWhile(final Evaluator<T> evaluator) {
        final var values = new ArrayList<T>();

        while (follows(evaluator)) {
            values.add(consume(evaluator));
        }

        return Streamable.of(values);
    }

    /**
     * Repeatedly consumes the type of value known (registered) with the {@link Scanner} while it matches at
     * the current position, returning all consumed values as a {@link Streamable}.
     *
     * @param <T>        the type of value
     * @param valueClass the {@link Class} of value
     * @return a {@link Streamable} of consumed values
     * @see #register(Class, Evaluator)
     */
    public <T> Streamable<T> consumeWhile(final Class<T> valueClass) {
        final var values = new ArrayList<T>();

        while (follows(valueClass)) {
            values.add(consume(valueClass));
        }

        return Streamable.of(values);
    }

    /**
     * Determines if there is further context to parse.
     *
     * @return {@code true} if there is further content, {@code false} otherwise
     */
    public boolean hasNext() {
        return available();
    }
}
