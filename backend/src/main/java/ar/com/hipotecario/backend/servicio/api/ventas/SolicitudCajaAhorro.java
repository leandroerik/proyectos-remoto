package ar.com.hipotecario.backend.servicio.api.ventas;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.CuentaLegal;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.DomicilioResumen;

public class SolicitudCajaAhorro extends ApiObjeto {

	public static String GET_CAJA_AHORRO = "CajaAhorro";
	public static String PUT_CAJA_AHORRO = "ActualizarCajaAhorro";

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String tipoProducto;
	public String IdProductoFrontEnd;
	public String TipoOperacion;
	public String Moneda;
	public String NombreCuenta;
	public Boolean DepositoCheques;
	public Boolean DepositoInicial;
	public Object MontoDepositoInicial;
	public String TipoPromedio;
	public String TipoCapitalizacion;
	public Boolean DepositoChequesGranel;
	public Boolean CuentaGastosPropia;
	public String TipoCuentaGastos;
	public Object NumeroCuentaGastos;
	public String Iva;
	public Boolean IvaExencion;
	public Boolean IvaVencimientoExencion;
	public Boolean IvaReduccion;
	public Boolean IvaVencimientoReduccion;
	public String Ganancias;
	public Object GanExencion;
	public Object GanVencimientoExencion;
	public Boolean DocumentacionTributaria;
	public Object NumeroOperacion;
	public String Subtipo;
	public Object ProductoBancario;
	public String Categoria;
	public DomicilioResumen DomicilioResumen;
	public String Oficina;
	public String Oficial;
	public Boolean CobroPrimerMantenimiento;
	public String Origen;
	public String UsoFirma;
	public String Ciclo;
	public Boolean ResumenMagnetico;
	public Boolean TransfiereAcredHab;
	public List<RolIntegrante> Integrantes;
	public Object Advertencias;
	public Boolean RechazadoMotor;
	public String IdPaqueteProductos;
	public String Convenio;
	public Boolean ClientePlanSueldo;
	public CuentaLegal CuentaLegales;

	public static class NuevaSolicitudCajaAhorro {
		public String TipoOperacion;
		public String Moneda;
		public String Categoria;
		public String Subtipo;
		public String Oficial;
		public String Oficina;
		public Boolean CobroPrimerMantenimiento;
		public Boolean TransfiereAcredHab;
		public Boolean ResumenMagnetico;
		public String Origen;
		public String UsoFirma;
		public String Ciclo;
		public List<RolIntegrante> Integrantes;
		public CuentaLegal CuentaLegales;
		public DomicilioResumen DomicilioResumen;
		public String ProductoBancario;
	}

	/* ========== SERVICIOS ========== */

	// GET
	static SolicitudCajaAhorro get(Contexto contexto, String idSolicitud, String idProducto) {
		ApiRequest request = new ApiRequest(GET_CAJA_AHORRO, ApiVentas.API, "GET", "/solicitudes/{idSolicitud}/cajaAhorro/{idProducto}", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.path("idProducto", idProducto);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudCajaAhorro.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// POST
	static SolicitudCajaAhorro post(Contexto contexto, String idSolicitud, NuevaSolicitudCajaAhorro solicitudCajaAhorro) {
		ApiRequest request = new ApiRequest(GET_CAJA_AHORRO, ApiVentas.API, "POST", "/solicitudes/{idSolicitud}/cajaAhorro", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);

		if (solicitudCajaAhorro != null) {
			request.body("TipoOperacion", solicitudCajaAhorro.TipoOperacion);
			request.body("Moneda", solicitudCajaAhorro.Moneda);
			request.body("Categoria", solicitudCajaAhorro.Categoria);
			request.body("ProductoBancario", solicitudCajaAhorro.ProductoBancario);
			request.body("Subtipo", solicitudCajaAhorro.Subtipo);
			request.body("Oficial", solicitudCajaAhorro.Oficial);
			request.body("Oficina", solicitudCajaAhorro.Oficina);
			request.body("CobroPrimerMantenimiento", solicitudCajaAhorro.CobroPrimerMantenimiento);
			request.body("Origen", solicitudCajaAhorro.Origen);
			request.body("UsoFirma", solicitudCajaAhorro.UsoFirma);
			request.body("Ciclo", solicitudCajaAhorro.Ciclo);
			request.body("TransfiereAcredHab", solicitudCajaAhorro.TransfiereAcredHab);
			request.body("ResumenMagnetico", solicitudCajaAhorro.ResumenMagnetico);
			request.body("Integrantes", solicitudCajaAhorro.Integrantes);
			request.body("CuentaLegales", solicitudCajaAhorro.CuentaLegales);
			request.body("DomicilioResumen", solicitudCajaAhorro.DomicilioResumen);
		}
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudCajaAhorro.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// POST
	static SolicitudCajaAhorro postStand(Contexto contexto, String idSolicitud, NuevaSolicitudCajaAhorro solicitudCajaAhorro) {
		ApiRequest request = new ApiRequest(GET_CAJA_AHORRO, ApiVentas.API, "POST", "/solicitudes/{idSolicitud}/cajaAhorro", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);

		request.body("TipoOperacion", solicitudCajaAhorro.TipoOperacion);
		request.body("Moneda", solicitudCajaAhorro.Moneda);
		request.body("Integrantes", solicitudCajaAhorro.Integrantes);
		request.body("Categoria", solicitudCajaAhorro.Categoria);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudCajaAhorro.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// PUT
	static SolicitudCajaAhorro put(Contexto contexto, String idSolicitud, String idProducto, SolicitudCajaAhorro solicitudCajaAhorro) {
		ApiRequest request = new ApiRequest(PUT_CAJA_AHORRO, ApiVentas.API, "PUT", "/solicitudes/{idSolicitud}/cajaAhorro/{idProducto}", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.path("idProducto", idProducto);

		request.body("TipoOperacion", solicitudCajaAhorro.TipoOperacion);
		request.body("ProductoBancario", solicitudCajaAhorro.ProductoBancario);
		request.body("DomicilioResumen", solicitudCajaAhorro.DomicilioResumen);
		request.body("CuentaLegales", solicitudCajaAhorro.CuentaLegales);
		request.body("Oficial", solicitudCajaAhorro.Oficial);
		request.body("Oficina", solicitudCajaAhorro.Oficina);
		request.body("Origen", solicitudCajaAhorro.Origen);
		request.body("UsoFirma", solicitudCajaAhorro.UsoFirma);
		request.body("Ciclo", solicitudCajaAhorro.Ciclo);
		request.body("ResumenMagnetico", solicitudCajaAhorro.ResumenMagnetico);
		request.body("TransfiereAcredHab", solicitudCajaAhorro.TransfiereAcredHab);
		request.body("CobroPrimerMantenimiento", solicitudCajaAhorro.CobroPrimerMantenimiento);
		request.body("Moneda", solicitudCajaAhorro.Moneda);
		request.body("Categoria", solicitudCajaAhorro.Categoria);
		request.body("Subtipo", solicitudCajaAhorro.Subtipo);
		request.body("Integrantes", solicitudCajaAhorro.Integrantes);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudCajaAhorro.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	static SolicitudCajaAhorro postStandV2(Contexto contexto, String idSolicitud, SolicitudCajaAhorro solicitudCajaAhorro) {
		ApiRequest request = new ApiRequest(GET_CAJA_AHORRO, ApiVentas.API, "POST", "/solicitudes/{idSolicitud}/cajaAhorro", contexto);
		request.header(ApiVentas.X_HANDLE, idSolicitud);
		request.path("idSolicitud", idSolicitud);

		request.body("TipoOperacion", solicitudCajaAhorro.TipoOperacion);
		request.body("ProductoBancario", solicitudCajaAhorro.ProductoBancario);
		request.body("DomicilioResumen", solicitudCajaAhorro.DomicilioResumen);
		request.body("CuentaLegales", solicitudCajaAhorro.CuentaLegales);
		request.body("Oficial", solicitudCajaAhorro.Oficial);
		request.body("Oficina", solicitudCajaAhorro.Oficina);
		request.body("Origen", solicitudCajaAhorro.Origen);
		request.body("UsoFirma", solicitudCajaAhorro.UsoFirma);
		request.body("Ciclo", solicitudCajaAhorro.Ciclo);
		request.body("ResumenMagnetico", solicitudCajaAhorro.ResumenMagnetico);
		request.body("TransfiereAcredHab", solicitudCajaAhorro.TransfiereAcredHab);
		request.body("CobroPrimerMantenimiento", solicitudCajaAhorro.CobroPrimerMantenimiento);
		request.body("Moneda", solicitudCajaAhorro.Moneda);
		request.body("Categoria", solicitudCajaAhorro.Categoria);
		request.body("Subtipo", solicitudCajaAhorro.Subtipo);
		request.body("Integrantes", solicitudCajaAhorro.Integrantes);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudCajaAhorro.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");

		String test = "put";

		if (test.equals("post")) {

			String idSolicitud = "30440033";

			DomicilioResumen domicilioResumen = new DomicilioResumen();
			domicilioResumen.Tipo = "DP";

			RolIntegrante rolIntegrante = new RolIntegrante();
			rolIntegrante.Rol = "T";
			rolIntegrante.NumeroDocumentoTributario = "27322895556";
			rolIntegrante.IdCobis = null;

			List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
			integrantes.add(rolIntegrante);

			CuentaLegal cuentaLegal = new CuentaLegal();
			cuentaLegal.Uso = "PER";

			NuevaSolicitudCajaAhorro scAhorro = new NuevaSolicitudCajaAhorro();
			scAhorro.TipoOperacion = "03";
			scAhorro.Moneda = "80";
			scAhorro.Categoria = "CGU";
			scAhorro.Subtipo = "1";
			scAhorro.DomicilioResumen = domicilioResumen;
			scAhorro.Oficial = "0";
			scAhorro.CobroPrimerMantenimiento = false;
			scAhorro.Origen = "29";
			scAhorro.UsoFirma = "U";
			scAhorro.Ciclo = "6";
			scAhorro.ProductoBancario = "3";
			scAhorro.TransfiereAcredHab = false;
			scAhorro.ResumenMagnetico = false;
			scAhorro.Integrantes = integrantes;
			scAhorro.CuentaLegales = cuentaLegal;

			SolicitudCajaAhorro respuesta = post(contexto, idSolicitud, scAhorro);
			imprimirResultadoApiVentas(contexto, respuesta);
		} else if (test.equals("put")) {
			String idSolicitud = "30449452";
			String idProducto = "12664251"; // Id de la caja de ahorro

			DomicilioResumen domicilioResumen = new DomicilioResumen();
			domicilioResumen.Tipo = "DP";

			RolIntegrante rolIntegrante = new RolIntegrante();
			rolIntegrante.Rol = "T";
			rolIntegrante.NumeroDocumentoTributario = "23349761114";
			rolIntegrante.IdCobis = null;

			List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
			integrantes.add(rolIntegrante);

			CuentaLegal cuentaLegal = new CuentaLegal();
			cuentaLegal.Uso = "PER";

			SolicitudCajaAhorro scAhorro = new SolicitudCajaAhorro();
			scAhorro.TipoOperacion = "03";
			scAhorro.Moneda = "80";
			scAhorro.Categoria = "CGU";
			scAhorro.Subtipo = "1";
			scAhorro.Oficial = "1";
			scAhorro.CobroPrimerMantenimiento = false;
			scAhorro.DomicilioResumen = domicilioResumen;
			scAhorro.Origen = "29";
			scAhorro.UsoFirma = "U";
			scAhorro.Ciclo = "6";
			scAhorro.ProductoBancario = "3";
			scAhorro.TransfiereAcredHab = false;
			scAhorro.ResumenMagnetico = false;
			scAhorro.Integrantes = integrantes;
			scAhorro.CuentaLegales = cuentaLegal;

			SolicitudCajaAhorro respuesta = put(contexto, idSolicitud, idProducto, scAhorro);
			imprimirResultadoApiVentas(contexto, respuesta);
		}

	}
}
