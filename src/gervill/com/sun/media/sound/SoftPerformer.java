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

import gervill.soundbanks.ModelByteBufferWavetable;
import own.main.ImmutableList;
import own.main.ImmutableMap;

import java.util.*;

/**
 * This class decodes information from ModelPeformer for use in SoftVoice.
 * It also adds default connections if they where missing in ModelPerformer.
 *
 * @author Karl Helgason
 */
public final class SoftPerformer {

    static final ModelConnectionBlock[] defaultconnections
            = new ModelConnectionBlock[42];

    static {
        int o = 0;
        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("noteon", "on", 0),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1, new ModelDestination(new ModelIdentifier("eg", "on", 0)));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("noteon", "on", 0),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1, new ModelDestination(new ModelIdentifier("eg", "on", 1)));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("eg", "active", 0),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1, new ModelDestination(new ModelIdentifier("mixer", "active", 0)));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("eg", 0),
                ModelStandardTransform.DIRECTION_MAX2MIN,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            -960, new ModelDestination(new ModelIdentifier("mixer", "gain")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("noteon", "velocity"),
                ModelStandardTransform.DIRECTION_MAX2MIN,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_CONCAVE),
            -960, new ModelDestination(new ModelIdentifier("mixer", "gain")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi", "pitch"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            new ModelSource(new ModelIdentifier("midi_rpn", "0"),
                    value -> {
                        int v = (int) (value * 16384.0);
                        int msb = v >> 7;
                        int lsb = v & 127;
                        return msb * 100 + lsb;
                    }),
            new ModelDestination(new ModelIdentifier("osc", "pitch")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("noteon", "keynumber"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            12800, new ModelDestination(new ModelIdentifier("osc", "pitch")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "7"),
                ModelStandardTransform.DIRECTION_MAX2MIN,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_CONCAVE),
            -960, new ModelDestination(new ModelIdentifier("mixer", "gain")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "8"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1000, new ModelDestination(new ModelIdentifier("mixer", "balance")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "10"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1000, new ModelDestination(new ModelIdentifier("mixer", "pan")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "11"),
                ModelStandardTransform.DIRECTION_MAX2MIN,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_CONCAVE),
            -960, new ModelDestination(new ModelIdentifier("mixer", "gain")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "91"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1000, new ModelDestination(new ModelIdentifier("mixer", "reverb")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "93"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            1000, new ModelDestination(new ModelIdentifier("mixer", "chorus")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "71"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            200, new ModelDestination(new ModelIdentifier("filter", "q")));
        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "74"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            9600, new ModelDestination(new ModelIdentifier("filter", "freq")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "72"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            6000, new ModelDestination(new ModelIdentifier("eg", "release2")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "73"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            2000, new ModelDestination(new ModelIdentifier("eg", "attack2")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "75"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            6000, new ModelDestination(new ModelIdentifier("eg", "decay2")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "67"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_SWITCH),
            -50, new ModelDestination(ModelDestination.DESTINATION_GAIN));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_cc", "67"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_UNIPOLAR,
                ModelStandardTransform.TRANSFORM_SWITCH),
            -2400, new ModelDestination(ModelDestination.DESTINATION_FILTER_FREQ));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_rpn", "1"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            100, new ModelDestination(new ModelIdentifier("osc", "pitch")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("midi_rpn", "2"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            12800, new ModelDestination(new ModelIdentifier("osc", "pitch")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("master", "fine_tuning"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            100, new ModelDestination(new ModelIdentifier("osc", "pitch")));

        defaultconnections[o++] = new ModelConnectionBlock(
            new ModelSource(
                new ModelIdentifier("master", "coarse_tuning"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            12800, new ModelDestination(new ModelIdentifier("osc", "pitch")));

        defaultconnections[o++] = new ModelConnectionBlock(13500,
                new ModelDestination(new ModelIdentifier("filter", "freq", 0)));

        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "delay", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "attack", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "hold", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "decay", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(1000,
                new ModelDestination(new ModelIdentifier("eg", "sustain", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "release", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(1200.0
                * Math.log(0.015) / Math.log(2), new ModelDestination(
                new ModelIdentifier("eg", "shutdown", 0))); // 15 msec default

        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "delay", 1)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "attack", 1)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "hold", 1)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "decay", 1)));
        defaultconnections[o++] = new ModelConnectionBlock(1000,
                new ModelDestination(new ModelIdentifier("eg", "sustain", 1)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("eg", "release", 1)));

        defaultconnections[o++] = new ModelConnectionBlock(-8.51318,
                new ModelDestination(new ModelIdentifier("lfo", "freq", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("lfo", "delay", 0)));
        defaultconnections[o++] = new ModelConnectionBlock(-8.51318,
                new ModelDestination(new ModelIdentifier("lfo", "freq", 1)));
        defaultconnections[o] = new ModelConnectionBlock(
                Float.NEGATIVE_INFINITY, new ModelDestination(
                new ModelIdentifier("lfo", "delay", 1)));

    }

    public final int exclusiveClass;
    public final boolean selfNonExclusive;
    public final boolean forcedVelocity;
    public final boolean forcedKeynumber;
    public final ImmutableList<ModelConnectionBlock> connections;
    public final ImmutableList<ModelByteBufferWavetable> oscillators;
    public final ImmutableMap<Integer, ImmutableList<Integer>> midi_rpn_connections;
    public final ImmutableMap<Integer, ImmutableList<Integer>> midi_nrpn_connections;
    public final ImmutableList<ImmutableList<Integer>> midi_ctrl_connections;
    public final ImmutableList<ImmutableList<Integer>> midi_connections;
    public final ImmutableList<Integer> ctrl_connections;

    private String extractKeys(ModelConnectionBlock conn) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ModelSource src : conn.getSources()) {
            sb.append(src.getIdentifier());
            sb.append(";");
        }
        sb.append("]");
        sb.append(";");
        if (conn.getDestination() != null) {
            sb.append(conn.getDestination().getIdentifier());
        }
        sb.append(";");
        return sb.toString();
    }

    private static void processSource(ModelSource src, int ix, List<Integer> ctrl_connections_list, Map<Integer, ImmutableList<Integer>> midi_nrpn_connections,  Map<Integer, ImmutableList<Integer>> midi_rpn_connections, List<ImmutableList<Integer>> midi_ctrl_connections, List<ImmutableList<Integer>> midi_connections) {
        ModelIdentifier id = src.getIdentifier();
        String o = id.getObject();
        switch (o) {
            case "midi_cc":
                processMidiControlSource(src, ix, midi_ctrl_connections);
                break;
            case "midi_rpn":
                processMidiRpnSource(src, ix, midi_rpn_connections);
                break;
            case "midi_nrpn":
                processMidiNrpnSource(src, ix, midi_nrpn_connections);
                break;
            case "midi":
                processMidiSource(src, ix, midi_connections);
                break;
            case "noteon":
                processNoteOnSource(src, ix, midi_connections);
                break;
            case "osc":
            case "mixer":
                return;
            default:
                ctrl_connections_list.add(ix);
                break;
        }
    }

    private static void processMidiControlSource(ModelSource src, int ix, List<ImmutableList<Integer>> midi_ctrl_connections) {
        String v = src.getIdentifier().getVariable();
        if (v == null)
            return;
        int c = Integer.parseInt(v);
        if (midi_ctrl_connections.get(c) == null)
            midi_ctrl_connections.set(c, ImmutableList.create(ix));
        else {
            ImmutableList<Integer> olda = midi_ctrl_connections.get(c);
            ImmutableList<Integer> newa = ImmutableList.create(olda, 1, i -> ix);
            midi_ctrl_connections.set(c, newa);
        }
    }

    private static void processNoteOnSource(ModelSource src, int ix, List<ImmutableList<Integer>> midi_connections) {
        String v = src.getIdentifier().getVariable();
        int c = -1;
        if (v.equals("on"))
            c = 3;
        if (v.equals("keynumber"))
            c = 4;
        if (c == -1)
            return;
        if (midi_connections.get(c) == null)
            midi_connections.set(c, ImmutableList.create(ix));
        else {
            ImmutableList<Integer> olda = midi_connections.get(c);
            ImmutableList<Integer> newa = ImmutableList.create(olda, 1, index -> ix);
            midi_connections.set(c, newa);
        }
    }

    private static void processMidiSource(ModelSource src, int ix, List<ImmutableList<Integer>> midi_connections) {
        String v = src.getIdentifier().getVariable();
        int c = -1;
        if (v.equals("pitch"))
            c = 0;
        if (v.equals("channel_pressure"))
            c = 1;
        if (v.equals("poly_pressure"))
            c = 2;
        if (c == -1)
            return;
        if (midi_connections.get(c) == null)
            midi_connections.set(c, ImmutableList.create(ix));
        else {
            ImmutableList<Integer> olda = midi_connections.get(c);
            ImmutableList<Integer> newa = ImmutableList.create(olda, 1, index -> ix);
            midi_connections.set(c, newa);
        }
    }

    private static void processMidiRpnSource(ModelSource src, int ix,  Map<Integer, ImmutableList<Integer>> midi_rpn_connections) {
        String v = src.getIdentifier().getVariable();
        if (v == null)
            return;
        int c = Integer.parseInt(v);
        if (midi_rpn_connections.get(c) == null)
            midi_rpn_connections.put(c, ImmutableList.create(ix));
        else {
            ImmutableList<Integer> olda = midi_rpn_connections.get(c);
            ImmutableList<Integer> newa = ImmutableList.create(olda, 1, index -> ix);
            midi_rpn_connections.put(c, newa);
        }
    }

    private static void processMidiNrpnSource(ModelSource src, int ix, Map<Integer, ImmutableList<Integer>> midi_nrpn_connections) {
        String v = src.getIdentifier().getVariable();
        if (v == null)
            return;
        int c = Integer.parseInt(v);
        if (midi_nrpn_connections.get(c) == null)
            midi_nrpn_connections.put(c, ImmutableList.create(ix));
        else {
            ImmutableList<Integer> olda = midi_nrpn_connections.get(c);
            ImmutableList<Integer> newa = ImmutableList.create(olda, 1, index -> ix);
            midi_nrpn_connections.put(c, newa);
        }
    }

    public SoftPerformer(ModelPerformer performer) {
        exclusiveClass = performer.getExclusiveClass();
        selfNonExclusive = performer.isSelfNonExclusive();

        Map<String, ModelConnectionBlock> connmap = new HashMap<>();

        List<ModelConnectionBlock> performer_connections = performer.getConnectionBlocks().toList();

        // Add modulation depth range (RPN 5) to the modulation wheel (cc#1)

        boolean isModulationWheelConectionFound = false;
        for (int j = 0; j < performer_connections.size(); j++) {
            ModelConnectionBlock connection = performer_connections.get(j);
            ImmutableList<ModelSource> sources = connection.getSources();
            ModelDestination dest = connection.getDestination();
            boolean isModulationWheelConection = false;
            if (dest != null && sources.size() > 1) {
                for (ModelSource source : sources) {
                    // check if connection block has the source "modulation
                    // wheel cc#1"
                    if (source.getIdentifier().getObject().equals(
                            "midi_cc")) {
                        if (source.getIdentifier().getVariable()
                                .equals("1")) {
                            isModulationWheelConection = true;
                            isModulationWheelConectionFound = true;
                            break;
                        }
                    }
                }
            }
            if (isModulationWheelConection) {
                ModelSource extra = new ModelSource(new ModelIdentifier("midi_rpn", "5"));
                ModelConnectionBlock connectionBlock = new ModelConnectionBlock(256, connection.getDestination(), connection.getSources().append(extra));
                performer_connections.set(j, connectionBlock);
            }
        }

        if (!isModulationWheelConectionFound) {
            ModelConnectionBlock conn = new ModelConnectionBlock(12800, new ModelDestination(ModelDestination.DESTINATION_PITCH), Arrays.asList(new ModelSource(ModelSource.SOURCE_LFO1,
                    ModelStandardTransform.DIRECTION_MIN2MAX,
                    ModelStandardTransform.POLARITY_BIPOLAR,
                    ModelStandardTransform.TRANSFORM_LINEAR), new ModelSource(new ModelIdentifier("midi_cc", "1", 0),
                    ModelStandardTransform.DIRECTION_MIN2MAX,
                    ModelStandardTransform.POLARITY_UNIPOLAR,
                    ModelStandardTransform.TRANSFORM_LINEAR), new ModelSource(new ModelIdentifier("midi_rpn",
                    "5"))));
            performer_connections.add(conn);

        }

        // Let Aftertouch to behave just like modulation wheel (cc#1)
        boolean channel_pressure_set = false;
        boolean poly_pressure = false;
        ModelConnectionBlock mod_cc_1_connection = null;
        int mod_cc_1_connection_src_ix = 0;

        for (ModelConnectionBlock connection : performer_connections) {
            ImmutableList<ModelSource> sources = connection.getSources();
            ModelDestination dest = connection.getDestination();
            // if(dest != null && sources != null)
            if (dest != null) {
                for (int i = 0; i < sources.size(); i++) {
                    ModelIdentifier srcid = sources.get(i).getIdentifier();
                    // check if connection block has the source "modulation
                    // wheel cc#1"
                    if (srcid.getObject().equals("midi_cc")) {
                        if (srcid.getVariable().equals("1")) {
                            mod_cc_1_connection = connection;
                            mod_cc_1_connection_src_ix = i;
                        }
                    }
                    // check if channel or poly pressure are already
                    // connected
                    if (srcid.getObject().equals("midi")) {
                        if (srcid.getVariable().equals("channel_pressure"))
                            channel_pressure_set = true;
                        if (srcid.getVariable().equals("poly_pressure"))
                            poly_pressure = true;
                    }
                }
            }

        }

        if (mod_cc_1_connection != null) {
            if (!channel_pressure_set) {
                ImmutableList<ModelSource> src_list = mod_cc_1_connection.getSources();
                ImmutableList<ModelSource> src_list_new = src_list.set(mod_cc_1_connection_src_ix, new ModelSource(new ModelIdentifier("midi", "channel_pressure")));
                ModelConnectionBlock mc = new ModelConnectionBlock(mod_cc_1_connection.getScale(), mod_cc_1_connection.getDestination(), src_list_new);
                connmap.put(extractKeys(mc), mc);
            }
            if (!poly_pressure) {
                ImmutableList<ModelSource> src_list = mod_cc_1_connection.getSources();
                ImmutableList<ModelSource> src_list_new = src_list.set(mod_cc_1_connection_src_ix, new ModelSource(new ModelIdentifier("midi", "poly_pressure")));
                ModelConnectionBlock mc = new ModelConnectionBlock(mod_cc_1_connection.getScale(), mod_cc_1_connection.getDestination(), src_list_new);
                connmap.put(extractKeys(mc), mc);
            }
        }

        // Enable Vibration Sound Controllers : 76, 77, 78
        ModelConnectionBlock found_vib_connection = null;
        for (ModelConnectionBlock connection : performer_connections) {
            ImmutableList<ModelSource> sources = connection.getSources();
            if (sources.size() != 0
                    && sources.get(0).getIdentifier().getObject().equals("lfo")) {
                if (connection.getDestination().getIdentifier().equals(
                        ModelDestination.DESTINATION_PITCH)) {
                    if (found_vib_connection == null)
                        found_vib_connection = connection;
                    else {
                        if (found_vib_connection.getSources().size() > sources.size())
                            found_vib_connection = connection;
                        else if (found_vib_connection.getSources().get(0)
                                .getIdentifier().getInstance() < 1) {
                            if (found_vib_connection.getSources().get(0)
                                    .getIdentifier().getInstance() >
                                    sources.get(0).getIdentifier().getInstance()) {
                                found_vib_connection = connection;
                            }
                        }
                    }

                }
            }
        }

        int instance = 1;

        if (found_vib_connection != null) {
            instance = found_vib_connection.getSources().get(0).getIdentifier()
                    .getInstance();
        }
        ModelConnectionBlock connection;

        connection = new ModelConnectionBlock(
            new ModelSource(new ModelIdentifier("midi_cc", "78"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            2000, new ModelDestination(
                new ModelIdentifier("lfo", "delay2", instance)));
        connmap.put(extractKeys(connection), connection);

        final double scale = found_vib_connection == null ? 0
                : found_vib_connection.getScale();
        connection = new ModelConnectionBlock(
            new ModelSource(new ModelIdentifier("lfo", instance)),
            new ModelSource(new ModelIdentifier("midi_cc", "77"),
                new ModelTransform() {
                    final double s = scale;
                    public double transform(double value) {
                        value = value * 2 - 1;
                        value *= 600;
                        if (s == 0) {
                            return value;
                        } else if (s > 0) {
                            if (value < -s)
                                value = -s;
                            return value;
                        } else {
                            if (value < s)
                                value = -s;
                            return -value;
                        }
                    }
                }), new ModelDestination(ModelDestination.DESTINATION_PITCH));
        connmap.put(extractKeys(connection), connection);

        connection = new ModelConnectionBlock(
            new ModelSource(new ModelIdentifier("midi_cc", "76"),
                ModelStandardTransform.DIRECTION_MIN2MAX,
                ModelStandardTransform.POLARITY_BIPOLAR,
                ModelStandardTransform.TRANSFORM_LINEAR),
            2400, new ModelDestination(
                new ModelIdentifier("lfo", "freq", instance)));
        connmap.put(extractKeys(connection), connection);

        // Add default connection blocks
        for (ModelConnectionBlock connection1 : defaultconnections)
            connmap.put(extractKeys(connection1), connection1);
        // Add connection blocks from modelperformer
        for (ModelConnectionBlock connection1 : performer_connections)
            connmap.put(extractKeys(connection1), connection1);
        // seperate connection blocks : Init time, Midi Time, Midi/Control Time,
        // Control Time
        List<ModelConnectionBlock> connections = new ArrayList<>();

        List<ImmutableList<Integer>> midi_ctrl_connections = new ArrayList<>(Collections.nCopies(128, null));
        List<ImmutableList<Integer>> midi_connections = new ArrayList<>(Collections.nCopies(5, null));

        int ix = 0;
        boolean mustBeOnTop = false;

        boolean forcedVelocity = false;
        boolean forcedKeynumber = false;

        for (ModelConnectionBlock connection1 : connmap.values()) {
            if (connection1.getDestination() != null) {
                ModelDestination dest = connection1.getDestination();
                ModelIdentifier id = dest.getIdentifier();
                if (id.getObject().equals("noteon")) {
                    mustBeOnTop = true;
                    if (id.getVariable().equals("keynumber"))
                        forcedKeynumber = true;
                    if (id.getVariable().equals("velocity"))
                        forcedVelocity = true;
                }
            }
            if (mustBeOnTop) {
                connections.add(0, connection1);
                mustBeOnTop = false;
            } else
                connections.add(connection1);
        }

        this.forcedVelocity = forcedVelocity;
        this.forcedKeynumber = forcedKeynumber;

        List<Integer> ctrl_connections_list = new ArrayList<>();
        Map<Integer, ImmutableList<Integer>> midi_rpn_connections = new HashMap<>();
        Map<Integer, ImmutableList<Integer>> midi_nrpn_connections = new HashMap<>();

        for (ModelConnectionBlock connection1 : connections) {
            for (ModelSource src : connection1.getSources()) {
                processSource(src, ix, ctrl_connections_list, midi_nrpn_connections, midi_rpn_connections, midi_ctrl_connections, midi_connections);
            }
            ix++;
        }

        this.midi_nrpn_connections = ImmutableMap.create(midi_nrpn_connections);
        this.midi_rpn_connections = ImmutableMap.create(midi_rpn_connections);
        this.midi_ctrl_connections = ImmutableList.create(midi_ctrl_connections);
        this.midi_connections = ImmutableList.create(midi_connections);
        ctrl_connections = ImmutableList.create(ctrl_connections_list.size(), ctrl_connections_list::get);
        this.connections = ImmutableList.create(connections);
        oscillators = performer.getOscillators();
    }

}
