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

import gervill.javax.sound.sampled.AudioFormat;
import gervill.javax.sound.sampled.AudioInputStream;
import own.main.ImmutableList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to create AudioFloatInputStream from AudioInputStream and
 * byte buffers.
 *
 * @author Karl Helgason
 */
public abstract class AudioFloatInputStream {

    private static class BytaArrayAudioFloatInputStream
            extends AudioFloatInputStream {

        private int pos = 0;
        private int markpos = 0;
        private final AudioFloatConverter converter;
        private final AudioFormat format;
        private final byte[] buffer;
        private final int buffer_offset;
        private final int buffer_len;
        private final int framesize_pc;

        BytaArrayAudioFloatInputStream(AudioFloatConverter converter,
                byte[] buffer, int offset, int len) {
            this.converter = converter;
            this.format = converter.getFormat();
            this.buffer = buffer;
            this.buffer_offset = offset;
            framesize_pc = format.getFrameSize() / format.getChannels();
            this.buffer_len = len / framesize_pc;

        }

        public AudioFormat getFormat() {
            return format;
        }

        public int read(float[] b, int off, int len) {
            if (b == null)
                throw new NullPointerException();
            if (off < 0 || len < 0 || len > b.length - off)
                throw new IndexOutOfBoundsException();
            if (pos >= buffer_len)
                return -1;
            if (len == 0)
                return 0;
            if (pos + len > buffer_len)
                len = buffer_len - pos;
            converter.toFloatArray(buffer, buffer_offset + pos * framesize_pc,
                    b, off, len);
            pos += len;
            return len;
        }

        public void skip(long len) {
            if (pos >= buffer_len || len <= 0)
                return;
            if (pos + len > buffer_len)
                len = buffer_len - pos;
            pos += len;
        }

        public void close() {
        }

        public void mark(int readlimit) {
            markpos = pos;
        }

        public void reset() {
            pos = markpos;
        }
    }

    private static class DirectAudioFloatInputStream
            extends AudioFloatInputStream {

        private final AudioInputStream stream;
        private final AudioFloatConverter converter;
        private final int framesize_pc; // framesize / channels
        private byte[] buffer;

        DirectAudioFloatInputStream(AudioInputStream stream) {
            converter = AudioFloatConverter.getConverter(stream.getFormat());
            framesize_pc = stream.getFormat().getFrameSize() / stream.getFormat().getChannels();
            this.stream = stream;
        }

        public AudioFormat getFormat() {
            return stream.getFormat();
        }

        public int read(float[] b, int off, int len) throws IOException {
            int b_len = len * framesize_pc;
            if (buffer == null || buffer.length < b_len)
                buffer = new byte[b_len];
            int ret = stream.read(buffer, b_len);
            if (ret == -1)
                return -1;
            converter.toFloatArray(buffer, b, off, ret / framesize_pc);
            return ret / framesize_pc;
        }

        public void skip(long len) throws IOException {
            long b_len = len * framesize_pc;
            stream.skip(b_len);
        }

        public void close() throws IOException {
            stream.close();
        }

        public void mark(int readlimit) {
            stream.mark(readlimit * framesize_pc);
        }

        public void reset() throws IOException {
            stream.reset();
        }
    }

    public static AudioFloatInputStream getInputStream(
            AudioInputStream stream) {
        return new DirectAudioFloatInputStream(stream);
    }

    public static AudioFloatInputStream getInputStream(AudioFormat format, ImmutableList<Byte> buffer, int offset, int len) {
        AudioFloatConverter converter = AudioFloatConverter
                .getConverter(format);

        byte[] realBuffer = ImmutableList.toArray(buffer);
        if (converter != null)
            return new BytaArrayAudioFloatInputStream(converter, realBuffer, offset, len);

        InputStream stream = new ByteArrayInputStream(realBuffer, offset, len);
        long aLen = format.getFrameSize() == AudioInputStream.NOT_SPECIFIED
                ? AudioInputStream.NOT_SPECIFIED : len / format.getFrameSize();
        AudioInputStream astream = new AudioInputStream(stream, format, aLen);
        return new DirectAudioFloatInputStream(astream);
    }

    public abstract AudioFormat getFormat();

    public abstract int read(float[] b, int off, int len) throws IOException;

    public abstract void skip(long len) throws IOException;

    public abstract void close() throws IOException;

    public abstract void mark(int readlimit);

    public abstract void reset() throws IOException;
}
