package com.solab.iso8583.codecs;

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.FieldParseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * A codec to manage subfields inside a field of a certain type.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 11:25
 */
public class CompositeField implements CustomBinaryField<CompositeField> {

    private static final Logger log = LoggerFactory.getLogger(CompositeField.class);
    private boolean binary;
    /** Stores the subfields. */
    private List<IsoValue<?>> values;
    /** Stores the parsers for the subfields. */
    private List<FieldParseInfo> parsers;

    public void setBinary(boolean flag) {
        binary = flag;
    }
    public boolean isBinary() {
        return binary;
    }

    public void setValues(List<IsoValue<?>> values) {
        this.values = values;
    }
    public List<IsoValue<?>> getValues() {
        return values;
    }
    public CompositeField addValue(IsoValue<?> v) {
        if (values == null) {
            values = new ArrayList<IsoValue<?>>(4);
        }
        values.add(v);
        return this;
    }

    public void setParsers(List<FieldParseInfo> fpis) {
        parsers = fpis;
    }
    public List<FieldParseInfo> getParsers() {
        return parsers;
    }
    public CompositeField addParser(FieldParseInfo fpi) {
        if (parsers == null) {
            parsers = new ArrayList<FieldParseInfo>(4);
        }
        parsers.add(fpi);
        return this;
    }

    @Override
    public CompositeField decodeBinaryField(byte[] buf, int offset, int length) {
        List<IsoValue<?>> vals = new ArrayList<IsoValue<?>>(parsers.size());
        int pos = 0;
        try {
            for (FieldParseInfo fpi : parsers) {
                IsoValue<?> v = fpi.parseBinary(0, buf, pos, null);
                if (v != null) {
                    if (v.getType() == IsoType.NUMERIC || v.getType() == IsoType.DATE10
                            || v.getType() == IsoType.DATE4 || v.getType() == IsoType.DATE_EXP
                            || v.getType() == IsoType.AMOUNT || v.getType() == IsoType.TIME) {
                        pos += (v.getLength() / 2) + (v.getLength() % 2);
                    } else {
                        pos += v.getLength();
                    }
                    if (v.getType() == IsoType.LLVAR || v.getType() == IsoType.LLBIN) {
                        pos++;
                    } else if (v.getType() == IsoType.LLLVAR || v.getType() == IsoType.LLLBIN) {
                        pos+=2;
                    }
                    vals.add(v);
                }
            }
            final CompositeField f = new CompositeField();
            f.setValues(vals);
            return f;
        } catch (ParseException ex) {
            log.error("Decoding binary CompositeField", ex);
            return null;
        } catch (UnsupportedEncodingException ex) {
            log.error("Decoding binary CompositeField", ex);
            return null;
        }
    }

    @Override
    public CompositeField decodeField(String value) {
        List<IsoValue<?>> vals = new ArrayList<IsoValue<?>>(parsers.size());
        byte[] buf = value.getBytes();
        int pos = 0;
        try {
            for (FieldParseInfo fpi : parsers) {
                IsoValue<?> v = fpi.parse(0, buf, pos, null);
                if (v != null) {
                    pos += v.toString().getBytes(fpi.getCharacterEncoding()).length;
                    if (v.getType() == IsoType.LLVAR || v.getType() == IsoType.LLBIN) {
                        pos+=2;
                    } else if (v.getType() == IsoType.LLLVAR || v.getType() == IsoType.LLLBIN) {
                        pos+=3;
                    }
                    vals.add(v);
                }
            }
            final CompositeField f = new CompositeField();
            f.setValues(vals);
            return f;
        } catch (ParseException ex) {
            log.error("Decoding CompositeField", ex);
            return null;
        } catch (UnsupportedEncodingException ex) {
            log.error("Decoding CompositeField", ex);
            return null;
        }
    }

    @Override
    public byte[] encodeBinaryField(CompositeField value) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            for (IsoValue<?> v : values) {
                v.write(bout, binary, true);
            }
        } catch (IOException ex) {
            log.error("Encoding binary CompositeField", ex);
            //shouldn't happen
        }
        return bout.toByteArray();
    }

    @Override
    public String encodeField(CompositeField value) {
        if (binary)return null;
        final byte[] buf = encodeBinaryField(value);
        String encoding = null;
        for (IsoValue<?> v : values) {
            if (encoding == null)encoding = v.getCharacterEncoding();
        }
        try {
            return new String(buf, encoding==null?"UTF-8":encoding);
        } catch (UnsupportedEncodingException ex) {
            return new String(buf);
        }
    }

}
