package j8583;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import com.solab.iso8583.IsoValue;
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

	private MessageFactory<IsoMessage> mf;

	@Before
	public void init() throws IOException {
		mf = new MessageFactory<IsoMessage>();
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
		Assert.assertEquals(0x210, iso.getType());
		byte[] b2 = iso.writeData();
		
		//Remove leftover newline and stuff from the original buffer
		byte[] b3 = new byte[b2.length];
		System.arraycopy(buf, 0, b3, 0, b3.length);
		Assert.assertArrayEquals(b3, b2);
	}

	@Test
	public void testTemplating() {
		IsoMessage iso1 = mf.newMessage(0x200);
		IsoMessage iso2 = mf.newMessage(0x200);
		assert iso1 != iso2;
		assert iso1.getObjectValue(3).equals(iso2.getObjectValue(3));
		assert iso1.getField(3) != iso2.getField(3);
		assert iso1.getField(48) != iso2.getField(48);
		CustomField48 cf48_1 = iso1.getObjectValue(48);
		int origv = cf48_1.getValue2();
		cf48_1.setValue2(origv + 1000);
		CustomField48 cf48_2 = iso2.getObjectValue(48);
		assert cf48_1 == cf48_2;
		assert cf48_2.getValue2() == origv + 1000;
	}

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleFieldSetter() {
        IsoMessage iso = mf.newMessage(0x200);
        IsoValue<String> f3 = iso.getField(3);
        iso.updateValue(3, "999999");
        assert iso.getObjectValue(3).equals("999999");
        IsoValue<String> nf3 = iso.getField(3);
        assert f3 != nf3;
        assert f3.getType() == nf3.getType();
        assert f3.getLength() == nf3.getLength();
        assert f3.getEncoder() == nf3.getEncoder();
        iso.updateValue(4, "INVALID!");
        throw new RuntimeException("Update failed!");
    }

}
