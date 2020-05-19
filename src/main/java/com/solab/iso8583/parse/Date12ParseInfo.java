/*
 * j8583 A Java implementation of the ISO8583 protocol
 * Copyright (C) 2007 Enrique Zamudio Lopez
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package com.solab.iso8583.parse;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.Bcd;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Blabla.
 *
 * @author Enrique Zamudio
 *         Date: 06/01/16 10:52 AM
 */
public class Date12ParseInfo extends DateTimeParseInfo {

    public Date12ParseInfo() {
   		super(IsoType.DATE12, 12);
   	}

   	@Override
   	public <T> IsoValue<Date> parse(final int field, final byte[] buf,
                                       final int pos, final CustomField<T> custom)
   			throws ParseException, UnsupportedEncodingException {
   		if (pos < 0) {
   			throw new ParseException(String.format("Invalid DATE12 field %d position %d",
                       field, pos), pos);
   		}
   		if (pos+12 > buf.length) {
   			throw new ParseException(String.format("Insufficient data for DATE12 field %d, pos %d",
                       field, pos), pos);
   		}
   		//A SimpleDateFormat in the case of dates won't help because of the missing data
   		//we have to use the current date for reference and change what comes in the buffer
        Calendar cal = Calendar.getInstance();
   		//Set the month in the date
        int year;
        if (forceStringDecoding) {
            year = Integer.parseInt(new String(buf, pos, 2, getCharacterEncoding()), 10);
            cal.set(Calendar.MONTH, Integer.parseInt(new String(buf, pos, 2, getCharacterEncoding()), 10)-1);
            cal.set(Calendar.DATE, Integer.parseInt(new String(buf, pos+2, 2, getCharacterEncoding()), 10));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(new String(buf, pos+4, 2, getCharacterEncoding()), 10));
            cal.set(Calendar.MINUTE, Integer.parseInt(new String(buf, pos+6, 2, getCharacterEncoding()), 10));
            cal.set(Calendar.SECOND, Integer.parseInt(new String(buf, pos+8, 2, getCharacterEncoding()), 10));
        } else {
            year = ((buf[pos] - 48) * 10) + buf[pos + 1] - 48;
            cal.set(Calendar.MONTH, ((buf[pos+2] - 48) * 10) + buf[pos + 3] - 49);
            cal.set(Calendar.DATE, ((buf[pos + 4] - 48) * 10) + buf[pos + 5] - 48);
            cal.set(Calendar.HOUR_OF_DAY, ((buf[pos + 6] - 48) * 10) + buf[pos + 7] - 48);
            cal.set(Calendar.MINUTE, ((buf[pos + 8] - 48) * 10) + buf[pos + 9] - 48);
            cal.set(Calendar.SECOND, ((buf[pos + 10] - 48) * 10) + buf[pos + 11] - 48);
        }
        if (year > 50) {
            cal.set(Calendar.YEAR, 1900+year);
        } else {
            cal.set(Calendar.YEAR, 2000+year);
        }
        cal.set(Calendar.MILLISECOND,0);
        return createValue(cal, true);
   	}

   	@Override
   	public <T> IsoValue<Date> parseBinary(final int field, final byte[] buf,
                                          final int pos, final CustomField<T> custom)
               throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid DATE12 field %d position %d",
                field, pos), pos);
        }
        if (pos+6 > buf.length) {
            throw new ParseException(String.format("Insufficient data for DATE12 field %d, pos %d",
                field, pos), pos);
        }
   		int[] tens = new int[6];
   		int start = 0;
   		for (int i = pos; i < pos + tens.length; i++) {
   			tens[start++] = Bcd.parseBcdLength(buf[i]);
   		}
   		Calendar cal = Calendar.getInstance();
   		//A SimpleDateFormat in the case of dates won't help because of the missing data
   		//we have to use the current date for reference and change what comes in the buffer
   		//Set the month in the date
        if (tens[0] > 50) {
            cal.set(Calendar.YEAR, 1900+tens[0]);
        } else {
            cal.set(Calendar.YEAR, 2000+tens[0]);
        }
   		cal.set(Calendar.MONTH, tens[1] - 1);
   		cal.set(Calendar.DATE, tens[2]);
   		cal.set(Calendar.HOUR_OF_DAY, tens[3]);
   		cal.set(Calendar.MINUTE, tens[4]);
   		cal.set(Calendar.SECOND, tens[5]);
   		cal.set(Calendar.MILLISECOND,0);
        return createValue(cal, true);
   	}

}
