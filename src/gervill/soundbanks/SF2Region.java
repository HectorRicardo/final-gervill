/*
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Soundfont general region.
 *
 * @author Karl Helgason
 */
class SF2Region {

    final static int GENERATOR_STARTADDRSOFFSET = 0;
    final static int GENERATOR_ENDADDRSOFFSET = 1;
    final static int GENERATOR_STARTLOOPADDRSOFFSET = 2;
    final static int GENERATOR_ENDLOOPADDRSOFFSET = 3;
    final static int GENERATOR_STARTADDRSCOARSEOFFSET = 4;
    final static int GENERATOR_MODLFOTOPITCH = 5;
    final static int GENERATOR_VIBLFOTOPITCH = 6;
    final static int GENERATOR_MODENVTOPITCH = 7;
    final static int GENERATOR_INITIALFILTERFC = 8;
    final static int GENERATOR_INITIALFILTERQ = 9;
    final static int GENERATOR_MODLFOTOFILTERFC = 10;
    final static int GENERATOR_MODENVTOFILTERFC = 11;
    final static int GENERATOR_ENDADDRSCOARSEOFFSET = 12;
    final static int GENERATOR_MODLFOTOVOLUME = 13;
    final static int GENERATOR_CHORUSEFFECTSSEND = 15;
    final static int GENERATOR_REVERBEFFECTSSEND = 16;
    final static int GENERATOR_PAN = 17;
    final static int GENERATOR_DELAYMODLFO = 21;
    final static int GENERATOR_FREQMODLFO = 22;
    final static int GENERATOR_DELAYVIBLFO = 23;
    final static int GENERATOR_FREQVIBLFO = 24;
    final static int GENERATOR_DELAYMODENV = 25;
    final static int GENERATOR_ATTACKMODENV = 26;
    final static int GENERATOR_HOLDMODENV = 27;
    final static int GENERATOR_DECAYMODENV = 28;
    final static int GENERATOR_SUSTAINMODENV = 29;
    final static int GENERATOR_RELEASEMODENV = 30;
    final static int GENERATOR_KEYNUMTOMODENVHOLD = 31;
    final static int GENERATOR_KEYNUMTOMODENVDECAY = 32;
    final static int GENERATOR_DELAYVOLENV = 33;
    final static int GENERATOR_ATTACKVOLENV = 34;
    final static int GENERATOR_HOLDVOLENV = 35;
    final static int GENERATOR_DECAYVOLENV = 36;
    final static int GENERATOR_SUSTAINVOLENV = 37;
    final static int GENERATOR_RELEASEVOLENV = 38;
    final static int GENERATOR_KEYNUMTOVOLENVHOLD = 39;
    final static int GENERATOR_KEYNUMTOVOLENVDECAY = 40;
    final static int GENERATOR_INSTRUMENT = 41;
    final static int GENERATOR_KEYRANGE = 43;
    final static int GENERATOR_VELRANGE = 44;
    final static int GENERATOR_STARTLOOPADDRSCOARSEOFFSET = 45;
    final static int GENERATOR_KEYNUM = 46;
    final static int GENERATOR_VELOCITY = 47;
    final static int GENERATOR_INITIALATTENUATION = 48;
    final static int GENERATOR_ENDLOOPADDRSCOARSEOFFSET = 50;
    final static int GENERATOR_COARSETUNE = 51;
    final static int GENERATOR_FINETUNE = 52;
    final static int GENERATOR_SAMPLEID = 53;
    final static int GENERATOR_SAMPLEMODES = 54;
    final static int GENERATOR_SCALETUNING = 56;
    final static int GENERATOR_EXCLUSIVECLASS = 57;
    final static int GENERATOR_OVERRIDINGROOTKEY = 58;

    private final Map<Integer, Short> generators;
    private final List<SF2Modulator> modulators;

    SF2Region() {
        this(new HashMap<>(), new ArrayList<>());
    }

    SF2Region(Map<Integer, Short> generators, List<SF2Modulator> modulators) {
        this.generators = generators;
        this.modulators = modulators;
    }

    SF2Region(Map<Integer, Short> generators) {
        this(generators, new ArrayList<>());
    }

    Map<Integer, Short> getGenerators() {
        return generators;
    }

    boolean contains(int generator) {
        return generators.containsKey(generator);
    }

    static short getDefaultValue(int generator) {
        if (generator == 8) return (short)13500;
        if (generator == 21) return (short)-12000;
        if (generator == 23) return (short)-12000;
        if (generator == 25) return (short)-12000;
        if (generator == 26) return (short)-12000;
        if (generator == 27) return (short)-12000;
        if (generator == 28) return (short)-12000;
        if (generator == 30) return (short)-12000;
        if (generator == 33) return (short)-12000;
        if (generator == 34) return (short)-12000;
        if (generator == 35) return (short)-12000;
        if (generator == 36) return (short)-12000;
        if (generator == 38) return (short)-12000;
        if (generator == 43) return (short)0x7F00;
        if (generator == 44) return (short)0x7F00;
        if (generator == 46) return (short)-1;
        if (generator == 47) return (short)-1;
        if (generator == 56) return (short)100;
        if (generator == 58) return (short)-1;
        return 0;
    }

    short getShort(int generator) {
        if (!contains(generator))
            return getDefaultValue(generator);
        return generators.get(generator);
    }

    byte[] getBytes(int generator) {
        int val = getInteger(generator);
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (0xFF & val);
        bytes[1] = (byte) ((0xFF00 & val) >> 8);
        return bytes;
    }

    int getInteger(int generator) {
        return 0xFFFF & getShort(generator);
    }

    List<SF2Modulator> getModulators() {
        return modulators;
    }

}
