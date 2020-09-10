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
 * <code>MidiDevice</code> is the base interface for all MIDI devices.
 * Common devices include synthesizers, sequencers, MIDI input ports, and MIDI
 * output ports.
 *
 * <p>A <code>MidiDevice</code> can be a transmitter or a receiver of
 * MIDI events, or both. Therefore, it can provide  Transmitter
 * or  Receiver instances (or both). Typically, MIDI IN ports
 * provide transmitters, MIDI OUT ports and synthesizers provide
 * receivers. A Sequencer typically provides transmitters for playback
 * and receivers for recording.
 *
 * <p>A <code>MidiDevice</code> can be opened and closed explicitly as
 * well as implicitly. Explicit opening is accomplished by calling
 *  #open, explicit closing is done by calling 
 * #close on the <code>MidiDevice</code> instance.
 * If an application opens a <code>MidiDevice</code>
 * explicitly, it has to close it explicitly to free system resources
 * and enable the application to exit cleanly. Implicit opening is
 * done by calling  gervill.javax.sound.midi.MidiSystem#getReceiver
 * MidiSystem.getReceiver and 
 * gervill.javax.sound.midi.MidiSystem#getTransmitter
 * MidiSystem.getTransmitter. The <code>MidiDevice</code> used by
 * <code>MidiSystem.getReceiver</code> and
 * <code>MidiSystem.getTransmitter</code> is implementation-dependant
 * unless the properties <code>gervill.javax.sound.midi.Receiver</code>
 * and <code>gervill.javax.sound.midi.Transmitter</code> are used (see the
 * description of properties to select default providers in
 *  gervill.javax.sound.midi.MidiSystem). A <code>MidiDevice</code>
 * that was opened implicitly, is closed implicitly by closing the
 * <code>Receiver</code> or <code>Transmitter</code> that resulted in
 * opening it. If more than one implicitly opening
 * <code>Receiver</code> or <code>Transmitter</code> were obtained by
 * the application, the device is closed after the last
 * <code>Receiver</code> or <code>Transmitter</code> has been
 * closed. On the other hand, calling <code>getReceiver</code> or
 * <code>getTransmitter</code> on the device instance directly does
 * not open the device implicitly. Closing these
 * <code>Transmitter</code>s and <code>Receiver</code>s does not close
 * the device implicitly. To use a device with <code>Receiver</code>s
 * or <code>Transmitter</code>s obtained this way, the device has to
 * be opened and closed explicitly.
 *
 * <p>If implicit and explicit opening and closing are mixed on the
 * same <code>MidiDevice</code> instance, the following rules apply:
 *
 * <ul>
 * <li>After an explicit open (either before or after implicit
 * opens), the device will not be closed by implicit closing. The only
 * way to close an explicitly opened device is an explicit close.</li>
 *
 * <li>An explicit close always closes the device, even if it also has
 * been opened implicitly. A subsequent implicit close has no further
 * effect.</li>
 * </ul>
 *
 * To detect if a MidiDevice represents a hardware MIDI port, the
 * following programming technique can be used:
 *
 * <pre>{@code
 * MidiDevice device = ...;
 * if ( ! (device instanceof Sequencer) && ! (device instanceof Synthesizer)) {
 *   // we're now sure that device represents a MIDI port
 *   // ...
 * }
 * }</pre>
 *
 * <p>
 * A <code>MidiDevice</code> includes a <code> MidiDevice.Info</code> object
 * to provide manufacturer information and so on.
 *
 * see Synthesizer
 * see Sequencer
 * see Receiver
 * see Transmitter
 *
 * @author Kara Kytle
 * @author Florian Bomers
 */

public interface MidiDevice extends AutoCloseable {


     /**
     * Closes the device, indicating that the device should now release
     * any system resources it is using.
     *
     * <p>All <code>Receiver</code> and <code>Transmitter</code> instances
     * open from this device are closed. This includes instances retrieved
     * via <code>MidiSystem</code>.
     *
     * see #open
     * see #isOpen
     */
    public void close();


}
