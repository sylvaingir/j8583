package j8583;

import com.solab.iso8583.IsoMessage;
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
        Assert.assertNotNull(mfact.getIsoHeader(0x800));
        Assert.assertNotNull(mfact.getIsoHeader(0x810));
        Assert.assertEquals(mfact.getIsoHeader(0x800), mfact.getIsoHeader(0x810));
    }

}
