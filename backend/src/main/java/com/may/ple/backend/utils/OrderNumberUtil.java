package com.may.ple.backend.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class OrderNumberUtil {
	private static final Logger LOG = Logger.getLogger(OrderNumberUtil.class.getName());
	public static String tabIndex[] = new String[]{"1", "2", "3", "4", "41", "42", "43", "44", "51"};
	public static String tabIndex4Export[] = new String[]{"51", "1", "2", "3", "4", "41", "42", "43", "44"}; //--: Started with TOD

	public static List<String> getOrderNumProb(String src) throws Exception {
		try {
			List<String> orderNumLst = new ArrayList<>();
			String result = "";
			int orderSet[][];

			if (src.length() == 2) {
				orderSet = new int[][] { { 0, 1 }, { 1, 0 } };
			} else if (src.length() == 3) {
				orderSet = new int[][] { { 0, 1, 2 }, { 1, 2, 0 }, { 2, 0, 1 }, { 2, 1, 0 }, { 1, 0, 2 }, { 0, 2, 1 } };
			} else {
				throw new Exception("Number of digit is not support.");
			}

			orderNumLst.add(src);

			outer: for (int i = 1; i < orderSet.length; i++) {
				for (int j = 0; j < orderSet[i].length; j++) {
					result += src.charAt(orderSet[i][j]);
				}
				if (orderNumLst.contains(result)) {
					break outer;
				}

				orderNumLst.add(result);
				result = "";
			}

			return orderNumLst;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public static List<String> getOrderNumProbOver3(String src) throws Exception {
		List<String> orderNumLst = new ArrayList<>();
		int orderSet[][];
		if(src.length() == 4) {
			orderSet = new int[][] {{0,1,2},{0,1,3},{0,2,3},{1,2,3}};
		} else if(src.length() == 5) {
			orderSet = new int[][] {{0,1,2},{0,1,3},{0,1,4},{0,2,3},{0,2,4},{0,3,4},{1,2,3},{1,2,4},{1,3,4},{2,3,4}};
		} else {
			throw new Exception("Number of digit is not support.");
		}

		String result = "";

		for (int i = 0; i < orderSet.length; i++) {
			for (int j = 0; j < orderSet[i].length; j++) {
				result += src.charAt(orderSet[i][j]);
			}
			if(orderNumLst.contains(result)) {
				result = "";
				continue;
			}
			orderNumLst.addAll(getOrderNumProb(result));
			result = "";
		}
		return orderNumLst;
	}

}
