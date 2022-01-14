package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class DefaultUserStatus {
	Integer statusCode;
	String message;
	LocalDateTime date;

	public DefaultUserStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultUserStatus(String message, LocalDateTime date) {
		this.statusCode = 200;
		this.message = message;
		this.date = date;
	}
}