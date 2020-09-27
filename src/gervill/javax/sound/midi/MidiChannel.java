/*
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
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
 * A <code>MidiChannel</code> object represents a single MIDI channel.
 * Generally, each <code>MidiChannel</code> method processes a like-named MIDI
 * "channel voice" or "channel mode" message as defined by the MIDI specification. However,
 * <code>MidiChannel</code> adds some "get" methods  that retrieve the value
 * most recently set by one of the standard MIDI channel messages.  Similarly,
 * methods for per-channel solo and mute have been added.
 * <p>
 * A <code> Synthesizer</code> object has a collection
 * of <code>MidiChannels</code>, usually one for each of the 16 channels
 * prescribed by the MIDI 1.0 specification.  The <code>Synthesizer</code>
 * generates sound when its <code>MidiChannels</code> receive
 * <code>noteOn</code> messages.
 * <p>
 * See the MIDI 1.0 Specification for more information about the prescribed
 * behavior of the MIDI channel messages, which are not exhaustively
 * documented here.  The specification is titled <code>MIDI Reference:
 * The Complete MIDI 1.0 Detailed Specification</code>, and is published by
 * the MIDI Manufacturer's Association (<a href = http://www.midi.org>
 * http://www.midi.org</a>).
 * <p>
 * MIDI was originally a protocol for reporting the gestures of a keyboard
 * musician.  This genesis is visible in the <code>MidiChannel</code> API, which
 * preserves such MIDI concepts as key number, key velocity, and key pressure.
 * It should be understood that the MIDI data does not necessarily originate
 * with a keyboard player (the source could be a different kind of musician, or
 * software).  Some devices might generate constant values for velocity
 * and pressure, regardless of how the note was performed.
 * Also, the MIDI specification often leaves it up to the
 * synthesizer to use the data in the way the implementor sees fit.  For
 * example, velocity data need not always be mapped to volume and/or brightness.
 * <p>
 * see Synthesizer#getChannels
 *
 * @author David Rivas
 * @author Kara Kytle
 */

public interface MidiChannel {

    /**
     * Starts the specified note sounding.  The key-down velocity
     * usually controls the note's volume and/or brightness.
     * If <code>velocity</code> is zero, this method instead acts like
     * #noteOff(int), terminating the note.
     *
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @param velocity   the speed with which the key was depressed
     *                   <p>
     *                   see #noteOff(int, int)
     */
    void noteOn(int noteNumber, int velocity);

    /**
     * Turns the specified note off.  The key-up velocity, if not ignored, can
     * be used to affect how quickly the note decays.
     * In any case, the note might not die away instantaneously; its decay
     * rate is determined by the internals of the <code>Instrument</code>.
     * If the Hold Pedal (a controller; see
     * #controlChange(int, int) controlChange)
     * is down, the effect of this method is deferred until the pedal is
     * released.
     *
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @param velocity   the speed with which the key was released
     *                   <p>
     *                   see #noteOff(int)
     *                   see #noteOn
     *                   see #allNotesOff
     *                   see #allSoundOff
     */
    void noteOff(int noteNumber, int velocity);

    /**
     * Turns the specified note off.
     *
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     *                   <p>
     *                   see #noteOff(int, int)
     */
    void noteOff(int noteNumber);

    /**
     * Reacts to a change in the specified note's key pressure.
     * Polyphonic key pressure
     * allows a keyboard player to press multiple keys simultaneously, each
     * with a different amount of pressure.  The pressure, if not ignored,
     * is typically used to vary such features as the volume, brightness,
     * or vibrato of the note.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support this MIDI message. In order
     * to verify that <code>setPolyPressure</code>
     * was successful, use <code>getPolyPressure</code>.
     *
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @param pressure   value for the specified key, from 0 to 127 (127 =
     *                   maximum pressure)
     *                   <p>
     *                   see #getPolyPressure(int)
     */
    void setPolyPressure(int noteNumber, int pressure);

    /**
     * Obtains the pressure with which the specified key is being depressed.
     *
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     *                   <p>
     *                   If the device does not support setting poly pressure,
     *                   this method always returns 0. Calling
     *                   <code>setPolyPressure</code> will have no effect then.
     * @return the amount of pressure for that note, from 0 to 127
     * (127 = maximum pressure)
     * <p>
     * see #setPolyPressure(int, int)
     */
    int getPolyPressure(int noteNumber);

    /**
     * Obtains the channel's keyboard pressure.
     * If the device does not support setting channel pressure,
     * this method always returns 0. Calling
     * <code>setChannelPressure</code> will have no effect then.
     *
     * @return the amount of pressure for that note,
     * from 0 to 127 (127 = maximum pressure)
     * <p>
     * see #setChannelPressure(int)
     */
    int getChannelPressure();

    /**
     * Reacts to a change in the keyboard pressure.  Channel
     * pressure indicates how hard the keyboard player is depressing
     * the entire keyboard.  This can be the maximum or
     * average of the per-key pressure-sensor values, as set by
     * <code>setPolyPressure</code>.  More commonly, it is a measurement of
     * a single sensor on a device that doesn't implement polyphonic key
     * pressure.  Pressure can be used to control various aspects of the sound,
     * as described under  #setPolyPressure(int, int) setPolyPressure.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support this MIDI message. In order
     * to verify that <code>setChannelPressure</code>
     * was successful, use <code>getChannelPressure</code>.
     *
     * @param pressure the pressure with which the keyboard is being depressed,
     *                 from 0 to 127 (127 = maximum pressure)
     *                 see #setPolyPressure(int, int)
     *                 see #getChannelPressure
     */
    void setChannelPressure(int pressure);

    /**
     * Reacts to a change in the specified controller's value.  A controller
     * is some control other than a keyboard key, such as a
     * switch, slider, pedal, wheel, or breath-pressure sensor.
     * The MIDI 1.0 Specification provides standard numbers for typical
     * controllers on MIDI devices, and describes the intended effect
     * for some of the controllers.
     * The way in which an
     * <code>Instrument</code> reacts to a controller change may be
     * specific to the <code>Instrument</code>.
     * <p>
     * The MIDI 1.0 Specification defines both 7-bit controllers
     * and 14-bit controllers.  Continuous controllers, such
     * as wheels and sliders, typically have 14 bits (two MIDI bytes),
     * while discrete controllers, such as switches, typically have 7 bits
     * (one MIDI byte).  Refer to the specification to see the
     * expected resolution for each type of control.
     * <p>
     * Controllers 64 through 95 (0x40 - 0x5F) allow 7-bit precision.
     * The value of a 7-bit controller is set completely by the
     * <code>value</code> argument.  An additional set of controllers
     * provide 14-bit precision by using two controller numbers, one
     * for the most significant 7 bits and another for the least significant
     * 7 bits.  Controller numbers 0 through 31 (0x00 - 0x1F) control the
     * most significant 7 bits of 14-bit controllers; controller numbers
     * 32 through 63 (0x20 - 0x3F) control the least significant 7 bits of
     * these controllers.  For example, controller number 7 (0x07) controls
     * the upper 7 bits of the channel volume controller, and controller
     * number 39 (0x27) controls the lower 7 bits.
     * The value of a 14-bit controller is determined
     * by the interaction of the two halves.  When the most significant 7 bits
     * of a controller are set (using controller numbers 0 through 31), the
     * lower 7 bits are automatically set to 0.  The corresponding controller
     * number for the lower 7 bits may then be used to further modulate the
     * controller value.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support a specific controller message. In order
     * to verify that a call to <code>controlChange</code>
     * was successful, use <code>getController</code>.
     *
     * @param controller the controller number (0 to 127; see the MIDI
     *                   1.0 Specification for the interpretation)
     * @param value      the value to which the specified controller is changed (0 to 127)
     *                   <p>
     *                   see #getController(int)
     */
    void controlChange(int controller, int value);

    /**
     * Obtains the current value of the specified controller.  The return
     * value is represented with 7 bits. For 14-bit controllers, the MSB and
     * LSB controller value needs to be obtained separately. For example,
     * the 14-bit value of the volume controller can be calculated by
     * multiplying the value of controller 7 (0x07, channel volume MSB)
     * with 128 and adding the
     * value of controller 39 (0x27, channel volume LSB).
     * <p>
     * If the device does not support setting a specific controller,
     * this method returns 0 for that controller.
     * Calling <code>controlChange</code> will have no effect then.
     *
     * @param controller the number of the controller whose value is desired.
     *                   The allowed range is 0-127; see the MIDI
     *                   1.0 Specification for the interpretation.
     * @return the current value of the specified controller (0 to 127)
     * <p>
     * see #controlChange(int, int)
     */
    int getController(int controller);

    void instrumentChange(Instrument instrument);

    /**
     * Obtains the upward or downward pitch offset for this channel.
     * If the device does not support setting pitch bend,
     * this method always returns 8192. Calling
     * <code>setPitchBend</code> will have no effect then.
     *
     * @return bend amount, as a nonnegative 14-bit value (8192 = no bend)
     * <p>
     * see #setPitchBend(int)
     */
    int getPitchBend();

    /**
     * Changes the pitch offset for all notes on this channel.
     * This affects all currently sounding notes as well as subsequent ones.
     * (For pitch bend to cease, the value needs to be reset to the
     * center position.)
     * <p> The MIDI specification
     * stipulates that pitch bend be a 14-bit value, where zero
     * is maximum downward bend, 16383 is maximum upward bend, and
     * 8192 is the center (no pitch bend).  The actual
     * amount of pitch change is not specified; it can be changed by
     * a pitch-bend sensitivity setting.  However, the General MIDI
     * specification says that the default range should be two semitones
     * up and down from center.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support this MIDI message. In order
     * to verify that <code>setPitchBend</code>
     * was successful, use <code>getPitchBend</code>.
     *
     * @param bend the amount of pitch change, as a nonnegative 14-bit value
     *             (8192 = no bend)
     *             <p>
     *             see #getPitchBend
     */
    void setPitchBend(int bend);

    /**
     * Resets all the implemented controllers to their default values.
     * <p>
     * see #controlChange(int, int)
     */
    void resetAllControllers();

    /**
     * Turns off all notes that are currently sounding on this channel.
     * The notes might not die away instantaneously; their decay
     * rate is determined by the internals of the <code>Instrument</code>.
     * If the Hold Pedal controller (see
     * #controlChange(int, int) controlChange)
     * is down, the effect of this method is deferred until the pedal is
     * released.
     * <p>
     * see #allSoundOff
     * see #noteOff(int)
     */
    void allNotesOff();

    /**
     * Immediately turns off all sounding notes on this channel, ignoring the
     * state of the Hold Pedal and the internal decay rate of the current
     * <code>Instrument</code>.
     * <p>
     * see #allNotesOff
     */
    void allSoundOff();

    /**
     * Obtains the current mono/poly mode.
     * Synthesizers that do not allow changing mono/poly mode
     * will always return the same value, regardless
     * of calls to <code>setMono</code>.
     *
     * @return <code>true</code> if mono mode is on, otherwise
     * <code>false</code> (meaning poly mode is on).
     * <p>
     * see #setMono(boolean)
     */
    boolean getMono();

    /**
     * Turns mono mode on or off.  In mono mode, the channel synthesizes
     * only one note at a time.  In poly mode (identical to mono mode off),
     * the channel can synthesize multiple notes simultaneously.
     * The default is mono off (poly mode on).
     * <p>
     * "Mono" is short for the word "monophonic," which in this context
     * is opposed to the word "polyphonic" and refers to a single synthesizer
     * voice per MIDI channel.  It
     * has nothing to do with how many audio channels there might be
     * (as in "monophonic" versus "stereophonic" recordings).
     * <p>
     * It is possible that the underlying synthesizer
     * does not support mono mode. In order
     * to verify that a call to <code>setMono</code>
     * was successful, use <code>getMono</code>.
     *
     * @param on <code>true</code> to turn mono mode on, <code>false</code> to
     *           turn it off (which means turning poly mode on).
     *           <p>
     *           see #getMono
     *           see VoiceStatus
     */
    void setMono(boolean on);

    /**
     * Obtains the current omni mode.
     * Synthesizers that do not allow changing the omni mode
     * will always return the same value, regardless
     * of calls to <code>setOmni</code>.
     *
     * @return <code>true</code> if omni mode is on, otherwise
     * <code>false</code> (meaning omni mode is off).
     * <p>
     * see #setOmni(boolean)
     */
    boolean getOmni();

    /**
     * Turns omni mode on or off.  In omni mode, the channel responds
     * to messages sent on all channels.  When omni is off, the channel
     * responds only to messages sent on its channel number.
     * The default is omni off.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support omni mode. In order
     * to verify that <code>setOmni</code>
     * was successful, use <code>getOmni</code>.
     *
     * @param on <code>true</code> to turn omni mode on, <code>false</code> to
     *           turn it off.
     *           <p>
     *           see #getOmni
     *           see VoiceStatus
     */
    void setOmni(boolean on);

    /**
     * Obtains the current mute state for this channel.
     * If the underlying synthesizer does not support
     * muting this channel, this method always returns
     * <code>false</code>.
     *
     * @return <code>true</code> the channel is muted,
     * or <code>false</code> if not
     * <p>
     * see #setMute(boolean)
     */
    boolean getMute();

    /**
     * Sets the mute state for this channel. A value of
     * <code>true</code> means the channel is to be muted, <code>false</code>
     * means the channel can sound (if other channels are not soloed).
     * <p>
     * Unlike  #allSoundOff(), this method
     * applies to only a specific channel, not to all channels.  Further, it
     * silences not only currently sounding notes, but also subsequently
     * received notes.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support muting channels. In order
     * to verify that a call to <code>setMute</code>
     * was successful, use <code>getMute</code>.
     *
     * @param mute the new mute state
     *             <p>
     *             see #getMute
     *             see #setSolo(boolean)
     */
    void setMute(boolean mute);

    /**
     * Obtains the current solo state for this channel.
     * If the underlying synthesizer does not support
     * solo on this channel, this method always returns
     * <code>false</code>.
     *
     * @return <code>true</code> the channel is solo,
     * or <code>false</code> if not
     * <p>
     * see #setSolo(boolean)
     */
    boolean getSolo();

    /**
     * Sets the solo state for this channel.
     * If <code>solo</code> is <code>true</code> only this channel
     * and other soloed channels will sound. If <code>solo</code>
     * is <code>false</code> then only other soloed channels will
     * sound, unless no channels are soloed, in which case all
     * unmuted channels will sound.
     * <p>
     * It is possible that the underlying synthesizer
     * does not support solo channels. In order
     * to verify that a call to <code>setSolo</code>
     * was successful, use <code>getSolo</code>.
     *
     * @param soloState new solo state for the channel
     *                  see #getSolo()
     */
    void setSolo(boolean soloState);
}
