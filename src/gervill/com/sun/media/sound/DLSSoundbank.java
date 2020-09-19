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
public final class DLSSoundbank extends Soundbank {

    static private class DLSID {
        private final long i1;
        private final int s1;
        private final int s2;
        private final int x1;
        private final int x2;
        private final int x3;
        private final int x4;
        private final int x5;
        private final int x6;
        private final int x7;
        private final int x8;

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
            long i1 = riff.readUnsignedInt();
            int s1 = riff.readUnsignedShort();
            int s2 = riff.readUnsignedShort();
            int x1 = riff.readUnsignedByte();
            int x2 = riff.readUnsignedByte();
            riff.readUnsignedByte();
            int x4 = riff.readUnsignedByte();
            int x5 = riff.readUnsignedByte();
            int x6 = riff.readUnsignedByte();
            int x7 = riff.readUnsignedByte();
            int x8 = riff.readUnsignedByte();
            return new DLSID(i1, s1, s2, x1, x2, x4, x5, x6, x7, x8);
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

    private final long major;
    private final long minor;

    private final String name;
    private final String engineers;
    private final String comments;

    public DLSSoundbank(String name, String engineers, String comments, long major, long minor, List<Instrument> instruments) {
        super(instruments);
        this.name = name;
        this.engineers = engineers;
        this.comments = comments;
        this.major = major;
        this.minor = minor;
    }

    public static DLSSoundbank createSoundbank(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            return readSoundbank(is, null);
        }
    }

    public static DLSSoundbank createSoundbank(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return readSoundbank(is, file);
        }
    }

    public static DLSSoundbank createSoundbank(InputStream inputstream) throws IOException {
        return readSoundbank(inputstream, null);
    }

    private static DLSSoundbank readSoundbank(InputStream inputstream, File sampleFile) throws IOException {
        RIFFReader riff = new RIFFReader(inputstream);
        if (!riff.getFormat().equals("RIFF")) {
            throw new RuntimeException("Input stream is not a valid RIFF stream!");
        }
        if (!riff.getType().equals("DLS ")) {
            throw new RuntimeException("Input stream is not a valid DLS soundbank!");
        }

        Map<DLSRegion, Integer> temp_rgnassign = new HashMap<>();

        String name = "untitled";
        String engineers = null;
        String comments = null;
        long major = -1;
        long minor = -1;
        List<DLSSample> samples = new ArrayList<>();
        List<Instrument> instruments = new ArrayList<>();

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("INFO")) {
                    name = null;
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        String format = subchunk.getFormat();
                        switch (format) {
                            case "INAM":
                                name = subchunk.readString(subchunk.available());
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
                                subchunk.readString(subchunk.available());
                                break;
                            case "IENG":
                                engineers = subchunk.readString(subchunk.available());
                                break;
                            case "ICMT":
                                comments = subchunk.readString(subchunk.available());
                                break;
                        }
                    }
                }
                if (chunk.getType().equals("lins"))
                    readLinsChunk(chunk, instruments, temp_rgnassign);
                if (chunk.getType().equals("wvpl"))
                    readWvplChunk(chunk, sampleFile, samples);
            } else {
                if (chunk.getFormat().equals("cdl ")) {
                    if (readCdlChunkInverted(chunk)) {
                        throw new RuntimeException("DLS file isn't supported!");
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

        for (Map.Entry<DLSRegion, Integer> entry : temp_rgnassign.entrySet()) {
            entry.getKey().setSample(samples.get(entry.getValue()));
        }

        return new DLSSoundbank(name, engineers, comments, major, minor, instruments);
    }

    private static boolean cdlIsQuerySupported(DLSID uuid) {
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

    private static long cdlQuery(DLSID uuid) {
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
    private static boolean readCdlChunkInverted(RIFFReader riff) throws IOException {

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

    private static void readLinsChunk(RIFFReader riff, List<Instrument> instruments, Map<DLSRegion, Integer> temp_rgnassign) throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("ins "))
                    readInsChunk(chunk, instruments, temp_rgnassign);
            }
        }
    }

    private static final Set<String> FORMATS;
    static {
        Set<String> formats = new HashSet<>();
        formats.add("ICRD");
        formats.add("ITCH");
        formats.add("ISRF");
        formats.add("ISRC");
        formats.add("ISBJ");
        formats.add("IMED");
        formats.add("IKEY");
        formats.add("IGNR");
        formats.add("ICMS");
        formats.add("IART");
        formats.add("IARL");
        formats.add("ISFT");
        formats.add("ICOP");
        formats.add("IPRD");
        formats.add("IENG");
        formats.add("ICMT");
        FORMATS = Collections.unmodifiableSet(formats);
    }

    private static void readInsChunk(RIFFReader riff, List<Instrument> instruments, Map<DLSRegion, Integer> temp_rgnassign) throws IOException {
        String name = null;
        List<DLSRegion> regions = new ArrayList<>();
        List<DLSModulator> modulators = new ArrayList<>();
        Patch patch = null;

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("LIST")) {
                if (chunk.getType().equals("INFO")) {
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        String formatAux = subchunk.getFormat();
                        if (formatAux.equals("INAM")) {
                            name = subchunk.readString(subchunk.available());
                        } else if (FORMATS.contains(formatAux)) {
                            subchunk.readString(subchunk.available());
                        }
                    }
                }
                if (chunk.getType().equals("lrgn")) {
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("LIST")) {
                            if (subchunk.getType().equals("rgn ")) {
                                DLSRegion split = readRgnChunk(subchunk, temp_rgnassign);
                                if (split != null)
                                    regions.add(split);
                            }
                            if (subchunk.getType().equals("rgn2")) {
                                // support for DLS level 2 regions
                                DLSRegion split = readRgnChunk(subchunk, temp_rgnassign);
                                if (split != null)
                                    regions.add(split);
                            }
                        }
                    }
                }
                if (chunk.getType().equals("lart")) {
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art1"))
                            readArtChunk(modulators, subchunk, 1);
                    }
                }
                if (chunk.getType().equals("lar2")) {
                    // support for DLS level 2 ART
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art2"))
                            readArtChunk(modulators, subchunk, 2);
                    }
                }
            } else {
                if (format.equals("dlid")) {
                    chunk.readFully(new byte[16]);
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

                    patch = new Patch(bank, id, (drumins & 128) > 0);
                }

            }
        }

        DLSInstrument instrument = new DLSInstrument(name, regions, modulators, patch);
        instruments.add(instrument);
    }

    private static void readArtChunk(List<DLSModulator> modulators, RIFFReader riff, int version)
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

    private static void readArtChunkSkip(RIFFReader riff)
            throws IOException {
        long size = riff.readUnsignedInt();
        long count = riff.readUnsignedInt();

        if (size - 8 != 0)
            riff.skip(size - 8);

        for (int i = 0; i < count; i++) {
            riff.readUnsignedShort();
            riff.readUnsignedShort();
            riff.readUnsignedShort();
            riff.readUnsignedShort();
            riff.readInt();
        }
    }

    private static DLSRegion readRgnChunk(RIFFReader riff, Map<DLSRegion, Integer> temp_rgnassign)
            throws IOException {
        int keyfrom = 0;
        int keyto = 0;
        int velfrom = 0;
        int velto = 0;
        int exclusiveClass = 0;
        int fusoptions = 0;
        DLSSampleOptions sampleoptions = null;
        int sampleid = 0;

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("LIST")) {
                if (chunk.getType().equals("lart")) {
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art1"))
                            readArtChunkSkip(subchunk);
                    }
                }
                if (chunk.getType().equals("lar2")) {
                    // support for DLS level 2 ART
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        if (subchunk.getFormat().equals("art2"))
                            readArtChunkSkip(subchunk);
                    }
                }
            } else {
                if (format.equals("cdl ") && readCdlChunkInverted(chunk)) {
                    return null;
                }
                if (format.equals("rgnh")) {
                    keyfrom = chunk.readUnsignedShort();
                    keyto = chunk.readUnsignedShort();
                    velfrom = chunk.readUnsignedShort();
                    velto = chunk.readUnsignedShort();
                    chunk.readUnsignedShort();
                    exclusiveClass = chunk.readUnsignedShort();
                }
                if (format.equals("wlnk")) {
                    fusoptions = chunk.readUnsignedShort();
                    chunk.readUnsignedShort();
                    chunk.readUnsignedInt();
                    sampleid = (int)chunk.readUnsignedInt();
                }
                if (format.equals("wsmp")) {
                    sampleoptions = readWsmpChunk(chunk);
                }
            }
        }

        DLSRegion split = new DLSRegion(keyfrom, keyto, velfrom, velto, exclusiveClass, fusoptions, sampleoptions);
        temp_rgnassign.put(split, sampleid);
        return split;
    }

    private static DLSSampleOptions readWsmpChunk(RIFFReader riff)
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

    private static void readWvplChunk(RIFFReader riff, File sampleFile, List<DLSSample> samples) throws IOException {
        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            if (chunk.getFormat().equals("LIST")) {
                if (chunk.getType().equals("wave"))
                    readWaveChunk(chunk, sampleFile, samples);
            }
        }
    }

    private static void readWaveChunk(RIFFReader riff, File sampleFile, List<DLSSample> samples) throws IOException {
        String name = null;
        AudioFormat sampleFormat = null;
        ModelByteBuffer mbb = null;
        DLSSampleOptions sampleoptions = null;

        while (riff.hasNextChunk()) {
            RIFFReader chunk = riff.nextChunk();
            String format = chunk.getFormat();
            if (format.equals("LIST")) {
                if (chunk.getType().equals("INFO")) {
                    while (chunk.hasNextChunk()) {
                        RIFFReader subchunk = chunk.nextChunk();
                        String formatAux = subchunk.getFormat();
                        if (formatAux.equals("INAM") || FORMATS.contains(formatAux)) {
                            subchunk.readString(subchunk.available());
                        }
                    }
                }
            } else {
                if (format.equals("dlid")) {
                    chunk.readFully(new byte[16]);
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

                    sampleFormat = audioformat;
                }

                if (format.equals("data")) {
                    if (sampleFile != null) {
                        mbb = new ModelByteBuffer(sampleFile, chunk.getFilePointer(), chunk.available());
                    } else {
                        byte[] buffer = new byte[chunk.available()];
                        mbb = new ModelByteBuffer(buffer);

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
                    sampleoptions = readWsmpChunk(chunk);
                }
            }
        }

        samples.add(new DLSSample(name, sampleFormat, mbb, sampleoptions));

    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return major + "." + minor;
    }

    public String getVendor() {
        return engineers;
    }

    public String getDescription() {
        return comments;
    }

}
