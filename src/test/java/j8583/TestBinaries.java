package j8583;

import java.io.IOException;

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
		mfactAscii.setConfigPath("config.xml");
		mfactBin.setConfigPath("config.xml");
		mfactAscii.setAssignDate(true);
		mfactBin.setAssignDate(true);
		mfactBin.setUseBinaryMessages(true);
	}

	@Test
	public void testMessages() {
		//Create a message with both factories
		IsoMessage ascii = mfactAscii.newMessage(0x600);
		IsoMessage bin = mfactBin.newMessage(0x600);
		//HEXencode the binary message, headers should be similar to the ASCII version
		String hexBin = HexCodec.hexEncode(bin.writeData());
		String hexAscii = new String(ascii.writeData());
		System.out.printf("bin:   %s%nascii: %s%n", hexBin, hexAscii);
		Assert.assertEquals("0600", hexBin.substring(0, 4));
		//Should be the same up to the trace (first 64 chars)
		Assert.assertEquals(hexAscii.substring(0, 64), hexBin.substring(0, 64));
		//Parse both messages
		//Compare values, should be the same
	}

}
