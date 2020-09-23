/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package gervill.com.sun.media.sound;

import java.io.*;

/**
 * This class is a pointer to a binary array either in memory or on disk.
 *
 * @author Karl Helgason
 */
public final class ModelByteBuffer {

    private final ModelByteBuffer root;
    private final File file;
    private final long fileoffset;
    private final byte[] buffer;
    private final long offset;
    private final long len;

    private class RandomFileInputStream extends InputStream {

        private final RandomAccessFile raf;
        private long left;
        private long mark = 0;
        private long markleft = 0;

        RandomFileInputStream() throws IOException {
            raf = new RandomAccessFile(root.file, "r");
            raf.seek(root.fileoffset + arrayOffset());
            left = capacity();
        }

        public int available() {
            if (left > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int)left;
        }

        public synchronized void mark(int readlimit) {
            try {
                mark = raf.getFilePointer();
                markleft = left;
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        public boolean markSupported() {
            return true;
        }

        public synchronized void reset() throws IOException {
            raf.seek(mark);
            left = markleft;
        }

        public long skip(long n) throws IOException {
            if( n < 0)
                return 0;
            if (n > left)
                n = left;
            long p = raf.getFilePointer();
            raf.seek(p + n);
            left -= n;
            return n;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len > left)
                len = (int)left;
            if (left == 0)
                return -1;
            len = raf.read(b, off, len);
            if (len == -1)
                return -1;
            left -= len;
            return len;
        }

        public int read(byte[] b) throws IOException {
            int len = b.length;
            if (len > left)
                len = (int)left;
            if (left == 0)
                return -1;
            len = raf.read(b, 0, len);
            if (len == -1)
                return -1;
            left -= len;
            return len;
        }

        public int read() throws IOException {
            if (left == 0)
                return -1;
            int b = raf.read();
            if (b == -1)
                return -1;
            left--;
            return b;
        }

        public void close() throws IOException {
            raf.close();
        }
    }

    private ModelByteBuffer(ModelByteBuffer parent,
            long beginIndex, long endIndex, boolean independent) {
        long parent_len = parent.len;
        if (beginIndex < 0)
            beginIndex = 0;
        if (beginIndex > parent_len)
            beginIndex = parent_len;
        if (endIndex < 0)
            endIndex = 0;
        if (endIndex > parent_len)
            endIndex = parent_len;
        if (beginIndex > endIndex)
            beginIndex = endIndex;
        len = endIndex - beginIndex;
        if (independent) {
            buffer = parent.root.buffer;
            long offsetAux = arrayOffset(parent.root, beginIndex);
            if (parent.root.file != null) {
                file = parent.root.file;
                fileoffset = parent.root.fileoffset + offsetAux;
                offset = 0;
            } else {
                offset = offsetAux;
                file = null;
                fileoffset = 0;
            }
            root = this;
        } else {
            root = parent.root;
            file = null;
            fileoffset = 0;
            buffer = null;
            offset = beginIndex;
        }
    }

    public ModelByteBuffer(byte[] buffer) {
        this.buffer = buffer;
        this.offset = 0;
        this.len = buffer.length;
        root = this;
        file = null;
        fileoffset = 0;
    }

    public ModelByteBuffer(File file, long offset, long len) {
        this.file = file;
        this.fileoffset = offset;
        this.len = len;
        root = this;
        buffer = null;
        this.offset = 0;
    }

    public InputStream getInputStream() {
        if (root.file != null && root.buffer == null) {
            try {
                return new RandomFileInputStream();
            } catch (IOException e) {
                //e.printStackTrace();
                return null;
            }
        }
        return new ByteArrayInputStream(array(),
                (int)arrayOffset(), (int)capacity());
    }

    public ModelByteBuffer subbuffer(long beginIndex, long endIndex) {
        return subbuffer(beginIndex, endIndex, false);
    }

    public ModelByteBuffer subbuffer(long beginIndex, long endIndex,
            boolean independent) {
        return new ModelByteBuffer(this, beginIndex, endIndex, independent);
    }

    public byte[] array() {
        return root.buffer;
    }

    public long arrayOffset(ModelByteBuffer root, long offset) {
        if (root != this)
            return root.arrayOffset() + offset;
        return offset;
    }

    public long arrayOffset() {
        return arrayOffset(root, offset);
    }

    public long capacity() {
        return len;
    }

}
