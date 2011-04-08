package j8583;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;

/** These are very simple tests for creating and manipulating messages.
 * 
 * @author Enrique Zamudio
 */
public class TestIsoMessage {

	private MessageFactory mf;

	@Before
	public void init() throws IOException {
		mf = new MessageFactory();
		mf.setCharacterEncoding("UTF-8");
		mf.setCustomField(48, new CustomField48());
		mf.setConfigPath("config.xml");
	}

	/** Creates a new message and checks that it has all the fields included in the config. */
	@Test
	public void testCreation() {
		IsoMessage iso = mf.newMessage(0x200);
		assert iso.getType() == 0x200;
		assert iso.getField(3).getType() == IsoType.NUMERIC && "650000".equals(iso.getObjectValue(3));
		assert iso.getField(32).getType() == IsoType.LLVAR;
		assert iso.getField(35).getType() == IsoType.LLVAR;
		assert iso.getField(43).getType() == IsoType.ALPHA && ((String)iso.getObjectValue(43)).length() == 40;
		assert iso.getField(48).getType() == IsoType.LLLVAR && iso.getObjectValue(48) instanceof CustomField48;
		assert iso.getField(49).getType() == IsoType.ALPHA;
		assert iso.getField(60).getType() == IsoType.LLLVAR;
		assert iso.getField(61).getType() == IsoType.LLLVAR;
		assert iso.getField(100).getType() == IsoType.LLVAR;
		assert iso.getField(102).getType() == IsoType.LLVAR;
		for (int i = 4; i < 32; i++) {
			assert !iso.hasField(i);
		}
		for (int i = 36; i < 43; i++) {
			assert !iso.hasField(i);
		}
		for (int i = 50; i < 60; i++) {
			assert !iso.hasField(i);
		}
		for (int i = 62; i < 100; i++) {
			assert !iso.hasField(i);
		}
		for (int i = 103; i < 128; i++) {
			assert !iso.hasField(i);
		}
	}

	@Test
	public void testEncoding() throws Exception {
		IsoMessage m1 = mf.newMessage(0x200);
		byte[] buf = m1.writeData();
		IsoMessage m2 = mf.parseMessage(buf, mf.getIsoHeader(0x200).length());
		assert m2.getType() == m1.getType();
		for (int i = 2; i < 128; i++) {
			//Either both have the field or neither have it
			if (m1.hasField(i) && m2.hasField(i)) {
				Assert.assertEquals(m1.getField(i).getType(), m2.getField(i).getType());
				Assert.assertEquals(m1.getObjectValue(i), m2.getObjectValue(i));
			} else {
				assert !m1.hasField(i) && !m2.hasField(i);
			}
		}
	}

	/** Parses a message from a file and checks the fields. */
	@Test
	public void testParsing() throws IOException, ParseException {
		InputStream ins = getClass().getResourceAsStream("/parse1.txt");
		byte[] buf = new byte[400];
		int pos = 0;
		while (ins.available() > 0) {
			buf[pos++] = (byte)ins.read();
		}
		ins.close();
		IsoMessage iso = mf.parseMessage(buf, mf.getIsoHeader(0x210).length());
		assert iso.getType() == 0x210;
		byte[] b2 = iso.writeData();
		
		//Remove leftover newline and stuff from the original buffer
		byte[] b3 = new byte[b2.length];
		System.arraycopy(buf, 0, b3, 0, b3.length);
		Assert.assertArrayEquals(b3, b2);
	}

}
