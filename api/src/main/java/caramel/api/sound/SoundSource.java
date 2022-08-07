package caramel.api.sound;

import caramel.api.debug.Debug;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public final class SoundSource {
    private final String path;
    private transient final Set<Sound> sounds;
    transient int bufferId = -1;

    private SoundSource(String path) {
        this.path = path;
        sounds = ConcurrentHashMap.newKeySet();
    }

    public boolean build() {
        stackPush();
        IntBuffer channels = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRate = stackMallocInt(1);

        ShortBuffer rawAudio = stb_vorbis_decode_filename(path, channels, sampleRate);

        if (rawAudio == null) {
            Debug.logError("Unable to load sound: " + path);
            stackPop();
            stackPop();
            return false;
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

        bufferId = alGenBuffers();
        alBufferData(bufferId, format, rawAudio, rate);

        free(rawAudio);
        return true;
    }

    public String getPath() {
        return path;
    }

    public Sound createSound(boolean loop) {
        if (bufferId == -1) {
            if (!build()) {
                return null;
            }
        }
        Sound sound = new Sound(this);
        sound.setLoop(loop);
        sounds.add(sound);
        return sound;
    }

    public void invalidate() {
        for (Sound sound : sounds) {
            sound.invalidate();
        }
        sounds.clear();
        alDeleteBuffers(bufferId);
        bufferId = 0;

        SOURCES.remove(path);
    }

    private final static Map<String, SoundSource> SOURCES = new ConcurrentHashMap<>();

    public static void invalidateAll() {
        for (SoundSource source : SOURCES.values()) {
            for (Sound sound : source.sounds) {
                sound.invalidate();
            }
            source.sounds.clear();
            alDeleteBuffers(source.bufferId);
            source.bufferId = 0;
        }
        SOURCES.clear();
    }

    public static SoundSource getSource(String path) {
        SoundSource source = SOURCES.get(path);
        if (source == null) {
            source = new SoundSource(path);
            source.build();
            SOURCES.put(path, source);
        }
        return source;
    }
}
