package com.may.ple.backend.constant;

public enum PluginModuleConstant {
	TUNNEL(9000),
	KYS(9001),
	JWS(9002),
	DPY(0000); // Waiting the deployer to use this plugin
	
	private Integer port;
	
	private PluginModuleConstant(Integer port) {
		this.port = port;
	}
	
	public Integer getPort() {
		return this.port;
	}
	
}
