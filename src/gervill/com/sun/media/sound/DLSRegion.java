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

/**
 * This class is used to store region parts for instrument.
 * A region has a velocity and key range which it response to.
 * And it has a list of modulators/articulators which
 * is used how to synthesize a single voice.
 * It is stored inside a "rgn " List Chunk inside DLS files.
 *
 * @author Karl Helgason
 */
public final class DLSRegion {

    public final static int OPTION_SELFNONEXCLUSIVE = 0x0001;

    private final int keyfrom;
    private final int keyto;
    private final int velfrom;
    private final int velto;
    private final int exclusiveClass;
    private final int fusoptions;
    private DLSSample sample;
    private final DLSSampleOptions sampleoptions;

    public DLSRegion(int keyfrom, int keyto, int velfrom, int velto, int exclusiveClass, int fusoptions, DLSSampleOptions sampleoptions) {
        this.keyfrom = keyfrom;
        this.keyto = keyto;
        this.velfrom = velfrom;
        this.velto = velto;
        this.exclusiveClass = exclusiveClass;
        this.fusoptions = fusoptions;
        this.sampleoptions = sampleoptions;
    }

    public void setSample(DLSSample sample) {
        this.sample = sample;
    }

    public int getExclusiveClass() {
        return exclusiveClass;
    }

    public int getFusoptions() {
        return fusoptions;
    }

    public int getKeyfrom() {
        return keyfrom;
    }

    public int getKeyto() {
        return keyto;
    }

    public DLSSample getSample() {
        return sample;
    }

    public int getVelfrom() {
        return velfrom;
    }

    public int getVelto() {
        return velto;
    }

    public DLSSampleOptions getSampleoptions() {
        return sampleoptions;
    }

}
