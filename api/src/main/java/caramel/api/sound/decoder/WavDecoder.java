package caramel.api.sound.decoder;

import caramel.api.sound.decoder.utils.StreamUtils;
import caramel.api.sound.decoder.utils.WavInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public final class WavDecoder extends AudioDecoder {

    public WavDecoder(String extension) {
        super(extension);
    }

    @Override
    public SoundFormat decode(String path) {
        try (WavInputStream inputStream = new WavInputStream(new File(path))) {
            if (inputStream.open()) {
                return new SoundFormat(inputStream.channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, inputStream.sampleRate, ByteBuffer.wrap(StreamUtils.copyStreamToByteArray(inputStream, inputStream.dataRemaining)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            logError(path);
        }
        return null;
    }
}
