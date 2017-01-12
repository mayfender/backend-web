package com.may.ple.backend.license;

import net.nicholaswilliams.java.licensing.FileLicenseProvider;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.may.ple.backend.service.SettingService;


public class MyLicenseProvider extends FileLicenseProvider {
	private static final Logger LOG = Logger.getLogger(MyLicenseProvider.class.getName());
	private SettingService service;
	
	public MyLicenseProvider(SettingService service) {
		this.service = service;
	}
	
	@Override
	protected byte[] getLicenseData(Object context) {
		try {
			byte[] licenseData = Base64.decodeBase64(service.getLicense());	
			return licenseData;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			return null;
		}
	}

}
