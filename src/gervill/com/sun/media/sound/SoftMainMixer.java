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

import gervill.javax.sound.sampled.AudioInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Software synthesizer main audio mixer.
 *
 * @author Karl Helgason
 */
public final class SoftMainMixer {

    public final static int CHANNEL_LEFT = 0;
    public final static int CHANNEL_RIGHT = 1;
    public final static int CHANNEL_MONO = 2;
    public final static int CHANNEL_DELAY_LEFT = 3;
    public final static int CHANNEL_DELAY_RIGHT = 4;
    public final static int CHANNEL_DELAY_MONO = 5;
    public final static int CHANNEL_EFFECT1 = 6;
    public final static int CHANNEL_EFFECT2 = 7;
    public final static int CHANNEL_DELAY_EFFECT1 = 8;
    public final static int CHANNEL_DELAY_EFFECT2 = 9;
    public final static int CHANNEL_LEFT_DRY = 10;
    public final static int CHANNEL_RIGHT_DRY = 11;
    private boolean pusher_silent = false;
    private int pusher_silent_count = 0;
    private final Object control_mutex;
    private final SoftSynthesizer synth;
    private final SoftVoice[] voicestatus;
    private final SoftAudioBuffer[] buffers;
    private final SoftReverb reverb;
    private final SoftChorus chorus;
    private final SoftLimiter agc;
    double last_volume_left = 1.0;
    double last_volume_right = 1.0;
    private final double[] co_master_balance = new double[1];
    private final double[] co_master_volume = new double[1];
    private final double[] co_master_coarse_tuning = new double[1];
    private final double[] co_master_fine_tuning = new double[1];
    private final AudioInputStream ais;
    final SoftControl co_master = new SoftControl() {

        final double[] balance = co_master_balance;
        final double[] volume = co_master_volume;
        final double[] coarse_tuning = co_master_coarse_tuning;
        final double[] fine_tuning = co_master_fine_tuning;

        public double[] get(int instance, String name) {
            if (name == null)
                return null;
            if (name.equals("balance"))
                return balance;
            if (name.equals("volume"))
                return volume;
            if (name.equals("coarse_tuning"))
                return coarse_tuning;
            if (name.equals("fine_tuning"))
                return fine_tuning;
            return null;
        }
    };

    void processAudioBuffers() {

        if(synth.weakstream != null && synth.weakstream.silent_samples.get() != 0)
        {
            synth.weakstream.silent_samples.set(0);
        }

        for (int i = 0; i < buffers.length; i++) {
            if(i != CHANNEL_DELAY_LEFT &&
                    i != CHANNEL_DELAY_RIGHT &&
                    i != CHANNEL_DELAY_MONO &&
                    i != CHANNEL_DELAY_EFFECT1 &&
                    i != CHANNEL_DELAY_EFFECT2)
                buffers[i].clear();
        }

        if(!buffers[CHANNEL_DELAY_LEFT].isSilent())
        {
            buffers[CHANNEL_LEFT].swap(buffers[CHANNEL_DELAY_LEFT]);
        }
        if(!buffers[CHANNEL_DELAY_RIGHT].isSilent())
        {
            buffers[CHANNEL_RIGHT].swap(buffers[CHANNEL_DELAY_RIGHT]);
        }
        if(!buffers[CHANNEL_DELAY_MONO].isSilent())
        {
            buffers[CHANNEL_MONO].swap(buffers[CHANNEL_DELAY_MONO]);
        }
        if(!buffers[CHANNEL_DELAY_EFFECT1].isSilent())
        {
            buffers[CHANNEL_EFFECT1].swap(buffers[CHANNEL_DELAY_EFFECT1]);
        }
        if(!buffers[CHANNEL_DELAY_EFFECT2].isSilent())
        {
            buffers[CHANNEL_EFFECT2].swap(buffers[CHANNEL_DELAY_EFFECT2]);
        }

        double volume_left;
        double volume_right;

        // perform control logic
        synchronized (control_mutex) {

            for (SoftVoice softVoice : voicestatus)
                if (softVoice.active)
                    softVoice.processControlLogic();

            double volume = co_master_volume[0];
            volume_left = volume;
            volume_right = volume;

            double balance = co_master_balance[0];
            if (balance > 0.5)
                volume_left *= (1 - balance) * 2;
            else
                volume_right *= balance * 2;

            chorus.processControlLogic();
            reverb.processControlLogic();

        }

        for (SoftVoice softVoice : voicestatus)
            if (softVoice.active)
                softVoice.processAudioLogic(buffers);

        if(!buffers[CHANNEL_MONO].isSilent())
        {
            float[] mono = buffers[CHANNEL_MONO].array();
            float[] left = buffers[CHANNEL_LEFT].array();
            int bufferlen = buffers[CHANNEL_LEFT].getSize();
            float[] right = buffers[CHANNEL_RIGHT].array();
            for (int i = 0; i < bufferlen; i++) {
                float v = mono[i];
                left[i] += v;
                right[i] += v;
            }
        }

        // Run effects
        chorus.processAudio();
        reverb.processAudio();

        // Set Volume / Balance
        if (last_volume_left != volume_left || last_volume_right != volume_right) {
            float[] left = buffers[CHANNEL_LEFT].array();
            float[] right = buffers[CHANNEL_RIGHT].array();
            int bufferlen = buffers[CHANNEL_LEFT].getSize();

            float amp;
            float amp_delta;
            amp = (float)(last_volume_left * last_volume_left);
            amp_delta = (float)((volume_left * volume_left - amp) / bufferlen);
            for (int i = 0; i < bufferlen; i++) {
                amp += amp_delta;
                left[i] *= amp;
            }
            amp = (float)(last_volume_right * last_volume_right);
            amp_delta = (float)((volume_right*volume_right - amp) / bufferlen);
            for (int i = 0; i < bufferlen; i++) {
                amp += amp_delta;
                right[i] *= volume_right;
            }
            last_volume_left = volume_left;
            last_volume_right = volume_right;

        } else {
            if (volume_left != 1.0 || volume_right != 1.0) {
                float[] left = buffers[CHANNEL_LEFT].array();
                float[] right = buffers[CHANNEL_RIGHT].array();
                int bufferlen = buffers[CHANNEL_LEFT].getSize();
                float amp;
                amp = (float) (volume_left * volume_left);
                for (int i = 0; i < bufferlen; i++)
                    left[i] *= amp;
                amp = (float)(volume_right * volume_right);
                for (int i = 0; i < bufferlen; i++)
                    right[i] *= amp;

            }
        }

        if(buffers[CHANNEL_LEFT].isSilent()
            && buffers[CHANNEL_RIGHT].isSilent())
        {
            pusher_silent_count++;
            if(pusher_silent_count > 5)
            {
                pusher_silent_count = 0;
                synchronized (control_mutex) {
                    pusher_silent = true;
                    if(synth.weakstream != null)
                        synth.weakstream.setInputStream(null);
                }
            }
        }
        else
            pusher_silent_count = 0;

        agc.processAudio();

    }

    // Must only we called within control_mutex synchronization
    public void activity()
    {
        if(pusher_silent)
        {
            pusher_silent = false;
            if(synth.weakstream != null)
            {
                synth.weakstream.setInputStream(ais);
            }
        }
    }

    public SoftMainMixer(SoftSynthesizer synth) {
        this.synth = synth;

        co_master_balance[0] = 0.5;
        co_master_volume[0] = 1;
        co_master_coarse_tuning[0] = 0.5;
        co_master_fine_tuning[0] = 0.5;

        int buffersize = 300;

        control_mutex = synth.control_mutex;
        buffers = new SoftAudioBuffer[14];
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = new SoftAudioBuffer(buffersize, SoftSynthesizer.SYNTH_FORMAT);
        }
        voicestatus = synth.getVoices();

        reverb = new SoftReverb();
        chorus = new SoftChorus();
        agc = new SoftLimiter();

        float samplerate = 44100;
        float controlrate = 147f;
        reverb.init(samplerate);
        chorus.init(samplerate, controlrate);
        agc.init(controlrate);

        chorus.setInput(0, buffers[CHANNEL_EFFECT2]);
        chorus.setOutput(0, buffers[CHANNEL_LEFT]);
        chorus.setOutput(1, buffers[CHANNEL_RIGHT]);
        chorus.setOutput(2, buffers[CHANNEL_EFFECT1]);

        reverb.setInput(0, buffers[CHANNEL_EFFECT1]);
        reverb.setOutput(0, buffers[CHANNEL_LEFT]);
        reverb.setOutput(1, buffers[CHANNEL_RIGHT]);

        agc.setInput(0, buffers[CHANNEL_LEFT]);
        agc.setInput(1, buffers[CHANNEL_RIGHT]);
        agc.setOutput(0, buffers[CHANNEL_LEFT]);
        agc.setOutput(1, buffers[CHANNEL_RIGHT]);

        InputStream in = new InputStream() {

            private final SoftAudioBuffer[] buffers = SoftMainMixer.this.buffers;
            private final byte[] bbuffer = new byte[4 * buffers[0].getSize()];
            private int bbuffer_pos = 0;
            private final byte[] single = new byte[1];

            public void fillBuffer() {
                processAudioBuffers();
                buffers[0].get(bbuffer, 0);
                buffers[1].get(bbuffer, 1);
                bbuffer_pos = 0;
            }

            public int read(byte[] b, int off, int len) {
                int bbuffer_len = bbuffer.length;
                int offlen = off + len;
                byte[] bbuffer = this.bbuffer;
                while (off < offlen) {
                    if (available() == 0)
                        fillBuffer();
                    else {
                        int bbuffer_pos = this.bbuffer_pos;
                        while (off < offlen && bbuffer_pos < bbuffer_len)
                            b[off++] = bbuffer[bbuffer_pos++];
                        this.bbuffer_pos = bbuffer_pos;
                    }
                }
                return len;
            }

            public int read() throws IOException {
                int ret = read(single);
                if (ret == -1)
                    return -1;
                return single[0] & 0xFF;
            }

            public int available() {
                return bbuffer.length - bbuffer_pos;
            }

            public void close() {
                SoftMainMixer.this.synth.close();
            }
        };

        ais = new AudioInputStream(in, SoftSynthesizer.SYNTH_FORMAT, AudioInputStream.NOT_SPECIFIED);

    }

    public AudioInputStream getInputStream() {
        return ais;
    }

}
