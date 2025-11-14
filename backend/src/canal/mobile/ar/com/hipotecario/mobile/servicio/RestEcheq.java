package ar.com.hipotecario.mobile.servicio;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Persona;

public class RestEcheq {

	private static DateTimeFormatter formatoJapFullTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static DateTimeFormatter formatoSpa = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public static ApiResponseMB obtenerEcheqsListas(ContextoMB contexto, int pagina) {
		ApiRequestMB request = ApiMB.request("V1ChequeListaGet", "cheques", "GET", "/v1/cheque/lista", contexto);
		String cuitCliente = contexto.persona().cuit();
		String select = "";
		String orderby = "";
		String filtro = "";
		String cant = "20";
		select = "cheques.cuenta_emisora.emisor_razon_social," + "cheques.cuenta_emisora.emisor_cuit," + "cheques.numero_chequera," + "cheques.cheque_id," + "cheques.cheque_numero," + "cheques.agrupador_id," + "cheques.certificado_emitido," + "cheques.estado," + "cheques.cuenta_emisora.emisor_moneda," + "cheques.cuenta_emisora.emisor_cuenta," + "cheques.cuenta_emisora.emisor_cp," + "cheques.cuenta_emisora.emisor_cbu," + "cheques.cuenta_emisora.emisor_domicilio," + "cheques.cuenta_emisora.emisor_subcuenta," + "cheques.cuenta_emisora.sucursal_codigo," + "cheques.cuenta_emisora.sucursal_cp," + "cheques.cuenta_emisora.sucursal_domicilio," + "cheques.cuenta_emisora.sucursal_nombre," + "cheques.cuenta_emisora.sucursal_provincia," + "cheques.emitido_a.beneficiario_nombre,"
				+ "cheques.emitido_a.beneficiario_documento_tipo," + "cheques.emitido_a.beneficiario_documento," + "cheques.tenencia.beneficiario_nombre," + "cheques.tenencia.beneficiario_documento_tipo," + "cheques.tenencia.beneficiario_documento," + "cheques.monto," + "cheques.motivo_anulacion," + "cheques.motivo_repudio_emision," + "cheques.fecha_emision," + "cheques.fecha_pago," + "cheques.fecha_pago_vencida," + "cheques.fecha_ult_modif," + "cheques.cheque_concepto," + "cheques.cheque_motivo_pago," + "cheques.cheque_caracter," + "cheques.cod_visualizacion," + "cheques.cuenta_emisora.banco_codigo," + "cheques.cuenta_emisora.banco_nombre," + "cheques.cheque_modo," + "cheques.cmc7," + "cheques.cuenta_emisora.sucursal_cp," + "cheques.cheque_tipo," + "cheques.codigo_emision,"
				+ "cheques.solicitando_acuerdo," + "cheques.acuerdo_rechazado," + "cheques.cheque_acordado," + "cheques.repudio_endoso," + "cheques.onp," + "cheques.endosos," + "cheques.endosos.tipo_endoso," + "cheques.endosos.fecha_hora," + "cheques.endosos.benef_documento_tipo," + "cheques.endosos.benef_documento," + "cheques.endosos.estado_endoso," + "cheques.endosos.motivo_repudio," + "cheques.re_presentar";
		String desdeStr = "";
		if (contexto.parametros.string("start_date", null) == null
				&& contexto.parametros.string("startDate", null) == null) {
			LocalDate fechadesde = LocalDate.now().minusDays(30);
			desdeStr = fechadesde.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		} else {
			String _date = contexto.parametros.string("start_date").isEmpty() ? contexto.parametros.string("startDate") : contexto.parametros.string("start_date");
			desdeStr = LocalDate.parse(_date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		}
		filtro = "cheques.fecha_pago%20ge%20__" + desdeStr + "__";
		if (contexto.parametros.string("status", null) != null && !("TODOS".equals(contexto.parametros.string("status")))) {
			filtro += "%20and%20cheques.estado%20eq%20__" + statusMapper(contexto.parametros.string("status"), true) + "__";
		}
		if (contexto.parametros.string("num_chequera") != null && !contexto.parametros.string("num_chequera").isEmpty()) {
			filtro += "%20and%20cheques.numero_chequera%20eq%20__" + contexto.parametros.string("num_chequera") + "__";
		}
		filtro += "%20and%20cuit%20eq%20__" + cuitCliente + "__";
		orderby = "cheques.fecha_pago!";
		request.query("$select", select);
		request.query("$filter", filtro);
		request.query("$orderby", orderby);
		request.query("$pag", Integer.toString(pagina));
		request.query("$cant", cant);
		return ApiMB.response(request);
	}

	public static ApiResponseMB obtenerEcheqsCuentas(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1CuentaGet", "cheques", "GET", "/v1/cuenta", contexto);
		String cuitCliente = contexto.persona().cuit();
		String select = "cuentas.emisor_cbu,cuentas.emisor_cuenta,cuentas.cuenta_estado,cuentas.emisor_moneda";
		String filtro = "cuentas.emisor_cuit%20eq%20__" + cuitCliente + "__";
		request.query("$select", select);
		request.query("$filter", filtro);
		return ApiMB.response(request);
	}

	public static ApiResponseMB obtenerEcheqsChequeras(ContextoMB contexto, Cuenta cuenta) {
		ApiRequestMB request = ApiMB.request("V1ChequeraGet", "cheques", "GET", "/v1/chequera", contexto);
		request.query("operacion", "C");
		request.query("cta_banco", cuenta.numero());
		request.query("ente", contexto.idCobis());
		return ApiMB.response(request);
	}

	public static ApiResponseMB aceptar(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeAdmitirPost", "cheques", "POST", "/v1/cheque/emitido/admitir", contexto);
		String cuitCliente = contexto.persona().cuit();
		Objeto body = new Objeto();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("beneficiario_documento_tipo", "cuit");
		body.set("beneficiario_documento", cuitCliente);
		body.set("firmantes", firmantes);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB rechazar(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeRepudiarPost", "cheques", "POST", "/v1/cheque/emitido/repudio", contexto);
		String cuitCliente = contexto.persona().cuit();
		String motivo = contexto.parametros.string("motivo_repudio", null);
		Objeto body = new Objeto();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("beneficiario_documento_tipo", "cuit");
		body.set("beneficiario_documento", cuitCliente);
		if (motivo == null || motivo.trim().isEmpty()) {
			motivo = "Varios";
		}
		body.set("cheque_motivo_repudio", motivo);
		body.set("firmantes", firmantes);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB habilitarCuenta(ContextoMB contexto, Objeto localAccount, String domicilio, String cp) {
		ApiRequestMB request = ApiMB.request("V1CuentaPost", "cheques", "POST", "/v1/cuenta", contexto);
		Objeto body = new Objeto();
		body.set("sucursal_codigo", localAccount.string("sucursalCodigo"));
		body.set("sucursal_nombre", localAccount.string("sucursalNombre"));
		body.set("sucursal_domicilio", localAccount.string("sucursalDomicilio"));
		body.set("sucursal_cp", localAccount.string("sucursalCp"));
		body.set("sucursal_provincia", localAccount.string("sucursalProvincia"));
		body.set("emisor_cuit", localAccount.string("emisorCuit"));
		body.set("emisor_razon_social", localAccount.string("emisorRazonSocial"));
		body.set("emisor_cbu", localAccount.string("emisorCbu"));
		body.set("emisor_cuenta", localAccount.string("emisorCuenta"));
		body.set("emisor_subcuenta", "");
		body.set("emisor_cuenta_fecha_alta", localAccount.string("emisorCuentaFechaAlta"));
		body.set("emisor_moneda", "032");
		body.set("emisor_domicilio", domicilio);
		body.set("emisor_cp", cp);
		body.set("emisor_emails", new ArrayList<Objeto>());
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB deshabilitarCuenta(ContextoMB contexto, String cbu, String cuit) {
		ApiRequestMB request = ApiMB.request("V1CuentaDeleteByCbu", "cheques", "DELETE", "/v1/cuenta/{cbu}/{cuit}", contexto);
		request.path("cbu", cbu);
		request.path("cuit", cuit);
		return ApiMB.response(request);
	}

	public static ApiResponseMB depositarActivo(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeActivoDepositarPost", "cheques", "POST", "/v1/cheque/activo/depositar", contexto);
		Objeto body = new Objeto();
		Persona beneficiario = contexto.persona();
		Cuenta cuenta = contexto.cuenta(contexto.parametros.string("id_producto"));
		String cuitCliente = beneficiario.cuit();
		String accountNumber = cuenta.numero();
		String branch = cuenta.sucursal();
		String cbu = cuenta.cbu();
		List<Objeto> depositos = new ArrayList<>();
		Objeto deposito = new Objeto();
		deposito.set("cheque_id", contexto.parametros.string("id_cheque"));
		deposito.set("beneficiario_documento_tipo", "cuit");
		deposito.set("beneficiario_documento", cuitCliente);
		deposito.set("beneficiario_cbu", cbu);
		depositos.add(deposito);
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("depositos", depositos);
		body.set("firmantes", firmantes);
		body.set("oficina_dep", Integer.parseInt(branch));
		body.set("cta_depositada", accountNumber);
		body.set("producto", cuenta.esCajaAhorro() ? 4 : 3);
		body.set("nro_cheque_banco", contexto.parametros.string("cheque_numero"));
		body.set("id_echeq", contexto.parametros.string("id_cheque"));
		body.set("codigo_visualizacion", contexto.parametros.string("cod_visualizacion", ""));
		body.set("codbanco", contexto.parametros.string("banco_codigo"));
		body.set("cod_postal", contexto.parametros.string("sucursal_cp"));
		body.set("cta_girada", contexto.parametros.string("emisor_cuenta"));
		body.set("importe", contexto.parametros.bigDecimal("monto"));
		body.set("tipo_echeq", contexto.parametros.string("cheque_tipo"));

		body.set("fecha_pago", LocalDate.parse(contexto.parametros.string("fecha_pago"), formatoSpa).format(DateTimeFormatter.ISO_LOCAL_DATE));
		body.set("num_boleta", 0);
		body.set("cmc7", contexto.parametros.string("cmc7"));
		body.set("modo", contexto.parametros.string("cheque_modo", ""));
		body.set("motivoPago", contexto.parametros.string("motivo_pago", ""));
		body.set("concepto", "Varios");
		body.set("caracter", contexto.parametros.string("cheque_caracter", ""));
		body.set("moneda", 80);
		body.set("fecha_presentacion", LocalDate.now().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		body.set("usuario", contexto.idCobis());
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB obtenerChequera(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeraGet", "cheques", "GET", "/v1/chequera", contexto);
		Cuenta cuenta = contexto.cuenta(contexto.parametros.string("id_producto", null));
		String accountNumber = "0";
		if (cuenta != null) {
			accountNumber = cuenta.numero();
		}
		request.query("operacion", "O");
		request.query("cta_banco", accountNumber);
		request.query("ente", contexto.idCobis());
		return ApiMB.response(request);
	}

	public static ApiResponseMB postChequera(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeraPost", "cheques", "POST", "/v1/chequera", contexto);
		Objeto body = new Objeto();
		String idProducto = contexto.parametros.string("id_producto");
		Cuenta cuenta = contexto.cuenta(idProducto);
		String accountNumber = cuenta.numero();
		String branch = cuenta.sucursal();
		int codSucInt = Integer.parseInt(branch);
		body.set("cod_banco", "044");
		body.set("cta_banco", accountNumber);
		body.set("moneda", 80);
		body.set("oficina", codSucInt);
		body.set("serie", null);
		body.set("tipo_chequera", contexto.parametros.string("tipo_chequera"));
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB obtenerChequeBancarizado(ContextoMB contexto, String documento, String tipoDocumento) {
		ApiRequestMB request = ApiMB.request("V1ChequeBancarizadoGetByTipo_documentoAndDocumento", "cheques", "GET", "/v1/cheque/bancarizado/{tipo_documento}/{documento}", contexto);
		request.path("tipo_documento", tipoDocumento);
		request.path("documento", documento);
		return ApiMB.response(request);
	}

	public static ApiResponseMB generar(ContextoMB contexto) throws ParseException {
		ApiRequestMB request = ApiMB.request("V1ChequePost", "cheques", "POST", "/v1/cheque?enviarMail=true", contexto);
		Persona cliente = contexto.persona();
		Cuenta cuenta = contexto.cuenta(contexto.parametros.string("id_producto", null));
//		SimpleDateFormat salidaSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		if (cuenta == null) {
			return obtenerRespuestaError("CUENTA_NO_EXISTENTE");
		}
		String cuitCliente = cliente.cuit();
		String nombreCliente = cliente.nombreCompleto();
		String accountNumber = cuenta.numero();
		String cbu = cuenta.cbu();
		String fechaPago = contexto.parametros.string("fecha_pago", LocalDateTime.now().format(formatoSpa));
		LocalDateTime fechaPagoDate;
		String fechaPagoStr = null;

		fechaPagoDate = LocalDate.parse(fechaPago, formatoSpa).atStartOfDay();
		fechaPagoStr = fechaPagoDate.format(formatoJapFullTime);

		String tipoCheque = fechaPagoDate.isAfter(LocalDateTime.now()) ? "CPD" : "CC";

		String branchCP = "1003";
		String cuentaSucursal = obtenerSucursalPorVe(cuenta.sucursal(), cliente.sucursal(), contexto);
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_mejora_echeq")) {
			Optional<Objeto> sucursalObj = obtenerSucursalPersona(contexto, cuentaSucursal);
			if (sucursalObj.isPresent()) {
				branchCP = sucursalObj.get().get("CodigoPostalSucursal").toString();
			}
		} else {
			for (Objeto sucursal : obtenerSucursales(contexto, cuentaSucursal)) {
				if (sucursal.string("codSucursal", "").equals(cuentaSucursal)) {
					branchCP = sucursal.string("CodigoPostalSucursal");
				}
			}
		}
		Objeto body = new Objeto();
		List<Objeto> firmantes = new ArrayList<>();
		List<Objeto> numeraciones = new ArrayList<>();
		List<Objeto> chqReferenciasPago = new ArrayList<>();
		Objeto numeracion = new Objeto();
		numeracion.set("cheque_numero", "");
		numeracion.set("cmc7", "");
		numeraciones.add(numeracion);
		Objeto chqReferenciaPago = new Objeto();
		chqReferenciaPago.set("valor", "");
		chqReferenciaPago.set("referencia", "");
		chqReferenciasPago.add(chqReferenciaPago);
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("numero_chequera", contexto.parametros.string("numero_chequera"));
		body.set("emisor_cuit", cuitCliente);
		body.set("emisor_nombre_apellidos", nombreCliente);
		body.set("emisor_cbu", cbu);
		body.set("fecha_pago", fechaPagoStr);
		body.set("monto", contexto.parametros.bigDecimal("monto"));
		body.set("beneficiario_documento_tipo", "cuit");
		body.set("beneficiario_documento", contexto.parametros.string("beneficiario_documento"));
		body.set("beneficiario_nombre_apellidos", contexto.parametros.string("beneficiario_nombre_apellidos", "").trim());
		body.set("beneficiario_cuit", contexto.parametros.string("beneficiario_documento"));
		body.set("beneficiario_email", contexto.parametros.string("beneficiario_email", ""));
		body.set("cheque_version", "001");
		body.set("cheque_instrumento_tipo", "ECHEQ");
		body.set("cheque_tipo", tipoCheque);
		body.set("cheque_caracter", "a la orden");
		if (contexto.parametros.string("motivo_pago", "").trim().isEmpty()) {
			body.set("cheque_motivo_pago", "Varios");
		} else {
			body.set("cheque_motivo_pago", contexto.parametros.string("motivo_pago"));
		}
		body.set("cheque_concepto", "Varios");
		body.set("cheque_cruzado", true);
		body.set("firmantes", firmantes);
		body.set("numeracion", numeraciones);
		body.set("moneda", 80);
		body.set("id_ente_cobis", Integer.parseInt(contexto.idCobis()));
		body.set("cta_banco", accountNumber);
		body.set("sucursal", String.format("%03d", Integer.parseInt(cuentaSucursal)));
		body.set("cp_sucursal", branchCP);
		body.set("chq_referencias_pago", chqReferenciasPago);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB cancelar(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeEmitidoAnularPost", "cheques", "POST", "/v1/cheque/emitido/anular", contexto);
		Objeto body = new Objeto();
		Persona cliente = contexto.persona();
		String cuitCliente = cliente.cuit();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("emisor_documento_tipo", "cuit");
		body.set("emisor_documento", cuitCliente);
		if (contexto.parametros.string("motivo_anulacion", "").trim().isEmpty()) {
			body.set("cheque_motivo_anulacion", "Varios");
		} else {
			body.set("cheque_motivo_anulacion", (String) contexto.parametros.string("motivo_anulacion"));
		}
		body.set("firmantes", firmantes);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB devolverPeticion(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeDevolucionSolicitarPost", "cheques", "POST", "/v1/cheque/devolucion/solicitar?enviarMail=true", contexto);
		Objeto body = new Objeto();
		Persona cliente = contexto.persona();
		String nombreCliente = cliente.nombreCompleto();
		String cuitCliente = cliente.cuit();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("firmantes", firmantes);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("emisor_documento_tipo", "cuit");
		body.set("emisor_documento", cuitCliente);
		body.set("emisor_cuit", cuitCliente);
		body.set("emisor_nombre_apellidos", nombreCliente);
		body.set("beneficiario_nombre_apellidos", contexto.parametros.string("beneficiario_nombre_apellidos", "").trim());
		body.set("beneficiario_email", contexto.parametros.string("beneficiario_email", ""));
		body.set("devolucion_motivo", contexto.parametros.string("motivo_devolucion"));
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB cancelarRetorno(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeDevolucionAnularPost", "cheques", "POST", "/v1/cheque/devolucion/anular", contexto);
		Objeto body = new Objeto();
		String cuitCliente = contexto.persona().cuit();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("emisor_documento_tipo", "cuit");
		body.set("emisor_documento", cuitCliente);
		body.set("devolucion_motivo", contexto.parametros.string("motivo_cancelacion"));
		body.set("firmantes", firmantes);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB rechazarRetorno(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeRechazarSolicitudPost", "cheques", "POST", "/v1/cheque/devolucion/rechazar", contexto);
		Objeto body = new Objeto();
		String beneficiarioCliente = contexto.persona().cuit();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", beneficiarioCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("beneficiario_documento_tipo", "cuit");
		body.set("beneficiario_documento", beneficiarioCliente);
		body.set("firmantes", firmantes);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB aceptarRetorno(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeDevolucionAceptarPost", "cheques", "POST", "/v1/cheque/devolucion/aceptar", contexto);
		Objeto body = new Objeto();
		String beneficiarioCliente = contexto.persona().cuit();
		List<Objeto> firmantes = new ArrayList<>();
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", beneficiarioCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("beneficiario_documento_tipo", "cuit");
		body.set("beneficiario_documento", beneficiarioCliente);
		body.set("firmantes", firmantes);
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB endosar(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1ChequeActivoEndosarPoslt", "cheques", "POST", "/v1/cheque/activo/endosar?enviarMail=true", contexto);
		Persona cliente = contexto.persona();
		Objeto body = new Objeto();
		List<Objeto> firmantes = new ArrayList<>();
		List<Objeto> chqReferenciasPago = new ArrayList<>();
		String cuitCliente = cliente.cuit();
		String nombreCliente = cliente.nombreCompleto();
		Objeto chqReferenciaPago = new Objeto();
		chqReferenciaPago.set("valor", contexto.parametros.string("id_cheque"));
		chqReferenciaPago.set("referencia", "");
		chqReferenciasPago.add(chqReferenciaPago);
		Objeto firmante = new Objeto();
		firmante.set("documento_tipo", "cuit");
		firmante.set("documento", cuitCliente);
		firmantes.add(firmante);
		body.set("cheque_id", contexto.parametros.string("id_cheque"));
		body.set("beneficiario_documento_tipo", "cuit");
		body.set("beneficiario_documento", cuitCliente); // String
		body.set("emisor_cuit", cuitCliente); // String
		body.set("emisor_nombre_apellidos", nombreCliente); // String
		body.set("nuevo_beneficiario_documento_tipo", "cuit");
		body.set("nuevo_beneficiario_documento", contexto.parametros.string("nuevo_beneficiario_documento"));
		body.set("nuevo_beneficiario_nombre_apellidos", contexto.parametros.string("nuevo_beneficiario_nombre_apellidos", "").trim()); // String
		body.set("nuevo_beneficiario_email", contexto.parametros.string("nuevo_beneficiario_email", ""));
		body.set("nuevo_beneficiario_cuit", contexto.parametros.string("nuevo_beneficiario_documento"));
		body.set("tipo_endoso", "NOM");
		body.set("firmantes", firmantes);
		body.set("chq_referencias_pago", chqReferenciasPago);
		request.body(body);
		return ApiMB.response(request);
	}

	public static String statusMapper(String estado, boolean reversa) {
		String estadoMB = estado;
		if (!reversa) {
			if ("CUSTODIA".equalsIgnoreCase(estado)) {
				estadoMB = "EN CUSTODIA";
			} else if ("EMITIDO-PENDIENTE".equalsIgnoreCase(estado)) {
				estadoMB = "EMITIDO";
			} else if ("DEVOLUCION-PENDIENTE".equalsIgnoreCase(estado)) {
				estadoMB = "DEVOLUCION PENDIENTE";
			} else if ("ACTIVO-PENDIENTE".equalsIgnoreCase(estado)) {
				estadoMB = "ENDOSADO";
			}
			if ("ENTREGADA A CLIENTE".equalsIgnoreCase(estado)) {
				estadoMB = "ACTIVA";
			}
		} else {
			if ("EN CUSTODIA".equalsIgnoreCase(estado)) {
				estadoMB = "CUSTODIA";
			} else if ("EMITIDO".equalsIgnoreCase(estado)) {
				estadoMB = "EMITIDO-PENDIENTE";
			} else if ("DEVOLUCION PENDIENTE".equalsIgnoreCase(estado)) {
				estadoMB = "DEVOLUCION-PENDIENTE";
			} else if (estado.startsWith("ENDOSADO")) {
				estadoMB = "ACTIVO-PENDIENTE";
			}
		}
		return estadoMB;
	}

	private static List<Objeto> obtenerSucursales(ContextoMB contexto, String cuentaSucursal) {
		List<Objeto> ret = new ArrayList<>();
		for (Objeto sucursal : RestCatalogo.sucursalesV2(contexto, cuentaSucursal).objetos()) {
			if (sucursal.string("domicilio").contains("CP")) {
				String domicilio = sucursal.string("domicilio");
				int startIndex = domicilio.indexOf("CP");
				try {
					String codigoPostalStr = domicilio.substring(startIndex).replace(" ", "").substring(2, 6);
					sucursal.set("CodigoPostalSucursal", Integer.parseInt(codigoPostalStr));
				} catch (Exception e) {
					sucursal.set("CodigoPostalSucursal", 0);
				}
			} else {
				sucursal.set("CodigoPostalSucursal", 0);
			}
			ret.add(sucursal);
		}
		return ret;
	}

	private static Optional<Objeto> obtenerSucursalPersona(ContextoMB contexto, String sucursalPersona) {
		List<Objeto> sucursales = RestCatalogo.sucursalesV2(contexto, sucursalPersona).objetos();

		sucursales.forEach(sucursal -> {
			if (!sucursal.string("codigoPostal").isEmpty()) {
				String codigoPostal = sucursal.string("codigoPostal");
				sucursal.set("CodigoPostalSucursal", Integer.parseInt(codigoPostal));
			} else {
				sucursal.set("CodigoPostalSucursal", 0);
			}
		});
		return Optional.of(sucursales.stream().filter((cadena) -> cadena.string("codSucursal", "").equals(sucursalPersona)).findFirst().get());
	}

	private static String obtenerSucursalPorVe(String sucursalCuenta, String sucursalPersona, ContextoMB contexto) {
		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_prendido_cuenta_sucursal")) {
			return sucursalCuenta;
		} else {
			return sucursalPersona;
		}
	}

	private static ApiResponseMB obtenerRespuestaError(String codigo) {
		ApiResponseMB response = new ApiResponseMB();
		response.set("error", codigo);
		response.codigo = 306;
		return response;
	}
}
