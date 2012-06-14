/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2011 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
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

	private static final long FUTURE_TOLERANCE;

	static {
		FUTURE_TOLERANCE = Long.parseLong(System.getProperty("j8583.future.tolerance", "900000"));
	}
	public Date10ParseInfo() {
		super(IsoType.DATE10, 10);
	}

	@Override
	public IsoValue<Date> parse(byte[] buf, int pos, CustomField<?> custom)
			throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid DATE10 position %d", pos), pos);
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
		cal.set(Calendar.MILLISECOND,0);
		adjustWithFutureTolerance(cal);
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

	@Override
	public IsoValue<Date> parseBinary(byte[] buf, int pos, CustomField<?> custom) throws ParseException {
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
		cal.set(Calendar.MILLISECOND,0);
		adjustWithFutureTolerance(cal);
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

	public static void adjustWithFutureTolerance(Calendar cal) {
		//We need to handle a small tolerance into the future (a couple of minutes)
		long now = System.currentTimeMillis();
		long then = cal.getTimeInMillis();
		if (then > now && then-now > FUTURE_TOLERANCE) {
			cal.add(Calendar.YEAR, -1);
		}
	}

}
