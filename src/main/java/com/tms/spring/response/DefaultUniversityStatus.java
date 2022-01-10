package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.UniversityModel;

@Getter
@Setter
public class DefaultUniversityStatus {
	Integer statusCode;
	String message;
    UniversityModel university;

	public DefaultUniversityStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

    public DefaultUniversityStatus(String message, UniversityModel university) {
		this.statusCode = 200;
		this.message = message;
		this.university = university;
	}
}