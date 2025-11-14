package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.ventas.ApiVentaException;

public class SituacionLaboral {
	/* ========== ATRIBUTOS ========== */
	public String idProfesion;
	public String idRamo;
	public String idCargo;
	public BigDecimal ingresoNeto;
	public Integer id;
	public String idSituacionLaboral;
	public boolean tienePrincipal;
	public String fechaInicioActividad;
	public String razonSocialEmpleador;
	public String cuitEmpleador;

	/* ========== UTIL ========== */
	public static SituacionLaboral situacionLaboralPrincipal(ContextoHB contexto) {

		ApiResponse actividades = RestPersona.consultarActividades(contexto);
		if (actividades.hayError() || !actividades.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(actividades);
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

	public String fechaInicioActividad(ContextoHB contexto, String actividadPrincipal) {
		ApiResponse actividades = RestPersona.consultarActividades(contexto);
		if (actividades.hayError() || !actividades.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(actividades);
		}

		for (Objeto item : actividades.objetos()) {
			if (actividadPrincipal.equals(item.string("idSituacionLaboral")) && item.bool("esPrincipal")) {
				fechaInicioActividad = item.string("fechaInicioActividad");
				break;
			}
		}
		return fechaInicioActividad;
	}

	public static List<SituacionLaboral> situacionesLaboralesCobis(ContextoHB contexto) {

		ApiResponse actividades = RestPersona.consultarActividades(contexto);
		if (actividades.hayError() || !actividades.objetos("Errores").isEmpty()) {
			throw new ApiVentaException(actividades);
		}

		List<SituacionLaboral> datos = new ArrayList<>();
		for (Objeto item : actividades.objetos()) {
			SituacionLaboral situacion = (new Gson()).fromJson(item.toJson(), SituacionLaboral.class);
			datos.add(situacion);
		}

		return datos;
	}

}
