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

/**
 * Soundfont modulator container.
 *
 * @author Karl Helgason
 */
final class SF2Modulator {

    public final static int SOURCE_NOTE_ON_VELOCITY = 2;
    public final static int SOURCE_NOTE_ON_KEYNUMBER = 3;
    public final static int SOURCE_POLY_PRESSURE = 10;
    public final static int SOURCE_CHANNEL_PRESSURE = 13;
    public final static int SOURCE_PITCH_WHEEL = 14;
    public final static int SOURCE_PITCH_SENSITIVITY = 16;
    public final static int SOURCE_MIDI_CONTROL = 128;
    public final static int SOURCE_DIRECTION_MAX_MIN = 256;
    public final static int SOURCE_POLARITY_BIPOLAR = 512;
    public final static int SOURCE_TYPE_CONCAVE = 1024;
    public final static int SOURCE_TYPE_CONVEX = 1024 * 2;
    public final static int SOURCE_TYPE_SWITCH = 1024 * 3;
    public final static int TRANSFORM_ABSOLUTE = 2;

    private final int sourceOperator;
    private final int destinationOperator;
    private final short amount;
    private final int amountSourceOperator;
    private final int transportOperator;

    SF2Modulator(int sourceOperator, int destinationOperator, short amount, int amountSourceOperator, int transportOperator) {
        this.sourceOperator = sourceOperator;
        this.destinationOperator = destinationOperator;
        this.amount = amount;
        this.amountSourceOperator = amountSourceOperator;
        this.transportOperator = transportOperator;
    }

    short getAmount() {
        return amount;
    }

    int getAmountSourceOperator() {
        return amountSourceOperator;
    }

    int getTransportOperator() {
        return transportOperator;
    }

    int getDestinationOperator() {
        return destinationOperator;
    }

    int getSourceOperator() {
        return sourceOperator;
    }

}
