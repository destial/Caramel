package caramel.api.sound.decoder;

import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;

public class SoundFormat {
    public int channels;
    public int frequency;
    public Buffer buffer;

    public void close() {
        if (buffer != null) {
            MemoryUtil.memFree(buffer);
            buffer = null;
        }
    }
}
