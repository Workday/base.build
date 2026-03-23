package build.base.expression;

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

import java.util.function.Function;

/**
 * A {@link Function} to resolve <i>property values</i> of a bean occurring in a {@link jakarta.el.ValueExpression}.
 * <p>
 * For example, the {@link Function} to <i>property values</i> in the bean named {@code 'mymap'} the
 * Jakarta Expression {@code mymap['some_property']} or {@code mymap.some_property}, could be defined by:
 * {@code processor.addPropertyResolver("mymap", propertyName -> ...);}
 *
 * @param <T> the type of value resolved
 * @author graeme.campbell
 * @author brian.oliver
 * @see <a href="https://docs.oracle.com/javaee/7/tutorial/jsf-el.htm">Java Expression Language Documentation</a>
 * @since Jan-2019
 */
@FunctionalInterface
public
interface PropertyResolver<T>
    extends Function<String, T> {

}
