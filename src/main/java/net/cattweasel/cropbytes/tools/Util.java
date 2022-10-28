package net.cattweasel.cropbytes.tools;

/**
 * Several utility functions used across the whole source code.
 * 
 * @author cattweasel
 *
 */
public class Util {

	/**
	 * Formats a number to a string with a given length.
	 * 
	 * @param input The input number to be formatted
	 * @param length The desired length for the output string
	 * @return The formatted number
	 */
	public static String formatNumber(Double input, Integer length) {
		return formatNumber(input, length, true);
	}
	
	/**
	 * Formats a number to a string with a given length.
	 * 
	 * @param input The input number to be formatted
	 * @param length The desired length for the output string
	 * @param includeSign True to always include the sign
	 * @return The formatted number
	 */
	public static String formatNumber(Double input, Integer length, Boolean includeSign) {
		input = Math.round(input * 1000000D) / 1000000D;
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
