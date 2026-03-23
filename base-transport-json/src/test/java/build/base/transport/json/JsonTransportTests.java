package build.base.transport.json;

import build.base.marshalling.Bound;
import build.base.marshalling.Marshal;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshalling;
import build.base.transport.json.example.Address;
import build.base.transport.json.example.AddressWithCountry;
import build.base.transport.json.example.ClassWithArrayOfClassWithObjectProperty;
import build.base.transport.json.example.ClassWithObjectProperty;
import build.base.transport.json.example.Company;
import build.base.transport.json.example.Country;
import build.base.transport.json.example.Expense;
import build.base.transport.json.example.ExpenseLine;
import build.base.transport.json.example.MultipleUnmarshalls;
import build.base.transport.json.example.Person;
import build.base.transport.json.example.PersonWithFirstName;
import build.base.transport.json.example.PersonWithOptionalLastName;
import build.base.transport.json.example.StreamsAndOptionals;
import build.base.transport.json.example.TemporalPerson;
import build.base.transport.json.example.Uber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;

/**
 * Tests for {@link JsonTransport}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
public class JsonTransportTests {

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing a {@link String} can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledString()
        throws IOException {

        final var person = new PersonWithFirstName("Gustav");

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithFirstName> transported = transport.read(parser);

        final PersonWithFirstName unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing a {@code null} {@link String} can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledNullString()
        throws IOException {

        final var person = new PersonWithFirstName(null);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithFirstName> transported = transport.read(parser);

        final PersonWithFirstName unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing an empty {@link String} can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledEmptyString()
        throws IOException {

        final var person = new PersonWithFirstName("");

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithFirstName> transported = transport.read(parser);

        final PersonWithFirstName unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing an {@link Optional} {@link String} can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledOptionalString()
        throws IOException {

        final var person = new PersonWithOptionalLastName("Gustav", Optional.of("von Redwitz"));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithOptionalLastName> transported = transport.read(parser);

        final PersonWithOptionalLastName unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing an {@link Optional#empty()} {@link String} can be
     * transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledOptionalEmptyString()
        throws IOException {

        final var person = new PersonWithOptionalLastName("Gustav", Optional.empty());

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithOptionalLastName> transported = transport.read(parser);

        final PersonWithOptionalLastName unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing a {@code null} {@link Optional} {@link String} can be
     * transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledNullOptionalString()
        throws IOException {

        final var person = new PersonWithOptionalLastName("Gustav", null);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithOptionalLastName> transported = transport.read(parser);

        final PersonWithOptionalLastName unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} containing a {@code null} {@link Optional} can be decoded.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldReadMarshalledNullOptionalString()
        throws IOException {

        final var marshaller = Marshalling.newMarshaller();

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();

        final var json = """
            {"type":"build.base.transport.json.example.PersonWithOptionalLastName","firstName":"Gustav","lastName":null}
            """;

        final var reader = new StringReader(json);
        final var parser = factory.createParser(reader);

        final Marshalled<PersonWithOptionalLastName> transported = transport.read(parser);

        final PersonWithOptionalLastName person = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotNull();

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} composed of another {@link Marshal}lable {@link Object} can be
     * transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadComposedObjectsUsingMarshalleds()
        throws IOException {

        final var person = new Person(new Address("Big Street", "Munich"));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<Person> transported = transport.read(parser);

        final Person unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} composed of another {@link Marshal}lable {@link Object} can be
     * transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadComposedStreamableObjects()
        throws IOException {

        final var company = new Company()
            .add(new Address("Big Street", "Munich"))
            .add(new Address("Small Street", "Boston"));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(company);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<Company> transported = transport.read(parser);

        final Company unmarshalledCompany = marshaller.unmarshal(transported);

        assertThat(company)
            .isNotSameAs(unmarshalledCompany);

        assertThat(company)
            .isEqualTo(unmarshalledCompany);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} composed of another {@link Marshal}lable {@link Object}, but without
     * any {@link Object}s, can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadComposedEmptyStreamableObjects()
        throws IOException {

        final var company = new Company();

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(company);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<Company> transported = transport.read(parser);

        final Company unmarshalledCompany = marshaller.unmarshal(transported);

        assertThat(company)
            .isNotSameAs(unmarshalledCompany);

        assertThat(company)
            .isEqualTo(unmarshalledCompany);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} using a {@code null} {@link Stream} can be decoded.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldReadComposedNullStream()
        throws IOException {

        final var marshaller = Marshalling.newMarshaller();

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();

        final var json = """
            {"type":"build.base.transport.json.example.Company","addresses":null}
            """;

        final var reader = new StringReader(json);
        final var parser = factory.createParser(reader);

        final Marshalled<Company> transported = transport.read(parser);

        final Company company = marshaller.unmarshal(transported);

        assertThat(company)
            .isNotNull();

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} composed of another {@link Marshal}lable {@code null}s can be
     * transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadComposedStreamableNulls()
        throws IOException {

        final var company = new Company()
            .add(null)
            .add(null);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(company);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<Company> transported = transport.read(parser);

        final Company unmarshalledCompany = marshaller.unmarshal(transported);

        assertThat(company)
            .isNotSameAs(unmarshalledCompany);

        assertThat(company)
            .isEqualTo(unmarshalledCompany);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable with an {@code enum} can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadClassWithEnum()
        throws IOException {

        final var address = new AddressWithCountry("Queen Street", "Brisbane", Country.Australia);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(address);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<AddressWithCountry> transported = transport.read(parser);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertThat(address)
            .isNotSameAs(unmarshalled);

        assertThat(address)
            .isEqualTo(unmarshalled);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable {@link Uber} can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadUberClass()
        throws IOException {

        final var uber = new Uber();

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(uber);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<Uber> transported = transport.read(parser);

        final Uber unmarshalledUber = marshaller.unmarshal(transported);

        assertThat(uber)
            .isNotSameAs(unmarshalledUber);

        assertThat(uber)
            .isEqualTo(unmarshalledUber);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable object that contains a null or empty {@link Stream} or {@link Optional} can be transported.
     * <p>
     * Note that null and empty {@link Stream} and {@link Optional} should not be written to the JSON, and
     * unmarshalling should cope with this. This can not be tested directly without confirming the
     * written JSON. It is confirmed indirectly by the unmarshalled object being slightly different
     * to the original.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadNullAndEmptySteamOrNullAndEmptyOptional()
        throws IOException {

        final var original = new StreamsAndOptionals();

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<StreamsAndOptionals> transported = transport.read(parser);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertThat(unmarshalled)
            .isNotSameAs(original);

        assertThat(unmarshalled)
            .isEqualTo(original);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable object that contains a mix of null and populated {@link Stream} or {@link Optional} can be transported.
     * <p>
     * Note that null and empty {@link Stream} and {@link Optional} should not be written to the JSON, and
     * unmarshalling should cope with this. This can not be tested directly without confirming the
     * written JSON. It is confirmed indirectly by the unmarshalled object being slightly different
     * to the original.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadObjectWithNullStreamAndOptional()
        throws IOException {

        final var original = new StreamsAndOptionals(null,
            Arrays.stream(new String[] { "One", "Two" }),
            null,
            Optional.of("Optional"));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<StreamsAndOptionals> transported = transport.read(parser);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertThat(unmarshalled)
            .isNotSameAs(original);

        assertThat(unmarshalled)
            .isEqualTo(original);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable object that contains multiple unmarshalls will prefer one that has all provided values.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadObjectPreferringCompleteUnmarshall()
        throws IOException {

        final var original = new MultipleUnmarshalls("MyValue", Optional.of("Optional"));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<MultipleUnmarshalls> transported = transport.read(parser);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertThat(original)
            .isNotSameAs(unmarshalled);

        assertThat(original)
            .isEqualTo(unmarshalled);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable object that contains multiple unmarshalls will defer to one that is missing
     * parameters that should be ignored.
     * <p>
     * Note that null and empty {@link Optional}s should not be written to the JSON, and
     * unmarshalling should cope with this. This can not be tested directly without confirming the
     * written JSON. It is confirmed indirectly by the unmarshalled object being slightly different
     * to the original.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadObjectUsingIncompleteUnmarshall()
        throws IOException {

        final var original = new MultipleUnmarshalls("MyValue", null);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<MultipleUnmarshalls> transported = transport.read(parser);

        final var unmarshalled = marshaller.unmarshal(transported);

        assertThat(original)
            .isNotSameAs(unmarshalled);

        assertThat(original)
            .isEqualTo(unmarshalled);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable object that contains an Object can be transported.
     * <p>
     * Note that the type contained within the object must be transportable. If not, then
     * an IOException will be thrown.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledClassWithObject()
        throws IOException {
        final var original = new ClassWithObjectProperty(16);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<ClassWithObjectProperty> transported = transport.read(parser);

        final ClassWithObjectProperty unmarshalled = marshaller.unmarshal(transported);

        assertThat(original)
            .isNotSameAs(unmarshalled);

        assertThat(original)
            .isEqualTo(unmarshalled);

        parser.close();
    }

    @Test
    void shouldWriteAndReadMarshalledClassWithArrayOfClassesWithObject()
        throws IOException {
        final var original = new ClassWithArrayOfClassWithObjectProperty(new ArrayList<>());
        original.values().add(new ClassWithObjectProperty(16));
        original.values().add(new ClassWithObjectProperty(32));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<ClassWithArrayOfClassWithObjectProperty> transported = transport.read(parser);

        final ClassWithArrayOfClassWithObjectProperty unmarshalled = marshaller.unmarshal(transported);

        assertThat(original)
            .isNotSameAs(unmarshalled);

        assertThat(original)
            .isEqualTo(unmarshalled);

        parser.close();
    }

    /**
     * Ensure {@link Marshal}lable object that contains an Object with a null value can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledClassWithNullObject()
        throws IOException {
        final var original = new ClassWithObjectProperty(null);

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(original);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<ClassWithObjectProperty> transported = transport.read(parser);

        final ClassWithObjectProperty unmarshalled = marshaller.unmarshal(transported);

        assertThat(original)
            .isNotSameAs(unmarshalled);

        assertThat(original)
            .isEqualTo(unmarshalled);

        parser.close();
    }

    /**
     * Ensure that an IOException is thrown when attempting to read a malformed class that
     * contains an Object. In this instance, the 'type' is not transportable.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldThrowOnReadMarshalledClassWithObjectThatIsNotTransportable()
        throws IOException {

        final var serialized =
            """
                {"type":"build.base.transport.json.example.ClassWithObjectProperty","object":{"type":"java.lang.StringBuilder","value":"16"}}
                """;
        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var reader = new StringReader(serialized);
        final var parser = factory.createParser(reader);

        final var ex = assertThrows(IOException.class, () -> transport.read(parser));
        assertTrue(
            ex.getMessage().startsWith("Failed to determine a Transformer, Codec or @Marshal-able for [object]"));

        parser.close();
    }

    /**
     * Ensure that an IOException is thrown when attempting to read a malformed class that
     * contains an Object. In this instance, the 'value' exists before the 'type'.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldThrowOnReadMarshalledClassWithObjectThatWasSerializedIncorrectOrder()
        throws IOException {

        final var serialized =
            """
                {"type":"build.base.transport.json.example.ClassWithObjectProperty","object":{"value":"16","type":"java.lang.StringBuilder"}}
                """;
        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var reader = new StringReader(serialized);
        final var parser = factory.createParser(reader);

        final var ex = assertThrows(IOException.class, () -> transport.read(parser));
        assertTrue(ex.getMessage().startsWith("Expected 'type' field name, but found"));

        parser.close();
    }

    /**
     * Ensure that an IOException is thrown when attempting to read a malformed class that
     * contains an Object. In this instance, the 'value' is missing.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldThrowOnReadMarshalledClassWithObjectThatWasSerializedWithMissingValue()
        throws IOException {

        final var serialized =
            """
                {"type":"build.base.transport.json.example.ClassWithObjectProperty","object":{"type":"java.lang.StringBuilder"}}
                """;
        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var reader = new StringReader(serialized);
        final var parser = factory.createParser(reader);

        final var ex = assertThrows(IOException.class, () -> transport.read(parser));
        assertTrue(ex.getMessage().startsWith("Expected 'value' field name, but found"));

        parser.close();
    }

    /**
     * Ensure that an IOException is thrown when attempting to read a malformed class that
     * contains an Object. In this instance, the 'object' is a value instead of a nested
     * JSON object.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldThrowOnReadMarshalledClassWithObjectThatIsNotNested()
        throws IOException {

        final var serialized =
            """
                {"type":"build.base.transport.json.example.ClassWithObjectProperty","object":16}
                """;
        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var reader = new StringReader(serialized);
        final var parser = factory.createParser(reader);

        final var ex = assertThrows(IOException.class, () -> transport.read(parser));
        assertTrue(ex.getMessage().startsWith("Expected a JsonObject for"));

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable {@link Object} requiring a {@link Bound} property can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadMarshalledWithBoundObject()
        throws IOException {

        final var now = Instant.now();
        final var person = new TemporalPerson(now, "Sebastian");

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(person);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        marshaller.bind(now).universally();

        final Marshalled<TemporalPerson> transported = transport.read(parser, marshaller);

        final TemporalPerson unmarshalledPerson = marshaller.unmarshal(transported);

        assertThat(person)
            .isNotSameAs(unmarshalledPerson);

        assertThat(person)
            .isEqualTo(unmarshalledPerson);

        parser.close();
    }

    /**
     * Ensure a {@link Marshal}lable composed of another {@link Marshal}lable can be transported.
     *
     * @throws IOException should an exception occur
     */
    @Test
    void shouldWriteAndReadComposedObject()
        throws IOException {

        final var expense = new Expense();
        expense.add(new ExpenseLine(expense, "Nasi Gorang", 100.0));
        expense.add(new ExpenseLine(expense, "Bintang", 200.0));

        final var marshaller = Marshalling.newMarshaller();
        final var marshalled = marshaller.marshal(expense);

        final var transport = new JsonTransport();
        final var factory = new JsonFactory();
        final var writer = new StringWriter();

        final var generator = factory.createGenerator(writer);

        transport.write(marshalled, generator);

        generator.close();

        final var reader = new StringReader(writer.toString());
        final var parser = factory.createParser(reader);

        final Marshalled<Expense> transported = transport.read(parser);

        final Expense unmarshalledExpense = marshaller.unmarshal(transported);

        assertThat(expense)
            .isNotSameAs(unmarshalledExpense);

        assertThat(expense)
            .isEqualTo(unmarshalledExpense);
    }
}
