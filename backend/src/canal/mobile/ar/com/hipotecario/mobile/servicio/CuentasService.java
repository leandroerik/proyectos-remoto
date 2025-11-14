package ar.com.hipotecario.mobile.servicio;

import static java.lang.Integer.parseInt;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.negocio.Cuenta;

public class CuentasService {

    public static SqlResponseMB selectComentarioCuenta(ContextoMB contexto) {
        SqlResponseMB response = null;
        SqlRequestMB sqlRequest = SqlMB.request("SelectComentarioCuenta", "hbs");
        sqlRequest.sql = "SELECT [idCobis], [cuenta], [comentario] FROM [Hbs].[dbo].[comentarios_cuentas] WHERE idCobis = ?";
        sqlRequest.add(contexto.idCobis());
        response = SqlMB.response(sqlRequest);
        return response;
    }

    /* ========== SERVICIOS ========== */
    public static ApiResponseMB cuentaBH(ContextoMB contexto, String numero) {
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

    public static ApiResponseMB cajaAhorroBH(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("CajaAhorroBH", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}", contexto);
        request.path("idcuenta", numero);
        request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.query("validacuentaempleado", "false");
        request.query("historico", "false");
        request.cacheSesion = true;
        request.permitirSinLogin = true;

        return ApiMB.response(request, numero);
    }

    public static ApiResponseMB cajaAhorroBHsinLogin(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("CajaAhorroBH", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}", contexto);
        request.path("idcuenta", numero);
        request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.query("validacuentaempleado", "false");
        request.query("historico", "false");
        request.permitirSinLogin = true;
        request.cacheSesion = true;
        return ApiMB.response(request, numero);
    }

    public static ApiResponseMB cuentaCorrienteBH(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("CuentaCorrienteBH", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}", contexto);
        request.path("idcuenta", numero);
        request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.query("validacuentaempleado", "false");
        request.cacheSesion = true;
        return ApiMB.response(request, numero);
    }

    public static ApiResponseMB cuentaCorrienteBHsinLogin(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("CuentaCorrienteBH", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}", contexto);
        request.path("idcuenta", numero);
        request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        request.query("validacuentaempleado", "false");
        request.permitirSinLogin = true;
        request.cacheSesion = true;
        return ApiMB.response(request, numero);
    }

    public static ApiResponseMB cuentaCbuBH(ContextoMB contexto, String cbu) {
        ApiRequestMB request = ApiMB.request("CuentaCbuBH", "cuentas", "GET", "/v1/cuentas", contexto);
        request.query("cbu", cbu);
        request.query("acuerdo", "false");
        request.query("consultaalias", "false");
        request.cacheSesion = true;
        return ApiMB.response(request, cbu);
    }

    public static ApiResponseMB cuentaLink(ContextoMB contexto, String cbu, String idMoneda) {
        if (contexto.tarjetaDebitoPorDefecto() == null) {
            ApiResponseMB response = new ApiResponseMB();
            response.codigo = 404;
            return response;
        }
        ApiRequestMB request = ApiMB.request("CuentaLink", "cuentas", "GET", "/v1/cuentas", contexto);
        request.query("idcliente", contexto.idCobis());
        request.query("cbu", cbu);
        request.query("numerotarjeta", contexto.tarjetaDebitoPorDefecto().numero());
        request.query("idmoneda", idMoneda);
        request.query("consultaalias", "false");
        request.cacheSesion = true;
        // request.cacheBaseDatosNoProductiva = true;
        ApiResponseMB response = ApiMB.response(request, cbu, idMoneda);
        return response;
    }

    public static ApiResponseMB cuentaCoelsa(ContextoMB contexto, String cbuAlias) {
        ApiRequestMB request = ApiMB.request("CuentaCoelsa", "cvu", "GET", "/v1/alias", contexto);
        if (esCbu(cbuAlias) || esCvu(cbuAlias)) {
            request.query("cbu", cbuAlias);
        } else if (esAlias(cbuAlias)) {
            request.query("alias", cbuAlias);
        } else {
            return null;
        }
        request.cacheSesion = true;
        ApiResponseMB response = ApiMB.response(request, cbuAlias);

        if (!response.string("respuesta.numero").equals("0100")) {
            return response;
        }

        ApiResponseMB responseMapeado = new ApiResponseMB();
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

    public static ApiResponseMB cuentaCoelsaApiCuenta(ContextoMB contexto, String cbuAlias) {
        ApiRequestMB request = ApiMB.request("CuentaCoelsa", "cuentas", "GET", "/v1/cuentas", contexto);
        if (esCbu(cbuAlias) || esCvu(cbuAlias)) {
            request.query("cbu", cbuAlias);
        } else if (esAlias(cbuAlias)) {
            request.query("alias", cbuAlias);
        } else {
            return null;
        }
        request.query("acuerdo", "false");
        request.query("consultaalias", "true");
        request.cacheSesion = true;
        return ApiMB.response(request, cbuAlias);
    }

    public static ApiResponseMB cajaAhorroBloqueos(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("CuentasCajaAhorroBloqueos", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}/bloqueos", contexto);
        request.path("idcuenta", numero);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        return ApiMB.response(request);
    }

    public static ApiResponseMB cuentaBloqueos(ContextoMB contexto, String numero, String tipoCuenta) {
        ApiRequestMB request = ApiMB.request("CuentasBloqueos", "cuentas", "GET", "/v1/cuentas/{id}/bloqueos", contexto);
        request.path("id", numero);
        request.query("tipoCuenta", tipoCuenta);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        request.cacheSesion = true;
        return ApiMB.response(request, contexto.idCobis(), numero);
    }

    public static ApiResponseMB cuentaCorrienteBloqueos(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("CuentasCorrientesBloqueos", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}/bloqueos", contexto);
        request.path("idcuenta", numero);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        return ApiMB.response(request);
    }

    public static ApiResponseMB chequeras(ContextoMB contexto, String numero) {
        ApiRequestMB request = ApiMB.request("Chequeras", "cuentas", "GET", "/v1/cuentascorrientes/{idcuenta}/chequeras", contexto);
        request.path("idcuenta", numero);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        return ApiMB.response(request);
    }

    public static ApiResponseMB debitosAdheridos(ContextoMB contexto, String numero) {
        return debitosAdheridosCuentaCorriente(contexto, numero);
    }

    public static ApiResponseMB debitosAdheridosCuentaCorriente(ContextoMB contexto, String numero) {
        return debitosAdheridos(contexto, numero, "cuentascorrientes");
    }

    public static ApiResponseMB debitosAdheridosCajaAhorro(ContextoMB contexto, String numero) {
        return debitosAdheridos(contexto, numero, "cajasahorros");
    }

    public static ApiResponseMB debitosAdheridos(ContextoMB contexto, String numero, String tipoCuenta) {
        ApiRequestMB request = ApiMB.request("DebitosAdheridos", "cuentas", "GET", "/v1/" + tipoCuenta + "/{idcuenta}/debitosadheridos", contexto);
        request.path("idcuenta", numero);
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        return ApiMB.response(request);
    }

    public static ApiResponseMB actualizarMarcaResumen(ContextoMB contexto, Cuenta cuenta, Boolean resumenDigital) {
        ApiRequestMB request = ApiMB.request("DebitosAdheridos", "cuentas", "PATCH", "/v1/cajasahorros/{idcuenta}/marcasDigitales", contexto);
        request.path("idcuenta", cuenta.numero());
        request.body("Transaccion", "3271");
        request.body("fechaDesde", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        request.body("fechaHasta", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        request.body("idcuenta", cuenta.numero());
        request.body("resumen", (resumenDigital ? "S" : "N"));
        request.header("x-usuariocanal", ConfigMB.string("configuracion_usuario"));
        return ApiMB.response(request);
    }

    public static ApiResponseMB aplicaCreditoDebito(ContextoMB contexto, String operacion, Cuenta cuenta, BigDecimal importe, String codigoConcepto, String fechaConcertacion) {
        ApiRequestMB requestAplicaCredito = ApiMB.request("AplicaCredito", "cuentas", "POST", "/v1/cuentas/{idcuenta}/" + operacion, contexto);
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

        return ApiMB.response(requestAplicaCredito, contexto.idCobis());
    }

    public static ApiResponseMB reservaAplicaCreditoDebito(ContextoMB contexto, String operacion, Cuenta cuenta, String idTransaccion, BigDecimal importe, String codigoConcepto, String fechaConcertacion) {
        ApiRequestMB requestAplicaCredito = ApiMB.request("AplicaReversaCredito", "cuentas", "PATCH", "/v1/cuentas/{idcuenta}/" + operacion + "/{idtransaccion}", contexto);
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

        return ApiMB.response(requestAplicaCredito, contexto.idCobis());
    }

    public static void eliminarCacheCuentaCoelsa(ContextoMB contexto, String cbuAlias) {
        ApiMB.eliminarCache(contexto, "CuentaCoelsa", cbuAlias);
    }

    public static ApiResponseMB getTarjetaDebito(ContextoMB contexto, Cuenta cuenta) {
        ApiRequestMB requestTarjetaDebitos = ApiMB.request("CuentasGetTarjetasDebito", "cuentas", "GET", cuenta.esCajaAhorro() ? "/v1/cajasahorros/{idcuenta}/tarjetasdebito" : "/v1/cuentascorrientes/{idcuenta}/tarjetasdebito", contexto);
        requestTarjetaDebitos.path("idcuenta", cuenta.numero());
        return ApiMB.response(requestTarjetaDebitos, cuenta.id());
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
