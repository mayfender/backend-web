package com.may.ple.backend.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class PaymentRulesUtil {
	private static final Logger LOG = Logger.getLogger(PaymentRulesUtil.class.getName());

	public static Map<String, String> getPaymentRules(String paymentRulesStr) throws Exception {
		try {
			if(StringUtils.isBlank(paymentRulesStr)) return null;
			
			String splited[] = paymentRulesStr.split("\\r?\\n");
			Map<String, String> rules = new HashMap<>();
			String[] expression;
			
			for (String rule : splited) {
				expression = rule.split("=");
				rules.put(expression[0], expression[1]);
			}
			
			return rules;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
