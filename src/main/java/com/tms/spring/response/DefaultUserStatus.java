package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.UserModel;

@Getter
@Setter
public class DefaultUserStatus {
	Integer statusCode;
	String message;
	UserModel user;

	public DefaultUserStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

    public DefaultUserStatus(String message, UserModel user) {
		this.statusCode = 200;
		this.message = message;
		this.user = user;
	}
}