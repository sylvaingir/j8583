package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

/**
 * Extended test for LLLLVar field
 *
 * @author Peter Margetiak
 */
public class TestLlllPatch {

    private MessageFactory<IsoMessage> mfact = new MessageFactory<>();

    @Before
    public void setup() throws IOException {
        mfact.setConfigPath("issue50.xml");
        mfact.setAssignDate(false);
        mfact.setUseBinaryBody(false);
    }

    @Test
    public void testParsingLength() throws Exception {
        final int LENGTH = 1234;

        // prepare
        String LLLLVar = makeLLLLVar(LENGTH);
        StringBuilder LLLLVarBuilder = new StringBuilder();
        LLLLVarBuilder.append("01004000000000000000")
                .append(String.format("%04d", LENGTH)).append(LLLLVar);

        // parse
        IsoMessage m = mfact.parseMessage(LLLLVarBuilder.toString().getBytes(), 0);
        Assert.assertNotNull(m);
        Assert.assertEquals(LLLLVar, m.getObjectValue(2));
    }

    @Test
    public void testSerialiseParseSmall() throws Exception {
        testSerialiseParse(88);
    }

    @Test
    public void testSerialiseParseMedium() throws Exception {
        testSerialiseParse(258);
    }

    @Test
    public void testSerialiseParseLarge() throws Exception {
        testSerialiseParse(9919);
    }

    private void testSerialiseParse(final int LENGTH) throws Exception {
        // prepare
        String LLLLVar = makeLLLLVar(LENGTH);
        IsoMessage m = mfact.newMessage(0x100);
        m.setValue(2, LLLLVar, IsoType.LLLLVAR, 0);
        m.setBinary(true);

        // write
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        m.write(bout, 2);
        bout.close();

        // read
        byte[] buf = new byte[2];
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        bin.read(buf);
        Assert.assertNotEquals(buf, new byte[2]);

        int len = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
        buf = new byte[len];
        bin.read(buf);
        bin.close();

        // parse
        mfact.setUseBinaryMessages(true);
        m = mfact.parseMessage(buf, mfact.getIsoHeader(0x100).length());
        Assert.assertNotNull(m);
        Assert.assertEquals(LLLLVar, m.getObjectValue(2));
    }

    private String makeLLLLVar(final int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }

        return sb.toString();
    }
}
