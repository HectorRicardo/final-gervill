/*
 * Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;


/**
 * An audio input stream is an input stream with a specified audio format and
 * length.  The length is expressed in sample frames, not bytes.
 * Several methods are provided for reading a certain number of bytes from
 * the stream, or an unspecified number of bytes.
 * The audio input stream keeps track  of the last byte that was read.
 * You can skip over an arbitrary number of bytes to get to a later position
 * for reading. An audio input stream may support marks.  When you set a mark,
 * the current position is remembered so that you can return to it later.
 * <p>
 * The <code>AudioSystem</code> class includes many methods that manipulate
 * <code>AudioInputStream</code> objects.
 * For example, the methods let you:
 * <ul>
 * <li> obtain an
 * audio input stream from an external audio file, stream, or URL
 * <li> write an external file from an audio input stream
 * <li> convert an audio input stream to a different audio format
 * </ul>
 *
 * @author David Rivas
 * @author Kara Kytle
 * @author Florian Bomers
 * <p>
 * see AudioSystem
 * see Clip#open(AudioInputStream) Clip.open(AudioInputStream)
 * @since 1.3
 */
public class AudioInputStream {

    public static final int NOT_SPECIFIED = -1;

    /**
     * The <code>InputStream</code> from which this <code>AudioInputStream</code>
     * object was constructed.
     */
    private final InputStream stream;

    /**
     * The format of the audio data contained in the stream.
     */
    private final AudioFormat format;

    /**
     * This stream's length, in sample frames.
     */
    private final long frameLength;

    /**
     * The size of each frame, in bytes.
     */
    private final int frameSize;

    /**
     * The current position in this stream, in sample frames (zero-based).
     */
    private long framePos;

    /**
     * The position where a mark was set.
     */
    private long markpos;

    /**
     * When the underlying stream could only return
     * a non-integral number of frames, store
     * the remainder in a temporary buffer
     */
    private byte[] pushBackBuffer = null;

    /**
     * number of valid bytes in the pushBackBuffer
     */
    private int pushBackLen = 0;

    /**
     * MarkBuffer at mark position
     */
    private byte[] markPushBackBuffer = null;

    /**
     * number of valid bytes in the markPushBackBuffer
     */
    private int markPushBackLen = 0;


    /**
     * Constructs an audio input stream that has the requested format and length in sample frames,
     * using audio data from the specified input stream.
     *
     * @param stream the stream on which this <code>AudioInputStream</code>
     *               object is based
     * @param format the format of this stream's audio data
     * @param length the length in sample frames of the data in this stream
     */
    public AudioInputStream(InputStream stream, AudioFormat format, long length) {

        super();

        this.format = format;
        this.frameLength = length;

        // any frameSize that is not well-defined will
        // cause that this stream will be read in bytes
        this.frameSize = Math.max(format.getFrameSize(), 1);

        this.stream = stream;
        framePos = 0;
        markpos = 0;
    }


    /**
     * Obtains the audio format of the sound data in this audio input stream.
     *
     * @return an audio format object describing this stream's format
     */
    public AudioFormat getFormat() {
        return format;
    }


    /**
     * Reads some number of bytes from the audio input stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer. This method blocks until input data is
     * available, the end of the stream is detected, or an exception is thrown.
     * <p>This method will always read an integral number of frames.
     * If the length of the array is not an integral number
     * of frames, a maximum of <code>b.length - (b.length % frameSize)
     * </code> bytes will be read.
     *
     * @param b the buffer into which the data is read
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached
     * throws IOException if an input or output error occurs
     * see #read(byte[], int, int)
     * see #read()
     * see #available
     */
    public int read(byte[] b) throws IOException {
        return read(b, b.length);
    }


    /**
     * Reads up to a specified maximum number of bytes of data from the audio
     * stream, putting them into the given byte array.
     * <p>This method will always read an integral number of frames.
     * If <code>len</code> does not specify an integral number
     * of frames, a maximum of <code>len - (len % frameSize)
     * </code> bytes will be read.
     *
     * @param b   the buffer into which the data is read
     * @param len the maximum number of bytes to read
     * @return the total number of bytes read into the buffer, or -1 if there
     * is no more data because the end of the stream has been reached
     * throws IOException if an input or output error occurs
     * see #read(byte[])
     * see #read()
     * see #skip
     * see #available
     */
    public int read(byte[] b, int len) throws IOException {

        // make sure we don't read fractions of a frame.
        if ((len % frameSize) != 0) {
            len -= (len % frameSize);
            if (len == 0) {
                return 0;
            }
        }

        if (frameLength != NOT_SPECIFIED) {
            if (framePos >= frameLength) {
                return -1;
            } else {

                // don't try to read beyond our own set length in frames
                if ((len / frameSize) > (frameLength - framePos)) {
                    len = (int) (frameLength - framePos) * frameSize;
                }
            }
        }

        int bytesRead = 0;
        int thisOff = 0;

        // if we've bytes left from last call to read(),
        // use them first
        if (pushBackLen > 0 && len >= pushBackLen) {
            System.arraycopy(pushBackBuffer, 0,
                    b, 0, pushBackLen);
            thisOff += pushBackLen;
            len -= pushBackLen;
            bytesRead += pushBackLen;
            pushBackLen = 0;
        }

        int thisBytesRead = stream.read(b, thisOff, len);
        if (thisBytesRead == -1) {
            return -1;
        }
        if (thisBytesRead > 0) {
            bytesRead += thisBytesRead;
        }
        if (bytesRead > 0) {
            pushBackLen = bytesRead % frameSize;
            if (pushBackLen > 0) {
                // copy everything we got from the beginning of the frame
                // to our pushback buffer
                if (pushBackBuffer == null) {
                    pushBackBuffer = new byte[frameSize];
                }
                System.arraycopy(b, bytesRead - pushBackLen,
                        pushBackBuffer, 0, pushBackLen);
                bytesRead -= pushBackLen;
            }
            // make sure to update our framePos
            framePos += bytesRead / frameSize;
        }
        return bytesRead;
    }


    /**
     * Skips over and discards a specified number of bytes from this
     * audio input stream.
     *
     * @param n the requested number of bytes to be skipped
     * @return the actual number of bytes skipped
     * throws IOException if an input or output error occurs
     * see #read
     * see #available
     */
    public long skip(long n) throws IOException {

        // make sure not to skip fractional frames
        if ((n % frameSize) != 0) {
            n -= (n % frameSize);
        }

        if (frameLength != NOT_SPECIFIED) {
            // don't skip more than our set length in frames.
            if ((n / frameSize) > (frameLength - framePos)) {
                n = (frameLength - framePos) * frameSize;
            }
        }
        long temp = stream.skip(n);

        // if no error, update our position.
        if (temp % frameSize != 0) {

            // Throw an IOException if we've skipped a fractional number of frames
            throw new IOException("Could not skip an integer number of frames.");
        }
        framePos += temp / frameSize;
        return temp;

    }


    /**
     * Closes this audio input stream and releases any system resources associated
     * with the stream.
     * throws IOException if an input or output error occurs
     */
    public void close() throws IOException {
        stream.close();
    }


    /**
     * Marks the current position in this audio input stream.
     *
     * @param readlimit the maximum number of bytes that can be read before
     *                  the mark position becomes invalid.
     *                  see #reset
     *                  see #markSupported
     */

    public void mark(int readlimit) {

        stream.mark(readlimit);
        if (markSupported()) {
            markpos = framePos;
            // remember the pushback buffer
            markPushBackLen = pushBackLen;
            if (markPushBackLen > 0) {
                if (markPushBackBuffer == null) {
                    markPushBackBuffer = new byte[frameSize];
                }
                System.arraycopy(pushBackBuffer, 0, markPushBackBuffer, 0, markPushBackLen);
            }
        }
    }


    /**
     * Repositions this audio input stream to the position it had at the time its
     * <code>mark</code> method was last invoked.
     * throws IOException if an input or output error occurs.
     * see #mark
     * see #markSupported
     */
    public void reset() throws IOException {

        stream.reset();
        framePos = markpos;
        // re-create the pushback buffer
        pushBackLen = markPushBackLen;
        if (pushBackLen > 0) {
            if (pushBackBuffer == null) {
                pushBackBuffer = new byte[frameSize - 1];
            }
            System.arraycopy(markPushBackBuffer, 0, pushBackBuffer, 0, pushBackLen);
        }
    }


    /**
     * Tests whether this audio input stream supports the <code>mark</code> and
     * <code>reset</code> methods.
     *
     * @return <code>true</code> if this stream supports the <code>mark</code>
     * and <code>reset</code> methods; <code>false</code> otherwise
     * see #mark
     * see #reset
     */
    public boolean markSupported() {

        return stream.markSupported();
    }


}
