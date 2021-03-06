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

import gervill.com.sun.media.sound.*;
import gervill.javax.sound.midi.Patch;
import own.main.ImmutableList;

import java.util.*;

/**
 * This class is used to store information to describe instrument.
 * It contains list of regions and modulators.
 * It is stored inside a "ins " List Chunk inside DLS files.
 * In the DLS documentation a modulator is called articulator.
 *
 * @author Karl Helgason
 */
final class DLSInstrument extends ModelInstrument {

    private final ImmutableList<DLSRegion> regions;
    private final ImmutableList<DLSModulator> modulators;

    DLSInstrument(String name, List<DLSRegion> regions, List<DLSModulator> modulators, Patch patch) {
        super(patch, name);
        this.regions = ImmutableList.create(regions);
        this.modulators = ImmutableList.create(modulators);
    }

    private static ModelIdentifier convertToModelDest(int dest) {
        if (dest == DLSModulator.CONN_DST_NONE)
            return null;
        if (dest == DLSModulator.CONN_DST_GAIN)
            return ModelDestination.DESTINATION_GAIN;
        if (dest == DLSModulator.CONN_DST_PITCH)
            return ModelDestination.DESTINATION_PITCH;
        if (dest == DLSModulator.CONN_DST_PAN)
            return ModelDestination.DESTINATION_PAN;

        if (dest == DLSModulator.CONN_DST_LFO_FREQUENCY)
            return ModelDestination.DESTINATION_LFO1_FREQ;
        if (dest == DLSModulator.CONN_DST_LFO_STARTDELAY)
            return ModelDestination.DESTINATION_LFO1_DELAY;

        if (dest == DLSModulator.CONN_DST_EG1_ATTACKTIME)
            return ModelDestination.DESTINATION_EG1_ATTACK;
        if (dest == DLSModulator.CONN_DST_EG1_DECAYTIME)
            return ModelDestination.DESTINATION_EG1_DECAY;
        if (dest == DLSModulator.CONN_DST_EG1_RELEASETIME)
            return ModelDestination.DESTINATION_EG1_RELEASE;
        if (dest == DLSModulator.CONN_DST_EG1_SUSTAINLEVEL)
            return ModelDestination.DESTINATION_EG1_SUSTAIN;

        if (dest == DLSModulator.CONN_DST_EG2_ATTACKTIME)
            return ModelDestination.DESTINATION_EG2_ATTACK;
        if (dest == DLSModulator.CONN_DST_EG2_DECAYTIME)
            return ModelDestination.DESTINATION_EG2_DECAY;
        if (dest == DLSModulator.CONN_DST_EG2_RELEASETIME)
            return ModelDestination.DESTINATION_EG2_RELEASE;
        if (dest == DLSModulator.CONN_DST_EG2_SUSTAINLEVEL)
            return ModelDestination.DESTINATION_EG2_SUSTAIN;

        // DLS2 Destinations
        if (dest == DLSModulator.CONN_DST_KEYNUMBER)
            return ModelDestination.DESTINATION_KEYNUMBER;

        if (dest == DLSModulator.CONN_DST_CHORUS)
            return ModelDestination.DESTINATION_CHORUS;
        if (dest == DLSModulator.CONN_DST_REVERB)
            return ModelDestination.DESTINATION_REVERB;

        if (dest == DLSModulator.CONN_DST_VIB_FREQUENCY)
            return ModelDestination.DESTINATION_LFO2_FREQ;
        if (dest == DLSModulator.CONN_DST_VIB_STARTDELAY)
            return ModelDestination.DESTINATION_LFO2_DELAY;

        if (dest == DLSModulator.CONN_DST_EG1_DELAYTIME)
            return ModelDestination.DESTINATION_EG1_DELAY;
        if (dest == DLSModulator.CONN_DST_EG1_HOLDTIME)
            return ModelDestination.DESTINATION_EG1_HOLD;
        if (dest == DLSModulator.CONN_DST_EG1_SHUTDOWNTIME)
            return ModelDestination.DESTINATION_EG1_SHUTDOWN;

        if (dest == DLSModulator.CONN_DST_EG2_DELAYTIME)
            return ModelDestination.DESTINATION_EG2_DELAY;
        if (dest == DLSModulator.CONN_DST_EG2_HOLDTIME)
            return ModelDestination.DESTINATION_EG2_HOLD;

        if (dest == DLSModulator.CONN_DST_FILTER_CUTOFF)
            return ModelDestination.DESTINATION_FILTER_FREQ;
        if (dest == DLSModulator.CONN_DST_FILTER_Q)
            return ModelDestination.DESTINATION_FILTER_Q;

        return null;
    }

    private static ModelIdentifier convertToModelSrc(int src) {
        if (src == DLSModulator.CONN_SRC_NONE)
            return null;

        if (src == DLSModulator.CONN_SRC_LFO)
            return ModelSource.SOURCE_LFO1;
        if (src == DLSModulator.CONN_SRC_KEYONVELOCITY)
            return ModelSource.SOURCE_NOTEON_VELOCITY;
        if (src == DLSModulator.CONN_SRC_KEYNUMBER)
            return ModelSource.SOURCE_NOTEON_KEYNUMBER;
        if (src == DLSModulator.CONN_SRC_EG1)
            return ModelSource.SOURCE_EG1;
        if (src == DLSModulator.CONN_SRC_EG2)
            return ModelSource.SOURCE_EG2;
        if (src == DLSModulator.CONN_SRC_PITCHWHEEL)
            return ModelSource.SOURCE_MIDI_PITCH;
        if (src == DLSModulator.CONN_SRC_CC1)
            return new ModelIdentifier("midi_cc", "1", 0);
        if (src == DLSModulator.CONN_SRC_CC7)
            return new ModelIdentifier("midi_cc", "7", 0);
        if (src == DLSModulator.CONN_SRC_CC10)
            return new ModelIdentifier("midi_cc", "10", 0);
        if (src == DLSModulator.CONN_SRC_CC11)
            return new ModelIdentifier("midi_cc", "11", 0);
        if (src == DLSModulator.CONN_SRC_RPN0)
            return new ModelIdentifier("midi_rpn", "0", 0);
        if (src == DLSModulator.CONN_SRC_RPN1)
            return new ModelIdentifier("midi_rpn", "1", 0);

        if (src == DLSModulator.CONN_SRC_POLYPRESSURE)
            return ModelSource.SOURCE_MIDI_POLY_PRESSURE;
        if (src == DLSModulator.CONN_SRC_CHANNELPRESSURE)
            return ModelSource.SOURCE_MIDI_CHANNEL_PRESSURE;
        if (src == DLSModulator.CONN_SRC_VIBRATO)
            return ModelSource.SOURCE_LFO2;
        if (src == DLSModulator.CONN_SRC_MONOPRESSURE)
            return ModelSource.SOURCE_MIDI_CHANNEL_PRESSURE;

        if (src == DLSModulator.CONN_SRC_CC91)
            return new ModelIdentifier("midi_cc", "91", 0);
        if (src == DLSModulator.CONN_SRC_CC93)
            return new ModelIdentifier("midi_cc", "93", 0);

        return null;
    }

    private static ModelConnectionBlock convertToModel(DLSModulator mod) {
        ModelIdentifier source = convertToModelSrc(mod.getSource());
        ModelIdentifier control = convertToModelSrc(mod.getControl());
        ModelIdentifier destination_id =
                convertToModelDest(mod.getDestination());

        if (destination_id == null) return null;

        int scale = mod.getScale();
        double f_scale = scale == Integer.MIN_VALUE ? Double.NEGATIVE_INFINITY : scale / 65536.0;

        List<ModelSource> sources = new ArrayList<>();
        if (control != null) {
            boolean direction = false;
            boolean polarity = false;
            int myTransform = 0;

            if (control == ModelSource.SOURCE_MIDI_PITCH || control == ModelSource.SOURCE_LFO1 || control == ModelSource.SOURCE_LFO2) {
                polarity = ModelStandardTransform.POLARITY_BIPOLAR;
            }
            if (mod.getVersion() == 1) {
                if (mod.getTransform() == DLSModulator.CONN_TRN_CONCAVE) {
                    myTransform = ModelStandardTransform.TRANSFORM_CONCAVE;
                    direction = ModelStandardTransform.DIRECTION_MAX2MIN;
                }
            } else if (mod.getVersion() == 2) {
                int transform = mod.getTransform();
                int ctr_transform_invert = (transform >> 9) & 1;
                int ctr_transform_bipolar = (transform >> 8) & 1;

                myTransform = ModelStandardTransform.TRANSFORM_LINEAR;
                polarity = ctr_transform_bipolar == 1;
                direction = ctr_transform_invert == 1;
            }

            sources.add(new ModelSource(control, new ModelStandardTransform(direction, polarity, myTransform)));
        }
        if (source != null) {
            boolean myDirection = false;
            boolean myPolarity = false;
            int myTransform = 0;

            if (source == ModelSource.SOURCE_MIDI_PITCH || source == ModelSource.SOURCE_LFO1 || source == ModelSource.SOURCE_LFO2) {
                myPolarity = ModelStandardTransform.POLARITY_BIPOLAR;
            }
            if (mod.getVersion() == 1) {
                if (mod.getTransform() == DLSModulator.CONN_TRN_CONCAVE) {
                    myTransform = ModelStandardTransform.TRANSFORM_CONCAVE;
                    myDirection = ModelStandardTransform.DIRECTION_MAX2MIN;
                }
            } else if (mod.getVersion() == 2) {
                int transform = mod.getTransform();
                int src_transform_invert = (transform >> 15) & 1;
                int src_transform_bipolar = (transform >> 14) & 1;

                myTransform = ModelStandardTransform.TRANSFORM_LINEAR;
                myPolarity = src_transform_bipolar == 1;
                myDirection = src_transform_invert == 1;
            }

            sources.add(new ModelSource(source, new ModelStandardTransform(myDirection, myPolarity, myTransform)));
        }

        return new ModelConnectionBlock(f_scale, new ModelDestination(destination_id), sources);

    }

    @Override
    protected ModelPerformer[] buildPerformers() {
        List<ModelPerformer> performers = new ArrayList<>();

        Map<String, DLSModulator> modmap = new HashMap<>();
        for (DLSModulator mod : modulators) {
            modmap.put(mod.getSource() + "x" + mod.getControl() + "=" +
                    mod.getDestination(), mod);
        }

        for (DLSRegion zone : regions) {

            List<ModelConnectionBlock> blocks = new ArrayList<>();
            for (DLSModulator mod : modmap.values()) {
                ModelConnectionBlock p = convertToModel(mod);
                if (p != null)
                    blocks.add(p);
            }


            DLSSample sample = zone.getSample();
            DLSSampleOptions sampleopt = zone.getSampleoptions();
            if (sampleopt == null)
                sampleopt = sample.getSampleoptions();

            ModelByteBuffer buff = sample.getDataBuffer();

            float pitchcorrection = (-sampleopt.getUnitynote() * 100) +
                    sampleopt.getFinetune();

            int loopStart = -1;
            int loopLength = -1;
            int loopType = ModelByteBufferWavetable.LOOP_TYPE_OFF;
            if (sampleopt.getLoops().size() != 0) {
                DLSSampleLoop loop = sampleopt.getLoops().get(0);
                loopStart = (int) loop.getStart();
                loopLength = (int) loop.getLength();
                if (loop.getType() == DLSSampleLoop.LOOP_TYPE_FORWARD)
                    loopType = ModelByteBufferWavetable.LOOP_TYPE_FORWARD;
                else if (loop.getType() == DLSSampleLoop.LOOP_TYPE_RELEASE)
                    loopType = ModelByteBufferWavetable.LOOP_TYPE_RELEASE;
                else
                    loopType = ModelByteBufferWavetable.LOOP_TYPE_FORWARD;
            }

            ModelByteBufferWavetable osc = new ModelByteBufferWavetable(buff, sample.getFormat(), pitchcorrection, 0, loopStart, loopLength, loopType, null);

            blocks.add(
                    new ModelConnectionBlock(SoftFilter.FILTERTYPE_LP12,
                            new ModelDestination(
                                    new ModelIdentifier("filter", "type", 1))));

            performers.add(new ModelPerformer(zone.getKeyfrom(), zone.getKeyto(), zone.getVelfrom(), zone.getVelto(), zone.getExclusiveClass(), (zone.getFusoptions() & DLSRegion.OPTION_SELFNONEXCLUSIVE) != 0, Collections.singletonList(osc), blocks));

        }

        return performers.toArray(new ModelPerformer[0]);
    }

}
