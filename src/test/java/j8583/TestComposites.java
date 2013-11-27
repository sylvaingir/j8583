package j8583;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.codecs.CompositeField;
import com.solab.iso8583.parse.AlphaParseInfo;
import com.solab.iso8583.parse.LlvarParseInfo;
import com.solab.iso8583.parse.NumericParseInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the CompositeField.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 17:43
 */
public class TestComposites {

    final String textData = "One  03Two00999X";
    final byte[] binaryData = new byte[]{'O', 'n', 'e', ' ', ' ', 3, 'T', 'w', 'o',
                    0, 9, (byte) 0x99, 'X'};

    @Test
    public void testEncodeText() {
        final CompositeField f = new CompositeField();
        f.addValue(new IsoValue<String>(IsoType.ALPHA, "One", 5));
        f.getValues().get(0).setCharacterEncoding("UTF-8");
        Assert.assertEquals("One  ", f.encodeField(f));
        f.addValue(new IsoValue<String>(IsoType.LLVAR, "Two"));
        f.getValues().get(1).setCharacterEncoding("UTF-8");
        Assert.assertEquals("One  03Two", f.encodeField(f));
        f.addValue(new IsoValue<Long>(IsoType.NUMERIC, 999l, 5));
        f.getValues().get(2).setCharacterEncoding("UTF-8");
        Assert.assertEquals("One  03Two00999", f.encodeField(f));
        f.addValue(new IsoValue<String>(IsoType.ALPHA, "X", 1));
        Assert.assertEquals(textData, f.encodeField(f));
    }

    @Test
    public void testEncodeBinary() {
        final CompositeField f = new CompositeField();
        f.addValue(new IsoValue<String>(IsoType.ALPHA, "One", 5));
        Assert.assertArrayEquals(new byte[]{'O', 'n', 'e', 32, 32}, f.encodeBinaryField(f));
        f.addValue(new IsoValue<String>(IsoType.LLVAR, "Two"));
        Assert.assertArrayEquals(new byte[]{'O', 'n', 'e', ' ', ' ', 3, 'T', 'w', 'o'},
                f.encodeBinaryField(f));
        f.addValue(new IsoValue<Long>(IsoType.NUMERIC, 999l, 5));
        f.addValue(new IsoValue<String>(IsoType.ALPHA, "X", 1));
        Assert.assertArrayEquals(binaryData, f.encodeBinaryField(f));
    }

    @Test
    public void testDecodeText() {
        final CompositeField dec = new CompositeField()
                .addParser(new AlphaParseInfo(5))
                .addParser(new LlvarParseInfo())
                .addParser(new NumericParseInfo(5))
                .addParser(new AlphaParseInfo(1));
        final CompositeField f = dec.decodeField(textData);
        Assert.assertNotNull(f);
        Assert.assertEquals(4, f.getValues().size());
        Assert.assertEquals("One  ", f.getValues().get(0).getValue());
        Assert.assertEquals("Two", f.getValues().get(1).getValue());
        Assert.assertEquals("00999", f.getValues().get(2).getValue());
        Assert.assertEquals("X", f.getValues().get(3).getValue());
    }

    @Test
    public void testDecodeBinary() {
        final CompositeField dec = new CompositeField()
                .addParser(new AlphaParseInfo(5))
                .addParser(new LlvarParseInfo())
                .addParser(new NumericParseInfo(5))
                .addParser(new AlphaParseInfo(1));
        final CompositeField f = dec.decodeBinaryField(binaryData, 0, binaryData.length);
        Assert.assertNotNull(f);
        Assert.assertEquals(4, f.getValues().size());
        Assert.assertEquals("One  ", f.getValues().get(0).getValue());
        Assert.assertEquals("Two", f.getValues().get(1).getValue());
        Assert.assertEquals(999l, f.getValues().get(2).getValue());
        Assert.assertEquals("X", f.getValues().get(3).getValue());
    }

}
