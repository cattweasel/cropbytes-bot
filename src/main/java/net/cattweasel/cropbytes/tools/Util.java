package net.cattweasel.cropbytes.tools;

public class Util {

	public static String formatNumber(Double input, Integer length) {
		return formatNumber(input, length, true);
	}
	
	public static String formatNumber(Double input, Integer length, Boolean includeSign) {
		String str = (input >= 0D ? "+" : "") + input.toString();
		if (!includeSign) {
			str = str.substring(1, str.length());
		}
		while (str.length() > length) {
			str = str.substring(0, str.length() - 1);
		}
		StringBuilder sb = new StringBuilder(str);
		while (sb.length() < length) {
			sb.append("0");
		}
		return sb.toString();
	}
}
