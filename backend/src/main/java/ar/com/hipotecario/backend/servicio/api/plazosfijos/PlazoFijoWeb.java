package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaLink;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cliente;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;

public class PlazoFijoWeb extends ApiObjeto {

	public static String PESOS = "0040";
	public static String UVAS = "0044";

	/* ========== ATRIBUTOS ========== */
	public BigDecimal interesEstimado;
	public BigDecimal totalInteresEstimado;
	public Fecha fechaPagoInteres;
	public String diaDePago;
	public Fecha fechaVencimiento;
	public Fecha fechaActivacion;
	public String tipo;
	public String moneda;
	public Boolean capitalizaIntereses;
	public BigDecimal tasa;
	public Integer plazo;
	public String cuentaADebitar;
	public Boolean garantiaDeLosDepositos;
	public String numeroOperacion;
	public String estadoTransaccion;
	public String numeroTransaccion;
	public String cancelacionAnticipada;
	public BigDecimal tasaCancelacionAnt;
	public BigDecimal tnaCancelacionAnt;
	public BigDecimal teaCancelacionAnt;

	/* ========== SERVICIOS ========== */
	// API-PlazoFijo_ConstitucionPlazoFijoWeb
	public static PlazoFijoWeb post(Contexto contexto, Persona persona, CuentaLink cuentaLink, BigDecimal monto, Integer plazo, String tipoOperacion) {
		ApiRequest request = new ApiRequest("CrearPlazoFijoWeb", "plazosfijos", "POST", "/v1/plazosfijosweb", contexto);
		request.body("cbu", cuentaLink.cbu);
		request.body("idCliente", persona.idCliente);
		request.body("moneda", "80");
		request.body("monto", monto);
		request.body("numeroSucursal", persona.idSucursalAsignada);
		request.body("plazo", plazo);
		request.body("tipoResidencia", "1");
		request.body("numeroCuit", persona.cuit);
		request.body("tipoCuentaDestino", cuentaLink.tipoProducto);
		request.body("cuentaDestinoPBF", cuentaLink.cuentaPBF);
		request.body("cuentaDestino", cuentaLink.cuenta);
		request.body("tipoOperacion", tipoOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("FUERA_HORARIO", response.codigo("40003"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(PlazoFijoWeb.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Cliente cliente = ApiPersonas.cliente(contexto, "8051446").get();
		CuentaLink cuentaLink = ApiCuentas.cuentaLink(contexto, "0140000701100000184292").get();
		PlazoFijoWeb datos = post(contexto, cliente, cuentaLink, new BigDecimal("6000"), 36, PlazoFijoWeb.PESOS);
		imprimirResultado(contexto, datos);
	}
}
