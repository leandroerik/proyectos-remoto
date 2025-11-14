package ar.com.hipotecario.mobile.api;


import java.util.List;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.servicio.TarjetaCreditoService;

public class MBPlanV {
	
	public static RespuestaMB obtenerInformacionFinanciamiento(ContextoMB contexto) {
		
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
		String tipoDocumento = contexto.parametros.string("tipoDocumento", null);
		String genero = contexto.parametros.string("genero", null);
		boolean tipoResumen = contexto.parametros.bool("tipoResumen", false);
		
		if (Objeto.anyEmpty(genero,tipoDocumento)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}
		
		ApiResponseMB response = TarjetaCreditoService.obtenerInformacionFinanciamiento(contexto, 
				tarjetaCredito, tipoDocumento, genero, tipoResumen);

		
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("maxCuotas", response.get("maxCuotas"));
		respuesta.set("minCuotas", response.get("minCuotas"));
		respuesta.set("montoCuotasDisponibles", response.get("montoCuotasDisponibles"));
		respuesta.set("maxMontoFinanciable", response.get("maxMontoFinanciable"));
		respuesta.set("maxMonto", response.get("maxMonto"));
		respuesta.set("minMonto", response.get("minMonto"));
		respuesta.set("limiteFinanciacion", response.get("limiteFinanciacion"));
		respuesta.set("pagoMinimo", response.get("pagoMinimo"));
		respuesta.set("pagosRegistrados", response.get("pagosRegistrados"));
		respuesta.set("tasaNominalAnual", response.get("tasaNominalAnual"));
		respuesta.set("saldoPesosYDolares", response.get("saldoPesosYDolares"));
		respuesta.set("saldoPesos", response.get("saldoPesos"));
		respuesta.set("saldoDolares", response.get("saldoDolares"));
		respuesta.set("cuotasSaldo", response.get("cuotasSaldo"));
		respuesta.set("fechaCierreUltimoCicloFacturacion", response.get("fechaCierreUltimoCicloFacturacion"));
		respuesta.set("codigoFinanciacion", response.get("codigoFinanciacion"));
		respuesta.set("consumosPesosYDolares", response.get("consumosPesosYDolares"));
		respuesta.set("consumosPesos", response.get("consumosPesos"));
		respuesta.set("consumosDolares", response.get("consumosDolares"));
		
		return respuesta;
	}
	
	public static RespuestaMB simularFinanciamiento(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
		String tipoDocumento = contexto.parametros.string("tipoDocumento", null);
		String genero = contexto.parametros.string("genero", null);
		String cuotas = contexto.parametros.string("cuotas", null);
		String moneda = contexto.parametros.string("moneda", null);
		String monto = contexto.parametros.string("monto", null);
		boolean tipoResumen = contexto.parametros.bool("tipoResumen", false);
		
		if (Objeto.anyEmpty(cuotas, moneda, monto,genero,tipoDocumento)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		
    	Objeto cliente = new Objeto();
    	cliente.set("genero", genero);
    	cliente.set("numeroDocumento", contexto.persona().numeroDocumento());
    	cliente.set("tipoDocumento", tipoDocumento);
		
		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}
		
    	Objeto tarjeta = new Objeto();
    	tarjeta.set("binTarjeta", tarjetaCredito.numeroBin());
    	tarjeta.set("ultimosCuatroDigitosTarjeta", tarjetaCredito.ultimos4digitos());
    	
    	ApiResponseMB response = TarjetaCreditoService.simularFinanciamiento(contexto,cliente,tarjeta,
    		cuotas,moneda,monto,tipoResumen);
    	
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		
		RespuestaMB respuesta = new RespuestaMB();
		
		respuesta.set("iva", response.get("iva"));
		respuesta.set("cuotas", response.get("cuotas"));
		respuesta.set("montoCargo", response.get("montoCargo"));
		respuesta.set("tipoCargo", response.get("tipoCargo"));
		respuesta.set("costoFinancieroTotal", response.get("costoFinancieroTotal"));
		respuesta.set("montoCuota", response.get("montoCuota"));
		respuesta.set("tasaAnualNominal", response.get("tasaAnualNominal"));
		respuesta.set("montoTasaSeguro", response.get("montoTasaSeguro"));
		respuesta.set("montoSolicitado", response.get("montoSolicitado"));

		return respuesta;
	}
	
	public static RespuestaMB confirmarFinanciamiento(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito", null);
		String tipoDocumento = contexto.parametros.string("tipoDocumento", null);
		String genero = contexto.parametros.string("genero", null);
		String cuotas = contexto.parametros.string("cuotas", null);
		String moneda = contexto.parametros.string("moneda", null);
		String monto = contexto.parametros.string("monto", null);
		boolean tipoResumen = contexto.parametros.bool("tipoResumen", false);
		
		if (Objeto.anyEmpty(cuotas, moneda, monto,genero,tipoDocumento)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		
    	Objeto cliente = new Objeto();
    	cliente.set("genero", genero);
    	cliente.set("numeroDocumento", contexto.persona().numeroDocumento());
    	cliente.set("tipoDocumento", tipoDocumento);
		
		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.estado("NO_EXISTE_TARJETA_CREDITO");
		}
		
    	Objeto tarjeta = new Objeto();
    	tarjeta.set("binTarjeta", tarjetaCredito.numeroBin());
    	tarjeta.set("ultimosCuatroDigitosTarjeta", tarjetaCredito.ultimos4digitos());
    	
    	ApiResponseMB response = TarjetaCreditoService.confirmarFinanciamiento(contexto,cliente,tarjeta,
        		cuotas,moneda,monto, tipoResumen);
        	
    	if (response.hayError()) {
    		return RespuestaMB.error();
    	}
    	RespuestaMB respuesta = new RespuestaMB();
    	
    	respuesta.set("moneda", response.get("moneda"));
		respuesta.set("iva", response.get("iva"));
		respuesta.set("costoFinancieroTotal", response.get("costoFinancieroTotal"));
		respuesta.set("montoCuota", response.get("montoCuota"));
		respuesta.set("cargoAdministrativo", response.get("cargoAdministrativo"));
		respuesta.set("cargoSeguroVida", response.get("cargoSeguroVida"));
		respuesta.set("montoSolicitud", response.get("montoSolicitud"));
		respuesta.set("numeroAplicacion", response.get("numeroAplicacion"));
		respuesta.set("planPago", response.get("planPago"));
		respuesta.set("tasaAnualNominal", response.get("tasaAnualNominal"));
		respuesta.set("pagoDiferido", response.get("pagoDiferido"));

		return respuesta;
	}
	
	public static RespuestaMB obtenerFinanciamientosAprobados(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string("idTarjetaCredito");
		// C para consumo, B para resumen 
		String tipo = contexto.parametros.string("tipo", null);
		
		if (Objeto.anyEmpty(idTarjetaCredito, tipo)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		
		TarjetaCredito tarjetaCredito = idTarjetaCredito == null ? contexto.tarjetaCreditoTitular()
				: contexto.tarjetaCredito(idTarjetaCredito);
		String nroCuenta = tarjetaCredito.numeroCuenta();
        if (nroCuenta == null) {
            return RespuestaMB.error();
        }
        
		ApiResponseMB response = TarjetaCreditoService.obtenerFinanciamientosAprobados(contexto,
				nroCuenta, tipo);
		
		if (response.hayError()) {
			return RespuestaMB.error();
		}
		
		RespuestaMB respuesta = new RespuestaMB();

		respuesta.add("datos", response.get("data"));
		
		return respuesta;
	}

}
