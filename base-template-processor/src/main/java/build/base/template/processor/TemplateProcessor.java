package build.base.template.processor;

/*-
 * #%L
 * base.build Template Processor
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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("build.base.template.ProcessTemplates")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedOptions("jt.sourceDir")
public final class TemplateProcessor extends AbstractProcessor {

    private boolean processed = false;

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        if (processed || roundEnv.processingOver()) {
            return false;
        }
        processed = true;

        final Path jtSourceDir = resolveJtSourceDir();
        if (jtSourceDir == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "base-template-processor: could not determine jt source directory; "
                + "set -Ajt.sourceDir=<path> if your layout is non-standard");
            return false;
        }
        if (!Files.isDirectory(jtSourceDir)) {
            return false;
        }

        try (Stream<Path> files = Files.walk(jtSourceDir)) {
            files.filter(p -> p.toString().endsWith(".jt"))
                .forEach(this::processTemplate);
        } catch (final IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "base-template-processor: failed to walk " + jtSourceDir + ": " + e.getMessage());
        }

        return false;
    }

    private void processTemplate(final Path jtFile) {
        try {
            final List<String> lines = Files.readAllLines(jtFile);
            final ParsedTemplate parsed = JtParser.parse(lines, jtFile.toString());
            final String source = CodeGenerator.generate(parsed);

            final javax.tools.JavaFileObject output = processingEnv.getFiler()
                .createSourceFile(parsed.qualifiedClassName());

            try (var writer = output.openWriter()) {
                writer.write(source);
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        } catch (final JtParseException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "base-template-processor: " + e.getMessage());
        }
    }

    private Path resolveJtSourceDir() {
        final String explicit = processingEnv.getOptions().get("jt.sourceDir");
        if (explicit != null) {
            return Path.of(explicit);
        }
        try {
            final javax.tools.FileObject probe = processingEnv.getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", ".base-template-probe");
            final URI uri = probe.toUri();
            probe.delete();

            // From target/classes → project root → src/main/jt
            final Path classOutput = Path.of(uri).getParent();
            return classOutput.getParent().getParent().resolve("src/main/jt");
        } catch (final IOException e) {
            return null;
        }
    }
}
