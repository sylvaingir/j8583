/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

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

/** This class is used to parse a field from a message buffer. There are concrete subclasses for each IsoType.
 * 
 * @author Enrique Zamudio
 */
public abstract class FieldParseInfo {

	protected IsoType type;
	protected int length;
	private String encoding = System.getProperty("file.encoding");

	/** Creates a new instance that parses a value of the specified type, with the specified length.
	 * The length is only useful for ALPHA and NUMERIC types.
	 * @param t The ISO type to be parsed.
	 * @param len The length of the data to be read (useful only for ALPHA and NUMERIC types). */
	public FieldParseInfo(IsoType t, int len) {
		if (t == null) {
			throw new IllegalArgumentException("IsoType cannot be null");
		}
		type = t;
		length = len;
	}

	public void setCharacterEncoding(String value) {
		encoding = value;
	}
	public String getCharacterEncoding() {
		return encoding;
	}

	/** Returns the specified length for the data to be parsed. */
	public int getLength() {
		return length;
	}

	/** Returns the data type for the data to be parsed. */
	public IsoType getType() {
		return type;
	}

	/** Parses the character data from the buffer and returns the
	 * IsoValue with the correct data type in it. */
	public abstract <T extends Object> IsoValue<?> parse(byte[] buf, int pos, CustomField<T> custom)
	throws ParseException, UnsupportedEncodingException;

	/** Parses binary data from the buffer, creating and returning an IsoValue of the configured
	 * type and length. */
	public abstract <T extends Object> IsoValue<?> parseBinary(byte[] buf, int pos, CustomField<T> custom)
	throws ParseException, UnsupportedEncodingException;

	/** Returns a new FieldParseInfo instance that can parse the specified type. */
	public static FieldParseInfo getInstance(IsoType t, int len, String encoding) {
		FieldParseInfo fpi = null;
		if (t == IsoType.ALPHA) {
			fpi = new AlphaParseInfo(len);
		} else if (t == IsoType.AMOUNT) {
			fpi = new AmountParseInfo();
		} else if (t == IsoType.BINARY) {
			fpi = new BinaryParseInfo(len);
		} else if (t == IsoType.DATE10) {
			fpi = new Date10ParseInfo();
		} else if (t == IsoType.DATE4) {
			fpi = new Date4ParseInfo();
		} else if (t == IsoType.DATE_EXP) {
			fpi = new DateExpParseInfo();
		} else if (t == IsoType.LLBIN) {
			fpi = new LlbinParseInfo();
		} else if (t == IsoType.LLLBIN) {
			fpi = new LllbinParseInfo();
		} else if (t == IsoType.LLLVAR) {
			fpi = new LllvarParseInfo();
		} else if (t == IsoType.LLVAR) {
			fpi = new LlvarParseInfo();
		} else if (t == IsoType.NUMERIC) {
			fpi = new NumericParseInfo(len);
		} else if (t == IsoType.TIME) {
			fpi = new TimeParseInfo();
		}
		if (fpi == null) {
	 		throw new IllegalArgumentException(String.format("Cannot parse type %s", t));
		}
		fpi.setCharacterEncoding(encoding);
		return fpi;
	}

}
