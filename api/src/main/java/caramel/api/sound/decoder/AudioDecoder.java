package caramel.api.sound.decoder;

import caramel.api.debug.Debug;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AudioDecoder {
    private final String extension;
    public AudioDecoder(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    protected void logError(String path) {
        Debug.logError("Unable to load " + extension + " file: " + path);
    }

    public abstract SoundFormat decode(String path);

    private static final Set<AudioDecoder> DECODERS = new HashSet<>();

    public static void load() {
        DECODERS.add(new Mp3Decoder(".mp3"));
        DECODERS.add(new WavDecoder(".wav"));
        DECODERS.add(new OggDecoder(".ogg"));

        for (AudioDecoder decoder : DECODERS) {
            Debug.console("Loaded audio decoder for " + decoder.getExtension());
        }
    }

    public static AudioDecoder getDecoder(String format) {
        return DECODERS.stream().filter(d -> d.getExtension().equalsIgnoreCase(format)).findFirst().orElse(null);
    }
}