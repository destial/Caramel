package caramel.api.sound.decoder.utils;

import caramel.api.sound.decoder.utils.exceptions.DecoderException;

public interface FrameDecoder {
    void decodeFrame() throws DecoderException;
}
