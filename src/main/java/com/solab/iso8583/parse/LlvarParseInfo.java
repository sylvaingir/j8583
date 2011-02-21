package com.solab.iso8583.parse;

import java.text.ParseException;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse fields of type LLVAR.
 * 
 * @author Enrique Zamudio
 */
public class LlvarParseInfo extends FieldParseInfo {

	public LlvarParseInfo() {
		super(IsoType.LLVAR, 0);
	}

	public <T extends Object> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid position %d", pos), pos);
		}
		length = ((buf[pos] - 48) * 10) + (buf[pos + 1] - 48);
		if (length < 0) {
			throw new ParseException(String.format("Invalid LLVAR length %d pos %d", length, pos), pos);
		}
		if (pos+2 > buf.length || length+pos+2 > buf.length) {
			throw new ParseException(String.format("Insufficient data for LLVAR field, pos %d", pos), pos);
		}
		String _v = length == 0 ? "" : new String(buf, pos + 2, length);
		if (custom == null) {
			return new IsoValue<String>(type, _v, length, null);
		} else {
			IsoValue<T> v = new IsoValue<T>(type, custom.decodeField(_v), length, custom);
			if (v.getValue() == null) {
				return new IsoValue<String>(type, _v, length, null);
			}
			return v;
		}
	}

	public <T extends Object> IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		
		length = (((buf[pos] & 0xf0) >> 4) * 10) + (buf[pos] & 0x0f);
		if (length < 0) {
			throw new ParseException(String.format("Invalid LLVAR length %d pos %d", length, pos), pos);
		}
		if (pos+1 > buf.length || length+pos+1 > buf.length) {
			throw new ParseException(String.format("Insufficient data for LLVAR field, pos %d", pos), pos);
		}
		if (custom == null) {
			return new IsoValue<String>(type, new String(buf, pos + 1, length), null);
		} else {
			IsoValue<T> v = new IsoValue<T>(type, custom.decodeField(new String(buf, pos + 1, length)), custom);
			if (v.getValue() == null) {
				return new IsoValue<String>(type, new String(buf, pos + 1, length), null);
			}
			return v;
		}
	}

}
