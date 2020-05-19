package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom class to parse fields of type LLLLBCDBIN with BCD length.
 */
public class BcdLengthLlllbinParseInfo extends LlllbinParseInfo {

    public BcdLengthLlllbinParseInfo() {
        super(IsoType.LLLLBCDBIN, 0);
    }

    @Override
    protected int getLengthForBinaryParsing(byte[] buf, int pos) {
        final int length = super.getLengthForBinaryParsing(buf, pos);
        return length % 2 == 0 ? length / 2 : (length / 2) + 1;
    }

}
