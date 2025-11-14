package ar.com.hipotecario.mobile.api.dto.modo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccountDto {
	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonProperty("account_id")
	private String accountId;

	@JsonProperty("account_number")
	private String accountNumber;

	@JsonProperty("account_type")
	private String accountType;

	@JsonProperty("cbu_number")
	private String cbuNumber;

	@JsonProperty("is_favourite")
	private Boolean isFavourite;

	@JsonProperty("cbu_hash")
	private String cbuHash;

	@JsonProperty("currency_code")
	private String currencyCode;

	@JsonProperty("last_digits")
	private String lastDigits;

	@JsonProperty("balance")
	private BigDecimal balance;

	@JsonProperty("account_linked")
	private boolean accountLinked;

	public AccountDto() {
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getCbuNumber() {
		return cbuNumber;
	}

	public void setCbuNumber(String cbuNumber) {
		this.cbuNumber = cbuNumber;
	}

	public Boolean getIsFavourite() {
		return isFavourite;
	}

	public void setIsFavourite(Boolean isFavourite) {
		this.isFavourite = isFavourite;
	}

	public String getCbuHash() {
		return cbuHash;
	}

	public void setCbuHash(String cbuHash) {
		this.cbuHash = cbuHash;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getLastDigits() {
		return lastDigits;
	}

	public void setLastDigits(String lastDigits) {
		this.lastDigits = lastDigits;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public boolean isAccountLinked() {
		return accountLinked;
	}

	public void setAccountLinked(boolean accountLinked) {
		this.accountLinked = accountLinked;
	}

}
