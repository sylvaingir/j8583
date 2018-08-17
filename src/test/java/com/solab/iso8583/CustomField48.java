package com.solab.iso8583;

/** This is an example of a custom field codec, which converts between strings and instances of this same class.
 * It's used to test the encoding and decoding of custom fields by the message factory.
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
	public int getValue2() {
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
		if (value.getValue1() != null) {
			sb.append(value.getValue1());
		}
		sb.append('|');
		sb.append(value.getValue2());
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CustomField48)) {
			return false;
		}
		CustomField48 other = (CustomField48)obj;
		if (other.getValue2() == v2) {
			if (other.getValue1() == null) {
				return v1 == null;
			} else {
				return other.getValue1().equals(v1);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (v1 == null ? 0 : v1.hashCode()) | v2;
	}

}
