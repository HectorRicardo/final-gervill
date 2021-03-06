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

import gervill.javax.sound.midi.Instrument;
import gervill.javax.sound.midi.MidiChannel;
import own.main.ImmutableList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Software Synthesizer MIDI channel class.
 *
 * @author Karl Helgason
 */
public final class SoftChannel implements MidiChannel {

    private static final boolean[] dontResetControls = new boolean[128];
    private static final int RPN_NULL_VALUE = (127 << 7) + 127;

    static {
        Arrays.fill(dontResetControls, false);

        dontResetControls[0] = true;   // Bank Select (MSB)
        dontResetControls[32] = true;  // Bank Select (LSB)
        dontResetControls[7] = true;   // Channel Volume (MSB)
        dontResetControls[8] = true;   // Balance (MSB)
        dontResetControls[10] = true;  // Pan (MSB)
        dontResetControls[11] = true;  // Expression (MSB)
        dontResetControls[91] = true;  // Effects 1 Depth (default: Reverb Send)
        dontResetControls[92] = true;  // Effects 2 Depth (default: Tremolo Depth)
        dontResetControls[93] = true;  // Effects 3 Depth (default: Chorus Send)
        dontResetControls[94] = true;  // Effects 4 Depth (default: Celeste [Detune] Depth)
        dontResetControls[95] = true;  // Effects 5 Depth (default: Phaser Depth)
        dontResetControls[70] = true;  // Sound Controller 1 (default: Sound Variation)
        dontResetControls[71] = true;  // Sound Controller 2 (default: Timbre / Harmonic Quality)
        dontResetControls[72] = true;  // Sound Controller 3 (default: Release Time)
        dontResetControls[73] = true;  // Sound Controller 4 (default: Attack Time)
        dontResetControls[74] = true;  // Sound Controller 5 (default: Brightness)
        dontResetControls[75] = true;  // Sound Controller 6 (GM2 default: Decay Time)
        dontResetControls[76] = true;  // Sound Controller 7 (GM2 default: Vibrato Rate)
        dontResetControls[77] = true;  // Sound Controller 8 (GM2 default: Vibrato Depth)
        dontResetControls[78] = true;  // Sound Controller 9 (GM2 default: Vibrato Delay)
        dontResetControls[79] = true;  // Sound Controller 10 (GM2 default: Undefined)
        dontResetControls[120] = true; // All Sound Off
        dontResetControls[121] = true; // Reset All Controllers
        dontResetControls[122] = true; // Local Control On/Off
        dontResetControls[123] = true; // All Notes Off
        dontResetControls[124] = true; // Omni Mode Off
        dontResetControls[125] = true; // Omni Mode On
        dontResetControls[126] = true; // Poly Mode Off
        dontResetControls[127] = true; // Poly Mode On

        dontResetControls[6] = true;   // Data Entry (MSB)
        dontResetControls[38] = true;  // Data Entry (LSB)
        dontResetControls[96] = true;  // Data Increment
        dontResetControls[97] = true;  // Data Decrement
        dontResetControls[98] = true;  // Non-Registered Parameter Number (LSB)
        dontResetControls[99] = true;  // Non-Registered Parameter Number(MSB)
        dontResetControls[100] = true; // RPN = Null
        dontResetControls[101] = true; // RPN = Null

    }

    final int[] portamento_lastnote = new int[128];
    final Map<Integer, int[]> co_midi_rpn_rpn_i = new HashMap<>();
    final Map<Integer, double[]> co_midi_rpn_rpn = new HashMap<>();
    final Map<Integer, int[]> co_midi_nrpn_nrpn_i = new HashMap<>();
    final Map<Integer, double[]> co_midi_nrpn_nrpn = new HashMap<>();
    private final Object control_mutex;
    private final int channel;
    private final SoftVoice[] voices;
    private final SoftSynthesizer synthesizer;
    private final int[] polypressure = new int[128];
    private final int[] controller = new int[128];
    private final double[] co_midi_pitch = new double[1];
    private final double[] co_midi_channel_pressure = new double[1];
    private final ImmutableList<MidiControlObject> co_midi = ImmutableList.create(128, index -> new MidiControlObject());
    private final double[][] co_midi_cc_cc = new double[128][1];
    private final SoftControl co_midi_cc = new SoftControl() {
        final double[][] cc = co_midi_cc_cc;

        public double[] get(int instance, String name) {
            if (name == null)
                return null;
            return cc[Integer.parseInt(name)];
        }
    };
    private final SoftControl co_midi_rpn = new SoftControl() {
        final Map<Integer, double[]> rpn = co_midi_rpn_rpn;

        public double[] get(int instance, String name) {
            if (name == null)
                return null;
            int iname = Integer.parseInt(name);
            return rpn.computeIfAbsent(iname, k -> new double[1]);
        }
    };
    private final SoftControl co_midi_nrpn = new SoftControl() {
        final Map<Integer, double[]> nrpn = co_midi_nrpn_nrpn;

        public double[] get(int instance, String name) {
            if (name == null)
                return null;
            int iname = Integer.parseInt(name);
            return nrpn.computeIfAbsent(iname, k -> new double[1]);
        }
    };
    private final int[] lastVelocity = new int[128];
    double portamento_time = 1; // keyschanges per control buffer time
    int portamento_lastnote_ix = 0;
    SoftInstrument current_instrument = null;
    ModelStandardIndexedDirector current_director = null;
    boolean sustain = false;
    boolean[][] keybasedcontroller_active = null;
    double[][] keybasedcontroller_value = null;
    private int rpn_control = RPN_NULL_VALUE;
    private int nrpn_control = RPN_NULL_VALUE;
    private boolean portamento = false;
    private boolean mono = false;
    private boolean mute = false;
    private boolean solo = false;
    private boolean solomute = false;
    private int channelpressure = 0;
    private int pitchbend;
    private int prevVoiceID;
    private boolean firstVoice = true;
    private int voiceNo = 0;
    private int play_noteNumber = 0;
    private int play_velocity = 0;
    private boolean play_releasetriggered = false;

    public SoftChannel(SoftSynthesizer synth, int channel) {
        this.channel = channel;
        this.voices = synth.getVoices();
        this.synthesizer = synth;
        control_mutex = synth.control_mutex;
        resetAllControllers(true);
    }

    private static int restrict7Bit(int value) {
        if (value < 0) return 0;
        return Math.min(value, 127);
    }

    private static int restrict14Bit(int value) {
        if (value < 0) return 0;
        return Math.min(value, 16256);
    }

    private int findFreeVoice(int x) {
        if (x == -1) {
            // x = -1 means that there where no available voice
            // last time we called findFreeVoice
            // and it hasn't changed because no audio has been
            // rendered in the meantime.
            // Therefore we have to return -1.
            return -1;
        }
        for (int i = x; i < voices.length; i++)
            if (!voices[i].active)
                return i;

        // No free voice was found, we must steal one

        // Default Voice Allocation
        //  * Find voice that is on
        //    and Find voice which has lowest voiceID ( oldest voice)
        //  * Or find voice that is off
        //    and Find voice which has lowest voiceID ( oldest voice)

        int voiceNo = -1;

        SoftVoice v = null;
        // Search for oldest voice in off state
        for (int j = 0; j < voices.length; j++) {
            if (voices[j].stealer_channel == null && !voices[j].on) {
                if (v == null) {
                    v = voices[j];
                    voiceNo = j;
                }
                if (voices[j].voiceID < v.voiceID) {
                    v = voices[j];
                    voiceNo = j;
                }
            }
        }
        // Search for oldest voice in on state
        if (voiceNo == -1) {

            for (int j = 0; j < voices.length; j++) {
                if (voices[j].stealer_channel == null) {
                    if (v == null) {
                        v = voices[j];
                        voiceNo = j;
                    }
                    if (voices[j].voiceID < v.voiceID) {
                        v = voices[j];
                        voiceNo = j;
                    }
                }
            }
        }

        return voiceNo;
    }

    void initVoice(SoftVoice voice, SoftPerformer p, int voiceID,
                   int noteNumber, int velocity, ModelConnectionBlock[] connectionBlocks,
                   boolean releaseTriggered) {
        if (voice.active) {
            // Voice is active , we must steal the voice
            voice.stealer_channel = this;
            voice.stealer_performer = p;
            voice.stealer_voiceID = voiceID;
            voice.stealer_noteNumber = noteNumber;
            voice.stealer_velocity = velocity;
            voice.stealer_extendedConnectionBlocks = connectionBlocks;
            voice.stealer_releaseTriggered = releaseTriggered;
            for (SoftVoice softVoice : voices)
                if (softVoice.active && softVoice.voiceID == voice.voiceID)
                    softVoice.soundOff();
            return;
        }

        voice.extendedConnectionBlocks = connectionBlocks;
        voice.releaseTriggered = releaseTriggered;
        voice.voiceID = voiceID;
        voice.exclusiveClass = p.exclusiveClass;
        voice.softchannel = this;
        voice.channel = channel;
        voice.performer = p;
        voice.objects.clear();
        voice.objects.put("midi", co_midi.get(noteNumber));
        voice.objects.put("midi_cc", co_midi_cc);
        voice.objects.put("midi_rpn", co_midi_rpn);
        voice.objects.put("midi_nrpn", co_midi_nrpn);
        voice.noteOn(noteNumber, velocity);
        voice.setMute(mute);
        voice.setSoloMute(solomute);
        if (releaseTriggered)
            return;
        if (controller[84] != 0) {
            int myNote = controller[84];
            assert myNote >= 0 && myNote < 128;
            voice.co_noteon_keynumber[0] = myNote / 128f;
            voice.portamento = true;
            controlChange(84, 0);
        } else if (portamento) {
            if (mono) {
                if (portamento_lastnote[0] != -1) {
                    int myNote = portamento_lastnote[0];
                    assert myNote >= 0 && myNote < 128;
                    voice.co_noteon_keynumber[0] = myNote / 128f;
                    voice.portamento = true;
                    controlChange(84, 0);
                }
                portamento_lastnote[0] = noteNumber;
            } else {
                if (portamento_lastnote_ix != 0) {
                    portamento_lastnote_ix--;
                    int myNote = portamento_lastnote[portamento_lastnote_ix];
                    assert myNote >= 0 && myNote < 128;
                    voice.co_noteon_keynumber[0] = myNote / 128f;
                    voice.portamento = true;
                }
            }
        }
    }

    public void noteOn(int noteNumber, int velocity) {
        noteNumber = restrict7Bit(noteNumber);
        velocity = restrict7Bit(velocity);
        noteOn_internal(noteNumber, velocity);
    }

    private void noteOn_internal(int noteNumber, int velocity) {

        if (velocity == 0) {
            noteOff_internal(noteNumber);
            return;
        }

        synchronized (control_mutex) {
            if (sustain) {
                sustain = false;
                for (SoftVoice voice : voices) {
                    if ((voice.sustain || voice.on)
                            && voice.channel == channel && voice.active
                            && voice.note == noteNumber) {
                        voice.sustain = false;
                        voice.on = true;
                        voice.noteOff();
                    }
                }
                sustain = true;
            }

            if (mono) {
                if (portamento) {
                    boolean n_found = false;
                    for (SoftVoice voice : voices) {
                        if (voice.on && voice.channel == channel
                                && voice.active
                                && !voice.releaseTriggered) {
                            voice.portamento = true;
                            voice.setNote(noteNumber);
                            n_found = true;
                        }
                    }
                    if (n_found) {
                        portamento_lastnote[0] = noteNumber;
                        return;
                    }
                }

                if (controller[84] != 0) {
                    boolean n_found = false;
                    for (SoftVoice voice : voices) {
                        if (voice.on && voice.channel == channel
                                && voice.active
                                && voice.note == controller[84]
                                && !voice.releaseTriggered) {
                            voice.portamento = true;
                            voice.setNote(noteNumber);
                            n_found = true;
                        }
                    }
                    controlChange(84, 0);
                    if (n_found)
                        return;
                }
            }

            if (mono)
                allNotesOff();

            prevVoiceID = synthesizer.voiceIDCounter++;
            firstVoice = true;
            voiceNo = 0;

            assert noteNumber >= 0 && noteNumber < 128;
            play_noteNumber = noteNumber;
            play_velocity = velocity;
            play_releasetriggered = false;
            lastVelocity[noteNumber] = velocity;
            current_director.noteOn(noteNumber, velocity);

            /*
            SoftPerformer[] performers = current_instrument.getPerformers();
            for (int i = 0; i < performers.length; i++) {
                SoftPerformer p = performers[i];
                if (p.keyFrom <= tunedKey && p.keyTo >= tunedKey) {
                    if (p.velFrom <= velocity && p.velTo >= velocity) {
                        if (firstVoice) {
                            firstVoice = false;
                            if (p.exclusiveClass != 0) {
                                int x = p.exclusiveClass;
                                for (int j = 0; j < voices.length; j++) {
                                    if (voices[j].active
                                            && voices[j].channel == channel
                                            && voices[j].exclusiveClass == x) {
                                        if (!(p.selfNonExclusive
                                                && voices[j].note == noteNumber))
                                            voices[j].shutdown();
                                    }
                                }
                            }
                        }
                        voiceNo = findFreeVoice(voiceNo);
                        if (voiceNo == -1)
                            return;
                        initVoice(voices[voiceNo], p, prevVoiceID, noteNumber,
                                velocity);
                    }
                }
            }
            */
        }
    }

    public void noteOff(int noteNumber, int velocity) {
        noteNumber = restrict7Bit(noteNumber);
        noteOff_internal(noteNumber);

    }

    private void noteOff_internal(int noteNumber) {
        synchronized (control_mutex) {

            if (!mono) {
                if (portamento) {
                    if (portamento_lastnote_ix != 127) {
                        portamento_lastnote[portamento_lastnote_ix] = noteNumber;
                        portamento_lastnote_ix++;
                    }
                }
            }

            for (SoftVoice voice : voices) {
                if (voice.on && voice.channel == channel
                        && voice.note == noteNumber
                        && !voice.releaseTriggered) {
                    voice.noteOff();
                }
                // We must also check stolen voices
                if (voice.stealer_channel == this && voice.stealer_noteNumber == noteNumber) {
                    voice.stealer_releaseTriggered = false;
                    voice.stealer_channel = null;
                    voice.stealer_performer = null;
                    voice.stealer_voiceID = -1;
                    voice.stealer_noteNumber = 0;
                    voice.stealer_velocity = 0;
                    voice.stealer_extendedConnectionBlocks = null;
                }
            }

            // Try play back note-off triggered voices,

            prevVoiceID = synthesizer.voiceIDCounter++;
            firstVoice = true;
            voiceNo = 0;

            play_noteNumber = noteNumber;
            play_velocity = lastVelocity[noteNumber];
            play_releasetriggered = true;
            if (current_director == null) {
                throw new NullPointerException("current_director");
            }

        }
    }

    public void play(int performerIndex, ModelConnectionBlock[] connectionBlocks) {

        int noteNumber = play_noteNumber;
        int velocity = play_velocity;
        boolean releasetriggered = play_releasetriggered;

        SoftPerformer p = current_instrument.getPerformers().get(performerIndex);

        if (firstVoice) {
            firstVoice = false;
            if (p.exclusiveClass != 0) {
                int x = p.exclusiveClass;
                for (SoftVoice voice : voices) {
                    if (voice.active && voice.channel == channel
                            && voice.exclusiveClass == x) {
                        if (!(p.selfNonExclusive && voice.note == noteNumber))
                            voice.shutdown();
                    }
                }
            }
        }

        voiceNo = findFreeVoice(voiceNo);

        if (voiceNo == -1)
            return;

        initVoice(voices[voiceNo], p, prevVoiceID, noteNumber, velocity,
                connectionBlocks, releasetriggered);
    }

    public void noteOff(int noteNumber) {
        if (noteNumber < 0 || noteNumber > 127) return;
        noteOff_internal(noteNumber);
    }

    public void setPolyPressure(int noteNumber, int pressure) {
        noteNumber = restrict7Bit(noteNumber);
        pressure = restrict7Bit(pressure);

        synchronized (control_mutex) {
            co_midi.get(noteNumber).get(0, "poly_pressure")[0] = pressure * (1.0 / 128.0);
            polypressure[noteNumber] = pressure;
            for (SoftVoice voice : voices) {
                if (voice.active && voice.note == noteNumber)
                    voice.setPolyPressure();
            }
        }
    }

    public int getPolyPressure(int noteNumber) {
        synchronized (control_mutex) {
            return polypressure[noteNumber];
        }
    }

    public int getChannelPressure() {
        synchronized (control_mutex) {
            return channelpressure;
        }
    }

    public void setChannelPressure(int pressure) {
        pressure = restrict7Bit(pressure);
        synchronized (control_mutex) {
            co_midi_channel_pressure[0] = pressure * (1.0 / 128.0);
            channelpressure = pressure;
            for (SoftVoice voice : voices) {
                if (voice.active)
                    voice.setChannelPressure();
            }
        }
    }

    public void controlChangePerNote(int noteNumber, int controller, int value) {

/*
 CC# | nn   | Name                    | vv             | default    | description
-----|------|-------------------------|----------------|------------|-------------------------------
7    |07H   |Note Volume              |00H-40H-7FH     |40H         |0-100-(127/64)*100(%)(Relative)
10   |0AH   |*Pan                     |00H-7FH absolute|Preset Value|Left-Center-Right (absolute)
33-63|21-3FH|LSB for                  |01H-1FH         |            |
71   |47H   |Timbre/Harmonic Intensity|00H-40H-7FH     |40H (???)   |
72   |48H   |Release Time             |00H-40H-7FH     |40H (???)   |
73   |49H   |Attack Time              |00H-40H-7FH     |40H (???)   |
74   |4AH   |Brightness               |00H-40H-7FH     |40H (???)   |
75   |4BH   |Decay Time               |00H-40H-7FH     |40H (???)   |
76   |4CH   |Vibrato Rate             |00H-40H-7FH     |40H (???)   |
77   |4DH   |Vibrato Depth            |00H-40H-7FH     |40H (???)   |
78   |4EH   |Vibrato Delay            |00H-40H-7FH     |40H (???)   |
91   |5BH   |*Reverb Send             |00H-7FH absolute|Preset Value|Left-Center-Right (absolute)
93   |5DH   |*Chorus Send             |00H-7FH absolute|Preset Value|Left-Center-Right (absolute)
120  |78H   |**Fine Tuning            |00H-40H-7FH     |40H (???)   |
121  |79H   |**Coarse Tuning          |00H-40H-7FH     |40H (???)   |
*/

        if (keybasedcontroller_active == null) {
            keybasedcontroller_active = new boolean[128][];
            keybasedcontroller_value = new double[128][];
        }
        if (keybasedcontroller_active[noteNumber] == null) {
            keybasedcontroller_active[noteNumber] = new boolean[128];
            Arrays.fill(keybasedcontroller_active[noteNumber], false);
            keybasedcontroller_value[noteNumber] = new double[128];
            Arrays.fill(keybasedcontroller_value[noteNumber], 0);
        }

        if (value == -1) {
            keybasedcontroller_active[noteNumber][controller] = false;
        } else {
            keybasedcontroller_active[noteNumber][controller] = true;
            keybasedcontroller_value[noteNumber][controller] = value / 128.0;
        }

        if (controller < 120) {
            for (SoftVoice voice : voices)
                if (voice.active)
                    voice.controlChange(controller);
        } else if (controller == 120) {
            for (SoftVoice voice : voices)
                if (voice.active)
                    voice.rpnChange(1);
        } else if (controller == 121) {
            for (SoftVoice voice : voices)
                if (voice.active)
                    voice.rpnChange(2);
        }

    }

    public void controlChange(int controller, int value) {
        controller = restrict7Bit(controller);
        value = restrict7Bit(value);

        synchronized (control_mutex) {
            switch (controller) {
            /*
            Map<String, int[]>co_midi_rpn_rpn_i = new HashMap<String, int[]>();
            Map<String, double[]>co_midi_rpn_rpn = new HashMap<String, double[]>();
            Map<String, int[]>co_midi_nrpn_nrpn_i = new HashMap<String, int[]>();
            Map<String, double[]>co_midi_nrpn_nrpn = new HashMap<String, double[]>();
             */

                case 5:
                    // This produce asin-like curve
                    // as described in General Midi Level 2 Specification, page 6
                    double x = -Math.asin((value / 128.0) * 2 - 1) / Math.PI + 0.5;
                    x = Math.pow(100000.0, x) / 100.0;  // x is now cent/msec
                    // Convert x from cent/msec to key/controlbuffertime
                    x = x / 100.0;                      // x is now keys/msec
                    x = x * 1000.0;                     // x is now keys/sec
                    x = x / 147f; // x is now keys/controlbuffertime
                    portamento_time = x;
                    break;
                case 6:
                case 38:
                case 96:
                case 97:
                    int val = 0;
                    if (nrpn_control != RPN_NULL_VALUE) {
                        int[] val_i = co_midi_nrpn_nrpn_i.get(nrpn_control);
                        if (val_i != null)
                            val = val_i[0];
                    }
                    if (rpn_control != RPN_NULL_VALUE) {
                        int[] val_i = co_midi_rpn_rpn_i.get(rpn_control);
                        if (val_i != null)
                            val = val_i[0];
                    }

                    if (controller == 6)
                        val = (val & 127) + (value << 7);
                    else if (controller == 38)
                        val = (val & (127 << 7)) + value;
                    else {
                        int step = 1;
                        if (rpn_control == 2 || rpn_control == 3 || rpn_control == 4)
                            step = 128;
                        if (controller == 96)
                            val += step;
                        if (controller == 97)
                            val -= step;
                    }

                    if (nrpn_control != RPN_NULL_VALUE)
                        nrpnChange(nrpn_control, val);
                    if (rpn_control != RPN_NULL_VALUE)
                        rpnChange(rpn_control, val);

                    break;
                case 64: // Hold1 (Damper) (cc#64)
                    boolean on = value >= 64;
                    if (sustain != on) {
                        sustain = on;
                        if (!on) {
                            for (SoftVoice voice : voices) {
                                if (voice.active && voice.sustain &&
                                        voice.channel == channel) {
                                    voice.sustain = false;
                                    if (!voice.on) {
                                        voice.on = true;
                                        voice.noteOff();
                                    }
                                }
                            }
                        } else {
                            for (SoftVoice voice : voices)
                                if (voice.active && voice.channel == channel)
                                    voice.redamp();
                        }
                    }
                    break;
                case 65:
                    //allNotesOff();
                    portamento = value >= 64;
                    portamento_lastnote[0] = -1;
                /*
                for (int i = 0; i < portamento_lastnote.length; i++)
                    portamento_lastnote[i] = -1;
                 */
                    portamento_lastnote_ix = 0;
                    break;
                case 66: // Sostenuto (cc#66)
                    on = value >= 64;
                    if (on) {
                        for (SoftVoice voice : voices) {
                            if (voice.active && voice.on &&
                                    voice.channel == channel) {
                                voice.sostenuto = true;
                            }
                        }
                    }
                    if (!on) {
                        for (SoftVoice voice : voices) {
                            if (voice.active && voice.sostenuto &&
                                    voice.channel == channel) {
                                voice.sostenuto = false;
                                if (!voice.on) {
                                    voice.on = true;
                                    voice.noteOff();
                                }
                            }
                        }
                    }
                    break;
                case 98:
                    nrpn_control = (nrpn_control & (127 << 7)) + value;
                    rpn_control = RPN_NULL_VALUE;
                    break;
                case 99:
                    nrpn_control = (nrpn_control & 127) + (value << 7);
                    rpn_control = RPN_NULL_VALUE;
                    break;
                case 100:
                    rpn_control = (rpn_control & (127 << 7)) + value;
                    nrpn_control = RPN_NULL_VALUE;
                    break;
                case 101:
                    rpn_control = (rpn_control & 127) + (value << 7);
                    nrpn_control = RPN_NULL_VALUE;
                    break;
                case 120:
                    allSoundOff();
                    break;
                case 121:
                    resetAllControllers(value == 127);
                    break;
                case 123:
                    allNotesOff();
                    break;
                case 124:
                    setOmni(false);
                    break;
                case 125:
                    setOmni(true);
                    break;
                case 126:
                    if (value == 1)
                        setMono(true);
                    break;
                case 127:
                    setMono(false);
                    break;

                default:
                    break;
            }

            co_midi_cc_cc[controller][0] = value * (1.0 / 128.0);

            if (controller == 0x00 || controller == 0x20) {
                return;
            }

            this.controller[controller] = value;
            if (controller < 0x20)
                this.controller[controller + 0x20] = 0;

            for (SoftVoice voice : voices)
                if (voice.active)
                    voice.controlChange(controller);

        }
    }

    public int getController(int controller) {
        synchronized (control_mutex) {
            // Should only return lower 7 bits,
            // even when controller is "boosted" higher.
            return this.controller[controller] & 127;
        }
    }

    public void instrumentChange(Instrument instrument) {
        current_instrument = synthesizer.findInstrument((ModelInstrument) instrument);
        current_director = current_instrument.getDirector(this);
    }

    public int getPitchBend() {
        synchronized (control_mutex) {
            return pitchbend;
        }
    }

    public void setPitchBend(int bend) {
        bend = restrict14Bit(bend);
        synchronized (control_mutex) {
            co_midi_pitch[0] = bend * (1.0 / 16384.0);
            pitchbend = bend;
            for (SoftVoice voice : voices)
                if (voice.active)
                    voice.setPitchBend();
        }
    }

    public void nrpnChange(int controller, int value) {

        /*
        System.out.println("(" + channel + ").nrpnChange("
                + Integer.toHexString(controller >> 7)
                + " " + Integer.toHexString(controller & 127)
                + ", " + Integer.toHexString(value >> 7)
                + " " + Integer.toHexString(value & 127) + ")");
         */

        if (controller == (0x01 << 7) + (0x08)) // Vibrato Rate
            controlChange(76, value >> 7);
        if (controller == (0x01 << 7) + (0x09)) // Vibrato Depth
            controlChange(77, value >> 7);
        if (controller == (0x01 << 7) + (0x0A)) // Vibrato Delay
            controlChange(78, value >> 7);
        if (controller == (0x01 << 7) + (0x20)) // Brightness
            controlChange(74, value >> 7);
        if (controller == (0x01 << 7) + (0x21)) // Filter Resonance
            controlChange(71, value >> 7);
        if (controller == (0x01 << 7) + (0x63)) // Attack Time
            controlChange(73, value >> 7);
        if (controller == (0x01 << 7) + (0x64)) // Decay Time
            controlChange(75, value >> 7);
        if (controller == (0x01 << 7) + (0x66)) // Release Time
            controlChange(72, value >> 7);

        if (controller >> 7 == 0x18) // Pitch coarse
            controlChangePerNote(controller % 128, 120, value >> 7);
        if (controller >> 7 == 0x1A) // Volume
            controlChangePerNote(controller % 128, 7, value >> 7);
        if (controller >> 7 == 0x1C) // Panpot
            controlChangePerNote(controller % 128, 10, value >> 7);
        if (controller >> 7 == 0x1D) // Reverb
            controlChangePerNote(controller % 128, 91, value >> 7);
        if (controller >> 7 == 0x1E) // Chorus
            controlChangePerNote(controller % 128, 93, value >> 7);

        int[] val_i = co_midi_nrpn_nrpn_i.get(controller);
        double[] val_d = co_midi_nrpn_nrpn.get(controller);
        if (val_i == null) {
            val_i = new int[1];
            co_midi_nrpn_nrpn_i.put(controller, val_i);
        }
        if (val_d == null) {
            val_d = new double[1];
            co_midi_nrpn_nrpn.put(controller, val_d);
        }
        val_i[0] = value;
        val_d[0] = val_i[0] * (1.0 / 16384.0);

        for (SoftVoice voice : voices)
            if (voice.active)
                voice.nrpnChange(controller);

    }

    public void rpnChange(int controller, int value) {

        /*
        System.out.println("(" + channel + ").rpnChange("
                + Integer.toHexString(controller >> 7)
                + " " + Integer.toHexString(controller & 127)
                + ", " + Integer.toHexString(value >> 7)
                + " " + Integer.toHexString(value & 127) + ")");
         */

        int[] val_i = co_midi_rpn_rpn_i.get(controller);
        double[] val_d = co_midi_rpn_rpn.get(controller);
        if (val_i == null) {
            val_i = new int[1];
            co_midi_rpn_rpn_i.put(controller, val_i);
        }
        if (val_d == null) {
            val_d = new double[1];
            co_midi_rpn_rpn.put(controller, val_d);
        }
        val_i[0] = value;
        val_d[0] = val_i[0] * (1.0 / 16384.0);

        for (SoftVoice voice : voices)
            if (voice.active)
                voice.rpnChange(controller);
    }

    public void resetAllControllers() {
        resetAllControllers(false);
    }

    public void resetAllControllers(boolean allControls) {
        synchronized (control_mutex) {

            for (int i = 0; i < 128; i++) {
                setPolyPressure(i, 0);
            }
            setChannelPressure(0);
            setPitchBend(8192);
            for (int i = 0; i < 128; i++) {
                if (!dontResetControls[i])
                    controlChange(i, 0);
            }

            controlChange(71, 64); // Filter Resonance
            controlChange(72, 64); // Release Time
            controlChange(73, 64); // Attack Time
            controlChange(74, 64); // Brightness
            controlChange(75, 64); // Decay Time
            controlChange(76, 64); // Vibrato Rate
            controlChange(77, 64); // Vibrato Depth
            controlChange(78, 64); // Vibrato Delay

            controlChange(8, 64); // Balance
            controlChange(11, 127); // Expression
            controlChange(98, 127); // NRPN Null
            controlChange(99, 127); // NRPN Null
            controlChange(100, 127); // RPN = Null
            controlChange(101, 127); // RPN = Null

            // see DLS 2.1 (Power-on Default Values)
            if (allControls) {

                keybasedcontroller_active = null;
                keybasedcontroller_value = null;

                controlChange(7, 100); // Volume
                controlChange(10, 64); // Pan
                controlChange(91, 40); // Reverb

                for (int controller : co_midi_rpn_rpn.keySet()) {
                    // don't reset tuning settings
                    if (controller != 3 && controller != 4)
                        rpnChange(controller, 0);
                }
                for (int controller : co_midi_nrpn_nrpn.keySet())
                    nrpnChange(controller, 0);
                rpnChange(0, 2 << 7);   // Bitch Bend sensitivity
                rpnChange(1, 64 << 7);  // Channel fine tunning
                rpnChange(2, 64 << 7);  // Channel Coarse Tuning
                rpnChange(5, 64);       // Modulation Depth, +/- 50 cent

            }

        }
    }

    public void allNotesOff() {
        synchronized (control_mutex) {
            for (SoftVoice voice : voices)
                if (voice.on && voice.channel == channel
                        && !voice.releaseTriggered) {
                    voice.noteOff();
                }
        }
    }

    public void allSoundOff() {
        synchronized (control_mutex) {
            for (SoftVoice voice : voices)
                if (voice.on && voice.channel == channel)
                    voice.soundOff();
        }
    }

    public boolean getMono() {
        synchronized (control_mutex) {
            return mono;
        }
    }

    public void setMono(boolean on) {
        synchronized (control_mutex) {
            allNotesOff();
            mono = on;
        }
    }

    public boolean getOmni() {
        return false;
    }

    public void setOmni(boolean on) {
        allNotesOff();
        // Omni is not supported by GM2
    }

    public boolean getMute() {
        synchronized (control_mutex) {
            return mute;
        }
    }

    public void setMute(boolean mute) {
        synchronized (control_mutex) {
            this.mute = mute;
            for (SoftVoice voice : voices)
                if (voice.active && voice.channel == channel)
                    voice.setMute(mute);
        }
    }

    private void setSoloMute(boolean mute) {
        synchronized (control_mutex) {
            if (solomute == mute)
                return;
            this.solomute = mute;
            for (SoftVoice voice : voices)
                if (voice.active && voice.channel == channel)
                    voice.setSoloMute(solomute);
        }
    }

    public boolean getSolo() {
        synchronized (control_mutex) {
            return solo;
        }
    }

    public void setSolo(boolean soloState) {

        synchronized (control_mutex) {
            this.solo = soloState;

            boolean soloinuse = false;
            for (SoftChannel c : synthesizer.channels) {
                if (c.solo) {
                    soloinuse = true;
                    break;
                }
            }

            if (!soloinuse) {
                for (SoftChannel c : synthesizer.channels)
                    c.setSoloMute(false);
                return;
            }

            for (SoftChannel c : synthesizer.channels)
                c.setSoloMute(!c.solo);

        }

    }

    private final class MidiControlObject implements SoftControl {
        private final double[] pitch = co_midi_pitch;
        private final double[] channel_pressure = co_midi_channel_pressure;
        private final double[] poly_pressure = new double[1];

        public double[] get(int instance, String name) {
            if (name == null)
                return null;
            if (name.equals("pitch"))
                return pitch;
            if (name.equals("channel_pressure"))
                return channel_pressure;
            if (name.equals("poly_pressure"))
                return poly_pressure;
            return null;
        }
    }
}
