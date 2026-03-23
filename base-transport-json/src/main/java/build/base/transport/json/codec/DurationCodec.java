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

import build.base.marshalling.Marshaller;
import build.base.marshalling.Parameter;
import build.base.transport.json.Codec;
import build.base.transport.json.JsonTransport;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.time.Duration;

/**
 * A {@link Codec} for {@link Duration} values.
 *
 * @author tim.berston
 * @since Jan-2025
 */
public class DurationCodec
    implements Codec<Duration> {

    @Override
    public Class<Duration> codecClass() {
        return Duration.class;
    }

    @Override
    public void write(final JsonTransport transport,
                      final Parameter parameter,
                      final Duration value,
                      final JsonGenerator generator,
                      final Marshaller marshaller)

        throws IOException {

        if (value == null) {
            generator.writeNull();
        }
        else {
            generator.writeNumber(value.toNanos());
        }
    }

    @Override
    public Duration read(final JsonTransport transport,
                           final Parameter parameter,
                           final JsonParser parser,
                           final Marshaller marshaller)
        throws IOException {

        final var currentToken = parser.currentToken();

        if (currentToken == JsonToken.VALUE_NULL) {
            return null;
        }
        else if (currentToken != JsonToken.VALUE_NUMBER_INT) {
            throw new IOException(
                "Expected a Duration but got " + parser.currentToken() + " at " + parser.currentLocation());
        }
        else {
            return Duration.ofNanos(parser.getLongValue());
        }
    }
}
