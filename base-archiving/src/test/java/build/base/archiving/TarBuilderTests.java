package build.base.archiving;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link TarBuilder}s.
 *
 * @author brian.oliver
 * @since Jul-2021
 */
class TarBuilderTests {

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
     * Creates a pre-configured {@link TarBuilder} for testing.
     *
     * @return a new {@link TarBuilder}
     */
    private TarBuilder createArchiveBuilder() {
        return new TarBuilder();
    }

    /**
     * Ensure the {@link TarBuilder} can build an empty archive.
     *
     * @throws IOException should building or reading the archive fail
     */
    @Test
    void shouldCreateAnEmptyArchive(@TempDir final Path temporaryFolder)
        throws IOException {

        final var builder = createArchiveBuilder();

        final var archive = temporaryFolder.resolve("empty.tar");

        assertThat(Files.exists(archive))
            .isFalse();

        builder.build(archive);

        assertThat(Files.exists(archive))
            .isTrue();
    }

    /**
     * Ensure the {@link TarBuilder} can build an archive containing a {@link File} resource,
     * in the root directory.
     *
     * @throws IOException should building or reading the archive fail
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
    }

    /**
     * Ensure the {@link TarBuilder} can build an archive containing a directory of nested resources.
     *
     * @throws IOException should building or reading the archive fail
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
    }
}
