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
package gervill.soundbanks;

import gervill.com.sun.media.sound.ModelByteBuffer;
import gervill.javax.sound.sampled.AudioFormat;

/**
 * Soundfont sample storage.
 *
 * @author Karl Helgason
 */
final class SF2Sample {

    private final long startLoop;
    private final long endLoop;
    private final int originalPitch;
    private final byte pitchCorrection;
    private final ModelByteBuffer data;
    private final ModelByteBuffer data24;
    private final AudioFormat audioFormat;

    SF2Sample(ModelByteBuffer data, ModelByteBuffer data24, long startLoop, long endLoop, long sampleRate, int originalPitch, byte pitchCorrection) {
        super();
        this.startLoop = startLoop;
        this.endLoop = endLoop;
        this.originalPitch = originalPitch;
        this.pitchCorrection = pitchCorrection;
        this.data = data;
        this.data24 = data24;
        this.audioFormat = new AudioFormat(sampleRate, 16, 1, true);
    }

    SF2Sample(byte[] data, long endLoop, long sampleRate, int originalPitch, byte pitchCorrection) {
        this(new ModelByteBuffer(data), null, 256, endLoop, sampleRate, originalPitch, pitchCorrection);
    }

    SF2Sample(byte[] data, long endLoop, long sampleRate, int originalPitch) {
        this(data, endLoop, sampleRate, originalPitch, (byte)0);
    }

    ModelByteBuffer getDataBuffer() {
        return data;
    }

    ModelByteBuffer getData24Buffer() {
        return data24;
    }

    AudioFormat getFormat() {
        return audioFormat;
    }

    long getEndLoop() {
        return endLoop;
    }

    int getOriginalPitch() {
        return originalPitch;
    }

    long getStartLoop() {
        return startLoop;
    }

    byte getPitchCorrection() {
        return pitchCorrection;
    }

}
