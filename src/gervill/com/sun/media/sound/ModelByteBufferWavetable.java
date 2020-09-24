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
import gervill.javax.sound.sampled.AudioFormat.Encoding;
import gervill.javax.sound.sampled.AudioInputStream;
import own.main.ImmutableList;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wavetable oscillator for pre-loaded data.
 *
 * @author Karl Helgason
 */
public final class ModelByteBufferWavetable {

    public static final int LOOP_TYPE_OFF = 0;
    public static final int LOOP_TYPE_FORWARD = 1;
    public static final int LOOP_TYPE_RELEASE = 2;

    private class Buffer8PlusInputStream extends InputStream {

        private final int framesize_pc;
        int pos = 0;
        int pos2 = 0;
        int markpos = 0;
        int markpos2 = 0;

        Buffer8PlusInputStream() {
            framesize_pc = format.getFrameSize() / format.getChannels();
        }

        public int read(byte[] b, int off, int len) {
            int avail = available();
            if (avail <= 0)
                return -1;
            if (len > avail)
                len = avail;
            ImmutableList<Byte> buff1 = buffer.array();
            ImmutableList<Byte> buff2 = buffer8.array();
            pos += buffer.arrayOffset();
            pos2 += buffer8.arrayOffset();
            for (int i = 0; i < len; i += (framesize_pc + 1)) {
                b[i] = buff2.get(pos2);
                for (int j = 0; j < framesize_pc; j++) {
                    b[i + 1 + j] = buff1.get(pos + j);
                }
                pos += framesize_pc;
                pos2 += 1;
            }
            pos -= buffer.arrayOffset();
            pos2 -= buffer8.arrayOffset();
            return len;
        }

        public long skip(long n) throws IOException {
            int avail = available();
            if (avail <= 0)
                return -1;
            if (n > avail)
                n = avail;
            pos += (n / (framesize_pc + 1)) * (framesize_pc);
            pos2 += n / (framesize_pc + 1);
            return super.skip(n);
        }

        public int read(byte[] b) {
            return read(b, 0, b.length);
        }

        public int read() {
            byte[] b = new byte[1];
            int ret = read(b, 0, 1);
            if (ret == -1)
                return -1;
            return 0;
        }

        public boolean markSupported() {
            return true;
        }

        public int available() {
            return (int)buffer.capacity() + (int)buffer8.capacity() - pos - pos2;
        }

        public synchronized void mark(int readlimit) {
            markpos = pos;
            markpos2 = pos2;
        }

        public synchronized void reset() {
            pos = markpos;
            pos2 = markpos2;

        }
    }

    private final float loopStart;
    private final float loopLength;
    private final ModelByteBuffer buffer;
    private final ModelByteBuffer buffer8;
    private final AudioFormat format;
    private final float pitchcorrection;
    private final float attenuation;
    private final int loopType;

    public ModelByteBufferWavetable(ModelByteBuffer buffer, AudioFormat format, float pitchcorrection, float attenuation, int loopStart, int loopLength, int loopType, ModelByteBuffer buffer8) {
        this.format = format;
        this.buffer = buffer;
        this.pitchcorrection = pitchcorrection;
        this.attenuation = attenuation;
        this.loopStart = loopStart;
        this.loopLength = loopLength;
        this.loopType = loopType;
        this.buffer8 = buffer8;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public AudioFloatInputStream openStream() {
        if (buffer == null || format == null)
            return null;
        if (buffer.array() == null) {
            /*return AudioFloatInputStream.getInputStream(new AudioInputStream(
                    buffer.getInputStream(), format,
                    buffer.capacity() / format.getFrameSize()));*/
            throw new NullPointerException("buffer.array() is null");
        }
        if (buffer8 != null) {
            if (format.getEncoding().equals(Encoding.PCM_SIGNED)
                    || format.getEncoding().equals(Encoding.PCM_UNSIGNED)) {
                InputStream is = new Buffer8PlusInputStream();
                AudioFormat format2 = new AudioFormat(
                        format.getEncoding(),
                        format.getSampleRate(),
                        format.getSampleSizeInBits() + 8,
                        format.getChannels(),
                        format.getFrameSize() + (format.getChannels()),
                        format.getFrameRate()
                );

                AudioInputStream ais = new AudioInputStream(is, format2,
                        buffer.capacity() / format.getFrameSize());
                return AudioFloatInputStream.getInputStream(ais);
            }
        }
        return AudioFloatInputStream.getInputStream(format, buffer.array(),
                (int)buffer.arrayOffset(), (int)buffer.capacity());
    }

    public int getChannels() {
        return getFormat().getChannels();
    }

    // attenuation is in cB
    public float getAttenuation() {
        return attenuation;
    }

    public float getLoopLength() {
        return loopLength;
    }

    public float getLoopStart() {
        return loopStart;
    }

    public int getLoopType() {
        return loopType;
    }

    public float getPitchcorrection() {
        return pitchcorrection;
    }

}
