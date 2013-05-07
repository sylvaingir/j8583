package j8583;

import com.solab.iso8583.codecs.BigIntBcdCodec;
import com.solab.iso8583.codecs.LongBcdCodec;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Unit tests for the numeric CustomBinaryField codecs.
 *
 * @author Enrique Zamudio
 *         Date: 07/05/13 13:20
 */
public class CustomBinCodecs {

    @Test
    public void testLongCodec() {
        final LongBcdCodec longCodec = new LongBcdCodec();
        final byte[] data1 = new byte[]{ 1, 0x23, 0x45, (byte)0x67, (byte)0x89, 00, 00, 00, 00, 00 };
        final byte[] data2 = new byte[]{ 0x12, 0x34, 0x56, 0x78, (byte)0x90, 00, 00, 00, 00, 00 };
        Assert.assertEquals(123456789l, (long) longCodec.decodeBinaryField(data1, 0, 5));
        Assert.assertEquals(1234567890l, (long)longCodec.decodeBinaryField(data2, 0, 5));
        final byte[] cod1 = longCodec.encodeBinaryField(123456789l);
        final byte[] cod2 = longCodec.encodeBinaryField(1234567890l);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("LONG Data1 differs at pos " + i, data1[i], cod1[i]);
            Assert.assertEquals("LONG Data2 differs at pos " + i, data2[i], cod2[i]);
        }
    }

    @Test
    public void testBigIntCodec() {
        final BigInteger b29 = new BigInteger("12345678901234567890123456789");
        final BigInteger b30 = new BigInteger("123456789012345678901234567890");
        final BigIntBcdCodec bigintCodec = new BigIntBcdCodec();
        final byte[] data1 = new byte[]{ 1, 0x23, 0x45, 0x67, (byte)0x89, 1, 0x23, 0x45, 0x67, (byte)0x89, 1, 0x23, 0x45, 0x67, (byte)0x89, 00, 00, 00, 00, 00 };
        final byte[] data2 = new byte[]{ 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, (byte)0x78, (byte)0x90, 00, 00, 00, 00, 00 };
        Assert.assertEquals(b29, bigintCodec.decodeBinaryField(data1, 0, 15));
        Assert.assertEquals(b30, bigintCodec.decodeBinaryField(data2, 0, 15));
        final byte[] cod1 = bigintCodec.encodeBinaryField(b29);
        final byte[] cod2 = bigintCodec.encodeBinaryField(b30);
        for (int i = 0; i < 15; i++) {
            Assert.assertEquals("BIGINT Data1 differs at pos " + i, data1[i], cod1[i]);
            Assert.assertEquals("BIGINT Data2 differs at pos " + i, data2[i], cod2[i]);
        }
    }

}
