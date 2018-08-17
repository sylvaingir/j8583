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
package com.solab.iso8583;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;

import com.solab.iso8583.util.Bcd;
import com.solab.iso8583.util.HexCodec;

/** Represents a value that is stored in a field inside an ISO8583 message.
 * It can format the value when the message is generated.
 * Some values have a fixed length, other values require a length to be specified
 * so that the value can be padded to the specified length. LLVAR and LLLVAR
 * values do not need a length specification because the length is calculated
 * from the stored value.
 * 
 * @author Enrique Zamudio
 */
public class IsoValue<T> implements Cloneable {

	private IsoType type;
	private T value;
	private CustomField<T> encoder;
	private int length;
	private String encoding;
    private TimeZone tz;

	public IsoValue(IsoType t, T value) {
		this(t, value, null);
	}

	/** Creates a new instance that stores the specified value as the specified type.
	 * Useful for storing LLVAR or LLLVAR types, as well as fixed-length value types
	 * like DATE10, DATE4, AMOUNT, etc.
	 * @param t the ISO type.
	 * @param value The value to be stored.
	 * @param custom An optional CustomField to encode/decode a custom value.
	 */
	public IsoValue(IsoType t, T value, CustomField<T> custom) {
		if (t.needsLength()) {
			throw new IllegalArgumentException("Fixed-value types must use constructor that specifies length");
		}
		encoder = custom;
		type = t;
		this.value = value;
		if (type == IsoType.LLVAR || type == IsoType.LLLVAR || type == IsoType.LLLLVAR) {
			if (custom == null) {
				length = value.toString().length();
			} else {
				String enc = custom.encodeField(value);
				if (enc == null) {
					enc = value == null ? "" : value.toString();
				}
				length = enc.length();
			}
			if (t == IsoType.LLVAR && length > 99) {
				throw new IllegalArgumentException("LLVAR can only hold values up to 99 chars");
			} else if (t == IsoType.LLLVAR && length > 999) {
				throw new IllegalArgumentException("LLLVAR can only hold values up to 999 chars");
			} else if (t == IsoType.LLLLVAR && length > 9999) {
                throw new IllegalArgumentException("LLLLVAR can only hold values up to 9999 chars");
            }
		} else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
			if (custom == null) {
				if (value instanceof byte[]) {
					length = ((byte[])value).length;
				} else {
					length = value.toString().length() / 2 + (value.toString().length() % 2);
				}
            } else if (custom instanceof CustomBinaryField) {
                length = ((CustomBinaryField<T>)custom).encodeBinaryField(value).length;
			} else {
				String enc = custom.encodeField(value);
				if (enc == null) {
					enc = value == null ? "" : value.toString();
				}
				length = enc.length();
			}
			if (t == IsoType.LLBIN && length > 99) {
				throw new IllegalArgumentException("LLBIN can only hold values up to 99 chars");
			} else if (t == IsoType.LLLBIN && length > 999) {
				throw new IllegalArgumentException("LLLBIN can only hold values up to 999 chars");
            } else if (t == IsoType.LLLLBIN && length > 9999) {
                throw new IllegalArgumentException("LLLLBIN can only hold values up to 9999 chars");
			}
		} else {
			length = type.getLength();
		}
	}

	public IsoValue(IsoType t, T val, int len) {
		this(t, val, len, null);
	}

	/** Creates a new instance that stores the specified value as the specified type.
	 * Useful for storing fixed-length value types.
	 * @param t The ISO8583 type for this field.
	 * @param val The value to store in the field.
	 * @param len The length for the value.
	 * @param custom An optional CustomField to encode/decode a custom value.
	 */
	public IsoValue(IsoType t, T val, int len, CustomField<T> custom) {
		type = t;
		value = val;
		length = len;
		encoder = custom;
		if (length == 0 && t.needsLength()) {
			throw new IllegalArgumentException(String.format("Length must be greater than zero for type %s (value '%s')", t, val));
		} else if (t == IsoType.LLVAR || t == IsoType.LLLVAR || t == IsoType.LLLLVAR) {
			if (len == 0) {
				length = custom == null ? val.toString().length() : custom.encodeField(value).length();
			}
			if (t == IsoType.LLVAR && length > 99) {
				throw new IllegalArgumentException("LLVAR can only hold values up to 99 chars");
			} else if (t == IsoType.LLLVAR && length > 999) {
				throw new IllegalArgumentException("LLLVAR can only hold values up to 999 chars");
            } else if (t == IsoType.LLLLVAR && length > 9999) {
                throw new IllegalArgumentException("LLLLVAR can only hold values up to 9999 chars");
			}
		} else if (t == IsoType.LLBIN || t == IsoType.LLLBIN || t == IsoType.LLLLBIN) {
			if (len == 0) {
                if (custom == null) {
                    length = ((byte[])val).length;
                } else if (custom instanceof CustomBinaryField) {
                    length = ((CustomBinaryField<T>)custom).encodeBinaryField(value).length;
                } else {
                    length = custom.encodeField(value).length();
                }
				length = custom == null ? ((byte[])val).length : custom.encodeField(value).length();
			}
			if (t == IsoType.LLBIN && length > 99) {
				throw new IllegalArgumentException("LLBIN can only hold values up to 99 chars");
			} else if (t == IsoType.LLLBIN && length > 999) {
				throw new IllegalArgumentException("LLLBIN can only hold values up to 999 chars");
            } else if (t == IsoType.LLLLBIN && length > 9999) {
                throw new IllegalArgumentException("LLLLBIN can only hold values up to 9999 chars");
			}
		}
	}

	/** Returns the ISO type to which the value must be formatted. */
	public IsoType getType() {
		return type;
	}

	/** Returns the length of the stored value, of the length of the formatted value
	 * in case of NUMERIC or ALPHA. It doesn't include the field length header in case
	 * of LLVAR or LLLVAR. */
	public int getLength() {
		return length;
	}

	/** Returns the stored value without any conversion or formatting. */
	public T getValue() {
		return value;
	}

	public void setCharacterEncoding(String value) {
		encoding = value;
	}
	public String getCharacterEncoding() {
		return encoding;
	}

    /** Sets the timezone, useful for date fields. */
    public void setTimeZone(TimeZone value) {
        tz = value;
    }
    public TimeZone getTimeZone() {
        return tz;
    }

	/** Returns the formatted value as a String. The formatting depends on the type of the
	 * receiver. */
	public String toString() {
		if (value == null) {
			return "ISOValue<null>";
		}
		if (type == IsoType.NUMERIC || type == IsoType.AMOUNT) {
			if (type == IsoType.AMOUNT) {
				if (value instanceof BigDecimal) {
					return type.format((BigDecimal) value, 12);
				} else {
					return type.format(value.toString(), 12);
				}
            } else if (value instanceof BigInteger) {
                return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length);
			} else if (value instanceof Number) {
				return type.format(((Number)value).longValue(), length);
			} else {
				return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length);
			}
		} else if (type == IsoType.ALPHA) {
			return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length);
		} else if (type == IsoType.LLVAR || type == IsoType.LLLVAR || type == IsoType.LLLLVAR) {
			return encoder == null ? value.toString() : encoder.encodeField(value);
		} else if (value instanceof Date) {
			return type.format((Date)value, tz);
		} else if (type == IsoType.BINARY) {
			if (value instanceof byte[]) {
                final byte[] _v = (byte[])value;
				return type.format(encoder == null ? HexCodec.hexEncode(_v, 0, _v.length) : encoder.encodeField(value), length * 2);
			} else {
				return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length * 2);
			}
		} else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
			if (value instanceof byte[]) {
                final byte[] _v = (byte[])value;
				return encoder == null ? HexCodec.hexEncode(_v, 0, _v.length) : encoder.encodeField(value);
			} else {
				final String _s = encoder == null ? value.toString() : encoder.encodeField(value);
				return (_s.length() % 2 == 1) ? String.format("0%s", _s) : _s;
			}
		}
		return encoder == null ? value.toString() : encoder.encodeField(value);
	}

	/** Returns a copy of the receiver that references the same value object. */
	@SuppressWarnings("unchecked")
	public IsoValue<T> clone() {
		try {
			return (IsoValue<T>)super.clone();
		} catch (CloneNotSupportedException ex) {
			return null;
		}
	}

	/** Returns true of the other object is also an IsoValue and has the same type and length,
	 * and if other.getValue().equals(getValue()) returns true. */
	public boolean equals(Object other) {
		if (other == null || !(other instanceof IsoValue<?>)) {
			return false;
		}
		IsoValue<?> comp = (IsoValue<?>)other;
		return (comp.getType() == getType() && comp.getValue().equals(getValue())
				&& comp.getLength() == getLength());
	}

	@Override
	public int hashCode() {
		return value == null ? 0 : toString().hashCode();
	}

	/** Returns the CustomField encoder for this value. */
	public CustomField<T> getEncoder() {
		return encoder;
	}

    protected void writeLengthHeader(final int l, final OutputStream outs, final IsoType type,
                                     final boolean binary, final boolean forceStringEncoding)
            throws IOException {
        final int digits;
        if (type == IsoType.LLLLBIN || type == IsoType.LLLLVAR) {
            digits = 4;
        } else if (type == IsoType.LLLBIN || type == IsoType.LLLVAR) {
            digits = 3;
        } else {
            digits = 2;
        }
        if (binary) {
            if (digits == 4) {
                outs.write((((l % 10000) / 1000) << 4) | ((l % 1000)/100));
            } else if (digits == 3) {
                outs.write(l / 100); //00 to 09 automatically in BCD
            }
            //BCD encode the rest of the length
            outs.write((((l % 100) / 10) << 4) | (l % 10));
        } else if (forceStringEncoding) {
            String lhead = Integer.toString(l);
            final int ldiff = digits - lhead.length();
            if (ldiff == 1) {
                lhead = '0' + lhead;
            } else if (ldiff == 2) {
                lhead = "00" + lhead;
            } else if (ldiff == 3) {
                lhead = "000" + lhead;
            }
            outs.write(encoding == null ? lhead.getBytes():lhead.getBytes(encoding));
        } else {
            //write the length in ASCII
            if (digits == 4) {
                outs.write((l/1000)+48);
                outs.write(((l%1000)/100)+48);
            } else if (digits == 3) {
                outs.write((l / 100) + 48);
            }
            if (l >= 10) {
                outs.write(((l % 100) / 10) + 48);
            } else {
                outs.write(48);
            }
            outs.write((l % 10) + 48);
        }
    }

	/** Writes the formatted value to a stream, with the length header
	 * if it's a variable length type.
     * @param outs The stream to which the value will be written.
     * @param binary Specifies whether the value should be written in binary or text format.
     * @param forceStringEncoding When using text format, force the encoding of length headers
     * for variable-length fields to be done with the proper character encoding. When false,
     * the length headers are encoded as ASCII; this used to be the only behavior. */
	public void write(final OutputStream outs, final boolean binary, final boolean forceStringEncoding) throws IOException {
		if (type == IsoType.LLLVAR || type == IsoType.LLVAR || type == IsoType.LLLLVAR) {
            writeLengthHeader(length, outs, type, binary, forceStringEncoding);
		} else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
            writeLengthHeader(binary ? length : length*2, outs, type, binary, forceStringEncoding);
		} else if (binary) {
			//numeric types in binary are coded like this
			byte[] buf = null;
			if (type == IsoType.NUMERIC) {
				buf = new byte[(length / 2) + (length % 2)];
			} else if (type == IsoType.AMOUNT) {
				buf = new byte[6];
			} else if (type == IsoType.DATE10 || type == IsoType.DATE4 ||
					type == IsoType.DATE_EXP || type == IsoType.TIME || type == IsoType.DATE12) {
				buf = new byte[length / 2];
			}
			//Encode in BCD if it's one of these types
			if (buf != null) {
				Bcd.encode(toString(), buf);
				outs.write(buf);
				return;
			}
		}
		if (binary && (type == IsoType.BINARY || type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN)) {
			int missing = 0;
			if (value instanceof byte[]) {
				outs.write((byte[])value);
				missing = length - ((byte[])value).length;
            } else if (encoder instanceof CustomBinaryField) {
                byte[] binval = ((CustomBinaryField<T>) encoder).encodeBinaryField(value);
                outs.write(binval);
                missing = length - binval.length;
			} else {
				byte[] binval = HexCodec.hexDecode(value.toString());
				outs.write(binval);
				missing = length - binval.length;
			}
			if (type == IsoType.BINARY && missing > 0) {
				for (int i = 0; i < missing; i++) {
					outs.write(0);
				}
			}
		} else {
			outs.write(encoding == null ? toString().getBytes() : toString().getBytes(encoding));
		}
	}

}
