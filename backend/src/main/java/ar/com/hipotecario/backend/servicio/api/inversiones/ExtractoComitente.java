package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ExtractoComitente extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String agenteLiquidacionYCompensacion;
	public String depositanteCajaDeValores;
	public String numeroAgenteMercado;
	public Cuenta cuenta;
	public Paginacion paginacion;
	public List<IntervinientesCobis> intervinientesCobis;
	public List<Operaciones> operaciones;

	public static class Especie {
		public String descripcion;
		public String codigo;
	}

	public static class Cuenta {
		public String numero;
		public String descripcion;
		public String codigoTipoRelacion;
		public String tipoRelacion;
		public String situacion;
	}

	public static class Paginacion {
		public String totalRegistrosPaginacion;
		public String ultimoSecuencialPaginacion;
		public Boolean existenMasRegistrosPaginacion;
	}

	public static class IntervinientesCobis {
		public String idCobis;
		public String direccionPostalCobis;
		public String telefonoDireccionPostalCobis;
		public String domicilioIDTelefonoCobis;
	}

	public static class Operaciones {
		public Especie especie;
		public String agenteLiquidacionYCompensacion;
		public String ordenSecuencial;
		public String idOperacion;
		public String tipo;
		public String numeroMinuta;
		public String numeroBoleto;
		public String cupon;
		public String plazo;
		public String totalLetrasML;
		public String totalLetrasME;
		public String unidadPlazo;
		public String cuentaLiquidacionME;
		public String cuentaCustodia;
		public String sucursal;
		public String horaOrden;
		public String contraparte;
		public String mercado;
		public String estado;
		public String cuentaLiquidacionML;
		public String moneda;
		public Fecha fechaConcertacion;
		public Fecha fechaVencimiento;
		public Fecha fechaPago;
		public Fecha fechaOrden;
		public BigDecimal valorResidual;
		public BigDecimal renta;
		public BigDecimal bruto;
		public BigDecimal comision;
		public BigDecimal aranceles;
		public BigDecimal arancelesML;
		public BigDecimal rentaME;
		public BigDecimal amortizacionME;
		public BigDecimal arancelesME;
		public BigDecimal ivaRI;
		public BigDecimal ivaRNI;
		public BigDecimal totalML;
		public BigDecimal totalME;
		public BigDecimal amortizacion;
		public BigDecimal rentaML;
		public BigDecimal amortizacionML;
		public BigDecimal precio;
		public Integer cantidadNominal;
		public Integer cantidadResidual;
		public Integer cantidadResidualActual;
		public Integer numeroOrdenInterno;
		public Integer numeroOrdenMercado;
		public Integer numeroRefMercado;
//		"derechos": null,
//		"acreditacion": null,
//		"revaluo": null,
//		"dividendoEnAcciones": null,
//		"dividendoEnEfectivo": null,
//		"centavosALiquidar": null,
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaExtractoComitente
	public static ExtractoComitente get(Contexto contexto, String cuenta, Fecha fechaInicio, Fecha fechaFin, String idCobis, String numeroSecuencial, String cantidadRegistro) {
		ApiRequest request = new ApiRequest("MovimientosTitulosValores", "inversiones", "GET", "/v1/{cuenta}/extractoComitente", contexto);
		request.path("cuenta", cuenta);
		request.query("fechaInicio", fechaInicio.string("dd/MM/yyyy"));
		request.query("fechaFin", fechaFin.string("dd/MM/yyyy"));
		request.query("idCobis", idCobis);
		request.query("numeroSecuencial", numeroSecuencial);
		request.query("cantidadRegistro", cantidadRegistro);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ExtractoComitente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaInicio = new Fecha("01/01/2019", "dd/MM/yyyy");
		Fecha fechaFin = new Fecha("01/01/2020", "dd/MM/yyyy");
		ExtractoComitente datos = get(contexto, "2-000108703", fechaInicio, fechaFin, "4373070", "1", "1");
		imprimirResultado(contexto, datos);
	}
}
