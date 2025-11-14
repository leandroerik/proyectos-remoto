package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class CancelacionAnticipada extends ApiObjeto {
	// Aclaración: No tengo caso ni en desa ni en homo. Lo paso para que esté pero
	// no veo que se esté usando

	/* ========== ATRIBUTOS ========== */
	public String fechaDesde;
	public String fechaHasta;
	public Integer operacion;
	public BigDecimal tasaCan;
	public BigDecimal teaCan;

	public Fecha fechaDesde() {
		return new Fecha(fechaDesde, "dd/MM/yyyy");
	}

	public Fecha fechaHasta() {
		return new Fecha(fechaHasta, "dd/MM/yyyy");
	}

	public static class SolicitudCancelacionAnticipadaEstado extends ApiObjeto {
		String email;
		String enfechaDeSolicitud;
		String fechaCancelacion;
		String fechaFinCanAnt;
		String fechaInicioCanAnt;
		BigDecimal interesCancelacionAnt;
		BigDecimal monto;
		String nombre;
		String permiteSolicitud;
		String solicitudCargada;
		BigDecimal tasaCancelacionAnt;
		BigDecimal teaCancelacionAnt;

		public Fecha fechaCancelacion() {
			return new Fecha(fechaCancelacion, "dd/MM/yyyy");
		}

		public Fecha fechaFinCanAnt() {
			return new Fecha(fechaFinCanAnt, "dd/MM/yyyy");
		}

		public Fecha fechaInicioCanAnt() {
			return new Fecha(fechaInicioCanAnt, "dd/MM/yyyy");
		}
	}

	public class SolicitudPrecancelar extends ApiObjeto {
		public String estado;
		public String fecha;
		public String fechaCancelacion;
		public String fechaReal;
		public String fechaUltMod;
		public BigDecimal interesCanAnt;
		public Integer operacion;
		public BigDecimal teaCan;
		public String tipoIngreso;
		public String usuario;
		public String usuarioUltMod;

		public Fecha fecha() {
			return new Fecha(fecha, "dd/MM/yyyy");
		}

		public Fecha fechaCancelacion() {
			return new Fecha(fechaCancelacion, "dd/MM/yyyy");
		}

		public Fecha fechaReal() {
			return new Fecha(fechaReal, "dd/MM/yyyy");
		}

		public Fecha fechaUltMod() {
			return new Fecha(fechaUltMod, "dd/MM/yyyy");
		}
	}

	/* ========== SERVICIOS ========== */
	public static CancelacionAnticipada get(Contexto contexto, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosGetCancelacionAnticipada", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada", contexto);
		request.path("nroOperacion", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CancelacionAnticipada.class);
	}

	public static SolicitudPrecancelar getSolicitud(Contexto contexto, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosGetCancelacionAnticipada", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud", contexto);
		request.path("nroOperacion", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudPrecancelar.class);
	}

	public static SolicitudCancelacionAnticipadaEstado getEstado(Contexto contexto, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosGetCancelacionAnticipadaEstado", "plazosfijos", "GET", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud/estado", contexto);
		request.path("nroOperacion", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudCancelacionAnticipadaEstado.class);
	}

	public static SolicitudCancelacionAnticipadaEstado post(Contexto contexto, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosPostCancelacionAnticipada", "plazosfijos", "POST", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud", contexto);
		request.body("nroOperacion", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudCancelacionAnticipadaEstado.class);
	}

	public static SolicitudCancelacionAnticipadaEstado patch(Contexto contexto, String nroOperacion) {
		ApiRequest request = new ApiRequest("PlazosFijosPostCancelacionAnticipada", "plazosfijos", "PATCH", "/v1/plazosfijos/{nroOperacion}/cancelacionanticipada/solicitud", contexto);
		request.body("nroOperacion", nroOperacion);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudCancelacionAnticipadaEstado.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CancelacionAnticipada datos = get(contexto, "4998590015391523");
		imprimirResultado(contexto, datos);
	}
}
