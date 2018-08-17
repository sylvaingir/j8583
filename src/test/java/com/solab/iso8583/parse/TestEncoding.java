package com.solab.iso8583.parse;

import com.solab.iso8583.IsoValue;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Test parsing of data with different encodings.
 *
 * @author Enrique Zamudio
 *         Date: 05/03/14 16:55
 */
public class TestEncoding {

    @Test
    public void windowsToUtf8() throws UnsupportedEncodingException, ParseException {
        final String data = "05ácido";
        final byte[] buf = data.getBytes("ISO-8859-1");
        final LlvarParseInfo parser = new LlvarParseInfo();
        parser.setCharacterEncoding("UTF-8");
        IsoValue<?> field = parser.parse(1, buf, 0, null);
        Assert.assertNotEquals(field.getValue(), data.substring(2));
        parser.setCharacterEncoding("ISO-8859-1");
        field = parser.parse(1, buf, 0, null);
        Assert.assertEquals(data.substring(2), field.getValue());
    }

}
