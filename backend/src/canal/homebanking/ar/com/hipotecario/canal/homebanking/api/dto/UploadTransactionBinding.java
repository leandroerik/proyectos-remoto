package ar.com.hipotecario.canal.homebanking.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class UploadTransactionBinding {

	@JsonProperty("fiidTerm")
	private String fiidTerm;

	@JsonProperty("termId")
	private String termId;

	@JsonProperty("fiidCard")
	private String fiidCard;

	@JsonProperty("pan")
	private String pan;

	@JsonProperty("seqNum")
	private String seqNum;

	@JsonProperty("tranDat")
	private String tranDat;

	@JsonProperty("tranTim")
	private String tranTim;

	@JsonProperty("typ")
	private String typ;

	@JsonProperty("typCde")
	private String typCde;

	@JsonProperty("postDat")
	private String postDat;

	@JsonProperty("tranCde")
	private String tranCde;

	@JsonProperty("fromAcct")
	private String fromAcct;

	@JsonProperty("tipoDep")
	private String tipoDep;

	@JsonProperty("toAcct")
	private String toAcct;

	@JsonProperty("importe")
	private String importe;

	@JsonProperty("respCde")
	private String respCde;

	@JsonProperty("issuerFiid")
	private String issuerFiid;

	@JsonProperty("termType")
	private String termType;

	@JsonProperty("tipoCambio")
	private String tipoCambio;

	@JsonProperty("tipoCambioC")
	private String tipoCambioC;

	@JsonProperty("cuota")
	private String cuota;

	@JsonProperty("ente")
	private String ente;

	@JsonProperty("termLn")
	private String termLn;

	@JsonProperty("crncyCde")
	private String crncyCde;

	@JsonProperty("cardType")
	private String cardType;

	@JsonProperty("codigoPais")
	private String codigoPais;

	@JsonProperty("codigoPaisEntidad")
	private String codigoPaisEntidad;

	@JsonProperty("locTerm")
	private String locTerm;

	@JsonProperty("denEstabl")
	private String denEstabl;

	@JsonProperty("establecimiento")
	private String establecimiento;

	@JsonProperty("rubro")
	private String rubro;

	@JsonProperty("cvvrc")
	private String cvvrc;

	@JsonProperty("direccionIp")
	private String direccionIp;

	@JsonProperty("canal")
	private String canal;

	@JsonProperty("producto")
	private String producto;

	@JsonProperty("tel")
	private String tel;

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
