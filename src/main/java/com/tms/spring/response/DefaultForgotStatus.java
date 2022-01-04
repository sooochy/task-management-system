package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultForgotStatus {
	Integer statusCode;
	String message;

	public DefaultForgotStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}
}