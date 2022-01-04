package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultSubjectStatus {
	Integer statusCode;
	String message;
	Long id;

	public DefaultSubjectStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultSubjectStatus(String message, Long id) {
		this.statusCode = 200;
		this.message = message;
		this.id = id;
	}
}