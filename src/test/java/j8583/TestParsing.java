package j8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.NumericParseInfo;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solab.iso8583.MessageFactory;

/** Test that parsing invalid messages is properly handled.
 * 
 * @author Enrique Zamudio
 */
public class TestParsing {

	private MessageFactory mf;

	@Before
	public void init() throws IOException {
		mf = new MessageFactory();
		mf.setCharacterEncoding("UTF-8");
		mf.setCustomField(48, new CustomField48());
		mf.setConfigPath("config.xml");
	}

	@Test(expected=ParseException.class)
	public void testEmpty() throws ParseException, UnsupportedEncodingException {
		mf.parseMessage(new byte[0], 0);
	}

	@Test(expected=ParseException.class)
	public void testShort() throws ParseException, UnsupportedEncodingException {
		mf.parseMessage(new byte[20], 8);
	}

	@Test(expected=ParseException.class)
	public void testShortBin() throws ParseException, UnsupportedEncodingException {
		mf.setUseBinaryMessages(true);
		mf.parseMessage(new byte[10], 1);
	}

	@Test(expected=ParseException.class)
	public void testShortSecondaryBitmap() throws ParseException, UnsupportedEncodingException {
		mf.parseMessage("02008000000000000000".getBytes(), 0);
	}

	@Test(expected=ParseException.class)
	public void testShortSecondaryBitmapBin() throws ParseException, UnsupportedEncodingException {
		mf.setUseBinaryMessages(true);
		mf.parseMessage(new byte[]{ 2, 0, (byte)128, 0, 0, 0, 0, 0, 0, 0 }, 0);
	}

	@Test(expected=ParseException.class)
	public void testNoFields() throws ParseException, UnsupportedEncodingException {
		mf.parseMessage("0210B23A80012EA080180000000014000004".getBytes(), 0);
	}

	@Test(expected=ParseException.class)
	public void testNoFieldsBin() throws ParseException, UnsupportedEncodingException {
		mf.setUseBinaryMessages(true);
		mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4}, 0);
	}

	@Test(expected=ParseException.class)
	public void testIncompleteFixedField() throws ParseException, UnsupportedEncodingException {
		mf.parseMessage("0210B23A80012EA08018000000001400000465000".getBytes(), 0);
	}

	@Test(expected=ParseException.class)
	public void testIncompleteFixedFieldBin() throws ParseException, UnsupportedEncodingException {
		mf.setUseBinaryMessages(true);
		mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4, 0x65, 0}, 0);
	}

	@Test(expected=ParseException.class)
	public void testIncompleteVarFieldHeader()  throws ParseException, UnsupportedEncodingException {
		mf.parseMessage("0210B23A80012EA08018000000001400000465000000000000300004281305474687711259460428042808115".getBytes(), 0);
	}

	@Test(expected=ParseException.class)
	public void testIncompleteVarFieldHeaderBin()  throws ParseException, UnsupportedEncodingException {
		mf.setUseBinaryMessages(true);
		mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4, 0x65, 0, 0, 0, 0, 0, 0, 0x30, 0, 0x04, 0x28, 0x13, 0x05, 0x47, 0x46, (byte)0x87, 0x71, 0x12, 0x59, 0x46, 0x04, 0x28, 0x04, 0x28, 0x08, 0x11}, 0);
	}

	@Test(expected=ParseException.class)
	public void testIncompleteVarFieldData()  throws ParseException, UnsupportedEncodingException {
		mf.parseMessage("0210B23A80012EA0801800000000140000046500000000000030000428130547468771125946042804280811051234".getBytes(), 0);
	}

	@Test(expected=ParseException.class)
	public void testIncompleteVarFieldDataBin()  throws ParseException, UnsupportedEncodingException {
		mf.setUseBinaryMessages(true);
		mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4, 0x65, 0, 0, 0, 0, 0, 0, 0x30, 0, 0x04, 0x28, 0x13, 0x05, 0x47, 0x46, (byte)0x87, 0x71, 0x12, 0x59, 0x46, 0x04, 0x28, 0x04, 0x28, 0x08, 0x11, 0x05, 0x12, 0x34}, 0);
	}

    @Test
    public void testBinaryNumberParsing() throws ParseException {
        NumericParseInfo npi = new NumericParseInfo(6);
        IsoValue<Number> val = npi.parseBinary(new byte[]{0x12, 0x34, 0x56}, 0, null);
        Assert.assertEquals(123456, val.getValue().intValue());
    }

}
