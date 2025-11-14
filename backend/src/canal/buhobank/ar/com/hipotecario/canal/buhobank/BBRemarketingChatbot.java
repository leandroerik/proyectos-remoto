package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBParametriasGeneralBuhobank;
import ar.com.hipotecario.backend.servicio.sql.esales.RemarketingsWhatsappEsales;

public class BBRemarketingChatbot extends Modulo {

	public static Object obtenerRemarketingWhatsapp(ContextoBB contexto) {

		Boolean actualizarHistorico = contexto.parametros.bool("actualizar_historico", false);

		BBParametriasGeneralBuhobank parametrias = SqlBuhoBank.obtenerParametriasGeneral(contexto).tryGet();
		Fecha fechaDesde = obtenerFechaUltimaConsulta(contexto, parametrias);
		String estados = BBParametriasGeneralBuhobank.buscarValor(parametrias, BBParametriasGeneralBuhobank.CHATBOT_FILTRO_ESTADOS);
		String eventos = BBParametriasGeneralBuhobank.buscarValor(parametrias, BBParametriasGeneralBuhobank.CHATBOT_FILTRO_EVENTOS);
		if (empty(estados) || empty(eventos)) {
			return respuesta("ERROR");
		}

		if (actualizarHistorico) {

			SqlEsales.ejecutarHistoricoRemarketingsWhatsapp(contexto, fechaDesde, estados, eventos).tryGet();
			actualizarFechaUltimaConsulta(contexto);
		}

		RemarketingsWhatsappEsales datos = SqlEsales.obtenerRemarketingsWhatsapp(contexto).tryGet();
		if (datos == null) {
			return respuesta(ErroresBB.ERROR_GET_DATOS);
		}

		Objeto respuesta = respuesta();
		respuesta.set("fecha_desde", fechaDesde);
		respuesta.set("total", datos.size());
		respuesta.set("datos", datos);

		return respuesta;
	}

	private static Fecha obtenerFechaUltimaConsulta(ContextoBB contexto, BBParametriasGeneralBuhobank parametrias) {

		String fechaDesdeSrt = BBParametriasGeneralBuhobank.buscarValor(parametrias, BBParametriasGeneralBuhobank.CHATBOT_FECHA_ULTIMA_CONSULTA);
		if (!empty(fechaDesdeSrt)) {

			try {
				return new Fecha(fechaDesdeSrt, GeneralBB.FORMATO_FECHA_COMPLETA);
			} catch (Exception e) {

			}
		}

		return Fecha.ahora().restarDias(1);
	}

	private static void actualizarFechaUltimaConsulta(ContextoBB contexto) {

		SqlBuhoBank.actualizarNombreParametriaGeneralBuhobank(contexto, BBParametriasGeneralBuhobank.CHATBOT_FECHA_ULTIMA_CONSULTA, Fecha.ahora().toString()).tryGet();
	}
}
