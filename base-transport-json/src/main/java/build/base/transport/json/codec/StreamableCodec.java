package build.base.transport.json.codec;

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
import build.base.marshalling.Marshaller;
import build.base.marshalling.Parameter;
import build.base.transport.json.ConditionalCodec;
import build.base.transport.json.JsonTransport;

import java.io.IOException;
import java.util.LinkedList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * A {@link ConditionalCodec} of {@link Streamable} values.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
public class StreamableCodec<T>
    implements ConditionalCodec<Streamable<T>> {

    @Override
    public Class<?> codecClass() {
        return Streamable.class;
    }

    @Override
    public boolean ignore(final Streamable<T> streamable) {
        return streamable != null && streamable.isEmpty();
    }

    @Override
    public Streamable<T> defaultValue() {
        return Streamable.empty();
    }

    @Override
    public void write(final JsonTransport transport,
                      final Parameter parameter,
                      final Streamable<T> streamable,
                      final JsonGenerator generator,
                      final Marshaller marshaller)
        throws IOException {

        if (streamable == null) {
            generator.writeNull();
        }
        else {
            // determine the type of Stream element
            final var elementType = Introspection.getParameterType(parameter.type())
                .orElseThrow(() -> new IOException("Failed to determine a Streamable type for "
                    + parameter.name() + " of type " + parameter.type()));

            generator.writeStartArray();

            for (Object element : streamable) {
                // only write non-null element values
                // (no point in writing nulls into an array)
                if (element != null) {
                    transport.write(parameter, elementType, element, generator, marshaller);
                }
            }

            generator.writeEndArray();
        }
    }

    @Override
    public Streamable<T> read(final JsonTransport transport,
                              final Parameter parameter,
                              final JsonParser parser,
                              final Marshaller marshaller)
        throws IOException {

        // determine the type of Stream element
        final var elementType = Introspection.getParameterType(parameter.type())
            .orElseThrow(() -> new IOException("Failed to determine a Stream type for "
                + parameter.name() + " of type " + parameter.type()
                + " at " + parser.currentLocation()));

        // determine the class of Stream element
        final var elementClass = Introspection.getClassFromType(elementType)
            .orElseThrow();

        if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
            return defaultValue();
        }

        if (!parser.isExpectedStartArrayToken()) {
            throw new IOException(
                "Expected start of array at " + parser.currentLocation() + " for parameter " + parameter.name());
        }

        // skip the start of the start of the JsonArray Token
        parser.clearCurrentToken();

        final var elements = new LinkedList<T>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            elements.add(transport.read(parameter, elementClass, parser, marshaller));
        }

        return Streamable.of(elements);
    }
}
