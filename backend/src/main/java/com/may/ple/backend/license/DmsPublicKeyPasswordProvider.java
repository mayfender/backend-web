package com.may.ple.backend.license;

import net.nicholaswilliams.java.licensing.encryption.PasswordProvider;

public class DmsPublicKeyPasswordProvider implements PasswordProvider {

	@Override
	public char[] getPassword() {
		return "w,j[vd8iy[".toCharArray();
	}

}
