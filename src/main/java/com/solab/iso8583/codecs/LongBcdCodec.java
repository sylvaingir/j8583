package com.solab.iso8583.codecs;

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.util.Bcd;

/**
 * A custom field encoder/decoder to be used with LLBIN/LLLBIN fields
 * that contain Longs in BCD encoding.
 *
 * @author Enrique Zamudio
 *         Date: 07/05/13 13:02
 */
public class LongBcdCodec implements CustomBinaryField<Long> {

    @Override
    public Long decodeBinaryField(byte[] value, int pos, int length) {
        return Bcd.decodeToLong(value, pos, length*2);
    }

    @Override
    public byte[] encodeBinaryField(Long value) {
        final String s = Long.toString(value, 10);
        final byte[] buf = new byte[s.length() / 2 + s.length() % 2];
        Bcd.encode(s, buf);
        return buf;
    }

    @Override
    public Long decodeField(String value) {
        return Long.parseLong(value, 10);
    }

    @Override
    public String encodeField(Long value) {
        return value.toString();
    }
}
