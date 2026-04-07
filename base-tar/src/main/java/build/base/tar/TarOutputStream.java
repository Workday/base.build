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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that writes entries in POSIX ustar tar format.
 * <p>
 * Usage: create a {@link TarOutputStream} wrapping an existing {@link OutputStream},
 * then repeatedly call {@link #putNextEntry(TarEntry)} followed by {@link #write(byte[], int, int)}
 * for file data, and finally {@link #close()} to finalize the archive.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public final class TarOutputStream extends OutputStream {

    /**
     * The block size for tar archives.
     */
    private static final int BLOCK_SIZE = TarHeader.BLOCK_SIZE;

    /**
     * The underlying output stream.
     */
    private final OutputStream out;

    /**
     * The number of data bytes written for the current entry.
     */
    private long bytesWritten;

    /**
     * Constructs a {@link TarOutputStream} wrapping the specified {@link OutputStream}.
     *
     * @param out the underlying {@link OutputStream}
     */
    public TarOutputStream(final OutputStream out) {
        this.out = out;
        this.bytesWritten = 0;
    }

    /**
     * Writes the header for the next tar entry. If a previous entry had file data,
     * any required padding to a 512-byte boundary is written first.
     *
     * @param entry the {@link TarEntry} to write
     * @throws IOException if an I/O error occurs
     */
    public void putNextEntry(final TarEntry entry) throws IOException {
        // pad the previous entry to a block boundary if needed
        padCurrentEntry();

        // write the header block
        final var headerBytes = entry.header().toBytes();
        out.write(headerBytes);

        // reset byte counter
        bytesWritten = 0;
    }

    @Override
    public void write(final int b) throws IOException {
        out.write(b);
        bytesWritten++;
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        out.write(b, off, len);
        bytesWritten += len;
    }

    @Override
    public void close() throws IOException {
        // pad the last entry
        padCurrentEntry();

        // write two 512-byte zero blocks to mark end of archive
        final var zeroBlock = new byte[BLOCK_SIZE];
        out.write(zeroBlock);
        out.write(zeroBlock);

        out.flush();
        out.close();
    }

    /**
     * Pads the current entry's data to a 512-byte block boundary.
     *
     * @throws IOException if an I/O error occurs
     */
    private void padCurrentEntry() throws IOException {
        if (bytesWritten > 0) {
            final var remainder = (int) (bytesWritten % BLOCK_SIZE);
            if (remainder != 0) {
                final var padding = BLOCK_SIZE - remainder;
                out.write(new byte[padding]);
            }
        }
    }
}
