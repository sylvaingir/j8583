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

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.HexCodec;

/** This class is used to parse fields of type LLLBIN.
 * 
 * @author Enrique Zamudio
 */
public class LllbinParseInfo extends FieldParseInfo {

	
	public LllbinParseInfo() {
		super(IsoType.LLLBIN, 0);
	}

	@Override
	public <T> IsoValue<?> parse(final int field, final byte[] buf,
                             final int pos, final CustomField<T> custom)
            throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid LLLBIN field %d pos %d",
                    field, pos), pos);
		} else if (pos+3 > buf.length) {
			throw new ParseException(String.format("Insufficient LLLBIN header field %d",
                    field), pos);
		}
		final int l = decodeLength(buf, pos, 3);
		if (l < 0) {
			throw new ParseException(String.format("Invalid LLLBIN length %d field %d pos %d",
                    l, field, pos), pos);
		} else if (l+pos+3 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for LLLBIN field %d, pos %d", field, pos), pos);
		}
		byte[] binval = l == 0 ? new byte[0] : HexCodec.hexDecode(new String(buf, pos + 3, l));
		if (custom == null) {
			return new IsoValue<byte[]>(type, binval, binval.length, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(
                    buf, pos + 3, l);
                return dec == null ? new IsoValue<byte[]>(type, binval, binval.length, null) :
                        new IsoValue<T>(type, dec, 0, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLBIN field %d, pos %d", field, pos), pos);
            }
		} else {
            try {
                T dec = custom.decodeField(
                    l == 0 ? "" : new String(buf, pos + 3, l));
                return dec == null ? new IsoValue<byte[]>(type, binval, binval.length, null) :
                        new IsoValue<T>(type, dec, l, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLBIN field %d, pos %d", field, pos), pos);
            }
		}
	}

	@Override
	public <T> IsoValue<?> parseBinary(final int field, final byte[] buf,
                                   final int pos, final CustomField<T> custom)
            throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin LLLBIN field %d pos %d",
                    field, pos), pos);
		} else if (pos+2 > buf.length) {
            throw new ParseException(String.format("Insufficient LLLBIN header field %d",
                             field), pos);
		}
		final int l = ((buf[pos] & 0x0f) * 100) + (((buf[pos + 1] & 0xf0) >> 4) * 10) + (buf[pos + 1] & 0x0f);
		if (l < 0) {
            throw new ParseException(String.format("Invalid LLLBIN length %d field %d pos %d",
                             l, field, pos), pos);
		}
		if (l+pos+2 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for bin LLLBIN field %d, pos %d requires %d, only %d available",
                    field, pos, l, buf.length-pos+1), pos);
		}
		byte[] _v = new byte[l];
		System.arraycopy(buf, pos+2, _v, 0, l);
		if (custom == null) {
			return new IsoValue<byte[]>(type, _v, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(
                    buf, pos + 2, l);
                return dec == null ? new IsoValue<byte[]>(type, _v, _v.length, null) :
                        new IsoValue<T>(type, dec, l, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLBIN field %d, pos %d", field, pos), pos);
            }
		} else {
            T dec = custom.decodeField(HexCodec.hexEncode(_v, 0, _v.length));
            return dec == null ? new IsoValue<byte[]>(type, _v, null) :
                    new IsoValue<T>(type, dec, custom);
		}
	}

}
