package ar.com.hipotecario.canal.homebanking.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.UnauthorizedException;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.servicio.CuentasService;

public class HBLogs {

	public static String logs(ContextoHB contexto) {
		UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
		String idCobis = contexto.parametros.string("buscar");

		if (idCobis.isEmpty()) {
			return "";
		}

		// EJECUTO LAS QUERYS EN PARALELO
		List<Futuro<List<Objeto>>> futuros = new ArrayList<>();
		futuros.add(new Futuro<>(() -> logsLogin(idCobis)));
		futuros.add(new Futuro<>(() -> logsCambioClave(idCobis)));
		futuros.add(new Futuro<>(() -> logsCambioUsuario(idCobis)));
		futuros.add(new Futuro<>(() -> logsPedidoOtp(idCobis)));
		futuros.add(new Futuro<>(() -> logsValidacionOtp(idCobis)));
		futuros.add(new Futuro<>(() -> logsPrestamosPersonales(idCobis)));
		futuros.add(new Futuro<>(() -> logsCambioCelular(idCobis)));
		futuros.add(new Futuro<>(() -> logsCambioMail(idCobis)));
		futuros.add(new Futuro<>(() -> logsTransferencia(idCobis)));
		futuros.add(new Futuro<>(() -> logsDebin(idCobis)));
		futuros.add(new Futuro<>(() -> logsOde(idCobis)));
		futuros.add(new Futuro<>(() -> logsBeneficiarios(idCobis)));
		futuros.add(new Futuro<>(() -> logsBiometria(idCobis)));
		futuros.add(new Futuro<>(() -> logsTransferenciaModo(idCobis)));
		futuros.add(new Futuro<>(() -> logsPagoQR(idCobis)));
		futuros.add(new Futuro<>(() -> logsRegistroDispositivo(idCobis)));
		futuros.add(new Futuro<>(() -> logsSoftTokenAlta(idCobis)));
		futuros.add(new Futuro<>(() -> logsAuditorServicio(idCobis)));
		futuros.add(new Futuro<>(() -> logsScanDni(contexto, idCobis)));
		futuros.add(new Futuro<>(() -> logsSoftToken(idCobis)));
		futuros.add(new Futuro<>(() -> logsSueldoDuho(idCobis)));
		futuros.add(new Futuro<>(() -> logsDebinProgramado(idCobis)));

		// ESPERO A QUE LAS QUERYS TERMINEN
		List<Objeto> empty = new ArrayList<>();
		List<Objeto> items = new ArrayList<>();
		for (Futuro<List<Objeto>> futuro : futuros) {
			items.addAll(futuro.tryGet(empty));
		}

		// ORDENO DEL MAS RECIENTE AL MAS ANTIGUO
		Collections.sort(items, (item1, item2) -> {
			return item2.date("momento").compareTo(item1.date("momento"));
		});

		// BENEFICIAROS TRANSFERENCIA
		List<Objeto> beneficiarosTransferencia = new Futuro<>(() -> beneficiarosTransferencia(idCobis)).tryGet(empty);

		// GENERO EL HTML
		String fechaAnterior = "";
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='font-family: calibri; font-size: 15px'>");
		for (Objeto item : items) {
			String fechaActual = item.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy");
			if (!fechaAnterior.equals(fechaActual)) {
				fechaAnterior = fechaActual;
				htmlHeader(sb, item, items, fechaActual);
			}
			htmlComun(sb, item);
			htmlRiesgoNet(sb, item);
			htmlOTP(sb, item);
			htmlPrestamo(sb, item);
			htmlCambioCelular(sb, item);
			htmlCambioEmail(sb, item);
			htmlTransferencia(sb, item, beneficiarosTransferencia);
			htmlDebin(sb, item);
			htmlODE(sb, item);
			htmlBeneficiario(sb, item);
			htmlBiometria(sb, item);
			htmlTransferenciaModo(sb, item);
			htmlPagoQR(sb, item);
			htmlRegistroDispositivo(sb, item);
			htmlSoftokenAlta(sb, item);
			htmlScanDni(sb, item);
			htmlSoftToken(sb, item);
			htmlSueldoDuho(sb, item);
			htmlDebinProgramado(sb, item);
			sb.append("<br/>");
		}
		sb.append("</div>");
		contexto.responseHeader("Content-Type", "text/html");
		return sb.toString();
	}

	/* ========== BENEFICIARIOS TRANSFERENCIA ========== */
	private static List<Objeto> beneficiarosTransferencia(String idCobis) {
		SqlRequest request = Sql.request("beneficiarosTransferencia", "homebanking");
		request.sql = "SELECT * FROM [Hbs].[dbo].[agenda_transferencias] WHERE nro_cliente = ?";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	/* ========== QUERYS ========== */
	private static List<Objeto> logsLogin(String idCobis) {
		SqlRequest request = Sql.request("logsLogin", "homebanking");
		request.sql = """
				SELECT 'LOGIN' tipo, canal, momento, direccionIp
				FROM [homebanking].[dbo].[logs_login]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsCambioClave(String idCobis) {
		SqlRequest request = Sql.request("logsCambioClave", "homebanking");
		request.sql = """
				SELECT 'CAMBIO_CLAVE' tipo, canal, momento, direccionIp
				FROM [homebanking].[dbo].[logs_cambio_clave]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsCambioUsuario(String idCobis) {
		SqlRequest request = Sql.request("logsCambioUsuario", "homebanking");
		request.sql = """
				SELECT 'CAMBIO_USUARIO' tipo, canal, momento, direccionIp
				FROM [homebanking].[dbo].[logs_cambio_usuario]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsPedidoOtp(String idCobis) {
		SqlRequest request = Sql.request("logsPedidoOtp", "homebanking");
		request.sql = """
				SELECT 'PEDIDO_OTP' tipo, canal, momento, direccionIp, celular, email, riesgoNet, link, estado
				FROM [homebanking].[dbo].[logs_envios_otp]
				WHERE idCobis = ? AND estado = 'P'""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsValidacionOtp(String idCobis) {
		SqlRequest request = Sql.request("logsValidacionOtp", "homebanking");
		request.sql = """
				SELECT 'VALIDACION_OTP' tipo, canal, momento, direccionIp, celular, email, riesgoNet, link, estado
				FROM [homebanking].[dbo].[logs_envios_otp]
				WHERE idCobis = ? AND estado != 'P'""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsPrestamosPersonales(String idCobis) {
		SqlRequest request = Sql.request("logsPrestamosPersonales", "homebanking");
		request.sql = """
				SELECT 'PRESTAMO_PERSONAL' tipo, canal, momento, direccionIp, importe, plazo, cuenta
				FROM [homebanking].[dbo].[logs_prestamos_personales]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsCambioCelular(String idCobis) {
		SqlRequest request = Sql.request("logsCambioCelular", "homebanking");
		request.sql = """
				SELECT 'CAMBIO_CELULAR' AS tipo, canal, momento, direccionIp, celularAnterior, celularNuevo
				FROM [homebanking].[dbo].[logs_cambio_celular]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsCambioMail(String idCobis) {
		SqlRequest request = Sql.request("logsCambioMail", "homebanking");
		request.sql = """
				SELECT 'CAMBIO_MAIL' AS tipo, canal, momento, direccionIp, mailAnterior, mailNuevo
				FROM [homebanking].[dbo].[logs_cambio_mail]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsTransferencia(String idCobis) {
		SqlRequest request = Sql.request("logsTransferencia", "hbs");
		request.sql = """
				SELECT 'TRANSFERENCIA' AS tipo, canal, momento, ip AS direccionIp, importe, tipo AS tipoTransferencia, cuentaOrigen, cuentaDestino, moneda, concepto, cuentaPropia, servicioDomestico, especial, tarjetaDebito, transaccion, codigoError, descripcionError AS descripcionTransferenciaError
				FROM [hbs].[dbo].[auditor_transferencia]
				WHERE cobis = ? AND tipo NOT IN ('nuevo_debin', 'acepta_debin', 'acepta_debin_programado')""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsDebin(String idCobis) {
		SqlRequest request = Sql.request("logsDebin", "hbs");
		request.sql = """
				SELECT 'DEBIN' AS tipo, canal, momento, ip AS direccionIp, importe, tipo AS tipoTransferencia, cuentaOrigen, cuentaDestino, moneda, concepto, cuentaPropia, servicioDomestico, especial, tarjetaDebito, transaccion, codigoError, descripcionError AS descripcionTransferenciaError
				FROM [hbs].[dbo].[auditor_transferencia]
				WHERE cobis = ? AND tipo IN ('acepta_debin')""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsDebinProgramado(String idCobis) {
		SqlRequest request = Sql.request("logsDebin", "hbs");
		request.sql = """
				SELECT 'DEBIN_PROGRAMADO' AS tipo, canal, momento, ip AS direccionIp, importe, tipo AS tipoTransferencia, cuentaOrigen, cuentaDestino, moneda, concepto, cuentaPropia, servicioDomestico, especial, tarjetaDebito, transaccion, codigoError, descripcionError AS descripcionTransferenciaError
				FROM [hbs].[dbo].[auditor_transferencia]
				WHERE cobis = ? AND tipo IN ('acepta_debin_programado')""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsOde(String idCobis) {
		SqlRequest request = Sql.request("logsOde", "hbs");
		request.sql = """
				SELECT 'ODE' AS tipo, canal, momento, direccionIp, importe, tipoCuenta AS cuenta, codigoError, descripcionError AS descripcionTransferenciaError, cuentaPBF AS 'cuenta_beneficiario', numeroDocumento AS 'documento_beneficiario', referencia AS 'nombre_beneficiario'
				FROM [hbs].[dbo].[auditor_ode]
				WHERE cobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsBeneficiarios(String idCobis) {
		SqlRequest request = Sql.request("logsBeneficiarios", "homebanking");
		request.sql = """
				SELECT 'BENEFICIARIO' AS tipo, canal, momento, direccionIp, cbu AS 'cbu_beneficiario', cuenta AS 'cuenta_beneficiario', documento AS 'documento_beneficiario', nombre AS 'nombre_beneficiario', accion AS 'accion_beneficiario'
				FROM [homebanking].[dbo].[logs_beneficiarios]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsBiometria(String idCobis) {
		SqlRequest request = Sql.request("logsBiometria", "homebanking");
		request.sql = """
				SELECT 'BIOMETRIA' AS tipo, canal, fecha AS 'momento', metodo, tipo_metodo, acceso, id_dispositivo, refres_token, estado_acceso, direccionIp
				FROM [homebanking].[dbo].[logs_biometria]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsTransferenciaModo(String idCobis) {
		SqlRequest request = Sql.request("logsTransferenciaModo", "mobile");
		request.sql = """
				SELECT 'TRANSFERENCIA_MODO' AS tipo, 'MB' AS canal, Created AS momento, Recipent_Phone_Number AS celular, Amount AS importe, Transfer_Originator AS tipoTransferencia, Id_Account AS cuentaOrigen, Recipient_CBU AS cuentaDestino, Currency_Code AS moneda, Reason_Code AS concepto, Modo_Transfer_Id AS transaccion, Recipient_CBU AS 'cbu_beneficiario', Recipent_Name AS 'nombre_beneficiario', Id_Transfer AS idProceso
				FROM [Mobile].[dbo].[MODO_Transfers]
				WHERE Id_Cobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsPagoQR(String idCobis) {
		SqlRequest request = Sql.request("logsPagoQR", "mobile");
		request.sql = """
				SELECT 'PAGO-QR' AS tipo, 'MB' AS canal, created AS momento, [status] AS estado, transaction_amount AS importe,
				installments AS plazo, tip_type AS tipoTransferencia, card_id AS cuentaOrigen,
				transaction_currency AS moneda, bank_payment_id AS transaccion, merchant_name AS nombre_beneficiario
				FROM [Mobile].[dbo].[MODO_Payments]
				WHERE id_cobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsRegistroDispositivo(String idCobis) {
		SqlRequest request = Sql.request("logsRegistroDispositivo", "mobile");
		request.sql = """
				SELECT 'REGISTRO_DISPOSITIVO' AS tipo, 'MB' AS canal, fecha_alta AS momento, [alias] + ' - ' + [id_dispositivo] AS id_dispositivo, [direccion_ip] AS direccionIp
				FROM [Mobile].[dbo].[registro_dispositivo]
				WHERE id_cobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsSoftTokenAlta(String idCobis) {
		SqlRequest request = Sql.request("logsSoftTokenAlta", "mobile");
		request.sql = """
				SELECT 'SOFT_TOKEN_ALTA' AS tipo, 'MB' AS canal, fecha_alta AS momento, [id_tarjeta_debito] AS tarjetaDebito, [id_dispositivo] AS id_dispositivo
				FROM [Mobile].[dbo].[soft_token_alta]
				WHERE id_cobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsAuditorServicio(String idCobis) {
		SqlRequest request = Sql.request("logsAuditorServicio", "hbs");
		request.sql = """
				SELECT nombreServicio AS tipo, canal, momento, email
				FROM [hbs].[dbo].[auditor_servicio]
				WHERE idCobis = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsScanDni(ContextoHB contexto, String idCobis) {
		ApiResponse cliente = cliente(contexto, idCobis);
		SqlRequest request = Sql.request("logsSacanDni", "mobile");
		request.sql = """
				SELECT 'SCAN_DNI' AS tipo, 'MB' AS canal, fecha_alta AS momento, cuil, id_tramite, ejemplar, fecha_emision, vencimiento, nombre, apellido, fecha_nacimiento, id_dispositivo, id_estado, validado_sucursal
				FROM [mobile].[dbo].[usuario_renaper]
				WHERE cuil = ?""";
		request.add(cliente.objetos().get(0).string("cuit"));
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsSoftToken(String idCobis) {
		SqlRequest request = Sql.request("logsAuditorServicio", "hbs");
		request.sql = """
				SELECT 'SOFT_TOKEN' AS tipo, canal, momento, estado, error
				FROM [hbs].[dbo].[auditor_soft_token]
				WHERE [cobis] = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	private static List<Objeto> logsSueldoDuho(String idCobis) {
		SqlRequest request = Sql.request("logsSueldoDuho", "hbs");
		request.sql = """
				SELECT 'SUELDO_DUHO' AS tipo, canal, momento, [canal], [cbu], [cuentaBH], [monto], [diaInicial], [diaFinal], [idRecurrencia], [accion], [estado], [error]
				FROM [hbs].[dbo].[auditor_sueldo_duho]
				WHERE [cobis] = ?""";
		request.add(idCobis);
		SqlResponse response = Sql.response(request);
		return response.registros;
	}

	/* ========== HTML ========== */
	private static void htmlHeader(StringBuilder sb, Objeto item, List<Objeto> items, String fecha) {
		List<Objeto> registros = items.stream().filter(itemConteo -> itemConteo.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy").equals(fecha)).filter(itemConteo -> "VALIDACION_OTP".equals(itemConteo.string("tipo")) || "PEDIDO_OTP".equals(itemConteo.string("tipo"))).collect(Collectors.toList());
		List<Objeto> riesgoNetFallido = registros.stream().filter(registro -> registro.integer("riesgoNet", 0) == 1 && registro.string("estado").equals("R")).collect(Collectors.toList());
		List<Objeto> riesgoNetCorrecto = registros.stream().filter(registro -> registro.integer("riesgoNet", 0) == 1 && registro.string("estado").equals("A")).collect(Collectors.toList());
		sb.append("<h2> ").append(fecha).append(" </h2>");
		sb.append("<h3> INTENTOS RIESGO NET: ");
		sb.append("fallidos: ").append(riesgoNetFallido.size()).append(" - ");
		sb.append("aceptados: ").append(riesgoNetCorrecto.size()).append(" - ");
		sb.append("TOTAL: ").append(riesgoNetFallido.size() + riesgoNetCorrecto.size()).append(" </h3> <hr/>");
	}

	private static void htmlComun(StringBuilder sb, Objeto item) {
		sb.append("<b>Momento: </b>").append(item.date("momento", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss")).append("<br/>");
		if ("VALIDACION_OTP".equals(item.string("tipo")) && item.integer("riesgoNet", 0) == 1) {
			sb.append("<b>Tipo: </b>").append("VALIDACION_RIESGONET").append("<br/>");
		} else if ("PEDIDO_OTP".equals(item.string("tipo")) && item.integer("riesgoNet", 0) == 1) {
			sb.append("<b>Tipo: </b>").append("PEDIDO_RIESGO_NET").append("<br/>");
		} else {
			sb.append("<b>Tipo: </b>").append(item.string("tipo")).append("<br/>");
		}
		sb.append("<b>Canal: </b>").append(item.string("canal")).append("<br/>");
		sb.append("<b>DireccionIp: </b>").append(item.string("direccionIp")).append("<br/>");
	}

	private static void htmlOTP(StringBuilder sb, Objeto item) {
		if ("PEDIDO_OTP".equals(item.string("tipo")) || "VALIDACION_OTP".equals(item.string("tipo"))) {
			if (!"".equals(item.string("celular"))) {
				sb.append("<b>Medio: </b>").append("CELULAR").append("<br/>");
				sb.append("<b>Celular: </b>").append(item.string("celular")).append("<br/>");
			}
			if (!"".equals(item.string("email"))) {
				sb.append("<b>Medio: </b>").append("EMAIL").append("<br/>");
				sb.append("<b>Email: </b>").append(item.string("email")).append("<br/>");
			}
			if (item.integer("riesgoNet") != null && item.integer("riesgoNet") == 1) {
				sb.append("<b>Medio: </b>").append("RIESGO NET").append("<br/>");
			}
			if (item.integer("link") != null && item.integer("link") == 1) {
				sb.append("<b>Medio: </b>").append("LINK").append("<br/>");
			}
			sb = estado(sb, item.string("estado"));
		}
	}

	private static void htmlPrestamo(StringBuilder sb, Objeto item) {
		if ("PRESTAMO_PERSONAL".equals(item.string("tipo"))) {
			sb.append("<b>Cuenta: </b>").append(item.string("cuenta")).append("<br/>");
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Plazo: </b>").append(item.string("plazo")).append("<br/>");
		}
	}

	private static void htmlTransferencia(StringBuilder sb, Objeto item, List<Objeto> beneficiarosTransferencia) {
		if ("TRANSFERENCIA".equals(item.string("tipo"))) {
			String nombreBeneficiario = "DESCONOCIDO";
			if (!"".equals(item.string("cuentaDestino"))) {
				for (Objeto registroBeneficiario : beneficiarosTransferencia) {
					if (item.string("cuentaDestino").equals(registroBeneficiario.string("cbu_destino")) || item.string("cuentaDestino").equals(registroBeneficiario.string("nro_cuenta_destino"))) {
						nombreBeneficiario = registroBeneficiario.string("titular", "DESCONOCIDO");
					}
				}
			}
			sb.append("<b>Tipo de transferencia: </b>").append(item.string("tipoTransferencia")).append("<br/>");
			sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
			if (CuentasService.esCbu(item.string("cuentaDestino"))) {
				sb.append("<b>Cbu Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
			} else if (CuentasService.esCvu(item.string("cuentaDestino"))) {
				sb.append("<b>Cvu Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
			} else {
				sb.append("<b>Cuenta Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
			}
			sb.append("<b>Titular: </b>").append(nombreBeneficiario).append("<br/>");
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Moneda: </b>").append("80".equals(item.string("moneda")) ? "$" : ("2".equals(item.string("moneda")) ? "USD" : "")).append("<br/>");
			sb.append("<b>Concepto: </b>").append(item.string("concepto")).append("<br/>");
			sb.append("<b>Servicio Domestico: </b>").append(item.string("servicioDomestico")).append("<br/>");
			sb.append("<b>Especial: </b>").append(item.string("especial")).append("<br/>");
			resultado(item, sb);
		}
	}

	private static void htmlDebin(StringBuilder sb, Objeto item) {
		if ("DEBIN".equals(item.string("tipo"))) {
			sb.append("<b>Tipo de debin: </b>").append(item.string("tipoTransferencia")).append("<br/>");
			sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
			sb.append("<b>Cuenta Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Moneda: </b>").append("80".equals(item.string("moneda")) ? "$" : ("2".equals(item.string("moneda")) ? "USD" : "")).append("<br/>");
			resultado(item, sb);
		}
	}

	private static void htmlCambioCelular(StringBuilder sb, Objeto item) {
		if ("CAMBIO_CELULAR".equals(item.string("tipo"))) {
			sb.append("<b>Celular Anterior: </b>").append(item.string("celularAnterior")).append("<br/>");
			sb.append("<b>Celular Nuevo: </b>").append(item.string("celularNuevo")).append("<br/>");
		}
	}

	private static void htmlCambioEmail(StringBuilder sb, Objeto item) {
		if ("CAMBIO_MAIL".equals(item.string("tipo"))) {
			sb.append("<b>Mail Anterior: </b>").append(item.string("mailAnterior")).append("<br/>");
			sb.append("<b>Mail Nuevo: </b>").append(item.string("mailNuevo")).append("<br/>");
		}
	}

	private static void htmlBeneficiario(StringBuilder sb, Objeto item) {
		if ("BENEFICIARIO".equals(item.string("tipo"))) {
			if (!item.string("cbu_beneficiario").equals("")) {
				sb.append("<b>Cbu: </b>").append(item.string("cbu_beneficiario")).append("<br/>");
			}
			if (!item.string("cuenta_beneficiario").equals("")) {
				sb.append("<b>Cuenta: </b>").append(item.string("cuenta_beneficiario")).append("<br/>");
			}
			if (!item.string("documento_beneficiario").equals("")) {
				sb.append("<b>Documento: </b>").append(item.string("documento_beneficiario")).append("<br/>");
			}
			if (!item.string("nombre_beneficiario").equals("")) {
				sb.append("<b>Nombre: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
			}
			sb.append("<b>Acci&oacute;n: </b>").append(item.string("accion_beneficiario").equals("A") ? "ALTA" : item.string("accion_beneficiario").equals("B") ? "BAJA" : "").append("<br/>");
		}
	}

	private static void htmlODE(StringBuilder sb, Objeto item) {
		if ("ODE".equals(item.string("tipo"))) {
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Referencia: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
			if (!item.string("documento_beneficiario").equals("")) {
				sb.append("<b>Documento beneficiario: </b>").append(item.string("documento_beneficiario")).append("<br/>");
			}
			sb.append("<b>Cuenta: </b>").append(item.string("cuenta_beneficiario")).append("<br/>");
			resultado(item, sb);
		}
	}

	private static void htmlRiesgoNet(StringBuilder sb, Objeto item) {
		if ("PreguntasRiesgoNet".equals(item.string("tipo"))) {
			sb.append("<b> Id cobis: </b>").append(item.string("idCobis")).append("<br/>");
			sb.append("<b> Id proceso: </b>").append(item.string("idProceso")).append("<br/>");
		}
	}

	private static void htmlBiometria(StringBuilder sb, Objeto item) {
		if ("BIOMETRIA".equals(item.string("tipo"))) {
			sb.append("<b>Metodo de Acceso: </b>").append(item.string("metodo")).append("<br/>");
			sb.append("<b>Tipo de Acceso: </b>").append(item.string("tipo_metodo")).append("<br/>");
			sb.append("<b>Acceso: </b>").append(item.string("acceso")).append("<br/>");
			sb.append("<b>Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
			if (!item.string("refres_token").equals("")) {
				sb.append("<b>Ultimo token ISVA: </b>").append(item.string("refres_token")).append("<br/>");
			}
			sb.append("<b>Estado: </b>").append(item.string("estado_acceso").equals("0") ? "Ok" : item.string("estado_acceso")).append("<br/>");
		}
	}

	private static void htmlTransferenciaModo(StringBuilder sb, Objeto item) {
		if ("TRANSFERENCIA_MODO".equals(item.string("tipo"))) {
			sb.append("<b>Tipo Transferencia: </b>").append(item.string("tipoTransferencia")).append("<br/>");
			sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
			sb.append("<b>Cbu Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
			sb.append("<b>Titular: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
			sb.append("<b>Celular: </b>").append(item.string("celular")).append("<br/>");
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Moneda: </b>").append(item.string("moneda")).append("<br/>");
			sb.append("<b>Concepto: </b>").append(item.string("concepto")).append("<br/>");
			sb.append("<b>ID Transaccion: </b>").append(item.string("idProceso")).append("<br/>");
			sb.append("<b>ID Modo Transaccion: </b>").append(item.string("transaccion")).append("<br/>");
		}
	}

	private static void htmlPagoQR(StringBuilder sb, Objeto item) {
		if ("PAGO-QR".equals(item.string("tipo"))) {
			sb.append("<b>Card ID en MODO: </b>").append(item.string("cuentaOrigen")).append("<br/>");
			sb.append("<b>Transaccion ID en MODO: </b>").append(item.string("transaccion")).append("<br/>");
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Moneda: </b>").append(item.string("moneda")).append("<br/>");
			sb.append("<b>Cuotas: </b>").append(item.string("plazo")).append("<br/>");
			sb.append("<b>Comercio: </b>").append(item.string("nombre_beneficiario")).append("<br/>");
		}
	}

	private static void htmlRegistroDispositivo(StringBuilder sb, Objeto item) {
		if ("REGISTRO_DISPOSITIVO".equals(item.string("tipo"))) {
			sb.append("<b>Fecha Alta: </b>").append(item.string("momento")).append("<br/>");
			sb.append("<b>Alias y Id Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
		}
	}

	private static void htmlSoftokenAlta(StringBuilder sb, Objeto item) {
		if ("SOFT_TOKEN_ALTA".equals(item.string("tipo"))) {
			sb.append("<b>Fecha Alta: </b>").append(item.string("momento")).append("<br/>");
			sb.append("<b>Id Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
			sb.append("<b>Id Tarjeta Débito: </b>").append(item.string("tarjetaDebito")).append("<br/>");
		}
	}

	private static void htmlScanDni(StringBuilder sb, Objeto item) {
		if ("SCAN_DNI".equals(item.string("tipo"))) {
			sb.append("<b>Cuit: </b>").append(item.string("cuil")).append("<br/>");
			sb.append("<b>Tramite: </b>").append(item.string("id_tramite")).append("<br/>");
			sb.append("<b>Ejemplar: </b>").append(item.string("ejemplar")).append("<br/>");
			sb.append("<b>Emision: </b>").append(item.date("fecha_emision", "dd/MM/yyyy")).append("<br/>");
			sb.append("<b>Vencimiento: </b>").append(item.date("vencimiento", "dd/MM/yyyy")).append("<br/>");
			sb.append("<b>Nombre: </b>").append(item.string("nombre")).append("<br/>");
			sb.append("<b>Apellido: </b>").append(item.string("apellido")).append("<br/>");
			sb.append("<b>Fecha Nacimiento: </b>").append(item.date("fecha_nacimiento", "dd/MM/yyyy")).append("<br/>");
			sb.append("<b>Id Dispositivo: </b>").append(item.string("id_dispositivo")).append("<br/>");
			sb.append("<b>Id Estado: </b>").append(item.string("id_estado")).append("<br/>");
			sb.append("<b>Validado Sucursal: </b>").append(item.bool("validado_sucursal")).append("<br/>");
		}
	}

	private static void htmlSoftToken(StringBuilder sb, Objeto item) {
		if ("SOFT_TOKEN".equals(item.string("tipo"))) {
			sb.append("<b>Estado: </b>").append(item.string("estado")).append("<br/>");
			sb.append("<b>Error: </b>").append(item.string("error")).append("<br/>");
		}
	}

	private static void htmlSueldoDuho(StringBuilder sb, Objeto item) {
		if ("SUELDO_DUHO".equals(item.string("tipo"))) {
			sb.append("<b>CBU: </b>").append(item.string("cbu")).append("<br/>");
			sb.append("<b>Cuenta BH: </b>").append(item.string("cuentaBH")).append("<br/>");
			sb.append("<b>Monto: </b>").append(item.string("monto")).append("<br/>");
			sb.append("<b>Dia Inicial: </b>").append(item.string("diaInicial")).append("<br/>");
			sb.append("<b>Dia Final: </b>").append(item.string("diaFinal")).append("<br/>");
			sb.append("<b>ID Recurrencia: </b>").append(item.string("idRecurrencia")).append("<br/>");
			sb.append("<b>Accion: </b>").append(item.string("accion")).append("<br/>");
			sb.append("<b>Estado: </b>").append(item.string("estado")).append("<br/>");
			sb.append("<b>Error: </b>").append(item.string("error")).append("<br/>");
		}
	}

	private static void htmlDebinProgramado(StringBuilder sb, Objeto item) {
		if ("DEBIN_PROGRAMADO".equals(item.string("tipo"))) {
			sb.append("<b>Tipo de debin: </b>").append(item.string("tipoTransferencia")).append("<br/>");
			sb.append("<b>Cuenta Origen: </b>").append(item.string("cuentaOrigen")).append("<br/>");
			sb.append(item.string("cuentaDestino").length() < 12 ? "<b>Cuit Vendedor: </b>":"<b>Cuenta Destino: </b>").append(item.string("cuentaDestino")).append("<br/>");
			sb.append("<b>Importe: </b>").append(Formateador.importe(item.bigDecimal("importe"))).append("<br/>");
			sb.append("<b>Moneda: </b>").append("80".equals(item.string("moneda")) ? "$" : ("2".equals(item.string("moneda")) ? "USD" : "")).append("<br/>");
			resultado(item, sb);
		}
	}

	/* ========== APIS ========== */
	public static ApiResponse cliente(ContextoHB contexto, String idCobis) {
		String idCobisTemp = contexto.sesion.idCobis;
		try {
			contexto.sesion.idCobis = idCobis;
			ApiRequest request = Api.request("Cliente", "personas", "GET", "/clientes/{idCliente}", contexto);
			request.path("idCliente", idCobis);
			request.query("userAgent", contexto.request.userAgent());
			request.permitirSinLogin = true;
			ApiResponse response = Api.response(request, idCobis);
			return response;
		} finally {
			contexto.sesion.idCobis = idCobisTemp;
		}
	}

	/* ========== UTIL ========== */
	private static StringBuilder estado(StringBuilder sb, String estado) {
		String state = "";
		if ("P".equals(estado)) {
			state = "PEDIDO";
		} else if ("A".equals(estado)) {
			state = "ACEPTADO";
		} else if ("R".equals(estado)) {
			state = "RECHAZADO";
		} else if ("E".equals(estado)) {
			state = "ERROR API";
		} else if ("T".equals(estado)) {
			state = "ERROR Api Envio Timeout";
		}
		if (!state.isEmpty()) {
			sb.append("<b>Estado: </b>").append(state).append("<br/>");
		}
		return sb;
	}

	private static void resultado(Objeto item, StringBuilder sb) {
		if ("0".equals(item.string("codigoError"))) {
			sb.append("<b>Resultado: </b>").append("OK <br/>");
		} else {
			sb.append("<b>Resultado: </b>").append("ERROR <br/>");
			sb.append("<b>codigoError: </b>").append(item.string("codigoError")).append("<br/>");
			sb.append("<b>descripcionError: </b>").append(item.string("descripcionTransferenciaError")).append("<br/>");
		}
	}
}
