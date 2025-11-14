package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.dto.modo.UploadTransactionBinding;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.negocio.CuentaAsociada;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class MBMonitoring {

	private final String FII_BH = "0044";
	private final String COD_OPERACION_CONSULTA_VARIAS = "0215";
//	private final String COD_OPERACION_RESPUESTA_AUTORIZACION = "0210";

	private final String TIPO_OPERACION_SIN_SOBRE = "31";
	private final String APROBADO = "000";
	private final String CODIGO_PAIS_ARGENTINA = "032";
	private final String CANAL_BM = "11";
	private final String DEFAULT_BANK_ACCOUNT = "00000";// en blanco
	private final String TERMID = "MB0044";

	public void sendMonitoringNoMoney(ContextoMB contexto, String pan, String codigoTransaccion, String email,
			String detail, String celular) {
		if(email == null && celular == null) return;

		List<TarjetaDebito> tarjetasDebitoRedLinkActivo = null;
		if (pan == null || pan.isEmpty()) {
			tarjetasDebitoRedLinkActivo = contexto.tarjetasDebitoRedLinkActivo();
		}
		if (tarjetasDebitoRedLinkActivo.isEmpty()) {
			return;
		}
		pan = (pan == null || pan.isEmpty()) ? tarjetasDebitoRedLinkActivo.get(0).numero() : pan;

		LocalDateTime now = LocalDateTime.now();
		String ip = this.obtainIp(contexto.ip());
		// DateTimeFormatter formatter_yyyymmdd =
		// DateTimeFormatter.ofPattern("YYYYMMdd");
		DateTimeFormatter formatter_yymmdd = DateTimeFormatter.ofPattern("YYMMdd");
		DateTimeFormatter formatter_HHmmss = DateTimeFormatter.ofPattern("HHmmss");
		UploadTransactionBinding upload = new UploadTransactionBinding();

		upload.setFiidTerm(FII_BH);
		upload.setTermId(TERMID); // probar si acepta null
		upload.setFiidCard(FII_BH);
		upload.setPan(pan);
		upload.setSeqNum(null);
		upload.setTranDat(now.format(formatter_yymmdd));
		upload.setTranTim(now.format(formatter_HHmmss));
		upload.setTyp(COD_OPERACION_CONSULTA_VARIAS);
		upload.setTypCde(TIPO_OPERACION_SIN_SOBRE);
		upload.setPostDat(now.format(formatter_yymmdd));
		upload.setTranCde(codigoTransaccion);
		upload.setFromAcct(DEFAULT_BANK_ACCOUNT);
		upload.setTipoDep(null);
		upload.setToAcct(DEFAULT_BANK_ACCOUNT);
		upload.setImporte("000");
		upload.setRespCde(APROBADO);
		upload.setIssuerFiid(FII_BH);
		upload.setTermType("82");
		upload.setTipoCambio(null);
		upload.setTipoCambioC(null);
		upload.setCuota("00000");
		upload.setEnte("000");//
		upload.setTermLn("LNK1");
		upload.setCrncyCde(CODIGO_PAIS_ARGENTINA);
		upload.setCardType(" P");// tarjeta debito
		upload.setCodigoPais(CODIGO_PAIS_ARGENTINA);
		upload.setCodigoPaisEntidad(CODIGO_PAIS_ARGENTINA);
		upload.setLocTerm(null);
		upload.setDenEstabl(detail != null ? detail.replaceAll("(\r\n|\n)", ""):null);
		upload.setEstablecimiento(null);
		upload.setRubro("12345");
		upload.setCvvrc("0"); // no se valido Cvv
		upload.setDireccionIp(ip);
		upload.setCanal(CANAL_BM);
		upload.setProducto("17");
		upload.setTel(celular);
		upload.setCrdLn("LNK1");// blanco
		upload.setEmail(email);
		ApiResponseMB apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transacciones no monetarias a link");
		}
	}

	public void sendMonitoringAgendaBeneficiario(ContextoMB contexto, String toCbu, String beneficiaryAlias,
			String establishment) {
		String codigoTransaccion = "YG0000";
		String processBank = "5";
		String toAcc = DEFAULT_BANK_ACCOUNT;
		String fromAcc = DEFAULT_BANK_ACCOUNT;

		String issuseriid = FII_BH;
		if (toCbu != null && toCbu.length() == 22) {
			toAcc = toCbu.substring(0, 19);
			issuseriid = toCbu.substring(19, toCbu.length());
		}

		List<TarjetaDebito> tarjetasDebitoRedLinkActivo = null;
		tarjetasDebitoRedLinkActivo = contexto.tarjetasDebitoRedLinkActivo();
		if (tarjetasDebitoRedLinkActivo.isEmpty()) {
			return;
		}
		TarjetaDebito tarjetaDebito = tarjetasDebitoRedLinkActivo.get(0);
		String pan = tarjetaDebito.numero();
		CuentaAsociada cuentasAsociadas = this.obtenerCuentaPrincipal(tarjetaDebito);
		if (cuentasAsociadas != null) {
			fromAcc = cuentasAsociadas.getNumero();
		}

		LocalDateTime now = LocalDateTime.now();
		String ip = this.obtainIp(contexto.ip());
		DateTimeFormatter formatter_yymmdd = DateTimeFormatter.ofPattern("YYMMdd");
		DateTimeFormatter formatter_HHmmss = DateTimeFormatter.ofPattern("HHmmss");
		UploadTransactionBinding upload = new UploadTransactionBinding();

		upload.setFiidTerm(FII_BH);
		upload.setTermId(TERMID); // probar si acepta null
		upload.setFiidCard(FII_BH);
		upload.setPan(pan);
		upload.setSeqNum(null);
		upload.setTranDat(now.format(formatter_yymmdd));
		upload.setTranTim(now.format(formatter_HHmmss));
		upload.setTyp(COD_OPERACION_CONSULTA_VARIAS);
		upload.setTypCde(TIPO_OPERACION_SIN_SOBRE);
		upload.setPostDat(now.format(formatter_yymmdd));
		upload.setTranCde(codigoTransaccion);
		upload.setFromAcct(fromAcc);
		upload.setTipoDep(null);
		upload.setToAcct(toAcc);
		upload.setImporte("000");
		upload.setRespCde(APROBADO);
		upload.setIssuerFiid(processBank + issuseriid);
		upload.setTermType("82");
		upload.setTipoCambio(null);
		upload.setTipoCambioC(null);
		upload.setCuota("00000");
		upload.setEnte("000");//
		upload.setTermLn("LNK1");
		upload.setCrncyCde(CODIGO_PAIS_ARGENTINA);
		upload.setCardType(" P");// tarjeta debito
		upload.setCodigoPais(CODIGO_PAIS_ARGENTINA);
		upload.setCodigoPaisEntidad(CODIGO_PAIS_ARGENTINA);
		upload.setLocTerm(null);
		upload.setDenEstabl(beneficiaryAlias.replaceAll("(\r\n|\n)", ""));
		upload.setEstablecimiento(establishment);
		upload.setRubro("12345");
		upload.setCvvrc("0"); // no se valido Cvv
		upload.setDireccionIp(ip);
		upload.setCanal(CANAL_BM);
		upload.setProducto("17");
		upload.setTel(null);
		upload.setCrdLn("LNK1");// blanco
		upload.setEmail(null);
		ApiResponseMB apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transacciones no monetarias a link");
		}
	}

	public void sendMonitoringVentaDolares(ContextoMB contexto, String pan, String codigoTransaccion, String importe,
			String tipoCambio, String crncyde, String cardType, String typ, String cuentaOrigen, String cuentaDestino,
			String seqNum) {
		LocalDateTime now = LocalDateTime.now();
		String ip = this.obtainIp(contexto.ip());
		DateTimeFormatter formatter_yymmdd = DateTimeFormatter.ofPattern("YYMMdd");
		DateTimeFormatter formatter_HHmmss = DateTimeFormatter.ofPattern("HHmmss");
		UploadTransactionBinding upload = new UploadTransactionBinding();
		upload.setFiidTerm(FII_BH);
		upload.setTermId(TERMID); // probar si acepta null
		upload.setFiidCard(FII_BH);
		upload.setPan(pan);
		upload.setSeqNum(seqNum);
		upload.setTranDat(now.format(formatter_yymmdd));
		upload.setTranTim(now.format(formatter_HHmmss));
		upload.setTyp(StringUtils.isNotBlank(typ) ? typ : COD_OPERACION_CONSULTA_VARIAS);
		upload.setTypCde("00");
		upload.setPostDat(now.format(formatter_yymmdd));
		upload.setTranCde(codigoTransaccion);
		upload.setFromAcct(cuentaOrigen);
		upload.setTipoDep(null);
		upload.setToAcct(cuentaDestino);
		upload.setImporte(StringUtils.isNotBlank(importe) ? importe : "000");
		upload.setRespCde(APROBADO);
		upload.setIssuerFiid("5000");// TODO encontrar cual banco es
		upload.setTermType("82");
		upload.setTipoCambio(tipoCambio);
		upload.setTipoCambioC(null);
		upload.setCuota("00000");
		upload.setEnte("000");
		upload.setTermLn("LNK1");
		upload.setCrncyCde(StringUtils.isNoneBlank(crncyde) ? crncyde : CODIGO_PAIS_ARGENTINA);
		upload.setCardType(StringUtils.isNotBlank(cardType) ? cardType : " P");// tarjeta debito
		upload.setCodigoPais(CODIGO_PAIS_ARGENTINA);
		upload.setCodigoPaisEntidad(CODIGO_PAIS_ARGENTINA);
		upload.setLocTerm(null);
		upload.setDenEstabl(null);
		upload.setEstablecimiento(null);
		upload.setRubro("00000");
		upload.setCvvrc("0"); // no se valido Cvv
		upload.setDireccionIp(ip);// TODO sacar ip
		upload.setCanal(CANAL_BM);
		upload.setProducto("17");
		upload.setTel(null);
		upload.setCrdLn("LNK1");
		upload.setEmail(null);
		ApiResponseMB apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transacciones no monetarias a link");
		}
	}

	public CuentaAsociada obtenerCuentaPrincipal(TarjetaDebito tarjetaDebito) {
		List<CuentaAsociada> cuentasAsociadas = tarjetaDebito.cuentasAsociadasPorIdTarjeta();
		if (cuentasAsociadas == null || cuentasAsociadas.isEmpty()) {
			System.out.println("No tiene ni una cuenta asociada RARO");

			return null;
		}
		List<CuentaAsociada> cuentasPrincipales = cuentasAsociadas.stream().filter(CuentaAsociada::getPrincipal)
				.collect(Collectors.toList());
		if (!cuentasPrincipales.isEmpty()) {
			System.out.println("DEVOLVIENDO PRIMERA CUENTA PRINCIPAL");
			return cuentasPrincipales.get(0);
		}
		System.out.println("DEVOLVIENDO PRIMERA CUALQUIERA PORQUE NO TENIA PRINCIPAL");
		return cuentasAsociadas.get(0);
	}

	public void sendMonitoringTransfer(ContextoMB contexto, String pan, String seqNum, String codigoTransaccion,
			String cuentaOrigen, String cuentaDestino, BigDecimal monto, String producto) {
		List<TarjetaDebito> tarjetasDebitoRedLinkActivo = null;
		if (pan == null || pan.isEmpty()) {
			tarjetasDebitoRedLinkActivo = contexto.tarjetasDebitoRedLinkActivo();
		}
		if (tarjetasDebitoRedLinkActivo.isEmpty()) {
			return;
		}
		pan = (pan == null || pan.isEmpty()) ? tarjetasDebitoRedLinkActivo.get(0).numero() : pan;

		LocalDateTime now = LocalDateTime.now();
		String ip = this.obtainIp(contexto.ip());
		DateTimeFormatter formatter_yymmdd = DateTimeFormatter.ofPattern("YYMMdd");
		DateTimeFormatter formatter_HHmmss = DateTimeFormatter.ofPattern("HHmmss");
		UploadTransactionBinding upload = new UploadTransactionBinding();
		upload.setFiidTerm(FII_BH);
		upload.setTermId(TERMID);
		upload.setFiidCard(FII_BH);
		upload.setPan(pan);
		upload.setSeqNum(seqNum);
		upload.setTranDat(now.format(formatter_yymmdd));
		upload.setTranTim(now.format(formatter_HHmmss));
		upload.setTyp(COD_OPERACION_CONSULTA_VARIAS);
		upload.setTypCde(TIPO_OPERACION_SIN_SOBRE);
		upload.setPostDat(now.format(formatter_yymmdd));
		upload.setTranCde(codigoTransaccion);
		upload.setFromAcct(cuentaOrigen);
		upload.setTipoDep(null);
		upload.setToAcct(cuentaDestino);
		upload.setImporte(monto.toString());// TODO ver que cumpla lo minimo para 3 digitos
		upload.setRespCde(APROBADO);
		upload.setIssuerFiid(FII_BH);// TODO encontrar cual banco es
		upload.setTermType("82");
		upload.setTipoCambio(null);
		upload.setTipoCambioC(null);
		upload.setCuota("00000");
		upload.setEnte("000");
		upload.setTermLn("LNK1");
		upload.setCrncyCde(CODIGO_PAIS_ARGENTINA);
		upload.setCardType(" P");// tarjeta debito
		upload.setCodigoPais(CODIGO_PAIS_ARGENTINA);
		upload.setCodigoPaisEntidad(CODIGO_PAIS_ARGENTINA);
		upload.setLocTerm(null);
		upload.setDenEstabl("");
		upload.setEstablecimiento(null);
		upload.setRubro("12345");
		upload.setCvvrc("0"); // no se valido Cvv
		upload.setDireccionIp(ip);
		upload.setCanal(CANAL_BM);
		upload.setProducto(producto);
		upload.setTel(null);
		upload.setCrdLn("LNK1");
		upload.setEmail(null);
		ApiResponseMB apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transferencia a queueing");
		}
	}

	public ApiResponseMB queueing(ContextoMB contexto, UploadTransactionBinding upload) {
		ApiRequestMB request = ApiMB.request("queueing", "link", "POST", "/v1/servicios/queueing", contexto);
		request.body("fiidTerm", upload.getFiidTerm());
		request.body("termId", upload.getTermId());
		request.body("fiidCard", upload.getFiidCard());
		request.body("pan", upload.getPan());
		request.body("seqNum", (upload.getSeqNum() == null) ? request.headers.get("x-idProceso") : upload.getSeqNum());
		request.body("tranDat", upload.getTranDat());
		request.body("tranTim", upload.getTranTim());
		request.body("typ", upload.getTyp());
		request.body("typCde", upload.getTypCde());
		request.body("postDat", upload.getPostDat());
		request.body("tranCde", upload.getTranCde());
		request.body("fromAcct", upload.getFromAcct());
		request.body("tipoDep", upload.getTipoDep());
		request.body("toAcct", upload.getToAcct());
		request.body("importe", upload.getImporte());
		request.body("respCde", upload.getRespCde());
		request.body("issuerFiid", upload.getIssuerFiid());
		request.body("termType", upload.getTermType());
		request.body("tipoCambio", upload.getTipoCambio());
		request.body("tipoCambioC", upload.getTipoCambioC());
		request.body("cuota", upload.getCuota());
		request.body("ente", upload.getEnte());
		request.body("termLn", upload.getTermLn());
		request.body("crncyCde", upload.getCrncyCde());
		request.body("cardType", upload.getCardType());
		request.body("codigoPais", upload.getCodigoPais());
		request.body("codigoPaisEntidad", upload.getCodigoPaisEntidad());
		request.body("locTerm", upload.getLocTerm());
		request.body("denEstabl", upload.getDenEstabl());
		request.body("establecimiento", upload.getEstablecimiento());
		request.body("rubro", upload.getRubro());
		request.body("cvvrc", upload.getCvvrc());
		request.body("direccionIp", upload.getDireccionIp());
		request.body("canal", upload.getCanal());
		request.body("producto", upload.getProducto());
		request.body("tel", upload.getTel());
		request.body("crdLn", upload.getCrdLn());
		request.body("email", upload.getEmail());
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	private String obtainIp(String rawIp) {
		if (rawIp == null || rawIp.startsWith("fe80:") || rawIp.contains(":")) {
			return "127000000001";
		}
		List<String> newList = Arrays.asList(rawIp.split("\\.")).stream().map(item -> {
			while (item.length() < 3) {
				item = "0" + item;
			}
			return item;
		}).collect(Collectors.toList());

		String result = "";
		for (String it : newList) {
			result += it;
		}
		return result;
	}

	public String resolveImporte(String importe) {
		if (importe == null || importe.length() == 0) {
			return "000";
		}
		if (importe.length() < 3) {
			return "00" + importe;
		}
		return importe;
	}
}
