package ar.com.hipotecario.mobile.api.dto.modo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserStatus {
	private Boolean onboarding = Boolean.FALSE;

	@JsonProperty(value = "account_linking")
	private Boolean accountLinking = Boolean.FALSE;

	@JsonProperty(value = "another_bank")
	private Boolean anotherBank = Boolean.FALSE;

	public UserStatus() {
	}

	public UserStatus(Boolean onboarding, Boolean accountLinking, Boolean anotherBank) {
		this.onboarding = onboarding = Boolean.FALSE;
		this.accountLinking = accountLinking = Boolean.FALSE;
		this.anotherBank = anotherBank = Boolean.FALSE;
	}

	public Boolean getOnboarding() {
		return onboarding;
	}

	public void setOnboarding(Boolean onboarding) {
		this.onboarding = onboarding;
	}

	public Boolean getAccountLinking() {
		return accountLinking;
	}

	public void setAccountLinking(Boolean accountLinking) {
		this.accountLinking = accountLinking;
	}

	public Boolean getAnotherBank() {
		return anotherBank;
	}

	public void setAnotherBank(Boolean anotherBank) {
		this.anotherBank = anotherBank;
	}
}
