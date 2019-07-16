package com.tpp.persistence.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

	/**
	 * Remove accent from vietnamese text
	 *
	 * @param text
	 * @return
	 */
	public static String removeAccent(String text) {
		String temp = Normalizer.normalize(text, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
	}

	/**
	 * compare unicode string
	 *
	 * @param source
	 * @param destination
	 * @return
	 */
	public static boolean compareIgnoreAccent(String source, String destination) {
		return removeAccent(source).equals(removeAccent(destination));
	}
}
