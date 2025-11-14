package ar.com.hipotecario.backend.servicio.api.ventas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Lista;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;

public class SolicitudPaquete extends ApiObjeto {

	public static String GET_SOLICITUD_PAQUETE = "SolicitudPaquete";
	public static String POST_SOLICITUD_PAQUETE = "CrearSolicitudPaquete";
	public static String DELETE_SOLICITUD_PAQUETE = "DeleteSolicitudPaquete";
	public static String CLIENTE_ELECCION_DISTRIBUCION = "CLIENTE";
	public static String BANCO_ELECCION_DISTRIBUCION = "BANCO";
	public static String ANDREANI_ELECCION_DISTRIBUCION = "ANDREANI";
	public static String TIPO_OPERACION_OPORTUNIDAD = "02";
	public static String TIPO_OPERACION_SOLICITUD = "03";

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public Boolean EjecutaMotor;
	public ResolucionPaquete Resolucion;
	public Paquete Paquete;
	public Object ProductosAdicionales;
	public Object Advertencias;
	public String TipoOperacion;

	/* ========== CLASES ========== */
	public static class ResolucionPaquete extends ApiObjeto {
		public Object Id;
		public String ResolucionCodigo;
		public String ResolucionDescripcion;
		public String DerivarA;
		public String Documentacion;
		public Object FlagSimulacion;
		public Object FlagSolicitaAprobacionCentralizada;
		public Object FlagSolicitaValidarIdentidad;
		public Object FlagSolicitaComprobarIngresos;
		public Object FlagSolicitaAprobacionEstandard;
		public Object FlagSolicitaExcepcion;
		public Object MotivoExcepcion;
		public Object CodigoActualizacionInformes;
		public Object FlagSolicitaEvaluarMercadoAbierto;
		public Object Advertencias;
		public String Explicacion;
		public String CodigoExplicacion;
	}

	public static class Paquete extends ApiObjeto {
		public String Id;
		public String TipoPaquete;
		public String ProductoBancario;
		public Object OrigenCaptacion;
		public String Oficina;
		public String Oficial;
		public Object UsoFirma;
		public String Ciclo;
		public Boolean ResumenMagnetico;
		public Object EleccionDistribucion;
		public Object EleccionDistribucionParam;
		public Object DestinoDistribucion;
		public String ProductoCobisCobro;
		public String ProductoBancarioCobro;
		public String MonedaCobro;
		public List<RolIntegrante> Integrantes;
		public DomicilioResumen DomicilioResumen;
		public ProductosNuevos ProductosNuevos = new ProductosNuevos();
		public Lista<ProductoExistente> ProductosExistentes = new Lista<>();
		public Object IncrementoLimite;
		public Object Advertencias;
	}

	public static class DomicilioResumen extends ApiObjeto {
		public String Tipo;
		public Integer SecuencialCobis;
	}

	public static class CuentaLegal extends ApiObjeto {
		public String Id;
		public String Uso;
		public String Transacciones;
		public Integer TransaccionesCantidad;
		public BigDecimal TransaccionesVolumen;
		public Boolean RealizaTransferencias;
		public Object TransferenciaPaisEnvio;
		public Object TransferenciaPaisRecibe;
		public Object TransferenciaTipo;
	}

	public static class ProductosNuevos extends ApiObjeto {
		public SolicitudCajaAhorro CajaAhorro;
		public SolicitudCajaAhorro CajaAhorroDolares;
		public SolicitudCuentaCorriente CuentaCorriente;
		public SolicitudTarjetaDebito TarjetaDebito;
		public SolicitudTarjetaCredito TarjetaCredito;
	}

	public static class ProductoExistente extends ApiObjeto {
		public String idProducto;
		public String productoFrontEnd;
		public String NumeroProducto;
		public List<RolIntegrante> Integrantes;
		public Object Marca;
		public String Producto;
		public Object Afinidad;
		public Object Oficina;
		public Object Letra;
		public Object FormaPago;
		public Object CuentaTipo;
		public Object CuentaNro;
		public Object CuentaMoneda;
		public String Emparejar;
		public Object Upgrade;
		public Object Recompensa;
		public Object NumeroCuenta;
		public Object AdicionalesTC;
	}

	/* ========== SERVICIOS ========== */
	// solicitudPaqueteGET
	public static SolicitudPaquete get(Contexto contexto, String numeroSolicitud, String idProducto) {
		ApiRequest request = new ApiRequest(GET_SOLICITUD_PAQUETE, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}/solicitudPaquete/{idProducto}", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.path("idProducto", idProducto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudPaquete.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// solicitudPaquetePOST
	public static SolicitudPaquete post(Contexto contexto, String numeroSolicitud, SolicitudPaquete solicitudPaquete) {
		ApiRequest request = new ApiRequest(POST_SOLICITUD_PAQUETE, ApiVentas.API, "POST", "/solicitudes/{numeroSolicitud}/solicitudPaquete", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.body("IdSolicitud", numeroSolicitud);
		request.body("TipoOperacion", solicitudPaquete.TipoOperacion);
		request.body("EjecutaMotor", solicitudPaquete.EjecutaMotor);
		request.body("Resolucion", solicitudPaquete.Resolucion);
		request.body("Paquete", solicitudPaquete.Paquete);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudPaquete.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// solicitudPaqueteDELETE
	public static SolicitudPaquete delete(Contexto contexto, String numeroSolicitud, String idProducto) {
		ApiRequest request = new ApiRequest(DELETE_SOLICITUD_PAQUETE, ApiVentas.API, "DELETE", "/solicitudes/{numeroSolicitud}/solicitudPaquete/{idProducto}", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.path("idProducto", idProducto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(SolicitudPaquete.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== CAJA AHORRO PESOS ========== */
	public Boolean tieneCajaAhorroPesos() {
		return cajaAhorroPesosNueva() != null || cajaAhorroPesosExistente() != null;
	}

	public SolicitudCajaAhorro cajaAhorroPesosNueva() {
		return Paquete.ProductosNuevos.CajaAhorro;
	}

	public ProductoExistente cajaAhorroPesosExistente() {
		return Paquete.ProductosExistentes.first(p -> Solicitud.CAJA_AHORRO_PESOS.equals(p.productoFrontEnd));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String test = "post";

		if (test.equals("get")) {
			SolicitudPaquete datos = get(contexto, "30357158", "12610395");
			imprimirResultadoApiVentas(contexto, datos);
		}

		if (test.equals("post")) {
			RolIntegrante rolIntegrante = new RolIntegrante();
			rolIntegrante.Rol = "T";
			rolIntegrante.NumeroDocumentoTributario = "20235684900";
			rolIntegrante.IdCobis = null;

			List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
			integrantes.add(rolIntegrante);

			DomicilioResumen domicilioResumen = new DomicilioResumen();
			domicilioResumen.Tipo = "DP";

			SolicitudTarjetaCredito tarjetaCredito = new SolicitudTarjetaCredito();
			tarjetaCredito.Embozado = "JUAN/MARTIN";
			tarjetaCredito.Integrantes = integrantes;
			tarjetaCredito.TipoOperacion = "02";

			ProductosNuevos productosNuevos = new ProductosNuevos();
			productosNuevos.TarjetaCredito = tarjetaCredito;

			Paquete paquete = new Paquete();
			paquete.ProductoBancario = "0";
			paquete.OrigenCaptacion = "10";
			paquete.Oficina = "0";
			paquete.Oficial = "1";
			paquete.UsoFirma = "U";
			paquete.Ciclo = "4";
			paquete.EleccionDistribucion = "CLIENTE";
			paquete.ProductoCobisCobro = "4";
			paquete.ProductoBancarioCobro = "3";
			paquete.Integrantes = integrantes;
			paquete.DomicilioResumen = domicilioResumen;
			paquete.ProductosNuevos = productosNuevos;

			SolicitudPaquete nuevoPaquete = new SolicitudPaquete();
			nuevoPaquete.TipoOperacion = "02";
			nuevoPaquete.Paquete = paquete;

			SolicitudPaquete datos = post(contexto, "30423866", nuevoPaquete);
			imprimirResultadoApiVentas(contexto, datos);
		}
	}
}
