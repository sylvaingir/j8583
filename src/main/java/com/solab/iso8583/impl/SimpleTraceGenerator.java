/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.solab.iso8583.impl;

import com.solab.iso8583.TraceNumberGenerator;

/** Simple implementation of a TraceNumberGenerator with an internal
 * number that is increased in memory but is not stored anywhere.
 * 
 * @author Enrique Zamudio
 */
public class SimpleTraceGenerator implements TraceNumberGenerator {

	private volatile int value = 0;

	/** Creates a new instance that will use the specified initial value. This means
	 * the first nextTrace() call will return this number.
	 * @param initialValue a number between 1 and 999999.
	 * @throws IllegalArgumentException if the number is less than 1 or greater than 999999. */
	public SimpleTraceGenerator(int initialValue) {
		if (initialValue < 1 || initialValue > 999999) {
			throw new IllegalArgumentException("Initial value must be between 1 and 999999");
		}
		value = initialValue - 1;
	}

	public int getLastTrace() {
		return value;
	}

	/** Returns the next number in the sequence. This method is synchronized, because the counter
	 * is incremented in memory only. */
	public synchronized int nextTrace() {
		value++;
		if (value > 999999) {
			value = 1;
		}
		return value;
	}

}
