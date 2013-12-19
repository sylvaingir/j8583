package j8583;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.solab.iso8583.IsoType;

/** Tests formatting of certain IsoTypes.
 * 
 * @author Enrique Zamudio
 */
public class TestFormats {

	private final Date date = new Date(96867296000l);

	@Test
	public void testDateFormats() {
		assert IsoType.DATE10.format(date, null).equals("0125213456");
		assert IsoType.DATE4.format(date, null).equals("0125");
		assert IsoType.DATE_EXP.format(date, null).equals("7301");
		assert IsoType.TIME.format(date, null).equals("213456");
        //Now with GMT
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Assert.assertEquals("0126033456", IsoType.DATE10.format(date, gmt));
        Assert.assertEquals("0126", IsoType.DATE4.format(date, gmt));
        Assert.assertEquals("7301", IsoType.DATE_EXP.format(date, gmt));
        Assert.assertEquals("033456", IsoType.TIME.format(date, gmt));
        //And now with GMT+1
        gmt = TimeZone.getTimeZone("GMT+0100");
        Assert.assertEquals("0126043456", IsoType.DATE10.format(date, gmt));
        Assert.assertEquals("0126", IsoType.DATE4.format(date, gmt));
        Assert.assertEquals("7301", IsoType.DATE_EXP.format(date, gmt));
        Assert.assertEquals("043456", IsoType.TIME.format(date, gmt));
	}

	@Test
	public void testNumericFormats() {
		assert IsoType.NUMERIC.format(123, 6).equals("000123");
		assert IsoType.NUMERIC.format("hola", 6).equals("00hola");
		assert IsoType.AMOUNT.format(12345, 0).equals("000001234500");
		assert IsoType.AMOUNT.format(new BigDecimal("12345.67"), 0).equals("000001234567");
		assert IsoType.AMOUNT.format("1234.56", 0).equals("000000123456");
	}

	@Test
	public void testStringFormats() {
		assert IsoType.ALPHA.format("hola", 3).equals("hol");
		assert IsoType.ALPHA.format("hola", 4).equals("hola");
		assert IsoType.ALPHA.format("hola", 6).equals("hola  ");
		assert IsoType.LLVAR.format("hola", 0).equals("hola");
		assert IsoType.LLLVAR.format("hola", 0).equals("hola");
	}

}
