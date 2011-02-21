package com.solab.iso8583.parse;

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

	public <T extends Object> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid position %d", pos), pos);
		}
		if (pos+12 > buf.length) {
			throw new ParseException(String.format("Insufficient data for AMOUNT field, pos %d", pos), pos);
		}
		String c = new String(buf, pos, 12);
		try {
			return new IsoValue<BigDecimal>(type, new BigDecimal(c).movePointLeft(2), null);
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Cannot read amount '%s' pos %d", new String(c), pos), pos);
		}
	}

	public <T extends Object> IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<T> custom) throws ParseException {
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
			return new IsoValue<BigDecimal>(IsoType.AMOUNT, new BigDecimal(new String(digits)), null);
		} catch (NumberFormatException ex) {
			throw new ParseException(String.format("Cannot read amount '%s' pos %d", new String(digits), pos), pos);
		}
	}

}
