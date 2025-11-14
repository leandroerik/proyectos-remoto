package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.RestAcumar;
import ar.com.hipotecario.mobile.servicio.RestArchivo;

public class MBAcumar {

	public static RespuestaMB oferta(ContextoMB contexto) {
		if (ConfigMB.esProduccion()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();

		Objeto plazos = respuesta.set("plazos");
		plazos.add(new Objeto().set("id", 60).set("valor", "60"));

		Objeto montos = respuesta.set("montos");
		montos.add(new Objeto().set("id", 40000).set("valor", "40.000"));
		montos.add(new Objeto().set("id", 50000).set("valor", "50.000"));
		montos.add(new Objeto().set("id", 60000).set("valor", "60.000"));

		Objeto seguros = respuesta.set("seguros");
		seguros.add(new Objeto().set("id", 40).set("valor", "BHN Vida S.A."));
		seguros.add(new Objeto().set("id", 4786211).set("valor", "Caruso Compañia Argentina de Seguros S.A."));

		return respuesta;
	}

	public static RespuestaMB detalle(ContextoMB contexto) {
		if (ConfigMB.esProduccion()) {
			return RespuestaMB.error();
		}

		String monto = contexto.parametros.string("monto");
		String plazo = contexto.parametros.string("plazo");

		if (Objeto.anyEmpty(monto, plazo)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Objeto objeto = new Objeto();
		objeto.set("tipo", "Préstamo Mejor Hogar");
		objeto.set("moneda", "Pesos ajustables por UVA");
		objeto.set("monto", "");
		objeto.set("plazo", plazo);
		objeto.set("seguro", "0,07 %");
		objeto.set("comision", "1,5 %");
		objeto.set("tna", "0,00 %");
		objeto.set("tea", "0,00 %");
		objeto.set("cft", "1,58 %");
		objeto.set("cuotaInicial", "");
		objeto.set("formaPago", "Débito automático");

		if (monto.equals("40000")) {
			objeto.set("monto", 40000);
			objeto.set("montoFormateado", "40.000");
			objeto.set("cuotaInicial", "$ 708");
		}

		if (monto.equals("50000")) {
			objeto.set("monto", 50000);
			objeto.set("montoFormateado", "50.000");
			objeto.set("cuotaInicial", "$ 885");
		}

		if (monto.equals("60000")) {
			objeto.set("monto", 60000);
			objeto.set("montoFormateado", "60.000");
			objeto.set("cuotaInicial", "$ 1.061");
		}

		return RespuestaMB.exito("detalle", objeto);
	}

	public static RespuestaMB subirDocumentacion(ContextoMB contexto) {
		if (ConfigMB.esProduccion()) {
			return RespuestaMB.error();
		}

		String dniFrente = contexto.parametros.string("dniFrente", null);
		String dniDorso = contexto.parametros.string("dniDorso", null);
		String presupuesto = contexto.parametros.string("presupuesto", null);

		if (dniFrente == null && dniDorso == null && presupuesto == null) {
			return RespuestaMB.parametrosIncorrectos();
		}

		if (dniFrente != null) {
			byte[] archivo = contexto.archivos.get("dniFrente");
			ApiResponseMB response = RestArchivo.subirDni(contexto, dniFrente, archivo);
			if (response.hayError()) {
				return RespuestaMB.error();
			}
		}

		if (dniDorso != null) {
			byte[] archivo = contexto.archivos.get("dniDorso");
			ApiResponseMB response = RestArchivo.subirDni(contexto, dniFrente, archivo);
			if (response.hayError()) {
				return RespuestaMB.error();
			}
		}

		if (presupuesto != null) {
			byte[] archivo = contexto.archivos.get("presupuesto");
			ApiResponseMB response = RestArchivo.subirPresupuesto(contexto, presupuesto, archivo);
			if (response.hayError()) {
				return RespuestaMB.error();
			}
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB prepararAltaAcumar(ContextoMB contexto) {
		if (ConfigMB.esProduccion()) {
			return RespuestaMB.error();
		}

		Integer monto = contexto.parametros.integer("monto");
		Integer plazo = contexto.parametros.integer("plazo");
		String seguroVida = contexto.parametros.string("seguroVida");

		if (Objeto.anyEmpty(monto, plazo, seguroVida)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cajaAhorroTitularPesos();
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();
		List<String> nemonicos = new ArrayList<>();

		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit());
		integrante.set("idCobis", Long.valueOf(contexto.idCobis()));
		integrante.set("numeroTarjetaDebito", tarjetaDebito != null ? tarjetaDebito.numero() : null);

		// GENERAR SOLICITUD
		ApiRequestMB requestPostSolicitudes = ApiMB.request("GenerarSolicitudAcumar", "ventas_windows", "POST", "/solicitudes", contexto);
		requestPostSolicitudes.body("TipoOperacion", "03");
		requestPostSolicitudes.body("CanalOriginacion1", ConfigMB.integer("api_venta_canalOriginacion1"));
		requestPostSolicitudes.body("CanalOriginacion2", ConfigMB.integer("api_venta_canalOriginacion2"));
		requestPostSolicitudes.body("CanalOriginacion3", ConfigMB.string("api_venta_canalOriginacion3"));
		requestPostSolicitudes.body("CanalVenta1", ConfigMB.string("api_venta_canalVenta1"));
		requestPostSolicitudes.body("CanalVenta2", ConfigMB.string("api_venta_canalVenta2"));
		requestPostSolicitudes.body("CanalVenta3", ConfigMB.string("api_venta_canalVenta3"));
		requestPostSolicitudes.body("CanalVenta4", ConfigMB.string("api_venta_canalVenta4"));
		requestPostSolicitudes.body("Oficina", "0");
		ApiResponseMB responsePostSolicitudes = ApiMB.response(requestPostSolicitudes, contexto.idCobis());
		if (responsePostSolicitudes.hayError() || !responsePostSolicitudes.objetos("Errores").isEmpty()) {
			return RespuestaMB.estado("ERROR_GENERANDO_SOLICITUD");
		}
		String idSolicitud = responsePostSolicitudes.objetos("Datos").get(0).string("IdSolicitud");

		// GENERAR INTEGRANTES
		ApiRequestMB requestPostIntegrantes = ApiMB.request("VentasWindowsPostIntegrantes", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
		requestPostIntegrantes.headers.put("X-Handle", idSolicitud);
		requestPostIntegrantes.path("SolicitudId", idSolicitud);
		requestPostIntegrantes.body("NumeroTributario", Long.valueOf(contexto.persona().cuit()));
		requestPostIntegrantes.body("TipoOperacion", "03");
		ApiResponseMB responsePostIntegrantes = ApiMB.response(requestPostIntegrantes, contexto.idCobis());
		if (responsePostIntegrantes.hayError() || !responsePostIntegrantes.objetos("Errores").isEmpty()) {
			return RespuestaMB.estado("ERROR_GENERANDO_INTEGRANTES");
		}

		// GENERAR CAJA AHORRO
		if (cuenta == null) {
			integrante.set("rol", "T");
			ApiRequestMB requestPostCajaAhorro = ApiMB.request("VentasWindowsPostCajaAhorro", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/cajaAhorro", contexto);
			requestPostCajaAhorro.headers.put("X-Handle", idSolicitud);
			requestPostCajaAhorro.path("SolicitudId", idSolicitud);
			requestPostCajaAhorro.body("Categoria", "MOV");
			requestPostCajaAhorro.body("ProductoBancario", "3");
			requestPostCajaAhorro.body("DomicilioResumen").set("tipo", "DP");
			requestPostCajaAhorro.body("Oficial", "0");
			requestPostCajaAhorro.body("CobroPrimerMantenimiento", true);
			requestPostCajaAhorro.body("Moneda", "80");
			requestPostCajaAhorro.body("Origen", "31");
			requestPostCajaAhorro.body("UsoFirma", "U");
			requestPostCajaAhorro.body("Ciclo", "5");
			requestPostCajaAhorro.body("ResumenMagnetico", false);
			requestPostCajaAhorro.body("TransfiereAcredHab", false);
			requestPostCajaAhorro.add("Integrantes", integrante);
			requestPostCajaAhorro.body("CuentaLegales").set("Uso", "PER").set("RealizaTransferencias", false);
			requestPostCajaAhorro.body("IdSolicitud", idSolicitud);
			requestPostCajaAhorro.body("TipoOperacion", "03");
			ApiResponseMB responsePostCajaAhorro = ApiMB.response(requestPostCajaAhorro, contexto.idCobis());
			if (responsePostCajaAhorro.hayError() || !responsePostCajaAhorro.objetos("Errores").isEmpty()) {
				return RespuestaMB.estado("ERROR_GENERANDO_CAJA_AHORRO");
			}
			nemonicos.add("CASOLIC");
		}

		// GENERAR TARJETA DEBITO
		if (tarjetaDebito == null) {
			integrante.set("rol", "T");
			Objeto tarjetaDebitoCuentasOperativas = new Objeto();
			tarjetaDebitoCuentasOperativas.set("producto", "4");
			tarjetaDebitoCuentasOperativas.set("cuenta", cuenta != null ? cuenta.numero() : "0");
			tarjetaDebitoCuentasOperativas.set("moneda", "80");
			tarjetaDebitoCuentasOperativas.set("principal", true);

			ApiRequestMB requestPostTarjetaDebito = ApiMB.request("VentasWindowsPostTarjetaDebito", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/tarjetaDebito", contexto);
			requestPostTarjetaDebito.headers.put("X-Handle", idSolicitud);
			requestPostTarjetaDebito.path("SolicitudId", idSolicitud);
			requestPostTarjetaDebito.body("Tipo", "NV"); // Visa Electron
			requestPostTarjetaDebito.body("Domicilio").set("Tipo", "DP");
			requestPostTarjetaDebito.body("Grupo", "3");
			requestPostTarjetaDebito.body("TipoCuentaComision", "4");
			requestPostTarjetaDebito.body("NumeroCtaComision", "0"); // ¿Mandar el numero de cuenta o siempre 0?
			requestPostTarjetaDebito.add("TarjetaDebitoCuentasOperativas", tarjetaDebitoCuentasOperativas);
			requestPostTarjetaDebito.add("Integrantes", integrante);
			requestPostTarjetaDebito.body("IdSolicitud", idSolicitud);
			requestPostTarjetaDebito.body("TipoOperacion", "03");
			ApiResponseMB responsePostTarjetaDebito = ApiMB.response(requestPostTarjetaDebito, contexto.idCobis());
			if (responsePostTarjetaDebito.hayError() || !responsePostTarjetaDebito.objetos("Errores").isEmpty()) {
				return RespuestaMB.estado("ERROR_GENERANDO_TARJETA_DEBITO");
			}
		}

		// GENERAR PRESTAMO
		if (true) {
			integrante.set("rol", "D");
			ApiRequestMB requestPostAcumar = ApiMB.request("VentasWindowsPostAcumar", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/prestamoPersonal", contexto);
			requestPostAcumar.headers.put("X-Handle", idSolicitud);
			requestPostAcumar.path("SolicitudId", idSolicitud);

			requestPostAcumar.add("Integrantes", integrante);

			Objeto formaDesembolso = new Objeto();
			formaDesembolso.set("Forma", "NCMNCA");
			formaDesembolso.set("Referencia", cuenta != null ? cuenta.numero() : "0");
			formaDesembolso.set("Beneficiario", contexto.persona().nombreCompleto());
			formaDesembolso.set("NroDesembolso", "1");
			formaDesembolso.set("Valor", monto);
			requestPostAcumar.body("Desembolsos").set("FormasDesembolso").add(formaDesembolso);

			requestPostAcumar.body("Amortizacion", "01");
			requestPostAcumar.body("TipoTasa", "01");
			requestPostAcumar.body("DestinoBien", ConfigMB.esDesarrollo() ? "134" : "10");
			requestPostAcumar.body("FormaCobroCuenta", cuenta != null ? cuenta.numero() : "0");
			requestPostAcumar.body("FormaCobroTipo", "NDMNCA");
			requestPostAcumar.body("EmpresaAseguradora", seguroVida);
			requestPostAcumar.body("MailAvisos", null);
			requestPostAcumar.body("TipoOperacion", "03");

			Objeto datosMicrocreditos = requestPostAcumar.body("DatosMicrocreditos");
			datosMicrocreditos.set("MicroCreditoRolPersona", "BENEFIAGUA");
			datosMicrocreditos.set("ApellidoPrestador", contexto.persona().nombreCompleto());
			datosMicrocreditos.set("CodClienteCobisBeneficiario", contexto.idCobis());
			datosMicrocreditos.set("IdCobisPrestador", null);
			datosMicrocreditos.set("IdCobisEmpresa", "300");
			datosMicrocreditos.set("DesEmpresa", "Acumar");
			datosMicrocreditos.set("MontoAdicional", null);
			datosMicrocreditos.set("MontoObra", null);
			datosMicrocreditos.set("NombrePrestador", null);
			datosMicrocreditos.set("NroCuentaEmpresa", null);
			datosMicrocreditos.set("NroCuentaPrestador", cuenta != null ? cuenta.numero() : "0");
			datosMicrocreditos.set("MicroCreditoDemandaId", null);
			datosMicrocreditos.set("TipoCuentaEmpresa", null);
			datosMicrocreditos.set("TipoCuentaPrestador", null);
			datosMicrocreditos.set("NumeroTribPrestador", null);
			datosMicrocreditos.set("NumeroTribEmpresa", null);
			datosMicrocreditos.set("TipologiaAjuste", null);

			requestPostAcumar.body("PlazoSolicitado", plazo);
			requestPostAcumar.body("Moneda", "80");
			requestPostAcumar.body("Mercado", "01");
			requestPostAcumar.body("MontoSolicitado", monto);
			requestPostAcumar.body("Oficial", 0);
			requestPostAcumar.body("Destino", "043");
			requestPostAcumar.body("FormaCobro", "NCMNCA");
			requestPostAcumar.body("Domicilio").set("tipo", "DP");
			requestPostAcumar.body("DestinoVivienda", "0");
			requestPostAcumar.body("Plazo", 48);
			requestPostAcumar.body("Tasa", 0);
			requestPostAcumar.body("FechaCobroFija", false);
			requestPostAcumar.body("IdSolicitud", idSolicitud);
			requestPostAcumar.body("Cuenta", cuenta != null ? cuenta.numero() : "0");
			requestPostAcumar.body("CFT", 1.58);
			requestPostAcumar.body("SubProducto", ConfigMB.esDesarrollo() ? 30 : 17);
			requestPostAcumar.body("ImporteCuota", montoPrimerCuota(monto));

			ApiResponseMB responsePostAcumar = ApiMB.response(requestPostAcumar, contexto.idCobis());
			if (responsePostAcumar.hayError() || !responsePostAcumar.objetos("Errores").isEmpty()) {
				return RespuestaMB.estado("ERROR_GENERANDO_PRESTAMO");
			}
			nemonicos.add("GRUPO3");
		}

		// CERRAR OPERACION
		ApiRequestMB requestPutResoluciones = ApiMB.request("VentasWindowsPutResoluciones", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		requestPutResoluciones.headers.put("X-Handle", idSolicitud);
		requestPutResoluciones.path("SolicitudId", idSolicitud);
		requestPutResoluciones.query("estado", "finalizar");
		requestPutResoluciones.body("TipoOperacion", "03");
		requestPutResoluciones.body("IdSolicitud", idSolicitud);
		ApiResponseMB responsePutResoluciones = ApiMB.response(requestPutResoluciones, contexto.idCobis());
		if (responsePutResoluciones.hayError() || !responsePutResoluciones.objetos("Errores").isEmpty()) {
			return RespuestaMB.estado("ERROR_CERRANDO_SOLICITUD");
		}

		return RespuestaMB.exito("solicitud", new Objeto().set("id", idSolicitud).set("nemonicos", nemonicos));
	}

	public static byte[] terminosCondiciones(ContextoMB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String nemonico = contexto.parametros.string("nemonico");

		if (idSolicitud.isEmpty() || nemonico.isEmpty()) {
			throw new RuntimeException();
		}

		ApiRequestMB request = ApiMB.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", idSolicitud);
		request.query("grupocodigo", nemonico);
		request.query("canal", "MB");

		if (nemonico.equalsIgnoreCase("PPADELANTO")) {
			request.header("x-cuil", contexto.persona().cuit());
			try {
				request.header("x-apellidoNombre", URLEncoder.encode(contexto.persona().apellidos() + " " + contexto.persona().nombres(), "UTF-8"));
			} catch (Exception e) {
				request.header("x-apellidoNombre", contexto.persona().apellidos() + " " + contexto.persona().nombres());
			}
			request.header("x-dni", contexto.persona().numeroDocumento());
			request.header("x-producto", "AdelantoBH");
		}

		ApiResponseMB response = ApiMB.response(request, idSolicitud, nemonico, contexto.idCobis());
		if (response.hayError()) {
			throw new RuntimeException();
		}

		String base64 = response.string("Data");
		byte[] archivo = Base64.getDecoder().decode(base64);
		try {
			archivo = Base64.getDecoder().decode(new String(archivo));
		} catch (Exception e) {
		}
		contexto.setHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + idSolicitud + "-" + nemonico + ".pdf");
		return archivo;
	}

	public static RespuestaMB terminosCondicionesString(ContextoMB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String nemonico = contexto.parametros.string("nemonico");

		if (idSolicitud.isEmpty() || nemonico.isEmpty()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "terminosCondiciones", null, "PARAMETROS_INCORRECTOS");
			return RespuestaMB.parametrosIncorrectos();
		}

		ApiRequestMB request = ApiMB.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", idSolicitud);
		request.query("grupocodigo", nemonico);
		request.query("canal", "MB");
		ApiResponseMB response = ApiMB.response(request, idSolicitud, nemonico, contexto.idCobis());
		if (response.hayError()) {
			throw new RuntimeException();
		}

		String base64 = response.string("Data");
		contexto.setHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + idSolicitud + "-" + nemonico + ".pdf");
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("Data", base64);

		return respuesta;
	}

	public static RespuestaMB finalizarAltaAcumar(ContextoMB contexto) {
		if (ConfigMB.esProduccion()) {
			return RespuestaMB.error();
		}

		String idSolicitud = contexto.parametros.string("idSolicitud");

		if (Objeto.anyEmpty(idSolicitud)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		// ALTA ACUMAR
		ApiRequestMB requestGetFinalizar = ApiMB.request("AltaAcumar", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		requestGetFinalizar.headers.put("X-Handle", idSolicitud);
		requestGetFinalizar.path("SolicitudId", idSolicitud);
		requestGetFinalizar.query("estado", "finalizar");
		ApiResponseMB responseGetFinalizar = ApiMB.response(requestGetFinalizar, contexto.idCobis());
		if (responseGetFinalizar.hayError() || !responseGetFinalizar.objetos("Errores").isEmpty()) {
			return RespuestaMB.estado("ERROR_FINALIZACION");
		}

		// CAMBIO ESTADO EN ESALES
		RestAcumar.actualizarEstado(contexto.idCobis(), "TC"); // Solicitud en trámite (Aceptó términos y condiciones)

		return RespuestaMB.exito();
	}

	public static RespuestaMB altaAcumar(ContextoMB contexto) {
		RespuestaMB respuesta = prepararAltaAcumar(contexto);
		String idSolicitud = respuesta.string("solicitud.id");
		if (!idSolicitud.isEmpty()) {
			contexto.parametros.set("idSolicitud", idSolicitud);
			respuesta = finalizarAltaAcumar(contexto);
		}
		return respuesta;
	}

	/* ========== UTIL ========== */
	private static BigDecimal montoPrimerCuota(Integer monto) {
		BigDecimal cft = null;
		cft = monto.equals(40000) ? new BigDecimal("708") : cft;
		cft = monto.equals(50000) ? new BigDecimal("885") : cft;
		cft = monto.equals(60000) ? new BigDecimal("1061") : cft;
		return cft;
	}
}
