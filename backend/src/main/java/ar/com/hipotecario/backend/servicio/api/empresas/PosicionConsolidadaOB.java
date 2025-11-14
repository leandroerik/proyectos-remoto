package ar.com.hipotecario.backend.servicio.api.empresas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PosicionConsolidadaOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String descripcion;
	public String id;
	public String banca;
	public String descripcionBanca;
	public String nombreOficial;
	public String sucursal;
	public String empresaControlante;
	public Integer totalRegistros;
	public Integer ultimoRegistro;
	public List<Cliente> clientes;

	public static class Cliente extends ApiObjeto {
		public String actividad;
		public String codigo;
		public String nombre;
		public String nroDocumento;
		public BigDecimal porcentajeParticipacion;
		public List<Producto> productos;
		public String tipo;
		public BigDecimal totalPosicionAcreedoraDolares;
		public BigDecimal totalPosicionAcreedoraPesos;
		public BigDecimal totalPosicionDeudoraDolares;
		public BigDecimal totalPosicionDeudoraPesos;
	}

	public static class Producto extends ApiObjeto {
		public String categoria;
		public String descripcionGarantias;
		public Fecha fechaInicio;
		public Fecha fechaVencimiento;
		public String garantias;
		public String moneda;
		public BigDecimal monto;
		public BigDecimal montoComisiones;
		public BigDecimal montoImpuestos;
		public BigDecimal montoIntereses;
		public String numero;
		public String tipo;
		public String tipoDeuda;
	}

	/* ========== SERVICIOS ========== */
	// API-Empresas_ConsultaPosicionConsolidadaEmpresa
	public static PosicionConsolidadaOB get(Contexto contexto, String idEmpresa) {
		ApiRequest request = new ApiRequest("PosicionConsolidadaOB", "empresas", "GET", "/v1/empresas/{id}/posicionconsolidada", contexto);
		request.body("ip", contexto.ip());
		request.query("grupo", true);
		request.query("tipoconsulta", "P");
		request.path("id", idEmpresa);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("CLIENTE_INVALIDO", response.contains("NOT_VALID_CLIENT"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		PosicionConsolidadaOB posicionConsolidada = response.crear(PosicionConsolidadaOB.class);

		return posicionConsolidada;
	}

	/* ========== TEST ========== */ // 5175946
	public static void main(String[] args) throws InterruptedException {

		Contexto contexto = contexto("OB", "homologacion");
		PosicionConsolidadaOB datos = get(contexto, "354");
		imprimirResultado(contexto, datos);
	}
}
