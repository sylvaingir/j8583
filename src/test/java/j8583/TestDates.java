package j8583;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.*;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.Date10ParseInfo;
import com.solab.iso8583.parse.Date4ParseInfo;

/** Test that the dates are formatted and parsed correctly.
 * 
 * @author Enrique Zamudio
 */
public class TestDates {

	private MessageFactory mf;

	@Before
	public void init() throws IOException {
		mf = new MessageFactory();
		mf.setCharacterEncoding("UTF-8");
		mf.setCustomField(48, new CustomField48());
		mf.setConfigPath("config.xml");
	}

	@Test
	public void testDate4FutureTolerance() throws ParseException, IOException {
		GregorianCalendar today = new GregorianCalendar();
		Date soon = new Date(today.getTime().getTime() + 50000);
		today.set(GregorianCalendar.HOUR,0);
		today.set(GregorianCalendar.MINUTE,0);
		today.set(GregorianCalendar.SECOND,0);
		today.set(GregorianCalendar.MILLISECOND,0);
		byte[] buf = IsoType.DATE4.format(soon).getBytes();
		IsoValue<Date> comp = new Date4ParseInfo().parse(buf, 0, null);
		Assert.assertEquals(comp.getValue(), today.getTime());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true);
		IsoValue<Date> bin = new Date4ParseInfo().parseBinary(bout.toByteArray(), 0, null);
		Assert.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

	@Test
	public void testDate10FutureTolerance() throws ParseException, IOException {
		Date soon = new Date(System.currentTimeMillis() + 50000);
		byte[] buf = IsoType.DATE10.format(soon).getBytes();
		IsoValue<Date> comp = new Date10ParseInfo().parse(buf, 0, null);
		assert comp.getValue().after(new Date());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true);
		IsoValue<Date> bin = new Date10ParseInfo().parseBinary(bout.toByteArray(), 0, null);
		Assert.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

}
