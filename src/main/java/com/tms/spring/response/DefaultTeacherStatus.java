package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultTeacherStatus {
	Integer statusCode;
	String message;
	Long id;

	public DefaultTeacherStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultTeacherStatus(String message, Long id) {
		this.statusCode = 200;
		this.message = message;
		this.id = id;
	}
}