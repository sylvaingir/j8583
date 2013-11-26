package j8583;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Test certain ConfigParser features.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 18:41
 */
public class TestConfigParser {

    @Test
    public void testParser() throws IOException {
        MessageFactory<IsoMessage> mfact = new MessageFactory<IsoMessage>();
        mfact.setConfigPath("config.xml");
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
    }

}
