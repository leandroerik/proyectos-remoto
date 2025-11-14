package ar.com.hipotecario.backend.servicio.api.ventas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.DomicilioResumen;

public class SolicitudTarjetaCredito extends ApiObjeto {

	public static String GET_TARJETA_CREDITO = "TarjetaCredito";

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String Embozado;
	public List<RolIntegrante> Integrantes;
	public String TipoOperacion;
	public Integer IdProductoFrontEnd;
	public Integer CarteraGrupo;
	public Boolean RechazadoMotor;
	public String IdPaqueteProductos;
	public String Letra;
	public Boolean AvisosViaMail;
	public Boolean AvisosCorreoTradicional;
	public MailAvisos MailAvisos;
	public String tipoProducto;
	public Integer Producto;
	public String Caracteristica;
	public Object TipoNegocio;
	public Integer Afinidad;
	public Integer ModeloLiquidacion;
	public Integer Distribucion;
	public DomicilioResumen Domicilio;
	public Object Telefono;
	public BigDecimal Limite;
	public Integer FormaPago;
	public String TipoCuenta;
	public Object TipoCuentaParam;
	public String NumeroCuenta;
	public String NumeroCuentaCorto;
	public String SucursalCuenta;
	public String MonedaCuenta;
	public String EmpresaAseguradora;
	public Object Advertencias;
	public Boolean esVirtual;
	public Boolean altaOnline;
	public String visualizaVirtual;

	/* ========== SERVICIOS ========== */
	// tarjetaCreditoGET
	public static SolicitudTarjetaCredito get(Contexto contexto, String numeroSolicitud, String idTarjeta) {
		ApiRequest request = new ApiRequest(GET_TARJETA_CREDITO, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}/tarjetaCredito/{idTarjeta}", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.path("idTarjeta", idTarjeta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudTarjetaCredito.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String test = "get";

		if (test.equals("get")) {
			SolicitudTarjetaCredito datos = get(contexto, "30424127", "12650649");
			imprimirResultadoApiVentas(contexto, datos);
		}
	}
}
