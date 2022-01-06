package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.MarkModel;

@Getter
@Setter
public class DefaultMarkStatus {
	Integer statusCode;
	String message;
	MarkModel mark;

	public DefaultMarkStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultMarkStatus(String message, MarkModel mark) {
		this.statusCode = 200;
		this.message = message;
		this.mark = mark;
	}
}