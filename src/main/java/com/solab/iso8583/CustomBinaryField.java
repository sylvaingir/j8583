/*
 * j8583 A Java implementation of the ISO8583 protocol
 * Copyright (C) 2007 Enrique Zamudio Lopez
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
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

    T decodeBinaryField(byte[] value, int offset, int length);

   	byte[] encodeBinaryField(T value);

}
