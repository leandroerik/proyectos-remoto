package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Microcreditos extends ApiObjeto {

	/* ========== ATRIBUTOS SOLICTUDES ========== */
	public static class Solicitudes extends ApiObjetos<Solicitud> {
	}

	public static class Solicitud extends ApiObjeto {
		public Boolean habilitadoSorteo;
	}

	/* ========== ATRIBUTOS PROCREAR ESTADISTICAS ========== */
	public static class ProcrearEstadisticas extends ApiObjeto {
		public Salida4 salida4;
		public List<Salida3> salida3;
		public List<Salida2> salida2;
		public List<Salida1> salida1;
	}

	public static class Procrear {
		public BigDecimal compraDeMateriales;
		public BigDecimal compraDeMaterialesHoy;
		public BigDecimal montoAprobado;
		public BigDecimal montoDesembolsado;
	}

	public static class Salida4 {
	}

	public static class Salida3 {
		public BigDecimal cantidadDeCreditos;
		public Fecha fecha;
	}

	public static class Salida2 extends Procrear {
		public Integer creditosOtorgados;
		public BigDecimal montoMaximoDeCredito;
		public String tipo;
	}

	public static class Salida1 extends Procrear {
		public BigDecimal cantidadDeCreditos;
		public Fecha fecha;
	}

	/* ========== ATRIBUTOS BENEFICIARIO ========== */
	public static class Beneficiarios extends ApiObjetos<Beneficiario> {
	}

	public static class Beneficiario extends ApiObjeto {
		public String idCobisTitular;
		public String nombreTitular;
		public String apellidoTitular;
		public String numeroIdentificacionTributariaTitular;
		public String nemonico;
		public String idCobisConyuge;
		public String nombreConyuge;
		public String apellidoConyuge;
		public String numeroIdentificacionTributariaConyuge;
		public String estado;
		public String codOferta;
		public String nombreOferta;
		public String descripcion;
		public Integer plazo;
		public BigDecimal tasa;
		public BigDecimal valorOferta;
		public BigDecimal valorCuota;
		public BigDecimal valorCFT;
	}

	/* ========== ATRIBUTOS INFORME VERAZ ========== */
	public static class InformesVeraz extends ApiObjeto {
		public List<EstadoProcrear> estado;
	}

	public static class EstadoProcrear {
		public String estado_procrear;
	}

	/* ========== ATRIBUTOS DETALLES ========== */
	public class Detalles extends ApiObjeto {
		public List<DetalleSolicitud> DetalleSolicitud;
		public List<DatosTitular> DatosTitular;
		public List<IngresosTitular> IngresosTitular;
		public List<DatosConyuge> DatosConyuge;
		public List<IngresosTitular> ingresosConyuge;
		public List<DatosSorteo> DatosSorteo;
	}

	public static class Titular {
		public String nombreTitular;
		public String apellidoTitular;
		public String cuilTitular;
	}

	public static class DetalleSolicitud extends Titular {
		public String codSolicitud;
		public String nombreConyuge;
		public String apellidoConyuge;
		public String motivoRechazo;
		public String cuilConyuge;
		public Fecha fechaAlta;
		public BigDecimal ingresosDeclarados;
		public Boolean habilitadoSorteo;
	}

	public static class DatosTitular extends Titular {
		public String codSolicitud;
		public String motivoRechazo;
		public BigDecimal ingresosSintys;
		public BigDecimal ingresosDeclarados;
		public Boolean habilitadoSorteo;
		public Fecha fechaAlta;
	}

	public static class IngresosTitular {
		public String anioRD;
		public String categoria;
		public String categoriaSintys;
		public String codIngresoAutonomo;
		public String codIngresoOtro;
		public String codInteresado;
		public String cuitEmpleador;
		public String documentoPresentar;
		public String esProfesional;
		public String fechaUltimoIngresoDDJJ;
		public String mesRD;
		public String monto;
		public String montoRD;
		public String origen;
		public String razonSocialEmpleador;
		public String razonSocialEmpleadorRD;
		public String vigente;
		public String ingresoBrutoAnual;
		public String ingresoCertificadoContador;
		public String ingresoFinalGanancias;
		public String periodicidad;
		public String anioIngresoJubilidado;
		public String codIngresoInteresado;
		public String codIngresoJubilado;
		public String codIngresoMonotributo;
		public String codIngresoRelacionDependencia;
		public String codValidacionInteresado;
		public Fecha fechaIngreso;
		public Fecha fechaUltimoIngresoGanancias;
		public String mesIngresoJubilidado;
		public String montoIngresoJubilidado;
	}

	public static class DatosConyuge {
		public String codSolicitud;
		public Boolean habilitadoSorteo;
		public BigDecimal ingresosDeclarados;
		public BigDecimal ingresosSintys;
		public Fecha fechaAlta;
		public String motivoRechazo;
		public String cuilConyuge;
		public String apellidoConyuge;
		public String nombreConyuge;
	}

	public static class DatosSorteo {
		public String codSolicitud;
		public String fechaSorteo;
		public String linea;
		public Boolean habilitadoOriginacion;
	}

	/* ========== SERVICIOS ========== */
	// Api-Prestamos_ConsultaSolicitudesLineasProCrear
	public static Solicitudes getSolicitudes(Contexto contexto, String cuil) {
		ApiRequest request = new ApiRequest("PrestamoSolicitudes", "prestamos", "GET", "/v1/prestamos/{cuil}/solicitudes", contexto);
		request.path("cuil", cuil);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Solicitudes.class);
	}

	// Api-Prestamos_ProcrearEstadistica
	public static ProcrearEstadisticas getProcrearEstadisticas(Contexto contexto) {
		ApiRequest request = new ApiRequest("PrestamoProcrearEstadistica", "prestamos", "GET", "/v1/prestamos/procrearestadistica", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ProcrearEstadisticas.class);
	}

	// Api-Prestamos_ConsultaBeneficiarioHabilitado
	public static Beneficiarios getBeneficiarios(Contexto contexto, String idCobis, String tipo) {
		ApiRequest request = new ApiRequest("PrestamoBeneficiarioHabilitado", "prestamos", "GET", "/v1/prestamos/{id}/beneficiario", contexto);
		request.path("id", idCobis);
		request.query("Tipo", tipo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Beneficiarios.class);
	}

	// Api-Prestamos_ConsultaSolicitudesLineasProCrear
	public static InformesVeraz getInformeVeraz(Contexto contexto, String cuit) {
		ApiRequest request = new ApiRequest("PrestamoInformeVeraz", "prestamos", "GET", "/v1/prestamos/{cuit}/informeveraz", contexto);
		request.path("cuit", cuit);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(InformesVeraz.class);
	}

	// Api-Prestamos_ConsultaDetalleSolicitudProCrear
	public static Detalles getDetalles(Contexto contexto, Integer codSolicitud) {
		ApiRequest request = new ApiRequest("PrestamoDetalle", "prestamos", "GET", "/v1/prestamos/{codSolicitud}/detalles", contexto);
		request.path("codSolicitud", codSolicitud.toString());
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Detalles.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String metodo = "getDetalles";

		if (metodo.equals("getProcrearEstadisticas")) {
			ProcrearEstadisticas datos = getProcrearEstadisticas(contexto);
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getProcrearEstadisticas")) {
			Solicitudes datos = getSolicitudes(contexto, "0170061912");
			// Solicitudes datos = get(contexto, "0690007944");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getBeneficiarios")) {
			Beneficiarios datos = getBeneficiarios(contexto, "8013371", "C");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getInformeVeraz")) {
			InformesVeraz datos = getInformeVeraz(contexto, "0690007944");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("getDetalles")) {
			Detalles datos = getDetalles(contexto, 0000331540);
			imprimirResultado(contexto, datos);
		}
	}
}
