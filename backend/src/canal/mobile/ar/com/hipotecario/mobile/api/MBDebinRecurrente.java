package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
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
import ar.com.hipotecario.mobile.excepcion.ApiExceptionMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.servicio.ProductosService;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestDebin;

public class MBDebinRecurrente {

	public static String prestacion = "PlanSueldoBH";

	public static RespuestaMB consulta(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("Recurrencias", "agendas", "GET", "/v1/recurrencia/{idCobis}/vigentes", contexto);
		request.path("idCobis", contexto.idCobis());
		request.cacheSesion = true;
		request.cache204 = true;
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

		if (response.hayError() || !response.string("codigo").isEmpty()) {
			return RespuestaMB.error();
		}

		Boolean mostrarOferta = true;
		mostrarOferta &= !contexto.persona().esEmpleado();
		mostrarOferta &= !contexto.depositaSueldo();

		Boolean esTarget = false;
		esTarget |= contexto.tienePaquete();
		esTarget |= contexto.tarjetaCreditoTitular() != null;

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("mostrarOferta", mostrarOferta);
		respuesta.set("esTarget", esTarget);
		respuesta.set("esPlanSueldoExcluyente", contexto.esPlanSueldoExcluyente());
		respuesta.set("esPlanSueldoInactivo", MBProducto.esPlanSueldoInactivo(contexto));
		respuesta.set("montoMinimo", montoMinimoV2(contexto));

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

	public static RespuestaMB alta(ContextoMB contexto) {
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
				return RespuestaMB.parametrosIncorrectos();
			}

			BigDecimal montoMinimo = montoMinimo(contexto);
			if (monto.compareTo(montoMinimo) < 0) {
				log.set("error", "MONTO_MINIMO_NO_SUPERADO");
				return RespuestaMB.estado("MONTO_MINIMO_NO_SUPERADO").set("mensaje", "El monto minimo es $ " + montoMinimo.toString());
			}

			Cuenta cuenta = contexto.cuenta(idCuenta);
			if (cuenta == null) {
				log.set("error", "CUENTA_NO_ENCONTRADA");
				return RespuestaMB.estado("CUENTA_NO_ENCONTRADA");
			}
			if (cuenta.esCuentaCorriente()) {
				log.set("error", "TIPO_CUENTA_NO_ADMITIDA");
				return RespuestaMB.estado("TIPO_CUENTA_NO_ADMITIDA");
			}
			if (cuenta.esDolares()) {
				log.set("error", "MONEDA_NO_ADMITIDA");
				return RespuestaMB.estado("MONEDA_NO_ADMITIDA");
			}

			CuentaTercero cuentaTercero = new CuentaTercero(contexto, cbu, true);
			if (cuentaTercero.cuentaCoelsa.hayError()) {
				log.set("error", "ERROR_COELSA");
				return RespuestaMB.estado("ERROR_COELSA");
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

			if (!esTitular && ConfigMB.esOpenShift()) {
				log.set("error", "NO_ES_TITULAR");
				return RespuestaMB.estado("NO_ES_TITULAR");
			}

			// Alta Cuenta Vendedor
			try {
				Boolean esCuentaVendedor = RestDebin.cuentaActivaVendedor(contexto, cuenta);
				if (!esCuentaVendedor) {
					ApiResponseMB response = RestDebin.activarCuentaVendedor(contexto, cuenta);
					if (response.hayError()) {
						log.set("error", "ERROR_ACTIVANDO_CUENTA_VENDEDOR");
						return RespuestaMB.estado("ERROR_ACTIVANDO_CUENTA_VENDEDOR");
					}
				}
			} catch (ApiExceptionMB ex) {
				log.set("error", "VENDEDOR_NO_ENCONTRADO");
				return RespuestaMB.estado("VENDEDOR_NO_ENCONTRADO");
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
				ApiResponseMB response = RestDebin.crearPrestacion(contexto, prestacion);
				if (response.hayError()) {
					log.set("error", "ERROR_CREANDO_PRESTACION");
					return RespuestaMB.estado("ERROR_CREANDO_PRESTACION");
				}
			}

			// Alta Recurrencia
			ApiResponseMB responseAlta = RestDebin.crearRecurrencia(contexto, cuenta, cuentaTercero, prestacion);
			if (responseAlta.hayError()) {
				log.set("error", "ERROR_CREANDO_RECURRENCIA");
				return RespuestaMB.estado("ERROR_CREANDO_RECURRENCIA");
			}
			
			String cbuTercero = cuentaTercero.cbu();
			ApiRequestMB request = ApiMB.request("CrearRecurrencia", "agendas", "POST", "/v1/recurrencia", contexto);
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

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis());
			if (response.hayError() || !response.string("codigo").isEmpty()) {
				if (response.string("codigo").equals("23921") || response.string("codigo").equals("23823")) {
					log.set("error", "RECURRENCIA_EXISTENTE");
					return RespuestaMB.estado("RECURRENCIA_EXISTENTE");
				}
				return RespuestaMB.error();
			}
			
			boolean estaAprobada = "5000".equals(responseAlta.objeto("respuesta").string("codigo"));
			if(estaAprobada) {
				ApiRequestMB requestAviso = ApiMB.request("AvisoRecurrencia", "debin", "POST", "/v1/coelsa/AvisoRecurrencia", contexto);
				Objeto bodyAviso = new Objeto();
				bodyAviso.set("id", responseAlta.string("id"));
				bodyAviso.set("comprador").set("cuit", contexto.persona().cuit()).set("cbu", cbuTercero);
				bodyAviso.set("debin").set("prestacion", prestacion);
				bodyAviso.set("estado").set("codigo", responseAlta.objeto("respuesta").string("codigo")).set("descripcion", responseAlta.objeto("respuesta").string("descripcion"));
				requestAviso.body(bodyAviso);

				ApiResponseMB responseAviso = ApiMB.response(requestAviso, contexto.idCobis());
				if (responseAviso.hayError()) {
					return RespuestaMB.error();
				}
			}

			ApiMB.eliminarCache(contexto, "Recurrencias", contexto.idCobis());
			RespuestaMB respuesta = new RespuestaMB();
			log.set("estado", "OK");
			
			if(estaAprobada) {
				respuesta.set("estado", "APROBADO");
			}

			return respuesta;
		} finally {
			insertSueldoDuho(contexto, log);
		}
	}

	public static void insertSueldoDuho(ContextoMB contexto, Objeto objeto) {
		try {
			SqlRequestMB request = SqlMB.request("logsSoftTokenAlta", "hbs");
			request.sql = """
					INSERT INTO [hbs].[dbo].[auditor_sueldo_duho] ([momento],[cobis],[canal],[cbu],[cuentaBH],[monto],[diaInicial],[diaFinal],[idRecurrencia],[accion],[estado],[error])
					VALUES (?,?,?,?,?,?,?,?,?,?)
					""";
			request.add(new Date());
			request.add(contexto.idCobis());
			request.add("MB");
			request.add(objeto.string("cbu"));
			request.add(objeto.string("cuentaBH"));
			request.add(objeto.string("monto"));
			request.add(objeto.string("diaInicial"));
			request.add(objeto.string("diaFinal"));
			request.add(objeto.string("idRecurrencia"));
			request.add(objeto.string("accion"));
			request.add(objeto.string("estado"));
			request.add(objeto.string("error"));
			SqlMB.response(request);
		} catch (Throwable t) {
		}
	}

	public static void guardarDesicionUsuario(ContextoMB contexto, Boolean acepto) {
		try {

			SqlRequestMB sqlRequest = SqlMB.request("InsertOrUpdateDecicionUsuario", "hbs");
			sqlRequest.sql = "UPDATE [Hbs].[dbo].[decicionUsuario_cobis] ";
			sqlRequest.sql += "SET [fecha_modificacion] = ?, [acepto] = ? ";
			sqlRequest.sql += "WHERE [id_cobis] = ? ";
			sqlRequest.add(new Date());
			sqlRequest.add(acepto ? "1" : "0");
			sqlRequest.add(contexto.idCobis());

			sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
			sqlRequest.sql += "INSERT INTO [Hbs].[dbo].[decicionUsuario_cobis] ([id_cobis], [fecha_modificacion], [acepto], [canal]) ";
			sqlRequest.sql += "VALUES (?, ?, ?, ?) ";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(new Date());
			sqlRequest.add(acepto ? "1" : "0");
			sqlRequest.add("MB");

			SqlMB.response(sqlRequest);
		} catch (Throwable t) {
		}
	}

	public static Boolean leerDesicionUsuario(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectDecicionUsuario", "hbs");
		sqlRequest.sql = "SELECT * FROM [Hbs].[dbo].[decicionUsuario_cobis] WHERE id_cobis = ?";
		sqlRequest.add(contexto.idCobis());
		SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
		Integer valor = null;
		for (Objeto registro : sqlResponse.registros) {
			valor = registro.integer("acepto");
		}
		return (valor != null && valor == 1);
	}

	public static RespuestaMB modificacion(ContextoMB contexto) {
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
				return RespuestaMB.parametrosIncorrectos();
			}

			BigDecimal montoMinimo = montoMinimoV2(contexto);
			if (monto.compareTo(montoMinimo) < 0) {
				log.set("error", "MONTO_MINIMO_NO_SUPERADO");
				return RespuestaMB.estado("MONTO_MINIMO_NO_SUPERADO").set("mensaje", "El monto mínimo deberá ser de $ " + Formateador.importe(montoMinimo.toString()));
			}

			ApiRequestMB request = ApiMB.request("ModificarRecurrencia", "agendas", "PATCH", "/v1/recurrencia", contexto);
			Objeto datos = new Objeto();
			datos.set("cliente").set("cliente", contexto.idCobis());
			datos.set("diaInicial", diaInicial);
			datos.set("diaFinal", diaFinal);
			datos.set("idRecurrencia", idRecurrencia);
			datos.set("tipo", "D");
			datos.set("valor", monto);
			request.body(datos);

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), idRecurrencia);
			if (response.hayError() || !response.string("codigo").isEmpty()) {
				return RespuestaMB.error();
			}

			ApiMB.eliminarCache(contexto, "Recurrencias", contexto.idCobis());
			log.set("estado", "OK");
			return RespuestaMB.exito();
		} finally {
			insertSueldoDuho(contexto, log);
			guardarDesicionUsuario(contexto, aumentamosPorVos);
		}
	}

	public static RespuestaMB baja(ContextoMB contexto) {
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
				return RespuestaMB.parametrosIncorrectos();
			}

			ApiRequestMB request = ApiMB.request("EliminarRecurrencia", "agendas", "DELETE", "/v1/recurrencia/{idCobis}/{idRecurrencia}", contexto);
			request.path("idCobis", contexto.idCobis());
			request.path("idRecurrencia", idRecurrencia);

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), idRecurrencia);
			if (response.hayError() || !response.string("codigo").isEmpty()) {
				return RespuestaMB.error();
			}

			ApiMB.eliminarCache(contexto, "Recurrencias", contexto.idCobis());
			log.set("estado", "OK");
			return RespuestaMB.exito();
		} finally {
			insertSueldoDuho(contexto, log);
		}
	}

	public static Object terminosCondiciones(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("FormulariosGet", "formularios_windows", "GET", "/api/FormularioImpresion/canales", contexto);
		request.query("solicitudid", "0");
		request.query("grupocodigo", "SUELDODUO");
		request.query("canal", "MB");
		request.header("x-cuil", contexto.persona().cuit());

		try {
			request.header("x-apellidoNombre", URLEncoder.encode(contexto.persona().apellidos() + " " + contexto.persona().nombres(), "UTF-8"));
		} catch (Exception e) {
			request.header("x-apellidoNombre", contexto.persona().apellidos() + " " + contexto.persona().nombres());
		}

		request.header("x-tipoPersona", "F");
		request.header("x-dni", contexto.persona().numeroDocumento());
		request.header("x-producto", "SueldoDUO");

		ApiResponseMB response = ApiMB.response(request, "0", "SUELDODUO", contexto.idCobis());
		if (response.hayError()) {
			contexto.setHeader("estado", "ERROR");
			return RespuestaMB.error();
		}

		String base64 = response.string("Data");
		try {
			byte[] archivo = Base64.getDecoder().decode(base64);
			contexto.setHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + "SueldoDuo" + ".pdf");
			contexto.setHeader("estado", "0");

			return archivo;
		} catch (Exception e) {
			contexto.setHeader("estado", "ERROR");

			return RespuestaMB.error();
		}
	}

	public static RespuestaMB consultaAumentoMontoMinimo(ContextoMB contexto) {
		RespuestaMB respuestaConsulta = consulta(contexto);

		if (respuestaConsulta.string("estado").equals("ERROR")) {
			return RespuestaMB.error();
		}

		RespuestaMB respuesta = new RespuestaMB();
		int minimo = respuestaConsulta.integer("montoMinimo");
		boolean mostrarPopup = false;
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
			RespuestaMB respRegistros = consultaCantidadVecesMostradas(contexto, "AUMENTOMIN");
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
	private static BigDecimal montoMinimo(ContextoMB contexto) {
		return contexto.tienePaquete() ? ConfigMB.bigDecimal("monto_minimo_plan_sueldo", "15000") : ConfigMB.bigDecimal("monto_minimo_plan_sueldo_sin_paquete", "15000");
	}

	private static BigDecimal montoMinimoV2(ContextoMB contexto) {
		String codigoPaquete = "";
		ApiResponseMB response = ProductosService.productos(contexto, false);
		BigDecimal montoMinimo = null;

		if (contexto.tienePaquete()) {
			for (Objeto item : response.objetos("productos")) {
				if ("PAQ".equals(item.string("tipo"))) {
					codigoPaquete = item.string("codigoPaquete");
					switch (codigoPaquete) {
					case "39":
						montoMinimo = ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_facil_pack", "45000");
						break;
					case "40":
						montoMinimo = ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_buho_pack", "65000");
						break;
					case "41":
						montoMinimo = ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_gold_pack", "110000");
						break;
					case "42":
						montoMinimo = ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_platinum_pack", "200000");
						break;
					case "43":
						montoMinimo = ConfigMB.bigDecimal("meta_bonificacion_plan_sueldo_black_pack", "300000");
						break;
					default:
						montoMinimo = ConfigMB.bigDecimal("monto_minimo_plan_sueldo", "45000");
					}
					break;
				}

			}
		} else {
			montoMinimo = ConfigMB.bigDecimal("monto_minimo_plan_sueldo_sin_paquete", "45000");
		}
		return montoMinimo;
	}

	private static void incrementarCantidadVecesMostradas(ContextoMB contexto, String nemonico) {
		try {
			SqlRequestMB sqlRequest = SqlMB.request("InsertContador", "homebanking");
			sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'HB')";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			SqlMB.response(sqlRequest);
		} catch (Exception e) {

		}
	}

	private static RespuestaMB consultaCantidadVecesMostradas(ContextoMB contexto, String nemonico) {
		try {
			RespuestaMB respuesta = new RespuestaMB();

			SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");
			sqlRequest.sql = "SELECT TOP 1 * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ? order by momento desc";
			sqlRequest.add(contexto.idCobis());
			sqlRequest.add(nemonico);
			SqlResponseMB sqlResponse = SqlMB.response(sqlRequest);
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
