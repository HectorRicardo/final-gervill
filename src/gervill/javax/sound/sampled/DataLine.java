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

package gervill.javax.sound.sampled;

import java.util.Arrays;

/**
 * <code>DataLine</code> adds media-related functionality to its
 * superinterface, <code> Line</code>.  This functionality includes
 * transport-control methods that start, stop, drain, and flush
 * the audio data that passes through the line.  A data line can also
 * report the current position, volume, and audio format of the media.
 * Data lines are used for output of audio by means of the
 * subinterfaces <code> SourceDataLine</code> or
 * <code> Clip</code>, which allow an application program to write data.  Similarly,
 * audio input is handled by the subinterface <code> TargetDataLine</code>,
 * which allows data to be read.
 * <p>
 * A data line has an internal buffer in which
 * the incoming or outgoing audio data is queued.  The
 * <code> #drain()</code> method blocks until this internal buffer
 * becomes empty, usually because all queued data has been processed.  The
 * <code> #flush()</code> method discards any available queued data
 * from the internal buffer.
 * <p>
 * A data line produces <code> LineEvent.Type#START START</code> and
 * <code> LineEvent.Type#STOP STOP</code> events whenever
 * it begins or ceases active presentation or capture of data.  These events
 * can be generated in response to specific requests, or as a result of
 * less direct state changes.  For example, if <code> #start()</code> is called
 * on an inactive data line, and data is available for capture or playback, a
 * <code>START</code> event will be generated shortly, when data playback
 * or capture actually begins.  Or, if the flow of data to an active data
 * line is constricted so that a gap occurs in the presentation of data,
 * a <code>STOP</code> event is generated.
 * <p>
 * Mixers often support synchronized control of multiple data lines.
 * Synchronization can be established through the Mixer interface's
 * <code> Mixer#synchronize synchronize</code> method.
 * See the description of the <code> Mixer Mixer</code> interface
 * for a more complete description.
 *
 * @author Kara Kytle
 * see LineEvent
 * @since 1.3
 */
public interface DataLine extends Line {


    /**
     * Flushes queued data from the line.  The flushed data is discarded.
     * In some cases, not all queued data can be discarded.  For example, a
     * mixer can flush data from the buffer for a specific input line, but any
     * unplayed data already in the output buffer (the result of the mix) will
     * still be played.  You can invoke this method after pausing a line (the
     * normal case) if you want to skip the "stale" data when you restart
     * playback or capture. (It is legal to flush a line that is not stopped,
     * but doing so on an active line is likely to cause a discontinuity in the
     * data, resulting in a perceptible click.)
     *
     * see #stop()
     * see #drain()
     */
    public void flush();

    /**
     * Allows a line to engage in data I/O.  If invoked on a line
     * that is already running, this method does nothing.  Unless the data in
     * the buffer has been flushed, the line resumes I/O starting
     * with the first frame that was unprocessed at the time the line was
     * stopped. When audio capture or playback starts, a
     * <code> LineEvent.Type#START START</code> event is generated.
     *
     * see #stop()
     * see #isRunning()
     * see LineEvent
     */
    public void start();

    /**
     * Stops the line.  A stopped line should cease I/O activity.
     * If the line is open and running, however, it should retain the resources required
     * to resume activity.  A stopped line should retain any audio data in its buffer
     * instead of discarding it, so that upon resumption the I/O can continue where it left off,
     * if possible.  (This doesn't guarantee that there will never be discontinuities beyond the
     * current buffer, of course; if the stopped condition continues
     * for too long, input or output samples might be dropped.)  If desired, the retained data can be
     * discarded by invoking the <code>flush</code> method.
     * When audio capture or playback stops, a <code> LineEvent.Type#STOP STOP</code> event is generated.
     *
     * see #start()
     * see #isRunning()
     * see #flush()
     * see LineEvent
     */
    public void stop();

    /**
     * Indicates whether the line is engaging in active I/O (such as playback
     * or capture).  When an inactive line becomes active, it sends a
     * <code> LineEvent.Type#START START</code> event to its listeners.  Similarly, when
     * an active line becomes inactive, it sends a
     * <code> LineEvent.Type#STOP STOP</code> event.
     * @return <code>true</code> if the line is actively capturing or rendering
     * sound, otherwise <code>false</code>
     * see #isOpen
     * see #addLineListener
     * see #removeLineListener
     * see LineEvent
     * see LineListener
     */
    public boolean isActive();

    /**
     * Obtains the current format (encoding, sample rate, number of channels,
     * etc.) of the data line's audio data.
     *
     * <p>If the line is not open and has never been opened, it returns
     * the default format. The default format is an implementation
     * specific audio format, or, if the <code>DataLine.Info</code>
     * object, which was used to retrieve this <code>DataLine</code>,
     * specifies at least one fully qualified audio format, the
     * last one will be used as the default format. Opening the
     * line with a specific audio format (e.g.
     *  SourceDataLine#open(AudioFormat)) will override the
     * default format.
     *
     * @return current audio data format
     * see AudioFormat
     */
    public AudioFormat getFormat();

    /**
     * Obtains the maximum number of bytes of data that will fit in the data line's
     * internal buffer.  For a source data line, this is the size of the buffer to
     * which data can be written.  For a target data line, it is the size of
     * the buffer from which data can be read.  Note that
     * the units used are bytes, but will always correspond to an integral
     * number of sample frames of audio data.
     *
     * @return the size of the buffer in bytes
     */
    public int getBufferSize();

    /**
     * Obtains the number of bytes of data currently available to the
     * application for processing in the data line's internal buffer.  For a
     * source data line, this is the amount of data that can be written to the
     * buffer without blocking.  For a target data line, this is the amount of data
     * available to be read by the application.  For a clip, this value is always
     * 0 because the audio data is loaded into the buffer when the clip is opened,
     * and persists without modification until the clip is closed.
     * <p>
     * Note that the units used are bytes, but will always
     * correspond to an integral number of sample frames of audio data.
     * <p>
     * An application is guaranteed that a read or
     * write operation of up to the number of bytes returned from
     * <code>available()</code> will not block; however, there is no guarantee
     * that attempts to read or write more data will block.
     *
     * @return the amount of data available, in bytes
     */
    public int available();


} // interface DataLine
