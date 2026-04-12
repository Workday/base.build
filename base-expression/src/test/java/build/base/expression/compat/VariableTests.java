package build.base.expression.compat;

/*-
 * #%L
 * base.build Expression
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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that {@link Variable}s are created properly and that their properties can be accessed.
 *
 * @author graeme.campbell
 * @since Jan-2019
 */
class VariableTests {

    @Test
    void shouldCreateVariableAndAccessItsProperties() {
        final Variable variable = Variable.of("application", "app");

        assertThat(variable.name()).isEqualTo("application");
        assertThat(variable.value()).isEqualTo("app");
    }
}
