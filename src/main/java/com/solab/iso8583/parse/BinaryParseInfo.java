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

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.HexCodec;

/** This class is used to parse fields of type BINARY.
 * 
 * @author Enrique Zamudio
 */
public class BinaryParseInfo extends FieldParseInfo {

	
	public BinaryParseInfo(int len) {
		super(IsoType.BINARY, len);
	}

	@Override
	public IsoValue<?> parse(final int field, final byte[] buf, final int pos,
                             final CustomField<?> custom)
			throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid BINARY field %d position %d",
                    field, pos), pos);
		}
		if (pos+(length*2) > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for BINARY field %d of length %d, pos %d",
				field, length, pos), pos);
		}
		byte[] binval = HexCodec.hexDecode(new String(buf, pos, length*2));
		if (custom == null) {
			return new IsoValue<byte[]>(type, binval, binval.length, null);
		} else {
			@SuppressWarnings({"unchecked", "rawtypes"})
			IsoValue<?> v = new IsoValue(type, custom.decodeField(new String(buf, pos, length*2, getCharacterEncoding())), length, custom);
			if (v.getValue() == null) {
				return new IsoValue<byte[]>(type, binval, binval.length, null);
			}
			return v;
		}
	}

	@Override
	public IsoValue<?> parseBinary(final int field, final byte[] buf, final int pos,
                                   final CustomField<?> custom) throws ParseException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid BINARY field %d position %d",
                      field, pos), pos);
        }
        if (pos+length > buf.length) {
            throw new ParseException(String.format(
                      "Insufficient data for BINARY field %d of length %d, pos %d",
                field, length, pos), pos);
        }
		byte[] _v = new byte[length];
		System.arraycopy(buf, pos, _v, 0, length);
		if (custom == null) {
			return new IsoValue<byte[]>(type, _v, length, null);
		} else {
			@SuppressWarnings({"unchecked", "rawtypes"})
			IsoValue<?> v = new IsoValue(type, custom.decodeField(HexCodec.hexEncode(_v, 0, _v.length)), length, custom);
			if (v.getValue() == null) {
				return new IsoValue<byte[]>(type, _v, length, null);
			}
			return v;
		}
	}

}
