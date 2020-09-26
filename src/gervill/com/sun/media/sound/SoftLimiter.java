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

/**
 * A simple look-ahead volume limiter with very fast attack and fast release.
 * This filter is used for preventing clipping.
 *
 * @author Karl Helgason
 */
public final class SoftLimiter {

    private float lastmax = 0;
    private float gain = 1;
    private float[] temp_bufferL;
    private float[] temp_bufferR;
    private final SoftAudioBuffer bufferL;
    private final SoftAudioBuffer bufferR;

    public SoftLimiter(SoftAudioBuffer left, SoftAudioBuffer right) {
        this.bufferL = left;
        this.bufferR = right;
    }

    private double silentcounter = 0;

    public void processAudio() {
        if (this.bufferL.isSilent() && this.bufferR.isSilent()) {
            float controlrate = 147f;
            silentcounter += 1 / controlrate;

            if (silentcounter > 60) {
                bufferL.clear();
                bufferR.clear();
                return;
            }
        } else
            silentcounter = 0;

        float[] bufferL = this.bufferL.array();
        float[] bufferR = this.bufferR.array();

        if (temp_bufferL == null)
            temp_bufferL = new float[bufferL.length];
        if (temp_bufferR == null)
            temp_bufferR = new float[bufferR.length];

        float max = 0;
        int len = bufferL.length;

        for (int i = 0; i < len; i++) {
            if (bufferL[i] > max)
                max = bufferL[i];
            if (bufferR[i] > max)
                max = bufferR[i];
            if (-bufferL[i] > max)
                max = -bufferL[i];
            if (-bufferR[i] > max)
                max = -bufferR[i];
        }

        float lmax = lastmax;
        lastmax = max;
        if (lmax > max)
            max = lmax;

        float newgain = 1;
        if (max > 0.99f)
            newgain = 0.99f / max;

        if (newgain > gain)
            newgain = (newgain + gain * 9) / 10f;

        float gaindelta = (newgain - gain) / len;
        for (int i = 0; i < len; i++) {
            gain += gaindelta;
            float bL = bufferL[i];
            float bR = bufferR[i];
            float tL = temp_bufferL[i];
            float tR = temp_bufferR[i];
            temp_bufferL[i] = bL;
            temp_bufferR[i] = bR;
            bufferL[i] = tL * gain;
            bufferR[i] = tR * gain;
        }

        gain = newgain;
    }

}
