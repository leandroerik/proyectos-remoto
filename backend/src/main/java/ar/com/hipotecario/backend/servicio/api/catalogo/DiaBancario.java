package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class DiaBancario extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String diaDeSemana;
	public String nombreDia;
	public Fecha diaHabilPosterior;
	public Fecha diaHabilAnterior;
	public String flEsUltimoDiaDelMes;
	public String anio;
	public String anioMes;
	public String esDiaHabil;
	public Fecha ultimoDiaDelMes;
	public Fecha fecha;
	public String anioBisiesto;
	public String trimestre;
	public String mes;
	public String diasDelMes;
	public String dia;
	public String semana;

	/* ========== METODOS ========== */
	public Boolean esDiaHabil() {
		return "1".equals(esDiaHabil);
	}
	
	public Boolean esFinDeSemana() {
		return nombreDia.equalsIgnoreCase("Sabado") || nombreDia.equalsIgnoreCase("Domingo");
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaCalendario
	static DiaBancario get(Contexto contexto, Fecha fecha) {
		ApiRequest request = new ApiRequest("DiaBancario", "catalogo", "GET", "/v1/calendario/{fecha}", contexto);
		request.path("fecha", fecha.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DiaBancario.class, response.objetos(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("HB", "homologacion");
		DiaBancario datos = get(contexto, Fecha.hoy());
		imprimirResultado(contexto, datos);
	}
}
