package own.impl;

import gervill.javax.sound.sampled.*;

import javax.sound.sampled.LineUnavailableException;

public class SourceDataLineImpl implements SourceDataLine {

    private final javax.sound.sampled.SourceDataLine realLine;

    public SourceDataLineImpl() {
        try {
            realLine = javax.sound.sampled.AudioSystem.getSourceDataLine(null);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void open(AudioFormat format, int bufferSize) {
        try {
            realLine.open(new javax.sound.sampled.AudioFormat(format.getSampleRate(), 16, format.getChannels(), true, false), bufferSize);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public int write(byte[] b, int off, int len) {
        return realLine.write(b, off, len);
    }

    @Override
    public void flush() {
        realLine.flush();
    }

    @Override
    public void start() {
        realLine.start();
    }

    @Override
    public void stop() {
        realLine.stop();
    }

    @Override
    public boolean isActive() {
        return realLine.isActive();
    }

    @Override
    public AudioFormat getFormat() {
        return null;
    }

    @Override
    public int getBufferSize() {
        return realLine.getBufferSize();
    }

    @Override
    public int available() {
        return realLine.available();
    }

    @Override
    public void close() {
        realLine.close();
    }

    @Override
    public boolean isOpen() {
        return realLine.isOpen();
    }

}