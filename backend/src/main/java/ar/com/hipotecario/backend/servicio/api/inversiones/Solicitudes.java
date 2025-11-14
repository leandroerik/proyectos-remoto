package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Solicitudes.Solicitud;

public class Solicitudes extends ApiObjetos<Solicitud> {

	/* ========== ATRIBUTOS ========== */
	public static class Solicitud extends ApiObjeto {
		public String CantCuotapartes;
		public String CuotapartistaID;
		public String CuotapartistaNombre;
		public Integer CuotapartistaNumero;
		public boolean EsTotal;
		public String EstadoSolicitud;
		public String FechaAcreditacion;
		public String FechaConcertacion;
		public String FondoID;
		public String FondoNombre;
		public String FondoNombreAbr;
		public Integer FondoNumero;
		public String IDSolicitud;
		public String Importe;
		public String MonedaCodISO;
		public String MonedaDescripcion;
		public String MonedaID;
		public String MonedaSimbolo;
		public Integer NumSolicitud;
		public String OrigenSolicitud;
		public String SucursalDescripcion;
		public String SucursalID;
		public Integer SucursalNumero;
		public String TipoSolicitud;
		public String TipoVCPAbreviatura;
		public String TipoVCPDescripcion;
		public String TipoVCPID;
		public String TipoVCPIDCafci;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaSolicitudes
	public static Solicitudes obtenerSolicitudes(Contexto contexto, String fechaDesde, String fechaHasta, String idAgColocador, String idCuotapartista, String idFondo, String idTpValorCp, String idUsuario, Integer numeroCuotapartista, String nombre) {
		ApiRequest request = new ApiRequest("obtenerSolicitudes", "inversiones", "POST", "/v1/solicitudes", contexto);
		request.body("pSolicitudSuscripcion.FechaDesde", fechaDesde);
		request.body("pSolicitudSuscripcion.FechaHasta", fechaHasta);
		request.body("pSolicitudSuscripcion.IDAgColocador", idAgColocador);
		request.body("pSolicitudSuscripcion.IDCuotapartista", idCuotapartista);
		request.body("pSolicitudSuscripcion.IDFondo", idFondo);
		request.body("pSolicitudSuscripcion.IDTpValorCp", idTpValorCp);
		request.body("pSolicitudSuscripcion.IDUsuario", idUsuario);
		request.body("pSolicitudSuscripcion.NumeroCuotapartista", numeroCuotapartista);
		request.body("nombre", nombre);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Solicitudes.class, response.objeto("SolicitudGenericoModel").objetos());

	}
}
