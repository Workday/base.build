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
import build.base.marshalling.Marshaller;
import build.base.marshalling.Parameter;
import build.base.transport.json.ConditionalCodec;
import build.base.transport.json.JsonTransport;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Optional;

/**
 * A {@link ConditionalCodec} of {@link Optional} values.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
@SuppressWarnings("rawtypes")
public class OptionalCodec
    implements ConditionalCodec<Optional<?>> {

    @Override
    public Class<? extends Optional> codecClass() {
        return Optional.class;
    }

    @Override
    public boolean ignore(final Optional<?> optional) {
        return optional != null && optional.isEmpty();
    }

    @Override
    public Optional<?> defaultValue() {
        return Optional.empty();
    }

    @Override
    public void write(final JsonTransport transport,
                      final Parameter parameter,
                      final Optional<?> optional,
                      final JsonGenerator generator,
                      final Marshaller marshaller)
        throws IOException {

        if (optional == null) {
            generator.writeNull();
        }
        else {
            // determine the type of Optional element
            final var elementType = Introspection.getParameterType(parameter.type())
                .orElseThrow(() -> new IOException("Failed to determine a Optional<T> element type T for ["
                    + parameter.name() + "] of type [" + parameter.type() + "]"));

            if (optional.isEmpty()) {
                generator.writeStartArray();
                generator.writeEndArray();
            }
            else {
                generator.writeStartArray();
                final var element = optional.get();
                transport.write(parameter, elementType, element, generator, marshaller);
                generator.writeEndArray();
            }
        }
    }

    @Override
    public Optional<?> read(final JsonTransport transport,
                            final Parameter parameter,
                            final JsonParser parser,
                            final Marshaller marshaller)
        throws IOException {

        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            parser.nextToken();

            return null;
        }

        // determine the type of Optional element
        final var elementType = Introspection.getParameterType(parameter.type())
            .orElseThrow(() -> new IOException("Failed to determine a Optional<T> element type T for ["
                + parameter.name() + "] of type [" + parameter.type() + "]"
                + " at " + parser.currentLocation()));

        // determine the class of Optional element
        final var elementClass = Introspection.getClassFromType(elementType)
            .orElse(Object.class);

        if (parser.currentToken() == JsonToken.START_ARRAY) {
            // skip start of array "["
            parser.nextToken();

            if (parser.currentToken() == JsonToken.END_ARRAY) {
                // skip end of array "]"
                parser.nextToken();

                return Optional.empty();
            }

            final var optional = Optional.ofNullable(transport.read(parameter, elementClass, parser, marshaller));

            // skip end of array "]"
            parser.nextToken();

            return optional;
        }
        else {
            // unexpected token
            throw new IllegalStateException("Unexpected token [" + parser.currentToken() + "]");
        }
    }
}
