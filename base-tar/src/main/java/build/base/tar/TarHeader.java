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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Represents a POSIX ustar tar entry header (512 bytes).
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public record TarHeader(String name, long size, long modTime, boolean directory, int permissions) {

    /**
     * The size of a tar header/block in bytes.
     */
    public static final int BLOCK_SIZE = 512;

    /**
     * The ustar magic string.
     */
    private static final String USTAR_MAGIC = "ustar";

    /**
     * The ustar version.
     */
    private static final String USTAR_VERSION = "00";

    /**
     * Creates a {@link TarHeader} for the specified entry.
     *
     * @param name        the entry name
     * @param size        the entry size in bytes (0 for directories)
     * @param modTime     the modification time in seconds since epoch
     * @param directory   whether the entry is a directory
     * @param permissions the POSIX permissions (e.g. 0755)
     * @return a new {@link TarHeader}
     */
    public static TarHeader createHeader(final String name,
                                         final long size,
                                         final long modTime,
                                         final boolean directory,
                                         final int permissions) {
        return new TarHeader(name, size, modTime, directory, permissions);
    }

    /**
     * Serializes this header into a 512-byte ustar header block.
     *
     * @return the 512-byte header block
     */
    byte[] toBytes() {
        final var header = new byte[BLOCK_SIZE];
        Arrays.fill(header, (byte) 0);

        // determine name and prefix
        final String entryName;
        final String prefix;

        if (name.length() > 100) {
            // split into prefix (up to 155) and name (up to 100)
            final int splitIndex = name.length() - 100;
            if (splitIndex > 155) {
                throw new IllegalArgumentException("Entry name too long: " + name);
            }
            prefix = name.substring(0, splitIndex);
            entryName = name.substring(splitIndex);
        } else {
            entryName = name;
            prefix = "";
        }

        // name (offset 0, 100 bytes)
        writeString(header, 0, 100, entryName);

        // mode (offset 100, 8 bytes)
        writeOctal(header, 100, 8, permissions);

        // uid (offset 108, 8 bytes)
        writeOctal(header, 108, 8, 0);

        // gid (offset 116, 8 bytes)
        writeOctal(header, 116, 8, 0);

        // size (offset 124, 12 bytes)
        writeOctal(header, 124, 12, directory ? 0 : size);

        // mtime (offset 136, 12 bytes)
        writeOctal(header, 136, 12, modTime);

        // checksum placeholder (offset 148, 8 bytes) - fill with spaces for calculation
        Arrays.fill(header, 148, 156, (byte) ' ');

        // typeflag (offset 156, 1 byte)
        header[156] = directory ? (byte) '5' : (byte) '0';

        // linkname (offset 157, 100 bytes) - empty

        // magic (offset 257, 6 bytes)
        writeString(header, 257, 6, USTAR_MAGIC);

        // version (offset 263, 2 bytes)
        header[263] = '0';
        header[264] = '0';

        // uname (offset 265, 32 bytes)
        writeString(header, 265, 32, "");

        // gname (offset 297, 32 bytes)
        writeString(header, 297, 32, "");

        // devmajor (offset 329, 8 bytes)
        writeOctal(header, 329, 8, 0);

        // devminor (offset 337, 8 bytes)
        writeOctal(header, 337, 8, 0);

        // prefix (offset 345, 155 bytes)
        writeString(header, 345, 155, prefix);

        // compute checksum
        long checksum = 0;
        for (final byte b : header) {
            checksum += (b & 0xFF);
        }
        // write checksum (offset 148, 8 bytes) as 6 octal digits + null + space
        final var checksumStr = String.format("%06o\0 ", checksum);
        final var checksumBytes = checksumStr.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(checksumBytes, 0, header, 148, Math.min(checksumBytes.length, 8));

        return header;
    }

    /**
     * Writes a string into the header at the specified offset.
     *
     * @param header the header buffer
     * @param offset the offset
     * @param length the field length
     * @param value  the string value
     */
    private static void writeString(final byte[] header,
                                    final int offset,
                                    final int length,
                                    final String value) {
        final var bytes = value.getBytes(StandardCharsets.US_ASCII);
        final var copyLen = Math.min(bytes.length, length);
        System.arraycopy(bytes, 0, header, offset, copyLen);
    }

    /**
     * Writes an octal value into the header at the specified offset.
     *
     * @param header the header buffer
     * @param offset the offset
     * @param length the field length
     * @param value  the numeric value
     */
    private static void writeOctal(final byte[] header,
                                   final int offset,
                                   final int length,
                                   final long value) {
        // format as octal string, null-terminated, right-aligned with leading zeros
        final var octal = String.format("%0" + (length - 1) + "o", value);
        final var bytes = octal.getBytes(StandardCharsets.US_ASCII);
        final var copyLen = Math.min(bytes.length, length - 1);
        System.arraycopy(bytes, 0, header, offset, copyLen);
        // null terminator is already there from Arrays.fill
    }
}
