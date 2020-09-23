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

import gervill.javax.sound.midi.*;
import gervill.javax.sound.sampled.AudioFormat;
import gervill.javax.sound.sampled.AudioInputStream;
import gervill.javax.sound.sampled.AudioSystem;
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
        public SoftAudioPusher pusher = null;
        public AudioInputStream jitter_stream = null;
        public SourceDataLine sourceDataLine = null;
        public final AtomicLong silent_samples = new AtomicLong(0);
        private final int framesize;
        private final WeakReference<AudioInputStream> weak_stream_link;
        private final AudioFloatConverter converter;
        private float[] silentbuffer = null;
        private final int samplesize;

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
                 int flen = len / samplesize;
                 if(silentbuffer == null || silentbuffer.length < flen)
                     silentbuffer = new float[flen];
                 converter.toByteArray(silentbuffer, flen, b, off);

                 silent_samples.addAndGet(len / framesize);

                 if(pusher != null)
                 if(weak_stream_link.get() == null)
                 {
                     Runnable runnable = new Runnable()
                     {
                         final SoftAudioPusher _pusher = pusher;
                         final AudioInputStream _jitter_stream = jitter_stream;
                         final SourceDataLine _sourceDataLine = sourceDataLine;
                         public void run()
                         {
                             _pusher.stop();
                             if(_jitter_stream != null)
                                try {
                                    _jitter_stream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                             if(_sourceDataLine != null)
                                 _sourceDataLine.close();
                         }
                     };
                     pusher = null;
                     jitter_stream = null;
                     sourceDataLine = null;
                     new Thread(runnable).start();
                 }
                 return len;
             }
        }

        public WeakAudioStream(AudioInputStream stream) {
            this.stream = stream;
            weak_stream_link = new WeakReference<>(stream);
            converter = AudioFloatConverter.getConverter(stream.getFormat());
            samplesize = stream.getFormat().getFrameSize() / stream.getFormat().getChannels();
            framesize = stream.getFormat().getFrameSize();
        }

        public AudioInputStream getAudioInputStream()
        {
            return new AudioInputStream(this, stream.getFormat(), AudioSystem.NOT_SPECIFIED);
        }

        public void close() throws IOException
        {
            AudioInputStream astream  = weak_stream_link.get();
            if(astream != null)
                astream.close();
        }
    }

    private static Soundbank defaultSoundBank = null;

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

    private final AudioFormat format = new AudioFormat(44100, 16, 2, true);

    private final SourceDataLine sourceDataLine = new SourceDataLine();

    private SoftAudioPusher pusher = null;
    private AudioInputStream pusher_stream = null;

    private boolean open = false;

    private final SoftLinearResampler2 resampler = new SoftLinearResampler2();

    private final static int MAX_POLY = 64;
    private final long latency = 120000L;

    private SoftMainMixer mainmixer;
    private final SoftVoice[] voices = new SoftVoice[MAX_POLY];

    private final Map<String, SoftInstrument> inslist
            = new HashMap<>();
    private final Map<String, ModelInstrument> loadedlist
            = new HashMap<>();

    private void getBuffers(ModelInstrument instrument,
            List<ModelByteBuffer> buffers) {
        for (ModelPerformer performer : instrument.getPerformers()) {
            if (performer.getOscillators() != null) {
                for (ModelByteBufferWavetable osc : performer.getOscillators()) {
                    if (osc != null) {
                        ModelByteBuffer buff = osc.getBuffer();
                        if (buff != null)
                            buffers.add(buff);
                        buff = osc.get8BitExtensionBuffer();
                        if (buff != null)
                            buffers.add(buff);
                    }
                }
            }
        }
    }

    private boolean loadSamples(List<ModelInstrument> instruments) {
        List<ModelByteBuffer> buffers = new ArrayList<>();
        for (ModelInstrument instrument : instruments)
            getBuffers(instrument, buffers);
        try {
            ModelByteBuffer.loadAll(buffers);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean loadInstruments(List<ModelInstrument> instruments) {
        if (!isOpen())
            return false;
        if (!loadSamples(instruments))
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
                SoftInstrument softins
                        = new SoftInstrument((ModelInstrument) instrument);
                inslist.put(pat, softins);
                loadedlist.put(pat, (ModelInstrument) instrument);
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

    public long getLatency() {
        synchronized (control_mutex) {
            return latency;
        }
    }

    public AudioFormat getFormat() {
        synchronized (control_mutex) {
            return format;
        }
    }

    public int getMaxPolyphony() {
        synchronized (control_mutex) {
            return MAX_POLY;
        }
    }

    public MidiChannel[] getChannels() {

        synchronized (control_mutex) {
            return external_channels;
        }
    }

    public VoiceStatus[] getVoiceStatus() {
        if (!isOpen()) {
            VoiceStatus[] tempVoiceStatusArray
                    = new VoiceStatus[getMaxPolyphony()];
            for (int i = 0; i < tempVoiceStatusArray.length; i++) {
                VoiceStatus b = new VoiceStatus();
                b.active = false;
                b.bank = 0;
                b.channel = 0;
                b.note = 0;
                b.program = 0;
                b.volume = 0;
                tempVoiceStatusArray[i] = b;
            }
            return tempVoiceStatusArray;
        }

        synchronized (control_mutex) {
            VoiceStatus[] tempVoiceStatusArray = new VoiceStatus[voices.length];
            for (int i = 0; i < voices.length; i++) {
                VoiceStatus a = voices[i];
                VoiceStatus b = new VoiceStatus();
                b.active = a.active;
                b.bank = a.bank;
                b.channel = a.channel;
                b.note = a.note;
                b.program = a.program;
                b.volume = a.volume;
                tempVoiceStatusArray[i] = b;
            }
            return tempVoiceStatusArray;
        }
    }

    public boolean isSoundbankSupported(Soundbank soundbank) {
        for (Instrument ins: soundbank.getInstruments())
            if (!(ins instanceof ModelInstrument))
                return false;
        return true;
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
            loadedlist.remove(pat);
            for (SoftChannel channel : channels) {
                channel.allSoundOff();
            }
        }
    }

    public Soundbank getDefaultSoundbank() {
        synchronized (SoftSynthesizer.class) {
            if (defaultSoundBank != null)
                return defaultSoundBank;

            try {
                /*
                 * Generate emergency soundbank
                 */
                defaultSoundBank = EmergencySoundbank.createSoundbank();
            } catch (Exception ignored) {
            }
        }
        return defaultSoundBank;
    }

    public ImmutableList<Instrument> getAvailableInstruments() {
        Soundbank defsbk = getDefaultSoundbank();
        return defsbk == null ? null : defsbk.getInstruments();
    }

    public Instrument[] getLoadedInstruments() {
        if (!isOpen())
            return new Instrument[0];

        synchronized (control_mutex) {
            ModelInstrument[] inslist_array =
                    new ModelInstrument[loadedlist.values().size()];
            loadedlist.values().toArray(inslist_array);
            Arrays.sort(inslist_array, ModelInstrumentComparator.COMPARATOR);
            return inslist_array;
        }
    }

    public boolean loadAllInstruments(Soundbank soundbank) {
        List<ModelInstrument> instruments = new ArrayList<>();
        for (Instrument ins: soundbank.getInstruments()) {
            if (!(ins instanceof ModelInstrument)) {
                throw new IllegalArgumentException(
                        "Unsupported instrument: " + ins);
            }
            instruments.add((ModelInstrument)ins);
        }
        return loadInstruments(instruments);
    }

    public void unloadAllInstruments(Soundbank soundbank) {
        if (soundbank == null || !isSoundbankSupported(soundbank))
            throw new IllegalArgumentException("Unsupported soundbank: " + soundbank);

        if (!isOpen())
            return;

        for (Instrument ins: soundbank.getInstruments()) {
            if (ins instanceof ModelInstrument) {
                unloadInstrument(ins);
            }
        }
    }

    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
        List<ModelInstrument> instruments = new ArrayList<>();
        for (Patch patch: patchList) {
            Instrument ins = soundbank.getInstrument(patch);
            if (!(ins instanceof ModelInstrument)) {
                throw new IllegalArgumentException(
                        "Unsupported instrument: " + ins);
            }
            instruments.add((ModelInstrument)ins);
        }
        return loadInstruments(instruments);
    }

    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        if (soundbank == null || !isSoundbankSupported(soundbank))
            throw new IllegalArgumentException("Unsupported soundbank: " + soundbank);

        if (!isOpen())
            return;

        for (Patch pat: patchList) {
            Instrument ins = soundbank.getInstrument(pat);
            if (ins instanceof ModelInstrument) {
                unloadInstrument(ins);
            }
        }
    }

    public void open() {
        if (isOpen()) {
            return;
        }
        synchronized (control_mutex) {
            try {
                AudioInputStream ais = openStream();

                weakstream = new WeakAudioStream(ais);
                ais = weakstream.getAudioInputStream();

                int bufferSize = getFormat().getFrameSize()
                    * (int)(getFormat().getFrameRate() * (latency/1000000f));
                // can throw LineUnavailableException,
                // IllegalArgumentException, SecurityException
                sourceDataLine.open(getFormat(), bufferSize);

                sourceDataLine.start();

                int controlbuffersize = 512;
                try {
                    controlbuffersize = ais.available();
                } catch (IOException ignored) {
                }

                // Tell mixer not fill read buffers fully.
                // This lowers latency, and tells DataPusher
                // to read in smaller amounts.
                //mainmixer.readfully = false;
                //pusher = new DataPusher(line, ais);

                int buffersize = sourceDataLine.getBufferSize();
                buffersize -= buffersize % controlbuffersize;

                if (buffersize < 3 * controlbuffersize)
                    buffersize = 3 * controlbuffersize;

                ais = new SoftJitterCorrector(ais, buffersize,
                        controlbuffersize);
                weakstream.jitter_stream = ais;
                pusher = new SoftAudioPusher(sourceDataLine, ais, controlbuffersize);
                pusher_stream = ais;
                pusher.start();

                weakstream.pusher = pusher;
                weakstream.sourceDataLine = sourceDataLine;

            } catch (final SecurityException
                    | IllegalArgumentException e) {
                if (isOpen()) {
                    close();
                }
                // am: need MidiUnavailableException(Throwable) ctor!
                RuntimeException ex = new RuntimeException(
                        "Can not open line");
                ex.initCause(e);
                throw ex;
            }
        }
    }

    private AudioInputStream openStream() {

        if (isOpen())
            throw new RuntimeException("Synthesizer is already open");

        synchronized (control_mutex) {

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
            loadedlist.clear();
        }
    }

    public boolean isOpen() {
        synchronized (control_mutex) {
            return open;
        }
    }

}
