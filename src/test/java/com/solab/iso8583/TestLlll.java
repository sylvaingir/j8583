package com.solab.iso8583;

import com.solab.iso8583.util.HexCodec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Tests for LLLLVAR and LLLLBIN.
 *
 * @author Enrique Zamudio
 *         Date: 19/02/15 18:25
 */
public class TestLlll {

    private MessageFactory<IsoMessage> mfact = new MessageFactory<>();

    @Before
    public void setup() throws IOException {
        mfact.setConfigPath("issue36.xml");
        mfact.setAssignDate(false);
    }

    @Test
    public void testTemplate() {
        IsoMessage m = mfact.newMessage(0x100);
        Assert.assertEquals("010060000000000000000001X0002FF", m.debugString());
        m.setBinary(true);
        Assert.assertArrayEquals(new byte[]{1, 0, (byte) 0x60, 0, 0, 0, 0, 0, 0, 0,
                0, 1, (byte) 'X', 0, 1, (byte)0xff}, m.writeData());
    }

    @Test
    public void testNewMessage() {
        IsoMessage m = mfact.newMessage(0x200);
        m.setValue(2, "Variable length text", IsoType.LLLLVAR, 0);
        m.setValue(3, "FFFF", IsoType.LLLLBIN, 0);
        Assert.assertEquals("020060000000000000000020Variable length text0004FFFF", m.debugString());
        m.setBinary(true);
        m.setValue(2, "XX", IsoType.LLLLVAR, 0);
        m.setValue(3, new byte[]{(byte) 0xff}, IsoType.LLLLBIN, 0);
        Assert.assertArrayEquals(new byte[]{2, 0, (byte) 0x60, 0, 0, 0, 0, 0, 0, 0,
                0, 2, (byte)'X', (byte)'X', 0, 1, (byte)0xff}, m.writeData());
    }

    @Test
    public void testParsing() throws ParseException, IOException {
        IsoMessage m = mfact.parseMessage("010060000000000000000001X0002FF".getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertEquals("X", m.getObjectValue(2));
        Assert.assertArrayEquals(new byte[]{(byte) 0xff}, (byte[])m.getObjectValue(3));
        mfact.setUseBinaryMessages(true);
        m = mfact.parseMessage(new byte[]{1, 0, (byte) 0x60, 0, 0, 0, 0, 0, 0, 0,
                        0, 2, (byte)'X', (byte)'X', 0, 1, (byte)0xff}, 0);
        Assert.assertNotNull(m);
        Assert.assertEquals("XX", m.getObjectValue(2));
        Assert.assertArrayEquals(new byte[]{(byte) 0xff}, (byte[])m.getObjectValue(3));
    }

    @Test
    public void testL4bin() throws ParseException, IOException {
        byte[] fieldData = new byte[1000];
        mfact.setUseBinaryMessages(true);
        IsoMessage m = mfact.newMessage(0x100);
        m.setValue(3, fieldData, IsoType.LLLLBIN, 0);
        fieldData = m.writeData();
        //2 for message header
        //8 bitmap
        //3 for field 2 (from template)
        //1002 for field 3
        Assert.assertEquals(1015, fieldData.length);
        m = mfact.parseMessage(fieldData, 0);
        Assert.assertTrue(m.hasField(3));
        fieldData = m.getObjectValue(3);
        Assert.assertEquals(1000, fieldData.length);
    }
}
