package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.PlanElementModel;
import com.tms.spring.model.TeacherSubjectTypeModel;

@Getter
@Setter
public class DefaultPlanElementStatus {
	Integer statusCode;
	String message;
	PlanElementModel planElement;
	TeacherSubjectTypeModel teacherSubjectType;

	public DefaultPlanElementStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultPlanElementStatus(String message, PlanElementModel planElement, TeacherSubjectTypeModel teacherSubjectType) {
		this.statusCode = 200;
		this.message = message;
		this.planElement = planElement;
		this.teacherSubjectType = teacherSubjectType;
	}
}