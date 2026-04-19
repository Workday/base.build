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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public abstract class Out {

    private final StringBuilder buffer;
    private final Writer writer;

    protected Out() {
        this.buffer = new StringBuilder();
        this.writer = null;
    }

    protected Out(final Writer writer) {
        this.buffer = null;
        this.writer = writer;
    }

    public final void raw(final String s) {
        if (writer != null) {
            try {
                writer.write(s);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            buffer.append(s);
        }
    }

    public abstract void write(Object value);

    @Override
    public final String toString() {
        return buffer != null ? buffer.toString() : "";
    }
}
