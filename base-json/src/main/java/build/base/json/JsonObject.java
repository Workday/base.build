package build.base.json;

/*-
 * #%L
 * base.build JSON
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

import java.util.Map;

/**
 * A JSON object value — an unordered collection of named members.
 */
public sealed interface JsonObject extends JsonValue permits JsonObjectImpl {

    Map<String, JsonValue> members();

    static JsonObject of(final Map<String, JsonValue> members) {
        return new JsonObjectImpl(Map.copyOf(members));
    }

    static Builder builder() {
        return new JsonObjectBuilderImpl();
    }

    default boolean has(final String key) {
        return members().containsKey(key);
    }

    @Override
    default JsonObject asObject() {
        return this;
    }

    /**
     * A mutable builder that produces an immutable {@link JsonObject}.
     */
    interface Builder {

        Builder put(String key, JsonValue value);

        Builder put(String key, String value);

        Builder put(String key, Number value);

        Builder put(String key, boolean value);

        Builder putNull(String key);

        JsonObject build();
    }
}
