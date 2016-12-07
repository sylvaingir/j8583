package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.IsoMessage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

/**
 * Issue 38.
 *
 * @author Enrique Zamudio
 *         Date: 26/03/15 10:54
 */
public class TestEmptyLvars {

    private static MessageFactory<IsoMessage> txtfact = new MessageFactory<>();
    private static MessageFactory<IsoMessage> binfact = new MessageFactory<>();
    @BeforeClass
    public static void setupSpec() throws IOException {
        txtfact.setConfigPath("issue38.xml");

        binfact.setUseBinaryMessages(true);
        binfact.setConfigPath("issue38.xml");
        binfact.setUseBinaryBody(true);
    }

    private void checkString(byte[] txt, byte[] bin, int field)
            throws IOException, ParseException {
        IsoMessage t = txtfact.parseMessage(txt, 0);
        IsoMessage b = binfact.parseMessage(bin, 0);
        Assert.assertTrue(t.hasField(field));
        Assert.assertTrue(b.hasField(field));
        Assert.assertTrue(((String) t.getObjectValue(field)).isEmpty());
        Assert.assertTrue(((String) b.getObjectValue(field)).isEmpty());
    }
    private void checkBin(byte[] txt, byte[] bin, int field)
            throws IOException, ParseException {
        IsoMessage t = txtfact.parseMessage(txt, 0);
        IsoMessage b = binfact.parseMessage(bin, 0);
        Assert.assertTrue(t.hasField(field));
        Assert.assertTrue(b.hasField(field));
        Assert.assertEquals(0, ((byte[]) t.getObjectValue(field)).length);
        Assert.assertEquals(0, ((byte[]) b.getObjectValue(field)).length);
    }
    @Test
    public void testEmptyLLVAR() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(2, "", IsoType.LLVAR, 0);
        b.setValue(2, "", IsoType.LLVAR, 0);
        checkString(t.writeData(), b.writeData(), 2);
    }
    @Test
    public void testEmptyLLLVAR() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(3, "", IsoType.LLLVAR, 0);
        b.setValue(3, "", IsoType.LLLVAR, 0);
        checkString(t.writeData(), b.writeData(), 3);
    }
    @Test
    public void testEmptyLLLLVAR() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(4, "", IsoType.LLLLVAR, 0);
        b.setValue(4, "", IsoType.LLLLVAR, 0);
        checkString(t.writeData(), b.writeData(), 4);
    }
    @Test
    public void testEmptyLLBIN() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(5, new byte[0], IsoType.LLBIN, 0);
        b.setValue(5, new byte[0], IsoType.LLBIN, 0);
        checkBin(t.writeData(), b.writeData(), 5);
    }
    @Test
    public void testEmptyLLLBIN() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(6, new byte[0], IsoType.LLLBIN, 0);
        b.setValue(6, new byte[0], IsoType.LLLBIN, 0);
        checkBin(t.writeData(), b.writeData(), 6);
    }
    @Test
    public void testEmptyLLLLBIN() throws Exception {
        IsoMessage t = txtfact.newMessage(0x100);
        IsoMessage b = binfact.newMessage(0x100);
        t.setValue(7, new byte[0], IsoType.LLLLBIN, 0);
        b.setValue(7, new byte[0], IsoType.LLLLBIN, 0);
        checkBin(t.writeData(), b.writeData(), 7);
    }

}
