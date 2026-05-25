
package com.interswitch.exception;

import com.interswitch.constant.ErrorCode;

import lombok.Getter;

@Getter
public class CustomSecurityException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;

	public CustomSecurityException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public CustomSecurityException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
