package com.solab.iso8583;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.solab.iso8583.codecs.CompositeField;
import com.solab.iso8583.parse.NumericParseInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** Test that parsing invalid messages is properly handled.
 * 
 * @author Enrique Zamudio
 */
public class TestParsing {

	private MessageFactory<IsoMessage> mf;

	@Before
	public void init() throws IOException {
		mf = new MessageFactory<>();
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
        IsoValue<Number> val = npi.parseBinary(0, new byte[]{0x12, 0x34, 0x56}, 0, null);
        Assert.assertEquals(123456, val.getValue().intValue());
    }

    @Test
    public void testDates() throws ParseException, UnsupportedEncodingException {
		com.solab.iso8583.parse.DateTimeParseInfo.setDefaultTimeZone(TimeZone.getTimeZone("GMT-0700"));
		Calendar cal = new GregorianCalendar();

        IsoMessage m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        Assert.assertNotNull(m);
        Date f = m.getObjectValue(7);
        Assert.assertNotNull(f);

		cal.setTimeZone(TimeZone.getTimeZone("GMT-0700"));
		cal.setTime(f);
        Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assert.assertEquals("Date", 25, cal.get(Calendar.DATE));
        Assert.assertEquals("Hour of Day", 21, cal.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals("debug string should match", "060002000000000000000125213456", m.debugString());

		com.solab.iso8583.parse.DateTimeParseInfo.setDefaultTimeZone(null);
		TimeZone utcTz = TimeZone.getTimeZone("UTC");
		mf.setTimezoneForParseGuide(0x600, 7, utcTz);


		m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        f = m.getObjectValue(7);
		cal.setTimeZone(TimeZone.getTimeZone("GMT-0600"));
		cal.setTime(f);

		Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assert.assertEquals(25, cal.get(Calendar.DATE));
        Assert.assertEquals("Hour of day mismatch", 15, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), m.getField(7).getTimeZone());
        mf.setTimezoneForParseGuide(0x600, 7, TimeZone.getTimeZone("GMT+0100"));
        m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        f = m.getObjectValue(7);
        cal.setTime(f);
        Assert.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assert.assertEquals(25, cal.get(Calendar.DATE));
        Assert.assertEquals("Hour of day mismatch", 14, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(TimeZone.getTimeZone("GMT+0100"), m.getField(7).getTimeZone());
    }

    @Test
    public void testTimezoneInResponse() throws ParseException, IOException {
        mf.setTimezoneForParseGuide(0x600, 7, TimeZone.getTimeZone("UTC"));
        IsoMessage m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), m.getField(7).getTimeZone());
        IsoMessage r = mf.createResponse(m);
        Assert.assertEquals(0x610, r.getType());
        Assert.assertTrue(r.hasField(7));
        Assert.assertTrue(m.getField(7) != r.getField(7));
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), r.getField(7).getTimeZone());
    }
}
