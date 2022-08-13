package caramel.api.sound.decoder;

import caramel.api.debug.Debug;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class OggDecoder implements Decoder {
    @Override
    public SoundFormat decode(String path) {
        stackPush();
        IntBuffer channels = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRate = stackMallocInt(1);
        ShortBuffer rawAudio = stb_vorbis_decode_filename(path, channels, sampleRate);

        if (rawAudio == null) {
            Debug.logError("Unable to load vorbis ogg sound: " + path);
            stackPop();
            stackPop();
            return null;
        }

        int c = channels.get();
        int rate = sampleRate.get();

        stackPop();
        stackPop();

        int format = -1;
        if (c == 1) {
            format = AL_FORMAT_MONO16;
        } else if (c == 2) {
            format = AL_FORMAT_STEREO16;
        }

        SoundFormat soundFormat = new SoundFormat();
        soundFormat.buffer = rawAudio;
        soundFormat.channels = format;
        soundFormat.frequency = rate;

        return soundFormat;
    }
}
