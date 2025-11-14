package ar.com.hipotecario.backend.servicio.api.tarjetaDebito;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TarjetasDebitos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String numeroTarjeta;
	public String tipoTarjeta;
	public String descTipoTarjeta;
	public String estadoTarjeta;
	public String descEstadoTarjeta;
	public BigDecimal limiteExtraccion;
	public Integer cantidadAdicionales;
	public String descSucursal;
	public Fecha fechaExpiracion;
	public String nroSolicitud;

	/* ========== SERVICIOS ========== */
	// API-TarjetasDebito_ConsultaTarjetaDebitoDetalle
	public static TarjetasDebitos get(Contexto contexto, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("TarjetaDebito", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{numeroTarjeta}", contexto);
		request.path("numeroTarjeta", numeroTarjeta);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TarjetasDebitos.class);
	}
	
	// API-TarjetasDebito_HabilitarTarjetaDebitoLink
	public static TarjetasDebitos habilitarTarjetaDebito(Contexto contexto, String numero) {
		ApiRequest request = new ApiRequest("TarjetaDebitoActivar", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}/habilitar", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TarjetasDebitos.class);
	}
	
	// API-TarjetasDebito_AltaSolicitudAmpliacionLimiteTarjetaDebito
	public static ApiResponse modificarLimiteTarjetaDebito(Contexto contexto, String idCliente, String numero, String limiteRetiro) {
		ApiRequest request = new ApiRequest("ModificarLimiteTarjetaDebito", "tarjetasdebito", "PUT", "/v1/tarjetasdebito/{nrotarjeta}", contexto);
		request.path("nrotarjeta", numero);
		request.query("idcliente", idCliente);
		request.query("limiteretiro", limiteRetiro);
		request.query("tipotarjeta", "EM");
		ApiResponse response = request.ejecutar();

		return response;
	}
	
	// API-TarjetasDebito_ConsultaTitularidadTD
	public static ApiResponse obtenerTitularidadTd(Contexto contexto, String numeroTarjeta, String extendido) {
		ApiRequest request = new ApiRequest("Consulta_TitularidadTd", "tarjetasdebito", "GET", "/v1/tarjetaDebitoTitular", contexto);
		request.query("tarjeta", numeroTarjeta);
		request.query("extendido", extendido);
		
		ApiResponse response = request.ejecutar();
		return response;
	}
	
	// API-TarjetasDebito_BlanqueoPinLink
	public static ApiResponse tarjetaDebitoBlanquearPin(Contexto contexto, String numero) {
		ApiRequest request = new ApiRequest("TarjetaDebitoBlanquearPin", "tarjetasdebito", "DELETE",
				"/v1/tarjetasdebito/{nrotarjeta}/pin", contexto);
		request.path("nrotarjeta", numero);
		request.query("digitoverificador", "0");
		request.query("numeromiembro", "0");
		request.query("numeroversion", "0");

		ApiResponse response = request.ejecutar();
		return response;
	}	
	
	// API-TarjetasDebito_ConsultaEstadoTarjetaDebitoLink
	public static ApiResponse consultaEstadoTarjetaDebitoLink(Contexto contexto,String digitoVerificador,String numeroMiembro,String numeroVersion, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("ConsultaEstadoTarjetaDebitoLink", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{nrotarjeta}/estado", contexto);
		request.path("nrotarjeta", numeroTarjeta);
		request.query("digitoverificador", digitoVerificador);
		request.query("numeromiembro", numeroMiembro);
		request.query("numeroversion", numeroVersion);


		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response;
	}

}
