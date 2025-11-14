package ar.com.hipotecario.canal.homebanking.servicio;

import static java.lang.Integer.parseInt;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;

public class CuentasService {

	public static SqlResponse selectComentarioCuenta(ContextoHB contexto) {
		SqlResponse response = null;
		SqlRequest sqlRequest = Sql.request("SelectComentarioCuenta", "hbs");
		sqlRequest.sql = "SELECT [idCobis], [cuenta], [comentario] FROM [Hbs].[dbo].[comentarios_cuentas] WHERE idCobis = ?";
		sqlRequest.add(contexto.idCobis());
		response = Sql.response(sqlRequest);
		return response;
	}

	/* ========== SERVICIOS ========== */
	public static ApiResponse cuentaBH(ContextoHB contexto, String numero) {
		if (esCajaAhorroBH(numero)) {
			return cajaAhorroBH(contexto, numero);
		}
		if (esCuentaCorrienteBH(numero)) {
			return cuentaCorrienteBH(contexto, numero);
		}
		if (numero != null && numero.startsWith("044")) {
			return cuentaCbuBH(contexto, numero);
		}
		return null;
	}

	public static ApiResponse cajaAhorroBH(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("CajaAhorroBH", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}", contexto);
		request.path("idcuenta", numero);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("validacuentaempleado", "false");
		request.query("historico", "false");
		request.cacheSesion = true;
		return Api.response(request, numero);
	}

	public static ApiResponse cajaAhorroBHsinLogin(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("CajaAhorroBH", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}", contexto);
		request.path("idcuenta", numero);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("validacuentaempleado", "false");
		request.query("historico", "false");
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, numero);
	}

	public static ApiResponse cuentaCorrienteBH(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("CuentaCorrienteBH", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}", contexto);
		request.path("idcuenta", numero);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("validacuentaempleado", "false");
		request.cacheSesion = true;
		return Api.response(request, numero);
	}

	public static ApiResponse cuentaCorrienteBHsinLogin(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("CuentaCorrienteBH", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}", contexto);
		request.path("idcuenta", numero);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("validacuentaempleado", "false");
		request.permitirSinLogin = true;
		request.cacheSesion = true;
		return Api.response(request, numero);
	}

	public static ApiResponse cuentaLink(ContextoHB contexto, String cbu, String idMoneda) {
		if (contexto.tarjetaDebitoPorDefecto() == null) {
			ApiResponse response = new ApiResponse();
			response.codigo = 404;
			return response;
		}
		ApiRequest request = Api.request("CuentaLink", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("cbu", cbu);
		request.query("numerotarjeta", contexto.tarjetaDebitoPorDefecto().numero());
		request.query("idmoneda", idMoneda);
		request.query("consultaalias", "false");
		request.cacheSesion = true;
		// request.cacheBaseDatosNoProductiva = true;
		ApiResponse response = Api.response(request, cbu, idMoneda);
		return response;
	}

	public static ApiResponse cuentaCbuBH(ContextoHB contexto, String cbu) {
		if (contexto.tarjetaDebitoPorDefecto() == null) {
			ApiResponse response = new ApiResponse();
			response.codigo = 404;
			return response;
		}
		ApiRequest request = Api.request("CuentaLink", "cuentas", "GET", "/v1/cuentas", contexto);
		request.query("cbu", cbu);
		request.query("consultaalias", "false");
		request.cacheSesion = true;
		ApiResponse response = Api.response(request, cbu);
		return response;
	}

	public static ApiResponse cuentaCoelsa(ContextoHB contexto, String cbuAlias) {
		ApiRequest request = Api.request("CuentaCoelsa", "cvu", "GET", "/v1/alias", contexto);
		if (esCbu(cbuAlias) || esCvu(cbuAlias)) {
			request.query("cbu", cbuAlias);
		} else if (esAlias(cbuAlias)) {
			request.query("alias", cbuAlias);
		} else {
			return null;
		}
		request.cacheSesion = true;
		ApiResponse response = Api.response(request, cbuAlias);

		if (!response.string("respuesta.numero").equals("0100")){
			return response;
		}

		ApiResponse responseMapeado = new ApiResponse();
		responseMapeado.codigo = response.codigo;
		responseMapeado.set("cbu", response.string("cuenta.nro_cbu"));
		responseMapeado.set("aliasValorOriginal", response.string("alias.valor_original"));
		responseMapeado.set("nombreTitular", response.string("titular.nombre").trim());
		responseMapeado.set("cuit", response.string("titular.cuit"));
		responseMapeado.set("nroBco", response.string("cuenta.nro_bco"));
		responseMapeado.set("ctaActiva", response.bool("cuenta.cta_activa"));
		responseMapeado.set("nuevoAlias", response.string("alias.valor"));
		responseMapeado.set("transaccion", response.string("transac"));
		responseMapeado.set("esTransaccional", !"0".equals(response.string("transac")));
		responseMapeado.set("tipoCuenta", "1".equals(response.string("cuenta.tipo_cta")) ? "CTE" : "AHO");
		responseMapeado.set("tipoMoneda", response.string("cuenta.tipo_cta", null));
		responseMapeado.set("tipoPersona", response.string("titular.tipo_persona"));

		return responseMapeado;
		
		/* ===== RESPONSE DE API CUENTAS =====
		{
			"cbu": "0000001700000000008455",
			"aliasValorOriginal": "PULMON.MITO.FILA",
			"nombreTitular": "PABLO",
			"cuit": "95944913",
			"nroBco": "198",
			"ctaActiva": true,
			"nuevoAlias": "PULMON.MITO.FILA",
			"transaccion": "0",
			"esTransaccional": false,
			"tipoCuenta": "CTE",
			"tipoPersona": "F"
		} */

		/* ===== RESPONSE DE API CVU =====
		{
			"respuesta": {
				"descripcion": "ALIAS ENCONTRADO",
				"numero": "0100"
			},
			"transac": "0",
			"cuenta": {
				"tipo_cta": "1",
				"nro_cbu": "0000001700000000008455",
				"nro_bco": "198",
				"cta_activa": true
			},
			"titular": {
				"tipo_persona": "F",
				"cuit": "95944913",
				"nombre": "PABLO"
			},
			"alias": {
				"valor": "PULMON.MITO.FILA",
				"valor_original": "PULMON.MITO.FILA"
			},
			"titulares": []
		}*/
	}

	public static ApiResponse cuentaCoelsaApiCuenta(ContextoHB contexto, String cbuAlias) {
		ApiRequest request = Api.request("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
		if (esCbu(cbuAlias) || esCvu(cbuAlias)) {
			request.query("cbu", cbuAlias);
		} else if (esAlias(cbuAlias)) {
			request.query("alias", cbuAlias);
		} else {
			return null;
		}
		request.query("acuerdo", "false");
		request.query("consultaalias", "true"); // este parametro indica que va a coelsa con el true;
		request.cacheSesion = true;
		return Api.response(request, cbuAlias);
	}

	public static ApiResponse cajaAhorroBloqueos(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("CuentasCajaAhorroBloqueos", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}/bloqueos", contexto);
		request.path("idcuenta", numero);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		return Api.response(request);
	}

	public static ApiResponse cuentaBloqueos(ContextoHB contexto, String numero, String tipoCuenta) {
		ApiRequest request = Api.request("CuentasBloqueos", "cuentas", "GET", "/v1/cuentas/{id}/bloqueos", contexto);
		request.path("id", numero);
		request.query("tipoCuenta", tipoCuenta);
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), numero);
	}

	public static ApiResponse actualizarMarcaResumen(ContextoHB contexto, Cuenta cuenta, Boolean resumenDigital) {
		ApiRequest request = Api.request("DebitosAdheridos", "cuentas", "PATCH", "/v1/cajasahorros/{idcuenta}/marcasDigitales", contexto);
		request.path("idcuenta", cuenta.numero());
		request.body("Transaccion", "3271");
		request.body("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
		request.body("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
		request.body("idcuenta", cuenta.numero());
		request.body("resumen", (resumenDigital ? "S" : "N"));
		request.header("x-usuariocanal", ConfigHB.string("configuracion_usuario"));
		return Api.response(request);
	}

	public static ApiResponse aplicaCreditoDebito(ContextoHB contexto, String operacion, Cuenta cuenta, BigDecimal importe, String codigoConcepto, String fechaConcertacion) {
		ApiRequest requestAplicaCredito = Api.request("AplicaCredito", "cuentas", "POST", "/v1/cuentas/{idcuenta}/" + operacion, contexto);
		requestAplicaCredito.path("idcuenta", cuenta.numero());
		requestAplicaCredito.body("tipoProducto", cuenta.idTipo());
		requestAplicaCredito.body("cuenta", cuenta.numero());
		requestAplicaCredito.body("idMoneda", parseInt(cuenta.idMoneda()));
		requestAplicaCredito.body("codigoOficina", parseInt(cuenta.sucursal()));
		requestAplicaCredito.body("importe", importe);
		requestAplicaCredito.body("codigoConcepto", codigoConcepto);
		requestAplicaCredito.body("fecha", fechaConcertacion);
		requestAplicaCredito.body("usuario", contexto.idCobis());
		requestAplicaCredito.body("importeBase", importe);
		requestAplicaCredito.body("ivaBasico", 0);
		requestAplicaCredito.body("ivaPercepcion", 0);
		requestAplicaCredito.body("ivaAdicional", 0);
		requestAplicaCredito.body("tasaIVABasico", 0);
		requestAplicaCredito.body("tasaIVAReduccion", 0);
		requestAplicaCredito.body("tasaIVAPercepcion", 0);
		requestAplicaCredito.body("tasaIVAExencion", 0);
		requestAplicaCredito.body("tasaIVAAdicional", 0);
		return Api.response(requestAplicaCredito, contexto.idCobis());
	}

	public static ApiResponse reservaAplicaCreditoDebito(ContextoHB contexto, String operacion, Cuenta cuenta, String idTransaccion, BigDecimal importe, String codigoConcepto, String fechaConcertacion) {
		ApiRequest requestAplicaCredito = Api.request("AplicaReversaCredito", "cuentas", "PATCH", "/v1/cuentas/{idcuenta}/" + operacion + "/{idtransaccion}", contexto);
		requestAplicaCredito.path("idcuenta", cuenta.numero());
		requestAplicaCredito.path("idtransaccion", idTransaccion);
		requestAplicaCredito.body("tipoProducto", cuenta.idTipo());
		requestAplicaCredito.body("cuenta", cuenta.numero());
		requestAplicaCredito.body("idMoneda", parseInt(cuenta.idMoneda()));
		requestAplicaCredito.body("codigoOficina", parseInt(cuenta.sucursal()));
		requestAplicaCredito.body("importe", importe);
		requestAplicaCredito.body("codigoConcepto", codigoConcepto);
		requestAplicaCredito.body("fecha", fechaConcertacion);
		requestAplicaCredito.body("usuario", contexto.idCobis());
		requestAplicaCredito.body("importeBase", importe);
		requestAplicaCredito.body("ivaBasico", 0);
		requestAplicaCredito.body("ivaPercepcion", 0);
		requestAplicaCredito.body("ivaAdicional", 0);
		requestAplicaCredito.body("tasaIVABasico", 0);
		requestAplicaCredito.body("tasaIVAReduccion", 0);
		requestAplicaCredito.body("tasaIVAPercepcion", 0);
		requestAplicaCredito.body("tasaIVAExencion", 0);
		requestAplicaCredito.body("tasaIVAAdicional", 0);
		return Api.response(requestAplicaCredito, contexto.idCobis());
	}

	public static ApiResponse getTarjetaDebito(ContextoHB contexto, Cuenta cuenta) {
		ApiRequest requestTarjetaDebitos = Api.request("CuentasGetTarjetasDebito", "cuentas", "GET", cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/tarjetasdebito" : "/v1/cuentascorrientes/{idcuenta}/tarjetasdebito", contexto);
		requestTarjetaDebitos.path("idcuenta", cuenta.numero());
		return Api.response(requestTarjetaDebitos, cuenta.id());
	}

	public static void eliminarCacheCuentaCoelsa(ContextoHB contexto, String cbuAlias) {
		Api.eliminarCache(contexto, "CuentaCoelsa", cbuAlias);
	}

	/* ========== STRING ========== */
	public static String tipoCuentaBH(String numeroCuenta) {
		return numeroCuenta.startsWith("2") || numeroCuenta.startsWith("4") ? "AHO" : numeroCuenta.startsWith("3") ? "CTE" : null;
	}

	public static String idMonedaCuentaBH(String numeroCuenta) {
		return numeroCuenta.startsWith("3") || numeroCuenta.startsWith("4") ? "80" : numeroCuenta.startsWith("2") ? "2" : null;
	}

	/* ========== BOOLEAN ========== */
	public static Boolean esCajaAhorroBH(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() != 22 && (parametro.startsWith("2") || parametro.startsWith("4"));
	}

	public static Boolean esCuentaCorrienteBH(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() != 22 && parametro.startsWith("3");
	}

	public static Boolean esCbuBH(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() == 22 && parametro.startsWith("044");
	}

	public static Boolean esCbu(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() == 22 && !parametro.startsWith("000");
	}

	public static Boolean esCvu(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() == 22 && parametro.startsWith("000");
	}

	public static Boolean esCuentaBH(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() == 15 && (parametro.startsWith("2") || parametro.startsWith("3") || parametro.startsWith("4"));
	}

	public static Boolean esAlias(String parametro) {
		return !parametro.matches("[0-9]*");
	}
}
