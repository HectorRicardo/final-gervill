/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates. All rights reserved.
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

import gervill.javax.sound.midi.Instrument;
import gervill.javax.sound.midi.MidiChannel;
import gervill.javax.sound.sampled.AudioFormat;
import gervill.javax.sound.sampled.AudioInputStream;
import gervill.javax.sound.sampled.SourceDataLine;
import gervill.soundbanks.EmergencySoundbank;
import own.main.ImmutableList;

import java.io.IOException;
import java.util.*;

/**
 * The software synthesizer class.
 *
 * @author Karl Helgason
 */
public final class SoftSynthesizer implements AutoCloseable {

    public static final int NUMBER_OF_CHANNELS = 16;
    public static final AudioFormat SYNTH_FORMAT = new AudioFormat(44100, 16, 2, true);
    public static final AudioFloatConverter SYNTH_CONVERTER = AudioFloatConverter.getConverter(SYNTH_FORMAT);
    private final static int MAX_POLY = 64;
    private static ImmutableList<Instrument> defaultInstruments = null;
    final Object control_mutex = this;
    final SoftChannelProxy[] external_channels = new SoftChannelProxy[NUMBER_OF_CHANNELS];
    private final SourceDataLine sourceDataLine = new SourceDataLine();
    private final SoftVoice[] voices = new SoftVoice[MAX_POLY];
    private final Map<ModelInstrument, SoftInstrument> inslist = new HashMap<>();
    int voiceIDCounter = 0;
    SoftChannel[] channels;
    private SoftAudioPusher pusher = null;
    private boolean open = false;
    private SoftMainMixer mainmixer;

    {
        for (int i = 0; i < NUMBER_OF_CHANNELS; i++) {
            external_channels[i] = new SoftChannelProxy();
        }
    }

    private boolean loadInstruments(List<ModelInstrument> instruments) {
        if (!isOpen())
            return false;

        synchronized (control_mutex) {
            if (channels != null)
                for (SoftChannel c : channels) {
                    c.current_instrument = null;
                    c.current_director = null;
                }
            for (Instrument instrument : instruments) {
                ModelInstrument mInstr = (ModelInstrument) instrument;
                inslist.put(mInstr, new SoftInstrument(mInstr));
            }
        }

        return true;
    }

    SoftMainMixer getMainMixer() {
        if (!isOpen())
            return null;
        return mainmixer;
    }

    SoftInstrument findInstrument(ModelInstrument instrument) {
        return inslist.get(instrument);
    }

    SoftVoice[] getVoices() {
        return voices;
    }

    public MidiChannel[] getChannels() {

        synchronized (control_mutex) {
            return external_channels;
        }
    }

    public boolean loadInstrument(Instrument instrument) {
        if ((!(instrument instanceof ModelInstrument))) {
            throw new IllegalArgumentException("Unsupported instrument: " +
                    instrument);
        }
        List<ModelInstrument> instruments = new ArrayList<>();
        instruments.add((ModelInstrument) instrument);
        return loadInstruments(instruments);
    }

    public void unloadInstrument(Instrument instrument) {
        if ((!(instrument instanceof ModelInstrument))) {
            throw new IllegalArgumentException("Unsupported instrument: " +
                    instrument);
        }
        if (!isOpen())
            return;

        synchronized (control_mutex) {
            for (SoftChannel c : channels)
                c.current_instrument = null;
            inslist.remove(instrument);
            for (SoftChannel channel : channels) {
                channel.allSoundOff();
            }
        }
    }

    public ImmutableList<Instrument> getDefaultSoundbank() {
        synchronized (SoftSynthesizer.class) {
            if (defaultInstruments != null)
                return defaultInstruments;

            try {
                /*
                 * Generate emergency soundbank
                 */
                defaultInstruments = EmergencySoundbank.createSoundbank();
            } catch (Exception ignored) {
            }
        }
        return defaultInstruments;
    }

    public void open() {
        if (isOpen()) {
            return;
        }
        synchronized (control_mutex) {
            AudioInputStream ais = openStream();
            sourceDataLine.open(SYNTH_FORMAT, 21168);
            sourceDataLine.start();
            pusher = new SoftAudioPusher(sourceDataLine, ais);
            pusher.start();
        }
    }

    private AudioInputStream openStream() {

        open = true;

        for (int i = 0; i < MAX_POLY; i++)
            voices[i] = new SoftVoice(this);

        mainmixer = new SoftMainMixer(this);

        channels = new SoftChannel[NUMBER_OF_CHANNELS];
        for (int i = 0; i < NUMBER_OF_CHANNELS; i++) {
            channels[i] = new SoftChannel(this, i);
            external_channels[i].setChannel(channels[i]);
        }

        for (SoftVoice voice : getVoices())
            voice.resampler = new SoftResamplerStreamer();

        return mainmixer.getInputStream();
    }

    public void close() {

        if (!isOpen())
            return;

        SoftAudioPusher pusher_to_be_closed = null;
        synchronized (control_mutex) {
            if (pusher != null) {
                pusher_to_be_closed = pusher;
                pusher = null;
            }
        }

        if (pusher_to_be_closed != null) {
            // Pusher must not be closed synchronized against control_mutex,
            // this may result in synchronized conflict between pusher
            // and current thread.
            pusher_to_be_closed.stop();

            try {
                pusher_to_be_closed.getAudioInputStream().close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        synchronized (control_mutex) {
            open = false;
            mainmixer = null;
            Arrays.fill(voices, null);
            channels = null;

            for (SoftChannelProxy external_channel : external_channels) external_channel.setChannel(null);

            sourceDataLine.close();

            inslist.clear();
        }
    }

    public boolean isOpen() {
        synchronized (control_mutex) {
            return open;
        }
    }

}
