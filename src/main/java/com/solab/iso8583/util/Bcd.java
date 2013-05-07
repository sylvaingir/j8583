package com.solab.iso8583.util;

import java.math.BigInteger;
import java.text.ParseException;

/**
 * Routines for Binary Coded Digits.
 *
 * @author Enrique Zamudio
 *         Date: 23/04/13 11:24
 */
public final class Bcd {

    private Bcd(){}

    /** Decodes a BCD-encoded number as a long.
     * @param buf The byte buffer containing the BCD data.
     * @param pos The starting position in the buffer.
     * @param length The number of DIGITS (not bytes) to read. */
    public static long decodeToLong(byte[] buf, int pos, int length)
            throws IndexOutOfBoundsException {
        if (length > 18) {
            throw new IndexOutOfBoundsException("Buffer too big to decode as long");
        }
        long l = 0;
        long power = 1L;
        for (int i = pos + (length / 2) + (length % 2) - 1; i >= pos; i--) {
            l += (buf[i] & 0x0f) * power;
            power *= 10L;
            l += ((buf[i] & 0xf0) >> 4) * power;
            power *= 10L;
        }
        return l;
    }

    /** Encode the value as BCD and put it in the buffer. The buffer must be big enough
   	 * to store the digits in the original value (half the length of the string). */
    public static void encode(String value, byte[] buf) {
        int charpos = 0; //char where we start
        int bufpos = 0;
        if (value.length() % 2 == 1) {
            //for odd lengths we encode just the first digit in the first byte
            buf[0] = (byte)(value.charAt(0) - 48);
            charpos = 1;
            bufpos = 1;
        }
        //encode the rest of the string
        while (charpos < value.length()) {
            buf[bufpos] = (byte)(((value.charAt(charpos) - 48) << 4)
                | (value.charAt(charpos + 1) - 48));
            charpos += 2;
            bufpos++;
        }
    }

    /** Decodes a BCD-encoded number as a BigInteger.
     * @param buf The byte buffer containing the BCD data.
     * @param pos The starting position in the buffer.
     * @param length The number of DIGITS (not bytes) to read. */
    public static BigInteger decodeToBigInteger(byte[] buf, int pos, int length)
            throws IndexOutOfBoundsException {
        char[] digits = new char[length];
        int start = 0;
        for (int i = pos; i < pos + (length / 2) + (length % 2); i++) {
            digits[start++] = (char)(((buf[i] & 0xf0) >> 4) + 48);
            digits[start++] = (char)((buf[i] & 0x0f) + 48);
        }
        return new BigInteger(new String(digits));
    }

}
