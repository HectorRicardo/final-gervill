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

import own.main.ImmutableList;

/**
 * This class is a pointer to a binary array either in memory or on disk.
 *
 * @author Karl Helgason
 */
public final class ModelByteBuffer {

    private final ModelByteBuffer root;
    private final ImmutableList<Byte> buffer;
    private final long offset;
    private final long len;

    private ModelByteBuffer(ModelByteBuffer parent,
            long beginIndex, long endIndex, boolean independent) {
        long parent_len = parent.len;
        if (beginIndex < 0)
            beginIndex = 0;
        if (beginIndex > parent_len)
            beginIndex = parent_len;
        if (endIndex < 0)
            endIndex = 0;
        if (endIndex > parent_len)
            endIndex = parent_len;
        if (beginIndex > endIndex)
            beginIndex = endIndex;
        len = endIndex - beginIndex;
        if (independent) {
            buffer = parent.root.buffer;
            offset = arrayOffset(parent.root, beginIndex);
            root = this;
        } else {
            root = parent.root;
            buffer = null;
            offset = beginIndex;
        }
    }

    public ModelByteBuffer(byte[] buffer) {
        this.buffer = ImmutableList.create(buffer);
        this.offset = 0;
        this.len = buffer.length;
        root = this;
    }

    public ModelByteBuffer subbuffer(long beginIndex, long endIndex) {
        return subbuffer(beginIndex, endIndex, false);
    }

    public ModelByteBuffer subbuffer(long beginIndex, long endIndex,
            boolean independent) {
        return new ModelByteBuffer(this, beginIndex, endIndex, independent);
    }

    public byte[] array() {
        return root.buffer == null ? null : ImmutableList.toArray(root.buffer);
    }

    public long arrayOffset(ModelByteBuffer root, long offset) {
        if (root != this)
            return root.arrayOffset() + offset;
        return offset;
    }

    public long arrayOffset() {
        return arrayOffset(root, offset);
    }

    public long capacity() {
        return len;
    }

}
