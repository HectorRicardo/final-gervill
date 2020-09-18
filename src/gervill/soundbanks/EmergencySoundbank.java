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
package gervill.soundbanks;

import gervill.com.sun.media.sound.AudioFloatConverter;
import gervill.javax.sound.midi.Instrument;
import gervill.javax.sound.midi.Patch;
import gervill.javax.sound.sampled.AudioFormat;

import java.util.*;

/**
 * Emergency Soundbank generator.
 * Used when no other default soundbank can be found.
 *
 * @author Karl Helgason
 */
public final class EmergencySoundbank {

    private final static String[] general_midi_instruments = {
        "Acoustic Grand Piano",
        "Bright Acoustic Piano",
        "Electric Grand Piano",
        "Honky-tonk Piano",
        "Electric Piano 1",
        "Electric Piano 2",
        "Harpsichord",
        "Clavi",
        "Celesta",
        "Glockenspiel",
        "Music Box",
        "Vibraphone",
        "Marimba",
        "Xylophone",
        "Tubular Bells",
        "Dulcimer",
        "Drawbar Organ",
        "Percussive Organ",
        "Rock Organ",
        "Church Organ",
        "Reed Organ",
        "Accordion",
        "Harmonica",
        "Tango Accordion",
        "Acoustic Guitar (nylon)",
        "Acoustic Guitar (steel)",
        "Electric Guitar (jazz)",
        "Electric Guitar (clean)",
        "Electric Guitar (muted)",
        "Overdriven Guitar",
        "Distortion Guitar",
        "Guitar harmonics",
        "Acoustic Bass",
        "Electric Bass (finger)",
        "Electric Bass (pick)",
        "Fretless Bass",
        "Slap Bass 1",
        "Slap Bass 2",
        "Synth Bass 1",
        "Synth Bass 2",
        "Violin",
        "Viola",
        "Cello",
        "Contrabass",
        "Tremolo Strings",
        "Pizzicato Strings",
        "Orchestral Harp",
        "Timpani",
        "String Ensemble 1",
        "String Ensemble 2",
        "SynthStrings 1",
        "SynthStrings 2",
        "Choir Aahs",
        "Voice Oohs",
        "Synth Voice",
        "Orchestra Hit",
        "Trumpet",
        "Trombone",
        "Tuba",
        "Muted Trumpet",
        "French Horn",
        "Brass Section",
        "SynthBrass 1",
        "SynthBrass 2",
        "Soprano Sax",
        "Alto Sax",
        "Tenor Sax",
        "Baritone Sax",
        "Oboe",
        "English Horn",
        "Bassoon",
        "Clarinet",
        "Piccolo",
        "Flute",
        "Recorder",
        "Pan Flute",
        "Blown Bottle",
        "Shakuhachi",
        "Whistle",
        "Ocarina",
        "Lead 1 (square)",
        "Lead 2 (sawtooth)",
        "Lead 3 (calliope)",
        "Lead 4 (chiff)",
        "Lead 5 (charang)",
        "Lead 6 (voice)",
        "Lead 7 (fifths)",
        "Lead 8 (bass + lead)",
        "Pad 1 (new age)",
        "Pad 2 (warm)",
        "Pad 3 (polysynth)",
        "Pad 4 (choir)",
        "Pad 5 (bowed)",
        "Pad 6 (metallic)",
        "Pad 7 (halo)",
        "Pad 8 (sweep)",
        "FX 1 (rain)",
        "FX 2 (soundtrack)",
        "FX 3 (crystal)",
        "FX 4 (atmosphere)",
        "FX 5 (brightness)",
        "FX 6 (goblins)",
        "FX 7 (echoes)",
        "FX 8 (sci-fi)",
        "Sitar",
        "Banjo",
        "Shamisen",
        "Koto",
        "Kalimba",
        "Bag pipe",
        "Fiddle",
        "Shanai",
        "Tinkle Bell",
        "Agogo",
        "Steel Drums",
        "Woodblock",
        "Taiko Drum",
        "Melodic Tom",
        "Synth Drum",
        "Reverse Cymbal",
        "Guitar Fret Noise",
        "Breath Noise",
        "Seashore",
        "Bird Tweet",
        "Telephone Ring",
        "Helicopter",
        "Applause",
        "Gunshot"
    };

    public static SF2Soundbank createSoundbank() {
        List<SF2Layer> layers = new ArrayList<>();
        List<SF2Sample> samples = new ArrayList<>();
        List<Instrument> instruments = new ArrayList<>();

        /*
         *  percussion instruments
         */

        SF2Layer bass_drum = new_bass_drum(layers, samples);
        SF2Layer snare_drum = new_snare_drum(layers, samples);
        SF2Layer tom = new_tom(layers, samples);
        SF2Layer open_hihat = new_open_hihat(layers, samples);
        SF2Layer closed_hihat = new_closed_hihat(layers, samples);
        SF2Layer crash_cymbal = new_crash_cymbal(layers, samples);
        SF2Layer side_stick = new_side_stick(layers, samples);

        SF2Layer[] drums = new SF2Layer[128];
        drums[35] = bass_drum;
        drums[36] = bass_drum;
        drums[38] = snare_drum;
        drums[40] = snare_drum;
        drums[41] = tom;
        drums[43] = tom;
        drums[45] = tom;
        drums[47] = tom;
        drums[48] = tom;
        drums[50] = tom;
        drums[42] = closed_hihat;
        drums[44] = closed_hihat;
        drums[46] = open_hihat;
        drums[49] = crash_cymbal;
        drums[51] = crash_cymbal;
        drums[52] = crash_cymbal;
        drums[55] = crash_cymbal;
        drums[57] = crash_cymbal;
        drums[59] = crash_cymbal;

        // Use side_stick for missing drums:
        drums[37] = side_stick;
        drums[39] = side_stick;
        drums[53] = side_stick;
        drums[54] = side_stick;
        drums[56] = side_stick;
        drums[58] = side_stick;
        drums[69] = side_stick;
        drums[70] = side_stick;
        drums[75] = side_stick;
        drums[60] = side_stick;
        drums[61] = side_stick;
        drums[62] = side_stick;
        drums[63] = side_stick;
        drums[64] = side_stick;
        drums[65] = side_stick;
        drums[66] = side_stick;
        drums[67] = side_stick;
        drums[68] = side_stick;
        drums[71] = side_stick;
        drums[72] = side_stick;
        drums[73] = side_stick;
        drums[74] = side_stick;
        drums[76] = side_stick;
        drums[77] = side_stick;
        drums[78] = side_stick;
        drums[79] = side_stick;
        drums[80] = side_stick;
        drums[81] = side_stick;

        List<SF2InstrumentRegion> regions = new ArrayList<>();
        for (int i = 0; i < drums.length; i++) {
            if (drums[i] != null) {
                byte iB = (byte) i;
                SF2InstrumentRegion region = new SF2InstrumentRegion(drums[i], new HashMap<Integer, Short>() {{
                    put(SF2InstrumentRegion.GENERATOR_KEYRANGE, (short) (iB + (iB << 8)));
                }});
                regions.add(region);
            }
        }

        SF2Instrument drum_instrument = new SF2Instrument("Standard Kit", new Patch(0, 0, true), regions);
        instruments.add(drum_instrument);


        /*
         *  melodic instruments
         */

        SF2Layer gpiano = new_gpiano(layers, samples);
        SF2Layer gpiano2 = new_gpiano2(layers, samples);
        SF2Layer gpiano_hammer = new_piano_hammer(layers, samples);
        SF2Layer piano1 = new_piano1(layers, samples);
        SF2Layer epiano1 = new_epiano1(layers, samples);
        SF2Layer epiano2 = new_epiano2(layers, samples);

        SF2Layer guitar = new_guitar1(layers, samples);
        SF2Layer guitar_pick = new_guitar_pick(layers, samples);
        SF2Layer guitar_dist = new_guitar_dist(layers, samples);
        SF2Layer bass1 = new_bass1(layers, samples);
        SF2Layer bass2 = new_bass2(layers, samples);
        SF2Layer synthbass = new_synthbass(layers, samples);
        SF2Layer string2 = new_string2(layers, samples);
        SF2Layer orchhit = new_orchhit(layers, samples);
        SF2Layer choir = new_choir(layers, samples);
        SF2Layer solostring = new_solostring(layers, samples);
        SF2Layer organ = new_organ(layers, samples);
        SF2Layer ch_organ = new_ch_organ(layers, samples);
        SF2Layer bell = new_bell(layers, samples);
        SF2Layer flute = new_flute(layers, samples);

        SF2Layer timpani = new_timpani(layers, samples);
        SF2Layer melodic_toms = new_melodic_toms(layers, samples);
        SF2Layer trumpet = new_trumpet(layers, samples);
        SF2Layer trombone = new_trombone(layers, samples);
        SF2Layer brass_section = new_brass_section(layers, samples);
        SF2Layer horn = new_horn(layers, samples);
        SF2Layer sax = new_sax(layers, samples);
        SF2Layer oboe = new_oboe(layers, samples);
        SF2Layer bassoon = new_bassoon(layers, samples);
        SF2Layer clarinet = new_clarinet(layers, samples);
        SF2Layer reverse_cymbal = new_reverse_cymbal(layers, samples);

        newInstrument(new Patch(0, 0), instruments, gpiano, gpiano_hammer);
        newInstrument(new Patch(0, 1), instruments, gpiano2, gpiano_hammer);
        newInstrument(new Patch(0, 2), instruments, piano1);
        newInstrument(new Patch(0, 3), instruments, piano1, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 80);
            put(SF2Region.GENERATOR_FINETUNE, (short) 30);
        }}, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 30);
        }});
        newInstrument(new Patch(0, 4), instruments, epiano2);
        newInstrument(new Patch(0, 5), instruments, epiano2);
        newInstrument(new Patch(0, 6), instruments, epiano1);
        newInstrument(new Patch(0, 7), instruments, epiano1);
        newInstrument(new Patch(0, 8), instruments, epiano2);
        newInstrument(new Patch(0, 9), instruments, bell);
        newInstrument(new Patch(0, 10), instruments, bell);
        newInstrument(new Patch(0, 11), instruments, bell);
        newInstrument(new Patch(0, 12), instruments, bell);
        newInstrument(new Patch(0, 13), instruments, bell);
        newInstrument(new Patch(0, 14), instruments, bell);
        newInstrument(new Patch(0, 15), instruments, organ);
        newInstrument(new Patch(0, 16), instruments, organ);
        newInstrument(new Patch(0, 17), instruments, organ);
        newInstrument(new Patch(0, 18), instruments, organ);
        newInstrument(new Patch(0, 19), instruments, ch_organ);
        newInstrument(new Patch(0, 20), instruments, organ);
        newInstrument(new Patch(0, 21), instruments, organ);
        newInstrument(new Patch(0, 22), instruments, organ);
        newInstrument(new Patch(0, 23), instruments, organ);
        newInstrument(new Patch(0, 24), instruments, guitar, guitar_pick);
        newInstrument(new Patch(0, 25), instruments, guitar, guitar_pick);
        newInstrument(new Patch(0, 26), instruments, guitar, guitar_pick);
        newInstrument(new Patch(0, 27), instruments, guitar, guitar_pick);
        newInstrument(new Patch(0, 28), instruments, guitar, guitar_pick);
        newInstrument(new Patch(0, 29), instruments, guitar_dist);
        newInstrument(new Patch(0, 30), instruments, guitar_dist);
        newInstrument(new Patch(0, 31), instruments, guitar, guitar_pick);
        newInstrument(new Patch(0, 32), instruments, bass1);
        newInstrument(new Patch(0, 33), instruments, bass1);
        newInstrument(new Patch(0, 34), instruments, bass1);
        newInstrument(new Patch(0, 35), instruments, bass2);
        newInstrument(new Patch(0, 36), instruments, bass2);
        newInstrument(new Patch(0, 37), instruments, bass2);
        newInstrument(new Patch(0, 38), instruments, synthbass);
        newInstrument(new Patch(0, 39), instruments, synthbass);
        newInstrument(new Patch(0, 40), instruments, string2, solostring);
        newInstrument(new Patch(0, 41), instruments, string2, solostring);
        newInstrument(new Patch(0, 42), instruments, string2, solostring);
        newInstrument(new Patch(0, 43), instruments, string2, solostring);
        newInstrument(new Patch(0, 44), instruments, string2, solostring);
        newInstrument(new Patch(0, 45), instruments, piano1);
        newInstrument(new Patch(0, 46), instruments, bell);
        newInstrument(new Patch(0, 47), instruments, timpani);
        newInstrument(new Patch(0, 48), instruments, string2);
        newInstrument(new Patch(0, 49), instruments, string2, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) 2500);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 2000);
        }});
        newInstrument(new Patch(0, 50), instruments, string2);
        newInstrument(new Patch(0, 51), instruments, string2);


        newInstrument(new Patch(0, 52), instruments, choir);
        newInstrument(new Patch(0, 53), instruments, choir);
        newInstrument(new Patch(0, 54), instruments, choir);
        newInstrument(new Patch(0, 55), instruments, orchhit, timpani, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_COARSETUNE, (short) -12);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});
        newInstrument(new Patch(0, 56), instruments, trumpet);
        newInstrument(new Patch(0, 57), instruments, trombone);
        newInstrument(new Patch(0, 58), instruments, trombone);
        newInstrument(new Patch(0, 59), instruments, trumpet);
        newInstrument(new Patch(0, 60), instruments, horn);
        newInstrument(new Patch(0, 61), instruments, brass_section);
        newInstrument(new Patch(0, 62), instruments, brass_section);
        newInstrument(new Patch(0, 63), instruments, brass_section);
        newInstrument(new Patch(0, 64), instruments, sax);
        newInstrument(new Patch(0, 65), instruments, sax);
        newInstrument(new Patch(0, 66), instruments, sax);
        newInstrument(new Patch(0, 67), instruments, sax);
        newInstrument(new Patch(0, 68), instruments, oboe);
        newInstrument(new Patch(0, 69), instruments, horn);
        newInstrument(new Patch(0, 70), instruments, bassoon);
        newInstrument(new Patch(0, 71), instruments, clarinet);
        newInstrument(new Patch(0, 72), instruments, flute);
        newInstrument(new Patch(0, 73), instruments, flute);
        newInstrument(new Patch(0, 74), instruments, flute);
        newInstrument(new Patch(0, 75), instruments, flute);
        newInstrument(new Patch(0, 76), instruments, flute);
        newInstrument(new Patch(0, 77), instruments, flute);
        newInstrument(new Patch(0, 78), instruments, flute);
        newInstrument(new Patch(0, 79), instruments, flute);
        newInstrument(new Patch(0, 80), instruments, organ);
        newInstrument(new Patch(0, 81), instruments, organ);
        newInstrument(new Patch(0, 82), instruments, flute);
        newInstrument(new Patch(0, 83), instruments, organ);
        newInstrument(new Patch(0, 84), instruments, organ);
        newInstrument(new Patch(0, 85), instruments, choir);
        newInstrument(new Patch(0, 86), instruments, organ);
        newInstrument(new Patch(0, 87), instruments, organ);
        newInstrument(new Patch(0, 88), instruments, string2);
        newInstrument(new Patch(0, 89), instruments, organ);
        newInstrument(new Patch(0, 90), instruments, piano1);
        newInstrument(new Patch(0, 91), instruments, choir);
        newInstrument(new Patch(0, 92), instruments, organ);
        newInstrument(new Patch(0, 93), instruments, organ);
        newInstrument(new Patch(0, 94), instruments, organ);
        newInstrument(new Patch(0, 95), instruments, organ);
        newInstrument(new Patch(0, 96), instruments, organ);
        newInstrument(new Patch(0, 97), instruments, organ);
        newInstrument(new Patch(0, 98), instruments, bell);
        newInstrument(new Patch(0, 99), instruments, organ);
        newInstrument(new Patch(0, 100), instruments, organ);
        newInstrument(new Patch(0, 101), instruments, organ);
        newInstrument(new Patch(0, 102), instruments, piano1);
        newInstrument(new Patch(0, 103), instruments, string2);
        newInstrument(new Patch(0, 104), instruments, piano1);
        newInstrument(new Patch(0, 105), instruments, piano1);
        newInstrument(new Patch(0, 106), instruments, piano1);
        newInstrument(new Patch(0, 107), instruments, piano1);
        newInstrument(new Patch(0, 108), instruments, bell);
        newInstrument(new Patch(0, 109), instruments, sax);
        newInstrument(new Patch(0, 110), instruments, string2, solostring);
        newInstrument(new Patch(0, 111), instruments, oboe);
        newInstrument(new Patch(0, 112), instruments, bell);
        newInstrument(new Patch(0, 113), instruments, melodic_toms);
        newInstrument(new Patch(0, 114), instruments, bell);
        newInstrument(new Patch(0, 115), instruments, melodic_toms);
        newInstrument(new Patch(0, 116), instruments, melodic_toms);
        newInstrument(new Patch(0, 117), instruments, melodic_toms);
        newInstrument(new Patch(0, 118), instruments, reverse_cymbal);
        newInstrument(new Patch(0, 119), instruments, reverse_cymbal);
        newInstrument(new Patch(0, 120), instruments, guitar);
        newInstrument(new Patch(0, 121), instruments, piano1);
        newInstrument(new Patch(0, 122), instruments, reverse_cymbal, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 18500);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 4500);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) -4500);
        }});
        newInstrument(new Patch(0, 123), instruments, flute, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_COARSETUNE, (short) 24);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) -3000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
        }});
        newInstrument(new Patch(0, 124), instruments, side_stick);
        newInstrument(new Patch(0, 125), instruments, reverse_cymbal, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 18500);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 4500);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) -4500);
        }});
        newInstrument(new Patch(0, 126), instruments, crash_cymbal);
        newInstrument(new Patch(0, 127), instruments, side_stick);

        return new SF2Soundbank("Emergency GM sound set", "Generated", "Emergency generated soundbank", 2, 1, instruments);
    }

    public static SF2Layer new_bell(List<SF2Layer> layers, List<SF2Sample> samples) {
        Random random = new Random(102030201);
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.01;
        double end_w = 0.05;
        double start_a = 0.2;
        double end_a = 0.00001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            double detune = 1 + (random.nextDouble() * 2 - 1) * 0.01;
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1) * detune, w, a);
            a *= a_step;
        }

        String name = "EPiano";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) 1200);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -9000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 16000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_guitar1(List<SF2Layer> layers, List<SF2Sample> samples) {

        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.01;
        double end_w = 0.01;
        double start_a = 2;
        double end_a = 0.01;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);

        double[] aa = new double[40];
        for (int i = 0; i < 40; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] = 2;
        aa[1] = 0.5;
        aa[2] = 0.45;
        aa[3] = 0.2;
        aa[4] = 1;
        aa[5] = 0.5;
        aa[6] = 2;
        aa[7] = 1;
        aa[8] = 0.5;
        aa[9] = 1;
        aa[9] = 0.5;
        aa[10] = 0.2;
        aa[11] = 1;
        aa[12] = 0.7;
        aa[13] = 0.5;
        aa[14] = 1;

        for (int i = 0; i < 40; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
        }

        String name = "Guitar";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 2400);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -100);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -6000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 16000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -20);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_guitar_dist(List<SF2Layer> layers, List<SF2Sample> samples) {

        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.01;
        double end_w = 0.01;
        double start_a = 2;
        double end_a = 0.01;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);

        double[] aa = new double[40];
        for (int i = 0; i < 40; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] = 5;
        aa[1] = 2;
        aa[2] = 0.45;
        aa[3] = 0.2;
        aa[4] = 1;
        aa[5] = 0.5;
        aa[6] = 2;
        aa[7] = 1;
        aa[8] = 0.5;
        aa[9] = 1;
        aa[9] = 0.5;
        aa[10] = 0.2;
        aa[11] = 1;
        aa[12] = 0.7;
        aa[13] = 0.5;
        aa[14] = 1;

        for (int i = 0; i < 40; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
        }

        String name = "Distorted Guitar";

        SF2Sample sample = newSimpleFFTSample_dist(name, data, base, 10000.0);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 8000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_guitar_pick(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;

        // Make treble part
        {
            int m = 2;
            int fftlen = 4096 * m;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5));
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 0; i < 2048 * m; i++) {
                data[i] *= Math.exp(-Math.abs((i - 23) / ((double) m)) * 1.2)
                        + Math.exp(-Math.abs((i - 40) / ((double) m)) * 0.9);
            }
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.8);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9994;
            }
            datab = data;

            fadeUp(data, 80);
        }

        String name = "Guitar Noise";

        SF2Sample sample = newSimpleDrumSample(name, datab, 60);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_gpiano(List<SF2Layer> layers, List<SF2Sample> samples) {
        //Random random = new Random(302030201);
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 15.0);

        double[] aa = new double[30];
        for (int i = 0; i < 30; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 2;
        //aa[2] *= 0.1;
        aa[4] *= 2;


        aa[12] *= 0.9;
        aa[13] *= 0.7;
        for (int i = 14; i < 30; i++) {
            aa[i] *= 0.5;
        }


        for (int i = 0; i < 30; i++) {
            //double detune = 1 + (random.nextDouble()*2 - 1)*0.0001;
            double w = 0.2;
            double ai = aa[i];
            if (i > 10) {
                w = 5;
                ai *= 10;
            }
            int adjust = 0;
            if (i > 5) {
                adjust = (i - 5) * 7;
            }
            complexGaussianDist(data, base * (i + 1) + adjust, w, ai);
        }

        String name = "Grand Piano";

        SF2Sample sample = newSimpleFFTSample(name, data, base, 200);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -7000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -5500);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 18000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_gpiano2(List<SF2Layer> layers, List<SF2Sample> samples) {
        //Random random = new Random(302030201);
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 20.0);

        double[] aa = new double[30];
        for (int i = 0; i < 30; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 1;
        //aa[2] *= 0.1;
        aa[4] *= 2;


        aa[12] *= 0.9;
        aa[13] *= 0.7;
        for (int i = 14; i < 30; i++) {
            aa[i] *= 0.5;
        }


        for (int i = 0; i < 30; i++) {
            //double detune = 1 + (random.nextDouble()*2 - 1)*0.0001;
            double w = 0.2;
            double ai = aa[i];
            if (i > 10) {
                w = 5;
                ai *= 10;
            }
            int adjust = 0;
            if (i > 5) {
                adjust = (i - 5) * 7;
            }
            complexGaussianDist(data, base * (i + 1) + adjust, w, ai);
        }

        String name = "Grand Piano";

        SF2Sample sample = newSimpleFFTSample(name, data, base, 200);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -7000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -5500);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 18000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_piano_hammer(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;

        // Make treble part
        {
            int m = 2;
            int fftlen = 4096 * m;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5));
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 0; i < 2048 * m; i++)
                data[i] *= Math.exp(-Math.abs((i - 37) / ((double) m)) * 0.05);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.6);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9997;
            }
            datab = data;

            fadeUp(data, 80);
        }

        String name = "Piano Hammer";

        SF2Sample sample = newSimpleDrumSample(name, datab, 60);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_piano1(List<SF2Layer> layers, List<SF2Sample> samples) {
        //Random random = new Random(302030201);
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_a = 0.2;
        double end_a = 0.0001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);

        double[] aa = new double[30];
        for (int i = 0; i < 30; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 5;
        aa[2] *= 0.1;
        aa[7] *= 5;


        for (int i = 0; i < 30; i++) {
            //double detune = 1 + (random.nextDouble()*2 - 1)*0.0001;
            double w = 0.2;
            double ai = aa[i];
            if (i > 12) {
                w = 5;
                ai *= 10;
            }
            int adjust = 0;
            if (i > 5) {
                adjust = (i - 5) * 7;
            }
            complexGaussianDist(data, base * (i + 1) + adjust, w, ai);
        }

        complexGaussianDist(data, base * (15.5), 1, 0.1);
        complexGaussianDist(data, base * (17.5), 1, 0.01);

        String name = "EPiano";

        SF2Sample sample = newSimpleFFTSample(name, data, base, 200);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -1200);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -5500);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 16000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_epiano1(List<SF2Layer> layers, List<SF2Sample> samples) {
        Random random = new Random(302030201);
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.05;
        double end_w = 0.05;
        double start_a = 0.2;
        double end_a = 0.0001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            double detune = 1 + (random.nextDouble() * 2 - 1) * 0.0001;
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1) * detune, w, a);
            a *= a_step;
        }

        String name = "EPiano";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) 1200);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -9000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 16000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_epiano2(List<SF2Layer> layers, List<SF2Sample> samples) {
        Random random = new Random(302030201);
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.01;
        double end_w = 0.05;
        double start_a = 0.2;
        double end_a = 0.00001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            double detune = 1 + (random.nextDouble() * 2 - 1) * 0.0001;
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1) * detune, w, a);
            a *= a_step;
        }

        String name = "EPiano";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 8000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) 2400);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -9000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 16000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_bass1(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.05;
        double end_w = 0.05;
        double start_a = 0.2;
        double end_a = 0.02;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 25.0);

        double[] aa = new double[25];
        for (int i = 0; i < 25; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 8;
        aa[1] *= 4;
        aa[3] *= 8;
        aa[5] *= 8;

        for (int i = 0; i < 25; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
        }

        String name = "Bass";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -3000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -5000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 11000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_synthbass(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.05;
        double end_w = 0.05;
        double start_a = 0.2;
        double end_a = 0.02;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 25.0);

        double[] aa = new double[25];
        for (int i = 0; i < 25; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 16;
        aa[1] *= 4;
        aa[3] *= 16;
        aa[5] *= 8;

        for (int i = 0; i < 25; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
        }

        String name = "Bass";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -3000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) -3000);
            put(SF2Region.GENERATOR_INITIALFILTERQ, (short) 100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 8000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_bass2(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 0.05;
        double end_w = 0.05;
        double start_a = 0.2;
        double end_a = 0.002;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 25.0);

        double[] aa = new double[25];
        for (int i = 0; i < 25; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 8;
        aa[1] *= 4;
        aa[3] *= 8;
        aa[5] *= 8;

        for (int i = 0; i < 25; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
        }

        String name = "Bass2";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -8000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 5000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_solostring(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 2;
        double end_w = 2;
        double start_a = 0.2;
        double end_a = 0.01;

        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 18; i++) {
            a *= a_step;
        }

        for (int i = 0; i < 18; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, a);
        }

        String name = "Strings";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -5000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
            put(SF2Region.GENERATOR_FREQVIBLFO, (short) -1000);
            put(SF2Region.GENERATOR_VIBLFOTOPITCH, (short) 15);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_orchhit(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 2;
        double end_w = 80;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, a);
            a *= a_step;
        }
        complexGaussianDist(data, base * 4, 300, 1);

        String name = "Och Strings";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -5000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 200);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 200);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_string2(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 2;
        double end_w = 80;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, a);
            a *= a_step;
        }

        String name = "Strings";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -5000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_choir(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 25;
        double start_w = 2;
        double end_w = 80;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        double[] aa = new double[40];
        for (int i = 0; i < aa.length; i++) {
            a *= a_step;
            aa[i] = a;
        }

        aa[5] *= 0.1;
        aa[6] *= 0.01;
        aa[7] *= 0.1;
        aa[8] *= 0.1;

        for (int i = 0; i < aa.length; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
        }

        String name = "Strings";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -5000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_organ(List<SF2Layer> layers, List<SF2Sample> samples) {
        Random random = new Random(102030201);
        int x = 1;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;
        double start_w = 0.01;
        double end_w = 0.01;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);

        for (int i = 0; i < 12; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w,
                    a * (0.5 + 3 * (random.nextDouble())));
            a *= a_step;
        }

        String name = "Organ";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_ch_organ(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 1;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;
        double start_w = 0.01;
        double end_w = 0.01;
        double start_a = 0.2;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 60.0);

        double[] aa = new double[60];
        for (int i = 0; i < aa.length; i++) {
            a *= a_step;
            aa[i] = a;
        }

        aa[0] *= 5;
        aa[1] *= 2;
        aa[2] = 0;
        aa[4] = 0;
        aa[5] = 0;
        aa[7] *= 7;
        aa[9] = 0;
        aa[10] = 0;
        aa[12] = 0;
        aa[15] *= 7;
        aa[18] = 0;
        aa[20] = 0;
        aa[24] = 0;
        aa[27] *= 5;
        aa[29] = 0;
        aa[30] = 0;
        aa[33] = 0;
        aa[36] *= 4;
        aa[37] = 0;
        aa[39] = 0;
        aa[42] = 0;
        aa[43] = 0;
        aa[47] = 0;
        aa[50] *= 4;
        aa[52] = 0;
        aa[55] = 0;
        aa[57] = 0;


        aa[10] *= 0.1;
        aa[11] *= 0.1;
        aa[12] *= 0.1;
        aa[13] *= 0.1;

        aa[17] *= 0.1;
        aa[18] *= 0.1;
        aa[19] *= 0.1;
        aa[20] *= 0.1;

        for (int i = 0; i < 60; i++) {
            double w = start_w + (end_w - start_w) * (i / 40.0);
            complexGaussianDist(data, base * (i + 1), w, aa[i]);
            a *= a_step;
        }

        String name = "Organ";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -10000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_flute(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        complexGaussianDist(data, base * 1, 0.001, 0.5);
        complexGaussianDist(data, base * 2, 0.001, 0.5);
        complexGaussianDist(data, base * 3, 0.001, 0.5);
        complexGaussianDist(data, base * 4, 0.01, 0.5);

        complexGaussianDist(data, base * 4, 100, 120);
        complexGaussianDist(data, base * 6, 100, 40);
        complexGaussianDist(data, base * 8, 100, 80);

        complexGaussianDist(data, base * 5, 0.001, 0.05);
        complexGaussianDist(data, base * 6, 0.001, 0.06);
        complexGaussianDist(data, base * 7, 0.001, 0.04);
        complexGaussianDist(data, base * 8, 0.005, 0.06);
        complexGaussianDist(data, base * 9, 0.005, 0.06);
        complexGaussianDist(data, base * 10, 0.01, 0.1);
        complexGaussianDist(data, base * 11, 0.08, 0.7);
        complexGaussianDist(data, base * 12, 0.08, 0.6);
        complexGaussianDist(data, base * 13, 0.08, 0.6);
        complexGaussianDist(data, base * 14, 0.08, 0.6);
        complexGaussianDist(data, base * 15, 0.08, 0.5);
        complexGaussianDist(data, base * 16, 0.08, 0.5);
        complexGaussianDist(data, base * 17, 0.08, 0.2);


        complexGaussianDist(data, base * 1, 10, 8);
        complexGaussianDist(data, base * 2, 10, 8);
        complexGaussianDist(data, base * 3, 10, 8);
        complexGaussianDist(data, base * 4, 10, 8);
        complexGaussianDist(data, base * 5, 10, 8);
        complexGaussianDist(data, base * 6, 20, 9);
        complexGaussianDist(data, base * 7, 20, 9);
        complexGaussianDist(data, base * 8, 20, 9);
        complexGaussianDist(data, base * 9, 20, 8);
        complexGaussianDist(data, base * 10, 30, 8);
        complexGaussianDist(data, base * 11, 30, 9);
        complexGaussianDist(data, base * 12, 30, 9);
        complexGaussianDist(data, base * 13, 30, 8);
        complexGaussianDist(data, base * 14, 30, 8);
        complexGaussianDist(data, base * 15, 30, 7);
        complexGaussianDist(data, base * 16, 30, 7);
        complexGaussianDist(data, base * 17, 30, 6);

        String name = "Flute";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_horn(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        double start_a = 0.5;
        double end_a = 0.00000000001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            if (i == 0)
                complexGaussianDist(data, base * (i + 1), 0.1, a * 0.2);
            else
                complexGaussianDist(data, base * (i + 1), 0.1, a);
            a *= a_step;
        }

        complexGaussianDist(data, base * 2, 100, 1);

        String name = "Horns";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -500);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) 5000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 4500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_trumpet(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        double start_a = 0.5;
        double end_a = 0.00001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 80.0);
        double[] aa = new double[80];
        for (int i = 0; i < 80; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 0.05;
        aa[1] *= 0.2;
        aa[2] *= 0.5;
        aa[3] *= 0.85;

        for (int i = 0; i < 80; i++) {
            complexGaussianDist(data, base * (i + 1), 0.1, aa[i]);
        }

        complexGaussianDist(data, base * 5, 300, 3);

        String name = "Trumpet";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -10000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 0);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -4000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) -2500);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) 5000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 4500);
            put(SF2Region.GENERATOR_INITIALFILTERQ, (short) 10);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_brass_section(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        double start_a = 0.5;
        double end_a = 0.005;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 30.0);
        double[] aa = new double[30];
        for (int i = 0; i < 30; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 0.8;
        aa[1] *= 0.9;

        double w = 5;
        for (int i = 0; i < 30; i++) {
            complexGaussianDist(data, base * (i + 1), 0.1 * w, aa[i] * w);
            w += 6; //*= w_step;
        }

        complexGaussianDist(data, base * 6, 300, 2);

        String name = "Brass Section";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -9200);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -3000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) 5000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 4500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_trombone(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        double start_a = 0.5;
        double end_a = 0.001;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 80.0);
        double[] aa = new double[80];
        for (int i = 0; i < 80; i++) {
            aa[i] = a;
            a *= a_step;
        }

        aa[0] *= 0.3;
        aa[1] *= 0.7;

        for (int i = 0; i < 80; i++) {
            complexGaussianDist(data, base * (i + 1), 0.1, aa[i]);
        }

        complexGaussianDist(data, base * 6, 300, 2);

        String name = "Trombone";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -8000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -2000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) 5000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 4500);
            put(SF2Region.GENERATOR_INITIALFILTERQ, (short) 10);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_sax(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        double start_a = 0.5;
        double end_a = 0.01;
        double a = start_a;
        double a_step = Math.pow(end_a / start_a, 1.0 / 40.0);
        for (int i = 0; i < 40; i++) {
            if (i == 0 || i == 2)
                complexGaussianDist(data, base * (i + 1), 0.1, a * 4);
            else
                complexGaussianDist(data, base * (i + 1), 0.1, a);
            a *= a_step;
        }

        complexGaussianDist(data, base * 4, 200, 1);

        String name = "Sax";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_ATTACKMODENV, (short) -3000);
            put(SF2Region.GENERATOR_RELEASEMODENV, (short) 12000);
            put(SF2Region.GENERATOR_MODENVTOFILTERFC, (short) 5000);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 4500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_oboe(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        complexGaussianDist(data, base * 5, 100, 80);


        complexGaussianDist(data, base * 1, 0.01, 0.53);
        complexGaussianDist(data, base * 2, 0.01, 0.51);
        complexGaussianDist(data, base * 3, 0.01, 0.48);
        complexGaussianDist(data, base * 4, 0.01, 0.49);
        complexGaussianDist(data, base * 5, 0.01, 5);
        complexGaussianDist(data, base * 6, 0.01, 0.51);
        complexGaussianDist(data, base * 7, 0.01, 0.50);
        complexGaussianDist(data, base * 8, 0.01, 0.59);
        complexGaussianDist(data, base * 9, 0.01, 0.61);
        complexGaussianDist(data, base * 10, 0.01, 0.52);
        complexGaussianDist(data, base * 11, 0.01, 0.49);
        complexGaussianDist(data, base * 12, 0.01, 0.51);
        complexGaussianDist(data, base * 13, 0.01, 0.48);
        complexGaussianDist(data, base * 14, 0.01, 0.51);
        complexGaussianDist(data, base * 15, 0.01, 0.46);
        complexGaussianDist(data, base * 16, 0.01, 0.35);
        complexGaussianDist(data, base * 17, 0.01, 0.20);
        complexGaussianDist(data, base * 18, 0.01, 0.10);
        complexGaussianDist(data, base * 19, 0.01, 0.5);
        complexGaussianDist(data, base * 20, 0.01, 0.1);

        String name = "Oboe";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_bassoon(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        complexGaussianDist(data, base * 2, 100, 40);
        complexGaussianDist(data, base * 4, 100, 20);

        complexGaussianDist(data, base * 1, 0.01, 0.53);
        complexGaussianDist(data, base * 2, 0.01, 5);
        complexGaussianDist(data, base * 3, 0.01, 0.51);
        complexGaussianDist(data, base * 4, 0.01, 0.48);
        complexGaussianDist(data, base * 5, 0.01, 1.49);
        complexGaussianDist(data, base * 6, 0.01, 0.51);
        complexGaussianDist(data, base * 7, 0.01, 0.50);
        complexGaussianDist(data, base * 8, 0.01, 0.59);
        complexGaussianDist(data, base * 9, 0.01, 0.61);
        complexGaussianDist(data, base * 10, 0.01, 0.52);
        complexGaussianDist(data, base * 11, 0.01, 0.49);
        complexGaussianDist(data, base * 12, 0.01, 0.51);
        complexGaussianDist(data, base * 13, 0.01, 0.48);
        complexGaussianDist(data, base * 14, 0.01, 0.51);
        complexGaussianDist(data, base * 15, 0.01, 0.46);
        complexGaussianDist(data, base * 16, 0.01, 0.35);
        complexGaussianDist(data, base * 17, 0.01, 0.20);
        complexGaussianDist(data, base * 18, 0.01, 0.10);
        complexGaussianDist(data, base * 19, 0.01, 0.5);
        complexGaussianDist(data, base * 20, 0.01, 0.1);

        String name = "Flute";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_clarinet(List<SF2Layer> layers, List<SF2Sample> samples) {
        int x = 8;
        int fftsize = 4096 * x;
        double[] data = new double[fftsize * 2];
        double base = x * 15;

        complexGaussianDist(data, base * 1, 0.001, 0.5);
        complexGaussianDist(data, base * 2, 0.001, 0.02);
        complexGaussianDist(data, base * 3, 0.001, 0.2);
        complexGaussianDist(data, base * 4, 0.01, 0.1);

        complexGaussianDist(data, base * 4, 100, 60);
        complexGaussianDist(data, base * 6, 100, 20);
        complexGaussianDist(data, base * 8, 100, 20);

        complexGaussianDist(data, base * 5, 0.001, 0.1);
        complexGaussianDist(data, base * 6, 0.001, 0.09);
        complexGaussianDist(data, base * 7, 0.001, 0.02);
        complexGaussianDist(data, base * 8, 0.005, 0.16);
        complexGaussianDist(data, base * 9, 0.005, 0.96);
        complexGaussianDist(data, base * 10, 0.01, 0.9);
        complexGaussianDist(data, base * 11, 0.08, 1.2);
        complexGaussianDist(data, base * 12, 0.08, 1.8);
        complexGaussianDist(data, base * 13, 0.08, 1.6);
        complexGaussianDist(data, base * 14, 0.08, 1.2);
        complexGaussianDist(data, base * 15, 0.08, 0.9);
        complexGaussianDist(data, base * 16, 0.08, 0.5);
        complexGaussianDist(data, base * 17, 0.08, 0.2);


        complexGaussianDist(data, base * 1, 10, 8);
        complexGaussianDist(data, base * 2, 10, 8);
        complexGaussianDist(data, base * 3, 10, 8);
        complexGaussianDist(data, base * 4, 10, 8);
        complexGaussianDist(data, base * 5, 10, 8);
        complexGaussianDist(data, base * 6, 20, 9);
        complexGaussianDist(data, base * 7, 20, 9);
        complexGaussianDist(data, base * 8, 20, 9);
        complexGaussianDist(data, base * 9, 20, 8);
        complexGaussianDist(data, base * 10, 30, 8);
        complexGaussianDist(data, base * 11, 30, 9);
        complexGaussianDist(data, base * 12, 30, 9);
        complexGaussianDist(data, base * 13, 30, 8);
        complexGaussianDist(data, base * 14, 30, 8);
        complexGaussianDist(data, base * 15, 30, 7);
        complexGaussianDist(data, base * 16, 30, 7);
        complexGaussianDist(data, base * 17, 30, 6);

        String name = "Clarinet";

        SF2Sample sample = newSimpleFFTSample(name, data, base);
        SF2Layer layer = newLayer(name, null, sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -6000);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 4000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) -100);
            put(SF2Region.GENERATOR_INITIALFILTERFC, (short) 9500);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_timpani(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;
        double[] datah;

        // Make Bass Part
        {
            int fftlen = 4096 * 8;
            double[] data = new double[2 * fftlen];
            double base = 48;
            complexGaussianDist(data, base * 2, 0.2, 1);
            complexGaussianDist(data, base * 3, 0.2, 0.7);
            complexGaussianDist(data, base * 5, 10, 1);
            complexGaussianDist(data, base * 6, 9, 1);
            complexGaussianDist(data, base * 8, 15, 1);
            complexGaussianDist(data, base * 9, 18, 0.8);
            complexGaussianDist(data, base * 11, 21, 0.5);
            complexGaussianDist(data, base * 13, 28, 0.3);
            complexGaussianDist(data, base * 14, 22, 0.1);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.5);
            data = realPart(data);

            double d_len = data.length;
            for (int i = 0; i < data.length; i++) {
                double g = (1.0 - (i / d_len));
                data[i] *= g * g;
            }
            fadeUp(data, 40);
            datab = data;
        }

        // Make treble part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2) {
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            }
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024 * 4; i < 2048 * 4; i++)
                data[i] = 1.0 - (i - 4096) / 4096.0;
            for (int i = 0; i < 300; i++) {
                double g = (1.0 - (i / 300.0));
                data[i] *= 1.0 + 20 * g * g;
            }
            for (int i = 0; i < 24; i++)
                data[i] = 0;
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9998;
            }
            datah = data;
        }

        for (int i = 0; i < datah.length; i++)
            datab[i] += datah[i] * 0.02;

        normalize(datab, 0.9);

        String name = "Timpani";

        SF2Sample sample = newSimpleDrumSample(name, datab, 60);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_melodic_toms(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;
        double[] datah;

        // Make Bass Part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            complexGaussianDist(data, 30, 0.5, 1);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.8);
            data = realPart(data);

            double d_len = data.length;
            for (int i = 0; i < data.length; i++)
                data[i] *= (1.0 - (i / d_len));
            datab = data;
        }

        // Make treble part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024 * 4; i < 2048 * 4; i++)
                data[i] = 1.0 - (i - 4096) / 4096.0;
            for (int i = 0; i < 200; i++) {
                double g = (1.0 - (i / 200.0));
                data[i] *= 1.0 + 20 * g * g;
            }
            for (int i = 0; i < 30; i++)
                data[i] = 0;
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9996;
            }
            datah = data;
        }

        for (int i = 0; i < datah.length; i++)
            datab[i] += datah[i] * 0.5;
        for (int i = 0; i < 5; i++)
            datab[i] *= i / 5.0;

        normalize(datab, 0.99);

        String name = "Melodic Toms";

        SF2Sample sample = newSimpleDrumSample(name, datab, 63);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_reverse_cymbal(List<SF2Layer> layers, List<SF2Sample> samples) {
        double[] datah;
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5));
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 0; i < 100; i++)
                data[i] = 0;

            for (int i = 0; i < 512 * 2; i++) {
                double gain = (i / (512.0 * 2.0));
                data[i] = 1 - gain;
            }
            datah = data;
        }

        String name = "Reverse Cymbal";

        SF2Sample sample = newSimpleFFTSample(name, datah, 100, 20);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_ATTACKVOLENV, (short) -200);
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) -12000);
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) -1000);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_snare_drum(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;
        double[] datah;

        // Make Bass Part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            complexGaussianDist(data, 24, 0.5, 1);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.5);
            data = realPart(data);

            double d_len = data.length;
            for (int i = 0; i < data.length; i++)
                data[i] *= (1.0 - (i / d_len));
            datab = data;
        }

        // Make treble part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024 * 4; i < 2048 * 4; i++)
                data[i] = 1.0 - (i - 4096) / 4096.0;
            for (int i = 0; i < 300; i++) {
                double g = (1.0 - (i / 300.0));
                data[i] *= 1.0 + 20 * g * g;
            }
            for (int i = 0; i < 24; i++)
                data[i] = 0;
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9998;
            }
            datah = data;
        }

        for (int i = 0; i < datah.length; i++)
            datab[i] += datah[i];
        for (int i = 0; i < 5; i++)
            datab[i] *= i / 5.0;

        String name = "Snare Drum";

        SF2Sample sample = newSimpleDrumSample(name, datab, 60);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_SCALETUNING, (short) 0);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_bass_drum(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;
        double[] datah;

        // Make Bass Part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            complexGaussianDist(data, 1.8 * 5 + 1, 2, 1);
            complexGaussianDist(data, 1.8 * 9 + 1, 2, 1);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double d_len = data.length;
            for (int i = 0; i < data.length; i++)
                data[i] *= (1.0 - (i / d_len));
            datab = data;
        }

        // Make treble part
        {
            int fftlen = 4096;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024; i < 2048; i++)
                data[i] = 1.0 - (i - 1024) / 1024.0;
            for (int i = 0; i < 512; i++)
                data[i] = 10 * i / 512.0;
            for (int i = 0; i < 10; i++)
                data[i] = 0;
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.999;
            }
            datah = data;
        }

        for (int i = 0; i < datah.length; i++)
            datab[i] += datah[i] * 0.5;
        for (int i = 0; i < 5; i++)
            datab[i] *= i / 5.0;

        String name = "Bass Drum";

        SF2Sample sample = newSimpleDrumSample(name, datab, 60);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_SCALETUNING, (short) 0);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_tom(List<SF2Layer> layers, List<SF2Sample> samples) {

        double[] datab;
        double[] datah;

        // Make Bass Part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            complexGaussianDist(data, 30, 0.5, 1);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.8);
            data = realPart(data);

            double d_len = data.length;
            for (int i = 0; i < data.length; i++)
                data[i] *= (1.0 - (i / d_len));
            datab = data;
        }

        // Make treble part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024 * 4; i < 2048 * 4; i++)
                data[i] = 1.0 - (i - 4096) / 4096.0;
            for (int i = 0; i < 200; i++) {
                double g = (1.0 - (i / 200.0));
                data[i] *= 1.0 + 20 * g * g;
            }
            for (int i = 0; i < 30; i++)
                data[i] = 0;
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9996;
            }
            datah = data;
        }

        for (int i = 0; i < datah.length; i++)
            datab[i] += datah[i] * 0.5;
        for (int i = 0; i < 5; i++)
            datab[i] *= i / 5.0;

        normalize(datab, 0.99);

        String name = "Tom";

        SF2Sample sample = newSimpleDrumSample(name, datab, 50);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -100);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_closed_hihat(List<SF2Layer> layers, List<SF2Sample> samples) {
        double[] datah;

        // Make treble part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024 * 4; i < 2048 * 4; i++)
                data[i] = 1.0 - (i - 4096) / 4096.0;
            for (int i = 0; i < 2048; i++)
                data[i] = 0.2 + 0.8 * (i / 2048.0);
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9996;
            }
            datah = data;
        }

        for (int i = 0; i < 5; i++)
            datah[i] *= i / 5.0;

        String name = "Closed Hi-Hat";

        SF2Sample sample = newSimpleDrumSample(name, datah, 60);

        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_SCALETUNING, (short) 0);
            put(SF2Region.GENERATOR_EXCLUSIVECLASS, (short) 1);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_open_hihat(List<SF2Layer> layers, List<SF2Sample> samples) {
        double[] datah;
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5));
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 0; i < 200; i++)
                data[i] = 0;
            for (int i = 0; i < 2048 * 4; i++) {
                double gain = (i / (2048.0 * 4.0));
                data[i] = gain;
            }
            datah = data;
        }

        String name = "Open Hi-Hat";

        SF2Sample sample = newSimpleFFTSample(name, datah, 1000, 5);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 1500);
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 1500);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_SCALETUNING, (short) 0);
            put(SF2Region.GENERATOR_EXCLUSIVECLASS, (short) 1);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_crash_cymbal(List<SF2Layer> layers, List<SF2Sample> samples) {
        double[] datah;
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5));
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 0; i < 100; i++)
                data[i] = 0;
            for (int i = 0; i < 512 * 2; i++) {
                double gain = (i / (512.0 * 2.0));
                data[i] = gain;
            }
            datah = data;
        }

        String name = "Crash Cymbal";

        SF2Sample sample = newSimpleFFTSample(name, datah, 1000, 5);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_DECAYVOLENV, (short) 1800);
            put(SF2Region.GENERATOR_SAMPLEMODES, (short) 1);
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 1800);
            put(SF2Region.GENERATOR_SUSTAINVOLENV, (short) 1000);
            put(SF2Region.GENERATOR_SCALETUNING, (short) 0);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Layer new_side_stick(List<SF2Layer> layers, List<SF2Sample> samples) {
        double[] datab;

        // Make treble part
        {
            int fftlen = 4096 * 4;
            double[] data = new double[2 * fftlen];
            Random random = new Random(3049912);
            for (int i = 0; i < data.length; i += 2)
                data[i] = (2.0 * (random.nextDouble() - 0.5)) * 0.1;
            fft(data);
            // Remove all negative frequency
            for (int i = fftlen / 2; i < data.length; i++)
                data[i] = 0;
            for (int i = 1024 * 4; i < 2048 * 4; i++)
                data[i] = 1.0 - (i - 4096) / 4096.0;
            for (int i = 0; i < 200; i++) {
                double g = (1.0 - (i / 200.0));
                data[i] *= 1.0 + 20 * g * g;
            }
            for (int i = 0; i < 30; i++)
                data[i] = 0;
            randomPhase(data, new Random(3049912));
            ifft(data);
            normalize(data, 0.9);
            data = realPart(data);
            double gain = 1.0;
            for (int i = 0; i < data.length; i++) {
                data[i] *= gain;
                gain *= 0.9996;
            }
            datab = data;
        }

        for (int i = 0; i < 10; i++)
            datab[i] *= i / 10.0;

        String name = "Side Stick";

        SF2Sample sample = newSimpleDrumSample(name, datab, 60);
        SF2Layer layer = newLayer(name, new SF2Region(), sample, new HashMap<Integer, Short>() {{
            put(SF2Region.GENERATOR_RELEASEVOLENV, (short) 12000);
            put(SF2Region.GENERATOR_SCALETUNING, (short) 0);
            put(SF2Region.GENERATOR_INITIALATTENUATION, (short) -50);
        }});

        samples.add(sample);
        layers.add(layer);

        return layer;
    }

    public static SF2Sample newSimpleFFTSample(String name, double[] data, double base) {
        return newSimpleFFTSample(name, data, base, 10);
    }

    public static SF2Sample newSimpleFFTSample(String name, double[] data, double base, int fadeuptime) {

        int fftsize = data.length / 2;
        AudioFormat format = new AudioFormat(44100, 16, 1, true);
        double basefreq = (base / fftsize) * format.getSampleRate() * 0.5;

        randomPhase(data);
        ifft(data);
        data = realPart(data);
        normalize(data, 0.9);
        float[] fdata = toFloat(data);
        fdata = loopExtend(fdata, fdata.length + 512);
        fadeUp(fdata, fadeuptime);
        byte[] bdata = toBytes(fdata, format);

        /*
         * Create SoundFont2 sample.
         */

        double orgnote = (69 + 12) + (12 * Math.log(basefreq / 440.0) / Math.log(2));

        return new SF2Sample(name, bdata, 256, fftsize + 256, (long) format.getSampleRate(), (int) orgnote, (byte) (-(orgnote - (int) orgnote) * 100.0));
    }

    public static SF2Sample newSimpleFFTSample_dist(String name, double[] data, double base, double preamp) {

        int fftsize = data.length / 2;
        AudioFormat format = new AudioFormat(44100, 16, 1, true);
        double basefreq = (base / fftsize) * format.getSampleRate() * 0.5;

        randomPhase(data);
        ifft(data);
        data = realPart(data);

        for (int i = 0; i < data.length; i++) {
            data[i] = (1 - Math.exp(-Math.abs(data[i] * preamp)))
                    * Math.signum(data[i]);
        }

        normalize(data, 0.9);
        float[] fdata = toFloat(data);
        fdata = loopExtend(fdata, fdata.length + 512);
        fadeUp(fdata, 80);
        byte[] bdata = toBytes(fdata, format);

        /*
         * Create SoundFont2 sample.
         */
        double orgnote = (69 + 12) + (12 * Math.log(basefreq / 440.0) / Math.log(2));

        return new SF2Sample(name, bdata, 256, fftsize + 256, (long) format.getSampleRate(), (int) orgnote, (byte) (-(orgnote - (int) orgnote) * 100.0));
    }

    public static SF2Sample newSimpleDrumSample(String name, double[] data, int originalPitch) {

        int fftsize = data.length;
        AudioFormat format = new AudioFormat(44100, 16, 1, true);

        byte[] bdata = toBytes(toFloat(realPart(data)), format);

        /*
         * Create SoundFont2 sample.
         */

        return new SF2Sample(name, bdata, 256, fftsize + 256, (long) format.getSampleRate(), originalPitch);
    }

    public static SF2Layer newLayer(String name, SF2Region globalRegion, SF2Sample sample, Map<Integer, Short> generators) {
        return new SF2Layer(name, globalRegion, Collections.singletonList(new SF2LayerRegion(sample, generators)));
    }

    public static void newInstrument(Patch patch, List<Instrument> instruments, SF2Layer... layers) {
        List<SF2InstrumentRegion> regions = new ArrayList<>(layers.length);
        for (SF2Layer layer : layers) {
            regions.add(new SF2InstrumentRegion(layer));
        }
        SF2Instrument ins = new SF2Instrument(general_midi_instruments[patch.getProgram()], patch, regions);
        instruments.add(ins);
    }

    public static void newInstrument(Patch patch, List<Instrument> instruments, SF2Layer layer, Map<Integer, Short> generators) {
        SF2Instrument ins = new SF2Instrument(general_midi_instruments[patch.getProgram()], patch, Collections.singletonList(new SF2InstrumentRegion(layer, generators)));
        instruments.add(ins);
    }

    public static void newInstrument(Patch patch, List<Instrument> instruments, SF2Layer layer, Map<Integer, Short> generators1, Map<Integer, Short> generators2) {
        List<SF2InstrumentRegion> regions = Arrays.asList(new SF2InstrumentRegion(layer, generators1), new SF2InstrumentRegion(layer, generators2));
        SF2Instrument ins = new SF2Instrument(general_midi_instruments[patch.getProgram()], patch, regions);
        instruments.add(ins);
    }

    public static void newInstrument(Patch patch, List<Instrument> instruments, SF2Layer layer1, SF2Layer layer2, Map<Integer, Short> generators) {
        List<SF2InstrumentRegion> regions = Arrays.asList(new SF2InstrumentRegion(layer1, generators), new SF2InstrumentRegion(layer1), new SF2InstrumentRegion(layer2));
        SF2Instrument ins = new SF2Instrument(general_midi_instruments[patch.getProgram()], patch, regions);
        instruments.add(ins);
    }

    static public void ifft(double[] data) {
        new FFT(data.length / 2, 1).transform(data);
    }

    static public void fft(double[] data) {
        new FFT(data.length / 2, -1).transform(data);
    }

    public static void complexGaussianDist(double[] cdata, double m,
            double s, double v) {
        for (int x = 0; x < cdata.length / 4; x++) {
            cdata[x * 2] += v * (1.0 / (s * Math.sqrt(2 * Math.PI))
                    * Math.exp((-1.0 / 2.0) * Math.pow((x - m) / s, 2.0)));
        }
    }

    static public void randomPhase(double[] data) {
        for (int i = 0; i < data.length; i += 2) {
            double phase = Math.random() * 2 * Math.PI;
            double d = data[i];
            data[i] = Math.sin(phase) * d;
            data[i + 1] = Math.cos(phase) * d;
        }
    }

    static public void randomPhase(double[] data, Random random) {
        for (int i = 0; i < data.length; i += 2) {
            double phase = random.nextDouble() * 2 * Math.PI;
            double d = data[i];
            data[i] = Math.sin(phase) * d;
            data[i + 1] = Math.cos(phase) * d;
        }
    }

    static public void normalize(double[] data, double target) {
        double maxvalue = 0;
        for (double datum : data) {
            if (datum > maxvalue)
                maxvalue = datum;
            if (-datum > maxvalue)
                maxvalue = -datum;
        }
        if (maxvalue == 0)
            return;
        double gain = target / maxvalue;
        for (int i = 0; i < data.length; i++)
            data[i] *= gain;
    }

    static public double[] realPart(double[] in) {
        double[] out = new double[in.length / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = in[i * 2];
        }
        return out;
    }

    static public float[] toFloat(double[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = (float) in[i];
        }
        return out;
    }

    static public byte[] toBytes(float[] in, AudioFormat format) {
        byte[] out = new byte[in.length * format.getFrameSize()];
        return AudioFloatConverter.getConverter(format).toByteArray(in, out);
    }

    static public void fadeUp(double[] data, int samples) {
        for (int i = 0; i < samples; i++)
            data[i] *= i / (double) samples;
    }

    static public void fadeUp(float[] data, int samples) {
        for (int i = 0; i < samples; i++)
            data[i] *= i / (double) samples;
    }

    static public float[] loopExtend(float[] data, int newsize) {
        float[] outdata = new float[newsize];
        int p_len = data.length;
        int p_ps = 0;
        for (int i = 0; i < outdata.length; i++) {
            outdata[i] = data[p_ps];
            p_ps++;
            if (p_ps == p_len)
                p_ps = 0;
        }
        return outdata;
    }
}
