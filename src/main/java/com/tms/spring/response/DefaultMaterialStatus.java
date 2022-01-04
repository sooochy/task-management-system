package com.tms.spring.response;

import lombok.Getter;
import lombok.Setter;
import com.tms.spring.model.MaterialModel;

@Getter
@Setter
public class DefaultMaterialStatus {
	Integer statusCode;
	String message;
	MaterialModel material;

	public DefaultMaterialStatus(String message) {
		this.statusCode = 200;
		this.message = message;
	}

	public DefaultMaterialStatus(String message, MaterialModel material) {
		this.statusCode = 200;
		this.message = message;
		this.material = material;
	}
}