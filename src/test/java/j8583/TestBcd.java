package j8583;

import com.solab.iso8583.util.Bcd;
import org.junit.Assert;
import org.junit.Test;

/**
 * BCD encoding tests.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 10:43
 */
public class TestBcd {

    @Test
    public void testEncoding() {
        byte[] buf = new byte[2];
        buf[0] = 1; buf[1]=1;
        Bcd.encode("00", buf);
        Assert.assertArrayEquals(new byte[]{0,         1}, buf);
        Bcd.encode("79", buf);
        Assert.assertArrayEquals(new byte[]{0x79,      1}, buf);
        Bcd.encode("80", buf);
        Assert.assertArrayEquals(new byte[]{(byte)0x80,1}, buf);
        Bcd.encode("99", buf);
        Assert.assertArrayEquals(new byte[]{(byte)0x99,1}, buf);
        Bcd.encode("100", buf);
        Assert.assertArrayEquals(new byte[]{1,         0}, buf);
        Bcd.encode("779", buf);
        Assert.assertArrayEquals(new byte[]{7,      0x79}, buf);
        Bcd.encode("999", buf);
        Assert.assertArrayEquals(new byte[]{9,(byte)0x99}, buf);
    }

}
