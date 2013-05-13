package j8583;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.LlbinParseInfo;
import com.solab.iso8583.parse.LllbinParseInfo;
import com.solab.iso8583.parse.LllvarParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * Test for EBCDIC support.
 *
 * @author Enrique Zamudio
 *         Date: 13/05/13 18:12
 */
public class TestEbcdic {

    private IsoValue<String> llvar = new IsoValue<String>(IsoType.LLVAR, "Testing, testing, 123");

    @Test
    public void testAscii() throws IOException, ParseException {
        llvar.setCharacterEncoding("UTF-8");
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        llvar.write(bout, false, false);
        byte[] buf = bout.toByteArray();
        Assert.assertEquals(50, buf[0]);
        Assert.assertEquals(49, buf[1]);
        final LlvarParseInfo parser = new LlvarParseInfo();
        parser.setCharacterEncoding("UTF-8");
        IsoValue<?> field = parser.parse(1, buf, 0, null);
        Assert.assertEquals(llvar.getValue(), field.getValue());
    }

    @Test
    public void testEbcdic() throws IOException, ParseException {
        llvar.setCharacterEncoding("Cp1047");
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        llvar.write(bout, false, true);
        byte[] buf = bout.toByteArray();
        Assert.assertEquals((byte)242, buf[0]);
        Assert.assertEquals((byte) 241, buf[1]);
        final LlvarParseInfo parser = new LlvarParseInfo();
        parser.setCharacterEncoding("Cp1047");
        parser.setForceStringDecoding(true);
        IsoValue<?> field = parser.parse(1, buf, 0, null);
        Assert.assertEquals(llvar.getValue(), field.getValue());
    }

    @Test
    public void testParsers() throws UnsupportedEncodingException, ParseException {
        final byte[] stringA = "A".getBytes("Cp1047");
        final LllvarParseInfo lllvar = new LllvarParseInfo();
        lllvar.setCharacterEncoding("Cp1047");
        lllvar.setForceStringDecoding(true);
        IsoValue<?> field = lllvar.parse(1, new byte[]{(byte)240, (byte)240, (byte)241, (byte)193}, 0, null);
        Assert.assertEquals(new String(stringA, "Cp1047"), field.getValue());

        final LllbinParseInfo lllbin = new LllbinParseInfo();
        lllbin.setCharacterEncoding("Cp1047");
        lllbin.setForceStringDecoding(true);
        field = lllbin.parse(1, new byte[]{(byte)240, (byte)240, (byte)242, 67, 49}, 0, null);
        Assert.assertArrayEquals(stringA, (byte[]) field.getValue());

        final LlbinParseInfo llbin = new LlbinParseInfo();
        llbin.setCharacterEncoding("Cp1047");
        llbin.setForceStringDecoding(true);
        field = llbin.parse(1, new byte[]{(byte)240, (byte)242, 67, 49}, 0, null);
        Assert.assertArrayEquals(stringA, (byte[]) field.getValue());
    }

    @Test
    public void testMessageType() {
        final IsoMessage msg = new IsoMessage();
        msg.setType(0x1100);
        msg.setBinaryBitmap(true);
        msg.setCharacterEncoding("Cp1047");
        final byte[] enc = msg.writeData();
        Assert.assertEquals(12, enc.length);
        Assert.assertEquals((byte)241, enc[0]);
        Assert.assertEquals((byte)241, enc[1]);
        Assert.assertEquals((byte)240, enc[2]);
        Assert.assertEquals((byte)240, enc[3]);
    }

}
