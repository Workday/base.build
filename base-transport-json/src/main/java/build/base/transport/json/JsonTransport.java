package build.base.transport.json;

/*-
 * #%L
 * base.build Transport (JSON)
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

import build.base.foundation.Introspection;
import build.base.foundation.stream.Streamable;
import build.base.foundation.stream.Streams;
import build.base.foundation.tuple.Pair;
import build.base.foundation.tuple.Triple;
import build.base.marshalling.Marshalled;
import build.base.marshalling.Marshaller;
import build.base.marshalling.Marshalling;
import build.base.marshalling.Out;
import build.base.marshalling.Parameter;
import build.base.marshalling.Schema;
import build.base.marshalling.SchemaFactory;
import build.base.transport.AbstractTransport;
import build.base.transport.Transport;
import build.base.transport.json.codec.BigDecimalCodec;
import build.base.transport.json.codec.BigIntegerCodec;
import build.base.transport.json.codec.BooleanCodec;
import build.base.transport.json.codec.ByteCodec;
import build.base.transport.json.codec.CharacterCodec;
import build.base.transport.json.codec.DateCodec;
import build.base.transport.json.codec.DoubleCodec;
import build.base.transport.json.codec.DurationCodec;
import build.base.transport.json.codec.FloatCodec;
import build.base.transport.json.codec.InstantCodec;
import build.base.transport.json.codec.IntegerCodec;
import build.base.transport.json.codec.LocalDateCodec;
import build.base.transport.json.codec.LocalDateTimeCodec;
import build.base.transport.json.codec.LocalTimeCodec;
import build.base.transport.json.codec.LongCodec;
import build.base.transport.json.codec.OptionalCodec;
import build.base.transport.json.codec.PeriodCodec;
import build.base.transport.json.codec.StreamableCodec;
import build.base.transport.json.codec.StringCodec;
import build.base.transport.json.codec.TimestampCodec;
import build.base.transport.json.codec.ZonedDateTimeCodec;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * A JSON-based {@link Transport} for {@link Marshalled} {@link Object}s.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
public class JsonTransport
    extends AbstractTransport<JsonTransport> {

    /**
     * The JSON field name to capture the {@link Class} name for {@link Marshalled}.
     */
    private static final String TYPE_FIELD_NAME = "type";

    /**
     * The {@link SchemaFactory} used by the {@link JsonTransport} for unmarshalling.
     */
    private final SchemaFactory schemaFactory;

    /**
     * The {@link Codec}s by {@link Class} supported by the {@link JsonTransport}.
     */
    private final ConcurrentHashMap<Class<?>, Codec<?>> codecs;

    /**
     * Constructs a default {@link JsonTransport} using the specified {@link SchemaFactory}.
     *
     * @param schemaFactory the {@link SchemaFactory}
     */
    public JsonTransport(final SchemaFactory schemaFactory) {

        this.codecs = new ConcurrentHashMap<>();
        this.schemaFactory = schemaFactory == null
            ? Marshalling.globalSchemaFactory()
            : schemaFactory;

        register(new StringCodec());
        register(new OptionalCodec());
        register(new StreamableCodec());
        register(new IntegerCodec());
        register(new BooleanCodec());
        register(new LongCodec());
        register(new ByteCodec());
        register(new FloatCodec());
        register(new DoubleCodec());
        register(new CharacterCodec());
        register(new BigDecimalCodec());
        register(new BigIntegerCodec());
        register(new InstantCodec());
        register(new LocalDateCodec());
        register(new LocalTimeCodec());
        register(new LocalDateTimeCodec());
        register(new ZonedDateTimeCodec());
        register(new DurationCodec());
        register(new PeriodCodec());
        register(new DateCodec());
        register(new TimestampCodec());
    }

    /**
     * Constructs a {@link JsonTransport} using the {@link Marshalling#globalSchemaFactory()}.
     */
    public JsonTransport() {
        this(Marshalling.globalSchemaFactory());
    }

    /**
     * Registers the specified {@link Codec} for use with the {@link JsonTransport}.
     *
     * @param codec the {@link Codec}
     * @return this {@link JsonTransport} to permit fluent-style method invocation
     */
    public JsonTransport register(final Codec<?> codec) {
        if (codec != null) {
            this.codecs.put(codec.codecClass(), codec);
        }

        return this;
    }

    /**
     * Obtains the {@link Codec} for the specified {@link Type}.
     *
     * @param type the {@link Type}
     * @return the {@link Optional} {@link Codec}, otherwise {@link Optional#empty()}
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<Codec<T>> getCodec(final Type type) {
        return Introspection.getClassFromType(type)
            .map(this.codecs::get)
            .map(codec -> (Codec<T>) codec);
    }

    /**
     * Write the specified {@link Marshalled} {@link Object} using the provided {@link JsonGenerator}.
     *
     * @param marshalled the {@link Marshalled}
     * @param generator  the {@link JsonGenerator}
     * @throws IOException should writing fail
     */
    @SuppressWarnings("unchecked")
    public void write(final Marshalled<?> marshalled,
                      final JsonGenerator generator)
        throws IOException {

        Objects.requireNonNull(marshalled, "The Marshalled object must not be null");
        Objects.requireNonNull(generator, "The JsonGenerator must not be null");

        generator.writeStartObject();
        generator.writeStringField(TYPE_FIELD_NAME, marshalled.schema().owner().getName());

        final Iterable<Pair<Parameter, Object>> iterable = () -> Streams.zip(
                marshalled.schema().parameters().stream(),
                marshalled.values().stream())
            .iterator();

        final var marshaller = this.schemaFactory.newMarshaller();

        for (Pair<Parameter, Object> pair : iterable) {
            final var parameter = pair.first();
            final var value = pair.second();

            final var codec = getCodec(parameter.type());

            // when there is a Codec for the type, determine if the value should be ignored
            if (codec.filter(ConditionalCodec.class::isInstance)
                .map(ConditionalCodec.class::cast)
                .filter(conditionalCodec -> conditionalCodec.ignore(value))
                .isPresent()) {

                continue;
            }

            generator.writeFieldName(parameter.name());

            write(parameter, parameter.type(), value, generator, marshaller);
        }

        generator.writeEndObject();
        generator.flush();
    }

    /**
     * Write the specified {@link Parameter} value, of the specified {@link Type}, which may be different from
     * the {@link Parameter#type()} due to transformation, using the {@link Codec}s known to the {@link Transport}
     * with the provided {@link JsonGenerator}, further transforming the value as needed.
     *
     * @param parameter  the {@link Parameter}
     * @param valueType  the {@link Type} of value
     * @param value      the value
     * @param generator  the {@link JsonGenerator}
     * @param marshaller the {@link Marshaller}
     * @throws IOException should writing fail
     */
    public void write(final Parameter parameter,
                      final Type valueType,
                      final Object value,
                      final JsonGenerator generator,
                      final Marshaller marshaller)
        throws IOException {

        if (value == null) {
            generator.writeNull();
        }
        else if (value instanceof Marshalled<?> marshalledValue) {
            write(marshalledValue, generator);
        }
        else {
            final var valueClass = Introspection.getClassFromType(valueType)
                .orElseThrow(() -> new IOException("Failed to determine Class of [" + valueType + "]"
                    + " to write parameter [" + parameter.name() + "]"));

            // attempt to locate a Transformer for the value
            final var optionalTransformer = getTransformer(valueClass);

            if (optionalTransformer.isPresent()) {
                final var transformer = optionalTransformer.orElseThrow();

                final var transformedValue = transformer.transform(marshaller, value);

                if (Objects.equals(transformedValue, value)) {
                    // as no transformation occurred, we can't write the value (we couldn't before this!)
                    throw new IOException("Failed to transform parameter [" + parameter.name() + "]"
                        + " with value [" + value + "]"
                        + " of type [" + value.getClass() + "]"
                        + " into a different value that can be written");
                }
                write(parameter, transformer.targetClass(), transformedValue, generator, marshaller);
            }
            else {
                // attempt to use a Codec
                final var optionalCodec = getCodec(valueType);

                if (optionalCodec.isPresent()) {
                    optionalCodec.orElseThrow()
                        .write(this, parameter, value, generator, marshaller);
                }
                else if (this.schemaFactory.isMarshallable(valueClass)) {
                    final var marshalledValue = marshaller.marshal(value);
                    write(marshalledValue, generator);
                }
                else {
                    // when the type is Object, an Interface or Abstract, attempt to use the concrete value type
                    if (valueClass == Object.class
                        || valueClass.isInterface()
                        || Modifier.isAbstract(valueClass.getModifiers())) {

                        // write out the concrete value so the reader can read it
                        generator.writeStartObject();
                        try {
                            generator.writeFieldName("type");
                            generator.writeString(value.getClass().getName());
                            generator.writeFieldName("value");
                            write(parameter, value.getClass(), value, generator, marshaller);
                        }
                        finally {
                            generator.writeEndObject();
                        }
                    }
                    else {
                        // TODO: it may be possible there is a transformer or codec for value.getClass()
                        // in which case we should try that too?

                        throw new IOException("Failed to write parameter [" + parameter.name() + "]"
                            + " with value [" + value + "]"
                            + " of type [" + valueClass + "]"
                            + " as no suitable Transformer, Codec or @Marshal-able type was found");
                    }
                }
            }
        }
    }

    /**
     * Reads a {@link Marshalled} {@link Object} using the provided {@link JsonParser} and {@link Marshaller}.
     *
     * @param parser     the {@link JsonParser}
     * @param marshaller the {@link Marshaller}
     * @param <T>        the type of {@link Marshalled} {@link Object}
     * @return the {@link Marshalled} {@link Object}
     * @throws IOException should an exception occur
     */
    @SuppressWarnings("unchecked")
    public <T> Marshalled<T> read(final JsonParser parser,
                                  final Marshaller marshaller)
        throws IOException {

        Objects.requireNonNull(parser, "The JsonParser must not be null");
        Objects.requireNonNull(schemaFactory, "The SchemaFactory must not be null");

        // ensure there's a current token from which to read the Marshalled
        // (by default a newly created JsonParser may not have read any tokens!)
        if (parser.currentToken() == null) {
            parser.nextToken();
        }

        if (!parser.isExpectedStartObjectToken()) {
            throw new IOException("Failed to read Marshalled Object at " + parser.currentLocation());
        }

        // skip the start of the start of the JsonObject Token
        parser.clearCurrentToken();

        // parse the class of type to unmarshal (from which we can then determine the available unmarshalling schemas)
        final var typeField = parser.nextFieldName();
        if (!typeField.equals(TYPE_FIELD_NAME)) {
            throw new IOException("Expected " + TYPE_FIELD_NAME + " field defined at " + parser.currentLocation());
        }

        final var typeName = parser.nextTextValue();
        if (typeName == null) {
            throw new IOException("Expected " + TYPE_FIELD_NAME + " field value at " + parser.currentLocation());
        }

        final Class<?> typeClass;
        try {
            typeClass = Class.forName(typeName);
        }
        catch (final ClassNotFoundException e) {
            throw new IOException("Failed to load class " + typeName + " to read at " + parser.currentLocation(), e);
        }

        // determine the Schemas, their Parameters and corresponding Out values that are candidates for unmarshalling
        final var schemas = this.schemaFactory.getUnmashallingSchemas(typeClass)
            .map(schema -> Pair.of(
                schema,
                schema.parameters().stream()
                    .collect(Collectors.toMap(
                        Parameter::name,
                        parameter -> Pair.of(parameter, Out.empty())))))
            .collect(Collectors.toCollection(ArrayList::new));

        if (schemas.isEmpty()) {
            throw new IOException("No schemas are defined for type " + typeName + " at " + parser.currentLocation());
        }

        // process the fields
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            final var parameterName = parser.currentName();

            // skip the field name token to advance to the possible value
            parser.nextToken();

            // remove the Schemas that don't have such a named parameter
            schemas.removeIf(pair -> !pair.second().containsKey(parameterName));

            if (schemas.isEmpty()) {
                throw new IOException("Failed to locate schema to support unmarshalling the type " + typeName + " at "
                    + parser.currentLocation());
            }

            // attempt to use the first available schema's first parameter type to read the value
            final var parameter = schemas.getFirst()
                .second()
                .get(parameterName)
                .first();

            final var value = read(parameter, parameter.type(), parser, marshaller);

            // update the remaining candidate schemas with the parameter value
            schemas.forEach(schema -> {
                final var argument = schema.second().get(parameterName);
                argument.second().set(value);
            });
        }

        // find the first schema with completed set of arguments
        var optionalSchemaValues = schemas.stream()
            .filter(pair -> pair.second().values().stream()
                .map(Pair::second)
                .allMatch(Out::isPresent))
            .findFirst();

        if (optionalSchemaValues.isEmpty()) {
            // attempt to complete missing values using ConditionalCodecs
            optionalSchemaValues = schemas.stream()
                .filter(pair -> pair.second().values().stream()
                    .map(triple -> Triple.of(triple.second(), triple.first().type(),
                        triple.second().isEmpty() ? null : triple.second().get()))
                    .allMatch(triple -> {
                        // skip the parameter when the value is present
                        if (triple.first().isPresent()) {
                            return true;
                        }

                        // attempt to use a ConditionalCodec for the parameter to provide a default value
                        final var codec = getCodec(triple.second());

                        codec.filter(ConditionalCodec.class::isInstance)
                            .map(ConditionalCodec.class::cast)
                            .ifPresent(conditionalCodec -> triple.first().set(conditionalCodec.defaultValue()));

                        return triple.first().isPresent();
                    }))
                .findFirst();
        }

        final var schemaValues = optionalSchemaValues
            .orElseThrow(() -> new IOException(
                "Failed to parse required values for type " + typeName + " at " + parser.currentLocation()));

        // ensure the order of the values is the same order as the schema parameters
        final var values = Streamable.of(schemaValues.first().parameters().stream()
            .map(parameter -> schemaValues.second().get(parameter.name()).second().orElse(null)));

        // establish a Marshalled representation using the schema and captured values
        return new Marshalled<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public Schema<T> schema() {
                return (Schema<T>) schemaValues.first();
            }

            @Override
            public Streamable<Object> values() {
                return values;
            }
        };
    }

    /**
     * Reads a {@link Marshalled} {@link Object} using the provided {@link JsonParser} and a default {@link Marshaller}.
     *
     * @param parser the {@link JsonParser}
     * @param <T>    the type of {@link Marshalled} {@link Object}
     * @return the {@link Marshalled} {@link Object}
     * @throws IOException should an exception occur
     */
    public <T> Marshalled<T> read(final JsonParser parser)
        throws IOException {

        return read(parser, this.schemaFactory.newMarshaller());
    }

    /**
     * Read the specified {@link Type} of {@link Parameter} value, which may be different from the
     * {@link Parameter#type()} due to transformation, using the {@link Codec}s known to the {@link Transport}
     * with the provided {@link JsonParser}, further transforming the value as needed.
     *
     * @param parameter  the {@link Parameter}
     * @param type       the {@link Type} of value
     * @param parser     the {@link JsonParser}
     * @param marshaller the {@link Marshaller}
     * @return the read value
     * @throws IOException should writing fail
     */
    @SuppressWarnings("unchecked")
    public <T> T read(final Parameter parameter,
                      final Type type,
                      final JsonParser parser,
                      final Marshaller marshaller)
        throws IOException {

        if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
            parser.clearCurrentToken();
            return null;
        }

        // determine if there is a Transformer to use for the type
        final var optionalTransformer = getTransformer(type);

        if (optionalTransformer.isPresent()) {
            final var transformer = optionalTransformer.get();
            final var readValue = read(parameter, transformer.targetClass(), parser, marshaller);
            final var transformedValue = transformer.reform(marshaller, parameter.type(), readValue);

            if (Objects.equals(transformedValue, readValue)) {
                throw new IOException("Failed to transform Parameter [" + parameter.name() + "]"
                    + " with value [" + readValue + "]"
                    + " into type [" + type + "]");
            }
            return (T) transformedValue;
        }
        else {
            // determine the initially expected readable class based on the type of value
            final var readableClass = Introspection.getClassFromType(type)
                .orElseThrow(() -> new IOException("Failed to determine a class of value for Parameter ["
                    + parameter.name() + "] of type [" + type + "]"
                    + " at " + parser.currentLocation()));

            // determine if there is a Codec to use for the type
            final var optionalCodec = getCodec(type);

            if (optionalCodec.isPresent()) {
                return (T) optionalCodec.orElseThrow()
                    .read(this, parameter, parser, marshaller);
            }
            else if (Marshalled.class.isAssignableFrom(readableClass)) {
                return (T) read(parser, marshaller);
            }
            else if (marshaller.isMarshallable(readableClass)) {
                final var marshalled = read(parser, marshaller);
                return (T) marshaller.unmarshal(marshalled);
            }
            else {
                // when the type is Object, an Interface or Abstract, attempt to use the concrete value type
                if (readableClass == Object.class
                    || readableClass.isInterface()
                    || Modifier.isAbstract(readableClass.getModifiers())) {

                    if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
                        parser.clearCurrentToken();
                        return null;
                    }
                    else if (parser.isExpectedStartObjectToken()) {
                        parser.nextToken();

                        // Expect that this object will be serialized with the 'type'
                        // field first. If it is not, then we cannot know what type to
                        // pass into the transport to parse the value.
                        if (!parser.getText().equals("type")) {
                            throw new IOException("Expected 'type' field name, but found ["
                                + parser.getText() + "] for ["
                                + parameter.name() + "] of type [" + parameter.type() + "]"
                                + " at " + parser.currentLocation());
                        }

                        parser.nextToken();
                        final var typeName = parser.getText();

                        try {
                            final var concreteType = Class.forName(typeName);

                            parser.nextToken();
                            if (!parser.getText().equals("value")) {
                                throw new IOException("Expected 'value' field name, but found ["
                                    + parser.getText() + "] for ["
                                    + parameter.name() + "] of type [" + parameter.type() + "]"
                                    + " at " + parser.currentLocation());
                            }
                            parser.nextToken();

                            final var value = read(parameter, concreteType, parser, marshaller);

                            // At this point we are still inside the serialized object, so we need to advance
                            // to the end of the object for the parsing to continue properly.
                            parser.nextToken();
                            return (T) value;
                        }
                        catch (final ClassNotFoundException e) {
                            throw new IOException("Failed to load class [" + typeName + "] for ["
                                + parameter.name() + "] of type [" + parameter.type() + "]"
                                + " at " + parser.currentLocation(), e);
                        }
                    }
                    else {
                        throw new IOException("Expected a JsonObject for ["
                            + parameter.name() + "] of type [" + parameter.type() + "]"
                            + " at " + parser.currentLocation());
                    }
                }
                else {
                    throw new IOException("Failed to determine a Transformer, Codec or @Marshal-able for ["
                        + parameter.name() + "] of type [" + parameter.type() + "]"
                        + " at " + parser.currentLocation());
                }
            }
        }
    }
}
