package com.may.ple.backend.license;

import net.nicholaswilliams.java.licensing.License;
import net.nicholaswilliams.java.licensing.LicenseValidator;
import net.nicholaswilliams.java.licensing.exception.InvalidLicenseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.may.ple.backend.service.SettingService;

public class DmsLicenseValidator implements LicenseValidator {
	private static final Logger LOG = Logger.getLogger(DmsLicenseValidator.class.getName());
	private SettingService setting;
	
	public DmsLicenseValidator(SettingService setting) {
		this.setting = setting;
	}
	
	@Override
	public void validateLicense(License license) throws InvalidLicenseException {
		String productKey = null;
		
		try {
			productKey = setting.getCer().getProductKey();
		} catch (Exception e) {
			LOG.error(e.toString());
		}
		
		if(StringUtils.isBlank(productKey)) 
			throw new InvalidLicenseException("Product Key is empty"); 
		
		if(!license.getProductKey().equals(productKey)) 
			throw new InvalidLicenseException("Product Key is not match."); 
	}

}
