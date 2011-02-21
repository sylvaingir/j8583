package com.solab.iso8583.parse;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse fields of type DATE10.
 * 
 * @author Enrique Zamudio
 */
public class Date10ParseInfo extends FieldParseInfo {

	
	public Date10ParseInfo() {
		super(IsoType.DATE10, 10);
	}

	@Override
	public <T> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom)
			throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid position %d", pos), pos);
		}
		if (pos+10 > buf.length) {
			throw new ParseException(String.format("Insufficient data for DATE10 field, pos %d", pos), pos);
		}
		//A SimpleDateFormat in the case of dates won't help because of the missing data
		//we have to use the current date for reference and change what comes in the buffer
		Calendar cal = Calendar.getInstance();
		//Set the month in the date
		cal.set(Calendar.MONTH, ((buf[pos] - 48) * 10) + buf[pos + 1] - 49);
		cal.set(Calendar.DATE, ((buf[pos + 2] - 48) * 10) + buf[pos + 3] - 48);
		cal.set(Calendar.HOUR_OF_DAY, ((buf[pos + 4] - 48) * 10) + buf[pos + 5] - 48);
		cal.set(Calendar.MINUTE, ((buf[pos + 6] - 48) * 10) + buf[pos + 7] - 48);
		cal.set(Calendar.SECOND, ((buf[pos + 8] - 48) * 10) + buf[pos + 9] - 48);
		if (cal.getTime().after(new Date())) {
			cal.add(Calendar.YEAR, -1);
		}
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

	@Override
	public <T> IsoValue<?> parseBinary(byte[] buf, int pos,
			CustomField<T> custom) throws ParseException {
		int[] tens = new int[5];
		int start = 0;
		for (int i = pos; i < pos + tens.length; i++) {
			tens[start++] = (((buf[i] & 0xf0) >> 4) * 10) + (buf[i] & 0x0f);
		}
		Calendar cal = Calendar.getInstance();
		//A SimpleDateFormat in the case of dates won't help because of the missing data
		//we have to use the current date for reference and change what comes in the buffer
		//Set the month in the date
		cal.set(Calendar.MONTH, tens[0] - 1);
		cal.set(Calendar.DATE, tens[1]);
		cal.set(Calendar.HOUR_OF_DAY, tens[2]);
		cal.set(Calendar.MINUTE, tens[3]);
		cal.set(Calendar.SECOND, tens[4]);
		if (cal.getTime().after(new Date())) {
			cal.add(Calendar.YEAR, -1);
		}
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

}
