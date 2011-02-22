package j8583;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import com.solab.iso8583.IsoType;

/** Tests formatting of certain IsoTypes.
 * 
 * @author Enrique Zamudio
 */
public class TestFormats {

	private Date date = new Date(96867296000l);

	@Test
	public void testDateFormats() {
		assert IsoType.DATE10.format(date).equals("0125213456");
		assert IsoType.DATE4.format(date).equals("0125");
		assert IsoType.DATE_EXP.format(date).equals("7301");
		assert IsoType.TIME.format(date).equals("213456");
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
