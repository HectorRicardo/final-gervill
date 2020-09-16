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

import gervill.javax.sound.midi.Soundbank;
import gervill.javax.sound.midi.SoundbankResource;
import gervill.javax.sound.sampled.AudioFormat;

/**
 * Soundfont sample storage.
 *
 * @author Karl Helgason
 */
public final class SF2Sample extends SoundbankResource {

    private final long startLoop;
    private final long endLoop;
    private final long sampleRate;
    private final int originalPitch;
    private final byte pitchCorrection;
    private final ModelByteBuffer data;
    private final ModelByteBuffer data24;

    public SF2Sample(Soundbank soundBank, String name, ModelByteBuffer data, ModelByteBuffer data24, long startLoop, long endLoop, long sampleRate, int originalPitch, byte pitchCorrection) {
        super(soundBank, name);
        this.startLoop = startLoop;
        this.endLoop = endLoop;
        this.sampleRate = sampleRate;
        this.originalPitch = originalPitch;
        this.pitchCorrection = pitchCorrection;
        this.data = data;
        this.data24 = data24;
    }

    public SF2Sample(Soundbank soundBank, String name, byte[] data, long startLoop, long endLoop, long sampleRate, int originalPitch, byte pitchCorrection) {
        this(soundBank, name, new ModelByteBuffer(data), null, startLoop, endLoop, sampleRate, originalPitch, pitchCorrection);
    }

    public SF2Sample(Soundbank soundBank, String name, byte[] data, long startLoop, long endLoop, long sampleRate, int originalPitch) {
        this(soundBank, name, data, startLoop, endLoop, sampleRate, originalPitch, (byte)0);
    }

    public ModelByteBuffer getDataBuffer() {
        return data;
    }

    public ModelByteBuffer getData24Buffer() {
        return data24;
    }

    public AudioFormat getFormat() {
        return new AudioFormat(sampleRate, 16, 1, true);
    }

    public long getEndLoop() {
        return endLoop;
    }

    public int getOriginalPitch() {
        return originalPitch;
    }

    public long getStartLoop() {
        return startLoop;
    }

    public byte getPitchCorrection() {
        return pitchCorrection;
    }

}
