package com.may.ple.backend.utils;

import java.util.List;

import com.may.ple.backend.entity.ActionCode;
import com.may.ple.backend.entity.ResultCode;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.Users;

public class TraceWorkMapObjUtil {
	
	public static void mappingObj(List<? extends TraceWork> traceWorks, List<ActionCode> actionCodes, List<ResultCode> resultCodes, List<Users> users) {
		
		for (TraceWork trace : traceWorks) {
			if(actionCodes != null) {			
				for (ActionCode acc : actionCodes) {
					if(trace.getActionCode() != null && trace.getActionCode().equals(acc.getId())) {
						trace.setActionCodeText(acc.getActCode());
						break;
					}
				}
			}
			if(resultCodes != null) {
				for (ResultCode rsc : resultCodes) {
					if(trace.getResultCode() != null && trace.getResultCode().equals(rsc.getId())) {
						trace.setResultCodeText(rsc.getRstCode());
						break;
					}
				}
			}
			if(users != null) {
				for (Users u : users) {
					if(trace.getCreatedBy().equals(u.getId())) {
						trace.setCreatedByText(u.getShowname());
						break;
					}
				}
			}
		}
	}
	
}
