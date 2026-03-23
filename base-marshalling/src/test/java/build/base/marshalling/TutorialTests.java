package build.base.marshalling;

import build.base.marshalling.tutorial.Address;
import build.base.marshalling.tutorial.AggregateExpense;
import build.base.marshalling.tutorial.AggregateExpenseLine;
import build.base.marshalling.tutorial.CityPredicate;
import build.base.marshalling.tutorial.Color;
import build.base.marshalling.tutorial.Expense;
import build.base.marshalling.tutorial.ExpenseLine;
import build.base.marshalling.tutorial.Person;
import build.base.marshalling.tutorial.PersonRecord;
import build.base.marshalling.tutorial.PersonWithAddress;
import build.base.marshalling.tutorial.PersonWithFavoriteColor;
import build.base.marshalling.tutorial.PersonWithOptionalMiddleName;
import build.base.marshalling.tutorial.PersonWithPostalAddress;
import build.base.marshalling.tutorial.PersonWithValidatedAddress;
import build.base.marshalling.tutorial.ResidentialAddress;
import build.base.marshalling.tutorial.SelfRegisteredPerson;
import build.base.marshalling.tutorial.ValidatedAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Marshalling tests for the tutorial examples.
 *
 * @author brian.oliver
 * Jan-2025
 */
public class TutorialTests {

    /**
     * Ensure a {@link Person} can't be marshalled and unmarshalled when it's not registered.
     */
    @Test
    void shouldNotMarshalAndUnmarshalUnknownSimplePerson() {

        final var person = new Person("Don", "Badman");

        final var marshaller = Marshalling.newMarshaller();

        assertThatThrownBy(() -> marshaller.marshal(person))
            .isInstanceOf(UnsupportedOperationException.class);

    }

    /**
     * Ensure a {@link Person} can be marshalled and unmarshalled (when registered).
     */
    @Test
    void shouldMarshalAndUnmarshalRegisteredSimplePerson() {

        Marshalling.register(Person.class);

        final var person = new Person("Don", "Badman");

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link SelfRegisteredPerson} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalSelfRegisteredSimplePerson() {

        final var person = new SelfRegisteredPerson("Don", "Badman");

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link PersonRecord} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalPersonRecord() {

        final var person = new PersonRecord("Don", "Badman");

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link Person} can be marshalled and unmarshalled (containing {@code null}s).
     */
    @Test
    void shouldMarshalAndUnmarshalSimplePersonWithNulls() {

        final var person = new SelfRegisteredPerson("Don", null);

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link PersonWithFavoriteColor} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalPersonWithFavoriteColor() {

        final var person = new PersonWithFavoriteColor("Don", "Badman", Color.BLUE);

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link PersonWithOptionalMiddleName} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalPersonWithOptionalMiddleName() {

        final var person = new PersonWithOptionalMiddleName("Don", "Badman", Optional.of("George"));

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link PersonWithAddress} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalPersonWithAddress() {

        final var person = new PersonWithAddress(
            "Don",
            "Badman",
            new Address("Shepherd St", "Bowral"));

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link PersonWithPostalAddress} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalPersonWithPostalAddress() {

        final var person = new PersonWithPostalAddress(
            "Don",
            "Badman",
            new ResidentialAddress("Shepherd St", "Bowral"));

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);
    }

    /**
     * Ensure a {@link ValidatedAddress} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalValidatedPostalAddress() {

        // a predicate to validate a city
        final CityPredicate cityPredicate = city -> city != null && !city.trim().isBlank();

        final var address = new ValidatedAddress(cityPredicate, "Shepherd St", "Bowral");

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(address);

        assertThat(marshalled)
            .isNotNull();

        // Important!  "Bind" the CityPredicate into the Marshalling "context"
        // (to allow the CityPredicate to be passed as @Bound when required)
        marshaller.bind(CityPredicate.class).to(cityPredicate);

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(address);

        assertThat(unmarshalled)
            .isEqualTo(address);
    }

    /**
     * Ensure a {@link PersonWithPostalAddress} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshalAndUnmarshalPersonWithValidatedAddress() {

        // a predicate to validate a city
        final CityPredicate cityPredicate = city -> city != null && !city.trim().isBlank();

        final var person = new PersonWithValidatedAddress(
            "Don",
            "Badman",
            new ValidatedAddress(cityPredicate, "Shepherd St", "Bowral"));

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(person);

        assertThat(marshalled)
            .isNotNull();

        // Question?  Why is the following line successful?
        // Notice!  We haven't defined the CityPredicate!
        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(person);

        assertThat(unmarshalled)
            .isEqualTo(person);

        // Reason:  Marshalling is ONLY PREFORMED for the "specific object" (aka: "one layer") and no more.
        // 1. It's never recursive for graphs/trees etc.
        // 2. Marshalled<T> "retains" objects that require further marshalling
        // 3. Unmarshalling of Marshalled<T> can thus "re-use" retained objects
        assertThat(person.address())
            .isSameAs(unmarshalled.address());
    }

    /**
     * Ensure an {@link Expense} consisting of {@link ExpenseLine}s can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshallAndUnmarshallExpenses() {
        final var expense = new Expense();
        expense.add(new ExpenseLine("Fruit", 1.42));
        expense.add(new ExpenseLine("Water", 2.00));

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(expense);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(expense);

        assertThat(unmarshalled)
            .isEqualTo(expense);
    }

    /**
     * Ensure an {@link AggregateExpense} can be marshalled and unmarshalled.
     */
    @Test
    void shouldMarshallAndUnmarshallAggregateExpenses() {
        final var expense = new AggregateExpense();
        expense.add(new AggregateExpenseLine(expense, "Fruit", 1.42));
        expense.add(new AggregateExpenseLine(expense, "Water", 2.00));

        final var marshaller = Marshalling.newMarshaller();

        final var marshalled = marshaller.marshal(expense);

        assertThat(marshalled)
            .isNotNull();

        final var unmarshalled = marshaller.unmarshal(marshalled);

        assertThat(unmarshalled)
            .isNotSameAs(expense);

        assertThat(unmarshalled)
            .isEqualTo(expense);
    }
}
