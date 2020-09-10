/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * A <code>ShortMessage</code> contains a MIDI message that has at most
 * two data bytes following its status byte.  The types of MIDI message
 * that satisfy this criterion are channel voice, channel mode, system common,
 * and system real-time--in other words, everything except system exclusive
 * and meta-events.  The <code>ShortMessage</code> class provides methods
 * for getting and setting the contents of the MIDI message.
 * <p>
 * A number of <code>ShortMessage</code> methods have integer parameters by which
 * you specify a MIDI status or data byte.  If you know the numeric value, you
 * can express it directly.  For system common and system real-time messages,
 * you can often use the corresponding fields of <code>ShortMessage</code>, such as
 *  #SYSTEM_RESET SYSTEM_RESET.  For channel messages,
 * the upper four bits of the status byte are specified by a command value and
 * the lower four bits are specified by a MIDI channel number. To
 * convert incoming MIDI data bytes that are in the form of Java's signed bytes,
 * you can use the <A HREF="MidiMessage.html#integersVsBytes">conversion code</A>
 * given in the <code> MidiMessage</code> class description.
 *
 * see SysexMessage
 * see MetaMessage
 *
 * @author David Rivas
 * @author Kara Kytle
 * @author Florian Bomers
 */

public class ShortMessage extends MidiMessage {


    // Status byte defines


    // System common messages


    // System real-time messages

    /**
     * Status byte for Active Sensing message (0xFE, or 254).
     * see MidiMessage#getStatus
     */
    public static final int ACTIVE_SENSING                              = 0xFE; // 254


    // Channel voice message upper nibble defines

    /**
     * Command value for Note Off message (0x80, or 128)
     */
    public static final int NOTE_OFF                                    = 0x80;  // 128

    /**
     * Command value for Note On message (0x90, or 144)
     */
    public static final int NOTE_ON                                             = 0x90;  // 144

    /**
     * Command value for Polyphonic Key Pressure (Aftertouch) message (0xA0, or 160)
     */
    public static final int POLY_PRESSURE                               = 0xA0;  // 160

    /**
     * Command value for Control Change message (0xB0, or 176)
     */
    public static final int CONTROL_CHANGE                              = 0xB0;  // 176

    /**
     * Command value for Program Change message (0xC0, or 192)
     */
    public static final int PROGRAM_CHANGE                              = 0xC0;  // 192

    /**
     * Command value for Channel Pressure (Aftertouch) message (0xD0, or 208)
     */
    public static final int CHANNEL_PRESSURE                    = 0xD0;  // 208

    /**
     * Command value for Pitch Bend message (0xE0, or 224)
     */
    public static final int PITCH_BEND                                  = 0xE0;  // 224


    // Instance variables


    /**
     * Constructs a new <code>ShortMessage</code>.
     * @param data an array of bytes containing the complete message.
     * The message data may be changed using the <code>setMessage</code>
     * method.
     * see #setMessage
     */
    // $$fb this should throw an Exception in case of an illegal message!
    protected ShortMessage(byte[] data) {
        // $$fb this may set an invalid message.
        // Can't correct without compromising compatibility
        super(data);
    }


    /**
     * Obtains the MIDI channel associated with this event.  This method
     * assumes that the event is a MIDI channel message; if not, the return
     * value will not be meaningful.
     * @return MIDI channel associated with the message.
     * see #setMessage(int, int, int, int)
     */
    public int getChannel() {
        // this returns 0 if an invalid message is set
        return (getStatus() & 0x0F);
    }


    /**
     * Obtains the MIDI command associated with this event.  This method
     * assumes that the event is a MIDI channel message; if not, the return
     * value will not be meaningful.
     * @return the MIDI command associated with this event
     * see #setMessage(int, int, int, int)
     */
    public int getCommand() {
        // this returns 0 if an invalid message is set
        return (getStatus() & 0xF0);
    }


    /**
     * Obtains the first data byte in the message.
     * @return the value of the <code>data1</code> field
     * see #setMessage(int, int, int)
     */
    public int getData1() {
        if (length > 1) {
            return (data[1] & 0xFF);
        }
        return 0;
    }


    /**
     * Obtains the second data byte in the message.
     * @return the value of the <code>data2</code> field
     * see #setMessage(int, int, int)
     */
    public int getData2() {
        if (length > 2) {
            return (data[2] & 0xFF);
        }
        return 0;
    }


    /**
     * Creates a new object of the same class and with the same contents
     * as this object.
     * @return a clone of this instance.
     */
    public Object clone() {
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, newData.length);

        ShortMessage msg = new ShortMessage(newData);
        return msg;
    }


}
