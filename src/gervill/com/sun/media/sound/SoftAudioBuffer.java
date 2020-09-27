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

import java.util.Arrays;

/**
 * This class is used to store audio buffer.
 *
 * @author Karl Helgason
 */
public final class SoftAudioBuffer {

    private float[] buffer;
    private boolean empty = true;
    private byte[] converter_buffer;

    public void swap(SoftAudioBuffer swap) {
        float[] bak_buffer = buffer;
        boolean bak_empty = empty;
        byte[] bak_converter_buffer = converter_buffer;

        buffer = swap.buffer;
        empty = swap.empty;
        converter_buffer = swap.converter_buffer;

        swap.buffer = bak_buffer;
        swap.empty = bak_empty;
        swap.converter_buffer = bak_converter_buffer;
    }

    public void clear() {
        if (!empty) {
            Arrays.fill(buffer, 0);
            empty = true;
        }
    }

    public boolean isSilent() {
        return empty;
    }

    public float[] array() {
        empty = false;
        if (buffer == null)
            buffer = new float[300];
        return buffer;
    }

    public void get(byte[] buffer, int channel) {

        if (converter_buffer == null)
            converter_buffer = new byte[600];

        SoftSynthesizer.SYNTH_CONVERTER.toByteArray(array(), 300, converter_buffer);
        if (channel >= 2)
            return;
        for (int j = 0; j < 2; j++) {
            int z = channel * 2 + j;
            for (int i = 0; i < 300; i++) {
                buffer[z] = converter_buffer[2 * i + j];
                z += 4;
            }
        }

    }
}
