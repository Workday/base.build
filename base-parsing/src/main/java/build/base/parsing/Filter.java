package build.base.parsing;

/*-
 * #%L
 * base.build Parsing
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

import build.base.io.LookaheadReader;

import java.util.function.Consumer;

/**
 * A {@link Consumer} of a {@link LookaheadReader} that skips content to be ignored while parsing.
 *
 * @author brian.oliver
 * @since Mar-2020
 */
public interface Filter
    extends Consumer<LookaheadReader> {

    /**
     * A {@link Filter} for characters that satisfy {@link Character#isWhitespace(char)}.
     */
    Filter WHITESPACE = (reader) -> {
        while (reader.follows(Character::isWhitespace)) {
            reader.skip(1);
        }
    };

    /**
     * A {@link Filter} for Java Single Line Comments.
     */
    Filter JAVA_SINGLE_LINE_COMMENT = (reader) -> {
        if (reader.follows("//")) {
            reader.skipWhile(c -> c != '\n');
        }
    };

    /**
     * A {@link Filter} for Java Multiline Comments.
     */
    Filter JAVA_MULTILINE_COMMENT = (reader) -> {
        if (reader.follows("/*")) {
            reader.consume(2);

            while (!reader.follows("*/")) {
                reader.skip(1);
            }

            if (reader.consume(2).length() != 2) {
                throw new ParseException(reader.getLocation(), "*/", reader.peekMaximum());
            }
        }
    };
}
