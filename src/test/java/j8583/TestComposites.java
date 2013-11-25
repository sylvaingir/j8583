package j8583;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.codecs.CompositeField;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the CompositeField.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 17:43
 */
public class TestComposites {

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
    }

    @Test
    public void testEncodeBinary() {
        final CompositeField f = new CompositeField();
        f.setBinary(true);
        f.addValue(new IsoValue<String>(IsoType.ALPHA, "One", 5));
        Assert.assertArrayEquals(new byte[]{'O', 'n', 'e', 32, 32}, f.encodeBinaryField(f));
        f.addValue(new IsoValue<String>(IsoType.LLVAR, "Two"));
        Assert.assertArrayEquals(new byte[]{'O', 'n', 'e', ' ', ' ', 3, 'T', 'w', 'o'},
                f.encodeBinaryField(f));
        f.addValue(new IsoValue<Long>(IsoType.NUMERIC, 999l, 5));
        Assert.assertArrayEquals(new byte[]{'O', 'n', 'e', ' ', ' ', 3, 'T', 'w', 'o',
                0,9,(byte)0x99}, f.encodeBinaryField(f));
    }

}
