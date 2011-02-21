package com.solab.iso8583.parse;

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

	public <T extends Object> IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		if (custom == null) {
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
