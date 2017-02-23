package com.may.ple.backend.utils;

import org.apache.commons.lang3.StringUtils;

public class Stringutil {
	
	public static String removeWhitespace(String str) {
		if(StringUtils.isBlank(str)) return str;
		
//		String result = StringUtils.deleteWhitespace(str);
		String result = str.trim();
		result = result.replace("\u00A0","");
		return result;
	}

}
