package j8583;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

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
		mf.setCustomField(48, new CustomField48());
		mf.setConfigPath("config.xml");
	}

	/** Creates a new message and checks that it has all the fields included in the config. */
	@Test
	public void testCreation() {
		IsoMessage iso = mf.newMessage(0x200);
		assert iso.getType() == 0x200;
		assert iso.getField(3).getType() == IsoType.NUMERIC && "650000".equals((String)iso.getObjectValue(3));
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
	}

}
