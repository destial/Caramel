package caramel.api.sound.decoder;

import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;

public final class SoundFormat {
    private final int channels;
    private final int frequency;
    private Buffer buffer;

    public SoundFormat(final int channels, final int frequency, final Buffer buffer) {
        this.channels = channels;
        this.frequency = frequency;
        this.buffer = buffer;
    }

    public void close() {
        if (buffer != null) {
            MemoryUtil.memFree(buffer);
            buffer = null;
        }
    }

    public int getChannels() {
        return channels;
    }

    public int getFrequency() {
        return frequency;
    }

    public Buffer getBuffer() {
        return buffer;
    }
}
