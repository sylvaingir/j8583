package com.solab.iso8583.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.*;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** Test that the dates are formatted and parsed correctly.
 * 
 * @author Enrique Zamudio
 */
public class TestDates {

	@Test
	public void testDate4FutureTolerance() throws ParseException, IOException {
		GregorianCalendar today = new GregorianCalendar();
		Date soon = new Date(today.getTime().getTime() + 50000);
		today.set(GregorianCalendar.HOUR,0);
		today.set(GregorianCalendar.MINUTE,0);
		today.set(GregorianCalendar.SECOND,0);
		today.set(GregorianCalendar.MILLISECOND,0);
		byte[] buf = IsoType.DATE4.format(soon, null).getBytes();
		IsoValue<Date> comp = new Date4ParseInfo().parse(0, buf, 0, null);
		Assert.assertEquals(comp.getValue(), today.getTime());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true, false);
		IsoValue<Date> bin = new Date4ParseInfo().parseBinary(0, bout.toByteArray(), 0, null);
		Assert.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

	@Test
	public void testDate10FutureTolerance() throws ParseException, IOException {
		Date soon = new Date(System.currentTimeMillis() + 50000);
		byte[] buf = IsoType.DATE10.format(soon, null).getBytes();
		IsoValue<Date> comp = new Date10ParseInfo().parse(0, buf, 0, null);
		assert comp.getValue().after(new Date());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true, false);
		IsoValue<Date> bin = new Date10ParseInfo().parseBinary(0, bout.toByteArray(), 0, null);
		Assert.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

}
