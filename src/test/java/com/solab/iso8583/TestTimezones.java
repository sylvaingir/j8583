package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.TimeZone;

/**
 * Test timezone assignment in date/time fields.
 *
 * @author Enrique Zamudio
 * Date: 4/17/18 11:50 AM
 */
public class TestTimezones {

    private final TimeZone utc = TimeZone.getTimeZone("UTC");
    private final TimeZone gmt5 = TimeZone.getTimeZone("GMT-500");
    private MessageFactory<IsoMessage> mf;

   	@Before
   	public void init() throws IOException {
        mf = new MessageFactory<>();
        mf.setCharacterEncoding("UTF-8");
        mf.setConfigPath("timezones.xml");
        mf.setAssignDate(true);
   	}

    @Test
    public void testTemplate() {
        IsoMessage t = mf.getMessageTemplate(0x100);
        Assert.assertTrue(t.hasEveryField(7, 12, 13));
        Assert.assertEquals(utc, t.getField(7).getTimeZone());
        Assert.assertEquals(gmt5, t.getField(12).getTimeZone());
        Assert.assertNull(t.getField(13).getTimeZone());
        t = mf.newMessage(0x100);
        Assert.assertTrue(t.hasField(7));
        Assert.assertEquals(utc, t.getField(7).getTimeZone());
    }

    @Test
    public void testParsingGuide() throws ParseException, IOException {
        String trama = "011002180000000000001231112233112233112233";
        IsoMessage m = mf.parseMessage(trama.getBytes(), 0);
        Assert.assertTrue(m.hasEveryField(7, 12, 13));
        Assert.assertEquals(utc, m.getField(7).getTimeZone());
        Assert.assertEquals(gmt5, m.getField(12).getTimeZone());
        Assert.assertNull(m.getField(13).getTimeZone());
    }

}
