package own.impl;

import gervill.javax.sound.sampled.*;

import javax.sound.sampled.LineUnavailableException;

public class SourceDataLineImpl implements SourceDataLine {

    private final javax.sound.sampled.SourceDataLine realLine;

    public SourceDataLineImpl(AudioFormat format) {
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
    public void open(AudioFormat format) {
    }

    @Override
    public int write(byte[] b, int off, int len) {
        return realLine.write(b, off, len);
    }

    @Override
    public void drain() {
        realLine.drain();
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
    public boolean isRunning() {
        return realLine.isRunning();
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
    public int getFramePosition() {
        return realLine.getFramePosition();
    }

    @Override
    public long getLongFramePosition() {
        return realLine.getLongFramePosition();
    }

    @Override
    public long getMicrosecondPosition() {
        return realLine.getMicrosecondPosition();
    }

    @Override
    public float getLevel() {
        return realLine.getLevel();
    }

    @Override
    public Line.Info getLineInfo() {
        return null;
    }

    @Override
    public void open() {
        try {
            realLine.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void close() {
        realLine.close();
    }

    @Override
    public boolean isOpen() {
        return realLine.isOpen();
    }

    @Override
    public Control[] getControls() {
        return null;
    }

    @Override
    public boolean isControlSupported(Control.Type control) {
        return false;
    }

    @Override
    public Control getControl(Control.Type control) {
        return null;
    }

    @Override
    public void addLineListener(LineListener listener) {
    }

    @Override
    public void removeLineListener(LineListener listener) {
    }
}