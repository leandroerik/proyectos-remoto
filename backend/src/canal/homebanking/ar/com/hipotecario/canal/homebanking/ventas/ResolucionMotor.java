package ar.com.hipotecario.canal.homebanking.ventas;

import java.util.List;

public class ResolucionMotor {

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public Object DocumentacionSolicitud;
	public Object ProductosOfrecidos;
	public String TipoProducto;
	public Object IdProductoFrontEnd;
	public Object TipoOperacion;
	public Object Advertencias;
	public String Explicacion;
	public String CodigoExplicacion;
	public String FechaVigencia;
	public Object UltimaInstancia;
	public String FechaUltimaInstancia;
	public Boolean FlagExcepcion;
	public String MotivoExcepcion;
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
	public Double IngresoComputado;
	public String EsquemaEvaluacion;
	public String CodigoDistribucionAdicionalesTc;
	public Object IndicadorBase;
	public List<IntegrantesScoring> IntegrantesScoring;
	public Boolean SolicitaMontoRefuerzo;
	public Boolean FlagSimulacion;
	public Boolean FlagSolicitaAprobacionCentralizada;
	public Boolean FlagSolicitaValidarIdentidad;
	public Boolean FlagSolicitaComprobarIngresos;
	public Boolean FlagSolicitaAprobacionEstandard;
	public Boolean FlagSolicitaExcepcion;
	public Object CodigoMotivoExcepcion;
	public Object CodigoActualizacionInformes;
	public Boolean FlagSolicitaEvaluarMercadoAbierto;
	public Object SituacionLaboral;
	public Object FechaInicio;
	public Object IngresosMensuales;
	public Object CategoriaMonotributo;
	public Object FechaCategoriaMonotributo;
	public Object FechaDDJJGanancias;
	public Object IngresosDDJJGanancias;
	public Object EsPlanSueldo;
	public Boolean RechazadoMotor;
	public String DerivarA;
	public Object IdPaqueteProductos;
	public Object BuhoBank;
	public Object TipoInvocacion;
	public Object NroInstancia;
	public Double MontoRefuerzo;
	public Boolean EsOfertaMejorableComprobandoIngresos;

	public static class IntegrantesScoring {
		public String Id;
		public Object Documentaciones;
		public Integer Secuencia;
		public String NumeroTributario;
		public Boolean Preaprobado;
		public Boolean IndOk;
		public Object RiesgoNetHTML;
		public Boolean SolicitarIngresosPreaprobado;
		public Object DdjjAceptada;
		public String Explicacion;
		public Object VerazHTML;
		public Object NosisHTML;
		public List<Object> IngresosInferidos;
	}

	/* ========== METODOS ========== */
	public Boolean esVerde() {
		return "AV".equals(this.ResolucionId);
	}

	public Boolean esAprobadoAmarillo() {
		return "AA".equals(this.ResolucionId);
	}

	public Boolean esAmarillo() {
		return "CT".equals(this.ResolucionId);
	}

	public Boolean esSAmarillo() {
		return "S".equals(this.ResolucionId);
	}

	public Boolean esRojo() {
		return "RE".equals(this.ResolucionId);
	}
}
