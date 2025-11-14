package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.util.Date;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.plazosfijos.PlazoFijoDetalle.PlazoFijoDet;

public class PlazoFijoDetalle extends ApiObjetos<PlazoFijoDet> {

	/* ========== ATRIBUTOS ========== */
	public class PlazoFijoDet extends ApiObjeto {
	    
	    public Date fechaIngreso;
	    public Double impuesto;
    	public Date	fechaValor;
    	public boolean cubiertoPorGarantia;
    	public Integer oficina;
    	public Double importePesos;
    	public Double importe;
    	public Date fechaVencimiento;
    	public String numeroBanco;
    	public Double tasaEfectiva;
    	public String cuenta;
    	public Double cotizacion;
    	public String idEstado;
    	public Double tasa;
    	public String renovaciones;
    	public boolean renuevaIntereses;
    	public Integer cantidadDias;
    	public boolean renueva;
    	public Double valorIndice;
    	public Double sellos;
    	public Integer nroOperacion;
    	public Double interesEstimado;
    	public String descOperacion;
    	public boolean renovacionAutomatica;
    	public Date fechaActivacion;
    	public String funcionario_bh;
    	public String cancelacionAnticipada;
    	public Double tnaCancelacionAnt;
    	public Double teaCancelacionAnt;

		public static class RespuestaOk extends ApiObjeto {
			public Boolean ok;
		}
	}

	/* ========== SERVICIOS ========== */	
	public static PlazoFijoDetalle get(Contexto contexto, String numeroBanco) {
		ApiRequest request = new ApiRequest("API-PlazoFijo_ConsultaPlazosFijos", "plazosfijos", "GET", "/v1/plazosfijos/{numeroBanco}", contexto);
		//ejemplo uri:"/v1/plazosfijos/00000210000000019?empresarial=false&historico=false"
		
		Parametros parametros = contexto.parametros;
		
		request.path("numeroBanco", numeroBanco);
		request.query("empresarial", parametros.get("empresarial"));
		request.query("historico", parametros.get("historico"));
		
		ApiResponse response = request.ejecutar();		
		ApiException.throwIf(!response.http(200), request, response);	
				
		return response.crear(PlazoFijoDetalle.class);
	}
}

