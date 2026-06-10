package build.base.compile.testing;

/*-
 * #%L
 * base.build Compile Testing
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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationTests {

    @Test
    void trivialSourceCompilesSuccessfully() {
        final Compilation result = Compiler.javac().compile(
            JavaFileObjects.forSourceString("com.example.Hello",
                "package com.example; public class Hello {}"));

        assertThat(result.status()).isEqualTo(Compilation.Status.SUCCESS);
        assertThat(result.succeeded()).isTrue();
        assertThat(result.errors().toList()).isEmpty();
    }

    @Test
    void syntaxErrorProducesFailureAndErrorDiagnostic() {
        final Compilation result = Compiler.javac().compile(
            JavaFileObjects.forSourceString("com.example.Broken",
                "package com.example; public class Broken { SYNTAX ERROR }"));

        assertThat(result.status()).isEqualTo(Compilation.Status.FAILURE);
        assertThat(result.errors().toList()).isNotEmpty();
    }

    @Test
    void unresolvedTypeProducesError() {
        final Compilation result = Compiler.javac().compile(
            JavaFileObjects.forSourceString("com.example.Missing",
                "package com.example; public class Missing { DoesNotExist x; }"));

        assertThat(result.status()).isEqualTo(Compilation.Status.FAILURE);
        assertThat(result.errors().toList()).isNotEmpty();
    }

    @Test
    void processorObservesRoundEnvironment() {
        final AtomicBoolean processorRan = new AtomicBoolean(false);

        final AbstractProcessor processor = new AbstractProcessor() {
            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Set.of("*");
            }

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latestSupported();
            }

            @Override
            public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                if (!roundEnv.processingOver()) {
                    processorRan.set(true);
                    assertThat(roundEnv.getRootElements()).isNotEmpty();
                }
                return false;
            }
        };

        final Compilation result = Compiler.javac()
            .withProcessors(processor)
            .compile(JavaFileObjects.forSourceString("com.example.Subject",
                "package com.example; public class Subject {}"));

        assertThat(result.succeeded()).isTrue();
        assertThat(processorRan).isTrue();
    }

    @Test
    void processorGeneratesSourceFileAccessibleFromResult() {
        final AtomicBoolean didGenerate = new AtomicBoolean(false);

        final AbstractProcessor processor = new AbstractProcessor() {
            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Set.of("*");
            }

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latestSupported();
            }

            @Override
            public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                if (!roundEnv.processingOver() && didGenerate.compareAndSet(false, true)) {
                    final Filer filer = processingEnv.getFiler();
                    try {
                        final JavaFileObject file = filer.createSourceFile("com.example.Generated");
                        try (final PrintWriter writer = new PrintWriter(file.openWriter())) {
                            writer.println("package com.example; public class Generated {}");
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return false;
            }
        };

        final Compilation result = Compiler.javac()
            .withProcessors(processor)
            .compile(JavaFileObjects.forSourceString("com.example.Trigger",
                "package com.example; public class Trigger {}"));

        assertThat(result.succeeded()).isTrue();
        assertThat(result.generatedFiles()).isNotEmpty();

        final Optional<JavaFileObject> generated = result.generatedSourceFile("com.example.Generated");
        assertThat(generated).isPresent();
    }

    @Test
    void multipleSourceFilesInDistinctPackagesCompileTogether() {
        final Compilation result = Compiler.javac().compile(
            JavaFileObjects.forSourceString("com.example.a.Alpha",
                "package com.example.a; public class Alpha {}"),
            JavaFileObjects.forSourceString("com.example.b.Beta",
                "package com.example.b; import com.example.a.Alpha; public class Beta { Alpha a; }"));

        assertThat(result.succeeded()).isTrue();
        assertThat(result.errors().toList()).isEmpty();
    }

    @Test
    void classpathEntriesArePickedUp() {
        final String assertjJar = findJarOnClasspath("assertj");
        Assumptions.assumeTrue(assertjJar != null, "assertj not found on java.class.path; skipping classpath test");

        final Compilation result = Compiler.javac()
            .withClasspath(Path.of(assertjJar))
            .compile(JavaFileObjects.forSourceString("com.example.UsesAssertJ",
                "package com.example;"
                    + " import org.assertj.core.api.Assertions;"
                    + " public class UsesAssertJ {"
                    + "   public void check(final boolean b) { Assertions.assertThat(b).isTrue(); }"
                    + " }"));

        assertThat(result.errors().toList()).isEmpty();
    }

    @Test
    void diagnosticsDoNotLeakToStderr() {
        final PrintStream originalErr = System.err;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));
        try {
            Compiler.javac().compile(
                JavaFileObjects.forSourceString("com.example.Bad",
                    "package com.example; public class Bad { SYNTAX ERROR }"));
        } finally {
            System.setErr(originalErr);
        }
        assertThat(captured.toString()).isEmpty();
    }

    @Test
    void forSourceLinesJoinsWithNewlines() {
        final Compilation result = Compiler.javac().compile(
            JavaFileObjects.forSourceLines("com.example.Multiline",
                "package com.example;",
                "public class Multiline {",
                "    public int value() { return 42; }",
                "}"));

        assertThat(result.succeeded()).isTrue();
    }

    @Test
    void generatedFileIsAccessibleByLocationPackageAndName() {
        final AtomicBoolean didGenerate = new AtomicBoolean(false);

        final AbstractProcessor processor = new AbstractProcessor() {
            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Set.of("*");
            }

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latestSupported();
            }

            @Override
            public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                if (!roundEnv.processingOver() && didGenerate.compareAndSet(false, true)) {
                    try {
                        final var resource = processingEnv.getFiler()
                            .createResource(StandardLocation.CLASS_OUTPUT, "com.example", "generated.txt");
                        try (final PrintWriter w = new PrintWriter(resource.openWriter())) {
                            w.print("hello");
                        }
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return false;
            }
        };

        final Compilation result = Compiler.javac()
            .withProcessors(processor)
            .compile(JavaFileObjects.forSourceString("com.example.Trigger",
                "package com.example; public class Trigger {}"));

        assertThat(result.succeeded()).isTrue();
        final Optional<JavaFileObject> resource = result.generatedFile(
            StandardLocation.CLASS_OUTPUT, "com.example", "generated.txt");
        assertThat(resource).isPresent();
    }

    private static String findJarOnClasspath(final String fragment) {
        final String cp = System.getProperty("java.class.path", "");
        for (final String entry : cp.split(File.pathSeparator)) {
            if (entry.contains(fragment)) {
                return entry;
            }
        }
        return null;
    }
}
