package com.may.ple.backend.license;

import net.nicholaswilliams.java.licensing.FileLicenseProvider;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.may.ple.backend.service.SettingService;


public class DmsLicenseProvider extends FileLicenseProvider {
	private static final Logger LOG = Logger.getLogger(DmsLicenseProvider.class.getName());
	private SettingService setting;
	
	public DmsLicenseProvider(SettingService setting) {
		this.setting = setting;
	}
	
	@Override
	protected byte[] getLicenseData(Object context) {
		try {
			byte[] licenseData = Base64.decodeBase64(this.setting.getCer().getLicense());	
			return licenseData;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			return null;
		}
	}

}
