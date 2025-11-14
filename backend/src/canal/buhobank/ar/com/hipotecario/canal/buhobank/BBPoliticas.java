package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.scheduler.ApiScheduler;
import ar.com.hipotecario.backend.servicio.api.scheduler.HorariosBancarios.HorarioBancario;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ParametriasEsales.HorarioOperativo;
import ar.com.hipotecario.backend.servicio.sql.esales.ParametriasEsales.PoliticasValidator;

public class BBPoliticas extends Modulo {

	public static Boolean maximoRechazosScoring(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String tipo = "RE";

		PoliticasValidator politicas = SqlEsales.politicas(contexto).tryGet();
		if (empty(politicas)) {
			return false;
		}

		Integer minutos = Util.integer(politicas.rechazoScoringMinutos);
		Fecha fechaInicio = sesion.fechaLogin.restarMinutos(minutos);

		Integer rechazos = SqlEsales.resultadosScoring(contexto, cuil, fechaInicio, tipo).tryGet();
		if (empty(rechazos)) {
			return false;
		}

		Integer rechazosMaximo = Util.integer(politicas.cuilIntentos);

		if (rechazos > rechazosMaximo) {
			sesion.resolucionMotorDeScoring = tipo;
			sesion.saveSesion();
		}

		return rechazos > rechazosMaximo;
	}

	public static Boolean estaEnHorario(ContextoBB contexto, Integer offset) {

		if (contexto.sesion().getParamHorarioBatch(contexto)) {
			return false;
		}

		PoliticasValidator politicas = SqlEsales.politicas(contexto).tryGet();
		if (empty(politicas)) {
			return false;
		}

		Fecha fechaActual = Fecha.ahora().sumarMinutos(offset);
		HorarioOperativo horarioDesde = politicas.horarioOperativoDesde;
		HorarioOperativo horarioHasta = politicas.horarioOperativoHasta;

		Boolean antesDefechaInicio = fechaActual.antesDeLas(horarioDesde.hora, horarioDesde.minuto, horarioDesde.segundo);
		Boolean despuesDeFechaFin = fechaActual.despuesDeLas(horarioHasta.hora, horarioHasta.minuto, horarioHasta.segundo);

		if (antesDefechaInicio || despuesDeFechaFin) {
			return false;
		}

		HorarioBancario horarioBancario = ApiScheduler.horario(contexto, GeneralBB.IDENTIF_BATCH_CORE).tryGet();
		if (empty(horarioBancario)) {
			return false;
		}

		String horarioBancarioDesde = horarioBancario.horaInicio + ":" + horarioBancario.minutoInicio;
		String horarioBancarioHasta = horarioBancario.horaFin + ":" + horarioBancario.minutoFin;

		Fecha fechaDesde = new Fecha(horarioBancarioDesde, "HH:mm");
		Fecha fechaHasta = new Fecha(horarioBancarioHasta, "HH:mm");

		if (fechaDesde.esPosterior(fechaHasta)) {
			return fechaActual.enIntervalo(fechaHasta, fechaDesde);
		}

		return !fechaActual.enIntervalo(fechaDesde, fechaHasta);
	}
}
