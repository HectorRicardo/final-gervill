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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to define how to synthesize audio in universal maner
 * for both SF2 and DLS instruments.
 *
 * @author Karl Helgason
 */
public final class ModelPerformer {

    private final List<ModelByteBufferWavetable> oscillators = new ArrayList<>();
    private final List<ModelConnectionBlock> connectionBlocks = new ArrayList<>();
    private final int keyFrom;
    private final int keyTo;
    private final int velFrom;
    private final int velTo;
    private final int exclusiveClass;
    private final boolean selfNonExclusive;

    public ModelPerformer(int keyFrom, int keyTo, int velFrom, int velTo, int exclusiveClass, boolean selfNonExclusive) {
        this.keyFrom = keyFrom;
        this.keyTo = keyTo;
        this.velFrom = velFrom;
        this.velTo = velTo;
        this.exclusiveClass = exclusiveClass;
        this.selfNonExclusive = selfNonExclusive;
    }

    public List<ModelConnectionBlock> getConnectionBlocks() {
        return connectionBlocks;
    }

    public List<ModelByteBufferWavetable> getOscillators() {
        return oscillators;
    }

    public int getExclusiveClass() {
        return exclusiveClass;
    }

    public boolean isSelfNonExclusive() {
        return selfNonExclusive;
    }

    public int getKeyFrom() {
        return keyFrom;
    }

    public int getKeyTo() {
        return keyTo;
    }

    public int getVelFrom() {
        return velFrom;
    }

    public int getVelTo() {
        return velTo;
    }

}
