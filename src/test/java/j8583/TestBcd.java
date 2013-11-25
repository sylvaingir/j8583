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

    @Test
    public void testDecoding() {
        byte[] buf = new byte[2];
        Assert.assertEquals(0, Bcd.decodeToLong(buf, 0, 1));
        Assert.assertEquals(0, Bcd.decodeToLong(buf, 0, 2));
        Assert.assertEquals(0, Bcd.decodeToLong(buf, 0, 3));
        Assert.assertEquals(0, Bcd.decodeToLong(buf, 0, 4));
        buf[0]=0x79;
        Assert.assertEquals(79, Bcd.decodeToLong(buf, 0, 2));
        buf[0]=(byte)0x80;
        Assert.assertEquals(80, Bcd.decodeToLong(buf, 0, 2));
        buf[0]=(byte)0x99;
        Assert.assertEquals(99, Bcd.decodeToLong(buf, 0, 2));
        buf[0]=1;
        Assert.assertEquals(100, Bcd.decodeToLong(buf,0,4));
        buf[1]=0x79;
        Assert.assertEquals(179, Bcd.decodeToLong(buf,0,4));
        buf[1]=(byte)0x99;
        Assert.assertEquals(199, Bcd.decodeToLong(buf,0,4));
        buf[0]=9;
        Assert.assertEquals(999, Bcd.decodeToLong(buf,0,4));
    }

}
