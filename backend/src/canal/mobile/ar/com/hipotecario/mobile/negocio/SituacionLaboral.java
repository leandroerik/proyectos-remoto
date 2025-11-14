package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.excepcion.ApiVentaExceptionMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestPersona;

public class SituacionLaboral {

	/* ========== ATRIBUTOS ========== */
	public String idProfesion;
	public String idRamo;
	public String idCargo;
	public BigDecimal ingresoNeto;
	public Integer id;
	public String idSituacionLaboral;
	public boolean esPrincipal;
	public String fechaInicioActividad;
	public String razonSocialEmpleador;
	public String cuitEmpleador;

	/* ========== UTIL ========== */
	public static SituacionLaboral situacionLaboralPrincipal(ContextoMB contexto) {

		ApiResponseMB actividades = RestPersona.consultarActividades(contexto);
		if (actividades.hayError() || !actividades.objetos("Errores").isEmpty()) {
			throw new ApiVentaExceptionMB(actividades);
		}

		Objeto datos = new Objeto();
		for (Objeto item : actividades.objetos()) {
			if (item.bool("esPrincipal")) {
				datos = item;
				break;
			}
		}

		SituacionLaboral actividadLaboral = (new Gson()).fromJson(datos.toJson(), SituacionLaboral.class);
		return actividadLaboral;
	}

	public String fechaInicioActividad(ContextoMB contexto, String actividadPrincipal) {
		ApiResponseMB actividades = RestPersona.consultarActividades(contexto);
		if (actividades.hayError() || !actividades.objetos("Errores").isEmpty()) {
			throw new ApiVentaExceptionMB(actividades);
		}

		for (Objeto item : actividades.objetos()) {
			if (actividadPrincipal.equals(item.string("idSituacionLaboral")) && item.bool("esPrincipal")) {
				fechaInicioActividad = item.string("fechaInicioActividad");
				break;
			}
		}
		return fechaInicioActividad;
	}

	public static List<SituacionLaboral> situacionesLaboralesCobis(ContextoMB contexto) {

		ApiResponseMB actividades = RestPersona.consultarActividades(contexto);
		if (actividades.hayError() || !actividades.objetos("Errores").isEmpty()) {
			throw new ApiVentaExceptionMB(actividades);
		}

		List<SituacionLaboral> datos = new ArrayList<>();
		for (Objeto item : actividades.objetos()) {
			SituacionLaboral situacion = (new Gson()).fromJson(item.toJson(), SituacionLaboral.class);
			datos.add(situacion);
		}

		return datos;
	}

	public Boolean actualizarSituacionLaboral(ContextoMB contexto) {

		ApiResponseMB respuesta = RestPersona.actualizarActividad(contexto, id, idSituacionLaboral, idProfesion, idRamo, idCargo, ingresoNeto, esPrincipal);
		if (respuesta.hayError() || !respuesta.objetos("Errores").isEmpty()) {
			throw new ApiVentaExceptionMB(respuesta);
		}
		return true;
	}

}
