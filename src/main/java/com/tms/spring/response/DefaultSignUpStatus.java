package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultSignUpStatus {
	Integer statusCode;
	String message;

	public DefaultSignUpStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}
}