package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.PlazoFijoLogro;
import ar.com.hipotecario.canal.homebanking.servicio.PlazoFijoLogrosService;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;

public class HBPlazoFijoLogro {

	public static Respuesta consolidadaPlazosFijoLogros(ContextoHB contexto) {

		// TODO: Se retira por Normativo “A” 7849
		/*
		 * if (contexto.persona().esMenor()) { return
		 * Respuesta.estado("MENOR_NO_AUTORIZADO"); }
		 */
		
		Futuro<Map<PlazoFijoLogro, List<ApiResponse>>> detalle = new Futuro<>(() -> PlazoFijoLogro.plazosFijosLogroDetalle(contexto));
		//Map<PlazoFijoLogro, List<ApiResponse>> detalle = PlazoFijoLogro.plazosFijosLogroDetalle(contexto);

		Respuesta respuesta = new Respuesta();
		Futuro<List<PlazoFijoLogro>> plazosFijosLogros = new Futuro<>(() -> contexto.plazosFijosLogros());
		//List<PlazoFijoLogro> plazosFijosLogros = contexto.plazosFijosLogros();

		for (PlazoFijoLogro plazoFijoLogro : plazosFijosLogros.get()) {
			Cuenta cuenta = contexto.cuenta(plazoFijoLogro.numeroCuenta());

			Objeto item = new Objeto();
			item.set("id", plazoFijoLogro.id());
			item.set("nombre", plazoFijoLogro.nombre());
			item.set("simboloMoneda", Formateador.simboloMoneda(plazoFijoLogro.idMoneda()));
			item.set("descripcionMoneda", Formateador.moneda(plazoFijoLogro.idMoneda()));
			item.set("cantidadCuotas", plazoFijoLogro.cantidadPlazosFijos());
			item.set("diaDebitos", String.format("%02d", plazoFijoLogro.diaDebito()));
			item.set("fecha", plazoFijoLogro.fechaAlta("dd/MM/yyyy"));
			item.set("fechaPrimerPago", plazoFijoLogro.fechaAlta("yyyy-MM-dd"));
			item.set("constituidos", plazoFijoLogro.constituidos(detalle.get()));
			item.set("vencidas", plazoFijoLogro.vencidos(detalle.get()));
			item.set("pendientes", plazoFijoLogro.pendientes(detalle.get()));
			item.set("acumulado", plazoFijoLogro.montoAcumulado(detalle.get()));
			item.set("acumuladoFormateado", Formateador.importe(plazoFijoLogro.montoAcumulado(detalle.get())));
			item.set("porcentajeTiempo", plazoFijoLogro.porcentajeDiasTranscurridos());
			item.set("diasFaltantes", plazoFijoLogro.diasRestantes());
			item.set("vencimiento", plazoFijoLogro.fechaVencimiento("dd/MM/yyyy"));
			item.set("cuota", plazoFijoLogro.montoProximoPlazoFijo());
			item.set("cuotaFormateada", Formateador.importe(plazoFijoLogro.montoProximoPlazoFijo()));
			item.set("idEstado", plazoFijoLogro.idEstado());
			item.set("estado", plazoFijoLogro.descripcionEstado());

			item.set("descripcionCuota", item.integer("constituidos") + " de " + item.integer("cantidadCuotas")); // TODO: deprecar
			item.set("cuotaActual", item.integer("constituidos")); // TODO: deprecar
			item.set("cuotasTotales", item.integer("cantidadCuotas")); // TODO: deprecar

			item.set("esUVA", plazoFijoLogro.esUva(detalle.get()));
			if (plazoFijoLogro.esUva(detalle.get())) {
				item.set("montoUVA", plazoFijoLogro.montoUva(detalle.get()));
				item.set("montoUVAFormateado", Formateador.importe(plazoFijoLogro.montoUva(detalle.get())));
			}

			if (cuenta != null) {
				item.set("cuentaDescripcionCorta", cuenta.descripcionCorta());
				item.set("cuentaUltimos4digitos", cuenta.ultimos4digitos());
				item.set("cuentaSaldo", cuenta.saldo());
				item.set("cuentaSaldoFormateado", Formateador.importe(cuenta.saldo()));
			}

			for (Integer id = 1; id <= plazoFijoLogro.cantidadPlazosFijos(); ++id) {
				Objeto subitem = new Objeto();
				subitem.set("id", id);
				subitem.set("cuota", id);
				subitem.set("fechaConstitucion", plazoFijoLogro.itemFechaAlta(id, "dd/MM/yyyy", detalle.get()));
				subitem.set("fechaDebito", plazoFijoLogro.itemFechaVencimiento(id, "dd/MM/yyyy", detalle.get()));
				subitem.set("monto", plazoFijoLogro.itemMontoInicial(id, detalle.get()));
				subitem.set("montoFormateado", Formateador.importe(plazoFijoLogro.itemMontoInicial(id, detalle.get())));
				subitem.set("tna", plazoFijoLogro.itemTasa(id, detalle.get()));
				subitem.set("tnaFormateada", Formateador.importe(plazoFijoLogro.itemTasa(id, detalle.get())));
				subitem.set("intereses", plazoFijoLogro.itemInteres(id, detalle.get()));
				subitem.set("interesesFormateado", Formateador.importe(plazoFijoLogro.itemInteres(id, detalle.get())));
				subitem.set("idEstado", plazoFijoLogro.itemIdEstado(id, detalle.get()));
				subitem.set("estado", plazoFijoLogro.itemDescripcionEstado(id, detalle.get()));
				subitem.set("numero", plazoFijoLogro.itemNumero(id, detalle.get()));
				subitem.set("renovacionAutomatica", "N");
				subitem.set("garantiaDeposito", plazoFijoLogro.itemGarantiaDeposito(id, detalle.get()));
				subitem.set("plazo", plazoFijoLogro.itemCantidadDias(id, detalle.get()));
				subitem.set("montoVencimiento", plazoFijoLogro.itemMontoVencimiento(id, detalle.get()));
				subitem.set("montoVencimientoFormateado", Formateador.importe(plazoFijoLogro.itemMontoVencimiento(id, detalle.get())));
				subitem.set("forzable", plazoFijoLogro.forzable(id, detalle.get()));
				item.add("lista", subitem);
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


			respuesta.set("configuracion", configuracion);
			respuesta.add("plazosFijosLogros", item);
		}

		return respuesta;
	}

	public static Respuesta terminosCondicionesPlazosFijoLogros(ContextoHB contexto) {
		Date fecha = contexto.parametros.date("fecha", "d/M/yyyy", new Objeto().set("fecha", new Date()).date("fecha"));

		SqlRequest sqlRequest = Sql.request("SelectTyCPlazosFijoLogro", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[pf_meta_tyc] WHERE ? BETWEEN vigencia_desde AND vigencia_hasta";
		sqlRequest.parametros.add(fecha);

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		for (Objeto item : sqlResponse.registros) {
			return Respuesta.exito("texto", item.string("data"));
		}

		return Respuesta.error();
	}

	public static Respuesta monedaValidasPlazosFijos(ContextoHB contexto) {
		Objeto monedas = new Objeto();
		monedas.add(new Objeto().set("id", "80").set("descripcion", "Pesos"));
		monedas.add(new Objeto().set("id", "2").set("descripcion", "Dolares"));
		monedas.add(new Objeto().set("id", "88").set("descripcion", "UVAs"));
		return Respuesta.exito("monedas", monedas);
	}

	public static Respuesta parametriaPlazoFijoLogros(ContextoHB contexto) {
		String idMoneda = contexto.parametros.string("idMoneda");

		if (Objeto.anyEmpty(idMoneda)) {
			return Respuesta.parametrosIncorrectos();
		}

		ApiRequest request = null;

		request = Api.request("PlazosFijosGetParametria", "catalogo", "GET", "/v1/plazoFijos/parametrias", contexto);
		request.query("operacion", "Q");
		request.query("opcion", "1");
		request.query("codCliente", contexto.idCobis());
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();
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

		return Respuesta.error();
	}

	@SuppressWarnings("static-access")
	public static Respuesta modificarPlazoFijoLogros(ContextoHB contexto) {
		String id = contexto.parametros.string("id");
		String nombre = contexto.parametros.string("nombre");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");

		if (Objeto.anyEmpty(id)) {
			return Respuesta.parametrosIncorrectos();
		}

		PlazoFijoLogro plazoFijoLogro = contexto.plazoFijoLogro(id);
		if (plazoFijoLogro == null) {
			return Respuesta.estado("PLAZO_FIJO_LOGRO_NO_EXISTE");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("CUENTA_NO_EXISTE");
		}

		ApiRequest request = null;
		request = Api.request("PlazosFijosGetModificacionLogro", "plazosfijos", "GET", "/v1/{idCobis}/modificacionPlazoFijoAhorro", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.path("idCobis", contexto.idCobis());
		request.query("planContratado", id);
		request.query("moneda", plazoFijoLogro.idMoneda());
		request.query("tipoCuenta", cuenta.idTipo());
		request.query("cuenta", cuenta.numero());
		request.query("monto", monto.toString());
		request.query("nombre", nombre);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			if ("143136".equals(response.string("codigo"))) {
				Api.eliminarCache(contexto, "CabeceraPlazoFijoLogro", contexto.idCobis(), "0");
				return Respuesta.exito();
			}
			return Respuesta.error();
		}
		

		//AGREGAR SALESFORCE
		if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			try {
				List<Objeto> obj = null;
				Objeto plazoFijoLogrocuota = null;
				List<Objeto> cuotasObject = null;
				Objeto cuotaActual = null;
				if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
					obj = PlazoFijoLogrosService.cabecera(contexto, "0").objetos();
					plazoFijoLogrocuota = (Objeto) obj.stream().filter(p ->	p.get("idPlanAhorro").toString().equals(id)).findFirst().orElse(null);
					plazoFijoLogro = new PlazoFijoLogro(contexto, plazoFijoLogrocuota);
					cuotasObject = plazoFijoLogro.plazosFijosLogroDetalle(plazoFijoLogro , 0, contexto).objetos();
					cuotaActual = cuotasObject.stream().filter(p -> p.get("estado").equals("A")).findFirst().orElse(null);
				}

				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
				parametros.set("CANAL", "Home Banking");
				parametros.set("MONEDA", Formateador.simboloMoneda(plazoFijoLogrocuota.string("moneda")));
				parametros.set("CAPITAL", Formateador.importe(plazoFijoLogrocuota.bigDecimal("monto")));
				parametros.set("NUMERO_PLAZO_FIJO",  cuotaActual.string("nroCertificado"));
				BigDecimal TNA = plazoFijoLogrocuota.bigDecimal("tasa");
				parametros.set("TNA", TNA);
				BigDecimal TEA = calcularTEA(TNA);
				BigDecimal TEM = calcularTEM(TEA);
				parametros.set("TEA",  TEA);
				parametros.set("TEM", TEM);
				parametros.set("PLAZO_CONSTITUCION_DIAS", null);
				parametros.set("FECHA_VENCIMIENTO", plazoFijoLogrocuota.string("vencimiento"));
				parametros.set("GARANTIA_DEPOSITOS", cuotaActual.string("garantizado"));
				parametros.set("TIPO_PLAZO_FIJO", plazoFijoLogrocuota.string("tipoPlazoFijo"));
				parametros.set("NOMBRE_PLAZO_FIJO", plazoFijoLogrocuota.string("nombre"));
				parametros.set("CANTIDAD_DEPOSITOS_DIA_BAJA", plazoFijoLogrocuota.string("cuota")); //baja o cancelacion
				parametros.set("CANTIDAD_DEPOSITOS_PROYECTADO", plazoFijoLogrocuota.string("cantidadPlazos"));
				parametros.set("DIA_DEBITO", plazoFijoLogrocuota.string("diaConstitucionPF"));
				parametros.set("MONTO_DEBITO", Formateador.importe(plazoFijoLogrocuota.bigDecimal("monto")));
				parametros.set("FECHA_CANCELACION_BAJA", new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
				parametros.set("DEPOSITO_EFECTIVO", plazoFijoLogrocuota.string("secuencial")); //baja o modificacion
				parametros.set("FECHA_CONSTITUCION", plazoFijoLogrocuota.string("fechaConstPlan"));
				parametros.set("INTERES_COBRAR", Formateador.importe(cuotaActual.bigDecimal("montoInteres")));
				parametros.set("MONTO_VENCIMIENTO", Formateador.importe(plazoFijoLogrocuota.bigDecimal("monto")));
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_modificacion_plazo_fijo_logros"), parametros));
			}
			catch(Exception e) {
				
			}
		}		
		
		Api.eliminarCache(contexto, "CabeceraPlazoFijoLogro", contexto.idCobis(), "0");

		plazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroDetalle();
		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		return Respuesta.exito();
	}

	public static Respuesta simularPlazoFijoLogro(ContextoHB contexto) {
		String idParametria = contexto.parametros.string("idParametria");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String nombre = contexto.parametros.string("nombre");
		Integer dia = contexto.parametros.integer("dia");

		if (Objeto.anyEmpty(idParametria, idCuenta, monto, nombre, dia)) {
			return Respuesta.parametrosIncorrectos();
		}

		// TODO: Se retira por Normativo “A” 7849
		String moneda = idParametria.split("_")[2];
		if (!moneda.equals("80") && contexto.persona().esMenor()) {
			return Respuesta.estado("MENOR_NO_AUTORIZADO");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);

		ApiRequest request = null;
		request = Api.request("SimuladoresGetLogro", "simuladores", "GET", "/v1/plazoFijos/meta", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.query("operacion", "S");
		request.query("opcion", "S");
		request.query("codCliente", contexto.idCobis());
		request.query("planContratado", idParametria.split("_")[1]);
		request.query("moneda", idParametria.split("_")[2]);
		request.query("cuenta", cuenta != null ? cuenta.numero() : "0");
		request.query("tipoCuenta", cuenta != null ? cuenta.idTipo() : "");
		request.query("monto", monto.toString());
		request.query("nombre", nombre.replace(" ", ""));
		request.query("dia", dia.toString());
		request.query("cuota", idParametria.split("_")[0]);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}

		Respuesta respuesta = new Respuesta();
		for (Objeto item : response.objetos()) {
			Objeto objeto = new Objeto();
			objeto.set("cuota", item.string("secuencial"));
			objeto.set("diaDebito", item.date("fechaConstiTeo", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
			if ("1".equals(item.string("secuencial"))) {

				ApiRequest requestPrimero = null;
				requestPrimero = Api.request("SimuladoresGetPlazoFijos", "simuladores", "GET", "/v1/plazoFijos", contexto);
				request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
				requestPrimero.query("idcliente", contexto.idCobis());
				requestPrimero.query("tipoOperacion", idParametria.split("_")[3]);
				requestPrimero.query("plazo", item.string("diasPlazo"));
				requestPrimero.query("monto", item.string("monto"));
				requestPrimero.query("moneda", item.string("moneda"));
				if (cuenta != null) {
					requestPrimero.query("tipoCuenta", cuenta.idTipo());
					requestPrimero.query("cuenta", cuenta.numero());
				}

				ApiResponse responsePrimero = Api.response(requestPrimero, contexto.idCobis());
				if (responsePrimero.hayError()) {

					// TODO: DLV-43547
					if ("123008".equals(responsePrimero.string("codigo")) && contexto.esProcrear(contexto)) {
						return Respuesta.estado("OPERACION_NO_POSIBLE_PROCREAR");
					}

					if ("123008".equals(responsePrimero.string("codigo"))) {
						return Respuesta.estado("OPERACION_NO_POSIBLE");
					}

					// TODO: DLV-43547
					if (contexto.esProcrear(contexto)) {
						return Respuesta.estado("OPERACION_NO_POSIBLE_PROCREAR");
					}

					return Respuesta.error();
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

	public static Respuesta altaPlazoFijoLogro(ContextoHB contexto) {
		String idParametria = contexto.parametros.string("idParametria");
		String idCuenta = contexto.parametros.string("idCuenta");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String nombre = contexto.parametros.string("nombre");
		Integer dia = contexto.parametros.integer("dia");

		if (Objeto.anyEmpty(idParametria, idCuenta, monto, nombre, dia)) {
			return Respuesta.parametrosIncorrectos();
		}

		// TODO: Se retira por Normativo “A” 7849
		String moneda = idParametria.split("_")[2];
		if (!moneda.equals("80") && contexto.persona().esMenor()) {
			return Respuesta.estado("MENOR_NO_AUTORIZADO");
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("NO_EXISTE_CUENTA");
		}
		ApiRequest request = null;
		request = Api.request("PlazosFijosLogrosPost", "plazosfijos", "POST", "/v1/plazoFijosMETA", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
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

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}

		String secuencial = response.objetos().get(0).string("secuencial");
		String diasPlazo = response.objetos().get(0).string("diasPlazo");

		ApiRequest requestPrimero = null;
		requestPrimero = Api.request("PlazosFijosPost", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
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

		ApiResponse responsePrimero = Api.response(requestPrimero, contexto.idCobis());
		if (responsePrimero.hayError()) {
			if ("258402".equals(responsePrimero.string("codigo"))) {
				return Respuesta.estado("SIN_PERFIL_PATRIMONIAL");
			}
			if ("141144".equals(responsePrimero.string("codigo"))) {
				return Respuesta.estado("SALDO_INSUFICIENTE");
			}
			if ("123008".equals(responsePrimero.string("codigo"))) {
				return Respuesta.estado("OPERACION_NO_POSIBLE");
			}
			return Respuesta.error();
		}

		//AGREGAR SALESFORCE
		if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			try {
				responsePrimero.set("idPlanAhorro", secuencial);
				PlazoFijoLogro plazoFijoLogro = new PlazoFijoLogro(contexto, responsePrimero);
				@SuppressWarnings("static-access")
				ApiResponse resp = plazoFijoLogro.plazosFijosLogroDetalle(plazoFijoLogro , 0, contexto);
				Objeto plazoFijoLogroPrimerCuota = ((Objeto) resp.lista.get(0));
				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
				parametros.set("CANAL", "Home Banking");
				parametros.set("MONEDA", Formateador.simboloMoneda(responsePrimero.string("moneda")));
				parametros.set("CAPITAL", Formateador.importe(responsePrimero.bigDecimal("capital")));
				parametros.set("NUMERO_PLAZO_FIJO",  responsePrimero.string("nroPlazoFijo"));
				BigDecimal tasaTNA = responsePrimero.string("tasa") != null ? responsePrimero.bigDecimal("tasa") : responsePrimero.bigDecimal("tasaInteres");
				parametros.set("TNA", tasaTNA);
				BigDecimal tea = calcularTEA(tasaTNA);
				BigDecimal tem = calcularTEM(tea);
				parametros.set("TEA",  tea);
				parametros.set("TEM", tem);
				parametros.set("PLAZO_CONSTITUCION_DIAS", responsePrimero.string("plazo"));
				LocalDate hoy = LocalDate.now();
				LocalDate fechaFutura = hoy.plusDays(responsePrimero.integer("plazo"));
				parametros.set("PLAZO_MESES", ChronoUnit.MONTHS.between(hoy, fechaFutura));
				parametros.set("FECHA_VENCIMIENTO", responsePrimero.string("fechaVencimiento"));
				parametros.set("GARANTIA_DEPOSITOS", responsePrimero.string("cubiertoPorGarantia"));
				parametros.set("TIPO_PLAZO_FIJO", responsePrimero.string("tipoOperacion"));
				parametros.set("NOMBRE_PLAZO_FIJO", responsePrimero.string("nombre"));
				parametros.set("CANTIDAD_DEPOSITOS_DIA_BAJA", null); //baja o cancelacion
				parametros.set("CANTIDAD_DEPOSITOS_PROYECTADO", idParametria.split("_")[0]);
				parametros.set("DIA_DEBITO", dia.toString());
				parametros.set("MONTO_DEBITO", monto.toString());
				parametros.set("FECHA_CANCELACION_BAJA",  null); //cancelacion
				parametros.set("NOMBRE_PLAZO_FIJO", nombre);
				parametros.set("DEPOSITO_EFECTIVO", null); //baja o modificacion
				parametros.set("FECHA_CONSTITUCION", responsePrimero.date("fechaActual", "yyyy-MM-dd", "dd/MM/yyyy"));
				parametros.set("INTERES_COBRAR", Formateador.importe(plazoFijoLogroPrimerCuota.bigDecimal("montoInteres")));
				parametros.set("MONTO_VENCIMIENTO", Formateador.importe(responsePrimero.bigDecimal("monto")));
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_alta_plazo_fijo_logro"), parametros));
			}
			catch(Exception e) {
				
			}
		}
		

		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		ProductosService.eliminarCacheProductos(contexto);
		return Respuesta.exito();
	}

	@SuppressWarnings("static-access")
	public static Respuesta bajaPlazoFijoLogro(ContextoHB contexto) {
		String id = contexto.parametros.string("id");

		// TODO: Se retira por Normativo “A” 7849
		if (contexto.persona().esMenor()) {
			return Respuesta.estado("MENOR_NO_AUTORIZADO");
		}

		List<Objeto> obj = null;
		Objeto plazoFijoLogrocuota = null;
		PlazoFijoLogro plazoFijoLogro = null;;
		List<Objeto> cuotasObject = null;
		Objeto cuotaActual = null;
		if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			obj = PlazoFijoLogrosService.cabecera(contexto, "0").objetos();
			plazoFijoLogrocuota = (Objeto) obj.stream().filter(p ->	p.get("idPlanAhorro").toString().equals(id)).findFirst().orElse(null);
			plazoFijoLogro = new PlazoFijoLogro(contexto, plazoFijoLogrocuota);
			cuotasObject = plazoFijoLogro.plazosFijosLogroDetalle(plazoFijoLogro , 0, contexto).objetos();
			cuotaActual = cuotasObject.stream().filter(p -> "A".equals(p.get("estado")))
				    .reduce((first, second) -> second).orElse(null);
		}

		
		ApiRequest request = null;
		request = Api.request("PlazosFijosGetBajaLogro", "plazosfijos", "GET", "/v1/{idCobis}/bajaPlazoFijoAhorro", contexto);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.path("idCobis", contexto.idCobis());
		request.query("planContratado", id);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			if ("143136".equals(response.string("codigo"))) {
				return Respuesta.exito();
			}
			return Respuesta.error();
		}

		//AGREGAR SALESFORCE
		if (HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
			try {
				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO",contexto.persona().apellido());
				parametros.set("CANAL", "Home Banking");
				parametros.set("MONEDA", Formateador.simboloMoneda(plazoFijoLogrocuota.string("moneda")));
				parametros.set("CAPITAL", Formateador.importe(plazoFijoLogrocuota.bigDecimal("monto")));
				parametros.set("NUMERO_PLAZO_FIJO",  cuotaActual.string("nroCertificado"));
				BigDecimal TNA = plazoFijoLogrocuota.bigDecimal("tasa");
				BigDecimal TEA = calcularTEA(TNA);
				BigDecimal TEM = calcularTEM(TEA);
				parametros.set("TNA", TNA);
				parametros.set("TEA",  TEA);
				parametros.set("TEM", TEM);
				parametros.set("PLAZO_CONSTITUCION_DIAS", null);
				parametros.set("FECHA_VENCIMIENTO", plazoFijoLogrocuota.string("vencimiento"));
				parametros.set("GARANTIA_DEPOSITOS", cuotaActual.string("garantizado"));
				parametros.set("TIPO_PLAZO_FIJO", plazoFijoLogrocuota.string("tipoPlazoFijo"));
				parametros.set("NOMBRE_PLAZO_FIJO", plazoFijoLogrocuota.string("nombre"));
				parametros.set("CANTIDAD_DEPOSITOS_DIA_BAJA", cuotaActual.string("cuota")); //baja o cancelacion
				parametros.set("CANTIDAD_DEPOSITOS_PROYECTADO", plazoFijoLogrocuota.string("cantidadPlazos"));
				parametros.set("DIA_DEBITO", plazoFijoLogrocuota.string("diaConstitucionPF"));
				parametros.set("MONTO_DEBITO", Formateador.importe(plazoFijoLogrocuota.bigDecimal("monto")));
				parametros.set("FECHA_CANCELACION_BAJA", new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date()));
				parametros.set("DEPOSITO_EFECTIVO", cuotaActual.string("secuencial")); //baja o modificacion
				parametros.set("FECHA_CONSTITUCION", plazoFijoLogrocuota.string("fechaConstPlan"));
				parametros.set("INTERES_COBRAR", Formateador.importe(cuotaActual.bigDecimal("montoInteres")));
				parametros.set("MONTO_VENCIMIENTO", Formateador.importe(plazoFijoLogrocuota.bigDecimal("monto")));
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_baja_plazo_fijo_logro"), parametros));
			}
			catch(Exception e) {
				
			}
		}

		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		return Respuesta.exito();
	}

	public static Respuesta forzarPlazoFijoLogro(ContextoHB contexto) {
		String id = contexto.parametros.string("id");
		String cuota = contexto.parametros.string("cuota");

		// TODO: Se retira por Normativo “A” 7849
		if (contexto.persona().esMenor()) {
			return Respuesta.estado("MENOR_NO_AUTORIZADO");
		}

		ApiRequest request = null;
		request = Api.request("PlazosFijosGetForzadoLogro", "plazosfijos", "GET", "/v1/{idCobis}/forzadoPlazoFijo", contexto);
		request.path("idCobis", contexto.idCobis());
		request.query("planContratado", id);
		request.query("secuencialPlazoFijo", cuota);

		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.hayError()) {
			return Respuesta.error();
		}

		contexto.plazoFijoLogro(id).eliminarCachePlazosFijosWindowsGetLogroDetalle();
		PlazoFijoLogro.eliminarCachePlazosFijosWindowsGetLogroCabecera(contexto);
		return Respuesta.exito();
	}
	
	//calcularTEA y calcularTEM solo sirven para tradicional pesos/dolares
	private static BigDecimal calcularTEM(BigDecimal teaPorcentaje) {
	    MathContext mc = new MathContext(15, RoundingMode.HALF_UP);
	    BigDecimal uno = BigDecimal.ONE;
	    BigDecimal teaDecimal = teaPorcentaje.divide(BigDecimal.valueOf(100), mc);
	    double pow = Math.pow(teaDecimal.add(uno).doubleValue(), 1.0 / 12.0);
	    BigDecimal temDecimal = BigDecimal.valueOf(pow).subtract(uno);
	    return temDecimal.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
	}
	
	//Cuando no dispongo del capital+interes
	private static BigDecimal calcularTEA(BigDecimal tna) {
    	tna = tna.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
        BigDecimal uno = BigDecimal.ONE;
        BigDecimal diasAnio = BigDecimal.valueOf(365);
        BigDecimal factor = uno.add(tna.divide(diasAnio, 10, RoundingMode.HALF_UP));
        BigDecimal tea = pow(factor, 365).subtract(uno);
        return tea.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }
	
	private static BigDecimal pow(BigDecimal base, double exponent) {
        double result = Math.pow(base.doubleValue(), exponent);
        return new BigDecimal(result, new MathContext(15, RoundingMode.HALF_UP));
    }

}
