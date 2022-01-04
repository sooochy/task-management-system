package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.HomeworkModel;

@Getter
@Setter
public class DefaultHomeworkStatus {
	Integer statusCode;
	String message;
	HomeworkModel homework;

	public DefaultHomeworkStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultHomeworkStatus(String message, HomeworkModel homework) {
		this.statusCode = 200;
		this.message = message;
		this.homework = homework;
	}
}