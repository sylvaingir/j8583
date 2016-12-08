package com.solab.iso8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solab.iso8583.util.HexCodec;

/** Test binary message encoding and binary fields. */
public class TestBinaries {

	private MessageFactory<IsoMessage> mfactAscii = new MessageFactory<IsoMessage>();
	private MessageFactory<IsoMessage> mfactBin = new MessageFactory<IsoMessage>();
	private MessageFactory<IsoMessage> mfactBinMtiAsciiBody = new MessageFactory<IsoMessage>();

	@Before
	public void setup() throws IOException {
		mfactAscii.setCharacterEncoding("UTF-8");
		mfactAscii.setConfigPath("config.xml");
		mfactAscii.setAssignDate(true);

		mfactBin.setCharacterEncoding("UTF-8");
		mfactBin.setConfigPath("config.xml");
		mfactBin.setAssignDate(true);
		mfactBin.setUseBinaryMessages(true);
		mfactBin.setUseBinaryBody(true);

		mfactBinMtiAsciiBody.setCharacterEncoding(StandardCharsets.US_ASCII.name());
		mfactBinMtiAsciiBody.setConfigPath("config800.xml");
		mfactBinMtiAsciiBody.setUseBinaryMessages(true);
		mfactBinMtiAsciiBody.setUseBinaryBitmap(true);
		mfactBinMtiAsciiBody.setUseBinaryBody(false);
	}

	void testParsed(IsoMessage m) {
		Assert.assertEquals(m.getType(), 0x600);
		Assert.assertEquals(new BigDecimal("1234.00"), m.getObjectValue(4));
		Assert.assertTrue("No field 7!", m.hasField(7));
		Assert.assertEquals("Wrong trace", "000123", m.getField(11).toString());
		byte[] buf = m.getObjectValue(41);
		byte[] exp = new byte[]{ (byte)0xab, (byte)0xcd, (byte)0xef, 0, 0, 0, 0, 0};
		Assert.assertEquals("Field 41 wrong length", 8, buf.length);
		Assert.assertArrayEquals("Field 41 wrong value", exp, buf);
		buf = m.getObjectValue(42);
		exp = new byte[]{ (byte)0x0a, (byte)0xbc, (byte)0xde, 0 };
		Assert.assertEquals("field 42 wrong length", 4, buf.length);
		Assert.assertArrayEquals("Field 42 wrong value", exp, buf);
		Assert.assertTrue(((String)m.getObjectValue(43)).startsWith("Field of length 40"));
		buf = m.getObjectValue(62);
		exp = new byte[]{ 1, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef,
				0x62, 1, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xab, (byte)0xcd };
		Assert.assertArrayEquals(exp, buf);
		buf = m.getObjectValue(64);
        exp[8] = 0x64;
		Assert.assertArrayEquals(exp, buf);
		buf = m.getObjectValue(63);
		exp = new byte[]{ 0, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x63 };
		Assert.assertArrayEquals(exp, buf);
		buf = m.getObjectValue(65);
        exp[5] = 0x65;
		Assert.assertArrayEquals(exp, buf);
	}

	@Test
	public void testMessages() throws ParseException, UnsupportedEncodingException {
		//Create a message with both factories
		IsoMessage ascii = mfactAscii.newMessage(0x600);
		IsoMessage bin = mfactBinMtiAsciiBody.newMessage(0x600);
        Assert.assertFalse(ascii.isBinary() || ascii.isBinaryBitmap());
        Assert.assertTrue(bin.isBinary());
		//HEXencode the binary message, headers should be similar to the ASCII version
        final byte[] _v = bin.writeData();
		String hexBin = HexCodec.hexEncode(_v, 0, _v.length);
		String hexAscii = new String(ascii.writeData()).toUpperCase();
		Assert.assertEquals("0600", hexBin.substring(0, 4));
		//Should be the same up to the field 42 (first 80 chars)
		Assert.assertEquals(hexAscii.substring(0, 88), hexBin.substring(0, 88));
        Assert.assertEquals(ascii.getObjectValue(43), new String(_v, 44, 40).trim());
		//Parse both messages
		byte[] asciiBuf = ascii.writeData();
		IsoMessage ascii2 = mfactAscii.parseMessage(asciiBuf, 0);
		testParsed(ascii2);
		Assert.assertEquals(ascii.getObjectValue(7).toString(), ascii2.getObjectValue(7).toString());
		IsoMessage bin2 = mfactBinMtiAsciiBody.parseMessage(bin.writeData(), 0);
		//Compare values, should be the same
		testParsed(bin2);
		Assert.assertEquals(bin.getObjectValue(7).toString(), bin2.getObjectValue(7).toString());
        //Test the debug string
        ascii.setValue(60, "XXX", IsoType.LLVAR, 0);
        bin.setValue(60, "XXX", IsoType.LLVAR, 0);
        Assert.assertEquals("Debug strings differ", ascii.debugString(), bin.debugString());
        Assert.assertTrue("LLVAR fields wrong", ascii.debugString().contains("03XXX"));
	}

    @Test
    public void testBinaryBitmap() throws UnsupportedEncodingException {
        IsoMessage iso1 = mfactAscii.newMessage(0x200);
        IsoMessage iso2 = mfactAscii.newMessage(0x200);
        iso1.setBinaryBitmap(true);
        byte[] data1 = iso1.writeData();
        byte[] data2 = iso2.writeData();
        //First message should be shorter by exactly 16 bytes
        Assert.assertEquals(data2.length-16, data1.length);
        //compare hex-encoded bitmap from one against the other
        byte[] sub1 = new byte[8];
        System.arraycopy(data1, 16, sub1, 0, 8);
        String sub2 = new String(data2, 16, 16, iso2.getCharacterEncoding());
        Assert.assertEquals(sub2, HexCodec.hexEncode(sub1, 0, sub1.length));
    }

    @Test
	public void test61() throws ParseException, UnsupportedEncodingException {
		BigInteger bignum = new BigInteger("1234512345123451234");
		IsoMessage iso1 = mfactBinMtiAsciiBody.newMessage(0x201);
		iso1.setValue(3, bignum, IsoType.NUMERIC, 19);
        iso1.setField(7, null);
        byte[] buf = iso1.writeData();
        System.out.println(HexCodec.hexEncode(buf, 0, buf.length));
		IsoMessage iso2 = mfactBinMtiAsciiBody.parseMessage(buf, 0);
		Assert.assertEquals(bignum, iso2.getObjectValue(3));
        bignum = new BigInteger("1234512345123451234522");
        iso1 = mfactBinMtiAsciiBody.newMessage(0x202);
        iso1.setValue(3, bignum, IsoType.NUMERIC, 22);
        iso1.setField(7, null);
        buf = iso1.writeData();
        System.out.println(HexCodec.hexEncode(buf, 0, buf.length));
		iso2 = mfactBinMtiAsciiBody.parseMessage(buf, 0);
		Assert.assertEquals(bignum, iso2.getObjectValue(3));
	}

	@Test
	public void test0810() throws ParseException, UnsupportedEncodingException {
		Date date = new Date();

		IsoMessage iso1 = mfactBinMtiAsciiBody.newMessage(0x810);
		iso1.setField(7, IsoType.DATE10.value(IsoType.DATE10.format(date, null)));
		iso1.setField(11, IsoType.NUMERIC.value(562040, 6));
		iso1.setField(70, IsoType.NUMERIC.value(1,3));

		IsoMessage iso2 = mfactBinMtiAsciiBody.parseMessage(iso1.writeData(), 0);

		Assert.assertEquals(date.toString(), iso2.getObjectValue(7).toString());
	}
}
