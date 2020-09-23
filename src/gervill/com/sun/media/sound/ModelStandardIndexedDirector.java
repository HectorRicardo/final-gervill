/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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

import own.main.Immutable2DList;
import own.main.ImmutableList;

/**
 * A standard indexed director who chooses performers
 * by there keyfrom,keyto,velfrom,velto properties.
 *
 * @author Karl Helgason
 */
public final class ModelStandardIndexedDirector {

    private final SoftChannel player;
    private final boolean noteOnUsed;

    // Variables needed for index
    private final Immutable2DList<Byte> trantables;
    private final int counter;
    private final Immutable2DList<Integer> mat;

    private ModelStandardIndexedDirector(SoftChannel player, boolean noteOnUsed, Byte[][] trantables, int counter, Integer[][] mat) {
        this.player = player;
        this.noteOnUsed = noteOnUsed;
        this.trantables = Immutable2DList.create(trantables);
        this.counter = counter;
        this.mat = Immutable2DList.create(mat);
    }

    public static ModelStandardIndexedDirector create(ImmutableList<ModelPerformer> performers, SoftChannel player) {
        Byte[][] trantables = new Byte[2][129];
        int[] counters = new int[trantables.length];
        Integer[][] mat = buildindex(performers, trantables, counters);
        return new ModelStandardIndexedDirector(player, performers.size() > 0, trantables, counters[0], mat);
    }

    private ImmutableList<Integer> lookupIndex(int x, int y) {
        if ((x >= 0) && (x < 128) && (y >= 0) && (y < 128)) {
            int xt = defVal(trantables.get(0, x));
            int yt = defVal(trantables.get(1, y));
            if (xt != -1 && yt != -1) {
                return mat.get(xt + yt * counter);
            }
        }
        return null;
    }

    private static int defVal(Byte val) {
        return val == null ? 0 : val;
    }

    private static int restrict(int value) {
        if(value < 0) return 0;
        return Math.min(value, 127);
    }

    private static Integer[][] buildindex(ImmutableList<ModelPerformer> performers, Byte[][] trantables, int[] counters) {
        for (ModelPerformer performer : performers) {
            int keyFrom = performer.getKeyFrom();
            int keyTo = performer.getKeyTo();
            int velFrom = performer.getVelFrom();
            int velTo = performer.getVelTo();
            if (keyFrom > keyTo) continue;
            if (velFrom > velTo) continue;
            keyFrom = restrict(keyFrom);
            keyTo = restrict(keyTo);
            velFrom = restrict(velFrom);
            velTo = restrict(velTo);
            trantables[0][keyFrom] = 1;
            trantables[0][keyTo + 1] = 1;
            trantables[1][velFrom] = 1;
            trantables[1][velTo + 1] = 1;
        }
        for (int d = 0; d < trantables.length; d++) {
            Byte[] trantable = trantables[d];
            int transize = trantable.length;
            for (int i = transize - 1; i >= 0; i--) {
                if (trantable[i] != null && trantable[i] == 1) {
                    trantable[i] = -1;
                    break;
                }
                trantable[i] = -1;
            }
            int counter = -1;
            for (int i = 0; i < transize; i++) {
                if (trantable[i] != null && trantable[i] != 0) {
                    counter++;
                    if (trantable[i] == -1)
                        break;
                }
                trantable[i] = (byte) counter;
            }
            counters[d] = counter;
        }
        Integer[][] mat = new Integer[counters[0] * counters[1]][];
        int ix = 0;
        for (ModelPerformer performer : performers) {
            int keyFrom = performer.getKeyFrom();
            int keyTo = performer.getKeyTo();
            int velFrom = performer.getVelFrom();
            int velTo = performer.getVelTo();
            if (keyFrom > keyTo) continue;
            if (velFrom > velTo) continue;
            keyFrom = restrict(keyFrom);
            keyTo = restrict(keyTo);
            velFrom = restrict(velFrom);
            velTo = restrict(velTo);
            int x_from = trantables[0][keyFrom];
            int x_to = trantables[0][keyTo + 1];
            int y_from = trantables[1][velFrom];
            int y_to = trantables[1][velTo + 1];
            if (x_to == -1)
                x_to = counters[0];
            if (y_to == -1)
                y_to = counters[1];
            for (int y = y_from; y < y_to; y++) {
                int i = x_from + y * counters[0];
                for (int x = x_from; x < x_to; x++) {
                    Integer[] mprev = mat[i];
                    if (mprev == null) {
                        mat[i] = new Integer[] { ix };
                    } else {
                        Integer[] mnew = new Integer[mprev.length + 1];
                        mnew[mnew.length - 1] = ix;
                        System.arraycopy(mprev, 0, mnew, 0, mprev.length);
                        mat[i] = mnew;
                    }
                    i++;
                }
            }
            ix++;
        }
        return mat;
    }

    public void noteOn(int noteNumber, int velocity) {
        if (!noteOnUsed)
            return;
        ImmutableList<Integer> plist = lookupIndex(noteNumber, velocity);
        if(plist == null) return;
        for (int i : plist) {
            player.play(i, null);
        }
    }
}
