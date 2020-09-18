/*
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package gervill.soundbanks;

/**
 * This class is used to store modulator/artiuclation data.
 * A modulator connects one synthesizer source to
 * a destination. For example a note on velocity
 * can be mapped to the gain of the synthesized voice.
 * It is stored as a "art1" or "art2" chunk inside DLS files.
 *
 * @author Karl Helgason
 */
final class DLSModulator {

    // DLS1 Destinations
    static final int CONN_DST_NONE = 0x000; // 0
    static final int CONN_DST_GAIN = 0x001; // cB
    static final int CONN_DST_PITCH = 0x003; // cent
    static final int CONN_DST_PAN = 0x004; // 0.1%
    static final int CONN_DST_LFO_FREQUENCY = 0x104; // cent (default 5 Hz)
    static final int CONN_DST_LFO_STARTDELAY = 0x105; // timecent
    static final int CONN_DST_EG1_ATTACKTIME = 0x206; // timecent
    static final int CONN_DST_EG1_DECAYTIME = 0x207; // timecent
    static final int CONN_DST_EG1_RELEASETIME = 0x209; // timecent
    static final int CONN_DST_EG1_SUSTAINLEVEL = 0x20A; // 0.1%
    static final int CONN_DST_EG2_ATTACKTIME = 0x30A; // timecent
    static final int CONN_DST_EG2_DECAYTIME = 0x30B; // timecent
    static final int CONN_DST_EG2_RELEASETIME = 0x30D; // timecent
    static final int CONN_DST_EG2_SUSTAINLEVEL = 0x30E; // 0.1%
    // DLS2 Destinations
    static final int CONN_DST_KEYNUMBER = 0x005;
    static final int CONN_DST_CHORUS = 0x080; // 0.1%
    static final int CONN_DST_REVERB = 0x081; // 0.1%
    static final int CONN_DST_VIB_FREQUENCY = 0x114; // cent
    static final int CONN_DST_VIB_STARTDELAY = 0x115; // dB
    static final int CONN_DST_EG1_DELAYTIME = 0x20B; // timecent
    static final int CONN_DST_EG1_HOLDTIME = 0x20C; // timecent
    static final int CONN_DST_EG1_SHUTDOWNTIME = 0x20D; // timecent
    static final int CONN_DST_EG2_DELAYTIME = 0x30F; // timecent
    static final int CONN_DST_EG2_HOLDTIME = 0x310; // timecent
    static final int CONN_DST_FILTER_CUTOFF = 0x500; // cent
    static final int CONN_DST_FILTER_Q = 0x501; // dB

    // DLS1 Sources
    static final int CONN_SRC_NONE = 0x000; // 1
    static final int CONN_SRC_LFO = 0x001; // linear (sine wave)
    static final int CONN_SRC_KEYONVELOCITY = 0x002; // ??db or velocity??
    static final int CONN_SRC_KEYNUMBER = 0x003; // ??cent or keynumber??
    static final int CONN_SRC_EG1 = 0x004; // linear direct from eg
    static final int CONN_SRC_EG2 = 0x005; // linear direct from eg
    static final int CONN_SRC_PITCHWHEEL = 0x006; // linear -1..1
    static final int CONN_SRC_CC1 = 0x081; // linear 0..1
    static final int CONN_SRC_CC7 = 0x087; // linear 0..1
    static final int CONN_SRC_CC10 = 0x08A; // linear 0..1
    static final int CONN_SRC_CC11 = 0x08B; // linear 0..1
    static final int CONN_SRC_RPN0 = 0x100; // ?? // Pitch Bend Range
    static final int CONN_SRC_RPN1 = 0x101; // ?? // Fine Tune
    // DLS2 Sources
    static final int CONN_SRC_POLYPRESSURE = 0x007; // linear 0..1
    static final int CONN_SRC_CHANNELPRESSURE = 0x008; // linear 0..1
    static final int CONN_SRC_VIBRATO = 0x009; // linear 0..1
    static final int CONN_SRC_MONOPRESSURE = 0x00A; // linear 0..1
    static final int CONN_SRC_CC91 = 0x0DB; // linear 0..1
    static final int CONN_SRC_CC93 = 0x0DD; // linear 0..1
    static final int CONN_TRN_CONCAVE = 0x001;

    private final int source;
    private final int control;
    private final int destination;
    private final int transform;
    private final int scale;
    private final int version;

    DLSModulator(int source, int control, int destination, int transform, int scale, int version) {
        this.source = source;
        this.control = control;
        this.destination = destination;
        this.transform = transform;
        this.scale = scale;
        this.version = version;
    }

    int getControl() {
        return control;
    }

    int getDestination() {
        return destination;
    }

    int getScale() {
        return scale;
    }

    int getSource() {
        return source;
    }

    int getVersion() {
        return version;
    }

    int getTransform() {
        return transform;
    }

}
