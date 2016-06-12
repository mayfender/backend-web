package com.may.ple.backend.utils;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class RandomUtil {
	
	/*
	 * Random value from 0 to length parameter.
	 * It will generate random number not same.
	 */
	public static List<Integer> random(int length) {
		List<Integer> lists = new Vector<Integer>();

		for (int i = 0; i < length; i++) {
			lists.add(i);
		}

		Collections.shuffle(lists);
		return lists;
	}
	
	public static List<Integer> order(int length) {
		List<Integer> lists = new Vector<Integer>();

		for (int i = 0; i < length; i++) {
			lists.add(i);
		}

		return lists;
	}

}
