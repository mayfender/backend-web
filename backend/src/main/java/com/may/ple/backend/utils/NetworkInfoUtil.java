package com.may.ple.backend.utils;

import java.io.IOException;

public class NetworkInfoUtil {
	
	/*public static void main(String[] args) {
		try {
			getPublicIp("https://api.ipify.org");			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public static String getPublicIp(String ipServ) throws IOException {
		
		try (java.util.Scanner s = new java.util.Scanner(new java.net.URL(ipServ).openStream(), "UTF-8").useDelimiter("\\A")) {
			return s.next();
		} catch (java.io.IOException e) {
			throw e;
		}	
	}

}
