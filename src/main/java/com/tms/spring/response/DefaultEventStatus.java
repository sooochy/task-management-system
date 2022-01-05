package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.EventModel;

@Getter
@Setter
public class DefaultEventStatus {
	Integer statusCode;
	String message;
	EventModel event;

	public DefaultEventStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultEventStatus(String message, EventModel event) {
		this.statusCode = 200;
		this.message = message;
		this.event = event;
	}
}