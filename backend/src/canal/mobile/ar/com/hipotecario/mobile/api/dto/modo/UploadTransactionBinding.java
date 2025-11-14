package ar.com.hipotecario.mobile.api.dto.modo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class UploadTransactionBinding {

	@JsonProperty("fiidTerm")
	@NotNull
	@Size(min = 4, max = 4, message = "fiidTerm debe ser un identificador de 4 digitos")
	private String fiidTerm;

	@JsonProperty("termId")
	@Size(max = 16)
	private String termId;

	@NotNull
	@Size(min = 4, max = 4)
	@JsonProperty("fiidCard")
	private String fiidCard;

	@Size(max = 19, min = 1)
	@NotNull(message = "Siempre debes mandar una PAN")
	@JsonProperty("pan")
	private String pan;

	@JsonProperty("seqNum")
	@Size(min = 1, max = 12)
	private String seqNum;

	@NotNull
	@Size(min = 6, max = 6)
	@Pattern(regexp = "^([0-9][0-9])([0][1-9]|[1][0-2])([0][1-9]|[1,2][0-9]|[3][0,1])$", message = "debe cumplir con el formato YYMMDD")
	@JsonProperty("tranDat")
	private String tranDat;

	@NotNull
	@Size(min = 6, max = 6)
	@Pattern(regexp = "^(([0,1][0-9])|([2][0-3]))([0-5][0-9]){2}$")
	@JsonProperty("tranTim")
	private String tranTim;

	@NotNull
	@Size(min = 4, max = 4)
	@JsonProperty("typ")
	private String typ;

	@Size(min = 2, max = 2)
	@JsonProperty("typCde")
	private String typCde;

	@Size(min = 6, max = 6)
	@Pattern(regexp = "^([0-9][0-9])([0][1-9]|[1][0-2])([0][1-9]|[1,2][0-9]|[3][0,1])$", message = "debe cumplir con el formato YYMMDD")
	@JsonProperty("postDat")
	private String postDat;

	@NotNull
	@Size(min = 6, max = 6)
	@JsonProperty("tranCde")
	private String tranCde;

	@Size(min = 5, max = 19)
	@JsonProperty("fromAcct")
	private String fromAcct;

	@Size(min = 0, max = 1)
	@JsonProperty("tipoDep")
	private String tipoDep;

	@Size(min = 5, max = 19)
	@JsonProperty("toAcct")
	private String toAcct;

	@Size(min = 3, max = 12)
	@JsonProperty("importe")
	private String importe;

	@Size(min = 3, max = 3)
	@JsonProperty("respCde")
	private String respCde;

	@Size(min = 4, max = 4)
	@JsonProperty("issuerFiid")
	private String issuerFiid;

	@Size(min = 2, max = 2)
	@JsonProperty("termType")
	private String termType;

	@Size(min = 0, max = 8)
	@JsonProperty("tipoCambio")
	private String tipoCambio;

	@Size(min = 0, max = 8)
	@JsonProperty("tipoCambioC")
	private String tipoCambioC;

	@Size(min = 5, max = 5)
	@JsonProperty("cuota")
	private String cuota;

	@Size(min = 3, max = 3)
	@JsonProperty("ente")
	private String ente;

	@Size(min = 4, max = 4)
	@JsonProperty("termLn")
	private String termLn;

	@Size(min = 3, max = 3)
	@JsonProperty("crncyCde")
	private String crncyCde;

	@Size(min = 2, max = 2)
	@JsonProperty("cardType")
	private String cardType;

	@Size(min = 3, max = 3)
	@JsonProperty("codigoPais")
	private String codigoPais;

	@Size(min = 3, max = 3)
	@JsonProperty("codigoPaisEntidad")
	private String codigoPaisEntidad;

	@Size(min = 0, max = 13)
	@JsonProperty("locTerm")
	private String locTerm;

	@Size(min = 0, max = 25)
	@JsonProperty("denEstabl")
	private String denEstabl;

	@Size(min = 0, max = 15)
	@JsonProperty("establecimiento")
	private String establecimiento;

	@Size(min = 5, max = 5)
	@JsonProperty("rubro")
	private String rubro;

	@Size(min = 1, max = 1)
	@JsonProperty("cvvrc")
	private String cvvrc;

	@Size(min = 12, max = 12)
	@JsonProperty("direccionIp")
	private String direccionIp;

	@Size(min = 2, max = 2)
	@JsonProperty("canal")
	private String canal;

	@Size(min = 2, max = 2)
	@JsonProperty("producto")
	private String producto;

	@Size(min = 0, max = 20)
	@JsonProperty("tel")
	private String tel;

	@Size(min = 4, max = 4)
	@JsonProperty("crdLn")
	private String crdLn;

	@JsonProperty("email")
	private String email;

	public UploadTransactionBinding() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCvvrc() {
		return cvvrc;
	}

	public void setCvvrc(String cvvrc) {
		this.cvvrc = cvvrc;
	}

	public String getDireccionIp() {
		return direccionIp;
	}

	public void setDireccionIp(String direccionIp) {
		this.direccionIp = direccionIp;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getProducto() {
		return producto;
	}

	public void setProducto(String producto) {
		this.producto = producto;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getCrdLn() {
		return crdLn;
	}

	public void setCrdLn(String crdLn) {
		this.crdLn = crdLn;
	}

	public String getFiidTerm() {
		return fiidTerm;
	}

	public void setFiidTerm(String fiidTerm) {
		this.fiidTerm = fiidTerm;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public String getFiidCard() {
		return fiidCard;
	}

	public void setFiidCard(String fiidCard) {
		this.fiidCard = fiidCard;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(String seqNum) {
		this.seqNum = seqNum;
	}

	public String getTranDat() {
		return tranDat;
	}

	public void setTranDat(String tranDat) {
		this.tranDat = tranDat;
	}

	public String getTranTim() {
		return tranTim;
	}

	public void setTranTim(String tranTim) {
		this.tranTim = tranTim;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getTypCde() {
		return typCde;
	}

	public void setTypCde(String typCde) {
		this.typCde = typCde;
	}

	public String getPostDat() {
		return postDat;
	}

	public void setPostDat(String postDat) {
		this.postDat = postDat;
	}

	public String getTranCde() {
		return tranCde;
	}

	public void setTranCde(String tranCde) {
		this.tranCde = tranCde;
	}

	public String getFromAcct() {
		return fromAcct;
	}

	public void setFromAcct(String fromAcct) {
		this.fromAcct = fromAcct;
	}

	public String getTipoDep() {
		return tipoDep;
	}

	public void setTipoDep(String tipoDep) {
		this.tipoDep = tipoDep;
	}

	public String getToAcct() {
		return toAcct;
	}

	public void setToAcct(String toAcct) {
		this.toAcct = toAcct;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

	public String getRespCde() {
		return respCde;
	}

	public void setRespCde(String respCde) {
		this.respCde = respCde;
	}

	public String getIssuerFiid() {
		return issuerFiid;
	}

	public void setIssuerFiid(String issuerFiid) {
		this.issuerFiid = issuerFiid;
	}

	public String getTermType() {
		return termType;
	}

	public void setTermType(String termType) {
		this.termType = termType;
	}

	public String getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(String tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getTipoCambioC() {
		return tipoCambioC;
	}

	public void setTipoCambioC(String tipoCambioC) {
		this.tipoCambioC = tipoCambioC;
	}

	public String getCuota() {
		return cuota;
	}

	public void setCuota(String cuota) {
		this.cuota = cuota;
	}

	public String getEnte() {
		return ente;
	}

	public void setEnte(String ente) {
		this.ente = ente;
	}

	public String getTermLn() {
		return termLn;
	}

	public void setTermLn(String termLn) {
		this.termLn = termLn;
	}

	public String getCrncyCde() {
		return crncyCde;
	}

	public void setCrncyCde(String crncyCde) {
		this.crncyCde = crncyCde;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCodigoPais() {
		return codigoPais;
	}

	public void setCodigoPais(String codigoPais) {
		this.codigoPais = codigoPais;
	}

	public String getCodigoPaisEntidad() {
		return codigoPaisEntidad;
	}

	public void setCodigoPaisEntidad(String codigoPaisEntidad) {
		this.codigoPaisEntidad = codigoPaisEntidad;
	}

	public String getLocTerm() {
		return locTerm;
	}

	public void setLocTerm(String locTerm) {
		this.locTerm = locTerm;
	}

	public String getDenEstabl() {
		return denEstabl;
	}

	public void setDenEstabl(String denEstabl) {
		this.denEstabl = denEstabl;
	}

	public String getEstablecimiento() {
		return establecimiento;
	}

	public void setEstablecimiento(String establecimiento) {
		this.establecimiento = establecimiento;
	}

	public String getRubro() {
		return rubro;
	}

	public void setRubro(String rubro) {
		this.rubro = rubro;
	}

}
