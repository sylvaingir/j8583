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

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse fields of type DATE_EXP.
 * 
 * @author Enrique Zamudio
 */
public class DateExpParseInfo extends DateTimeParseInfo {

	public DateExpParseInfo() {
		super(IsoType.DATE_EXP, 4);
	}

	@Override
	public <T> IsoValue<Date> parse(final int field, final byte[] buf,
                                final int pos, final CustomField<T> custom)
            throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid DATE_EXP field %d position %d",
                    field, pos), pos);
		}
		if (pos+4 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for DATE_EXP field %d pos %d", field, pos), pos);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DATE, 1);
		//Set the month in the date
        if (forceStringDecoding) {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - (cal.get(Calendar.YEAR) % 100)
                    + Integer.parseInt(new String(buf, pos, 2, getCharacterEncoding()), 10));
            cal.set(Calendar.MONTH, Integer.parseInt(new String(buf, pos+2, 2, getCharacterEncoding()), 10)-1);
        } else {
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - (cal.get(Calendar.YEAR) % 100)
                    + ((buf[pos] - 48) * 10) + buf[pos + 1] - 48);
            cal.set(Calendar.MONTH, ((buf[pos + 2] - 48) * 10) + buf[pos + 3] - 49);
        }
        if (tz != null) {
            cal.setTimeZone(tz);
        }
		return new IsoValue<Date>(type, cal.getTime(), null);
	}

	@Override
	public <T> IsoValue<Date> parseBinary(final int field, final byte[] buf,
                                      final int pos, final CustomField<T> custom)
            throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid DATE_EXP field %d position %d",
                      field, pos), pos);
        }
        if (pos+2 > buf.length) {
            throw new ParseException(String.format(
                      "Insufficient data for DATE_EXP field %d pos %d", field, pos), pos);
        }
		int[] tens = new int[2];
		int start = 0;
		for (int i = pos; i < pos + tens.length; i++) {
			tens[start++] = (((buf[i] & 0xf0) >> 4) * 10) + (buf[i] & 0x0f);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.DATE, 1);
		//Set the month in the date
		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)
				- (cal.get(Calendar.YEAR) % 100) + tens[0]);
		cal.set(Calendar.MONTH, tens[1] - 1);
        if (tz != null) {
            cal.setTimeZone(tz);
        }
		return new IsoValue<Date>(type, cal.getTime(), null);
	}
}
