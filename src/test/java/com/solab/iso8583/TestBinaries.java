package com.solab.iso8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solab.iso8583.util.HexCodec;

/** Test binary message encoding and binary fields. */
public class TestBinaries {

	private MessageFactory<IsoMessage> mfactAscii = new MessageFactory<>();
	private MessageFactory<IsoMessage> mfactBin = new MessageFactory<>();

	@Before
	public void setup() throws IOException {
		mfactAscii.setCharacterEncoding("UTF-8");
		mfactAscii.setConfigPath("config.xml");
		mfactAscii.setAssignDate(true);
		mfactBin.setCharacterEncoding("UTF-8");
		mfactBin.setConfigPath("config.xml");
		mfactBin.setAssignDate(true);
		mfactBin.setUseBinaryMessages(true);
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
		IsoMessage bin = mfactBin.newMessage(0x600);
        Assert.assertFalse(ascii.isBinaryHeader() || ascii.isBinaryFields() || ascii.isBinaryBitmap());
        Assert.assertTrue(bin.isBinaryHeader() && bin.isBinaryFields());
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
		IsoMessage bin2 = mfactBin.parseMessage(bin.writeData(), 0);
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
		IsoMessage iso1 = mfactBin.newMessage(0x201);
		iso1.setValue(3, bignum, IsoType.NUMERIC, 19);
        iso1.setField(7, null);
        byte[] buf = iso1.writeData();
        System.out.println(HexCodec.hexEncode(buf, 0, buf.length));
		IsoMessage iso2 = mfactBin.parseMessage(buf, 0);
		Assert.assertEquals(bignum, iso2.getObjectValue(3));
        bignum = new BigInteger("1234512345123451234522");
        iso1 = mfactBin.newMessage(0x202);
        iso1.setValue(3, bignum, IsoType.NUMERIC, 22);
        iso1.setField(7, null);
        buf = iso1.writeData();
        System.out.println(HexCodec.hexEncode(buf, 0, buf.length));
		iso2 = mfactBin.parseMessage(buf, 0);
		Assert.assertEquals(bignum, iso2.getObjectValue(3));
    }

    @Test
    public void testLLBCDBINWithoutZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x281);
        iso1.setField(3, new IsoValue<>(IsoType.LLBCDBIN, "12345"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assert.assertEquals("12345", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assert.assertEquals("012345", value);
    }

    @Test
    public void testLLBCDBINWithZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x281);
        iso1.setField(3, new IsoValue<>(IsoType.LLBCDBIN, "012345"));
        byte[] buf = iso1.writeData();
        
        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assert.assertEquals("012345", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assert.assertEquals("012345", value);
    }

    @Test
    public void testLLLBCDBINWithoutZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x282);
        iso1.setField(3, new IsoValue<>(IsoType.LLLBCDBIN, "123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assert.assertEquals("123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        //In ASCII mode the leading 0 can't be truncated
        Assert.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    public void testLLLBCDBINWithZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x282);
        iso1.setField(3, new IsoValue<>(IsoType.LLLBCDBIN, "0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assert.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assert.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    public void testLLLLBCDBINWithoutZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x283);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBCDBIN, "123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assert.assertEquals("123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        //ASCII mode cannot truncate the leading 0
        Assert.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    public void testLLLLBCDBINWithZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x283);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBCDBIN, "0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assert.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assert.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }
}
