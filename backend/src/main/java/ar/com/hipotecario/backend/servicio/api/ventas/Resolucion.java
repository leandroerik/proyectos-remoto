package ar.com.hipotecario.backend.servicio.api.ventas;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.IntegrantesScoring.IntegranteScoring;

public class Resolucion extends ApiObjeto {

	public static String PUT_RESOLUCION = "ActualizarResolucion";
	public static String GET_RESOLUCION = "ObtenerResolucion";

	/* ========== CONSTANTES ========== */
	public static String RECHAZAR = "RE";
	public static String CONTROLAR = "CT";
	public static String APROBAR_AMARILLO = "AA";
	public static String APROBAR_VERDE = "AV";
	public static String CONTINUAR = "CO";
	public static String SC = "SC";

	/* ========== ATRIBUTOS ========== */
	public Object DocumentacionSolicitud;
	public Object ProductosOfrecidos;
	public String tipoProducto;
	public Object IdProductoFrontEnd;
	public Object TipoOperacion;
	public Object Advertencias;
	public String Explicacion;
	public String CodigoExplicacion;
	public Fecha FechaVigencia;
	public Object UltimaInstancia;
	public Fecha FechaUltimaInstancia;
	public Boolean FlagExcepcion;
	public Object MotivoExcepcion;
	public String MotivoExcepcionMotor;
	public String Documentacion;
	public String Productos;
	public String Observacion;
	public String ResolucionId;
	public String ResolucionDesc;
	public String ModoAprobacionId;
	public String ModoAprobacionDesc;
	public String EsquemaEvaluacionId;
	public String EsquemaEvaluacionDesc;
	public Boolean ControlEtapa4;
	public String MotivoControlEtapa4;
	public BigDecimal IngresoComputado;
	public BigDecimal CompMensual;
	public String GrupoRiesgo;
	public String EsquemaEvaluacion;
	public String CodigoDistribucionAdicionalesTc;
	public Object IndicadorBase;
	public List<IntegranteScoring> IntegrantesScoring;
	public Boolean SolicitaMontoRefuerzo;
	public Boolean FlagSimulacion;
	public Boolean FlagSolicitaAprobacionCentralizada;
	public Boolean FlagSolicitaValidarIdentidad;
	public Boolean FlagSolicitaComprobarIngresos;
	public Boolean FlagSolicitaExcepcionCRM;
	public Boolean FlagSolicitaAprobacionEstandard;
	public Boolean FlagSolicitaExcepcion;
	public Object CodigoMotivoExcepcion;
	public Object CodigoActualizacionInformes;
	public Boolean FlagSolicitaEvaluarMercadoAbierto;
	public Boolean FlagSolicitaExcepcionChequeoFinal;
	public Object SituacionLaboral;
	public Fecha FechaInicio;
	public Object IngresosMensuales;
	public Object CategoriaMonotributo;
	public Fecha FechaCategoriaMonotributo;
	public Fecha FechaDDJJGanancias;
	public Object IngresosDDJJGanancias;
	public Object EsPlanSueldo;
	public Boolean RechazadoMotor;
	public Boolean ControlMuestral;
	public String DerivarA;
	public Object IdPaqueteProductos;
	public Object BuhoBank;
	public Object TipoInvocacion;
	public Object NroInstancia;
	public BigDecimal MontoRefuerzo;
	public Boolean EsOfertaMejorableComprobandoIngresos;
	public String Id;

	/* ========== CLASES ========== */
	public static class NuevaResolucion extends ApiObjeto {
		public String TipoOperacion;
		public Boolean FlagExcepcion;
		public String MotivoExcepcion;
		public Boolean SolicitaMontoRefuerzo;
		public String CodigoMotivoExcepcion;
		public String EsPlanSueldo;
		public Boolean FlagSimulacion;
		public Boolean FlagSolicitaAprobacionCentralizada;
		public Boolean FlagSolicitaValidarIdentidad;
		public Boolean FlagSolicitaComprobarIngresos;
		public Boolean FlagSolicitaExcepcionCRM;
		public Boolean FlagSolicitaAprobacionEstandard;
		public Boolean FlagSolicitaExcepcion;
		public Boolean FlagSolicitaEvaluarMercadoAbierto;
		public Boolean FlagSolicitaExcepcionChequeoFinal;
		public BuhoBank BuhoBank;
		public String TipoInvocacion;
		public Integer NroInstancia;
	}

	public static class BuhoBank extends ApiObjeto {
		public BigDecimal ingresoNeto;
		public String situacionLaboral;
	}

	/* ========== SERVICIOS ========== */
	// resolucionesPUT
	public static Resolucion put(Contexto contexto, String numeroSolicitud, NuevaResolucion nuevaResolucion) {
		ApiRequest request = new ApiRequest(PUT_RESOLUCION, ApiVentas.API, "PUT", "/solicitudes/{numeroSolicitud}/resoluciones", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.bodyIfNotNull("IdSolicitud", numeroSolicitud);
		request.bodyIfNotNull("TipoOperacion", nuevaResolucion.TipoOperacion);
		request.bodyIfNotNull("MotivoExcepcion", nuevaResolucion.MotivoExcepcion);
		request.bodyIfNotNull("CodigoMotivoExcepcion", nuevaResolucion.CodigoMotivoExcepcion);
		request.body("SolicitaMontoRefuerzo", nuevaResolucion.SolicitaMontoRefuerzo);
		request.body("BuhoBank", nuevaResolucion.BuhoBank);
		request.body("TipoInvocacion", nuevaResolucion.TipoInvocacion);
		request.body("NroInstancia", nuevaResolucion.NroInstancia);
		request.body("EsPlanSueldo", nuevaResolucion.EsPlanSueldo);

		request.body("FlagExcepcion", nuevaResolucion.FlagExcepcion);
		request.body("FlagSimulacion", nuevaResolucion.FlagSimulacion);
		request.body("FlagSolicitaAprobacionCentralizada", nuevaResolucion.FlagSolicitaAprobacionCentralizada);
		request.body("FlagSolicitaValidarIdentidad", nuevaResolucion.FlagSolicitaValidarIdentidad);
		request.body("FlagSolicitaComprobarIngresos", nuevaResolucion.FlagSolicitaComprobarIngresos);
		request.body("FlagSolicitaExcepcionCRM", nuevaResolucion.FlagSolicitaExcepcionCRM);
		request.body("FlagSolicitaAprobacionEstandard", nuevaResolucion.FlagSolicitaAprobacionEstandard);
		request.body("FlagSolicitaExcepcion", nuevaResolucion.FlagSolicitaExcepcion);
		request.body("FlagSolicitaEvaluarMercadoAbierto", nuevaResolucion.FlagSolicitaEvaluarMercadoAbierto);
		request.body("FlagSolicitaExcepcionChequeoFinal", nuevaResolucion.FlagSolicitaExcepcionChequeoFinal);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Resolucion.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	public static Resolucion get(Contexto contexto, String numeroSolicitud) {
		ApiRequest request = new ApiRequest(GET_RESOLUCION, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}/resoluciones", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Resolucion.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== METODOS ========== */
	public Boolean aprobado() {
		if (empty(ResolucionId))
			return false;
		return ResolucionId.equals(APROBAR_AMARILLO) || ResolucionId.equals(APROBAR_VERDE);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String test = "put";

		if ("put".equals(test)) {
			String idSolicitud = "30423866";

			NuevaResolucion nuevaResolucion = new NuevaResolucion();
			nuevaResolucion.TipoOperacion = "02";

			Resolucion datos = put(contexto, idSolicitud, nuevaResolucion);
			imprimirResultadoApiVentas(contexto, datos);
		}
	}
}
