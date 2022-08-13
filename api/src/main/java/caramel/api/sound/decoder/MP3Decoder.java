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
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public final class MP3Decoder implements Decoder {
    /**
     * The Bistream from which the MPEG audio frames are read.
     */
    // private Bitstream stream;
    /**
     * The Obuffer instance that will receive the decoded PCM samples.
     */
    private OutputBuffer output;

    /**
     * Synthesis filter for the left channel.
     */
    private SynthesisFilter filter1;

    /**
     * Sythesis filter for the right channel.
     */
    private SynthesisFilter filter2;

    private LayerIIIDecoder l3decoder;
    private LayerIIDecoder l2decoder;
    private LayerIDecoder l1decoder;

    private int outputFrequency;
    private int outputChannels;

    private boolean initialized;

    /**
     * Creates a new <code>Decoder</code> instance with default parameters.
     */

    public MP3Decoder () {
    }

    public OutputBuffer decodeFrame (Header header, Bitstream stream) throws DecoderException {
        if (!initialized) initialize(header);

        int layer = header.layer();

        FrameDecoder decoder = retrieveDecoder(header, stream, layer);

        decoder.decodeFrame();

        return output;
    }

    /**
     * Changes the output buffer. This will take effect the next time decodeFrame() is called.
     */
    public void setOutputBuffer (OutputBuffer out) {
        output = out;
    }

    public int getOutputFrequency () {
        return outputFrequency;
    }

    /**
     * Retrieves the number of channels of PCM samples output by this decoder. This usually corresponds to the number of channels
     * in the MPEG audio stream, although it may differ.
     *
     * @return The number of output channels in the decoded samples: 1 for mono, or 2 for stereo.
     *
     */
    public int getOutputChannels () {
        return outputChannels;
    }

    protected DecoderException newDecoderException (int errorcode) {
        return new DecoderException(errorcode, null);
    }

    protected DecoderException newDecoderException (int errorcode, Throwable throwable) {
        return new DecoderException(errorcode, throwable);
    }

    protected FrameDecoder retrieveDecoder (Header header, Bitstream stream, int layer) throws DecoderException {
        FrameDecoder decoder = null;

        // REVIEW: allow channel output selection type
        // (LEFT, RIGHT, BOTH, DOWNMIX)
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

        if (decoder == null) throw newDecoderException(UNSUPPORTED_LAYER, null);

        return decoder;
    }

    private void initialize (Header header) throws DecoderException {

        // REVIEW: allow customizable scale factor
        float scalefactor = 32700.0f;

        int mode = header.mode();
        header.layer();
        int channels = mode == Header.SINGLE_CHANNEL ? 1 : 2;

        // set up output buffer if not set up by client.
        if (output == null) throw new RuntimeException("Output buffer was not set.");

        filter1 = new SynthesisFilter(0, scalefactor, null);

        // REVIEW: allow mono output for stereo
        if (channels == 2) filter2 = new SynthesisFilter(1, scalefactor, null);

        outputChannels = channels;
        outputFrequency = header.frequency();

        initialized = true;
    }

    static public final int DECODER_ERROR = 0x200;

    static public final int UNKNOWN_ERROR = DECODER_ERROR;

    static public final int UNSUPPORTED_LAYER = DECODER_ERROR + 1;

    static public final int ILLEGAL_SUBBAND_ALLOCATION = DECODER_ERROR + 2;

    @Override
    public SoundFormat decode(String path) {
        try (InputStream file = new FileInputStream(new File(path))) {
            Bitstream bitstream = new Bitstream(file);
            Header header = bitstream.readFrame();
            if (header == null) {
                Debug.logError("Empty mp3 file: " + path);
                return null;
            }
            int channels = header.mode()  == Header.SINGLE_CHANNEL ? 1 : 2;
            int rate = header.getSampleRate();
            OutputBuffer outputBuffer = new OutputBuffer(channels, false);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(4096);
            setOutputBuffer(outputBuffer);

            // ByteBuffer buffer = MemoryUtil.memAlloc(4096);
            while (true) {
                header = bitstream.readFrame();
                if (header == null) break;
                try {
                    decodeFrame(header, bitstream);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                bitstream.closeFrame();
                buffer.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
            }
            bitstream.close();

            SoundFormat soundFormat = new SoundFormat();
            soundFormat.channels = outputBuffer.isStereo() ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
            soundFormat.frequency = rate;
            ByteBuffer output = MemoryUtil.memAlloc(buffer.size());
            output.put(buffer.toByteArray());
            soundFormat.buffer = output;
        } catch (Exception e) {
            Debug.logError("Unable to load mp3 file: " + path);
            e.printStackTrace();
        }
        return null;
    }


    private static ByteBuffer realloc(ByteBuffer ptr, byte[] buffer, int offset, int length) {
        try {
            ptr.put(buffer, offset, length);
        } catch (BufferOverflowException e) {
            ptr = MemoryUtil.memRealloc(ptr, ptr.capacity() + length);
            ptr = realloc(ptr, buffer, offset, length);
        }
        return ptr;
    }

}
