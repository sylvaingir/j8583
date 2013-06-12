package j8583;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.*;
import com.solab.iso8583.util.HexCodec;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

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
    public void testMessageType() throws UnsupportedEncodingException, ParseException {
        final IsoMessage msg = new IsoMessage();
        msg.setType(0x1100);
        msg.setBinaryBitmap(true);
        msg.setCharacterEncoding("Cp1047");
        final byte[] enc = msg.writeData();
        Assert.assertEquals(12, enc.length);
        Assert.assertEquals((byte) 241, enc[0]);
        Assert.assertEquals((byte) 241, enc[1]);
        Assert.assertEquals((byte)240, enc[2]);
        Assert.assertEquals((byte) 240, enc[3]);
        MessageFactory<IsoMessage> mf = new MessageFactory<IsoMessage>();
        HashMap<Integer, FieldParseInfo> pmap = new HashMap<Integer, FieldParseInfo>();
        mf.setForceStringEncoding(true);
        mf.setUseBinaryBitmap(true);
        mf.setCharacterEncoding("Cp1047");
        mf.setParseMap(0x1100, pmap);
        IsoMessage m2 = mf.parseMessage(enc, 0);
        Assert.assertEquals(msg.getType(), m2.getType());
        //Now with text bitmap
        msg.setBinaryBitmap(false);
        msg.setForceStringEncoding(true);
        final byte[] enc2 = msg.writeData();
        Assert.assertEquals(20, enc2.length);
        mf.setUseBinaryBitmap(false);
        m2 = mf.parseMessage(enc2, 0);
        Assert.assertEquals(msg.getType(), m2.getType());
    }

    @Test
    public void testDate4() throws UnsupportedEncodingException, ParseException {
        final Date4ParseInfo parser = new Date4ParseInfo();
        parser.setCharacterEncoding("Cp1047");
        parser.setForceStringDecoding(true);
        final IsoValue<Date> v = parser.parse(1, new byte[]{(byte)240, (byte)241, (byte)242, (byte)245}, 0, null);
        Assert.assertEquals("01", String.format("%Tm", v.getValue()));
        Assert.assertEquals("25", String.format("%Td", v.getValue()));
    }

    @Test
    public void testDate10() throws UnsupportedEncodingException, ParseException {
        final Date10ParseInfo parser = new Date10ParseInfo();
        parser.setCharacterEncoding("Cp1047");
        parser.setForceStringDecoding(true);
        final IsoValue<Date> v = parser.parse(1, new byte[]{(byte)240, (byte)241, (byte)242, (byte)245, (byte)242, (byte)243, (byte)245, (byte)249, (byte)245, (byte)249}, 0, null);
        Assert.assertEquals("01", String.format("%Tm", v.getValue()));
        Assert.assertEquals("25", String.format("%Td", v.getValue()));
        Assert.assertEquals("23:59:59", String.format("%TT", v.getValue()));
    }

    @Test
    public void testDateExp() throws UnsupportedEncodingException, ParseException {
        final DateExpParseInfo parser = new DateExpParseInfo();
        parser.setCharacterEncoding("Cp1047");
        parser.setForceStringDecoding(true);
        final IsoValue<Date> v = parser.parse(1, new byte[]{(byte)241, (byte)247, (byte)241, (byte)242}, 0, null);
        Assert.assertEquals("12", String.format("%Tm", v.getValue()));
        Assert.assertEquals("2017", String.format("%TY", v.getValue()));
    }

    @Test
    public void testTime() throws UnsupportedEncodingException, ParseException {
        final TimeParseInfo parser = new TimeParseInfo();
        parser.setCharacterEncoding("Cp1047");
        parser.setForceStringDecoding(true);
        final IsoValue<Date> v = parser.parse(1, new byte[]{(byte)242, (byte)241, (byte)243, (byte)244, (byte)245, (byte)246}, 0, null);
        Assert.assertEquals("21:34:56", String.format("%TT", v.getValue()));
    }

    @Test
    public void testAmount() throws UnsupportedEncodingException, ParseException {
        final AmountParseInfo parser = new AmountParseInfo();
        parser.setCharacterEncoding("Cp1047");
        parser.setForceStringDecoding(true);
        IsoValue<BigDecimal> v = parser.parse(4, new byte[]{(byte)240, (byte)240, (byte)240, (byte)241, (byte)242, (byte)243, (byte)244, (byte)245, (byte)246, (byte)247, (byte)248, (byte)249}, 0, null);
        Assert.assertEquals(new BigDecimal("1234567.89"), v.getValue());
    }

    @Test
    public void testMessage() throws ParseException, UnsupportedEncodingException {
        final byte[] trama = HexCodec.hexDecode("f1f8f1f42030010002000000f0f0f0f0f0f0f0f1f5f9f5f5f1f3f0f6f1f2f1f1f2f9f0f8f8f3f1f8f0f0");
        MessageFactory<IsoMessage> mfact = new MessageFactory<IsoMessage>();
        mfact.setUseBinaryBitmap(true);
        HashMap<Integer, FieldParseInfo> pinfo = new HashMap<Integer, FieldParseInfo>();
        pinfo.put(3, new AlphaParseInfo(6));
        pinfo.put(11, new AlphaParseInfo(6));
        pinfo.put(12, new AlphaParseInfo(12));
        pinfo.put(24, new AlphaParseInfo(3));
        pinfo.put(39, new AlphaParseInfo(3));
        mfact.setParseMap(0x1814, pinfo);
        mfact.setCharacterEncoding("Cp1047");
        mfact.setForceStringEncoding(true);
        IsoMessage iso = mfact.parseMessage(trama, 0);
        Assert.assertNotNull(iso);
        Assert.assertEquals("000000", iso.getObjectValue(3));
        Assert.assertEquals("015955", iso.getObjectValue(11));
        Assert.assertEquals("130612112908", iso.getObjectValue(12));
        Assert.assertEquals("831", iso.getObjectValue(24));
        Assert.assertEquals("800", iso.getObjectValue(39));
    }

}
