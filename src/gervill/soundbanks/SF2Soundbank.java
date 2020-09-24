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

import gervill.com.sun.media.sound.ModelByteBuffer;
import gervill.javax.sound.midi.Instrument;
import gervill.javax.sound.midi.Patch;
import gervill.javax.sound.midi.Soundbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A SoundFont 2.04 soundbank reader.
 *
 * Based on SoundFont 2.04 specification from:
 * <p>  http://developer.creative.com <br>
 *      http://www.soundfont.com/ ;
 *
 * @author Karl Helgason
 */
public final class SF2Soundbank extends Soundbank {

    SF2Soundbank(List<Instrument> instruments) {
        super(instruments);
    }

    public static SF2Soundbank createSoundbank(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            return readSoundbank(is, null);
        }
    }

    public static SF2Soundbank createSoundbank(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return readSoundbank(is, file);
        }
    }

    public static SF2Soundbank createSoundbank(InputStream inputstream) throws IOException {
        return readSoundbank(inputstream, null);
    }

    private static SF2Soundbank readSoundbank(InputStream inputstream, File sampleFile) throws IOException {
        RIFFReader riff = new RIFFReader(inputstream);
        if (!riff.getFormat().equals("RIFF")) {
            throw new RuntimeException("Input stream is not a valid RIFF stream!");
        }
        if (!riff.getType().equals("sfbk")) {
            throw new RuntimeException("Input stream is not a valid SoundFont!");
        }

        ModelByteBuffer[] datas = new ModelByteBuffer[2];
        List<Instrument> instruments = new ArrayList<>();

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                switch (chunk.getType()) {
                    case "INFO":
                        chunk.finish();
                        break;
                    case "sdta":
                        readSdtaChunk(chunk, sampleFile, datas);
                        break;
                    case "pdta":
                        readPdtaChunk(chunk, datas[0], datas[1], instruments);
                        break;
                }
            }
        }

        return new SF2Soundbank(instruments);
    }

    private static void readSdtaChunk(RIFFReader riff, File sampleFile, ModelByteBuffer[] datas) throws IOException {

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("smpl")) {
                if (sampleFile == null) {
                    byte[] sampleData = new byte[chunk.available()];

                    int read = 0;
                    int avail = chunk.available();
                    while (read != avail) {
                        if (avail - read > 65536) {
                            chunk.readFully(sampleData, read, 65536);
                            read += 65536;
                        } else {
                            chunk.readFully(sampleData, read, avail - read);
                            read = avail;
                        }

                    }
                    datas[0] = new ModelByteBuffer(sampleData);
                    //chunk.read(sampleData);
                } else {
                    datas[0] = new ModelByteBuffer(sampleFile, chunk.getFilePointer(), chunk.available());
                }
            } else if (format.equals("sm24")) {
                if (sampleFile == null) {
                    byte[] sampleData24 = new byte[chunk.available()];
                    //chunk.read(sampleData24);

                    int read = 0;
                    int avail = chunk.available();
                    while (read != avail) {
                        if (avail - read > 65536) {
                            chunk.readFully(sampleData24, read, 65536);
                            read += 65536;
                        } else {
                            chunk.readFully(sampleData24, read, avail - read);
                            read = avail;
                        }

                    }
                    datas[1] = new ModelByteBuffer(sampleData24);
                } else {
                    datas[1] = new ModelByteBuffer(sampleFile, chunk.getFilePointer(), chunk.available());
                }
            }
        }
    }

    private static void readPdtaChunk(RIFFReader riff, ModelByteBuffer sampleData, ModelByteBuffer sampleData24, List<Instrument> instruments) throws IOException {

        List<SF2Layer> layers = new ArrayList<>();
        List<SF2Sample> samples = new ArrayList<>();

        List<Integer> presets_bagNdx = new ArrayList<>();
        List<SF2InstrumentRegion> presets_splits_gen
                = new ArrayList<>();
        List<SF2InstrumentRegion> presets_splits_mod
                = new ArrayList<>();

        List<Integer> instruments_bagNdx = new ArrayList<>();
        List<SF2LayerRegion> instruments_splits_gen
                = new ArrayList<>();
        List<SF2LayerRegion> instruments_splits_mod
                = new ArrayList<>();

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            switch (chunk.getFormat()) {
                case "phdr": {
                    // Preset Header / Instrument
                    if (chunk.available() % 38 != 0)
                        throw new RuntimeException();
                    int count = chunk.available() / 38;
                    for (int i = 0; i < count; i++) {
                        String name = chunk.readString(20);
                        int program = chunk.readUnsignedShort();
                        int bank = chunk.readUnsignedShort();
                        presets_bagNdx.add(chunk.readUnsignedShort());
                        chunk.skip(12);
                        if (i != count - 1) {
                            Patch patch = bank == 128 ? new Patch(0, program, true) : new Patch(bank << 7, program, false);
                            instruments.add(new SF2Instrument(name, patch));
                        }
                    }
                    break;
                }
                case "pbag": {
                    // Preset Zones / Instruments splits
                    if (chunk.available() % 4 != 0)
                        throw new RuntimeException();
                    int count = chunk.available() / 4;

                    // Skip first record
                    {
                        int gencount = chunk.readUnsignedShort();
                        int modcount = chunk.readUnsignedShort();
                        while (presets_splits_gen.size() < gencount)
                            presets_splits_gen.add(null);
                        while (presets_splits_mod.size() < modcount)
                            presets_splits_mod.add(null);
                        count--;
                    }

                    if (presets_bagNdx.isEmpty()) {
                        throw new RuntimeException();
                    }
                    int offset = presets_bagNdx.get(0);
                    // Offset should be 0 (but just case)
                    for (int i = 0; i < offset; i++) {
                        if (count == 0)
                            throw new RuntimeException();
                        int gencount = chunk.readUnsignedShort();
                        int modcount = chunk.readUnsignedShort();
                        while (presets_splits_gen.size() < gencount)
                            presets_splits_gen.add(null);
                        while (presets_splits_mod.size() < modcount)
                            presets_splits_mod.add(null);
                        count--;
                    }

                    for (int i = 0; i < presets_bagNdx.size() - 1; i++) {
                        int zone_count = presets_bagNdx.get(i + 1)
                                - presets_bagNdx.get(i);
                        SF2Instrument preset = (SF2Instrument) instruments.get(i);
                        for (int ii = 0; ii < zone_count; ii++) {
                            if (count == 0)
                                throw new RuntimeException();
                            int gencount = chunk.readUnsignedShort();
                            int modcount = chunk.readUnsignedShort();
                            SF2InstrumentRegion split = new SF2InstrumentRegion();
                            preset.getRegions().add(split);
                            while (presets_splits_gen.size() < gencount)
                                presets_splits_gen.add(split);
                            while (presets_splits_mod.size() < modcount)
                                presets_splits_mod.add(split);
                            count--;
                        }
                    }
                    break;
                }
                case "pmod":
                    // Preset Modulators / Split Modulators
                    for (SF2InstrumentRegion sf2InstrumentRegion : presets_splits_mod) {
                        int sourceOperator = chunk.readUnsignedShort();
                        int destinationOperator = chunk.readUnsignedShort();
                        short amount = chunk.readShort();
                        int amountSourceOperator = chunk.readUnsignedShort();
                        int transportOperator = chunk.readUnsignedShort();
                        if (sf2InstrumentRegion != null) {
                            sf2InstrumentRegion.getModulators().add(new SF2Modulator(sourceOperator, destinationOperator, amount, amountSourceOperator, transportOperator));
                        }
                    }
                    break;
                case "pgen":
                    // Preset Generators / Split Generators
                    for (SF2InstrumentRegion sf2InstrumentRegion : presets_splits_gen) {
                        int operator = chunk.readUnsignedShort();
                        short amount = chunk.readShort();
                        if (sf2InstrumentRegion != null)
                            sf2InstrumentRegion.getGenerators().put(operator, amount);
                    }
                    break;
                case "inst": {
                    // Instrument Header / Layers
                    if (chunk.available() % 22 != 0)
                        throw new RuntimeException();
                    int count = chunk.available() / 22;
                    for (int i = 0; i < count; i++) {
                        chunk.skip(20);
                        SF2Layer layer = new SF2Layer();
                        instruments_bagNdx.add(chunk.readUnsignedShort());
                        if (i != count - 1)
                            layers.add(layer);
                    }
                    break;
                }
                case "ibag": {
                    // Instrument Zones / Layer splits
                    if (chunk.available() % 4 != 0)
                        throw new RuntimeException();
                    int count = chunk.available() / 4;

                    // Skip first record
                    {
                        int gencount = chunk.readUnsignedShort();
                        int modcount = chunk.readUnsignedShort();
                        while (instruments_splits_gen.size() < gencount)
                            instruments_splits_gen.add(null);
                        while (instruments_splits_mod.size() < modcount)
                            instruments_splits_mod.add(null);
                        count--;
                    }

                    if (instruments_bagNdx.isEmpty()) {
                        throw new RuntimeException();
                    }
                    int offset = instruments_bagNdx.get(0);
                    // Offset should be 0 (but just case)
                    for (int i = 0; i < offset; i++) {
                        if (count == 0)
                            throw new RuntimeException();
                        int gencount = chunk.readUnsignedShort();
                        int modcount = chunk.readUnsignedShort();
                        while (instruments_splits_gen.size() < gencount)
                            instruments_splits_gen.add(null);
                        while (instruments_splits_mod.size() < modcount)
                            instruments_splits_mod.add(null);
                        count--;
                    }

                    for (int i = 0; i < instruments_bagNdx.size() - 1; i++) {
                        int zone_count = instruments_bagNdx.get(i + 1) - instruments_bagNdx.get(i);
                        SF2Layer layer = layers.get(i);
                        for (int ii = 0; ii < zone_count; ii++) {
                            if (count == 0)
                                throw new RuntimeException();
                            int gencount = chunk.readUnsignedShort();
                            int modcount = chunk.readUnsignedShort();
                            SF2LayerRegion split = new SF2LayerRegion();
                            layer.getRegions().add(split);
                            while (instruments_splits_gen.size() < gencount)
                                instruments_splits_gen.add(split);
                            while (instruments_splits_mod.size() < modcount)
                                instruments_splits_mod.add(split);
                            count--;
                        }
                    }

                    break;
                }
                case "imod":
                    // Instrument Modulators / Split Modulators
                    for (int i = 0; i < instruments_splits_mod.size(); i++) {
                        int sourceOperator = chunk.readUnsignedShort();
                        int destinationOperator = chunk.readUnsignedShort();
                        short amount = chunk.readShort();
                        int amountSourceOperator = chunk.readUnsignedShort();
                        int transportOperator = chunk.readUnsignedShort();
                        if (i >= instruments_splits_gen.size()) {
                            throw new RuntimeException();
                        }
                        SF2LayerRegion split = instruments_splits_gen.get(i);
                        if (split != null)
                            split.getModulators().add(new SF2Modulator(sourceOperator, destinationOperator, amount, amountSourceOperator, transportOperator));
                    }
                    break;
                case "igen":
                    // Instrument Generators / Split Generators
                    for (SF2LayerRegion sf2LayerRegion : instruments_splits_gen) {
                        int operator = chunk.readUnsignedShort();
                        short amount = chunk.readShort();
                        if (sf2LayerRegion != null)
                            sf2LayerRegion.getGenerators().put(operator, amount);
                    }
                    break;
                case "shdr": {
                    // Sample Headers
                    if (chunk.available() % 46 != 0)
                        throw new RuntimeException();
                    int count = chunk.available() / 46;
                    for (int i = 0; i < count; i++) {

                        chunk.skip(20);
                        long start = chunk.readUnsignedInt();
                        long end = chunk.readUnsignedInt();
                        ModelByteBuffer data = sampleData == null ? null : sampleData.subbuffer(start * 2, end * 2, true);
                        ModelByteBuffer data24 = sampleData24 == null ? null : sampleData24.subbuffer(start, end, true);
                        long startLoop = Math.max(-1, chunk.readUnsignedInt() - start);
                        long endLoop = Math.max(-1, chunk.readUnsignedInt() - start);
                        long sampleRate = chunk.readUnsignedInt();
                        int originalPitch = chunk.readUnsignedByte();
                        byte pitchCorrection = chunk.readByte();

                        SF2Sample sample = new SF2Sample(data, data24, startLoop, endLoop, sampleRate, originalPitch, pitchCorrection);

                        chunk.skip(4);
                        if (i != count - 1)
                            samples.add(sample);
                    }
                    break;
                }
            }
        }

        for (SF2Layer layer : layers) {
            Iterator<SF2LayerRegion> siter = layer.getRegions().iterator();
            SF2Region globalsplit = null;
            while (siter.hasNext()) {
                SF2LayerRegion split = siter.next();
                if (split.getGenerators().get(SF2LayerRegion.GENERATOR_SAMPLEID) != null) {
                    int sampleid = split.getGenerators().get(
                            SF2LayerRegion.GENERATOR_SAMPLEID);
                    split.getGenerators().remove(SF2LayerRegion.GENERATOR_SAMPLEID);
                    if (sampleid < 0 || sampleid >= samples.size()) {
                        throw new RuntimeException();
                    }
                    split.setSample(samples.get(sampleid));
                } else {
                    globalsplit = split;
                }
            }
            if (globalsplit != null) {
                layer.getRegions().remove(globalsplit);
                layer.setGlobalZone(new SF2Region(globalsplit.getGenerators(), globalsplit.getModulators()));
            }
        }


        for (Instrument instrumentG : instruments) {
            SF2Instrument instrument = (SF2Instrument) instrumentG;
            Iterator<SF2InstrumentRegion> siter = instrument.getRegions().iterator();
            SF2Region globalsplit = null;
            while (siter.hasNext()) {
                SF2InstrumentRegion split = siter.next();
                if (split.getGenerators().get(SF2LayerRegion.GENERATOR_INSTRUMENT) != null) {
                    int instrumentid = split.getGenerators().get(
                            SF2InstrumentRegion.GENERATOR_INSTRUMENT);
                    split.getGenerators().remove(SF2LayerRegion.GENERATOR_INSTRUMENT);
                    if (instrumentid < 0 || instrumentid >= layers.size()) {
                        throw new RuntimeException();
                    }
                    split.setLayer(layers.get(instrumentid));
                } else {
                    globalsplit = split;
                }
            }

            if (globalsplit != null) {
                instrument.getRegions().remove(globalsplit);
                instrument.setGlobalZone(new SF2Region(globalsplit.getGenerators(), globalsplit.getModulators()));
            }
        }

    }

}
