package j8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.util.HexCodec;

/** Test binary message encoding and binary fields. */
public class TestBinaries {

	private MessageFactory mfactAscii = new MessageFactory();
	private MessageFactory mfactBin = new MessageFactory();

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
		byte[] buf = (byte[])m.getObjectValue(41);
		byte[] exp = new byte[]{ (byte)0xab, (byte)0xcd, (byte)0xef, 0, 0, 0, 0, 0};
		Assert.assertEquals("Field 41 wrong length", 8, buf.length);
		Assert.assertTrue("Field 41 wrong value", Arrays.equals(exp, buf));
		buf = (byte[])m.getObjectValue(42);
		exp = new byte[]{ (byte)0x0a, (byte)0xbc, (byte)0xde, 0 };
		Assert.assertEquals("field 42 wrong length", 4, buf.length);
		Assert.assertTrue("Field 42 wrong value", Arrays.equals(exp, buf));
		Assert.assertTrue(((String)m.getObjectValue(43)).startsWith("Field of length 40"));
		buf = (byte[])m.getObjectValue(62);
		exp = new byte[]{ 1, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef,
				1, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef };
		Assert.assertEquals(16, buf.length);
		Assert.assertTrue(Arrays.equals(exp, buf));
		buf = (byte[])m.getObjectValue(64);
		Assert.assertEquals(16, buf.length);
		Assert.assertTrue(Arrays.equals(exp, buf));
		buf = (byte[])m.getObjectValue(63);
		exp = new byte[]{ 0, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x9a };
		Assert.assertEquals(6, buf.length);
		Assert.assertTrue(Arrays.equals(exp, buf));
		buf = (byte[])m.getObjectValue(65);
		Assert.assertEquals(6, buf.length);
		Assert.assertTrue(Arrays.equals(exp, buf));
	}

	@Test
	public void testMessages() throws ParseException, UnsupportedEncodingException {
		//Create a message with both factories
		IsoMessage ascii = mfactAscii.newMessage(0x600);
		IsoMessage bin = mfactBin.newMessage(0x600);
		//HEXencode the binary message, headers should be similar to the ASCII version
		String hexBin = HexCodec.hexEncode(bin.writeData());
		String hexAscii = new String(ascii.writeData()).toUpperCase();
		Assert.assertEquals("0600", hexBin.substring(0, 4));
		//Should be the same up to the field 42 (first 80 chars)
		Assert.assertEquals(hexAscii.substring(0, 88), hexBin.substring(0, 88));
		//Parse both messages
		byte[] asciiBuf = ascii.writeData();
		IsoMessage ascii2 = mfactAscii.parseMessage(asciiBuf, 0);
		testParsed(ascii2);
		Assert.assertEquals(ascii.getObjectValue(7).toString(), ascii2.getObjectValue(7).toString());
		IsoMessage bin2 = mfactBin.parseMessage(bin.writeData(), 0);
		//Compare values, should be the same
		testParsed(bin2);
		Assert.assertEquals(bin.getObjectValue(7).toString(), bin2.getObjectValue(7).toString());
	}

}
