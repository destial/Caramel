package caramel.api.sound;

import caramel.api.debug.Debug;
import caramel.api.sound.decoder.MP3Decoder;
import caramel.api.sound.decoder.OggDecoder;
import caramel.api.sound.decoder.SoundFormat;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;

public final class SoundSource {
    private final String path;
    private transient final Set<Sound> sounds;
    transient int bufferId = -1;

    private SoundSource(String path) {
        this.path = path;
        sounds = ConcurrentHashMap.newKeySet();
    }

    public boolean build() {
        if (bufferId != -1) return true;
        caramel.api.sound.decoder.Decoder decoder = null;
        if (path.toLowerCase().endsWith(".ogg")) {
            decoder = new OggDecoder();
        } else if (path.toLowerCase().endsWith(".mp3")) {
            decoder = new MP3Decoder();
        }

        if (decoder == null) {
            Debug.logError("Unsupported audio file type: " + path.substring(path.lastIndexOf(".")));
            return false;
        }

        SoundFormat format = decoder.decode(path);
        if (format == null) return false;
        bufferId = alGenBuffers();
        if (format.buffer instanceof ByteBuffer) {
            alBufferData(bufferId, format.channels, (ByteBuffer) format.buffer, format.frequency);
        } else if (format.buffer instanceof ShortBuffer) {
            alBufferData(bufferId, format.channels, (ShortBuffer) format.buffer, format.frequency);
        }
        format.close();

        return true;
    }

    public String getPath() {
        return path;
    }

    public Sound createSound() {
        if (bufferId == -1) {
            if (!build()) {
                return null;
            }
        }
        Sound sound = new Sound(this);
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

    public void invalidate(Sound sound) {
        if (sounds.remove(sound)) {
            sound.invalidate();
        }
    }
}
