package com.solab.iso8583.codecs;

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.FieldParseInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * A codec to manage subfields inside a field of a certain type.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 11:25
 */
public class CompositeField implements CustomBinaryField<CompositeField> {

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

    public void setValue(List<IsoValue<?>> values) {
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
    public CompositeField decodeBinaryField(byte[] value, int offset, int length) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] encodeBinaryField(CompositeField value) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            for (IsoValue<?> v : values) {
                v.write(bout, binary, true);
            }
        } catch (IOException ex) {
            //shouldn't happen
        }
        return bout.toByteArray();
    }

    @Override
    public CompositeField decodeField(String value) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
