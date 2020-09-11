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
import own.impl.SourceDataLineImpl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;

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
        public volatile long silent_samples = 0;
        private int framesize = 0;
        private WeakReference<AudioInputStream> weak_stream_link;
        private AudioFloatConverter converter;
        private float[] silentbuffer = null;
        private int samplesize;

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

                 silent_samples += (long)((len / framesize));

                 if(pusher != null)
                 if(weak_stream_link.get() == null)
                 {
                     Runnable runnable = new Runnable()
                     {
                         SoftAudioPusher _pusher = pusher;
                         AudioInputStream _jitter_stream = jitter_stream;
                         SourceDataLine _sourceDataLine = sourceDataLine;
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
            weak_stream_link = new WeakReference<AudioInputStream>(stream);
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

    private static SourceDataLine testline = null;

    private static Soundbank defaultSoundBank = null;

    WeakAudioStream weakstream = null;

    final Object control_mutex = this;

    int voiceIDCounter = 0;

    // 0: default
    // 1: DLS Voice Allocation
    int voice_allocation_mode = 0;

    boolean reverb_light = true;
    boolean reverb_on = true;
    boolean chorus_on = true;
    boolean agc_on = true;

    SoftChannel[] channels;
    SoftChannelProxy[] external_channels = null;

    private boolean largemode = false;

    // 0: GM Mode off (default)
    // 1: GM Level 1
    // 2: GM Level 2
    private int gmmode = 0;

    private AudioFormat format = new AudioFormat(44100, 16, 2, true, false);

    private SourceDataLine sourceDataLine = null;

    private SoftAudioPusher pusher = null;
    private AudioInputStream pusher_stream = null;

    private float controlrate = 147f;

    private boolean open = false;

    private SoftResampler resampler;

    private int number_of_midi_channels = 16;
    private int maxpoly = 64;
    private long latency = 200000; // 200 msec
    private boolean jitter_correction = false;

    private SoftMainMixer mainmixer;
    private SoftVoice[] voices;

    private Map<String, SoftTuning> tunings
            = new HashMap<String, SoftTuning>();
    private Map<String, SoftInstrument> inslist
            = new HashMap<String, SoftInstrument>();
    private Map<String, ModelInstrument> loadedlist
            = new HashMap<String, ModelInstrument>();

    private void getBuffers(ModelInstrument instrument,
            List<ModelByteBuffer> buffers) {
        for (ModelPerformer performer : instrument.getPerformers()) {
            if (performer.getOscillators() != null) {
                for (ModelOscillator osc : performer.getOscillators()) {
                    if (osc instanceof ModelByteBufferWavetable) {
                        ModelByteBufferWavetable w = (ModelByteBufferWavetable)osc;
                        ModelByteBuffer buff = w.getBuffer();
                        if (buff != null)
                            buffers.add(buff);
                        buff = w.get8BitExtensionBuffer();
                        if (buff != null)
                            buffers.add(buff);
                    }
                }
            }
        }
    }

    private boolean loadSamples(List<ModelInstrument> instruments) {
        if (largemode)
            return true;
        List<ModelByteBuffer> buffers = new ArrayList<ModelByteBuffer>();
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

    private void processPropertyInfo() {
        this.resampler = new SoftLinearResampler2();

        setFormat(new AudioFormat(44100, 16, 2, true, false));
        controlrate = 147f;
        latency = 120000L;
        maxpoly = 64;
        reverb_on = true;
        chorus_on = true;
        agc_on = true;
        largemode = false;
        number_of_midi_channels = 16;
        jitter_correction = true;
        reverb_light = true;
    }

    private String patchToString(Patch patch) {
        if (patch instanceof ModelPatch && ((ModelPatch) patch).isPercussion())
            return "p." + patch.getProgram() + "." + patch.getBank();
        else
            return patch.getProgram() + "." + patch.getBank();
    }

    private void setFormat(AudioFormat format) {
        if (format.getChannels() > 2) {
            throw new IllegalArgumentException(
                    "Only mono and stereo audio supported.");
        }
        if (AudioFloatConverter.getConverter(format) == null)
            throw new IllegalArgumentException("Audio format not supported.");
        this.format = format;
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
            if (current_instrument != null)
                return current_instrument;
            return null;
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
        if (current_instrument != null)
            return current_instrument;
        return null;
    }

    int getVoiceAllocationMode() {
        return voice_allocation_mode;
    }

    int getGeneralMidiMode() {
        return gmmode;
    }

    float getControlRate() {
        return controlrate;
    }

    SoftVoice[] getVoices() {
        return voices;
    }

    SoftTuning getTuning(Patch patch) {
        String t_id = patchToString(patch);
        SoftTuning tuning = tunings.get(t_id);
        if (tuning == null) {
            tuning = new SoftTuning();
            tunings.put(t_id, tuning);
        }
        return tuning;
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
            return maxpoly;
        }
    }

    public MidiChannel[] getChannels() {

        synchronized (control_mutex) {
            // if (external_channels == null) => the synthesizer is not open,
            // create 16 proxy channels
            // otherwise external_channels has the same length as channels array
            if (external_channels == null) {
                external_channels = new SoftChannelProxy[16];
                for (int i = 0; i < external_channels.length; i++)
                    external_channels[i] = new SoftChannelProxy();
            }
            MidiChannel[] ret;
            if (isOpen())
                ret = new MidiChannel[channels.length];
            else
                ret = new MidiChannel[16];
            for (int i = 0; i < ret.length; i++)
                ret[i] = external_channels[i];
            return ret;
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
        if (instrument == null || (!(instrument instanceof ModelInstrument))) {
            throw new IllegalArgumentException("Unsupported instrument: " +
                    instrument);
        }
        List<ModelInstrument> instruments = new ArrayList<ModelInstrument>();
        instruments.add((ModelInstrument)instrument);
        return loadInstruments(instruments);
    }

    public void unloadInstrument(Instrument instrument) {
        if (instrument == null || (!(instrument instanceof ModelInstrument))) {
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
            for (int i = 0; i < channels.length; i++) {
                channels[i].allSoundOff();
            }
        }
    }

    public boolean remapInstrument(Instrument from, Instrument to) {

        if (from == null)
            throw new NullPointerException();
        if (to == null)
            throw new NullPointerException();
        if (!(from instanceof ModelInstrument)) {
            throw new IllegalArgumentException("Unsupported instrument: " +
                    from.toString());
        }
        if (!(to instanceof ModelInstrument)) {
            throw new IllegalArgumentException("Unsupported instrument: " +
                    to.toString());
        }
        if (!isOpen())
            return false;

        synchronized (control_mutex) {
            if (!loadedlist.containsValue(to))
                throw new IllegalArgumentException("Instrument to is not loaded.");
            unloadInstrument(from);
            ModelMappedInstrument mfrom = new ModelMappedInstrument(
                    (ModelInstrument)to, from.getPatch());
            return loadInstrument(mfrom);
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
            } catch (Exception e) {
            }
        }
        return defaultSoundBank;
    }

    public Instrument[] getAvailableInstruments() {
        Soundbank defsbk = getDefaultSoundbank();
        if (defsbk == null)
            return new Instrument[0];
        Instrument[] inslist_array = defsbk.getInstruments();
        Arrays.sort(inslist_array, new ModelInstrumentComparator());
        return inslist_array;
    }

    public Instrument[] getLoadedInstruments() {
        if (!isOpen())
            return new Instrument[0];

        synchronized (control_mutex) {
            ModelInstrument[] inslist_array =
                    new ModelInstrument[loadedlist.values().size()];
            loadedlist.values().toArray(inslist_array);
            Arrays.sort(inslist_array, new ModelInstrumentComparator());
            return inslist_array;
        }
    }

    public boolean loadAllInstruments(Soundbank soundbank) {
        List<ModelInstrument> instruments = new ArrayList<ModelInstrument>();
        for (Instrument ins: soundbank.getInstruments()) {
            if (ins == null || !(ins instanceof ModelInstrument)) {
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
        List<ModelInstrument> instruments = new ArrayList<ModelInstrument>();
        for (Patch patch: patchList) {
            Instrument ins = soundbank.getInstrument(patch);
            if (ins == null || !(ins instanceof ModelInstrument)) {
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

    public void open() throws MidiUnavailableException {
        if (isOpen()) {
            synchronized (control_mutex) {
            }
            return;
        }
        open(null);
    }

    private void open(SourceDataLine line) throws MidiUnavailableException {
        if (isOpen()) {
            synchronized (control_mutex) {
            }
            return;
        }
        synchronized (control_mutex) {
            try {
                if (line != null) {
                    // can throw IllegalArgumentException
                    setFormat(line.getFormat());
                }

                AudioInputStream ais = openStream(getFormat());

                weakstream = new WeakAudioStream(ais);
                ais = weakstream.getAudioInputStream();

                if (line == null)
                {
                    if (testline != null) {
                        line = testline;
                    } else {
                        // can throw LineUnavailableException,
                        // IllegalArgumentException, SecurityException
                        line = new SourceDataLineImpl();
                    }
                }

                double latency = this.latency;

                if (!line.isOpen()) {
                    int bufferSize = getFormat().getFrameSize()
                        * (int)(getFormat().getFrameRate() * (latency/1000000f));
                    // can throw LineUnavailableException,
                    // IllegalArgumentException, SecurityException
                    line.open(getFormat(), bufferSize);

                    // Remember that we opened that line
                    // so we can close again in SoftSynthesizer.close()
                    sourceDataLine = line;
                }
                if (!line.isActive())
                    line.start();

                int controlbuffersize = 512;
                try {
                    controlbuffersize = ais.available();
                } catch (IOException e) {
                }

                // Tell mixer not fill read buffers fully.
                // This lowers latency, and tells DataPusher
                // to read in smaller amounts.
                //mainmixer.readfully = false;
                //pusher = new DataPusher(line, ais);

                int buffersize = line.getBufferSize();
                buffersize -= buffersize % controlbuffersize;

                if (buffersize < 3 * controlbuffersize)
                    buffersize = 3 * controlbuffersize;

                if (jitter_correction) {
                    ais = new SoftJitterCorrector(ais, buffersize,
                            controlbuffersize);
                    if(weakstream != null)
                        weakstream.jitter_stream = ais;
                }
                pusher = new SoftAudioPusher(line, ais, controlbuffersize);
                pusher_stream = ais;
                pusher.start();

                if(weakstream != null)
                {
                    weakstream.pusher = pusher;
                    weakstream.sourceDataLine = sourceDataLine;
                }

            } catch (final SecurityException
                    | IllegalArgumentException e) {
                if (isOpen()) {
                    close();
                }
                // am: need MidiUnavailableException(Throwable) ctor!
                MidiUnavailableException ex = new MidiUnavailableException(
                        "Can not open line");
                ex.initCause(e);
                throw ex;
            }
        }
    }

    private AudioInputStream openStream(AudioFormat targetFormat) throws MidiUnavailableException {

        if (isOpen())
            throw new MidiUnavailableException("Synthesizer is already open");

        synchronized (control_mutex) {

            gmmode = 0;
            voice_allocation_mode = 0;

            processPropertyInfo();

            open = true;

            if (targetFormat != null)
                setFormat(targetFormat);

            voices = new SoftVoice[maxpoly];
            for (int i = 0; i < maxpoly; i++)
                voices[i] = new SoftVoice(this);

            mainmixer = new SoftMainMixer(this);

            channels = new SoftChannel[number_of_midi_channels];
            for (int i = 0; i < channels.length; i++)
                channels[i] = new SoftChannel(this, i);

            if (external_channels == null) {
                // Always create external_channels array
                // with 16 or more channels
                // so getChannels works correctly
                // when the synhtesizer is closed.
                if (channels.length < 16)
                    external_channels = new SoftChannelProxy[16];
                else
                    external_channels = new SoftChannelProxy[channels.length];
                for (int i = 0; i < external_channels.length; i++)
                    external_channels[i] = new SoftChannelProxy();
            } else {
                // We must resize external_channels array
                // but we must also copy the old SoftChannelProxy
                // into the new one
                if (channels.length > external_channels.length) {
                    SoftChannelProxy[] new_external_channels
                            = new SoftChannelProxy[channels.length];
                    for (int i = 0; i < external_channels.length; i++)
                        new_external_channels[i] = external_channels[i];
                    for (int i = external_channels.length;
                            i < new_external_channels.length; i++) {
                        new_external_channels[i] = new SoftChannelProxy();
                    }
                }
            }

            for (int i = 0; i < channels.length; i++)
                external_channels[i].setChannel(channels[i]);

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

            if (mainmixer != null)
                mainmixer.close();
            open = false;
            mainmixer = null;
            voices = null;
            channels = null;

            if (external_channels != null)
                for (int i = 0; i < external_channels.length; i++)
                    external_channels[i].setChannel(null);

            if (sourceDataLine != null) {
                sourceDataLine.close();
                sourceDataLine = null;
            }

            inslist.clear();
            loadedlist.clear();
            tunings.clear();

        }
    }

    public boolean isOpen() {
        synchronized (control_mutex) {
            return open;
        }
    }

}
