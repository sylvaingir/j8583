package com.solab.iso8583.parse;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse TIME fields.
 * 
 * @author Enrique Zamudio
 */
public class TimeParseInfo extends FieldParseInfo {

	
	public TimeParseInfo() {
		super(IsoType.TIME, 6);
	}

	@Override
	public <T> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom)
			throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid position %d", pos), pos);
		}
		if (pos+6 > buf.length) {
			throw new ParseException(String.format("Insufficient data for TIME field, pos %d", pos), pos);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, ((buf[pos] - 48) * 10) + buf[pos + 1] - 48);
		cal.set(Calendar.MINUTE, ((buf[pos + 2] - 48) * 10) + buf[pos + 3] - 48);
		cal.set(Calendar.SECOND, ((buf[pos + 4] - 48) * 10) + buf[pos + 5] - 48);
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

	@Override
	public <T> IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		int[] tens = new int[3];
		int start = 0;
		for (int i = pos; i < pos + tens.length; i++) {
			tens[start++] = (((buf[i] & 0xf0) >> 4) * 10) + (buf[i] & 0x0f);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, tens[0]);
		cal.set(Calendar.MINUTE, tens[1]);
		cal.set(Calendar.SECOND, tens[2]);
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

}
