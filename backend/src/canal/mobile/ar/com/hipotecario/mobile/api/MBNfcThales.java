package ar.com.hipotecario.mobile.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;


public class MBNfcThales {
	
	public static RespuestaMB registrar(ContextoMB contexto) {
		
		if(!(contexto.sesion() != null && contexto.sesion().idCobisReal() != null)) {
			return RespuestaMB.estado("ERROR_SIN_SESION");
		}
		
		Objeto registrar = new Objeto();
			
		List<Objeto> tarjetasDebito = new ArrayList<>();
		String idCobis = contexto.idCobis();
		
		registrar.set("idCobis", idCobis);
		
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			Objeto tDebito = new Objeto();
			tDebito.set("numTarjeta", tarjetaDebito.numero() );
			tDebito.set("expTarjeta", tarjetaDebito.fechaVencimiento("MM/yy"));
			tDebito.set("tipoDescripcion", tarjetaDebito.producto());
			tDebito.set("tipo", tarjetaDebito.getTipo());
			
			if(!tarjetaDebito.activacionTemprana()) {
				tarjetasDebito.add(tDebito);
			}
		}
		 registrar.set("tarjetasDebito", tarjetasDebito);
		
		 List<Objeto> tarjetasCredito = new ArrayList<>();
		
		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
			Objeto tCredito = new Objeto();
			
			tCredito.set("numTarjeta",tarjetaCredito.id());
			tCredito.set("expTarjeta", tarjetaCredito.fechaVigenciaHasta("MM/yy"));
			tCredito.set("tipo", tarjetaCredito.idTipo()); 
			tCredito.set("tipoDescripcion", tarjetaCredito.tipo());
			tCredito.set("esTitular", tarjetaCredito.esTitular());
			tCredito.set("titularidad", tarjetaCredito.titularidad());
			tCredito.set("esLibertad", tarjetaCredito.esHML());			
			
			if(!tarjetaCredito.idEstado().equals("29") ) {
				tarjetasCredito.add(tCredito);
			}

		}		
		 registrar.set("tarjetasCredito", tarjetasCredito);
		 
		 RespuestaMB respuesta = new RespuestaMB();
		 
		 if(tarjetasDebito.isEmpty() &&  tarjetasCredito.isEmpty()) {
			 respuesta.set("estado", "0");
			 respuesta.set("consumerId", idCobis);
				respuesta.set("tarjetasDebito", tarjetasDebito);
				respuesta.set("tarjetasCredito", tarjetasCredito);
			 return respuesta;
		 }
		 
		 ApiRequestMB request = ApiMB.request("Registro", "thales", "POST", "/v1/tarjetas", contexto);
		    request.body(registrar);

		    ApiResponseMB apiResponse = ApiMB.response(request);

			if (apiResponse.hayError()) {
				respuesta.set("estado", "ERROR");
				respuesta.set("error", apiResponse.get("mensajeAlUsuario"));
				respuesta.set("detalleError", apiResponse.get("detalle"));
			}else {
				respuesta.set("estado", "0");
				respuesta.set("consumerId", apiResponse.get("consumerId"));
				respuesta.set("tarjetasDebito", apiResponse.get("tarjetasDebito"));
				respuesta.set("tarjetasCredito", apiResponse.get("tarjetasCredito"));
				
			}
			
			return respuesta;
	}
	
	public static RespuestaMB obtenerTokenThales(ContextoMB contexto) {

			String consumerId = contexto.parametros.string("consumerId");
			
			if(consumerId.isEmpty()) {
				return RespuestaMB.estado("PARAMETRO_INCORRECTO");
			}
			
			 RespuestaMB respuesta = new RespuestaMB();
			 ApiRequestMB request = ApiMB.request("Autenticar", "thales", "GET", "/v1/tokens/{consumerId}", contexto);
			 request.permitirSinLogin = true;
			 request.path("consumerId", consumerId);

			 CompletableFuture<ApiResponseMB> apiResponseFuture = CompletableFuture.supplyAsync(() -> ApiMB.response(request));

			    apiResponseFuture.thenAccept(apiResponse -> {
			        if (apiResponse.hayError()) {
			            RespuestaMB.error();
			        } else {
			            respuesta.set("estado", "0");
			            respuesta.set("token", apiResponse.get("token"));
			        }
			    }).join();

			    return respuesta;
	}

}
