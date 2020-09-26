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
 * A chorus effect made using LFO and variable delay. One for each channel
 * (left,right), with different starting phase for stereo effect.
 *
 * @author Karl Helgason
 */
public final class SoftChorus {

    private static class VariableDelay {

        private final float[] delaybuffer;
        private int rovepos = 0;
        private float delay = 0;
        private float lastdelay = 0;
        private float feedback = 0;

        VariableDelay(int maxbuffersize) {
            delaybuffer = new float[maxbuffersize];
        }

        public void setDelay(float delay) {
            this.delay = delay;
        }

        public void setFeedBack(float feedback) {
            this.feedback = feedback;
        }

        public void processMix(float[] in, float[] out) {
            float delay = this.delay;
            float feedback = this.feedback;

            float[] delaybuffer = this.delaybuffer;
            int len = in.length;
            float delaydelta = (delay - lastdelay) / len;
            int rnlen = delaybuffer.length;
            int rovepos = this.rovepos;

            for (int i = 0; i < len; i++) {
                float r = rovepos - (lastdelay + 2) + rnlen;
                int ri = (int) r;
                float s = r - ri;
                float a = delaybuffer[ri % rnlen];
                float b = delaybuffer[(ri + 1) % rnlen];
                float o = a * (1 - s) + b * (s);
                out[i] += o;
                delaybuffer[rovepos] = in[i] + o * feedback;
                rovepos = (rovepos + 1) % rnlen;
                lastdelay += delaydelta;
            }
            this.rovepos = rovepos;
            lastdelay = delay;
        }

    }

    private static class LFODelay {

        private double phase;
        private double phase_step = 0;
        private double depth = 0;
        private VariableDelay vdelay;

        LFODelay(double phase) {
            // vdelay = new VariableDelay((int)(samplerate*4));
            vdelay = new VariableDelay(20);
            this.phase = phase;
        }

        public void setDepth(double depth) {
            this.depth = depth * 44100;
            vdelay = new VariableDelay((int) ((this.depth + 10) * 2));
        }

        public void setRate(double rate) {
            double controlrate = 147;
            phase_step = (Math.PI * 2) * (rate / controlrate);
        }

        public void setFeedBack(float feedback) {
            vdelay.setFeedBack(feedback);
        }

        public void processMix(float[] in, float[] out) {
            phase += phase_step;
            while(phase > (Math.PI * 2)) phase -= (Math.PI * 2);
            vdelay.setDelay((float) (depth * 0.5 * (Math.cos(phase) + 2)));
            vdelay.processMix(in, out);
        }

    }

    private final SoftAudioBuffer inputA;
    private final SoftAudioBuffer left;
    private final SoftAudioBuffer right;
    private final LFODelay vdelay1L = new LFODelay(0.5 * Math.PI);
    private final LFODelay vdelay1R = new LFODelay(0);
    private boolean dirty = true;

    double silentcounter = 1000;

    public SoftChorus(SoftAudioBuffer inputA, SoftAudioBuffer left, SoftAudioBuffer right) {
        this.inputA = inputA;
        this.left = left;
        this.right = right;
    }

    public void processControlLogic() {
        if (dirty) {
            dirty = false;
            vdelay1L.setRate(0.366);
            vdelay1R.setRate(0.366);
            vdelay1L.setDepth(1 / 160f);
            vdelay1R.setDepth(1 / 160f);
            vdelay1L.setFeedBack(0.06104f);
            vdelay1R.setFeedBack(0.06104f);
        }
    }

    public void processAudio() {

        if (inputA.isSilent()) {
            silentcounter += 1 / 147f;

            if (silentcounter > 1) {
                return;
            }
        } else
            silentcounter = 0;

        float[] inputA = this.inputA.array();
        float[] left = this.left.array();
        float[] right = this.right == null ? null : this.right.array();

        vdelay1L.processMix(inputA, left);
        if (right != null)
            vdelay1R.processMix(inputA, right);
    }
}
