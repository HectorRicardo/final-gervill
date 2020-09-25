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
import gervill.javax.sound.midi.Patch;
import gervill.javax.sound.sampled.AudioFormat;
import gervill.javax.sound.sampled.AudioInputStream;
import gervill.javax.sound.sampled.SourceDataLine;
import gervill.soundbanks.EmergencySoundbank;
import own.main.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The software synthesizer class.
 *
 * @author Karl Helgason
 */
public final class SoftSynthesizer implements AutoCloseable {

    protected static final class WeakAudioStream extends InputStream
    {
        private volatile AudioInputStream stream;
        public final AtomicLong silent_samples = new AtomicLong(0);
        private final WeakReference<AudioInputStream> weak_stream_link;
        private float[] silentbuffer = null;

        public void setInputStream(AudioInputStream stream)
        {
            this.stream = stream;
        }

        public int available() throws IOException {
            AudioInputStream local_stream = stream;
            if(local_stream != null)
                return local_stream.available();
            return 0;
        }

        public int read() throws IOException {
             byte[] b = new byte[1];
             if (read(b) == -1)
                  return -1;
             return b[0] & 0xFF;
        }

        public int read(byte[] b, int off, int len) throws IOException {
             AudioInputStream local_stream = stream;
             if(local_stream != null)
                 return local_stream.read(b, off, len);
             else
             {
                 int flen = len / 2;
                 if(silentbuffer == null || silentbuffer.length < flen)
                     silentbuffer = new float[flen];
                 SYNTH_CONVERTER.toByteArray(silentbuffer, flen, b, off);

                 silent_samples.addAndGet(len / 4);

                 return len;
             }
        }

        public WeakAudioStream(AudioInputStream stream) {
            this.stream = stream;
            weak_stream_link = new WeakReference<>(stream);
        }

        public AudioInputStream getAudioInputStream()
        {
            return new AudioInputStream(this, SYNTH_FORMAT, AudioInputStream.NOT_SPECIFIED);
        }

        public void close() throws IOException
        {
            AudioInputStream astream  = weak_stream_link.get();
            if(astream != null)
                astream.close();
        }
    }

    private static ImmutableList<Instrument> defaultInstruments = null;

    WeakAudioStream weakstream = null;

    final Object control_mutex = this;

    int voiceIDCounter = 0;

    public static final int NUMBER_OF_CHANNELS = 16;
    SoftChannel[] channels;
    final SoftChannelProxy[] external_channels = new SoftChannelProxy[NUMBER_OF_CHANNELS];
    {
        for (int i = 0; i < NUMBER_OF_CHANNELS; i++) {
            external_channels[i] = new SoftChannelProxy();
        }
    }

    public static final AudioFormat SYNTH_FORMAT = new AudioFormat(44100, 16, 2, true);
    public static final AudioFloatConverter SYNTH_CONVERTER = AudioFloatConverter.getConverter(SYNTH_FORMAT);

    private final SourceDataLine sourceDataLine = new SourceDataLine();

    private SoftAudioPusher pusher = null;
    private AudioInputStream pusher_stream = null;

    private boolean open = false;

    private final SoftLinearResampler2 resampler = new SoftLinearResampler2();

    private final static int MAX_POLY = 64;

    private SoftMainMixer mainmixer;
    private final SoftVoice[] voices = new SoftVoice[MAX_POLY];

    private final Map<String, SoftInstrument> inslist = new HashMap<>();

    private boolean loadInstruments(List<ModelInstrument> instruments) {
        if (!isOpen())
            return false;

        synchronized (control_mutex) {
            if (channels != null)
                for (SoftChannel c : channels)
                {
                    c.current_instrument = null;
                    c.current_director = null;
                }
            for (Instrument instrument : instruments) {
                String pat = patchToString(instrument.getPatch());
                inslist.put(pat, new SoftInstrument((ModelInstrument) instrument));
            }
        }

        return true;
    }

    private String patchToString(Patch patch) {
        return (patch.isPercussion() ? "p." : "") +  patch.getProgram() + "." + patch.getBank();
    }

    SoftMainMixer getMainMixer() {
        if (!isOpen())
            return null;
        return mainmixer;
    }

    SoftInstrument findInstrument(int program, int bank, int channel) {

        // Add support for GM2 banks 0x78 and 0x79
        // as specified in DLS 2.2 in Section 1.4.6
        // which allows using percussion and melodic instruments
        // on all channels
        if (bank >> 7 == 0x78 || bank >> 7 == 0x79) {
            SoftInstrument current_instrument
                    = inslist.get(program + "." + bank);
            if (current_instrument != null)
                return current_instrument;

            String p_plaf;
            if (bank >> 7 == 0x78)
                p_plaf = "p.";
            else
                p_plaf = "";

            // Instrument not found fallback to MSB:bank, LSB:0
            current_instrument = inslist.get(p_plaf + program + "."
                    + ((bank & 128) << 7));
            if (current_instrument != null)
                return current_instrument;
            // Instrument not found fallback to MSB:0, LSB:bank
            current_instrument = inslist.get(p_plaf + program + "."
                    + (bank & 128));
            if (current_instrument != null)
                return current_instrument;
            // Instrument not found fallback to MSB:0, LSB:0
            current_instrument = inslist.get(p_plaf + program + ".0");
            if (current_instrument != null)
                return current_instrument;
            // Instrument not found fallback to MSB:0, LSB:0, program=0
            current_instrument = inslist.get(p_plaf + program + "0.0");
            return current_instrument;
        }

        // Channel 10 uses percussion instruments
        String p_plaf;
        if (channel == 9)
            p_plaf = "p.";
        else
            p_plaf = "";

        SoftInstrument current_instrument
                = inslist.get(p_plaf + program + "." + bank);
        if (current_instrument != null)
            return current_instrument;
        // Instrument not found fallback to MSB:0, LSB:0
        current_instrument = inslist.get(p_plaf + program + ".0");
        if (current_instrument != null)
            return current_instrument;
        // Instrument not found fallback to MSB:0, LSB:0, program=0
        current_instrument = inslist.get(p_plaf + "0.0");
        return current_instrument;
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
        instruments.add((ModelInstrument)instrument);
        return loadInstruments(instruments);
    }

    public void unloadInstrument(Instrument instrument) {
        if ((!(instrument instanceof ModelInstrument))) {
            throw new IllegalArgumentException("Unsupported instrument: " +
                    instrument);
        }
        if (!isOpen())
            return;

        String pat = patchToString(instrument.getPatch());
        synchronized (control_mutex) {
            for (SoftChannel c: channels)
                c.current_instrument = null;
            inslist.remove(pat);
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

            weakstream = new WeakAudioStream(ais);
            ais = weakstream.getAudioInputStream();

            // can throw LineUnavailableException,
            // IllegalArgumentException, SecurityException
            sourceDataLine.open(SYNTH_FORMAT, 21168);
            sourceDataLine.start();

            // Tell mixer not fill read buffers fully.
            // This lowers latency, and tells DataPusher
            // to read in smaller amounts.
            //mainmixer.readfully = false;
            //pusher = new DataPusher(line, ais);

            int buffersize = sourceDataLine.getBufferSize();
            buffersize -= buffersize % 1200;
            buffersize = Math.max(3600, buffersize);

            ais = new SoftJitterCorrector(ais, buffersize);
            pusher = new SoftAudioPusher(sourceDataLine, ais);
            pusher_stream = ais;
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

        for (SoftVoice voice: getVoices())
            voice.resampler = resampler.openStreamer();

        return mainmixer.getInputStream();
    }

    public void close() {

        if (!isOpen())
            return;

        SoftAudioPusher pusher_to_be_closed = null;
        AudioInputStream pusher_stream_to_be_closed = null;
        synchronized (control_mutex) {
            if (pusher != null) {
                pusher_to_be_closed = pusher;
                pusher_stream_to_be_closed = pusher_stream;
                pusher = null;
                pusher_stream = null;
            }
        }

        if (pusher_to_be_closed != null) {
            // Pusher must not be closed synchronized against control_mutex,
            // this may result in synchronized conflict between pusher
            // and current thread.
            pusher_to_be_closed.stop();

            try {
                pusher_stream_to_be_closed.close();
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
