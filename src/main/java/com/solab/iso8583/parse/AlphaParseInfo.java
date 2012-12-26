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

/** This is the class used to parse ALPHA fields.
 * 
 * @author Enrique Zamudio
 */
public class AlphaParseInfo extends AlphaNumericFieldParseInfo {

	public AlphaParseInfo(int len) {
		super(IsoType.ALPHA, len);
	}

	public IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<?> custom)
	throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin ALPHA position %d", pos), pos);
		} else if (pos+length > buf.length) {
			throw new ParseException(String.format("Insufficient data for bin %s field of length %d, pos %d",
				type, length, pos), pos);
		}
        try {
            if (custom == null) {
                return new IsoValue<String>(type, new String(buf, pos, length, getCharacterEncoding()), length, null);
            } else {
                @SuppressWarnings({"unchecked", "rawtypes"})
                IsoValue<?> v = new IsoValue(type, custom.decodeField(new String(buf, pos, length, getCharacterEncoding())), length, custom);
                if (v.getValue() == null) {
                    return new IsoValue<String>(type, new String(buf, pos, length, getCharacterEncoding()), length, null);
                }
                return v;
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format("Insufficient data for bin %s field of length %d, pos %d",
         				type, length, pos), pos);
        }
	}

}
