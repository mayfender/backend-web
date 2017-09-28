package com.may.ple.backend.criteria;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ProgramFileFindCriteriaReq extends CommonCriteriaResp {
	private String id;
	private Integer currentPage;
	private Integer itemsPerPage;
	private Boolean isDeployer;
	private Boolean isTunnel;
	private String command;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getIsDeployer() {
		return isDeployer;
	}

	public void setIsDeployer(Boolean isDeployer) {
		this.isDeployer = isDeployer;
	}

	public Boolean getIsTunnel() {
		return isTunnel;
	}

	public void setIsTunnel(Boolean isTunnel) {
		this.isTunnel = isTunnel;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
