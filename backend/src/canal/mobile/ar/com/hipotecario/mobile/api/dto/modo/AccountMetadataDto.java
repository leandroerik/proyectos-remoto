package ar.com.hipotecario.mobile.api.dto.modo;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public class AccountMetadataDto {

	@JsonProperty("payment_method")
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private List<String> currencyPermit;

	@JsonProperty("hasAccountLinked")
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private boolean hasAccountLinked;

	@JsonProperty("isOnboarding")
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private boolean onbordeado;

	public AccountMetadataDto() {

	}

	public AccountMetadataDto(List<String> currencyPermit, boolean hasAccountLinked, boolean onbordeado) {
		super();
		this.currencyPermit = currencyPermit;
		this.hasAccountLinked = hasAccountLinked;
		this.onbordeado = onbordeado;
	}

	public List<String> getCurrencyPermit() {
		return currencyPermit;
	}

	public void setCurrencyPermit(List<String> currencyPermit) {

		if (currencyPermit == null) {
			this.currencyPermit = Collections.emptyList();
		} else {
			this.currencyPermit = currencyPermit;
		}
	}

	public boolean isHasAccountLinked() {
		return hasAccountLinked;
	}

	public void setHasAccountLinked(boolean hasAccountLinked) {
		this.hasAccountLinked = hasAccountLinked;
	}

	public boolean isOnbordeado() {
		return onbordeado;
	}

	public void setOnbordeado(boolean onbordeado) {
		this.onbordeado = onbordeado;
	}

}
