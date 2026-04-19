package build.base.template;

/*-
 * #%L
 * base.build Template
 * %%
 * Copyright (C) 2026 Workday, Inc.
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

import java.io.Writer;

public final class HtmlOut extends Out {

    public HtmlOut() {
        super();
    }

    public HtmlOut(final Writer writer) {
        super(writer);
    }

    @Override
    public void write(final Object value) {
        if (value != null) {
            raw(escape(String.valueOf(value)));
        }
    }

    private static String escape(final String s) {
        StringBuilder result = null;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final String replacement = switch (c) {
                case '&' -> "&amp;";
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '"' -> "&quot;";
                case '\'' -> "&#39;";
                default -> null;
            };
            if (replacement != null) {
                if (result == null) {
                    result = new StringBuilder(s.length() + 8);
                    result.append(s, 0, i);
                }
                result.append(replacement);
            } else if (result != null) {
                result.append(c);
            }
        }
        return result != null ? result.toString() : s;
    }
}
