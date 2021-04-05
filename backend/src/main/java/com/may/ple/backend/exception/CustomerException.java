package com.may.ple.backend.exception;

public class CustomerException extends Exception {
	private static final long serialVersionUID = -2778152939502565089L;
	public int errCode;
	public String orderNumber;
	public String msg;

	public CustomerException(int errCode, String msg) {
		super(msg);
		this.errCode = errCode;
		this.msg = msg;
	}

	public CustomerException(int errCode, String orderNumber, String msg) {
		super(msg);
		this.errCode = errCode;
		this.orderNumber = orderNumber;
		this.msg = msg;
	}

	public String getMsg() {
		return this.msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
