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

/**
 * A {@link Codec} for {@link Character} and {@code char} values.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
public class CharacterCodec
    implements Codec<Character> {

    @Override
    public Class<Character> codecClass() {
        return Character.class;
    }

    @Override
    public void write(final JsonTransport transport,
                      final Parameter parameter,
                      final Character value,
                      final JsonGenerator generator,
                      final Marshaller marshaller)

        throws IOException {

        if (value == null) {
            generator.writeNull();
        }
        else {
            generator.writeString(String.valueOf(value));
        }
    }

    @Override
    public Character read(final JsonTransport transport,
                          final Parameter parameter,
                          final JsonParser parser,
                          final Marshaller marshaller)
        throws IOException {

        final var currentToken = parser.currentToken();

        if (currentToken == JsonToken.VALUE_NULL) {
            return null;
        }
        else if (currentToken != JsonToken.VALUE_STRING) {
            throw new IOException(
                "Expected a Character but got " + parser.currentToken() + " at " + parser.currentLocation());
        }
        else {
            return parser.getText().charAt(0);
        }
    }
}
