package com.solab.iso8583.util;

import org.junit.Assert;
import org.junit.Test;

public class TestHexCodec {

	public void encodeDecode(String hex) {
		byte[] buf = HexCodec.hexDecode(hex);
		Assert.assertEquals((hex.length() / 2) + (hex.length() % 2), buf.length);
		String reenc = HexCodec.hexEncode(buf, 0, buf.length);
		if (reenc.startsWith("0") && !hex.startsWith("0")) {
			Assert.assertEquals(reenc.substring(1), hex);
		} else {
			Assert.assertEquals(hex, reenc);
		}
	}

	@Test
	public void testCodec() {
		byte[] buf = HexCodec.hexDecode("A");
		Assert.assertEquals(0x0a, buf[0]);
		encodeDecode("A");
		encodeDecode("0123456789ABCDEF");
		buf = HexCodec.hexDecode("0123456789ABCDEF");
		Assert.assertEquals(1, buf[0]);
		Assert.assertEquals(0x23, buf[1]);
		Assert.assertEquals(0x45, buf[2]);
		Assert.assertEquals(0x67, buf[3]);
		Assert.assertEquals(0x89, (buf[4] & 0xff));
		Assert.assertEquals(0xab, (buf[5] & 0xff));
		Assert.assertEquals(0xcd, (buf[6] & 0xff));
		Assert.assertEquals(0xef, (buf[7] & 0xff));
		buf = HexCodec.hexDecode("ABC");
		Assert.assertEquals(0x0a, (buf[0] & 0xff));
		Assert.assertEquals(0xbc, (buf[1] & 0xff));
		encodeDecode("ABC");
	}

    @Test
    public void testPartial() {
        Assert.assertEquals("FF01", HexCodec.hexEncode(new byte[]{0, (byte)0xff, 1, 2, 3, 4},
                1, 2));
    }

}
