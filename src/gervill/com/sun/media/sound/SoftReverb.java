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

import own.main.ImmutableList;

/**
 * Reverb effect based on allpass/comb filters. First audio is send to 8
 * parelled comb filters and then mixed together and then finally send thru 3
 * different allpass filters.
 *
 * @author Karl Helgason
 */
public final class SoftReverb {

    private final static class Delay {

        private float[] delaybuffer;
        private int rovepos = 0;

        Delay() {
            delaybuffer = null;
        }

        public void setDelay(int delay) {
            if (delay == 0)
                delaybuffer = null;
            else
                delaybuffer = new float[delay];
            rovepos = 0;
        }

        public void processReplace(float[] inout) {
            if (delaybuffer == null)
                return;
            int len = inout.length;
            int rnlen = delaybuffer.length;
            int rovepos = this.rovepos;

            for (int i = 0; i < len; i++) {
                float x = inout[i];
                inout[i] = delaybuffer[rovepos];
                delaybuffer[rovepos] = x;
                if (++rovepos == rnlen)
                    rovepos = 0;
            }
            this.rovepos = rovepos;
        }
    }

    private final static class AllPass {

        private final float[] delaybuffer;
        private final int delaybuffersize;
        private int rovepos = 0;
        private final float feedback;

        AllPass(int size, float feedback) {
            delaybuffer = new float[size];
            delaybuffersize = size;
            this.feedback = feedback;
        }

        public void processReplace(float[] inout) {
            int len = inout.length;
            int delaybuffersize = this.delaybuffersize;
            int rovepos = this.rovepos;
            for (int i = 0; i < len; i++) {
                float delayout = delaybuffer[rovepos];
                float input = inout[i];
                inout[i] = delayout - input;
                delaybuffer[rovepos] = input + delayout * feedback;
                if (++rovepos == delaybuffersize)
                    rovepos = 0;
            }
            this.rovepos = rovepos;
        }

    }

    private final static class Comb {

        private final float[] delaybuffer;
        private final int delaybuffersize;
        private int rovepos = 0;
        private float feedback;
        private float filtertemp = 0;
        private float filtercoeff1 = 0;
        private float filtercoeff2 = 1;

        Comb(int size) {
            delaybuffer = new float[size];
            delaybuffersize = size;
        }

        public void processMix(float[] in, float[] out) {
            int len = in.length;
            int delaybuffersize = this.delaybuffersize;
            int rovepos = this.rovepos;
            float filtertemp = this.filtertemp;
            float filtercoeff1 = this.filtercoeff1;
            float filtercoeff2 = this.filtercoeff2;
            for (int i = 0; i < len; i++) {
                float delayout = delaybuffer[rovepos];
                // One Pole Lowpass Filter
                filtertemp = (delayout * filtercoeff2)
                        + (filtertemp * filtercoeff1);
                out[i] += delayout;
                delaybuffer[rovepos] = in[i] + filtertemp;
                if (++rovepos == delaybuffersize)
                    rovepos = 0;
            }
            this.filtertemp  = filtertemp;
            this.rovepos = rovepos;
        }

        public void processReplace(float[] in, float[] out) {
            int len = in.length;
            int delaybuffersize = this.delaybuffersize;
            int rovepos = this.rovepos;
            float filtertemp = this.filtertemp;
            float filtercoeff1 = this.filtercoeff1;
            float filtercoeff2 = this.filtercoeff2;
            for (int i = 0; i < len; i++) {
                float delayout = delaybuffer[rovepos];
                // One Pole Lowpass Filter
                filtertemp = (delayout * filtercoeff2)
                        + (filtertemp * filtercoeff1);
                out[i] = delayout;
                delaybuffer[rovepos] = in[i] + filtertemp;
                if (++rovepos == delaybuffersize)
                    rovepos = 0;
            }
            this.filtertemp  = filtertemp;
            this.rovepos = rovepos;
        }

        public void setDamp(float val) {
            filtercoeff1 = val;
            filtercoeff2 = (1 - filtercoeff1)* feedback;
        }
    }

    private float gain = 1;
    private final Delay delay = new Delay();
    private final ImmutableList<Comb> combL = ImmutableList.create(new Comb(1116), new Comb(1188), new Comb(1277), new Comb(1356), new Comb(1422), new Comb(1491), new Comb(1557), new Comb(1617));
    private final ImmutableList<Comb> combR = ImmutableList.create(new Comb(1139), new Comb(1211), new Comb(1300), new Comb(1379), new Comb(1445), new Comb(1514), new Comb(1580), new Comb(1640));
    private final ImmutableList<AllPass> allpassL = ImmutableList.create(new AllPass(556, 0.5f), new AllPass(441, 0.5f), new AllPass(341, 0.5f), new AllPass(225, 0.5f));
    private float[] input;
    private float[] pre1;
    private float[] pre2;
    private float[] pre3;
    private boolean denormal_flip = false;
    private SoftAudioBuffer inputA;
    private SoftAudioBuffer left;
    private SoftAudioBuffer right;
    private boolean dirty = true;

    public void setInput(int pin, SoftAudioBuffer input) {
        if (pin == 0)
            inputA = input;
    }

    public void setOutput(int pin, SoftAudioBuffer output) {
        if (pin == 0)
            left = output;
        if (pin == 1)
            right = output;
    }

    private boolean silent = true;

    public void processAudio() {
        boolean silent_input = this.inputA.isSilent();
        if(!silent_input)
            silent = false;
        if(silent)
        {
            return;
        }

        float[] inputA = this.inputA.array();
        float[] left = this.left.array();
        float[] right = this.right.array();

        int numsamples = inputA.length;
        if (input == null || input.length < numsamples)
            input = new float[numsamples];

        float again = gain * 0.018f / 2;

        denormal_flip = !denormal_flip;
        if(denormal_flip)
            for (int i = 0; i < numsamples; i++)
                input[i] = inputA[i] * again + 1E-20f;
        else
            for (int i = 0; i < numsamples; i++)
                input[i] = inputA[i] * again - 1E-20f;

        delay.processReplace(input);

        if (pre1 == null || pre1.length < numsamples)
        {
            pre1 = new float[numsamples];
            pre2 = new float[numsamples];
            pre3 = new float[numsamples];
        }

        for (AllPass allPass : allpassL) allPass.processReplace(input);

        combL.get(0).processReplace(input, pre3);
        combL.get(1).processReplace(input, pre3);

        combL.get(2).processReplace(input, pre1);
        for (int i = 4; i < combL.length-2; i+=2)
            combL.get(i).processMix(input, pre1);

        combL.get(3).processReplace(input, pre2);
        for (int i = 5; i < combL.length-2; i+=2)
            combL.get(i).processMix(input, pre2);

        for (int i = combR.length-2; i < combR.length; i++)
            combR.get(i).processMix(input, right);
        for (int i = combL.length-2; i < combL.length; i++)
            combL.get(i).processMix(input, left);

        for (int i = 0; i < numsamples; i++)
        {
            float p = pre1[i] - pre2[i];
            float m = pre3[i];
            left[i] += m + p;
            right[i] += m - p;
        }


        if (silent_input) {
            silent = true;
            for (int i = 0; i < numsamples; i++)
            {
                float v = left[i];
                if(v > 1E-10 || v < -1E-10)
                {
                    silent = false;
                    break;
                }
            }
        }

    }

    public void processControlLogic() {
        if (dirty) {
            dirty = false;
            setRoomSize(1.8f);
            setDamp(24000);
            setPreDelay(0.03f);
            setGain(1.5f);
        }
    }

    public void setRoomSize(float value) {
        float roomsize = 1 - (0.17f / value);

        for (int i = 0; i < combL.length; i++) {
            combL.get(i).feedback = roomsize;
            combR.get(i).feedback = roomsize;
        }
    }

    public void setPreDelay(float value) {
        delay.setDelay((int)(value * 44100));
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public void setDamp(float value) {
        double x = (value / 44100) * (2 * Math.PI);
        double cx = 2 - Math.cos(x);
        float damp = (float) (cx - Math.sqrt(cx * cx - 1));
        if (damp > 1)
            damp = 1;
        if (damp < 0)
            damp = 0;

        // damp = value * 0.4f;
        for (int i = 0; i < combL.length; i++) {
            combL.get(i).setDamp(damp);
            combR.get(i).setDamp(damp);
        }

    }
}

