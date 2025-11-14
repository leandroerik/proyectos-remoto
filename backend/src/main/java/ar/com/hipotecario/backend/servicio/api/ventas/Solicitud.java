package ar.com.hipotecario.backend.servicio.api.ventas;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Lista;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;

public class Solicitud extends ApiObjeto {

	public static String GET_SOLICITUD = "Solicitud";
	public static String GET_SOLICITUD_ESTADO = "SolicitudEstado";
	public static String POST_SOLICITUD = "CrearSolicitud";
	public static String PUT_SOLICITUD = "ActualizarSolicitud";

	/* ========== ENUM ========== */
	public static String PRESTAMO_HIPOTECARIO = "1";
	public static String PRESTAMO_PERSONAL = "2";
	public static String TARJETA_CREDITO = "5";
	public static String CUENTA_CORRIENTE_1 = "6";
	public static String CUENTA_CORRIENTE_2 = "7";
	public static String CAJA_AHORRO_PESOS = "8";
	public static String CAJA_AHORRO_DOLARES = "9";
	public static String TARJETA_DEBITO = "11";
	public static String PAQUETE = "32";

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String IdSolicitud;
	public String TipoOperacion;
	public String Oficina;
	public String CanalOriginacion1;
	public String CanalOriginacion2;
	public String CanalOriginacion3;
	public CanalOriginacion CanalOriginacionNivel3;
	public String CanalVenta1;
	public String CanalVenta2;
	public String CanalVenta3;
	public String CanalVenta4;
	public CanalVenta CanalVentaNivel4;
	public Object Observaciones;
	public String Estado;
	public Fecha FechaAlta;
	public Object Advertencias;
	public Lista<SolicitudProducto> Productos;
	public Object FlagSimulacion;
	public Object Simulacion;
	public Object Formularios;
	public Boolean PermiteImpresionDDJJAnses;
	public String CanalVenta;
	public String OficialVenta;
	public List<RolIntegrante> Integrantes;
	public Boolean Finalizada;
	public String OrigenDocumentacion;
	public String ResolucionCodigo;
	public String DerivarA;
	public String Campania;
	public Boolean DerivaAFraude;
	public Boolean TieneLegajoFisico;
	public Boolean ControlMuestral;
	public Boolean DocumentacionEnContenedor;
	public String ModoAprobacionId;

	public static class NuevaSolicitud {
		public String TipoOperacion;
		public String CanalOriginacion1;
		public String CanalOriginacion2;
		public String CanalOriginacion3;
		public String CanalVenta1;
		public String CanalVenta2;
		public String CanalVenta3;
		public String CanalVenta4;
		public String Oficina;
	}

	public static class CanalOriginacion extends ApiObjeto {
		public String Id;
		public String Nombre;
		public String Apellido;
		public String Nivel2;
		public String IdDocbis;
		public Object Advertencias;
	}

	public static class CanalVenta extends ApiObjeto {
		public String Id;
		public String Nombre;
		public String Apellido;
		public String Empresa;
		public String Funcionario;
		public String Estado;
		public Object CBU;
		public String Nivel3;
		public Object Advertencias;
	}

	public static class SolicitudProducto extends ApiObjeto {
		public String Id;
		public String tipoProducto;
		public String IdProductoFrontEnd;
		public String TipoOperacion;
		public String Oficina;
		public String Producto;
		public Boolean Validado;
		public String Oficial;
		public Object Moneda;
		public List<RolIntegrante> Integrantes;
		public Object Advertencias;
		public Boolean RechazadoMotor;
		public String IdPaqueteProductos;
		public Object MontoAprobado;
		public Object ModoAprobacionId;
		public Object ConSeguroVida;
		public Object IdProductoPadre;
		public Boolean EsMicrocredito;
		public String Nemonico;
		public String SubProducto;
		public String DestinoFondos;
		public Object DesembolsosPropuestos;
		public Object NroDesembolsosPropuestos;
		public Object NroUltimoDesembolsoLiquidado;
	}

	/* ========== SERVICIOS ========== */
	// solicitudesGET
	public static Solicitud get(Contexto contexto, String numeroSolicitud) {
		ApiRequest request = new ApiRequest(GET_SOLICITUD, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Solicitud.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	public static Solicitud getEstado(Contexto contexto, String numeroSolicitud, String estado) {
		ApiRequest request = new ApiRequest(GET_SOLICITUD_ESTADO, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}?estado={estado}&ejecutamotor=0", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.path("estado", estado);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Solicitud.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// solicitudesPOST
	public static Solicitud post(Contexto contexto, NuevaSolicitud nuevaSolicitud) {
		ApiRequest request = new ApiRequest(POST_SOLICITUD, ApiVentas.API, "POST", "/solicitudes", contexto);
		request.body("TipoOperacion", nuevaSolicitud.TipoOperacion);
		// TODO: Obtener de forma generica por canal
		request.body("CanalOriginacion1", nuevaSolicitud.CanalOriginacion1);
		request.body("CanalOriginacion2", nuevaSolicitud.CanalOriginacion2);
		request.body("CanalOriginacion3", nuevaSolicitud.CanalOriginacion3);
		request.body("CanalVenta1", nuevaSolicitud.CanalVenta1);
		request.body("CanalVenta2", nuevaSolicitud.CanalVenta2);
		request.body("CanalVenta3", nuevaSolicitud.CanalVenta3);
		request.body("CanalVenta4", nuevaSolicitud.CanalVenta4);
		request.body("Oficina", nuevaSolicitud.Oficina);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Solicitud.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	// solicitudesPUT - contenedor
	public static Solicitud put(Contexto contexto, Solicitud solicitud) {
		ApiRequest request = new ApiRequest(PUT_SOLICITUD, ApiVentas.API, "PUT", "/solicitudes/" + solicitud.IdSolicitud, contexto);
		request.header(ApiVentas.X_HANDLE, solicitud.IdSolicitud);
		request.body("TipoOperacion", solicitud.TipoOperacion);
		request.body("CanalOriginacion1", solicitud.CanalOriginacion1);
		request.body("CanalOriginacion2", solicitud.CanalOriginacion2);
		request.body("CanalOriginacion3", solicitud.CanalOriginacion3);
		request.body("CanalVenta1", solicitud.CanalVenta1);
		request.body("CanalVenta2", solicitud.CanalVenta2);
		request.body("CanalVenta3", solicitud.CanalVenta3);
		request.body("CanalVenta4", solicitud.CanalVenta4);
		request.body("Oficina", solicitud.Oficina);
		request.body("TieneLegajoFisico", solicitud.TieneLegajoFisico);
		request.body("OrigenDocumentacion", solicitud.OrigenDocumentacion);
		request.body("DocumentacionEnContenedor", solicitud.DocumentacionEnContenedor);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Solicitud.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== PRODUCTOS ========== */
	public SolicitudProducto paquete() {
		return Productos.first(p -> PAQUETE.equals(p.IdProductoFrontEnd));
	}

	public SolicitudProducto cajaAhorroPesos() {
		return Productos.first(p -> CAJA_AHORRO_PESOS.equals(p.IdProductoFrontEnd));
	}
	
	public SolicitudProducto cajaAhorroDolares() {
		return Productos.first(p -> CAJA_AHORRO_DOLARES.equals(p.IdProductoFrontEnd));
	}

	public SolicitudProducto tarjetaDebito() {
		return Productos.first(p -> TARJETA_DEBITO.equals(p.IdProductoFrontEnd));
	}

	/* ========== DETALLE PRODUCTOS ========== */
	public SolicitudPaquete detallePaquete(Contexto contexto) {
		SolicitudProducto paquete = paquete();
		return paquete != null ? ApiVentas.solicitudPaquete(contexto, Id, paquete.Id).get() : null;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String test = "get";

		if (test.equals("get")) {
			Solicitud datos = get(contexto, "30388995");
			imprimirResultadoApiVentas(contexto, datos);
		}

		if (test.equals("gete")) {
			Solicitud datos = getEstado(contexto, "30388995", "validar");
			imprimirResultadoApiVentas(contexto, datos);
		}

		if (test.equals("put")) {
			NuevaSolicitud nuevaSolicitud = new NuevaSolicitud();

			nuevaSolicitud.TipoOperacion = "03";
			nuevaSolicitud.CanalOriginacion1 = "40";
			nuevaSolicitud.CanalOriginacion2 = "1063";
			nuevaSolicitud.CanalOriginacion3 = "ESACCS";
			nuevaSolicitud.CanalVenta1 = "25";
			nuevaSolicitud.CanalVenta2 = "2211";
			nuevaSolicitud.CanalVenta3 = "2230";
			nuevaSolicitud.CanalVenta4 = "25001";
			nuevaSolicitud.Oficina = "0";

			Solicitud datos = post(contexto, nuevaSolicitud);
			imprimirResultadoApiVentas(contexto, datos);
		}

	}
}
