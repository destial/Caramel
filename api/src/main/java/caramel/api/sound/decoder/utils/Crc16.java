package caramel.api.sound.decoder.utils;

public final class Crc16 {
    private static final short polynomial = (short)0x8005;
    private short crc;

    public Crc16() {
        crc = (short)0xFFFF;
    }

    public void add_bits(int bitstring, int length) {
        int bitmask = 1 << length - 1;
        do
            if ((crc & 0x8000) == 0 ^ (bitstring & bitmask) == 0) {
                crc <<= 1;
                crc ^= polynomial;
            } else
                crc <<= 1;
        while ((bitmask >>>= 1) != 0);
    }

    public short checksum() {
        short sum = crc;
        crc = (short)0xFFFF;
        return sum;
    }
}
