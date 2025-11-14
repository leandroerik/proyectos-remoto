package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.TransmitMB;

public class MBTransferenciaPull {
	
	public static RespuestaMB getListadoCuentas(ContextoMB contexto) {
		
		if(!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
			return RespuestaMB.estado("ERROR_SIN_SESION");
		}
		
		RespuestaMB respuesta = new RespuestaMB();

		String idCobis = contexto.idCobis();
		ApiRequestMB request = ApiMB.request("GetPcp", "canales", "GET", "/obtener-pcp", contexto);
		request.query("idCobis", idCobis);
		
		ApiResponseMB apiResponse = ApiMB.response(request);
		
		if (apiResponse.hayError()) {
			return respuesta.setEstado("ERROR_CTAS_ASOCIADAS");
		}else {
			if(!apiResponse.get("estado").equals("0") ) {
				return respuesta.setEstado("ERROR_CTAS_ASOCIADAS");
				
			}else if(apiResponse.get("datos").equals("[]") ) {
				respuesta.set("datos", new int[0]);
			}else {
				respuesta.set("datos", apiResponse.get("datos"));
			}
			
			return respuesta;
		}
		
	}
	
	public static RespuestaMB ingresarDinero(ContextoMB contexto) {
		String bcraId = contexto.parametros.string("bcraId");
		String cbuOrigen = contexto.parametros.string("cbu_origen");
		String idCtaDestino = contexto.parametros.string("id_cuenta_destino");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String csmId = contexto.parametros.string("csmId", "");
		String checksum = contexto.parametros.string("checksum", "");

		if(!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
			return RespuestaMB.estado("ERROR_SIN_SESION");
		}
		
		if(bcraId.isEmpty() || cbuOrigen.isEmpty() || idCtaDestino.isEmpty()) {
			return RespuestaMB.parametrosIncorrectos();
		}

		boolean esMigrado = contexto.esMigrado(contexto);
		if (esMigrado && Objeto.anyEmpty(csmId, checksum))
			return RespuestaMB.parametrosIncorrectos();

		if(!TransmitMB.validarCsmTransaccion(contexto, JourneyTransmitEnum.MB_INICIO_SESION))
			return RespuestaMB.requiereSegundoFactor();
		
		RespuestaMB respuesta = new RespuestaMB();
		
		String idCobis = contexto.idCobis();
		Cuenta cuenta = contexto.cuenta(idCtaDestino);
		
		String sucursal = cuenta.sucursal();		
		String cbu = cuenta.cbu();
		
		ApiRequestMB request = ApiMB.request("IngresarDinero", "canales", "POST", "/ingresar-dinero", contexto);
		request.body("idCobis", idCobis );
		request.body("bcraId", bcraId );
		request.body("cbuOrigen", cbuOrigen );
		request.body("sucursal", sucursal );
		request.body("cbu", cbu );
		request.body("monto", monto);
		
		ApiResponseMB apiResponse = ApiMB.response(request);

		if (apiResponse.hayError() || apiResponse.get("estado").equals("ERROR_TRX_PULL")) {
			return respuesta.set("ERROR", contexto.csmIdAuth);
		}else if(apiResponse.get("estado").equals("PENDIENTE")){
			return respuesta.set("PENDIENTE", contexto.csmIdAuth);
		}else {
			if (MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
				Objeto parametros = new Objeto();
				String salesforce_enviar_y_recibir_dinero_modo = ConfigMB.string("salesforce_enviar_y_recibir_dinero_modo");
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO", contexto.persona().apellido());
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
//				parametros.set("TRANSACCION_ID", String.valueOf(transferMap.get("id_transaction")));
				parametros.set("RECIPENT_CBU", cbu);
				parametros.set("MONTO", monto);
				parametros.set("EMAIL", contexto.persona().email());
				parametros.set("TIPO_OPERACION", "Recibir");
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_enviar_y_recibir_dinero_modo, parametros));
			}

			return respuesta.set("csmIdAuth", contexto.csmIdAuth);
		}
	
	}

}
