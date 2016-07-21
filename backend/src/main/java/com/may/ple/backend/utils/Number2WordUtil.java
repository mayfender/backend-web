package com.may.ple.backend.utils;

import java.util.ArrayList;
import java.util.List;

public class Number2WordUtil {
	private static String numberWords[] = new String[]{"หนึ่ง", "สอง", "สาม", "สี่", "ห้า", "หก", "เจ็ด", "แปด", "เก้า"};
	private static String digitWords[] = new String[]{"", "สิบ", "ร้อย", "พัน", "หมื่น", "แสน", "ล้าน"};
	private static String extWords[] = new String[]{"สิบ", "ยี่สิบ"};
	private static String unitWord[] = new String[]{"บาท", "สตางค์"};
	private static String oneWordExt = "เอ็ด";
	private static String millionWord = "ล้าน";
	
	public static String bahtText(String value) throws Exception {
		String word = "";
		String[] split = value.split("\\.");
		String fPartStr, lPartStr;
		int fPart, lPart;
		
		fPartStr = split[0].replace(",", "");
		lPartStr = split[1];
		
		if(!fPartStr.equals("0")) {
			int length = fPartStr.length();
			
			if(length > 7) {
				word = moreMillion(length, fPartStr);
			} else {
				fPart = Integer.parseInt(fPartStr);	
				word = _2Word(fPart);
			}
			
			word += unitWord[0];
		}
		
		lPart = Integer.parseInt(lPartStr);
		if(lPart != 0) {			
			word += _2Word(lPart) + unitWord[1];
		} else {
			word += "ถ้วน";
		}
		
		return word;
	}
	
	private static String moreMillion(int length, String fPartStr) throws Exception {
		List<String> groupNum = new ArrayList<String>();
		String wordResult = "";
		int bb = length / 6;
		int fPart;
		
		for (int i = 0; i < bb; i++) {
			groupNum.add(fPartStr.substring(fPartStr.length() - 6, fPartStr.length()));
			fPartStr = fPartStr.substring(0, fPartStr.length() - 6);
		}	
		
		if(!fPartStr.isEmpty()) {
			groupNum.add(fPartStr);				
		}
		
		for (int i = groupNum.size(); i > 0 ; i--) {
			fPartStr = groupNum.get(i - 1);
			fPart = Integer.parseInt(fPartStr);	
			wordResult += _2Word(fPart);
			
			if(i != 1) {
				wordResult += millionWord;
			}
		}
		
		return wordResult;
	}
	
	private static String _2Word(int fraction) throws Exception {
		if(fraction == 0) throw new Exception("Value is 0");
		
		String word = "";
		String frStr = String.valueOf(fraction);
		int length = frStr.length();
		int l = length;
		int i = 0;
			
		if(l == 1)
			return word += numberWords[fraction - 1];
		
		for (;l > 0; l--) {
			i = length - l;
			int num = Integer.parseInt(frStr.substring(i, i + 1));
			
			// Found 0 last digit.
			if(l == 1 && num == 0) break;
			
			// Found 0 any digit but not last.
			if(num == 0) continue;
			
			// Found 1 last digit.
			if(l == 1 && num == 1) {
				word += oneWordExt;
				break;
			}else if(l == 2){
				// Found 1 or 2 on digit 2.
				if(num == 1 || num == 2){
					word += extWords[num - 1];
					continue;
				}
			}
			
			word += numberWords[num - 1] + "" + digitWords[l - 1];
		}
		
		return word;
	}
}
