package com.solab.iso8583.parse;

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.HexCodec;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Blabla.
 *
 * @author Enrique Zamudio
 *         Date: 19/02/15 18:29
 */
public class LlllbinParseInfo  extends FieldParseInfo {


	public LlllbinParseInfo() {
		super(IsoType.LLLLBIN, 0);
	}

	@Override
	public <T> IsoValue<?> parse(final int field, final byte[] buf,
                             final int pos, final CustomField<T> custom)
            throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid LLLLBIN field %d pos %d",
                    field, pos), pos);
		} else if (pos+4 > buf.length) {
			throw new ParseException(String.format("Insufficient LLLLBIN header field %d",
                    field), pos);
		}
		final int l = decodeLength(buf, pos, 4);
		if (l < 0) {
			throw new ParseException(String.format("Invalid LLLLBIN length %d field %d pos %d",
                    l, field, pos), pos);
		} else if (l+pos+4 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for LLLLBIN field %d, pos %d", field, pos), pos);
		}
		byte[] binval = l == 0 ? new byte[0] : HexCodec.hexDecode(new String(buf, pos + 4, l));
		if (custom == null) {
			return new IsoValue<>(type, binval, binval.length, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(
                    buf, pos + 4, l);
                return dec == null ? new IsoValue<>(type, binval, binval.length, null) :
                        new IsoValue<>(type, dec, 0, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLLBIN field %d, pos %d", field, pos), pos);
            }
		} else {
            try {
                T dec = custom.decodeField(
                    l == 0 ? "" : new String(buf, pos + 4, l));
                return dec == null ? new IsoValue<>(type, binval, binval.length, null) :
                        new IsoValue<>(type, dec, l, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLLBIN field %d, pos %d", field, pos), pos);
            }
		}
	}

	@Override
	public <T> IsoValue<?> parseBinary(final int field, final byte[] buf,
                                   final int pos, final CustomField<T> custom)
            throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin LLLLBIN field %d pos %d",
                    field, pos), pos);
		} else if (pos+2 > buf.length) {
            throw new ParseException(String.format("Insufficient LLLLBIN header field %d",
                             field), pos);
		}
		final int l = ((buf[pos] & 0xf0) * 1000) + ((buf[pos] & 0x0f) * 100)
                + (((buf[pos + 1] & 0xf0) >> 4) * 10) + (buf[pos + 1] & 0x0f);
		if (l < 0) {
            throw new ParseException(String.format("Invalid LLLLBIN length %d field %d pos %d",
                             l, field, pos), pos);
		}
		if (l+pos+2 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for bin LLLLBIN field %d, pos %d requires %d, only %d available",
                    field, pos, l, buf.length-pos+1), pos);
		}
		byte[] _v = new byte[l];
		System.arraycopy(buf, pos+2, _v, 0, l);
		if (custom == null) {
			return new IsoValue<>(type, _v, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(
                    buf, pos + 2, l);
                return dec == null ? new IsoValue<>(type, _v, _v.length, null) :
                        new IsoValue<T>(type, dec, l, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLLBIN field %d, pos %d", field, pos), pos);
            }
		} else {
            T dec = custom.decodeField(HexCodec.hexEncode(_v, 0, _v.length));
            return dec == null ? new IsoValue<>(type, _v, null) :
                    new IsoValue<>(type, dec, custom);
		}
	}
}
