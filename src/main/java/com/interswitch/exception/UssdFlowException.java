package com.interswitch.exception;

import com.interswitch.constant.ErrorCode;

import lombok.Getter;

@Getter
public class UssdFlowException extends RuntimeException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final ErrorCode errorCode;

	public UssdFlowException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public UssdFlowException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
