package com.solab.iso8583.codecs;

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.util.Bcd;

import java.math.BigInteger;

/**
 * A custom field encoder/decoder to be used with LLBIN/LLLBIN fields
 * that contain BigIntegers in BCD encoding.
 *
 * @author Enrique Zamudio
 *         Date: 07/05/13 13:02
 */
public class BigIntBcdCodec implements CustomBinaryField<BigInteger> {

    @Override
    public BigInteger decodeBinaryField(byte[] value, int pos, int len) {
        return Bcd.decodeToBigInteger(value, pos, len*2);
    }

    @Override
    public byte[] encodeBinaryField(BigInteger value) {
        final String s = value.toString(10);
        final byte[] buf = new byte[s.length() / 2 + s.length() % 2];
        Bcd.encode(s, buf);
        return buf;
    }

    @Override
    public BigInteger decodeField(String value) {
        return new BigInteger(value, 10);
    }

    @Override
    public String encodeField(BigInteger value) {
        return value.toString(10);
    }

}
