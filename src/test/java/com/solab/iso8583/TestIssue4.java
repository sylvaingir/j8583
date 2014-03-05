package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;

/**
 * Tests for chochos/j8583#4.
 *
 * @author Enrique Zamudio
 *         Date: 18/01/13 10:13
 */
public class TestIssue4 {

    @Test
    public void testTextBitmap() throws IOException, ParseException {
        MessageFactory<IsoMessage> tmf = new MessageFactory<IsoMessage>();
        ConfigParser.configureFromClasspathConfig(tmf, "issue4.xml");
        IsoMessage tm = tmf.newMessage(0x800);
        final ByteBuffer bb = tm.writeToBuffer(2);
        Assert.assertEquals("Wrong message length for new TXT",
                70, bb.array().length);
        Assert.assertEquals(68, bb.getShort());

        MessageFactory<IsoMessage> tmfp = new MessageFactory<IsoMessage>();
        ConfigParser.configureFromClasspathConfig(tmfp, "issue4.xml");
        byte[] buf2 = new byte[bb.remaining()];
        bb.get(buf2);
        tm = tmfp.parseMessage(buf2, 0);
        final ByteBuffer bbp = tm.writeToBuffer(2);
        Assert.assertArrayEquals("Parsed-reencoded TXT differs from original",
                bb.array(), bbp.array());
    }

    @Test
    public void testBinaryBitmap() throws IOException, ParseException {
        MessageFactory<IsoMessage> mf = new MessageFactory<IsoMessage>();
        ConfigParser.configureFromClasspathConfig(mf, "issue4.xml");
        IsoMessage bm = mf.getMessageTemplate(0x800);
        bm.setBinaryBitmap(true);
        final ByteBuffer bb = bm.writeToBuffer(2);
        Assert.assertEquals("Wrong message length for new BIN", 62, bb.array().length);
        Assert.assertEquals(60, bb.getShort());

        MessageFactory<IsoMessage> mfp = new MessageFactory<IsoMessage>();
        mfp.setUseBinaryBitmap(true);
        ConfigParser.configureFromClasspathConfig(mfp, "issue4.xml");
        byte[] buf2 = new byte[bb.remaining()];
        bb.get(buf2);
        bm = mfp.parseMessage(buf2, 0);
        Assert.assertTrue("Parsed message should have binary bitmap flag set",
                bm.isBinaryBitmap());
        Assert.assertFalse(bm.isBinary());
        final ByteBuffer bbp = bm.writeToBuffer(2);
        Assert.assertArrayEquals("Parsed-reencoded BIN differs from original",
                bb.array(), bbp.array());
    }

}
