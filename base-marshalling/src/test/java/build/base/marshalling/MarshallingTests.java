package build.base.marshalling;

import build.base.foundation.Introspection;
import build.base.marshalling.example.Expense;
import build.base.marshalling.example.ExpenseLine;
import build.base.marshalling.example.Person;
import build.base.marshalling.example.Point;
import build.base.marshalling.example.PointWithMarshaller;
import build.base.marshalling.example.StaticallyRegisteredPoint;
import build.base.marshalling.example.Wrapper;
import build.base.marshalling.tutorial.Address;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Marshalling}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class MarshallingTests {

    /**
     * Ensure platform types are not directly marshallable.
     */
    @Test
    void shouldNotBeMarshallable() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        assertThat(schemaFactory.isMarshallable(String.class))
            .isFalse();

        assertThat(schemaFactory.isMarshallable(Integer.class))
            .isFalse();
    }

    /**
     * Ensure a marshallable {@link Class} can be statically registered.
     */
    @Test
    void shouldStaticallyRegisterMarshallableClass() {

        final var schemaFactory = Marshalling.globalSchemaFactory();

        final var marshallingSchema = schemaFactory
            .getMarshallingSchema(StaticallyRegisteredPoint.class)
            .orElseThrow();

        assertThat(schemaFactory.isMarshallable(StaticallyRegisteredPoint.class))
            .isTrue();

        assertThat(marshallingSchema)
            .isNotNull();

        assertThat(marshallingSchema.owner())
            .isEqualTo(StaticallyRegisteredPoint.class);

        assertThat(marshallingSchema.parameters().count())
            .isEqualTo(2);

        assertThat(marshallingSchema.parameters()
            .stream()
            .map(Parameter::name))
            .containsExactly("x", "y");

        assertThat(marshallingSchema.parameters()
            .stream()
            .map(Parameter::type))
            .allMatch(Integer.class::equals);

        assertThat(schemaFactory.getUnmarshallingSchemas(StaticallyRegisteredPoint.class))
            .hasSize(1);

        final var unmashallingSchema = schemaFactory.getUnmarshallingSchemas(StaticallyRegisteredPoint.class)
            .findFirst()
            .orElseThrow();

        assertThat(unmashallingSchema.owner())
            .isEqualTo(StaticallyRegisteredPoint.class);

        assertThat(unmashallingSchema.parameters().count())
            .isEqualTo(2);

        assertThat(unmashallingSchema.parameters()
            .stream()
            .map(Parameter::name))
            .containsExactly("x", "y");

        assertThat(unmashallingSchema.parameters()
            .stream()
            .map(Parameter::type)
            .map(type -> Introspection.getClassFromType(type).orElse(Object.class)))
            .map(Introspection::getBoxedClass)
            .allMatch(Integer.class::equals);
    }

    /**
     * Ensure a marshallable {@link Class} can be dynamically registered.
     */
    @Test
    void shouldDynamicallyRegisterMarshallableClass() {

        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Point.class);

        final var marshallingSchema = schemaFactory
            .getMarshallingSchema(Point.class)
            .orElseThrow();

        assertThat(marshallingSchema)
            .isNotNull();

        assertThat(marshallingSchema.owner())
            .isEqualTo(Point.class);

        assertThat(marshallingSchema.parameters().count())
            .isEqualTo(2);

        assertThat(marshallingSchema.parameters()
            .stream()
            .map(Parameter::name))
            .containsExactly("x", "y");

        assertThat(marshallingSchema.parameters()
            .stream()
            .map(Parameter::type))
            .allMatch(Integer.class::equals);

        assertThat(schemaFactory.getUnmarshallingSchemas(Point.class))
            .hasSize(1);

        final var unmashallingSchema = schemaFactory.getUnmarshallingSchemas(Point.class)
            .findFirst()
            .orElseThrow();

        assertThat(unmashallingSchema.owner())
            .isEqualTo(Point.class);

        assertThat(unmashallingSchema.parameters().count())
            .isEqualTo(2);

        assertThat(unmashallingSchema.parameters()
            .stream()
            .map(Parameter::name))
            .containsExactly("x", "y");

        assertThat(unmashallingSchema.parameters()
            .stream()
            .map(Parameter::type)
            .map(type -> Introspection.getClassFromType(type).orElse(Object.class)))
            .map(Introspection::getBoxedClass)
            .allMatch(Integer.class::equals);
    }

    /**
     * Ensure a {@link Schema} for an unmarshallable {@link Class} can't be obtained.
     */
    @Test
    void shouldNotObtainSchemaForUnmarshallableClass() {
        assertThat(Marshalling.globalSchemaFactory()
            .getMarshallingSchema(String.class))
            .isEmpty();
    }

    /**
     * Ensure a marshallable {@link Object} can be marshalled.
     */
    @Test
    void shouldMarshal() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Point.class);

        final var point = new Point(42, 24);

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(point);

        assertThat(marshalled)
            .isNotNull();

        assertThat(marshalled.schema().owner())
            .isEqualTo(Point.class);

        assertThat(marshalled.values().count())
            .isEqualTo(2);

        assertThat(marshalled.values().stream())
            .containsExactly(42, 24);
    }

    /**
     * Ensure a marshallable {@link Object} can be unmarshalled.
     */
    @Test
    void shouldUnmarshal() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Point.class);

        final var point = new Point(42, 24);

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(point);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(point);

        assertThat(unmarshalled)
            .isEqualTo(point);
    }

    /**
     * Ensure a marshallable {@link Object} can be unmarshalled.
     */
    @Test
    void shouldUnmarshalWithMarshaller() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(PointWithMarshaller.class);

        final var point = new PointWithMarshaller(42, 24);

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(point);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(point);

        assertThat(unmarshalled)
            .isEqualTo(point);
    }

    /**
     * Ensure a marshallable {@link Object} can be marshalled and unmarshalled with {@code null} values.
     */
    @Test
    void shouldMarshallAndUnmarshalNullValues() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Wrapper.class);

        final var wrapper = new Wrapper<>(null);

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(wrapper);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(wrapper);

        assertThat(unmarshalled)
            .isEqualTo(wrapper);
    }

    /**
     * Ensure the composed marshallable {@link Object} of another (outer) marshallable {@link Object} is not
     * automatically marshalled and unmarshalled.
     */
    @Test
    void shouldNotAutomaticallyMarshallAndUnmarshalComposedMarshables() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Wrapper.class);

        final var wrapper = new Wrapper<>(new Wrapper<>(42));

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(wrapper);

        assertThat(marshalled)
            .isNotNull();

        assertThat(marshalled.values().stream()
            .findFirst()
            .orElseThrow())
            .isInstanceOf(Wrapper.class);

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(wrapper);

        assertThat(unmarshalled)
            .isEqualTo(wrapper);
    }

    /**
     * Ensure a marshallable {@link Object} composed of another marshallable {@link Object}, that explicitly uses
     * {@link Marshalled} types, can be marshalled and unmarshalled.
     */
    @Test
    void shouldSupportExplicitlyUsingMarshalledsForMarshallingAndUnmarshalling() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Person.class);
        schemaFactory.register(Address.class);

        final var person = new Person(new Address("Bourbon St", "New Orleans"));

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        assertThat(marshalled.values().stream().findFirst().orElseThrow())
            .isInstanceOf(Marshalled.class);

        final var unmarshalled = marshaller.unmarshal(marshalled);

        // ensure the Person is not the same instance
        assertThat(unmarshalled)
            .isNotSameAs(person);

        // ensure the Person instances are equal
        assertThat(unmarshalled)
            .isEqualTo(person);

        // ensure the Address part of the Person is not the same instance
        assertThat(unmarshalled.address())
            .isNotSameAs(person.address());
    }

    @Test
    void shouldSupportBoundDependencies() {
        final var schemaFactory = Marshalling.globalSchemaFactory();

        schemaFactory.register(Expense.class);
        schemaFactory.register(ExpenseLine.class);

        final var expense = new Expense();
        expense.add(new ExpenseLine(expense, "Food", 42.0));
        expense.add(new ExpenseLine(expense, "Water", 1.50));

        final var marshaller = schemaFactory.newMarshaller();

        final var marshalled = marshaller.marshal(expense);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(expense);
    }
}

