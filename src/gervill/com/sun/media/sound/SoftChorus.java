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
public final class SoftChorus implements SoftAudioProcessor {

    private static class VariableDelay {

        private final float[] delaybuffer;
        private int rovepos = 0;
        private float gain = 1;
        private float rgain = 0;
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

        public void setGain(float gain) {
            this.gain = gain;
        }

        public void setReverbSendGain(float rgain) {
            this.rgain = rgain;
        }

        public void processMix(float[] in, float[] out, float[] rout) {
            float gain = this.gain;
            float delay = this.delay;
            float feedback = this.feedback;

            float[] delaybuffer = this.delaybuffer;
            int len = in.length;
            float delaydelta = (delay - lastdelay) / len;
            int rnlen = delaybuffer.length;
            int rovepos = this.rovepos;

            if (rout == null)
                for (int i = 0; i < len; i++) {
                    float r = rovepos - (lastdelay + 2) + rnlen;
                    int ri = (int) r;
                    float s = r - ri;
                    float a = delaybuffer[ri % rnlen];
                    float b = delaybuffer[(ri + 1) % rnlen];
                    float o = a * (1 - s) + b * (s);
                    out[i] += o * gain;
                    delaybuffer[rovepos] = in[i] + o * feedback;
                    rovepos = (rovepos + 1) % rnlen;
                    lastdelay += delaydelta;
                }
            else
                for (int i = 0; i < len; i++) {
                    float r = rovepos - (lastdelay + 2) + rnlen;
                    int ri = (int) r;
                    float s = r - ri;
                    float a = delaybuffer[ri % rnlen];
                    float b = delaybuffer[(ri + 1) % rnlen];
                    float o = a * (1 - s) + b * (s);
                    out[i] += o * gain;
                    rout[i] += o * rgain;
                    delaybuffer[rovepos] = in[i] + o * feedback;
                    rovepos = (rovepos + 1) % rnlen;
                    lastdelay += delaydelta;
                }
            this.rovepos = rovepos;
            lastdelay = delay;
        }

    }

    private static class LFODelay {

        private double phase = 1;
        private double phase_step = 0;
        private double depth = 0;
        private VariableDelay vdelay;
        private final double samplerate;
        private final double controlrate;

        LFODelay(double samplerate, double controlrate) {
            this.samplerate = samplerate;
            this.controlrate = controlrate;
            // vdelay = new VariableDelay((int)(samplerate*4));
            vdelay = new VariableDelay((int) ((this.depth + 10) * 2));

        }

        public void setDepth(double depth) {
            this.depth = depth * samplerate;
            vdelay = new VariableDelay((int) ((this.depth + 10) * 2));
        }

        public void setRate(double rate) {
            phase_step = (Math.PI * 2) * (rate / controlrate);
        }

        public void setPhase(double phase) {
            this.phase = phase;
        }

        public void setFeedBack(float feedback) {
            vdelay.setFeedBack(feedback);
        }

        public void setGain(float gain) {
            vdelay.setGain(gain);
        }

        public void setReverbSendGain(float rgain) {
            vdelay.setReverbSendGain(rgain);
        }

        public void processMix(float[] in, float[] out, float[] rout) {
            phase += phase_step;
            while(phase > (Math.PI * 2)) phase -= (Math.PI * 2);
            vdelay.setDelay((float) (depth * 0.5 * (Math.cos(phase) + 2)));
            vdelay.processMix(in, out, rout);
        }

    }

    private SoftAudioBuffer inputA;
    private SoftAudioBuffer left;
    private SoftAudioBuffer right;
    private SoftAudioBuffer reverb;
    private LFODelay vdelay1L;
    private LFODelay vdelay1R;
    private float rgain = 0;
    private boolean dirty = true;
    private double dirty_vdelay1L_rate;
    private double dirty_vdelay1R_rate;
    private double dirty_vdelay1L_depth;
    private double dirty_vdelay1R_depth;
    private float dirty_vdelay1L_feedback;
    private float dirty_vdelay1R_feedback;
    private float dirty_vdelay1L_reverbsendgain;
    private float dirty_vdelay1R_reverbsendgain;
    private float controlrate;

    public void init(float samplerate, float controlrate) {
        this.controlrate = controlrate;
        vdelay1L = new LFODelay(samplerate, controlrate);
        vdelay1R = new LFODelay(samplerate, controlrate);
        vdelay1L.setGain(1.0f); // %
        vdelay1R.setGain(1.0f); // %
        vdelay1L.setPhase(0.5 * Math.PI);
        vdelay1R.setPhase(0);

        globalParameterControlChange(0, 2);
    }

    private void globalParameterControlChange(long param,
                                              long value) {
        if (param == 0) { // Chorus Type
            // Chorus 3 8 (6%) 3 (0.4Hz) 19 (6.3ms) 0 (0%)
            globalParameterControlChange(3, 8);
            globalParameterControlChange(1, 3);
            globalParameterControlChange(2, 19);
            globalParameterControlChange(4, 0);
        } else if (param == 1) { // Mod Rate
            dirty_vdelay1L_rate = (value * 0.122);
            dirty_vdelay1R_rate = (value * 0.122);
            dirty = true;
        } else if (param == 2) { // Mod Depth
            dirty_vdelay1L_depth = ((value + 1) / 3200.0);
            dirty_vdelay1R_depth = ((value + 1) / 3200.0);
            dirty = true;
        } else if (param == 3) { // Feedback
            dirty_vdelay1L_feedback = (value * 0.00763f);
            dirty_vdelay1R_feedback = (value * 0.00763f);
            dirty = true;
        }
        if (param == 4) { // Send to Reverb
            rgain = value * 0.00787f;
            dirty_vdelay1L_reverbsendgain = (value * 0.00787f);
            dirty_vdelay1R_reverbsendgain = (value * 0.00787f);
            dirty = true;
        }

    }

    public void processControlLogic() {
        if (dirty) {
            dirty = false;
            vdelay1L.setRate(dirty_vdelay1L_rate);
            vdelay1R.setRate(dirty_vdelay1R_rate);
            vdelay1L.setDepth(dirty_vdelay1L_depth);
            vdelay1R.setDepth(dirty_vdelay1R_depth);
            vdelay1L.setFeedBack(dirty_vdelay1L_feedback);
            vdelay1R.setFeedBack(dirty_vdelay1R_feedback);
            vdelay1L.setReverbSendGain(dirty_vdelay1L_reverbsendgain);
            vdelay1R.setReverbSendGain(dirty_vdelay1R_reverbsendgain);
        }
    }
    double silentcounter = 1000;

    public void processAudio() {

        if (inputA.isSilent()) {
            silentcounter += 1 / controlrate;

            if (silentcounter > 1) {
                return;
            }
        } else
            silentcounter = 0;

        float[] inputA = this.inputA.array();
        float[] left = this.left.array();
        float[] right = this.right == null ? null : this.right.array();
        float[] reverb = rgain != 0 ? this.reverb.array() : null;

        vdelay1L.processMix(inputA, left, reverb);
        if (right != null)
            vdelay1R.processMix(inputA, right, reverb);
    }

    public void setInput(int pin, SoftAudioBuffer input) {
        if (pin == 0)
            inputA = input;
    }

    public void setOutput(int pin, SoftAudioBuffer output) {
        if (pin == 0)
            left = output;
        if (pin == 1)
            right = output;
        if (pin == 2)
            reverb = output;
    }
}
