package j8583;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.codecs.BigIntBcdCodec;
import com.solab.iso8583.codecs.LongBcdCodec;
import com.solab.iso8583.parse.FieldParseInfo;
import com.solab.iso8583.parse.LlbinParseInfo;
import com.solab.iso8583.parse.LllbinParseInfo;
import com.solab.iso8583.util.HexCodec;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.HashMap;

/**
 * Unit tests for the numeric CustomBinaryField codecs.
 *
 * @author Enrique Zamudio
 *         Date: 07/05/13 13:20
 */
public class CustomBinCodecs {

    private final BigInteger b29 = new BigInteger("12345678901234567890123456789");
    final byte[] longData2 = new byte[]{ 0x12, 0x34, 0x56, 0x78, (byte)0x90, 00, 00, 00, 00, 00 };
    final byte[] bigintData1 = new byte[]{ 1, 0x23, 0x45, 0x67, (byte)0x89, 1, 0x23, 0x45, 0x67, (byte)0x89, 1, 0x23, 0x45, 0x67, (byte)0x89, 00, 00, 00, 00, 00 };

    @Test
    public void testLongCodec() {
        final LongBcdCodec longCodec = new LongBcdCodec();
        final byte[] data1 = new byte[]{ 1, 0x23, 0x45, (byte)0x67, (byte)0x89, 00, 00, 00, 00, 00 };
        Assert.assertEquals(123456789l, (long) longCodec.decodeBinaryField(data1, 0, 5));
        Assert.assertEquals(1234567890l, (long)longCodec.decodeBinaryField(longData2, 0, 5));
        final byte[] cod1 = longCodec.encodeBinaryField(123456789l);
        final byte[] cod2 = longCodec.encodeBinaryField(1234567890l);
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("LONG Data1 differs at pos " + i, data1[i], cod1[i]);
            Assert.assertEquals("LONG Data2 differs at pos " + i, longData2[i], cod2[i]);
        }
    }

    @Test
    public void testBigIntCodec() {
        final BigInteger b30 = new BigInteger("123456789012345678901234567890");
        final BigIntBcdCodec bigintCodec = new BigIntBcdCodec();
        final byte[] data2 = new byte[]{ 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, (byte)0x78, (byte)0x90, 00, 00, 00, 00, 00 };
        Assert.assertEquals(b29, bigintCodec.decodeBinaryField(bigintData1, 0, 15));
        Assert.assertEquals(b30, bigintCodec.decodeBinaryField(data2, 0, 15));
        final byte[] cod1 = bigintCodec.encodeBinaryField(b29);
        final byte[] cod2 = bigintCodec.encodeBinaryField(b30);
        for (int i = 0; i < 15; i++) {
            Assert.assertEquals("BIGINT Data1 differs at pos " + i, bigintData1[i], cod1[i]);
            Assert.assertEquals("BIGINT Data2 differs at pos " + i, data2[i], cod2[i]);
        }
    }

    private void testFieldType(final IsoType type, final FieldParseInfo fieldParser, int offset1, int offset2) throws UnsupportedEncodingException, ParseException {
        final BigIntBcdCodec bigintCodec = new BigIntBcdCodec();
        final LongBcdCodec longCodec = new LongBcdCodec();
        final MessageFactory<IsoMessage> mfact = new MessageFactory<IsoMessage>();
        IsoMessage tmpl = new IsoMessage();
        tmpl.setBinary(true);
        tmpl.setType(0x200);
        tmpl.setValue(2, 1234567890l, longCodec, type, 0);
        tmpl.setValue(3, b29, bigintCodec, type, 0);
        mfact.addMessageTemplate(tmpl);
        mfact.setCustomField(2, longCodec);
        mfact.setCustomField(3, bigintCodec);
        HashMap<Integer, FieldParseInfo> parser = new HashMap<Integer,FieldParseInfo>();
        parser.put(2, fieldParser);
        parser.put(3, fieldParser);
        mfact.setParseMap(0x200, parser);
        mfact.setUseBinaryMessages(true);
        //Test encoding
        tmpl = mfact.newMessage(0x200);
        byte[] buf = tmpl.writeData();
        System.out.println("MENSAJE: " + HexCodec.hexEncode(buf, 0, buf.length));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals("LONG Data differs at pos " + i, longData2[i], buf[i+offset1]);
        }
        for (int i = 0; i < 15; i++) {
            Assert.assertEquals("BIGINT Data differs at pos " + i, bigintData1[i], buf[i+offset2]);
        }
        //Test parsing
        tmpl = mfact.parseMessage(buf, 0);
        Assert.assertEquals(new Long(1234567890l), tmpl.getObjectValue(2));
        Assert.assertEquals(b29, tmpl.getObjectValue(3));
    }

    @Test
    public void testLLBIN() throws ParseException, UnsupportedEncodingException {
        testFieldType(IsoType.LLBIN, new LlbinParseInfo(), 11, 17);
    }

    @Test
    public void testLLLBIN() throws ParseException, UnsupportedEncodingException {
        testFieldType(IsoType.LLLBIN, new LllbinParseInfo(), 12, 19);
    }

}
