package caramel.api.sound.decoder;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class OggDecoder extends AudioDecoder {
    public OggDecoder(final String extension) {
        super(extension);
    }

    @Override
    public SoundFormat decode(final String path) {
        stackPush();
        final IntBuffer channels = stackMallocInt(1);
        stackPush();
        final IntBuffer sampleRate = stackMallocInt(1);
        final ShortBuffer rawAudio = stb_vorbis_decode_filename(path, channels, sampleRate);

        if (rawAudio == null) {
            logError(path);
            stackPop();
            stackPop();
            return null;
        }

        final int c = channels.get();
        final int rate = sampleRate.get();

        stackPop();
        stackPop();

        int format = -1;
        if (c == 1) {
            format = AL_FORMAT_MONO16;
        } else if (c == 2) {
            format = AL_FORMAT_STEREO16;
        }

        return new SoundFormat(format, rate, rawAudio);
    }
}
