package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultSignInStatus {
	Integer statusCode;
	String message;
	String email;
    String token;
	Short type;

	public DefaultSignInStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultSignInStatus(String message, String email, String token, Short type) {
		this.statusCode = 200;
		this.message = message;
		this.email = email;
		this.token = token;
		this.type = type;
	}
}