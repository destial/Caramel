package caramel.api.sound.decoder;

import caramel.api.debug.Debug;
import caramel.api.sound.decoder.utils.Bitstream;
import caramel.api.sound.decoder.utils.FrameDecoder;
import caramel.api.sound.decoder.utils.Header;
import caramel.api.sound.decoder.utils.LayerIDecoder;
import caramel.api.sound.decoder.utils.LayerIIDecoder;
import caramel.api.sound.decoder.utils.LayerIIIDecoder;
import caramel.api.sound.decoder.utils.OutputBuffer;
import caramel.api.sound.decoder.utils.OutputChannels;
import caramel.api.sound.decoder.utils.SynthesisFilter;
import caramel.api.sound.decoder.utils.exceptions.DecoderException;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public final class Mp3Decoder extends AudioDecoder {
    static public final int DECODER_ERROR = 0x200;
    static public final int UNKNOWN_ERROR = DECODER_ERROR;
    static public final int UNSUPPORTED_LAYER = DECODER_ERROR + 1;
    static public final int ILLEGAL_SUBBAND_ALLOCATION = DECODER_ERROR + 2;

    public Mp3Decoder(final String extension) {
        super(extension);
    }

    static private class Decode {
        private OutputBuffer output;
        private SynthesisFilter filter1;
        private SynthesisFilter filter2;

        private LayerIIIDecoder l3decoder;
        private LayerIIDecoder l2decoder;
        private LayerIDecoder l1decoder;

        private boolean initialized;

        public void decodeFrame(final Header header, final Bitstream stream) throws DecoderException {
            if (!initialized) initialize(header);
            int layer = header.layer();
            final FrameDecoder decoder = retrieveDecoder(header, stream, layer);
            decoder.decodeFrame();
        }

        public void setOutputBuffer(final OutputBuffer out) {
            output = out;
        }

        private FrameDecoder retrieveDecoder(final Header header, final Bitstream stream, final int layer) throws DecoderException {
            FrameDecoder decoder = null;

            switch (layer) {
                case 3:
                    if (l3decoder == null)
                        l3decoder = new LayerIIIDecoder(stream, header, filter1, filter2, output, OutputChannels.BOTH_CHANNELS);

                    decoder = l3decoder;
                    break;
                case 2:
                    if (l2decoder == null) {
                        l2decoder = new LayerIIDecoder();
                        l2decoder.create(stream, header, filter1, filter2, output, OutputChannels.BOTH_CHANNELS);
                    }
                    decoder = l2decoder;
                    break;
                case 1:
                    if (l1decoder == null) {
                        l1decoder = new LayerIDecoder();
                        l1decoder.create(stream, header, filter1, filter2, output, OutputChannels.BOTH_CHANNELS);
                    }
                    decoder = l1decoder;
                    break;
            }

            if (decoder == null) throw new DecoderException(UNSUPPORTED_LAYER, null);

            return decoder;
        }

        private void initialize(final Header header) {
            final float scalefactor = 32700.0f;
            final int mode = header.mode();
            header.layer();
            final int channels = mode == Header.SINGLE_CHANNEL ? 1 : 2;
            if (output == null) throw new RuntimeException("Output buffer was not set.");

            filter1 = new SynthesisFilter(0, scalefactor, null);
            if (channels == 2) filter2 = new SynthesisFilter(1, scalefactor, null);

            initialized = true;
        }
    }

    @Override
    public SoundFormat decode(final String path) {
        try (final InputStream file = Files.newInputStream(new File(path).toPath())) {
            final Bitstream bitstream = new Bitstream(file);
            Header header = bitstream.readFrame();
            if (header == null) {
                Debug.logError("Empty mp3 file: " + path);
                return null;
            }
            final int channels = header.mode()  == Header.SINGLE_CHANNEL ? 1 : 2;
            final int rate = header.getSampleRate();
            final OutputBuffer outputBuffer = new OutputBuffer(channels, false);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
            final Decode decoder = new Decode();
            decoder.setOutputBuffer(outputBuffer);
            while (true) {
                header = bitstream.readFrame();
                if (header == null) break;
                try {
                    decoder.decodeFrame(header, bitstream);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                bitstream.closeFrame();
                buffer.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
            }
            bitstream.close();

            final int bytes = buffer.size() - (buffer.size() % (channels > 1 ? 4 : 2));
            final ByteBuffer output = MemoryUtil.memAlloc(bytes);
            output.order(ByteOrder.nativeOrder());
            output.put(buffer.toByteArray(), 0, bytes);
            output.flip();

            return new SoundFormat(channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16, rate, output.asShortBuffer());
        } catch (Exception e) {
            logError(path);
            e.printStackTrace();
        }
        return null;
    }
}
