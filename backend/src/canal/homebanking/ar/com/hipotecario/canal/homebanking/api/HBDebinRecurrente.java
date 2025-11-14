package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaTercero;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestDebin;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;

public class HBDebinRecurrente {

	public static String prestacion = "PlanSueldoBH";

	public static Respuesta consulta(ContextoHB contexto) {
		ApiRequest request = Api.request("Recurrencias", "agendas", "GET", "/v1/recurrencia/{idCobis}/vigentes", contexto);
		request.path("idCobis", contexto.idCobis());
		request.cacheSesion = true;
		request.cache204 = true;
		ApiResponse response = Api.response(request, contexto.idCobis());

		if (response.hayError() || !response.string("codigo").isEmpty()) {
			return Respuesta.error();
		}

		Boolean mostrarOferta = true;
		Persona per = contexto.persona();
		if (per != null) {
			mostrarOferta &= !per.esEmpleado();
		}
		mostrarOferta &= !contexto.depositaSueldo();

		Boolean esTarget = false;
		esTarget |= contexto.tienePaquete();
		esTarget |= contexto.tarjetaCreditoTitular() != null;

		Respuesta respuesta = new Respuesta();
		respuesta.set("mostrarOferta", mostrarOferta);
		respuesta.set("esTarget", esTarget);
		respuesta.set("esPlanSueldoExcluyente", contexto.esPlanSueldoExcluyente());
		respuesta.set("montoMinimo", montoMinimoV2(contexto));
		respuesta.set("esPlanSueldoInactivo", esPlanSueldoInactivo(contexto));

		for (Objeto item : response.objetos()) {
			
			if (item.string("estado").toUpperCase().equals("V") || item.string("estado").toUpperCase().equals("T")) {
				String cuentaOrigen = "CBU XXX-" + Formateador.ultimos4digitos(item.string("cuentaOtroBanco.cbu"));

				String cuentaDestino = item.string("cuentaBH.ctaBanco").startsWith("3") ? "CC " : "CA ";
				cuentaDestino += "XXX-" + Formateador.ultimos4digitos(item.string("cuentaBH.ctaBanco"));

				Objeto datos = new Objeto();
				datos.set("estado", item.string("estado").toUpperCase());
				datos.set("id", item.string("idRecurrencia"));
				datos.set("cbuOrigen", item.string("cuentaOtroBanco.cbu"));
				datos.set("cbuDestino", item.string("cuentaBH.cbu"));
				datos.set("descripcionCuentaOrigen", cuentaOrigen);
				datos.set("descripcionCuentaDestino", cuentaDestino);
				datos.set("bancoOrigen", RestCatalogo.banco(item.string("cuentaOtroBanco.cbu").substring(0, 3)));
				datos.set("bancoDestino", RestCatalogo.banco(item.string("cuentaBH.cbu").substring(0, 3)));
				datos.set("monto", item.bigDecimal("valor"));
				datos.set("montoFormateado", Formateador.importe(item.bigDecimal("valor")));
				datos.set("simboloMoneda", "$");
				datos.set("diaInicial", item.integer("diaInicial"));
				datos.set("diaFinal", item.integer("diaFinal"));
				respuesta.add("recurrencias", datos);
			}
		}

		return respuesta;
	}

	public static Respuesta alta(ContextoHB contexto) {
		String idCuenta = contexto.parametros.string("idCuenta");
		String cbu = contexto.parametros.string("cbu");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		Integer diaInicial = contexto.parametros.integer("diaInicial");
		Integer diaFinal = contexto.parametros.integer("diaFinal");

		Objeto log = new Objeto();
		log.set("cbu", cbu);
		log.set("cuentaBH", idCuenta);
		log.set("monto", monto);
		log.set("diaInicial", diaInicial);
		log.set("diaFinal", diaFinal);
		log.set("idRecurrencia", "");
		log.set("accion", "ALTA");
		log.set("estado", "ERROR");
		log.set("error", "");

		try {
			if (Objeto.anyEmpty(idCuenta, cbu, monto)) {
				return Respuesta.parametrosIncorrectos();
			}

			BigDecimal montoMinimo = montoMinimo(contexto);
			if (monto.compareTo(montoMinimo) < 0) {
				log.set("error", "MONTO_MINIMO_NO_SUPERADO");
				return Respuesta.estado("MONTO_MINIMO_NO_SUPERADO").set("mensaje", "El monto mínimo deberá ser de $ " + Formateador.importe(montoMinimo.toString()));
			}

			Cuenta cuenta = contexto.cuenta(idCuenta);
			if (cuenta == null) {
				log.set("error", "CUENTA_NO_ENCONTRADA");
				return Respuesta.estado("CUENTA_NO_ENCONTRADA");
			}
			if (cuenta.esCuentaCorriente()) {
				log.set("error", "TIPO_CUENTA_NO_ADMITIDA");
				return Respuesta.estado("TIPO_CUENTA_NO_ADMITIDA");
			}
			if (cuenta.esDolares()) {
				log.set("error", "MONEDA_NO_ADMITIDA");
				return Respuesta.estado("MONEDA_NO_ADMITIDA");
			}

			CuentaTercero cuentaTercero = new CuentaTercero(contexto, cbu, true);
			if (cuentaTercero.cuentaCoelsa.hayError()) {
				log.set("error", "ERROR_COELSA");
				return Respuesta.estado("ERROR_COELSA");
			}

			Boolean esTitular = cuentaTercero.mismoTitularColesa(contexto.persona().cuit());
			if (!esTitular) {
				List<Objeto> cotitulares = cuentaTercero.cuentaCoelsa.objetos("cotitulares");
				for (Objeto cotitular : cotitulares) {
					String cuitOrigen = contexto.persona().cuit();
					String cuitDestino = cotitular.string("cuit");
					if (cuitOrigen.equals(cuitDestino)) {
						esTitular = true;
						continue;
					}
				}
			}

			if (!esTitular && ConfigHB.esOpenShift()) {
				log.set("error", "NO_ES_TITULAR");
				return Respuesta.estado("NO_ES_TITULAR");
			}

			// Alta Cuenta Vendedor
			try {

				Boolean esCuentaVendedor = RestDebin.cuentaActivaVendedor(contexto, cuenta);
				if (!esCuentaVendedor) {
					ApiResponse response = RestDebin.activarCuentaVendedor(contexto, cuenta);
					if (response.hayError()) {
						log.set("error", "ERROR_ACTIVANDO_CUENTA_VENDEDOR");
						return Respuesta.estado("ERROR_ACTIVANDO_CUENTA_VENDEDOR");
					}
				}
			} catch (ApiException respon) {
				log.set("error", "VENDEDOR_NO_ENCONTRADO");
				return Respuesta.estado("VENDEDOR_NO_ENCONTRADO");
			}

			// Alta Cuenta Comprador
//		Boolean esCuentaComprador = RestDebin.cuentaActivaComprador(contexto, contexto.persona().cuit(), cuenta.cbu());
//		if (!esCuentaComprador) {
//			ApiResponse response = RestDebin.activarCuentaComprador(contexto,  contexto.persona().cuit(), cuenta.cbu());
//			if (response.hayError()) {
//				return Respuesta.estado("ERROR_ACTIVANDO_CUENTA_COMPRADOR");
//			}
//		}

			// Alta Prestacion
			Boolean existePrestacion = RestDebin.existePrestacion(contexto, prestacion);
			if (!existePrestacion) {
				ApiResponse response = RestDebin.crearPrestacion(contexto, prestacion);
				if (response.hayError()) {
					log.set("error", "ERROR_CREANDO_PRESTACION");
					return Respuesta.estado("ERROR_CREANDO_PRESTACION");
				}
			}

			// Alta Recurrencia
			ApiResponse responseAlta = RestDebin.crearRecurrencia(contexto, cuenta, cuentaTercero, prestacion);
			if (responseAlta.hayError()) {
				log.set("error", "ERROR_CREANDO_RECURRENCIA");
				return Respuesta.estado("ERROR_CREANDO_RECURRENCIA");
			}

			String cbuTercero = cuentaTercero.cbu();
			ApiRequest request = Api.request("CrearRecurrencia", "agendas", "POST", "/v1/recurrencia", contexto);
			Objeto datos = new Objeto();
			datos.set("cliente").set("cliente", contexto.idCobis()).set("cuil", contexto.persona().cuit());
			datos.set("cuentaBH").set("cbu", cuenta.cbu()).set("ctaBanco", cuenta.numero()).set("cuenta", cuenta.id());
			datos.set("cuentaOtroBanco").set("cbu", cbuTercero);
			datos.set("diaInicial", diaInicial);
			datos.set("diaFinal", diaFinal);
			datos.set("idRecurrencia", null);
			datos.set("moneda", 80);
			datos.set("prestacion", null);
			datos.set("tipo", "D");
			datos.set("valor", monto);
			request.body(datos);

			ApiResponse response = Api.response(request, contexto.idCobis());
			if (response.hayError() || !response.string("codigo").isEmpty()) {
				if (response.string("codigo").equals("23921") || response.string("codigo").equals("23823")) {
					log.set("error", "RECURRENCIA_EXISTENTE");
					return Respuesta.estado("RECURRENCIA_EXISTENTE");
				}
				return Respuesta.error();
			}
			
			boolean estaAprobada = "5000".equals(responseAlta.objeto("respuesta").string("codigo"));
			if(estaAprobada) {
				ApiRequest requestAviso = Api.request("AvisoRecurrencia", "debin", "POST", "/v1/coelsa/AvisoRecurrencia", contexto);
				Objeto bodyAviso = new Objeto();
				bodyAviso.set("id", responseAlta.string("id"));
				bodyAviso.set("comprador").set("cuit", contexto.persona().cuit()).set("cbu", cbuTercero);
				bodyAviso.set("debin").set("prestacion", prestacion);
				bodyAviso.set("estado").set("codigo", responseAlta.objeto("respuesta").string("codigo")).set("descripcion", responseAlta.objeto("respuesta").string("descripcion"));
				requestAviso.body(bodyAviso);

				ApiResponse responseAviso = Api.response(requestAviso, contexto.idCobis());
				if (responseAviso.hayError()) {
					return Respuesta.error();
				}
			}
			
			Api.eliminarCache(contexto, "Recurrencias", contexto.idCobis());
			Respuesta respuesta = new Respuesta();
			log.set("estado", "OK");
			
			if(estaAprobada) {
				try {
					String canal = contexto.parametros.string("canal");
					if(!"CRM".equalsIgnoreCase(canal)){
						respuesta.set("estado", "APROBADO");
					}
				}catch (Exception e){
					respuesta.set("estado", "APROBADO");
				}
			}

			return respuesta;
		} finally {
			insertSueldoDuho(contexto, log);
		}
	}

	public static void insertSueldoDuho(ContextoHB contexto, Objeto objeto) {
		try {
			SqlRequest request = Sql.request("logsSoftTokenAlta", "hbs");
			request.sql = """
					INSERT INTO [hbs].[dbo].[auditor_sueldo_duho] ([momento],[cobis],[canal],[cbu],[cuentaBH],[monto],[diaInicial],[diaFinal],[idRecurrencia],[accion],[estado],[error])
					VALUES (?,?,?,?,?,?,?,?,?,?)
					""";
			String canal = contexto.sesion.canal;
			request.add(new Date());
			request.add(contexto.idCobis());
			request.add(canal != null && !canal.isEmpty() ? canal : "HB");
			request.add(objeto.string("cbu"));
			request.add(objeto.string("cuentaBH"));
			request.add(objeto.string("monto"));
			request.add(objeto.string("diaInicial"));
			request.add(objeto.string("diaFinal"));
			request.add(objeto.string("idRecurrencia"));
			request.add(objeto.string("accion"));
			request.add(objeto.string("estado"));
			request.add(objeto.string("error"));
			Sql.response(request);
		} catch (Throwable t) {
		}
	}

	public static void guardarDesicionUsuario(ContextoHB contexto, Boolean acepto) {
		try {
			String canal = contexto.sesion.canal;
			String cobis = contexto.idCobis();
			SqlRequest sqlRequest = Sql.request("InsertOrUpdateDecicionUsuario", "hbs");

			sqlRequest.sql = "UPDATE [Hbs].[dbo].[decicionUsuario_cobis] ";
			sqlRequest.sql += "SET [fecha_modificacion] = ?, [acepto] = ? WHERE [id_cobis] = ? ";

			sqlRequest.add(new Date());
			sqlRequest.add(acepto ? "1" : "0");
			sqlRequest.add(cobis);

			sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
			sqlRequest.sql += "INSERT INTO [Hbs].[dbo].[decicionUsuario_cobis] ([id_cobis], [fecha_modificacion], [acepto], [canal]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?) ";
			sqlRequest.add(cobis);
			sqlRequest.add(new Date());
			sqlRequest.add(acepto ? "1" : "0");
			sqlRequest.add(canal != null ? canal : "HB");

			Sql.response(sqlRequest);
		} catch (Throwable t) {
		}
	}

	public static Boolean leerDesicionUsuario(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("SelectDecicionUsuario", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[decicionUsuario_cobis] WHERE id_cobis = ?";
		sqlRequest.add(contexto.idCobis());
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		Integer valor = null;
		for (Objeto registro : sqlResponse.registros) {
			valor = registro.integer("acepto");
		}
		return (valor != null && valor == 1);
	}

	public static Respuesta modificacion(ContextoHB contexto) {
		String idRecurrencia = contexto.parametros.string("idRecurrencia");
		String diaInicial = contexto.parametros.string("diaInicial", null);
		String diaFinal = contexto.parametros.string("diaFinal", null);
		BigDecimal monto = contexto.parametros.bigDecimal("monto");

		boolean aumentamosPorVos = contexto.parametros.bool("aumentamosPorVos", false);

		Objeto log = new Objeto();
		log.set("cbu", "");
		log.set("cuentaBH", "");
		log.set("monto", monto);
		log.set("diaInicial", diaInicial);
		log.set("diaFinal", diaFinal);
		log.set("idRecurrencia", idRecurrencia);
		log.set("accion", "MODIFICACION");
		log.set("estado", "ERROR");
		log.set("error", "");

		try {
			if (Objeto.anyEmpty(idRecurrencia) || Objeto.allEmpty(diaInicial, diaFinal, monto)) {
				return Respuesta.parametrosIncorrectos();
			}

			BigDecimal montoMinimo = montoMinimoV2(contexto);
			if (monto.compareTo(montoMinimo) < 0) {
				log.set("error", "MONTO_MINIMO_NO_SUPERADO");
				return Respuesta.estado("MONTO_MINIMO_NO_SUPERADO").set("mensaje", "El monto mínimo deberá ser de $ " + Formateador.importe(montoMinimo.toString()));
			}

			ApiRequest request = Api.request("ModificarRecurrencia", "agendas", "PATCH", "/v1/recurrencia", contexto);
			Objeto datos = new Objeto();
			datos.set("cliente").set("cliente", contexto.idCobis());
			datos.set("diaInicial", diaInicial);
			datos.set("diaFinal", diaFinal);
			datos.set("idRecurrencia", idRecurrencia);
			datos.set("tipo", "D");
			datos.set("valor", monto);
			request.body(datos);

			ApiResponse response = Api.response(request, contexto.idCobis(), idRecurrencia);
			if (response.hayError() || !response.string("codigo").isEmpty()) {
				return Respuesta.error();
			}

			Api.eliminarCache(contexto, "Recurrencias", contexto.idCobis());
			log.set("estado", "OK");
			return Respuesta.exito();
		} finally {
			insertSueldoDuho(contexto, log);
			guardarDesicionUsuario(contexto, aumentamosPorVos);
		}
	}

	public static Respuesta baja(ContextoHB contexto) {
		String idRecurrencia = contexto.parametros.string("idRecurrencia");

		Objeto log = new Objeto();
		log.set("cbu", "");
		log.set("cuentaBH", "");
		log.set("monto", "");
		log.set("diaInicial", "");
		log.set("diaFinal", "");
		log.set("idRecurrencia", idRecurrencia);
		log.set("accion", "BAJA");
		log.set("estado", "ERROR");
		log.set("error", "");

		try {
			if (Objeto.anyEmpty(idRecurrencia)) {
				return Respuesta.parametrosIncorrectos();
			}

			ApiRequest request = Api.request("EliminarRecurrencia", "agendas", "DELETE", "/v1/recurrencia/{idCobis}/{idRecurrencia}", contexto);
			request.path("idCobis", contexto.idCobis());
			request.path("idRecurrencia", idRecurrencia);

			ApiResponse response = Api.response(request, contexto.idCobis(), idRecurrencia);
			if (response.hayError() || !response.string("codigo").isEmpty()) {
				return Respuesta.error();
			}

			Api.eliminarCache(contexto, "Recurrencias", contexto.idCobis());
			log.set("estado", "OK");
			return Respuesta.exito();
		} finally {
			insertSueldoDuho(contexto, log);
		}
	}

	public static Object terminosCondiciones(ContextoHB contexto) {
		ApiRequest request = Api.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", "0");
		request.query("grupocodigo", "SUELDODUO");
		request.query("canal", "HB");
		request.header("x-cuil", contexto.persona().cuit());
		try {
			request.header("x-apellidoNombre", URLEncoder.encode(contexto.persona().apellidos() + " " + contexto.persona().nombres(), "UTF-8"));
		} catch (Exception e) {
			request.header("x-apellidoNombre", contexto.persona().apellidos() + " " + contexto.persona().nombres());
		}
		request.header("x-tipoPersona", "F");
		request.header("x-dni", contexto.persona().numeroDocumento());
		request.header("x-producto", "SueldoDUO");

		ApiResponse response = Api.response(request, "0", "SUELDODUO", contexto.idCobis());
		if (response.hayError()) {
			contexto.responseHeader("estado", "ERROR");
			return Respuesta.error();
		}

		String base64 = response.string("Data");
		try {
			byte[] archivo = Base64.getDecoder().decode(base64);
			contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + "SueldoDuo" + ".pdf");
			contexto.responseHeader("estado", "0");
			return archivo;
		} catch (Exception e) {
			contexto.responseHeader("estado", "ERROR");
			return Respuesta.error();
		}
	}

	public static Respuesta consultaAumentoMontoMinimo(ContextoHB contexto) {
		Respuesta respuestaConsulta = consulta(contexto);

		if (respuestaConsulta.string("estado").equals("ERROR")) {
			return Respuesta.error();
		}
		boolean mostrarPopup = false;
		Respuesta respuesta = new Respuesta();
		int minimo = respuestaConsulta.integer("montoMinimo");
		for (Objeto recuerrencia : respuestaConsulta.objetos("recurrencias")) {
			if (recuerrencia.bigDecimal("monto").compareTo(BigDecimal.valueOf(minimo)) == -1) {
				mostrarPopup = true;
				respuesta.set("idRecurrencia", recuerrencia.string("id"));
				respuesta.set("diaInicial", recuerrencia.string("diaInicial"));
				respuesta.set("diaFinal", recuerrencia.string("diaFinal"));
				respuesta.set("monto", montoMinimoV2(contexto));
				respuesta.set("montoFormateado", Formateador.importe(montoMinimoV2(contexto)));
				break;
			}
		}

		if (mostrarPopup) {
			Respuesta respRegistros = consultaCantidadVecesMostradas(contexto, "AUMENTOMIN");
			if (respRegistros != null && respRegistros.existe("mostrarPopup")) {
				mostrarPopup = respRegistros.bool("mostrarPopup");
			}
			if (mostrarPopup) {
				incrementarCantidadVecesMostradas(contexto, "AUMENTOMIN");
			}
		}

		respuesta.set("mostrarPopup", mostrarPopup);
		respuesta.set("aumentamosPorVos", leerDesicionUsuario(contexto));

		return respuesta;
	}

	/* =========== MONTO MINIMO ========== */
	public static BigDecimal montoMinimo(ContextoHB contexto) {
		return contexto.tienePaquete() ? ConfigHB.bigDecimal("monto_minimo_plan_sueldo", "15000") : ConfigHB.bigDecimal("monto_minimo_plan_sueldo_sin_paquete", "15000");
		// return Config.bigDecimal("monto_minimo_plan_sueldo", "25000");
	}

	private static boolean esPlanSueldoInactivo(ContextoHB contexto) {

		try {
			ApiResponse convenios = RestPersona.convenios(contexto);
			if (convenios.hayError()) {
				return HBProducto.esPlanSueldoInactivo(contexto);
			} else {
				Objeto convenio = convenios.objetos().get(0);
				if (convenio.get("codigoConvenio").equals("3500")) {
					return true;
				}
			}
		} catch (Exception ignred) {

		}
		return HBProducto.esPlanSueldoInactivo(contexto);
	}

	public static BigDecimal montoMinimoV2(ContextoHB contexto) {
		String codigoPaquete = "";
		ApiResponse response = ProductosService.productos(contexto, false);
		BigDecimal montoMinimo = null;

		if (contexto.tienePaquete()) {
			for (Objeto item : response.objetos("productos")) {
				if ("PAQ".equals(item.string("tipo")) || "T".equals(item.string("tipoTitularidad"))) {
					codigoPaquete = item.string("codigoPaquete");
					switch (codigoPaquete) {
					case "39":
						montoMinimo = ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_facil_pack", "35000");
						break;
					case "40":
						montoMinimo = ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_buho_pack", "35000");
						break;
					case "41":
						montoMinimo = ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_gold_pack", "35000");
						break;
					case "42":
						montoMinimo = ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_platinum_pack", "35000");
						break;
					case "43":
						montoMinimo = ConfigHB.bigDecimal("meta_bonificacion_plan_sueldo_black_pack", "35000");
						break;
					default:
						montoMinimo = ConfigHB.bigDecimal("monto_minimo_plan_sueldo", "35000");
					}
					break;
				}

			}
		} else {
			montoMinimo = ConfigHB.bigDecimal("monto_minimo_plan_sueldo_sin_paquete", "35000");
		}
		return montoMinimo;
	}

	private static void incrementarCantidadVecesMostradas(ContextoHB contexto, String nemonico) {
		try {
			SqlRequest sqlRequest = Sql.request("InsertContador", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'HB')";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			Sql.response(sqlRequest);
		} catch (Exception e) {

		}
	}

	private static Respuesta consultaCantidadVecesMostradas(ContextoHB contexto, String nemonico) {
		try {
			Respuesta respuesta = new Respuesta();

			SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");
			sqlRequest.sql = "SELECT TOP 1 * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ? order by momento desc";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			SqlResponse sqlResponse = Sql.response(sqlRequest);
			for (Objeto item : sqlResponse.registros) {

				respuesta.set("fecha", item.string("momento"));
				respuesta.set("mostrarPopup", false);

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				cal.setTime(sdf.parse(item.string("momento")));
				cal.add(Calendar.DAY_OF_MONTH, 7); // Incrementamos en 7 dias, la fecha guarda
				String momentoPlusDias = sdf.format(cal.getTime());

				Calendar fechaActual = Calendar.getInstance();
				fechaActual.setTime(new Date());
				Date dFechaActual = fechaActual.getTime();
				Date fechaMomentoPlusDias = new SimpleDateFormat("yyyy-MM-dd").parse(momentoPlusDias);
				/*
				 * if (date1.compareTo(date2) > 0) { System.out.println("Date1 is after Date2");
				 * } else if (date1.compareTo(date2) < 0) {
				 * System.out.println("Date1 is before Date2"); } else {
				 * System.out.println("Date1 is equal to Date2"); }
				 */
				if (dFechaActual.compareTo(fechaMomentoPlusDias) >= 0) {
					// En caso de cumplirse los 7 dias adicionales del ultimo guardado.. mostramos
					// popup
					respuesta.set("mostrarPopup", true);
				} else {
					respuesta.set("mostrarPopup", false);
				}
			}
			return respuesta;
		} catch (Exception e) {
		}
		return null;
	}
}
