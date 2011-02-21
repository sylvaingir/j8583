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

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse fields of type LLLVAR.
 * 
 * @author Enrique Zamudio
 */
public class LllvarParseInfo extends FieldParseInfo {

	
	public LllvarParseInfo() {
		super(IsoType.LLLVAR, 0);
	}

	public <T extends Object> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid position %d", pos), pos);
		}
		if (!(Character.isDigit(buf[pos]) && Character.isDigit(buf[pos+1]) && Character.isDigit(buf[pos+2]))) {
			throw new ParseException(String.format("Invalid LLLVAR length '%s' pos %d",
				new String(buf, pos, 3), pos), pos);
		}
		length = ((buf[pos] - 48) * 100) + ((buf[pos + 1] - 48) * 10) + (buf[pos + 2] - 48);
		if (length < 0) {
			throw new ParseException(String.format("Invalid LLLVAR length %d pos %d", length, pos), pos);
		}
		if (length+pos+3 > buf.length) {
			throw new ParseException(String.format("Insufficient data for LLLVAR field, pos %d", pos), pos);
		}
		String _v = length == 0 ? "" : new String(buf, pos + 3, length);
		if (custom == null) {
			return new IsoValue<String>(type, _v, length, null);
		} else {
			IsoValue<T> v = new IsoValue<T>(type, custom.decodeField(_v), length, custom);
			if (v.getValue() == null) {
				//problems decoding? return the string
				return new IsoValue<String>(type, _v, length, null);
			}
			return v;
		}
	}

	public <T extends Object> IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		length = ((buf[pos] & 0x0f) * 100) + (((buf[pos + 1] & 0xf0) >> 4) * 10) + (buf[pos + 1] & 0x0f);
		if (length < 0) {
			throw new ParseException(String.format("Invalid LLLVAR length %d pos %d", length, pos), pos);
		}
		if (length+pos+2 > buf.length) {
			throw new ParseException(String.format("Insufficient data for LLLVAR field, pos %d", pos), pos);
		}
		if (custom == null) {
			return new IsoValue<String>(type, new String(buf, pos + 2, length), null);
		} else {
			IsoValue<T> v = new IsoValue<T>(type, custom.decodeField(new String(buf, pos + 2, length)), custom);
			if (v.getValue() == null) {
				return new IsoValue<String>(type, new String(buf, pos + 2, length), null);
			}
			return v;
		}
	}

}
