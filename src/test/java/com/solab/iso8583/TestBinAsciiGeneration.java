package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.Test;
import org.junit.Assert;

public class TestBinAsciiGeneration {
    @Test
    public void generateBinAscii800() throws Exception{
        MessageFactory<IsoMessage> mf = ConfigParser.createDefault();
        IsoMessage msg = mf.newMessage(0x800);
        Assert.assertFalse("Bin Header should default to false", msg.isBinaryHeader());
        Assert.assertFalse("Bin Fields should default to false", msg.isBinaryFields());

        msg.setBinaryHeader(true);

        msg.setField(7,new IsoValue<>(IsoType.DATE10, "0322220001"));
        msg.setField(11,new IsoValue<>(IsoType.NUMERIC, 562040,6));
        msg.setField(37,new IsoValue<>(IsoType.ALPHA, 562040,12));
        msg.setField(53,new IsoValue<>(IsoType.LLVAR,  "0001000000"));
        msg.setField(70,new IsoValue<>(IsoType.NUMERIC, 1,3));

        byte[] actual = msg.writeData();

        byte[] expected = TestBinAsciiParsing.loadData("bin_ascii_800.bin");

        Assert.assertArrayEquals(expected,actual);
    }

}
