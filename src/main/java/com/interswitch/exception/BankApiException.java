package com.interswitch.exception;

import com.interswitch.constant.ErrorCode;

import lombok.Getter;

@Getter
public class BankApiException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final ErrorCode errorCode;

	public BankApiException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BankApiException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}