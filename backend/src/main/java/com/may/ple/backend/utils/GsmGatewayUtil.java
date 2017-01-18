package com.may.ple.backend.utils;

import com.dinstar.result.SendUSSDResult;
import com.dinstar.sms.Gateway;
import com.dinstar.smsenum.USSDCommandEnum;

public class GsmGatewayUtil {
	private String host;
	private int port; 
	private String authUser; 
	private String authPasswd;
	
	public GsmGatewayUtil(String host, int port, String authUser, String authPasswd) {
		this.host = host;
		this.port = port;
		this.authUser = authUser;
		this.authPasswd = authPasswd;
	}

	public static void gsmGateway() {
		String host = "192.168.1.1";
		int port = 0;
		String authUser = "";
		String authPasswd = "";
		
		Gateway gateway = new Gateway(host, port, authUser, authPasswd);
		
		String text= "*933*0844358987#";
		int[] ports= { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
		
		SendUSSDResult sendUSSD = gateway.sendUSSD(text, ports, USSDCommandEnum.SEND);
		
	}

}
