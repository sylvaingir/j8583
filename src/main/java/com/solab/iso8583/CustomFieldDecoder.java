package com.solab.iso8583;

/**
 * Blabla.
 *
 * @author Enrique Zamudio
 * Date: 2019-02-08 11:21
 */
public interface CustomFieldDecoder<DataType> {

    DataType decodeField(String value);
}
