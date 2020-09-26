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

import java.util.List;

/**
 * Connection blocks are used to connect source variable
 * to a destination variable.
 * For example Note On velocity can be connected to output gain.
 * In DLS this is called articulator and in SoundFonts (SF2) a modulator.
 *
 * @author Karl Helgason
 */
public final class ModelConnectionBlock {

    private static final ImmutableList<ModelSource> no_sources = ImmutableList.create();

    private final ImmutableList<ModelSource> sources;
    private final double scale;
    private final ModelDestination destination;

    public ModelConnectionBlock(double scale, ModelDestination destination) {
        this.scale = scale;
        this.destination = destination;
        sources = no_sources;
    }

    public ModelConnectionBlock(double scale, ModelDestination destination, List<ModelSource> sources) {
        this(scale, destination, ImmutableList.create(sources));
    }

    public ModelConnectionBlock(double scale, ModelDestination destination, ImmutableList<ModelSource> sources) {
        this.scale = scale;
        this.destination = destination;
        this.sources = sources;
    }

    public ModelConnectionBlock(ModelSource source, double scale, ModelDestination destination) {
        sources = source == null ? no_sources : ImmutableList.create(source);
        this.scale = scale;
        this.destination = destination;
    }

    public ModelConnectionBlock(ModelSource source, ModelSource control, double scale, ModelDestination destination) {
        if (source == null) {
            sources = no_sources;
        } else if (control == null) {
            sources = ImmutableList.create(source);
        } else {
            sources = ImmutableList.create(source, control);
        }
        this.scale = scale;
        this.destination = destination;
    }

    public ModelConnectionBlock(ModelSource source, ModelSource control, ModelDestination destination) {
        this(source, control, 1, destination);
    }

    public ModelDestination getDestination() {
        return destination;
    }

    public double getScale() {
        return scale;
    }

    public ImmutableList<ModelSource> getSources() {
        return sources;
    }

}
