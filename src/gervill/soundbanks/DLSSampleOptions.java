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

import own.main.ImmutableList;

import java.util.List;

/**
 * This class stores options how to playback sampled data like pitch/tuning,
 * attenuation and loops.
 * It is stored as a "wsmp" chunk inside DLS files.
 *
 * @author Karl Helgason
 */
final class DLSSampleOptions {

    private final int unitynote;
    private final short finetune;
    private final ImmutableList<DLSSampleLoop> loops;

    DLSSampleOptions(int unitynote, short finetune, List<DLSSampleLoop> loops) {
        this.unitynote = unitynote;
        this.finetune = finetune;
        this.loops = ImmutableList.create(loops);
    }

    ImmutableList<DLSSampleLoop> getLoops() {
        return loops;
    }

    int getUnitynote() {
        return unitynote;
    }

    short getFinetune() {
        return finetune;
    }

}
