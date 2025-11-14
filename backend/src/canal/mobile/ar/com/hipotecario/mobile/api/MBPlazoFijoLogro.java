package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.PlazoFijoLogro;
import ar.com.hipotecario.mobile.servicio.ProductosService;

public class MBPlazoFijoLogro {

	public static RespuestaMB consolidadaPlazosFijoLogros(ContextoMB contexto) {

		// TODO: Se retira por Normativo “A” 7849
		/*
		 * if (contexto.persona().esMenor()) { return
		 * Respuesta.estado("MENOR_NO_AUTORIZADO"); }
		 */

		RespuestaMB respuesta = new RespuestaMB();
		Objeto listaPlazoFijosLogros = new Objeto();
		List<PlazoFijoLogro> plazosFijosLogros = contexto.plazosFijosLogros();
		BigDecimal invertidoPesos = BigDecimal.ZERO;
		BigDecimal invertidoDolares = BigDecimal.ZERO;

		for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogros) {

			if (LocalDate.now().isBefore(LocalDate.parse(plazoFijoLogro.fechaVencimiento("yyyy-MM-dd")))) {
				Cuenta cuenta = contexto.cuenta(plazoFijoLogro.numeroCuenta());
				Objeto item = new Objeto();
				String simboloMoneda = Formateador.simboloMonedaActual(plazoFijoLogro.idMoneda());

				item.set("id", plazoFijoLogro.id());
				item.set("nombre", plazoFijoLogro.nombre());
				item.set("simboloMoneda", simboloMoneda);
				item.set("descripcionMoneda", Formateador.moneda(plazoFijoLogro.idMoneda()));
				item.set("cantidadCuotas", plazoFijoLogro.cantidadPlazosFijos());
				item.set("diaDebitos", String.format("%02d", plazoFijoLogro.diaDebito()));
				item.set("fecha", plazoFijoLogro.fechaAlta("dd/MM/yyyy"));
				item.set("constituidos", plazoFijoLogro.constituidos());
				item.set("acumulado", plazoFijoLogro.montoAcumulado());
				item.set("acumuladoFormateado", Formateador.importe(plazoFijoLogro.montoAcumulado()));
				item.set("porcentajeTiempo", plazoFijoLogro.porcentajeDiasTranscurridos());
				item.set("diasFaltantesAlVenc", plazoFijoLogro.diasRestantes());
				item.set("vencimiento", plazoFijoLogro.fechaVencimiento("dd/MM/yyyy"));
				String estado = plazoFijoLogro.idEstado();
				item.set("idEstado", estado);
				item.set("estado", plazoFijoLogro.descripcionEstado());
				setearInfoCuotaAProcesar(item, plazoFijoLogro, estado);

				if ("$".equals(simboloMoneda)) {
					invertidoPesos = invertidoPesos.add(plazoFijoLogro.montoAcumulado());
				} else {
					invertidoDolares = invertidoPesos.add(plazoFijoLogro.montoAcumulado());
				}

				item.set("esUVA", plazoFijoLogro.esUva());
				if (plazoFijoLogro.esUva()) {
					item.set("montoUVA", plazoFijoLogro.montoUva());
					item.set("montoUVAFormateado", Formateador.importe(plazoFijoLogro.montoUva()));
				}

				if (cuenta != null) {
					item.set("cuentaDescripcionCorta", cuenta.descripcionCorta());
					item.set("cuentaUltimos4digitos", cuenta.ultimos4digitos());
					// item.set("cuentaSaldo", cuenta.saldo());
					// item.set("cuentaSaldoFormateado", Formateador.importe(cuenta.saldo()));
				}

				for (Integer id = 1; id <= plazoFijoLogro.cantidadPlazosFijos(); ++id) {
					Objeto subitem = new Objeto();
					subitem.set("id", id);
					subitem.set("descripcionCuota", plazoFijoLogro.itemCuota(id));
					subitem.set("fechaPagoCuota", plazoFijoLogro.itemFechaPagoCuota(id, "dd/MM/yyyy"));
					subitem.set("fechaConstitucionCuota", plazoFijoLogro.itemFechaVencimientoCuota(id, "dd/MM/yyyy"));
					subitem.set("fechaVencimientoCuota", plazoFijoLogro.itemFechaVencimiento(id, "dd/MM/yyyy"));
					subitem.set("montoCuota", plazoFijoLogro.itemMontoInicial(id));
					subitem.set("montoCuotaFormateado", Formateador.importe(plazoFijoLogro.itemMontoInicial(id)));
					subitem.set("tna", plazoFijoLogro.itemTasa(id));
					subitem.set("tnaFormateada", Formateador.importe(plazoFijoLogro.itemTasa(id)));
					subitem.set("interesesCuota", plazoFijoLogro.itemInteres(id));
					subitem.set("interesesCuotaFormateado", Formateador.importe(plazoFijoLogro.itemInteres(id)));
					subitem.set("idEstado", plazoFijoLogro.itemIdEstado(id));
					subitem.set("estado", plazoFijoLogro.itemDescripcionEstado(id));
					subitem.set("numero", plazoFijoLogro.itemNumero(id));
					subitem.set("renovacionAutomatica", "N");
					subitem.set("garantiaDeposito", plazoFijoLogro.itemGarantiaDeposito(id));
					subitem.set("plazo", plazoFijoLogro.itemCantidadDias(id));
					subitem.set("montoVencimiento", plazoFijoLogro.itemMontoVencimiento(id));
					subitem.set("montoVencimientoFormateado", Formateador.importe(plazoFijoLogro.itemMontoVencimiento(id)));
					subitem.set("forzable", plazoFijoLogro.forzable(id));
					item.add("lista", subitem);
				}
				listaPlazoFijosLogros.add(item);
			}

		}
		if (!listaPlazoFijosLogros.esLista()) {
			return respuesta;
		}

		Objeto configuracion = new Objeto();
		configuracion.set("montoMinimoTradicional", new BigDecimal("1000.00"));
		configuracion.set("montoMinimoTradicionalDolares", new BigDecimal("100.00"));
		configuracion.set("montoMinimoUVA", new BigDecimal("1000.00"));
		configuracion.set("montoMaximoUVA", new BigDecimal("5000000.00"));
		configuracion.set("diasMinimoTradicional", 30);
		configuracion.set("diasMinimoUVA", 90);
		configuracion.set("diasMaximoUVA", 729);
		configuracion.set("montoPorDefecto", "1.000,00");
		configuracion.set("fechaEstandarTradicional", 36);

		respuesta.set("configuracion",configuracion);
		respuesta.set("invertidoPesos", Formateador.importe(invertidoPesos));
		respuesta.set("invertidoDolares", Formateador.importe(invertidoDolares));
		respuesta.set("plazosFijosLogros", Fecha.ordenarPorFechaAsc(listaPlazoFijosLogros, "vencimiento", "dd/MM/yyyy"));
		return respuesta;
	}

	private static void setearInfoCuotaAProcesar(Objeto item, PlazoFijoLogro plazoFijoLogro, String estado) {

		boolean faltanCuotas = item.integer("constituidos") < plazoFijoLogro.cantidadPlazosFijos();
		int cuotaAprocesar = faltanCuotas ? item.integer("constituidos") + 1 : item.integer("constituidos");
		item.set("descripcionCuota", plazoFijoLogro.estadoDescripcionPF(estado, cuotaAprocesar));

		if (faltanCuotas && "A".equals(estado)) {
			item.set("fechaConstitucionCuota", plazoFijoLogro.itemFechaVencimientoCuota(cuotaAprocesar, "dd/MM/yyyy", estado));
			item.set("montoCuota", Formateador.importe(plazoFijoLogro.itemMontoInicial(cuotaAprocesar)));
		} else {
			item.set("fechaConstitucionCuota", "");
			item.set("montoCuota", Formateador.importe(plazoFijoLogro.montoAcumuladoConstituidos()));
		}

		item.set("mensajeEstado", plazoFijoLogro.itemMensajeEstado(cuotaAprocesar));
		item.set("ultimaCuota", "C".equals(estado) || "B".equals(estado) || !faltanCuotas);
	}

	public static RespuestaMB terminosCondicionesPlazosFijoLogros(ContextoMB contexto) {
		Date fecha = contexto.parametros.date("fecha", "d/M/yyyy", new Objeto().set("fecha", new Date()).date("fecha"));

		SqlRequestMB sqlRequest = SqlMB.request("SelectTyCPlazosFijoLogro", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[pf_meta_tyc] WHERE ? BETWEEN vigencia_desde AND vigencia_hasta";
		sqlRequest.parametros.add(fecha);

		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		for (Objeto item : sqlResponse.registros) {
			return RespuestaMB.exito("texto", item.string("data"));
		}

		return RespuestaMB.error();
	}

	public static RespuestaMB monedaValidasPlazosFijos(ContextoMB contexto) {
		Objeto monedas = new Objeto();
		monedas.add(new Objeto().set("id", "80").set("descripcion", "Pesos"));
		monedas.add(new Objeto().set("id", "2").set("descripcion", "Dolares"));
		monedas.add(new Objeto().set("id", "88").set("descripcion", "UVAs"));
		return RespuestaMB.exito("monedas", monedas);
	}

	public static RespuestaMB parametriaPlazoFijoLogros(ContextoMB contexto) {
		String idMoneda = contexto.parametros.string("idMoneda");

		if (Objeto.anyEmpty(idMoneda)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Boolean habilitarCatalogoApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_catalogos_api");

		ApiRequestMB request = null;

		if (habilitarCatalogoApi) {
			request = ApiMB.request("PlazosFijosGetParametria", "catalogo", "GET", "/v1/plazoFijos/parametrias", contexto);
			request.query("operacion", "Q");
			request.query("opcion", "1");
			request.query("codCliente", contexto.idCobis());
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PlazosFijosWindowsGetParametria", "catalogos_windows", "GET", "/v1/plazoFijos/parametrias", contexto);
			request.query("operacion", "Q");
			request.query("opcion", "1");
			request.query("codCliente", contexto.idCobis());
		}

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		for (Objeto item : response.objetos()) {
			Boolean aceptar = false;
			aceptar |= idMoneda.equals("80") && item.string("moneda").equals("80") && !"S".equals(item.string("ajuste")); // PESOS
			aceptar |= idMoneda.equals("2") && item.string("moneda").equals("2"); // DOLARES
			aceptar |= idMoneda.equals("88") && "S".equals(item.string("ajuste")); // UVAS
			if (aceptar) {
				for (String plazo : item.string("plazo").split(";")) {
					if (!plazo.trim().isEmpty()) {
						Objeto objeto = new Objeto();
						objeto.set("id", plazo + "_" + item.string("secuencial") + "_" + item.string("moneda") + "_" + item.string("tipoPlazoFijo"));
						objeto.set("descripcion", String.format("%02d", Integer.valueOf(plazo)));
						respuesta.add("plazos", objeto);
					}
				}
				return respuesta;
			}
		}

		return RespuestaMB.error();
	}

	public static RespuestaMB modificarPlazoFijoLogros(ContextoMB contexto) {
		String id = contexto.parametros.string("id");
		String nombre = contexto.parametros.string("nombre");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");

		if (Objeto.anyEmpty(id)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		PlazoFijoLogro plazoFijoLogro = contexto.plazoFijoLogro(id);
		if (plazoFijoLogro == null) {
			return RespuestaMB.estado("PLAZO_FIJO_LOGRO_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("CUENTA_NO_EXISTE");
		}

		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosGetModificacionLogro", "plazosfijos", "GET", "/v1/{idCobis}/modificacionPlazoFijoAhorro", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PlazosFijosWindowsGetModificacionLogro", "plazosfijos_windows", "GET", "/v1/{idCobis}/modificacionPlazoFijoAhorro", contexto);
		}
		request.path("idCobis", contexto.idCobis());
		request.query("planContratado", id);
		request.query("moneda", plazoFijoLogro.idMoneda());
		request.query("tipoCuenta", cuenta.idTipo());
		request.query("cuenta", cuenta.numero());
		request.query("monto", monto.toString());
		request.query("nombre", nombre);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			if ("143136".equals(response.string("codigo"))) {
				return RespuestaMB.exito();
			}
			return RespuestaMB.error();
		}

		plazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroDetalle();
		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		return RespuestaMB.exito();
	}

	public static RespuestaMB simularPlazoFijoLogro(ContextoMB contexto) {
		String idParametria = contexto.parametros.string("idParametria");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String nombre = contexto.parametros.string("nombre");
		Integer dia = contexto.parametros.integer("dia");

		if (Objeto.anyEmpty(idParametria, idCuenta, monto, nombre, dia)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		// TODO: Se retira por Normativo “A” 7849
		String moneda = idParametria.split("_")[2];
		if (!moneda.equals("80") && contexto.persona().esMenor()) {
			return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);

		Boolean habilitarSimuladorApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_simulador_api");
		ApiRequestMB request = null;
		if (habilitarSimuladorApi) {
			request = ApiMB.request("SimuladoresGetLogro", "simuladores", "GET", "/v1/plazoFijos/meta", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("SimuladoresWindowsGetLogro", "simuladores_windows", "GET", "/v1/plazoFijos/meta", contexto);
		}
		request.query("operacion", "S");
		request.query("opcion", "S");
		request.query("codCliente", contexto.idCobis());
		request.query("planContratado", idParametria.split("_")[1]);
		request.query("moneda", idParametria.split("_")[2]);
		request.query("cuenta", cuenta.numero());
		request.query("tipoCuenta", cuenta.idTipo());
		request.query("monto", monto.toString());
		request.query("nombre", nombre.replace(" ", ""));
		request.query("dia", dia.toString());
		request.query("cuota", idParametria.split("_")[0]);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		for (Objeto item : response.objetos()) {
			Objeto objeto = new Objeto();
			objeto.set("cuota", item.string("secuencial"));
			objeto.set("diaDebito", item.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
			if ("1".equals(item.string("secuencial"))) {

				ApiRequestMB requestPrimero = null;
				if (habilitarSimuladorApi) {
					requestPrimero = ApiMB.request("SimuladoresGetPlazoFijos", "simuladores", "GET", "/v1/plazoFijos", contexto);
					request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
				} else {
					requestPrimero = ApiMB.request("SimuladoresWindowsGetPlazoFijos", "simuladores_windows", "GET", "/v1/plazoFijos", contexto);
					requestPrimero.query("canal", "26");
				}
				requestPrimero.query("idcliente", contexto.idCobis());
				requestPrimero.query("tipoOperacion", idParametria.split("_")[3]);
				requestPrimero.query("plazo", item.string("diasPlazo"));
				requestPrimero.query("monto", item.string("monto"));
				requestPrimero.query("moneda", item.string("moneda"));
				if (cuenta != null) {
					requestPrimero.query("tipoCuenta", cuenta.idTipo());
					requestPrimero.query("cuenta", cuenta.numero());
				}

				ApiResponseMB responsePrimero = ApiMB.response(requestPrimero, contexto.idCobis());
				if (responsePrimero.hayError()) {
					return RespuestaMB.error();
				}

				Objeto primero = new Objeto();
				primero.set("cuota", objeto.string("cuota"));
				primero.set("diaDebito", objeto.string("diaDebito"));
				primero.set("fechaVencimiento", responsePrimero.objetos().get(0).date("fechaVencimiento", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
				primero.set("intereses", responsePrimero.objetos().get(0).string("interesEstimado"));
				primero.set("interesesFormateado", Formateador.importe(responsePrimero.objetos().get(0).bigDecimal("interesEstimado")));
				primero.set("tna", responsePrimero.objetos().get(0).bigDecimal("tasa"));
				primero.set("tnaFormateada", Formateador.importe(responsePrimero.objetos().get(0).bigDecimal("tasa")));
				primero.set("montoVencimiento", responsePrimero.objetos().get(0).bigDecimal("montoTotal"));
				primero.set("montoVencimientoFormateado", Formateador.importe(responsePrimero.objetos().get(0).bigDecimal("montoTotal")));
				primero.set("moneda", Formateador.simboloMoneda(item.string("moneda")));
				respuesta.set("primero", primero);
			}
			respuesta.add("cuotas", objeto);
		}

		return respuesta;
	}

	public static RespuestaMB altaPlazoFijoLogro(ContextoMB contexto) {
		String idParametria = contexto.parametros.string("idParametria");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String nombre = contexto.parametros.string("nombre");
		Integer dia = contexto.parametros.integer("dia");

		if (Objeto.anyEmpty(idParametria, idCuenta, monto, nombre, dia)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		// TODO: Se retira por Normativo “A” 7849
		String moneda = idParametria.split("_")[2];
		if (!moneda.equals("80") && contexto.persona().esMenor()) {
			return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("NO_EXISTE_CUENTA");
		}
		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosLogrosPost", "plazosfijos", "POST", "/v1/plazoFijosMETA", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PlazosFijosWindowsLogrosPost", "plazosfijos_windows", "POST", "/v1/plazoFijosMETA", contexto);
		}
		request.body("codCliente", contexto.idCobis());
		request.body("cuenta", cuenta.numero());
		request.body("cuota", idParametria.split("_")[0]);
		request.body("dia", dia.toString());
		request.body("moneda", idParametria.split("_")[2]);
		request.body("monto", monto);
		request.body("nombre", nombre);
		request.body("opcion", "A");
		request.body("operacion", "A");
		request.body("planContratado", idParametria.split("_")[1]);
		request.body("tipoCuenta", cuenta.idTipo());

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		String secuencial = response.objetos().get(0).string("secuencial");
		String diasPlazo = response.objetos().get(0).string("diasPlazo");

		ApiRequestMB requestPrimero = null;
		if (habilitarPlazosFijosApi) {
			requestPrimero = ApiMB.request("PlazosFijosPost", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
		} else {
			requestPrimero = ApiMB.request("PlazosFijosWindowsPost", "plazosfijos_windows", "POST", "/v1/plazoFijos", contexto);
		}
		requestPrimero.body("canal", 26);
		requestPrimero.body("capInteres", "N");
		requestPrimero.body("cuenta", cuenta.numero());
		requestPrimero.body("idPlanAhorro", secuencial);
		requestPrimero.body("idcliente", contexto.idCobis());
		requestPrimero.body("moneda", Integer.parseInt(idParametria.split("_")[2]));
		requestPrimero.body("monto", monto);
		requestPrimero.body("nroOperacion", 1);
		requestPrimero.body("periodo", 0);
		requestPrimero.body("plazo", diasPlazo);
		requestPrimero.body("renova", "N");
		requestPrimero.body("reverso", null);
		requestPrimero.body("tipoCuenta", cuenta.idTipo());
		requestPrimero.body("tipoOperacion", idParametria.split("_")[3]);
		requestPrimero.body("usuarioAlta", null);

		ApiResponseMB responsePrimero = ApiMB.response(requestPrimero, contexto.idCobis());
		if (responsePrimero.hayError()) {
			if ("258402".equals(responsePrimero.string("codigo"))) {
				return RespuestaMB.estado("SIN_PERFIL_PATRIMONIAL");
			}
			if ("141144".equals(responsePrimero.string("codigo"))) {
				return RespuestaMB.estado("SALDO_INSUFICIENTE");
			}
			return RespuestaMB.error();
		}

		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		ProductosService.eliminarCacheProductos(contexto);
		return RespuestaMB.exito();
	}

	public static RespuestaMB bajaPlazoFijoLogro(ContextoMB contexto) {
		String id = contexto.parametros.string("id");

		// TODO: Se retira por Normativo “A” 7849
		if (contexto.persona().esMenor()) {
			return RespuestaMB.estado("MENOR_NO_AUTORIZADO");
		}

		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosGetBajaLogro", "plazosfijos", "GET", "/v1/{idCobis}/bajaPlazoFijoAhorro", contexto);
			request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
		} else {
			request = ApiMB.request("PlazosFijosWindowsGetBajaLogro", "plazosfijos_windows", "GET", "/v1/{idCobis}/bajaPlazoFijoAhorro", contexto);
		}
		request.path("idCobis", contexto.idCobis());
		request.query("planContratado", id);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			if ("143136".equals(response.string("codigo"))) {
				return RespuestaMB.exito();
			}
			return RespuestaMB.error();
		}

		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		return RespuestaMB.exito();
	}

	public static RespuestaMB forzarPlazoFijoLogro(ContextoMB contexto) {
		String id = contexto.parametros.string("id");
		String cuota = contexto.parametros.string("cuota");

		// TODO: Se retira por Normativo “A” 7849
		/*
		 * if (contexto.persona().esMenor()) { return
		 * RespuestaMB.estado("MENOR_NO_AUTORIZADO"); }
		 */

		Boolean habilitarPlazosFijosApi = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_plazo_fijo_api");
		ApiRequestMB request = null;
		if (habilitarPlazosFijosApi) {
			request = ApiMB.request("PlazosFijosGetForzadoLogro", "plazosfijos", "GET", "/v1/{idCobis}/forzadoPlazoFijo", contexto);
		} else {
			request = ApiMB.request("PlazosFijosWindowsGetForzadoLogro", "plazosfijos_windows", "GET", "/v1/{idCobis}/forzadoPlazoFijo", contexto);
		}
		request.path("idCobis", contexto.idCobis());
		request.query("planContratado", id);
		request.query("secuencialPlazoFijo", cuota);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		contexto.plazoFijoLogro(id).eliminarCachePlazosFijosWindowsGetLogroDetalle();
		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		return RespuestaMB.exito();
	}
}
