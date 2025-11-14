package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class SimulacionPlazoFijoWeb extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String tipo;
	public String moneda;
	public BigDecimal capital;
	public BigDecimal tasa;
	public Integer plazo;
	public Fecha fechaActivacion;
	public BigDecimal totalInteresEstimado;
	public Fecha fechaPagoInteres;
	public String diaDePago;
	public Fecha fechaVencimiento;
	public Boolean garantiaDeLosDepositos;
	public String cancelacionAnticipada;
	public BigDecimal tasaCancelacionAnt;
	public BigDecimal tnaCancelacionAnt;
	public BigDecimal teaCancelacionAnt;
	public Fecha fechaDesdeCancelacionAnt = Fecha.nunca();
	public Fecha fechaHastaCancelacionAnt = Fecha.nunca();
	public BigDecimal montoUVA;
	public BigDecimal cotizacionUVA;
	public Fecha fechaCotizacionUVA = Fecha.nunca();

	/* ========== SERVICIOS ========== */
	public static SimulacionPlazoFijoWeb post(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion) {
		return post(contexto, monto, plazo, tipoOperacion, "0");
	}

	// API-PlazoFijo_SimulacionPlazoFijoWeb
	public static SimulacionPlazoFijoWeb post(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion, String numeroSucursal) {
		ApiRequest request = new ApiRequest("SimulacionPlazoFijoWeb", "plazosfijos", "POST", "/v1/plazosfijosweb/simulacion", contexto);
		request.body("tipoOperacion", tipoOperacion);
		request.body("moneda", "80");
		request.body("monto", monto);
		request.body("plazo", plazo);
		request.body("numeroSucursal", numeroSucursal);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("FUERA_HORARIO", response.codigo("40003"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SimulacionPlazoFijoWeb.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SimulacionPlazoFijoWeb datos = post(contexto, new BigDecimal("5000"), 90, PlazoFijoWeb.UVAS);
		imprimirResultado(contexto, datos);
	}
}
