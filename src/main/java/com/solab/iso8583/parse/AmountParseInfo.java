/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2011 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

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
import java.math.BigDecimal;
import java.text.ParseException;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse AMOUNT fields.
 * 
 * @author Enrique Zamudio
 */
public class AmountParseInfo extends FieldParseInfo {

	
	public AmountParseInfo() {
		super(IsoType.AMOUNT, 12);
	}

    @Override
	public <T> IsoValue<BigDecimal> parse(final int field, final byte[] buf,
                                      final int pos, final CustomField<T> custom)
            throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid AMOUNT field %d position %d",
                    field, pos), pos);
		}
		if (pos+12 > buf.length) {
			throw new ParseException(String.format("Insufficient data for AMOUNT field %d, pos %d",
                    field, pos), pos);
		}
		String c = new String(buf, pos, 12, getCharacterEncoding());
		try {
			return new IsoValue<>(type, new BigDecimal(c).movePointLeft(2));
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Cannot read amount '%s' field %d pos %d",
                    c, field, pos), pos);
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for AMOUNT field %d, pos %d", field, pos), pos);
		}
	}

    @Override
	public <T> IsoValue<BigDecimal> parseBinary(final int field, final byte[] buf,
                                            final int pos, final CustomField<T> custom)
            throws ParseException {
		char[] digits = new char[13];
		digits[10] = '.';
		int start = 0;
		for (int i = pos; i < pos + 6; i++) {
			digits[start++] = (char)(((buf[i] & 0xf0) >> 4) + 48);
			digits[start++] = (char)((buf[i] & 0x0f) + 48);
			if (start == 10) {
				start++;
			}
		}
		try {
			return new IsoValue<>(IsoType.AMOUNT, new BigDecimal(new String(digits)));
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Cannot read amount '%s' field %d pos %d",
                    new String(digits), field, pos), pos);
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for AMOUNT field %d, pos %d", field, pos), pos);
		}
	}

}
