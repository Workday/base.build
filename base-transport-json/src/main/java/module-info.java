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
/**
 * A facility to <i>transport</i> <i>marshalled</i> objects using {@code json} format.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
open module build.base.transport.json {
    requires build.base.foundation;
    requires build.base.marshalling;
    requires build.base.transport;

    requires com.fasterxml.jackson.core;
    requires java.sql;

    exports build.base.transport.json;
}
