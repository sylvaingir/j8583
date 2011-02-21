package com.solab.iso8583.parse;

import java.text.ParseException;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This is the common abstract superclass to parse ALPHA and NUMERIC field types.
 * 
 * @author Enrique Zamudio
 */
public abstract class AlphaNumericFieldParseInfo extends FieldParseInfo {

	public AlphaNumericFieldParseInfo(IsoType t, int len) {
		super(t, len);
	}

	public <T extends Object> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid position %d", pos), pos);
		}
		if (pos+length > buf.length) {
			throw new ParseException(String.format("Insufficient data for %s field of length %d, pos %d",
				type, length, pos), pos);
		}
		if (custom == null) {
			System.out.printf("Parseando tipo %s largo %s pos %s: '%s'%n", type, length, pos, new String(buf, pos, length));
			return new IsoValue<String>(type, new String(buf, pos, length), length, null);
		} else {
			IsoValue<T> v = new IsoValue<T>(type, custom.decodeField(new String(buf, pos, length)), length, custom);
			if (v.getValue() == null) {
				return new IsoValue<String>(type, new String(buf, pos, length), length, null);
			}
			return v;
		}
	}

}
