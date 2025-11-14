package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.api.dto.UploadTransactionBinding;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaAsociada;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;

public class HBMonitoring {

	private final String FII_BH = "0044";
	private final String COD_OPERACION_CONSULTA_VARIAS = "0215";
	private final String COD_OPERACION_RESPUESTA_AUTORIZACION = "0210";
	private final String TIPO_OPERACION_SIN_SOBRE = "31";
	private final String APROBADO = "000";
	private final String CODIGO_PAIS_ARGENTINA = "032";
	private final String CANAL_HB = "06";
	private final String DEFAULT_BANK_ACCOUNT = "00000";// en blanco
	private final String TERMID = "HB0044";

	public Boolean sendMonitoringNoMoney(ContextoHB contexto, String pan, String codigoTransaccion, String email,
			String detail, String celular) {
		try {
			if(email == null && celular == null){
				return false;
			}

			List<TarjetaDebito> tarjetasDebitoRedLinkActivo = null;
			if (pan == null || pan.isEmpty()) {
				tarjetasDebitoRedLinkActivo = contexto.tarjetasDebitoRedLinkActivo();
			}
			if (tarjetasDebitoRedLinkActivo != null && tarjetasDebitoRedLinkActivo.isEmpty()) {
				return false;
			}
			pan = (pan == null || pan.isEmpty()) ? tarjetasDebitoRedLinkActivo.get(0).numero() : pan;

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
			upload.setFromAcct(DEFAULT_BANK_ACCOUNT);
			upload.setTipoDep(null);
			upload.setToAcct(DEFAULT_BANK_ACCOUNT);
			upload.setImporte("000");
			upload.setRespCde(APROBADO);
			upload.setIssuerFiid(FII_BH);// TODO encontrar cual banco es
			upload.setTermType("53");
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
			upload.setDenEstabl( detail != null ? detail.replaceAll("(\r\n|\n)", ""): "");
			upload.setEstablecimiento(null);
			upload.setRubro("12345");
			upload.setCvvrc("0"); // no se valido Cvv
			upload.setDireccionIp(ip);// TODO sacar ip
			upload.setCanal(CANAL_HB);
			upload.setProducto("17");
			upload.setTel(celular);
			upload.setCrdLn("LNK1");
			upload.setEmail(email);
			ApiResponse apiResponse = this.queueing(contexto, upload);
			if (apiResponse.hayError()) {
				System.out.println("Error: Enviando transacciones no monetarias a link");
			}
			return true;
		}catch (Exception e){
			return false;
		}
	}

	public void sendMonitoringAgendaBeneficiario(ContextoHB contexto, String toCbu, String beneficiaryAlias,
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
		upload.setTermType("53");
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
		upload.setCanal(CANAL_HB);
		upload.setProducto("17");
		upload.setTel(null);
		upload.setCrdLn("LNK1");// blanco
		upload.setEmail(null);
		ApiResponse apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transacciones no monetarias a link");
		}
	}

	public void sendMonitoringAltaPrestamo(ContextoHB contexto, String idSolicitudPrestamo, String importe,
			String toAcc) {
		// NCMNCA ---->NOTA CREDITO CAJA AHORRO MN 2P0011
		// prestamos por canales digitales siempre es en Caja de Ahorros
		String codigoTransaccion = "2P0011";
		List<TarjetaDebito> tarjetasDebitoRedLinkActivo = null;
		tarjetasDebitoRedLinkActivo = contexto.tarjetasDebitoRedLinkActivo();
		if (tarjetasDebitoRedLinkActivo.isEmpty()) {
			return;
		}
		TarjetaDebito tarjetaDebito = tarjetasDebitoRedLinkActivo.get(0);
		String pan = tarjetaDebito.numero();

		LocalDateTime now = LocalDateTime.now();
		String ip = this.obtainIp(contexto.ip());
		DateTimeFormatter formatter_yymmdd = DateTimeFormatter.ofPattern("YYMMdd");
		DateTimeFormatter formatter_HHmmss = DateTimeFormatter.ofPattern("HHmmss");
		UploadTransactionBinding upload = new UploadTransactionBinding();

		upload.setFiidTerm(FII_BH);
		upload.setTermId(TERMID); // probar si acepta null
		upload.setFiidCard(FII_BH);
		upload.setPan(pan);
		upload.setSeqNum(idSolicitudPrestamo);
		upload.setTranDat(now.format(formatter_yymmdd));
		upload.setTranTim(now.format(formatter_HHmmss));
		upload.setTyp(COD_OPERACION_RESPUESTA_AUTORIZACION);
		upload.setTypCde("00");
		upload.setPostDat(now.format(formatter_yymmdd));
		upload.setTranCde(codigoTransaccion);
		upload.setFromAcct(DEFAULT_BANK_ACCOUNT);
		upload.setTipoDep(null);
		upload.setToAcct(toAcc);
		upload.setImporte(resolveTlfFormat(importe, 12));
		upload.setRespCde(APROBADO);
		upload.setIssuerFiid(FII_BH);
		upload.setTermType("53");
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
		upload.setDenEstabl(null);
		upload.setEstablecimiento(null);
		upload.setRubro("00000");
		upload.setCvvrc("0"); // no se valido Cvv
		upload.setDireccionIp(ip);
		upload.setCanal(CANAL_HB);
		upload.setProducto("11");
		upload.setTel(null);
		upload.setCrdLn("LNK1");// blanco
		upload.setEmail(null);
		ApiResponse apiResponse = this.queueing(contexto, upload);
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

	public void sendMonitoringVentaDolares(ContextoHB contexto, String pan, String codigoTransaccion, String importe,
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
		upload.setTermType("53");
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
		upload.setCanal(CANAL_HB);
		upload.setProducto("17");
		upload.setTel(null);
		upload.setCrdLn("LNK1");
		upload.setEmail(null);
		ApiResponse apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transacciones no monetarias a link");
		}
	}

	public Boolean sendMonitoringDebin(ContextoHB contexto, String pan, String importe, String crncyde, String cardType,
			String typ, String cuentaOrigen, String cuentaDestino, String seqNum, String codigoTransaccion) {

		String processBank = "5";
		String issuseriid = FII_BH;
		if (cuentaDestino != null && cuentaDestino.length() == 22)
			issuseriid = cuentaDestino.substring(19, cuentaDestino.length());

		if (cuentaDestino.length() >= 19)
			cuentaDestino = cuentaDestino.substring(0, 19);

		LocalDateTime now = LocalDateTime.now();
		String ip = this.obtainIp(contexto.ip());
		DateTimeFormatter formatter_yymmdd = DateTimeFormatter.ofPattern("YYMMdd");
		DateTimeFormatter formatter_HHmmss = DateTimeFormatter.ofPattern("HHmmss");
		UploadTransactionBinding upload = new UploadTransactionBinding();
		upload.setFiidTerm(FII_BH);
		upload.setTermId(TERMID);
		upload.setFiidCard(FII_BH);
		upload.setPan(pan);
		upload.setSeqNum(seqNum); // request.idProceso
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
		upload.setIssuerFiid(processBank + issuseriid);
		upload.setTermType("53");
		upload.setTipoCambio(null);
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
		upload.setCvvrc("0");
		upload.setDireccionIp(ip);
		upload.setCanal(CANAL_HB);
		upload.setProducto("00");
		upload.setTel(null);
		upload.setCrdLn("LNK1");
		upload.setEmail(null);
		ApiResponse apiResponse = this.queueing(contexto, upload);
		if (apiResponse.hayError()) {
			System.out.println("Error: Enviando transacciones no monetarias a link");
		}

		return true;
	}

	public ApiResponse queueing(ContextoHB contexto, UploadTransactionBinding upload) {
		ApiRequest request = Api.request("queueing", "link", "POST", "/v1/servicios/queueing", contexto);
		request.permitirSinLogin = true;
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
		return Api.response(request, contexto.idCobis());
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

	public String resolveTlfFormat(String valor, int minimunLenght) {
		if (valor == null || valor.equals("")) {
			return "000000000000";
		}
		BigDecimal bd = new BigDecimal(valor);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		String formateado = bd.toString().replace(".", "");
		int initialLenght = formateado.length();
		for (int i = 0; i < minimunLenght - initialLenght; i++) {
			formateado = "0" + formateado;
		}
		return formateado;
	}
}
