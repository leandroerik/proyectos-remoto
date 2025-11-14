package ar.com.hipotecario.mobile.servicio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class RestAgenda {

//	private static DateTimeFormatter formatoSpaFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//	private static DateTimeFormatter formatoSpaHora = DateTimeFormatter.ofPattern("hh:mm:ss");

	public static ApiResponseMB habilitado(ContextoMB contexto, String cuil) {
		ApiRequestMB request = ApiMB.request("ConsultaCuilHabilitadoParaAgendar", "agendas", "GET", "/v1/status/{cuil}/habilitado", contexto);
		request.path("cuil", cuil);
		return ApiMB.response(request, cuil);
	}

	public static ApiResponseMB crearTurno(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1TurnoPost", "agendas", "POST", "/v1/turno", contexto);
		Objeto body = new Objeto();
		String cuil = contexto.persona().cuit();
		String nombre = contexto.persona().nombre();
		String sucursalDesc = "";
		Integer codSucursal = contexto.parametros.integer("codSucursal");
		for (Objeto sucursal : RestCatalogo.sucursales(contexto).objetos()) {
			if (sucursal.integer("CodSucursal", 0).equals(codSucursal)) {
				sucursalDesc = sucursal.string("DesSucursal");
			}
		}
		body.set("cuil", cuil);
		body.set("nombre", nombre);
		body.set("email", contexto.parametros.string("email"));
		body.set("idTurno", contexto.parametros.integer("idTurno"));
		body.set("fecha", contexto.parametros.string("fecha"));
		body.set("hora", contexto.parametros.string("hora"));
		body.set("operador", contexto.parametros.string("operador"));
		body.set("telefono", contexto.parametros.string("telefono"));
		body.set("codTipoReserva", contexto.parametros.integer("tipoTurno"));
		body.set("canal", 4);
		body.set("sucursalDesc", sucursalDesc);
		body.set("comentarios", contexto.parametros.string("comentarios"));
		request.body(body);
		return ApiMB.response(request);
	}

	public static ApiResponseMB obtenerTurnos(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("V1TurnoGet", "agendas", "GET", "/v1/turno", contexto);
		String cuil = contexto.persona().cuit();
		LocalDate fechaInicio = null;
		LocalDate fechaFin = null;
		if (!contexto.parametros.string("fechaDesde").isEmpty()) {
			fechaInicio = LocalDate.parse(contexto.parametros.string("fechaDesde"), DateTimeFormatter.ISO_LOCAL_DATE);
		} else {
			fechaInicio = LocalDate.now();
		}
		fechaFin = fechaInicio.plusDays(20);
		request.query("codTipoReserva", contexto.parametros.string("tipoTurno"));
		request.query("codigoSucursal", contexto.parametros.string("codigoSucursal"));
		request.query("documento", cuil);
		request.query("fechaInicio", fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE));
		request.query("fechaFin", fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return ApiMB.response(request);
	}
}
