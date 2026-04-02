package build.base.archiving;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JarBuilder}.
 *
 * @author brian.oliver
 * @since Jul-2021
 */
public class JarBuilderTests {

    /**
     * The {@link Path} to the resources directory.
     */
    private Path resourcesPath;

    @BeforeEach
    void setup() {

        // locate the test resources directory (based on looking up the "welcome.txt" file)
        this.resourcesPath = Paths.get(getClass().getClassLoader().getResource("welcome.txt").getFile())
            .getParent();

        // ensure that it exists
        assertThat(Files.exists(this.resourcesPath))
            .isTrue();
    }

    /**
     * Creates a pre-configured {@link JarBuilder} for testing.
     *
     * @return a new {@link JarBuilder}
     */
    private JarBuilder createArchiveBuilder() {
        return new JarBuilder();
    }

    /**
     * Ensure the {@link JarBuilder} can build an empty Java Archive.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnEmptyArchive(@TempDir final Path temporaryFolder)
        throws IOException {

        final var builder = createArchiveBuilder();

        final var archive = temporaryFolder.resolve("empty.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the META-INF/MANIFEST.MF is present
            final var entry = jarFile.stream()
                .findFirst()
                .orElseThrow(
                    () -> new AssertionError("Java Archive is empty.  It should contain a single entry"));

            assertThat(entry.getName())
                .isEqualTo("META-INF/MANIFEST.MF");

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(2);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build a Java Archive containing the standard main manifest attributes.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveWithMainManifestAttributes(@TempDir final Path temporaryFolder)
        throws IOException {

        final var builder = createArchiveBuilder();

        builder.withManifestVersion("1.0.0")
            .withImplementationTitle("Implementation")
            .withImplementationVendor("Workday")
            .withImplementationVersion("2.0.0")
            .withMainClass(JarBuilder.class)
            .withSignatureVersion("3.0.0")
            .withSpecificationVendor("Acme")
            .withSpecificationVersion("4.0.0")
            .withSpecificationTitle("Specification");

        final var archive = temporaryFolder.resolve("empty.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the META-INF/MANIFEST.MF is present
            final var entry = jarFile.stream()
                .findFirst()
                .orElseThrow(
                    () -> new AssertionError("Java Archive is empty.  It should contain a single entry"));

            assertThat(entry.getName())
                .isEqualTo("META-INF/MANIFEST.MF");

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(2);

            // ensure the manifest contains the prescribed values
            final var manifest = jarFile.getManifest();
            final var attributes = manifest.getMainAttributes();

            assertThat(attributes.size())
                .isEqualTo(10);

            assertThat(attributes.get(Attributes.Name.MANIFEST_VERSION))
                .isEqualTo("1.0.0");

            assertThat(attributes.get(Attributes.Name.IMPLEMENTATION_TITLE))
                .isEqualTo("Implementation");

            assertThat(attributes.get(Attributes.Name.IMPLEMENTATION_VENDOR))
                .isEqualTo("Workday");

            assertThat(attributes.get(Attributes.Name.IMPLEMENTATION_VERSION))
                .isEqualTo("2.0.0");

            assertThat(attributes.get(Attributes.Name.MAIN_CLASS))
                .isEqualTo(JarBuilder.class.getName());

            assertThat(attributes.get(Attributes.Name.SIGNATURE_VERSION))
                .isEqualTo("3.0.0");

            assertThat(attributes.get(Attributes.Name.SPECIFICATION_VENDOR))
                .isEqualTo("Acme");

            assertThat(attributes.get(Attributes.Name.SPECIFICATION_VERSION))
                .isEqualTo("4.0.0");

            assertThat(attributes.get(Attributes.Name.SPECIFICATION_TITLE))
                .isEqualTo("Specification");

            // ensure the Created-By attribute contains the java.version and java.vendor
            // (when the manifest entries are not empty, this main attributed is automatically included)
            final var createdBy = (String) attributes.get(new Attributes.Name("Created-By"));
            assertThat(createdBy)
                .contains(System.getProperty("java.version"));

            assertThat(createdBy)
                .contains(System.getProperty("java.vendor"));
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a {@link Class} resource.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingAClass(@TempDir final Path temporaryFolder)
        throws IOException {

        // we'll package the JavaArchiveBuilder itself in the Java Archive
        final var classToInclude = JarBuilder.class;

        final var builder = createArchiveBuilder();

        builder.content().add(classToInclude);

        final Path archive = temporaryFolder.resolve("single-class.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the class is present
            final var entry = jarFile.stream()
                .filter(e -> e.getName().contains(classToInclude.getSimpleName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Class not found in the archive"));

            assertThat(entry.getName())
                .isEqualTo(classToInclude.getName().replace(".", "/") + ".class");

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(6);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a {@link File} resource,
     * in the root directory.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingASingleFileInTheRootDirectory(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var fileName = "welcome.txt";

        final var path = this.resourcesPath.resolve(fileName);

        assertThat(Files.exists(path))
            .isTrue();

        final var builder = createArchiveBuilder();

        builder.content().add(path);

        final var archive = temporaryFolder.resolve("single-file.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the file is present
            jarFile.stream()
                .filter(e -> e.getName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(3);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a renamed {@link File} resource,
     * in the root directory.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingASingleRenamedFileInTheRootDirectory(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var fileName = "welcome.txt";

        final var path = this.resourcesPath.resolve(fileName);

        assertThat(Files.exists(path))
            .isTrue();

        final var builder = createArchiveBuilder();

        final var renamed = "hello.txt";
        builder.content().add(renamed, path);

        final var archive = temporaryFolder.resolve("single-renamed-file.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the file is present
            jarFile.stream()
                .filter(e -> e.getName().equals(renamed))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(3);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive copying a directory in the root directory.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveCopyingADirectoryIntoTheRootDirectory(@TempDir final Path temporaryFolder)
        throws IOException {

        // the directory to include
        final var directoryName = "child/";

        final var path = this.resourcesPath.resolve("parent").resolve(directoryName);

        assertThat(Files.exists(path))
            .isTrue();

        assertThat(Files.isDirectory(path))
            .isTrue();

        final var builder = createArchiveBuilder();

        builder.content().add(directoryName, path);

        final var archive = temporaryFolder.resolve("single-directory.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the file is present
            jarFile.stream()
                .filter(e -> e.getName().equals("child/"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Directory resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(4);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing {@link File} resources,
     * in numerous directories through navigation.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingUsingNavigation(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var fileName = "welcome.txt";

        final var path = this.resourcesPath.resolve(fileName);

        assertThat(Files.exists(path))
            .isTrue();

        final var builder = createArchiveBuilder();

        builder.in("MANIFEST-INF").add(path).in("..").add(path);
        builder.in("MANIFEST-INF").in("services").add(path).in("../..").add(path);
        builder.in("MANIFEST-INF").in("services").in("/").add(path);
        builder.in("MANIFEST-INF/services/").in("/").add(path);
        builder.in(".").add(path);
        builder.in("/").add(path);
        builder.in("MANIFEST-INF/services/").in("..").in("components").add(path);
        builder.in("MANIFEST-INF").in("..").in("MANIFEST-INF").add(path);

        final var archive = temporaryFolder.resolve("navigation.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(9);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a directory of nested resources.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingDirectoryOfNestedResources(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var directoryName = "parent";

        final var directory = this.resourcesPath.resolve(directoryName);

        assertThat(Files.exists(directory))
            .isTrue();

        final var builder = createArchiveBuilder();

        builder.content().add(directoryName, directory);

        final var archive = temporaryFolder.resolve("multiple-directories.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the directory is present
            jarFile.stream()
                .filter(e -> e.getName().equals("parent/"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(8);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a renamed directory of nested resources.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingAnRenamedDirectoryOfNestedResources(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var directoryName = "parent";

        final var directory = this.resourcesPath.resolve(directoryName);

        assertThat(Files.exists(directory))
            .isTrue();

        final var builder = createArchiveBuilder();

        builder.content().add("renamed", directory);

        final var archive = temporaryFolder.resolve("renamed-with-multiple-directories.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the directory is present
            jarFile.stream()
                .filter(e -> e.getName().equals("renamed/"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(8);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a renamed {@link File} resource,
     * containing a path.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingAFileInASpecifiedDirectory(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var fileName = "welcome.txt";

        final var path = this.resourcesPath.resolve(fileName);

        assertThat(Files.exists(path))
            .isTrue();

        final var builder = createArchiveBuilder();

        final var newPath = "META-INF/hello.txt";
        builder.content().add(newPath, path);

        final var archive = temporaryFolder.resolve("single-file-into-a-path.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the file is present
            jarFile.stream()
                .filter(e -> e.getName().equals(newPath))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(3);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }

    /**
     * Ensure the {@link JarBuilder} can build an Java Archive containing a relocated directory of nested resources.
     *
     * @throws IOException should building or reading the Java Archive fail
     */
    @Test
    void shouldCreateAnArchiveContainingAnRenamedAndRelocatedDirectoryOfNestedResources(@TempDir final Path temporaryFolder)
        throws IOException {

        // the file to include
        final var directoryName = "parent";

        final var directory = this.resourcesPath.resolve(directoryName);

        assertThat(Files.exists(directory))
            .isTrue();

        final var builder = createArchiveBuilder();

        builder.content().add("META-INF/hidden/", directory);

        final var archive = temporaryFolder.resolve("relocated-with-multiple-directories.jar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();

        try (var jarFile = new JarFile(archive.toFile())) {
            // ensure the directory is present
            jarFile.stream()
                .filter(e -> e.getName().equals("META-INF/hidden/"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource not found in the archive"));

            // ensure there are the expected entries in the archive (includes the directories we expect to be created)
            assertThat(jarFile.stream())
                .hasSize(8);

            // ensure the manifest is empty
            final var manifest = jarFile.getManifest();

            assertThat(manifest.getMainAttributes())
                .isEmpty();
        }
    }
}
