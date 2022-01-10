package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.FacultyModel;

@Getter
@Setter
public class DefaultFacultyStatus {
	Integer statusCode;
	String message;
    FacultyModel faculty;

	public DefaultFacultyStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

    public DefaultFacultyStatus(String message, FacultyModel faculty) {
		this.statusCode = 200;
		this.message = message;
		this.faculty = faculty;
	}
}