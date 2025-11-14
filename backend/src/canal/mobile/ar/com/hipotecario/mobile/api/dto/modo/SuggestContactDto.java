package ar.com.hipotecario.mobile.api.dto.modo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class SuggestContactDto {

	@JsonProperty("id")
	private String id;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("on_board")
	private Boolean onBoard;

	@JsonProperty("phone_number")
	private String phoneNumber;

	@JsonProperty("total_transfers")
	private Integer totalTransfers;

	public SuggestContactDto() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Boolean getOnBoard() {
		return onBoard;
	}

	public void setOnBoard(Boolean onBoard) {
		this.onBoard = onBoard;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Integer getTotalTransfers() {
		return totalTransfers;
	}

	public void setTotalTransfers(Integer totalTransfers) {
		this.totalTransfers = totalTransfers;
	}

}
