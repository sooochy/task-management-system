package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.FieldModel;

@Getter
@Setter
public class DefaultFieldStatus {
	Integer statusCode;
	String message;
    FieldModel field;

	public DefaultFieldStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

    public DefaultFieldStatus(String message, FieldModel field) {
		this.statusCode = 200;
		this.message = message;
		this.field = field;
	}
}