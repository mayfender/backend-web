package com.may.ple.backend.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class ModelBase implements Serializable {
	private static final long serialVersionUID = 3459895569130537701L;

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
