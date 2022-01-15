package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultNotificationStatus {
	Integer statusCode;
	String message;

	public DefaultNotificationStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}
}