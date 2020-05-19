package com.solab.iso8583;

import com.solab.iso8583.util.HexCodec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

public class TestHexadecimalLengthDecoding {

    private final MessageFactory<IsoMessage> mf = new MessageFactory<>();

    @Before
    public void init() throws IOException {
        mf.setConfigPath("hexadecimal.xml");
        mf.setBinaryHeader(true);
        mf.setBinaryFields(true);
    }

    @Test
    public void shouldParseLengthWithBcdDecoding() throws IOException, ParseException {
        // Given
        String input = "0100" +                                 //MTI
                "7F80000000000000" +                            // bitmap (with fields 2,3,4,5,6,7,8,9)
                "09" + "0666666666" +                           // F2(LLBCDBIN) length (09 = 9) + BCD value
                "26" + "01234567890123456789012345" +           // F3(LLBCDBIN) length (26 = 26) + BCD value
                "18" + "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0" + // F4(LLBIN) length (18 = 18) + EBCDIC value
                "0112" + repeat("5", 112) +         // F5(LLLBCDBIN) length (0112 = 112) + BCD value
                "0112" + repeat("C1", 112) +        // F6(LLLBIN) length (0112 = 112) + EBCDIC value
                "1112" + repeat("6", 1112) +        // F7(LLLLBCDBIN) length (1112 = 1112) + BCD value
                "1112" + repeat("C2", 1112) +       // F8(LLLLBIN) length (1112 = 1112) + EBCDIC value
                "88888888";                                     // F9(BINARY)

        // When
        final IsoMessage m = mf.parseMessage(HexCodec.hexDecode(input), 0);

        // Then
        Assert.assertNotNull(m);
        Assert.assertEquals("666666666", m.getField(2).toString());
        Assert.assertEquals("01234567890123456789012345", m.getField(3).toString());
        Assert.assertEquals("C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0", m.getField(4).toString());
        Assert.assertEquals(repeat("5", 112), m.getField(5).toString());
        Assert.assertEquals(repeat("C1", 112), m.getField(6).toString());
        Assert.assertEquals(repeat("6", 1112), m.getField(7).toString());
        Assert.assertEquals(repeat("C2", 1112), m.getField(8).toString());
        Assert.assertEquals("88888888", m.getField(9).toString());
    }

    @Test
    public void shouldParseLengthWithHexadecimalDecoding() throws IOException, ParseException {
        // Given
        mf.setVariableLengthFieldsInHex(true);
        String input = "0100" +                                 //MTI
                "7F80000000000000" +                            // bitmap (with fields 2,3,4,5,9)
                "09" + "0666666666" +                           // F2(LLBCDBIN) length (09 = 9) + BCD value
                "1A" + "01234567890123456789012345" +           // F3(LLBCDBIN) length (1A = 26) + BCD value
                "12" + "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0" + // F4(LLBIN) length (12 = 18) + EBCDIC value
                "0112" + repeat("5", 274) +         // F5(LLLBCDBIN) length (0112 = 274) + BCD value
                "0112" + repeat("C1", 274) +        // F6(LLLBIN) length (0112 = 274) + EBCDIC value
                "1112" + repeat("6", 4370) +        // F7(LLLLBCDBIN) length (1112 = 4370) + BCD value
                "1112" + repeat("C2", 4370) +       // F8(LLLLBIN) length (1112 = 4370) + EBCDIC value
                "88888888";                                     // F9(BINARY)

        // When
        final IsoMessage m = mf.parseMessage(HexCodec.hexDecode(input), 0);

        // Then
        Assert.assertNotNull(m);
        Assert.assertEquals("666666666", m.getField(2).toString());
        Assert.assertEquals("01234567890123456789012345", m.getField(3).toString());
        Assert.assertEquals("C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0", m.getField(4).toString());
        Assert.assertEquals(repeat("5", 274), m.getField(5).toString());
        Assert.assertEquals(repeat("C1", 274), m.getField(6).toString());
        Assert.assertEquals(repeat("6", 4370), m.getField(7).toString());
        Assert.assertEquals(repeat("C2", 4370), m.getField(8).toString());
        Assert.assertEquals("88888888", m.getField(9).toString());
    }

    @Test
    public void shouldWriteLengthWithBcdDecoding() throws IOException {
        boolean hexa = false;

        Object[][] inputs = {
                {IsoType.LLBCDBIN, "26", "01234567890123456789012345"},
                {IsoType.LLBIN, "26", "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0"},
                {IsoType.LLLBCDBIN, "0126", repeat("7", 126)},
                {IsoType.LLLBIN, "0126", repeat("C1", 126)},
                {IsoType.LLLLBCDBIN, "1126", repeat("7", 1126)},
                {IsoType.LLLLBIN, "1126", repeat("C1", 1126)},
        };

        for (Object[] input : inputs) {
            IsoType isoType = (IsoType) input[0];
            String len = (String) input[1];
            String value = (String) input[2];

            IsoValue<byte[]> isoValue = new IsoValue<>(isoType, HexCodec.hexDecode(value), Integer.parseInt(len), hexa);

            String result = getResultAsString(isoValue, hexa);
            Assert.assertEquals(len + value, result);
        }
    }

    @Test
    public void shouldWriteLengthWithHexadecimalDecoding() throws IOException {
        boolean hexa = true;

        Object[][] inputs = {
                {IsoType.LLBCDBIN, "26", "1A", "01234567890123456789012345"},
                {IsoType.LLBIN, "26", "1A", "C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0C0"},
                {IsoType.LLLBCDBIN, "298", "012A", repeat("7", 298)},
                {IsoType.LLLBIN, "298", "012A", repeat("C1", 298)},
                {IsoType.LLLLBCDBIN, "4394", "112A", repeat("7", 4394)},
                {IsoType.LLLLBIN, "4394", "112A", repeat("C1", 4394)},
        };

        for (Object[] input : inputs) {
            IsoType isoType = (IsoType) input[0];
            String len = (String) input[1];
            String hexaLen = (String) input[2];
            String value = (String) input[3];

            IsoValue<byte[]> isoValue = new IsoValue<>(isoType, HexCodec.hexDecode(value), Integer.parseInt(len), hexa);

            String result = getResultAsString(isoValue, hexa);
            Assert.assertEquals(hexaLen + value, result);
        }
    }

    private String getResultAsString(IsoValue<?> isoValue, boolean hexa) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        isoValue.write(bout, true, false, hexa);
        byte[] writtenBytes = bout.toByteArray();
        return HexCodec.hexEncode(writtenBytes, 0, writtenBytes.length);
    }

    private String repeat(String value, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(value);
        }
        return sb.toString();
    }
}
