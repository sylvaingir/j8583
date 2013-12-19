package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Abstract class for date/time parsers.
 *
 * @author Enrique Zamudio
 *         Date: 18/12/13 18:21
 */
public abstract class DateTimeParseInfo extends FieldParseInfo {

    protected static final long FUTURE_TOLERANCE;
    protected TimeZone tz;

   	static {
   		FUTURE_TOLERANCE = Long.parseLong(System.getProperty("j8583.future.tolerance", "900000"));
   	}

    protected DateTimeParseInfo(IsoType type, int length) {
        super(type, length);
    }

    public void setTimeZone(TimeZone value) {
        tz = value;
    }
    public TimeZone getTimeZone() {
        return tz;
    }

    public static void adjustWithFutureTolerance(Calendar cal) {
   		//We need to handle a small tolerance into the future (a couple of minutes)
   		long now = System.currentTimeMillis();
   		long then = cal.getTimeInMillis();
   		if (then > now && then-now > FUTURE_TOLERANCE) {
   			cal.add(Calendar.YEAR, -1);
   		}
   	}

}
