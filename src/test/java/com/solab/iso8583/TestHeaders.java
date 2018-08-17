package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Test ISO headers.
 *
 * @author Enrique Zamudio
 *         Date: 01/07/16 8:21 AM
 */
public class TestHeaders {

    private MessageFactory<IsoMessage> mf;

   	@Before
   	public void init() throws IOException {
   		mf = new MessageFactory<>();
   		mf.setCharacterEncoding("UTF-8");
   		mf.setConfigPath("config.xml");
   	}

    @Test
    public void testBinaryHeader() throws Exception {
        IsoMessage m = mf.newMessage(0x280);
        Assert.assertNotNull(m.getBinaryIsoHeader());
        byte[] buf = m.writeData();
        Assert.assertEquals(4+4+16+2, buf.length);
        for (int i=0; i < 4; i++) {
            Assert.assertEquals(buf[i], (byte)0xff);
        }
        Assert.assertEquals(buf[4], 0x30);
        Assert.assertEquals(buf[5], 0x32);
        Assert.assertEquals(buf[6], 0x38);
        Assert.assertEquals(buf[7], 0x30);
        //Then parse and check the header is binary 0xffffffff
        m = mf.parseMessage(buf, 4, true);
        Assert.assertNull(m.getIsoHeader());
        buf = m.getBinaryIsoHeader();
        Assert.assertNotNull(buf);
        for (int i=0; i < 4; i++) {
            Assert.assertEquals(buf[i], (byte)0xff);
        }
        Assert.assertEquals(0x280, m.getType());
        Assert.assertTrue(m.hasField(3));
    }

}
