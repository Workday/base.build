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

import build.base.marshalling.Marshaller;
import build.base.marshalling.Parameter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * A JSON <a href="https://en.wikipedia.org/wiki/Codec">Codec</a> for a specific type of value.
 *
 * @param <T> the type of value
 * @author brian.oliver
 * @since Nov-2024
 */
public interface Codec<T> {

    /**
     * Obtains the {@link Class} of value supported by the {@link Codec}.
     *
     * @return the {@link Class} of value
     */
    Class<?> codecClass();

    /**
     * Writes the specified {@link Parameter} value using the provided {@link JsonGenerator}.
     *
     * @param transport  the {@link JsonTransport}
     * @param parameter  the {@link Parameter}
     * @param value      the value to write
     * @param generator  the {@link JsonGenerator}
     * @param marshaller the {@link Marshaller}
     * @throws IOException should writing fail
     */
    void write(JsonTransport transport,
               Parameter parameter,
               T value,
               JsonGenerator generator,
               Marshaller marshaller)
        throws IOException;

    /**
     * Reads the specified {@link Parameter} value using the provided the {@link JsonParser}.
     *
     * @param transport  the {@link JsonTransport}
     * @param parameter  the {@link Parameter}
     * @param parser     the {@link JsonParser}
     * @param marshaller the {@link Marshaller}
     * @return the value
     * @throws IOException should reading fail
     */
    T read(JsonTransport transport,
           Parameter parameter,
           JsonParser parser,
           Marshaller marshaller)
        throws IOException;
}
