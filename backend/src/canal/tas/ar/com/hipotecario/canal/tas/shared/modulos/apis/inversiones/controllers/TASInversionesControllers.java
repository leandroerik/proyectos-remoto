package ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.controllers;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.servicios.TASRestInversiones;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TASInversionesControllers {


    public static Objeto getCotizaciones(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idMoneda = contexto.parametros.string("idMoneda");
            if(idMoneda.isEmpty()) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            if(idMoneda.equals("02")){
                Objeto responseCotizacion = getCotizacionByMercado(contexto);
                if(responseCotizacion.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto,"TASInversionesController - getCotizaciones()", (Exception) responseCotizacion.get("error"));
                if(responseCotizacion.isEmpty()) return RespuestaTAS.sinResultados(contexto, "cotizacion no encontrada");
                return responseCotizacion;
            }
            Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", new Fecha(new Date()));
            Fecha fechaHasta = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", new Fecha(new Date()));
            Objeto cotizacionUvaResponse = TASRestInversiones.getCotizacionUVA(contexto, idMoneda, fechaDesde, fechaHasta);
            return cotizacionUvaResponse.isEmpty() ?
                    RespuestaTAS.sinResultados(contexto, "cotizacion UVA no encontrada")
            : new Objeto().set("cotUva", Double.valueOf(cotizacionUvaResponse.objetos().get(0).string("valorCotizacion")));
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASInversionesController - getCotizaciones()", e);
        }
    }

    public static Objeto getCotizacionByMercado(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente", "0");
            String codigoMoneda = contexto.parametros.string("idMoneda");
            String codigoMercado = "";
            if(codigoMoneda.equals("02")) codigoMercado = "06";
            Objeto responseCotizacion = TASRestInversiones.getCotizacionByMercado(contexto, idCliente, codigoMercado);
            Objeto response = new Objeto();
            for(Objeto responseApi : responseCotizacion.objetos()){
                if(responseApi.string("moneda").equals("2")){
                    response.set("cotDolar", Double.valueOf(responseApi.bigDecimal("venta").toString()));
                }
            }
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    
    public static Objeto getCotizacionesByMercado(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente", "0");
            String codigoMercado = "06";
            Objeto responseCotizacion = TASRestInversiones.getCotizacionByMercado(contexto, idCliente, codigoMercado);
    		Objeto response = new Objeto();
            for(Objeto responseApi : responseCotizacion.objetos()){
            	Objeto cot = new Objeto();
                cot.set("cotizacionCompra", Double.valueOf(responseApi.bigDecimal("compra").toString()));
                cot.set("cotizacionVenta", Double.valueOf(responseApi.bigDecimal("venta").toString()));
                if(responseApi.string("moneda").equals("2")){
                    cot.set("descripcionMoneda","Dólares EEUU");
                } else if (responseApi.string("moneda").equals("98")){
                    cot.set("descripcionMoneda","Euros");
                }
                response.add(cot);
            }
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

	public static Object getCotizacionesMoneda(ContextoTAS contexto) {
		  try {
	          
			  		
			  		Objeto cotizaciones =  new Objeto();
	                Objeto responseCotizacion = getCotizacionesByMercado(contexto);
	                if(responseCotizacion.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto,"TASInversionesController - getCotizaciones()", (Exception) responseCotizacion.get("error"));
	                if(responseCotizacion.isEmpty()) return RespuestaTAS.sinResultados(contexto, "cotizacion no encontrada");	       
	                Integer cantCotizaciones = responseCotizacion.objetos().size();
	            Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", new Fecha(new Date()));
	            Fecha fechaHasta = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", new Fecha(new Date()));
	            Objeto cotizacionUvaResponse = TASRestInversiones.getCotizacionUVA(contexto, "88", fechaDesde, fechaHasta);
	           if (!cotizacionUvaResponse.isEmpty()) {
	        	   cotizaciones.set("cotUva", Double.valueOf(cotizacionUvaResponse.objetos().get(0).string("valorCotizacion")));
	        	   cotizaciones.set("descripcionMoneda", "UVA");
	        	   cantCotizaciones++;
	           }
	           cotizaciones.set("cotizaciones", responseCotizacion);
	           cotizaciones.set("cantidadCotizaciones", cantCotizaciones);
	           return cotizaciones;
		  }catch (Exception e){
	            return RespuestaTAS.error(contexto, "TASInversionesController - getCotizaciones()", e);
	        }
	}
}
