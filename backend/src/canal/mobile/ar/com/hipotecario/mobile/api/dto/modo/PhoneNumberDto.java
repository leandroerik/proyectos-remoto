package ar.com.hipotecario.mobile.api.dto.modo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhoneNumberDto {

	@JsonProperty(value = "phone_number")
	private String phone_number;

	public PhoneNumberDto() {
	}

	public PhoneNumberDto(String phone_number) {
		this.phone_number = phone_number;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}
}
