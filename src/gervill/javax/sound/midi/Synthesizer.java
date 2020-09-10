/*
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
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

package gervill.javax.sound.midi;


/**
 * A <code>Synthesizer</code> generates sound.  This usually happens when one of
 * the <code>Synthesizer</code>'s  MidiChannel objects receives a
 *  MidiChannel#noteOn(int, int) noteOn message, either
 * directly or via the <code>Synthesizer</code> object.
 * Many <code>Synthesizer</code>s support <code>Receivers</code>, through which
 * MIDI events can be delivered to the <code>Synthesizer</code>.
 * In such cases, the <code>Synthesizer</code> typically responds by sending
 * a corresponding message to the appropriate <code>MidiChannel</code>, or by
 * processing the event itself if the event isn't one of the MIDI channel
 * messages.
 * <p>
 * The <code>Synthesizer</code> interface includes methods for loading and
 * unloading instruments from soundbanks.  An instrument is a specification for synthesizing a
 * certain type of sound, whether that sound emulates a traditional instrument or is
 * some kind of sound effect or other imaginary sound. A soundbank is a collection of instruments, organized
 * by bank and program number (via the instrument's <code>Patch</code> object).
 * Different <code>Synthesizer</code> classes might implement different sound-synthesis
 * techniques, meaning that some instruments and not others might be compatible with a
 * given synthesizer.
 * Also, synthesizers may have a limited amount of memory for instruments, meaning
 * that not every soundbank and instrument can be used by every synthesizer, even if
 * the synthesis technique is compatible.
 * To see whether the instruments from
 * a certain soundbank can be played by a given synthesizer, invoke the
 *  #isSoundbankSupported(Soundbank) isSoundbankSupported method of
 * <code>Synthesizer</code>.
 * <p>
 * "Loading" an instrument means that that instrument becomes available for
 * synthesizing notes.  The instrument is loaded into the bank and
 * program location specified by its <code>Patch</code> object.  Loading does
 * not necessarily mean that subsequently played notes will immediately have
 * the sound of this newly loaded instrument.  For the instrument to play notes,
 * one of the synthesizer's <code>MidiChannel</code> objects must receive (or have received)
 * a program-change message that causes that particular instrument's
 * bank and program number to be selected.
 *
 * see MidiSystem#getSynthesizer
 * see Soundbank
 * see Instrument
 * see MidiChannel#programChange(int, int)
 * see Receiver
 * see Transmitter
 * see MidiDevice
 *
 * @author Kara Kytle
 */
public interface Synthesizer extends MidiDevice {


    // SYNTHESIZER METHODS


    // RECEIVER METHODS

    /**
     * Obtains the name of the receiver.
     * @return receiver name
     */
    //  public abstract String getName();


    /**
     * Opens the receiver.
     * throws MidiUnavailableException if the receiver is cannot be opened,
     * usually because the MIDI device is in use by another application.
     * throws SecurityException if the receiver cannot be opened due to security
     * restrictions.
     */
    //  public abstract void open() throws MidiUnavailableException, SecurityException;


    /**
     * Closes the receiver.
     */
    //  public abstract void close();


    /**
     * Sends a MIDI event to the receiver.
     * @param event event to send.
     * throws IllegalStateException if the receiver is not open.
     */
    //  public void send(MidiEvent event) throws IllegalStateException {
    //
    //  }


    /**
     * Obtains the set of controls supported by the
     * element.  If no controls are supported, returns an
     * array of length 0.
     * @return set of controls
     */
    // $$kk: 03.04.99: josh bloch recommends getting rid of this:
    // what can you really do with a set of untyped controls??
    // $$kk: 03.05.99: i am putting this back in.  for one thing,
    // you can check the length and know whether you should keep
    // looking....
    // public Control[] getControls();

    /**
     * Obtains the specified control.
     * @param controlClass class of the requested control
     * @return requested control object, or null if the
     * control is not supported.
     */
    // public Control getControl(Class controlClass);
}
