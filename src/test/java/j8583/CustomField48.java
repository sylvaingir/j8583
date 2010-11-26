package j8583;

import com.solab.iso8583.CustomField;

/** This is an example of a custom field codec, which converts between strings and instances of this same class.
 * 
 * @author Enrique Zamudio
 */
public class CustomField48 implements CustomField<CustomField48> {

	private String v1;
	private int v2;

	public void setValue1(String value) {
		v1 = value;
	}
	public String getValue1() {
		return v1;
	}
	public void setValue2(int value) {
		v2 = value;
	}
	public int setValue2() {
		return v2;
	}

	@Override
	public CustomField48 decodeField(String value) {
		CustomField48 cf = null;
		if (value != null) {
			if (value.length() == 1 && value.charAt(0) == '|') {
				cf = new CustomField48();
			} else {
				int idx = value.lastIndexOf('|');
				if (idx < 0 || idx == value.length() - 1) {
					throw new IllegalArgumentException(String.format("Invalid data '%s' for field 48", value));
				}
				cf = new CustomField48();
				cf.setValue1(value.substring(0, idx));
				cf.setValue2(Integer.parseInt(value.substring(idx + 1)));
			}
		}
		return cf;
	}

	@Override
	public String encodeField(CustomField48 value) {
		StringBuilder sb = new StringBuilder();
		if (v1 != null) {
			sb.append(v1);
		}
		sb.append('|');
		sb.append(v2);
		return sb.toString();
	}

}
