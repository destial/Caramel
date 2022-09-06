package caramel.api.sound;

import caramel.api.debug.Debug;
import caramel.api.sound.decoder.AudioDecoder;
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

    private SoundSource(final String path) {
        this.path = path;
        sounds = ConcurrentHashMap.newKeySet();
    }

    public boolean build() {
        if (bufferId != -1) return true;
        final String fileFormat = path.substring(path.lastIndexOf(".")).toLowerCase();
        final AudioDecoder decoder = AudioDecoder.getDecoder(fileFormat);
        if (decoder == null) {
            Debug.logError("Unsupported audio file type: " + fileFormat);
            return false;
        }
        final SoundFormat format = decoder.decode(path);
        if (format == null) {
            Debug.logError("Unable to read audio file: " + path);
            return false;
        }
        bufferId = alGenBuffers();
        if (format.getBuffer() instanceof ByteBuffer) {
            alBufferData(bufferId, format.getChannels(), (ByteBuffer) format.getBuffer(), format.getFrequency());
        } else if (format.getBuffer() instanceof ShortBuffer) {
            alBufferData(bufferId, format.getChannels(), (ShortBuffer) format.getBuffer(), format.getFrequency());
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
        final Sound sound = new Sound(this);
        sounds.add(sound);
        return sound;
    }

    public void invalidate() {
        for (final Sound sound : sounds) {
            sound.invalidate();
        }
        sounds.clear();
        alDeleteBuffers(bufferId);
        bufferId = 0;

        SOURCES.remove(path);
    }

    private final static Map<String, SoundSource> SOURCES = new ConcurrentHashMap<>();

    public static void invalidateAll() {
        for (final SoundSource source : SOURCES.values()) {
            for (final Sound sound : source.sounds) {
                sound.invalidate();
            }
            source.sounds.clear();
            alDeleteBuffers(source.bufferId);
            source.bufferId = 0;
        }
        SOURCES.clear();
    }

    public static SoundSource getSource(final String path) {
        SoundSource source = SOURCES.get(path);
        if (source == null) {
            source = new SoundSource(path);
            source.build();
            SOURCES.put(path, source);
        }
        return source;
    }

    public void invalidate(final Sound sound) {
        if (sounds.remove(sound)) {
            sound.invalidate();
        }
    }
}
