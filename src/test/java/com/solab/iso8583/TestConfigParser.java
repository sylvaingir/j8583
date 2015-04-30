package com.solab.iso8583;

import com.solab.iso8583.codecs.CompositeField;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Test certain ConfigParser features.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 18:41
 */
public class TestConfigParser {

    private MessageFactory<IsoMessage> config(String path) throws IOException {
        MessageFactory<IsoMessage> mfact = new MessageFactory<>();
        mfact.setConfigPath(path);
        return mfact;
    }

    @Test
    public void testParser() throws IOException, ParseException {
        final MessageFactory<IsoMessage> mfact = config("config.xml");
        //Headers
        Assert.assertNotNull(mfact.getIsoHeader(0x800));
        Assert.assertNotNull(mfact.getIsoHeader(0x810));
        Assert.assertEquals(mfact.getIsoHeader(0x800), mfact.getIsoHeader(0x810));
        //Templates
        final IsoMessage m200 = mfact.getMessageTemplate(0x200);
        Assert.assertNotNull(m200);
        final IsoMessage m400 = mfact.getMessageTemplate(0x400);
        Assert.assertNotNull(m400);
        for (int i=2; i <89; i++) {
            IsoValue<?> v = m200.getField(i);
            if (v == null) {
                Assert.assertFalse(m400.hasField(i));
            } else {
                Assert.assertTrue(m400.hasField(i));
                Assert.assertEquals(v, m400.getField(i));
            }
        }
        Assert.assertFalse(m200.hasField(90));
        Assert.assertTrue(m400.hasField(90));
        Assert.assertTrue(m200.hasField(102));
        Assert.assertFalse(m400.hasField(102));

        //Parsing guides
        final String s800 = "0800201080000000000012345611251125";
        final String s810 = "08102010000002000000123456112500";
        IsoMessage m = mfact.parseMessage(s800.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertTrue(m.hasField(3));
        Assert.assertTrue(m.hasField(12));
        Assert.assertTrue(m.hasField(17));
        Assert.assertFalse(m.hasField(39));
        m = mfact.parseMessage(s810.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertTrue(m.hasField(3));
        Assert.assertTrue(m.hasField(12));
        Assert.assertFalse(m.hasField(17));
        Assert.assertTrue(m.hasField(39));
        //Composite fields
        final String compositeMsg = "12000040000000000000018ALPHA05LLVAR12345X";
        m = mfact.parseMessage(compositeMsg.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertTrue(m.hasField(10));
        CompositeField cf = m.getObjectValue(10);
        Assert.assertNotNull(cf.getField(0));
        Assert.assertNotNull(cf.getField(1));
        Assert.assertNotNull(cf.getField(2));
        Assert.assertNotNull(cf.getField(3));
        Assert.assertNull(cf.getField(4));
        Assert.assertEquals("ALPHA", cf.getField(0).getValue());
        Assert.assertEquals("LLVAR", cf.getField(1).getValue());
        Assert.assertEquals("12345", cf.getField(2).getValue());
        Assert.assertEquals("X", cf.getField(3).getValue());
        m = mfact.newMessage(0x1200);
        Assert.assertNotNull(m);
        Assert.assertTrue(m.hasField(10));
        Assert.assertFalse(m.hasField(1));
        Assert.assertFalse(m.hasField(2));
        Assert.assertFalse(m.hasField(3));
        Assert.assertFalse(m.hasField(4));
    }

    @Test //issue 34
    public void testMultilevelExtendParseGuides() throws IOException, ParseException {
        final MessageFactory<IsoMessage> mfact = config("issue34.xml");
        //Parse a 200
        final String m200 = "0200422000000880800001X1231235959123456101010202020TERMINAL484";
        final String m210 = "0210422000000A80800001X123123595912345610101020202099TERMINAL484";
        final String m400 = "0400422000000880800401X1231235959123456101010202020TERMINAL484001X";
        final String m410 = "0410422000000a80800801X123123595912345610101020202099TERMINAL484001X";
        IsoMessage m = mfact.parseMessage(m200.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertEquals("X", m.getObjectValue(2));
        Assert.assertEquals("123456", m.getObjectValue(11));
        Assert.assertEquals("TERMINAL", m.getObjectValue(41));
        Assert.assertEquals("484", m.getObjectValue(49));
        m = mfact.parseMessage(m210.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertEquals("X", m.getObjectValue(2));
        Assert.assertEquals("123456", m.getObjectValue(11));
        Assert.assertEquals("TERMINAL", m.getObjectValue(41));
        Assert.assertEquals("484", m.getObjectValue(49));
        Assert.assertEquals("99", m.getObjectValue(39));
        m = mfact.parseMessage(m400.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertEquals("X", m.getObjectValue(2));
        Assert.assertEquals("123456", m.getObjectValue(11));
        Assert.assertEquals("TERMINAL", m.getObjectValue(41));
        Assert.assertEquals("484", m.getObjectValue(49));
        Assert.assertEquals("X", m.getObjectValue(62));
        m = mfact.parseMessage(m410.getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertEquals("X", m.getObjectValue(2));
        Assert.assertEquals("123456", m.getObjectValue(11));
        Assert.assertEquals("TERMINAL", m.getObjectValue(41));
        Assert.assertEquals("484", m.getObjectValue(49));
        Assert.assertEquals("99", m.getObjectValue(39));
        Assert.assertEquals("X", m.getObjectValue(61));
    }
}
