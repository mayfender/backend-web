package com.may.ple.backend.utils;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
	
	public static String removeWhitespace(String str) {
		if(StringUtils.isBlank(str)) return str;
		
//		String result = StringUtils.deleteWhitespace(str);
//		result = result.replaceAll("/^\\s+|\\s+$/g", "");
		
		String result = str.replace("\u00A0","");
		return result.trim();
	}

}
