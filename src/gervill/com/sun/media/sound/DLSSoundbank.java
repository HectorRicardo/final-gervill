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
import gervill.javax.sound.midi.Patch;
import gervill.javax.sound.midi.Soundbank;
import gervill.javax.sound.sampled.AudioFormat;
import gervill.javax.sound.sampled.AudioFormat.Encoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * A DLS Level 1 and Level 2 soundbank reader (from files/url/streams).
 *
 * @author Karl Helgason
 */
public final class DLSSoundbank implements Soundbank {

    static private class DLSID {
        long i1;
        int s1;
        int s2;
        int x1;
        int x2;
        int x3;
        int x4;
        int x5;
        int x6;
        int x7;
        int x8;

        private DLSID() {
        }

        DLSID(long i1, int s1, int s2, int x1, int x2, int x4,
              int x5, int x6, int x7, int x8) {
            this.i1 = i1;
            this.s1 = s1;
            this.s2 = s2;
            this.x1 = x1;
            this.x2 = x2;
            this.x3 = 0;
            this.x4 = x4;
            this.x5 = x5;
            this.x6 = x6;
            this.x7 = x7;
            this.x8 = x8;
        }

        public static DLSID read(RIFFReader riff) throws IOException {
            DLSID d = new DLSID();
            d.i1 = riff.readUnsignedInt();
            d.s1 = riff.readUnsignedShort();
            d.s2 = riff.readUnsignedShort();
            d.x1 = riff.readUnsignedByte();
            d.x2 = riff.readUnsignedByte();
            d.x3 = riff.readUnsignedByte();
            d.x4 = riff.readUnsignedByte();
            d.x5 = riff.readUnsignedByte();
            d.x6 = riff.readUnsignedByte();
            d.x7 = riff.readUnsignedByte();
            d.x8 = riff.readUnsignedByte();
            return d;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof DLSID)) {
                return false;
            }
            DLSID t = (DLSID) obj;
            return i1 == t.i1 && s1 == t.s1 && s2 == t.s2
                && x1 == t.x1 && x2 == t.x2 && x3 == t.x3 && x4 == t.x4
                && x5 == t.x5 && x6 == t.x6 && x7 == t.x7 && x8 == t.x8;
        }
    }

    /** X = X & Y */
    private static final int DLS_CDL_AND = 0x0001;
    /** X = X | Y */
    private static final int DLS_CDL_OR = 0x0002;
    /** X = X ^ Y */
    private static final int DLS_CDL_XOR = 0x0003;
    /** X = X + Y */
    private static final int DLS_CDL_ADD = 0x0004;
    /** X = X - Y */
    private static final int DLS_CDL_SUBTRACT = 0x0005;
    /** X = X * Y */
    private static final int DLS_CDL_MULTIPLY = 0x0006;
    /** X = X / Y */
    private static final int DLS_CDL_DIVIDE = 0x0007;
    /** X = X && Y */
    private static final int DLS_CDL_LOGICAL_AND = 0x0008;
    /** X = X || Y */
    private static final int DLS_CDL_LOGICAL_OR = 0x0009;
    /** X = (X < Y) */
    private static final int DLS_CDL_LT = 0x000A;
    /** X = (X <= Y) */
    private static final int DLS_CDL_LE = 0x000B;
    /** X = (X > Y) */
    private static final int DLS_CDL_GT = 0x000C;
    /** X = (X >= Y) */
    private static final int DLS_CDL_GE = 0x000D;
    /** X = (X == Y) */
    private static final int DLS_CDL_EQ = 0x000E;
    /** X = !X */
    private static final int DLS_CDL_NOT = 0x000F;
    /** 32-bit constant */
    private static final int DLS_CDL_CONST = 0x0010;
    /** 32-bit value returned from query */
    private static final int DLS_CDL_QUERY = 0x0011;
    /** 32-bit value returned from query */
    private static final int DLS_CDL_QUERYSUPPORTED = 0x0012;

    private static final DLSID DLSID_GMInHardware = new DLSID(0x178f2f24,
            0xc364, 0x11d1, 0xa7, 0x60, 0x00, 0xf8, 0x75, 0xac, 0x12);
    private static final DLSID DLSID_GSInHardware = new DLSID(0x178f2f25,
            0xc364, 0x11d1, 0xa7, 0x60, 0x00, 0xf8, 0x75, 0xac, 0x12);
    private static final DLSID DLSID_XGInHardware = new DLSID(0x178f2f26,
            0xc364, 0x11d1, 0xa7, 0x60, 0x00, 0xf8, 0x75, 0xac, 0x12);
    private static final DLSID DLSID_SupportsDLS1 = new DLSID(0x178f2f27,
            0xc364, 0x11d1, 0xa7, 0x60, 0x00, 0xf8, 0x75, 0xac, 0x12);
    private static final DLSID DLSID_SupportsDLS2 = new DLSID(0xf14599e5,
            0x4689, 0x11d2, 0xaf, 0xa6, 0xaa, 0x0, 0x24, 0xd8, 0xb6);
    private static final DLSID DLSID_SampleMemorySize = new DLSID(0x178f2f28,
            0xc364, 0x11d1, 0xa7, 0x60, 0x00, 0xf8, 0x75, 0xac, 0x12);
    private static final DLSID DLSID_ManufacturersID = new DLSID(0xb03e1181,
            0x8095, 0x11d2, 0xa1, 0xef, 0x60, 0x8, 0x33, 0xdb, 0xd8);
    private static final DLSID DLSID_ProductID = new DLSID(0xb03e1182,
            0x8095, 0x11d2, 0xa1, 0xef, 0x60, 0x8, 0x33, 0xdb, 0xd8);
    private static final DLSID DLSID_SamplePlaybackRate = new DLSID(0x2a91f713,
            0xa4bf, 0x11d2, 0xbb, 0xdf, 0x60, 0x8, 0x33, 0xdb, 0xd8);

    private long major = -1;
    private long minor = -1;

    private final DLSInfo info = new DLSInfo();

    private final List<DLSInstrument> instruments = new ArrayList<>();
    private final List<DLSSample> samples = new ArrayList<>();

    private boolean largeFormat = false;
    private File sampleFile;

    public DLSSoundbank(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            readSoundbank(is);
        }
    }

    public DLSSoundbank(File file) throws IOException {
        largeFormat = true;
        sampleFile = file;
        try (InputStream is = new FileInputStream(file)) {
            readSoundbank(is);
        }
    }

    public DLSSoundbank(InputStream inputstream) throws IOException {
        readSoundbank(inputstream);
    }

    private void readSoundbank(InputStream inputstream) throws IOException {
        RIFFReader riff = new RIFFReader(inputstream);
        if (!riff.getFormat().equals("RIFF")) {
            throw new RuntimeException(
                    "Input stream is not a valid RIFF stream!");
        }
        if (!riff.getType().equals("DLS ")) {
            throw new RuntimeException(
                    "Input stream is not a valid DLS soundbank!");
        }
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("INFO"))
                    readInfoChunk(chunk);
                if (chunk.getType().equals("lins"))
                    readLinsChunk(chunk);
                if (chunk.getType().equals("wvpl"))
                    readWvplChunk(chunk);
            } else {
                if (chunk.getFormat().equals("cdl ")) {
                    if (readCdlChunkInverted(chunk)) {
                        throw new RuntimeException(
                                "DLS file isn't supported!");
                    }
                }
                // skipped because we will load the entire bank into memory
                // long instrumentcount = chunk.readUnsignedInt();
                // System.out.println("instrumentcount = "+ instrumentcount);
                // Pool Table Chunk
                // skipped because we will load the entire bank into memory
                if (chunk.getFormat().equals("vers")) {
                    major = chunk.readUnsignedInt();
                    minor = chunk.readUnsignedInt();
                }
            }
        }

        for (Map.Entry<DLSRegion, Long> entry : temp_rgnassign.entrySet()) {
            entry.getKey().sample = samples.get((int)entry.getValue().longValue());
        }

        temp_rgnassign = null;
    }

    private boolean cdlIsQuerySupported(DLSID uuid) {
        return uuid.equals(DLSID_GMInHardware)
            || uuid.equals(DLSID_GSInHardware)
            || uuid.equals(DLSID_XGInHardware)
            || uuid.equals(DLSID_SupportsDLS1)
            || uuid.equals(DLSID_SupportsDLS2)
            || uuid.equals(DLSID_SampleMemorySize)
            || uuid.equals(DLSID_ManufacturersID)
            || uuid.equals(DLSID_ProductID)
            || uuid.equals(DLSID_SamplePlaybackRate);
    }

    private long cdlQuery(DLSID uuid) {
        if (uuid.equals(DLSID_GMInHardware))
            return 1;
        if (uuid.equals(DLSID_GSInHardware))
            return 0;
        if (uuid.equals(DLSID_XGInHardware))
            return 0;
        if (uuid.equals(DLSID_SupportsDLS1))
            return 1;
        if (uuid.equals(DLSID_SupportsDLS2))
            return 1;
        if (uuid.equals(DLSID_SampleMemorySize))
            return Runtime.getRuntime().totalMemory();
        if (uuid.equals(DLSID_ManufacturersID))
            return 0;
        if (uuid.equals(DLSID_ProductID))
            return 0;
        if (uuid.equals(DLSID_SamplePlaybackRate))
            return 44100;
        return 0;
    }


    // Reading cdl-ck Chunk
    // "cdl " chunk can only appear inside : DLS,lart,lar2,rgn,rgn2
    private boolean readCdlChunkInverted(RIFFReader riff) throws IOException {

        DLSID uuid;
        long x;
        long y;
        Stack<Long> stack = new Stack<>();

        while (riff.available() != 0) {
            int opcode = riff.readUnsignedShort();
            switch (opcode) {
            case DLS_CDL_AND:
                case DLS_CDL_LOGICAL_AND:
                    x = stack.pop();
                y = stack.pop();
                stack.push((long) (((x != 0) && (y != 0)) ? 1 : 0));
                break;
            case DLS_CDL_OR:
                case DLS_CDL_LOGICAL_OR:
                    x = stack.pop();
                y = stack.pop();
                stack.push((long) (((x != 0) || (y != 0)) ? 1 : 0));
                break;
            case DLS_CDL_XOR:
                x = stack.pop();
                y = stack.pop();
                stack.push((long) (((x != 0) ^ (y != 0)) ? 1 : 0));
                break;
            case DLS_CDL_ADD:
                x = stack.pop();
                y = stack.pop();
                stack.push(x + y);
                break;
            case DLS_CDL_SUBTRACT:
                x = stack.pop();
                y = stack.pop();
                stack.push(x - y);
                break;
            case DLS_CDL_MULTIPLY:
                x = stack.pop();
                y = stack.pop();
                stack.push(x * y);
                break;
            case DLS_CDL_DIVIDE:
                x = stack.pop();
                y = stack.pop();
                stack.push(x / y);
                break;
                case DLS_CDL_LT:
                x = stack.pop();
                y = stack.pop();
                stack.push((long) ((x < y) ? 1 : 0));
                break;
            case DLS_CDL_LE:
                x = stack.pop();
                y = stack.pop();
                stack.push((long) ((x <= y) ? 1 : 0));
                break;
            case DLS_CDL_GT:
                x = stack.pop();
                y = stack.pop();
                stack.push((long) ((x > y) ? 1 : 0));
                break;
            case DLS_CDL_GE:
                x = stack.pop();
                y = stack.pop();
                stack.push((long) ((x >= y) ? 1 : 0));
                break;
            case DLS_CDL_EQ:
                x = stack.pop();
                y = stack.pop();
                stack.push((long) ((x == y) ? 1 : 0));
                break;
            case DLS_CDL_NOT:
                x = stack.pop();
                stack.pop();
                stack.push((long) ((x == 0) ? 1 : 0));
                break;
            case DLS_CDL_CONST:
                stack.push(riff.readUnsignedInt());
                break;
            case DLS_CDL_QUERY:
                uuid = DLSID.read(riff);
                stack.push(cdlQuery(uuid));
                break;
            case DLS_CDL_QUERYSUPPORTED:
                uuid = DLSID.read(riff);
                stack.push((long) (cdlIsQuerySupported(uuid) ? 1 : 0));
                break;
            default:
                break;
            }
        }
        if (stack.isEmpty())
            return true;

        return stack.pop() != 1;
    }

    private void readInfoChunk(RIFFReader riff) throws IOException {
        info.name = null;
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            switch (format) {
                case "INAM":
                    info.name = chunk.readString(chunk.available());
                    break;
                case "ICRD":
                case "ITCH":
                case "ISRF":
                case "ISRC":
                case "ISBJ":
                case "IMED":
                case "IKEY":
                case "IGNR":
                case "ICMS":
                case "IART":
                case "IARL":
                case "ISFT":
                case "ICOP":
                case "IPRD":
                    chunk.readString(chunk.available());
                    break;
                case "IENG":
                    info.engineers = chunk.readString(chunk.available());
                    break;
                case "ICMT":
                    info.comments = chunk.readString(chunk.available());
                    break;
            }
        }
    }

    private void readLinsChunk(RIFFReader riff) throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("ins "))
                    readInsChunk(chunk);
            }
        }
    }

    private void readInsChunk(RIFFReader riff) throws IOException {
        DLSInstrument instrument = new DLSInstrument(this);

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("LIST")) {
                if (chunk.getType().equals("INFO")) {
                    readInsInfoChunk(instrument, chunk);
                }
                if (chunk.getType().equals("lrgn")) {
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("LIST")) {
                            if (subchunk.getType().equals("rgn ")) {
                                DLSRegion split = new DLSRegion();
                                if (readRgnChunk(split, subchunk))
                                    instrument.getRegions().add(split);
                            }
                            if (subchunk.getType().equals("rgn2")) {
                                // support for DLS level 2 regions
                                DLSRegion split = new DLSRegion();
                                if (readRgnChunk(split, subchunk))
                                    instrument.getRegions().add(split);
                            }
                        }
                    }
                }
                if (chunk.getType().equals("lart")) {
                    List<DLSModulator> modlist = new ArrayList<>();
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art1"))
                            readArtChunk(modlist, subchunk, 1);
                    }
                    instrument.getModulators().addAll(modlist);
                }
                if (chunk.getType().equals("lar2")) {
                    // support for DLS level 2 ART
                    List<DLSModulator> modlist = new ArrayList<>();
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art2"))
                            readArtChunk(modlist, subchunk, 2);
                    }
                    instrument.getModulators().addAll(modlist);
                }
            } else {
                if (format.equals("dlid")) {
                    instrument.guid = new byte[16];
                    chunk.readFully(instrument.guid);
                }
                if (format.equals("insh")) {
                    chunk.readUnsignedInt(); // Read Region Count - ignored

                    int bank = chunk.read();             // LSB
                    bank += (chunk.read() & 127) << 7;   // MSB
                    chunk.read(); // Read Reserved byte
                    int drumins = chunk.read();          // Drum Instrument

                    int id = chunk.read() & 127; // Read only first 7 bits
                    chunk.read(); // Read Reserved byte
                    chunk.read(); // Read Reserved byte
                    chunk.read(); // Read Reserved byte

                    instrument.bank = bank;
                    instrument.preset = id;
                    instrument.druminstrument = (drumins & 128) > 0;
                    //System.out.println("bank="+bank+" drumkit="+drumkit
                    //        +" id="+id);
                }

            }
        }
        instruments.add(instrument);
    }

    private void readArtChunk(List<DLSModulator> modulators, RIFFReader riff, int version)
            throws IOException {
        long size = riff.readUnsignedInt();
        long count = riff.readUnsignedInt();

        if (size - 8 != 0)
            riff.skip(size - 8);

        for (int i = 0; i < count; i++) {
            int source = riff.readUnsignedShort();
            int control = riff.readUnsignedShort();
            int destination = riff.readUnsignedShort();
            int transform = riff.readUnsignedShort();
            int scale = riff.readInt();
            modulators.add(new DLSModulator(source, control, destination, transform, scale, version));
        }
    }

    private Map<DLSRegion, Long> temp_rgnassign = new HashMap<>();

    private boolean readRgnChunk(DLSRegion split, RIFFReader riff)
            throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("LIST")) {
                if (chunk.getType().equals("lart")) {
                    List<DLSModulator> modlist = new ArrayList<>();
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art1"))
                            readArtChunk(modlist, subchunk, 1);
                    }
                    split.getModulators().addAll(modlist);
                }
                if (chunk.getType().equals("lar2")) {
                    // support for DLS level 2 ART
                    List<DLSModulator> modlist = new ArrayList<>();
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art2"))
                            readArtChunk(modlist, subchunk, 2);
                    }
                    split.getModulators().addAll(modlist);
                }
            } else {

                if (format.equals("cdl ")) {
                    if (readCdlChunkInverted(chunk))
                        return false;
                }
                if (format.equals("rgnh")) {
                    split.keyfrom = chunk.readUnsignedShort();
                    split.keyto = chunk.readUnsignedShort();
                    split.velfrom = chunk.readUnsignedShort();
                    split.velto = chunk.readUnsignedShort();
                    chunk.readUnsignedShort();
                    split.exclusiveClass = chunk.readUnsignedShort();
                }
                if (format.equals("wlnk")) {
                    split.fusoptions = chunk.readUnsignedShort();
                    chunk.readUnsignedShort();
                    chunk.readUnsignedInt();
                    long sampleid = chunk.readUnsignedInt();
                    temp_rgnassign.put(split, sampleid);
                }
                if (format.equals("wsmp")) {
                    split.sampleoptions = readWsmpChunk(chunk);
                }
            }
        }
        return true;
    }

    private DLSSampleOptions readWsmpChunk(RIFFReader riff)
            throws IOException {
        long size = riff.readUnsignedInt();
        int unitynote = riff.readUnsignedShort();
        short finetune = riff.readShort();
        riff.readInt();
        riff.readUnsignedInt();
        long loops = riff.readInt();

        if (size > 20)
            riff.skip(size - 20);

        List<DLSSampleLoop> loopsList = new ArrayList<>();

        for (int i = 0; i < loops; i++) {
            long size2 = riff.readUnsignedInt();
            long type = riff.readUnsignedInt();
            long start = riff.readUnsignedInt();
            long length = riff.readUnsignedInt();
            loopsList.add(new DLSSampleLoop(type, start, length));
            if (size2 > 16)
                riff.skip(size2 - 16);
        }

        return new DLSSampleOptions(unitynote, finetune, loopsList);
    }

    private void readInsInfoChunk(DLSInstrument dlsinstrument, RIFFReader riff)
            throws IOException {
        dlsinstrument.info.name = null;
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            switch (format) {
                case "INAM":
                    dlsinstrument.info.name = chunk.readString(chunk.available());
                    break;
                case "ICRD":
                case "ITCH":
                case "ISRF":
                case "ISRC":
                case "ISBJ":
                case "IMED":
                case "IKEY":
                case "IGNR":
                case "ICMS":
                case "IART":
                case "IARL":
                case "ISFT":
                case "ICOP":
                case "IPRD":
                    chunk.readString(chunk.available());
                    break;
                case "IENG":
                    dlsinstrument.info.engineers =
                            chunk.readString(chunk.available());
                    break;
                case "ICMT":
                    dlsinstrument.info.comments =
                            chunk.readString(chunk.available());
                    break;
            }
        }
    }

    private void readWvplChunk(RIFFReader riff) throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("wave"))
                    readWaveChunk(chunk);
            }
        }
    }

    private void readWaveChunk(RIFFReader riff) throws IOException {
        DLSSample sample = new DLSSample(this);

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("LIST")) {
                if (chunk.getType().equals("INFO")) {
                    readWaveInfoChunk(sample, chunk);
                }
            } else {
                if (format.equals("dlid")) {
                    sample.guid = new byte[16];
                    chunk.readFully(sample.guid);
                }

                if (format.equals("fmt ")) {
                    int sampleformat = chunk.readUnsignedShort();
                    if (sampleformat != 1 && sampleformat != 3) {
                        throw new RuntimeException(
                                "Only PCM samples are supported!");
                    }
                    int channels = chunk.readUnsignedShort();
                    long samplerate = chunk.readUnsignedInt();
                    // bytes per sec
                    /* long framerate = */ chunk.readUnsignedInt();
                    // block align, framesize
                    int framesize = chunk.readUnsignedShort();
                    int bits = chunk.readUnsignedShort();
                    AudioFormat audioformat = null;
                    if (sampleformat == 1) {
                        if (bits == 8) {
                            audioformat = new AudioFormat(
                                    Encoding.PCM_UNSIGNED, samplerate, bits,
                                    channels, framesize, samplerate);
                        } else {
                            audioformat = new AudioFormat(
                                    Encoding.PCM_SIGNED, samplerate, bits,
                                    channels, framesize, samplerate);
                        }
                    }
                    if (sampleformat == 3) {
                        audioformat = new AudioFormat(
                                Encoding.PCM_FLOAT, samplerate, bits,
                                channels, framesize, samplerate);
                    }

                    sample.format = audioformat;
                }

                if (format.equals("data")) {
                    if (largeFormat) {
                        sample.setData(new ModelByteBuffer(sampleFile,
                                chunk.getFilePointer(), chunk.available()));
                    } else {
                        byte[] buffer = new byte[chunk.available()];
                        //  chunk.read(buffer);
                        sample.setData(buffer);

                        int read = 0;
                        int avail = chunk.available();
                        while (read != avail) {
                            if (avail - read > 65536) {
                                chunk.readFully(buffer, read, 65536);
                                read += 65536;
                            } else {
                                chunk.readFully(buffer, read, avail - read);
                                read = avail;
                            }
                        }
                    }
                }

                if (format.equals("wsmp")) {
                    sample.sampleoptions = readWsmpChunk(chunk);
                }
            }
        }

        samples.add(sample);

    }

    private void readWaveInfoChunk(DLSSample dlssample, RIFFReader riff)
            throws IOException {
        dlssample.info.name = null;
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            switch (format) {
                case "INAM":
                    dlssample.info.name = chunk.readString(chunk.available());
                    break;
                case "ICRD":
                case "ITCH":
                case "ISRF":
                case "ISRC":
                case "ISBJ":
                case "IMED":
                case "IKEY":
                case "IGNR":
                case "ICMS":
                case "IART":
                case "IARL":
                case "ISFT":
                case "ICOP":
                case "IPRD":
                    chunk.readString(chunk.available());
                    break;
                case "IENG":
                    dlssample.info.engineers = chunk.readString(chunk.available());
                    break;
                case "ICMT":
                    dlssample.info.comments = chunk.readString(chunk.available());
                    break;
            }
        }
    }

    public String getName() {
        return info.name;
    }

    public String getVersion() {
        return major + "." + minor;
    }

    public String getVendor() {
        return info.engineers;
    }

    public String getDescription() {
        return info.comments;
    }

    public DLSInstrument[] getInstruments() {
        DLSInstrument[] inslist_array =
                instruments.toArray(new DLSInstrument[0]);
        Arrays.sort(inslist_array, new ModelInstrumentComparator());
        return inslist_array;
    }

    public Instrument getInstrument(Patch patch) {
        for (Instrument instrument : instruments) {
            if (patch.equals(instrument.getPatch())) {
                return instrument;
            }
        }
        return null;
    }

}
