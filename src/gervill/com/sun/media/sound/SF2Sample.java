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
import gervill.javax.sound.sampled.AudioInputStream;

import java.io.InputStream;

/**
 * Soundfont sample storage.
 *
 * @author Karl Helgason
 */
public final class SF2Sample extends SoundbankResource {

    String name = "";
    long startLoop = 0;
    long endLoop = 0;
    long sampleRate = 44100;
    int originalPitch = 60;
    byte pitchCorrection = 0;
    ModelByteBuffer data;
    ModelByteBuffer data24;

    public SF2Sample(Soundbank soundBank) {
        super(soundBank, null);
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

    public void setData(byte[] data) {
        this.data = new ModelByteBuffer(data);
    }

    /*
    public void setData(File file, int offset, int length) {
        this.data = null;
        this.sampleFile = file;
        this.sampleOffset = offset;
        this.sampleLen = length;
    }
    */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndLoop(long endLoop) {
        this.endLoop = endLoop;
    }

    public void setOriginalPitch(int originalPitch) {
        this.originalPitch = originalPitch;
    }

    public void setPitchCorrection(byte pitchCorrection) {
        this.pitchCorrection = pitchCorrection;
    }

    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setStartLoop(long startLoop) {
        this.startLoop = startLoop;
    }

}
