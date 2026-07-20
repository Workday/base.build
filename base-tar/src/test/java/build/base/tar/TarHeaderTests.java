package build.base.tar;

/*-
 * #%L
 * base.build Tar
 * %%
 * Copyright (C) 2026 Workday Inc
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TarHeader}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class TarHeaderTests {

    @Test
    void shouldProduceExactly512Bytes() {
        final var header = TarHeader.createHeader("file.txt", 1024, 0, false, 0644);
        assertThat(header.toBytes()).hasSize(512);
    }

    @Test
    void shouldWriteEntryName() {
        final var header = TarHeader.createHeader("hello.txt", 0, 0, false, 0644);
        final var bytes = header.toBytes();
        final var name = new String(bytes, 0, 9, StandardCharsets.US_ASCII);
        assertThat(name).isEqualTo("hello.txt");
    }

    @Test
    void shouldWriteFileTypeflag() {
        final var header = TarHeader.createHeader("file.txt", 100, 0, false, 0644);
        assertThat(header.toBytes()[156]).isEqualTo((byte) '0');
    }

    @Test
    void shouldWriteDirectoryTypeflag() {
        final var header = TarHeader.createHeader("dir/", 0, 0, true, 0755);
        assertThat(header.toBytes()[156]).isEqualTo((byte) '5');
    }

    @Test
    void shouldWriteZeroSizeForDirectory() {
        // size passed to createHeader is ignored for directories
        final var header = TarHeader.createHeader("dir/", 999, 0, true, 0755);
        final var bytes = header.toBytes();
        final var sizeField = new String(bytes, 124, 11, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(sizeField, 8)).isEqualTo(0);
    }

    @Test
    void shouldWriteFileSizeInOctal() {
        final var header = TarHeader.createHeader("file.bin", 1024, 0, false, 0644);
        final var bytes = header.toBytes();
        final var sizeField = new String(bytes, 124, 11, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(sizeField, 8)).isEqualTo(1024);
    }

    @Test
    void shouldWriteUstarMagic() {
        final var header = TarHeader.createHeader("file.txt", 0, 0, false, 0644);
        final var bytes = header.toBytes();
        final var magic = new String(bytes, 257, 5, StandardCharsets.US_ASCII);
        assertThat(magic).isEqualTo("ustar");
    }

    @Test
    void shouldWriteUstarVersion() {
        final var header = TarHeader.createHeader("file.txt", 0, 0, false, 0644);
        final var bytes = header.toBytes();
        assertThat(bytes[263]).isEqualTo((byte) '0');
        assertThat(bytes[264]).isEqualTo((byte) '0');
    }

    @Test
    void shouldWriteNonZeroChecksum() {
        final var header = TarHeader.createHeader("file.txt", 1024, 0, false, 0644);
        final var bytes = header.toBytes();
        final var checksumField = new String(bytes, 148, 7, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(checksumField, 8)).isGreaterThan(0);
    }

    @Test
    void shouldSplitLongNameIntoPrefixAndNameFields() {
        // 150-char path: 49-char prefix + '/' + 100-char name
        final var prefix = "a".repeat(49);
        final var name = "b".repeat(100);
        final var longName = prefix + "/" + name;

        final var header = TarHeader.createHeader(longName, 0, 0, false, 0644);
        final var bytes = header.toBytes();

        final var nameField = new String(bytes, 0, 100, StandardCharsets.US_ASCII).replace("\0", "");
        final var prefixField = new String(bytes, 345, 155, StandardCharsets.US_ASCII).replace("\0", "");

        assertThat(prefixField).isEqualTo(prefix);
        assertThat(nameField).isEqualTo(name);
    }

    @Test
    void shouldSplitPrefixAndNameOnPathSeparatorPerPosixSpec() {
        // POSIX ustar spec: when the prefix field is used, a compliant reader
        // reconstructs the full pathname as `prefix + "/" + name` (the separator is
        // implicit and not stored in either field). The split must therefore land
        // exactly on a '/' in the original path, not at an arbitrary character offset.
        final var name = "x".repeat(30) + "/" + "y".repeat(90);
        assertThat(name).hasSize(121);
        assertThat(name.charAt(name.length() - 100)).isNotEqualTo('/');

        final var header = TarHeader.createHeader(name, 0, 0, false, 0644);
        final var bytes = header.toBytes();

        final var nameField = new String(bytes, 0, 100, StandardCharsets.US_ASCII).replace("\0", "");
        final var prefixField = new String(bytes, 345, 155, StandardCharsets.US_ASCII).replace("\0", "");

        assertThat(prefixField + "/" + nameField).isEqualTo(name);
    }

    @Test
    void shouldWriteSymlinkTypeflagAndLinkName() {
        final var header = TarHeader.createSymlinkHeader("link.txt", "target.txt", 0, 0777);
        final var bytes = header.toBytes();

        assertThat(bytes[156]).isEqualTo((byte) '2');
        final var linkNameField = new String(bytes, 157, 100, StandardCharsets.UTF_8).replace("\0", "");
        assertThat(linkNameField).isEqualTo("target.txt");
    }

    @Test
    void shouldWriteHardlinkTypeflagAndLinkName() {
        final var header = TarHeader.createHardlinkHeader("link.txt", "target.txt", 0, 0644);
        final var bytes = header.toBytes();

        assertThat(bytes[156]).isEqualTo((byte) '1');
        final var linkNameField = new String(bytes, 157, 100, StandardCharsets.UTF_8).replace("\0", "");
        assertThat(linkNameField).isEqualTo("target.txt");
    }

    @Test
    void shouldRoundTripHardlinkThroughParseHeader() {
        final var header = TarHeader.createHardlinkHeader("link.txt", "target.txt", 0, 0644);
        final var parsed = TarHeader.parseHeader(header.toBytes());

        assertThat(parsed.name()).isEqualTo("link.txt");
        assertThat(parsed.linkName()).isEqualTo("target.txt");
        assertThat(parsed.hardlink()).isTrue();
        assertThat(parsed.directory()).isFalse();
    }

    @Test
    void shouldRoundTripSymlinkThroughParseHeader() {
        final var header = TarHeader.createSymlinkHeader("link.txt", "target.txt", 0, 0777);
        final var parsed = TarHeader.parseHeader(header.toBytes());

        assertThat(parsed.name()).isEqualTo("link.txt");
        assertThat(parsed.linkName()).isEqualTo("target.txt");
        assertThat(parsed.directory()).isFalse();
    }

    @Test
    void shouldRoundTripNonAsciiNameThroughParseHeader() {
        final var header = TarHeader.createHeader("café-ü.txt", 0, 0, false, 0644);
        final var parsed = TarHeader.parseHeader(header.toBytes());

        assertThat(parsed.name()).isEqualTo("café-ü.txt");
    }

    @Test
    void shouldRoundTripOwnershipFields() {
        final var header = TarHeader.createHeader("file.txt", 0, 0, false, 0644, 1001, 1002, "alice", "staff");
        final var parsed = TarHeader.parseHeader(header.toBytes());

        assertThat(parsed.uid()).isEqualTo(1001);
        assertThat(parsed.gid()).isEqualTo(1002);
        assertThat(parsed.userName()).isEqualTo("alice");
        assertThat(parsed.groupName()).isEqualTo("staff");
    }

    @Test
    void shouldRoundTripSizeAboveOctalFieldLimitUsingBase256() {
        // 11-digit octal size field tops out just under 8GB; this exceeds that
        final var hugeSize = 10_000_000_000L;
        final var header = TarHeader.createHeader("huge.bin", hugeSize, 0, false, 0644);
        final var bytes = header.toBytes();

        // GNU base-256 extension: high bit of the size field's first byte is set
        assertThat(bytes[124] & 0x80).isNotZero();

        final var parsed = TarHeader.parseHeader(bytes);
        assertThat(parsed.size()).isEqualTo(hugeSize);
    }

    @Test
    void shouldNeedPaxExtensionForNameUnder100CharsButOver100Utf8Bytes() {
        // 90 CJK characters: 90 UTF-16 chars (<= 100) but 270 UTF-8 bytes (> 100).
        // nameNeedsPaxExtension() must key off byte length, not char length.
        final var name = "中".repeat(90);
        assertThat(name.length()).isLessThanOrEqualTo(100);
        assertThat(name.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(100);

        final var header = TarHeader.createHeader(name, 0, 0, false, 0644);

        assertThat(header.nameNeedsPaxExtension()).isTrue();
    }

    @Test
    void shouldRejectRatherThanSilentlyTruncateNameUnder100CharsButOver100Utf8Bytes() {
        // Same shape as above, but exercised through toBytes() directly: it must fail loudly
        // like any other unsplittable long name, rather than have writeString() truncate the
        // encoded bytes to fit the 100-byte name field and silently corrupt the entry name.
        final var name = "中".repeat(90);
        final var header = TarHeader.createHeader(name, 0, 0, false, 0644);

        assertThatThrownBy(header::toBytes)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("too long");
    }

    @Test
    void shouldNeedPaxExtensionForLinkNameUnder100CharsButOver100Utf8Bytes() {
        final var target = "中".repeat(90);
        assertThat(target.length()).isLessThanOrEqualTo(100);
        assertThat(target.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(100);

        final var header = TarHeader.createSymlinkHeader("link.txt", target, 0, 0777);

        assertThat(header.linkNameNeedsPaxExtension()).isTrue();
    }

    @Test
    void shouldRejectRatherThanOverflowPrefixSplitNameFieldWithMultibyteCharacters() {
        // 60 CJK characters fit the char-based prefix/name split (suffixLength <= 100 chars),
        // but encode to 180 UTF-8 bytes, overflowing the 100-byte name field the split assumed.
        // No valid byte-respecting split exists here, so this must be rejected like any other
        // unsplittable long name rather than silently overflow the name field.
        final var prefix = "a".repeat(50);
        final var name = "中".repeat(60);
        final var longName = prefix + "/" + name;

        final var header = TarHeader.createHeader(longName, 0, 0, false, 0644);

        assertThatThrownBy(header::toBytes)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("too long");
    }

    @Test
    void shouldSplitPrefixAndNameOnPathSeparatorRespectingUtf8ByteWidths() {
        // The name half is 90 CJK chars (270 bytes) if measured wrong, but split correctly
        // here since it's under the 100-byte limit on its own; verifies the split still works
        // once it is UTF-8-byte-aware, not just that oversized cases are rejected.
        final var prefix = "a".repeat(50);
        final var name = "中".repeat(30);
        final var longName = prefix + "/" + name;

        final var header = TarHeader.createHeader(longName, 0, 0, false, 0644);
        final var bytes = header.toBytes();

        final var nameField = new String(bytes, 0, 100, StandardCharsets.UTF_8).replace("\0", "");
        final var prefixField = new String(bytes, 345, 155, StandardCharsets.UTF_8).replace("\0", "");
        assertThat(prefixField).isEqualTo(prefix);
        assertThat(nameField).isEqualTo(name);
    }

    @Test
    void shouldWriteSparseTypeflagAndRealSize() {
        final var chunks = List.of(new TarHeader.SparseChunk(0, 100), new TarHeader.SparseChunk(1000, 50));
        final var header = TarHeader.createSparseHeader("sparse.bin", 2000, chunks, 0, 0644);
        final var bytes = header.toBytes();

        assertThat(bytes[156]).isEqualTo((byte) 'S');
        // size field (offset 124) holds the stored (chunk) bytes, not the logical size
        final var storedSizeField = new String(bytes, 124, 11, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(storedSizeField, 8)).isEqualTo(150);
        // realsize field (offset 483) holds the logical size
        final var realSizeField = new String(bytes, 483, 11, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(realSizeField, 8)).isEqualTo(2000);
        // isextended (offset 482) is unset since both chunks fit in the main block
        assertThat(bytes[482]).isEqualTo((byte) 0);
        assertThat(header.sparseExtensionBlocks()).isEmpty();
    }

    @Test
    void shouldRoundTripSparseHeaderWithFewChunksThroughParseHeader() {
        final var chunks = List.of(new TarHeader.SparseChunk(0, 100), new TarHeader.SparseChunk(1000, 50));
        final var header = TarHeader.createSparseHeader("sparse.bin", 2000, chunks, 0, 0644);
        final var mainBlock = header.toBytes();

        assertThat(TarHeader.sparseMainBlockIsExtended(mainBlock)).isFalse();
        final var parsed = TarHeader.parseSparseMainBlock(mainBlock);

        assertThat(parsed.name()).isEqualTo("sparse.bin");
        assertThat(parsed.size()).isEqualTo(2000);
        assertThat(parsed.sparseChunks()).isEqualTo(chunks);
        assertThat(parsed.directory()).isFalse();
    }

    @Test
    void shouldRequireExtensionBlocksForMoreThanFourSparseChunks() {
        final var chunks = new ArrayList<TarHeader.SparseChunk>();
        for (int i = 0; i < 30; i++) {
            chunks.add(new TarHeader.SparseChunk(i * 1000L, 10));
        }
        final var header = TarHeader.createSparseHeader("sparse.bin", 30_000, chunks, 0, 0644);
        final var mainBlock = header.toBytes();

        assertThat(TarHeader.sparseMainBlockIsExtended(mainBlock)).isTrue();
        final var extensionBlocks = header.sparseExtensionBlocks();
        // 30 total chunks - 4 in the main block = 26 remaining, 21 per extension block
        assertThat(extensionBlocks).hasSize(2);

        var parsed = TarHeader.parseSparseMainBlock(mainBlock);
        for (final var block : extensionBlocks) {
            parsed = parsed.withAdditionalSparseChunks(TarHeader.parseSparseExtensionBlock(block));
        }

        assertThat(parsed.sparseChunks()).isEqualTo(chunks);
        assertThat(TarHeader.sparseExtensionBlockIsExtended(extensionBlocks.get(0))).isTrue();
        assertThat(TarHeader.sparseExtensionBlockIsExtended(extensionBlocks.get(1))).isFalse();
    }

    @Test
    void shouldRejectNameThatIsTooLong() {
        // no '/' anywhere in the name, so no valid prefix/name split exists
        final var tooLong = "a".repeat(256);
        final var header = TarHeader.createHeader(tooLong, 0, 0, false, 0644);
        assertThatThrownBy(header::toBytes)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("too long");
    }
}
