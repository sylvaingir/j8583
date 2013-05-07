package com.solab.iso8583;

/**
 * An extension of the CustomField interface, useful for binary fields.
 * CustomBinaryField encoders can return null for the two CustomField
 * methods IF they are only used with binary messages.
 *
 * @author Enrique Zamudio
 *         Date: 07/05/13 13:04
 */
public interface CustomBinaryField<T> extends CustomField<T> {

    public T decodeBinaryField(byte[] value, int offset, int length);

   	public byte[] encodeBinaryField(T value);

}
