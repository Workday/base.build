package build.base.foundation;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Preconditions}.
 *
 * @author jason.howes
 * @since Feb-2019
 */
class PreconditionsTests {

    /**
     * A test error message that contains a boolean.
     */
    private final static String ERROR_MESSAGE_BOOLEAN = "Validation error. Value is true.";

    /**
     * A test error message that contains a floating point number.
     */
    private final static String ERROR_MESSAGE_FLOAT = "Validation error. Value is 0.0.";

    /**
     * A test error message that contains an integer.
     */
    private final static String ERROR_MESSAGE_INTEGER = "Validation error. Value is 0.";

    /**
     * A test error message that contains an object.
     */
    private final static String ERROR_MESSAGE_OBJECT = "Validation error. Value is Object.";

    /**
     * A test error message template.
     */
    private final static String ERROR_TEMPLATE = "Validation error. Value is %1$s.";

    /**
     * A test object.
     */
    private static final Object OBJECT = "Object";

    // verify(final T argument, final boolean check) tests

    @Test
    void shouldVerify() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true));

        // negative verification
        try {
            Preconditions.require(OBJECT, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyInt() {

        // positive verification
        assertEquals(1, Preconditions.require(1, true));

        // negative verification
        try {
            Preconditions.require(1, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyLong() {

        // positive verification
        assertEquals(1L, Preconditions.require(1L, true));

        // negative verification
        try {
            Preconditions.require(1L, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyFloat() {

        // positive verification
        assertEquals(1.0f, Preconditions.require(1.0f, true), 0.001f);

        // negative verification
        try {
            Preconditions.require(1.0f, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyDouble() {

        // positive verification
        assertEquals(1.0d, Preconditions.require(1.0d, true), 0.001d);

        // negative verification
        try {
            Preconditions.require(1.0d, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyByte() {

        // positive verification
        assertEquals((byte) 1, Preconditions.require((byte) 1, true));

        // negative verification
        try {
            Preconditions.require((byte) 1, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyShort() {

        // positive verification
        assertEquals((short) 1, Preconditions.require((short) 1, true));

        // negative verification
        try {
            Preconditions.require((short) 1, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyChar() {

        // positive verification
        assertEquals('a', Preconditions.require('a', true));

        // negative verification
        try {
            Preconditions.require('a', false);
        }

        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyBoolean() {

        // positive verification
        assertTrue(Preconditions.require(true, true));

        // negative verification
        try {
            Preconditions.require(true, false);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithMessage() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_MESSAGE_OBJECT));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_MESSAGE_OBJECT);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_OBJECT);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndObject() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, OBJECT));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, OBJECT);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_OBJECT);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndBoolean() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, true));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, true);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_BOOLEAN);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndChar() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, '0'));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, '0');
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndShort() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, (short) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, (short) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndInt() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndLong() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, (long) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, (long) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndFloat() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, (float) 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, (float) 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_FLOAT);
        }
    }

    @Test
    void shouldVerifyWithTemplateAndDouble() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, ERROR_TEMPLATE, 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, ERROR_TEMPLATE, 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_FLOAT);
        }
    }

    @Test
    void shouldVerifyWithNullMessage() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndObject() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, OBJECT));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, OBJECT);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndBoolean() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, true));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, true);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndChar() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, '0'));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, '0');
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndShort() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, (short) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, (short) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndInt() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndLong() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, (long) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, (long) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndFloat() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, (float) 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, (float) 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyWithNullTemplateAndDouble() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, true, null, 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, false, null, 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    // verify(final T argument, final Predicate<? super T> predicate) tests

    @Test
    void shouldVerifyPredicate() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o)));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o));
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithMessage() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_MESSAGE_OBJECT));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_MESSAGE_OBJECT);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_OBJECT);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndObject() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, OBJECT));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, OBJECT);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_OBJECT);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndBoolean() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, true));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, true);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_BOOLEAN);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndChar() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, '0'));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, '0');
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndShort() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, (short) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, (short) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndInt() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndLong() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, (long) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, (long) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_INTEGER);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndFloat() {

        // positive verification
        assertEquals(OBJECT,
            Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, (float) 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, (float) 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_FLOAT);
        }
    }

    @Test
    void shouldVerifyPredicateWithTemplateAndDouble() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), ERROR_TEMPLATE, 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), ERROR_TEMPLATE, 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageEquals(e, ERROR_MESSAGE_FLOAT);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullMessage() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndObject() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, OBJECT));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, OBJECT);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndBoolean() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, true));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, true);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndChar() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, '0'));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, '0');
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndShort() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, (short) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, (short) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndInt() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndLong() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, (long) 0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, (long) 0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndFloat() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, (float) 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, (float) 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyPredicateWithNullTemplateAndDouble() {

        // positive verification
        assertEquals(OBJECT, Preconditions.require(OBJECT, o -> Objects.equals(OBJECT, o), null, 0.0));

        // negative verification
        try {
            Preconditions.require(OBJECT, o -> !Objects.equals(OBJECT, o), null, 0.0);
        }
        catch (final IllegalArgumentException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicate() {
        try {
            Preconditions.require(OBJECT, null);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithMessage() {
        try {
            Preconditions.require(OBJECT, null, ERROR_MESSAGE_OBJECT);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndObject() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, OBJECT);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndBoolean() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, true);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndChar() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, '0');
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndShort() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, (short) 0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndInt() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, 0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndLong() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, (long) 0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndFloat() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, (float) 0.0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithTemplateAndDouble() {
        try {
            Preconditions.require(OBJECT, null, ERROR_TEMPLATE, 0.0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullMessage() {
        try {
            Preconditions.require(OBJECT, null, null);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndObject() {
        try {
            Preconditions.require(OBJECT, null, null, OBJECT);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndBoolean() {
        try {
            Preconditions.require(OBJECT, null, null, true);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndChar() {
        try {
            Preconditions.require(OBJECT, null, null, '0');
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndShort() {
        try {
            Preconditions.require(OBJECT, null, null, (short) 0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndInt() {
        try {
            Preconditions.require(OBJECT, null, null, 0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndLong() {
        try {
            Preconditions.require(OBJECT, null, null, (long) 0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndFloat() {
        try {
            Preconditions.require(OBJECT, null, null, (float) 0.0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    @Test
    void shouldVerifyNullPredicateWithNullTemplateAndDouble() {
        try {
            Preconditions.require(OBJECT, null, null, 0.0);
        }
        catch (final NullPointerException e) {
            assertErrorMessageNotEmpty(e);
        }
    }

    /**
     * Assert that the message of the given exception is equal to the supplied message.
     *
     * @param e       the exception to check
     * @param message the expected message
     */
    protected void assertErrorMessageEquals(final Exception e, final String message) {
        if (!Objects.equals(e.getMessage(), message)) {
            throw new AssertionError("Mismatched error messages: " + e.getMessage() + " != " + message);
        }
    }

    /**
     * Assert that the message of the given exception is not empty.
     *
     * @param e the exception to check
     */
    protected void assertErrorMessageNotEmpty(final Exception e) {
        if (Strings.isEmpty(e.getMessage())) {
            throw new AssertionError("Empty exception message");
        }
    }
}
