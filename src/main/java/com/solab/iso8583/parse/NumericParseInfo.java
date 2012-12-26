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

import java.math.BigInteger;
import java.text.ParseException;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse NUMERIC fields.
 * 
 * @author Enrique Zamudio
 */
public class NumericParseInfo extends AlphaNumericFieldParseInfo {

	public NumericParseInfo(int len) {
		super(IsoType.NUMERIC, len);
	}

	public IsoValue<Number> parseBinary(byte[] buf, int pos, CustomField<?> custom) throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin NUMERIC position %d", pos), pos);
		} else if (pos+(length/2) > buf.length) {
			throw new ParseException(String.format("Insufficient data for bin %s field of length %d, pos %d",
				type, length, pos), pos);
		}
		//A long covers up to 18 digits
		if (length < 19) {
			long l = 0;
			long power = 1L;
			for (int i = pos + (length / 2) + (length % 2) - 1; i >= pos; i--) {
				l += (buf[i] & 0x0f) * power;
				power *= 10L;
				l += ((buf[i] & 0xf0) >> 4) * power;
				power *= 10L;
			}
			return new IsoValue<Number>(IsoType.NUMERIC, l, length, null);
		} else {
			//Use a BigInteger
			char[] digits = new char[length];
			int start = 0;
            try {
                for (int i = pos; i < pos + (length / 2) + (length % 2); i++) {
                    digits[start++] = (char)(((buf[i] & 0xf0) >> 4) + 48);
                    digits[start++] = (char)((buf[i] & 0x0f) + 48);
                }
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format("Insufficient data for bin %s field of length %d, pos %d",
             				type, length, pos), pos);
            }
			return new IsoValue<Number>(IsoType.NUMERIC, new BigInteger(new String(digits)), length, null);
		}
	}

}
