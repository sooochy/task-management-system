package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultUserStatus {
	Integer statusCode;
	String message;

	public DefaultUserStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}
}