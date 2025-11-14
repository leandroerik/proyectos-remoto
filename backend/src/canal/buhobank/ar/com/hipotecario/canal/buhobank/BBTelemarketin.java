package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.ventas.ApiVentas;
import ar.com.hipotecario.backend.servicio.api.ventas.Indicador;
import ar.com.hipotecario.backend.servicio.sql.SqlCarritoAbandonado;
import ar.com.hipotecario.backend.servicio.sql.SqlCrm;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.ProspectsEsales.ProspectEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.telemarketing.CarritoAbandonadosTelemarketing;
import ar.com.hipotecario.backend.servicio.sql.telemarketing.CarritoAbandonadosTelemarketing.CarritoAbandonadoTelemarketing;

public class BBTelemarketin extends Modulo {

	public static void procesoTelemarketing(ContextoBB contexto) {
		BBTelemarketin.marcarLlamados(contexto);
		BBTelemarketin.refreshCarritoAbandonado(contexto);
		BBTelemarketin.enviarAGenesis(contexto);
	}

	public static void marcarLlamados(ContextoBB contexto) {
		Fecha fechaDesde = Fecha.ahora().restarDias(90);

		Integer count = 0;

		CarritoAbandonadosTelemarketing llamadosCarrito = SqlCarritoAbandonado.obtenerLlamadosCarritoAbandonado(contexto).tryGet();
		for (Integer i = 0; i < llamadosCarrito.size(); i++) {
			
			CarritoAbandonadoTelemarketing llamadoCarrito = llamadosCarrito.get(i);
			SqlEsales.actualizarEstadoProspect(contexto, llamadoCarrito.cuil, llamadoCarrito.lista_discador).tryGet();
			count++;
		}

		ProspectsEsales prospects = SqlEsales.obtenerProspects(contexto, fechaDesde).tryGet();
		for (Integer i = 0; i < prospects.size(); i++) {

			ProspectEsales prospect = prospects.get(i);

			if (prospect.esListaDiscadorPaquete() || prospect.esListaDiscadorIndicador()) {

				if (SqlCrm.fueLlamadoCrmPaquetes(contexto, prospect.CUIL, fechaDesde).tryGet()) {
					SqlEsales.actualizarEstadoProspect(contexto, prospect.CUIL, prospect.ListaDiscador).tryGet();
					count++;
				}
			}
		}

		LogBB.evento(contexto, GeneralBB.PROCESO_TELEMARKETING, "finalizo etapa marcar llamados, countLlamados: " + count.toString());
	}

	public static void refreshCarritoAbandonado(ContextoBB contexto) {
		SqlCarritoAbandonado.refresh(contexto).tryGet();
	}

	public static void enviarAGenesis(ContextoBB contexto) {
		Fecha fechaDesde = Fecha.ahora().restarDias(90);

		SqlEsales.guardarProspects(contexto, ProspectsEsales.listaDiscadorPaquete, fechaDesde).tryGet();

		if (contexto.config.bool("buhobank_proceso_indicador", false)) {
			indicadorPasibleCredito(contexto);
		}

		Integer countPaquete = 0;
		Integer countIndicador = 0;

		ProspectsEsales prospects = SqlEsales.obtenerProspects(contexto, fechaDesde).tryGet();
		for (Integer i = 0; i < prospects.size(); i++) {

			if (SqlCarritoAbandonado.enviarAGenesis(contexto, prospects.get(i)).tryGet()) {
				SqlEsales.actualizarFechaUltimoEnvio(contexto, prospects.get(i)).tryGet();

				if (prospects.get(i).ListaDiscador.equals(ProspectsEsales.listaDiscadorPaquete)) {
					countPaquete++;
				}

				if (prospects.get(i).ListaDiscador.equals(ProspectsEsales.listaDiscadorIndicador)) {
					countIndicador++;
				}
			}
		}

		LogBB.evento(contexto, GeneralBB.PROCESO_TELEMARKETING, "finalizo etapa enviar a genesis, countListaIndicador: " + countIndicador.toString() + " countListaPaquete: " + countPaquete.toString());
	}

	public static final String uuid() {
		String result = java.util.UUID.randomUUID().toString();
		result = result.replaceAll("-", "");
		result = result.substring(0, 16);
		return result;
	}

	public static void indicadorPasibleCredito(ContextoBB contexto) {
		Fecha fechaHasta = Fecha.ahora().restarHoras(3);
		Fecha fechaDesde = Fecha.ahora().restarDias(90);

		SesionesEsales sesiones = SqlEsales.getSesionesSinOferta(contexto, fechaDesde, fechaHasta).tryGet();

		for (Integer i = 0; i < sesiones.size(); i++) {

			SesionEsales sesion = sesiones.get(i);

			if (empty(sesion.sexo)) {

				Cuils resCuils = ApiPersonas.cuils(contexto, sesion.documento_numero).tryGet();

				if (!empty(resCuils) && resCuils.size() == 1) {
					Persona persona = ApiPersonas.persona(contexto, sesion.cuil, false).tryGet();
					if (persona != null) {
						sesion.sexo = persona.idSexo;

						if (empty(sesion.nombre)) {
							sesion.nombre = persona.nombres;
						}

						if (empty(sesion.apellido)) {
							sesion.apellido = persona.apellidos;
						}
					}
				}
			}
			else {

				if (empty(sesion.cuil_tipo)) {
					sesion.cuil_tipo = GeneralBB.CUIL;
				}

				Indicador indicador = ApiVentas.sujetoPasibleCredito(contexto, sesion.documento_numero, sesion.cuil, sesion.sexo, sesion.documento_tipo_id, sesion.cuil_tipo).tryGet();
				if (indicador != null) {

					if (indicador.indOK) {
						SqlEsales.actualizarMotorIndicador(contexto, sesion.id, 1).tryGet();

						if (empty(sesion.nombre)) {
							sesion.nombre = "SIN_NOMBR";
						}
						if (empty(sesion.apellido)) {
							sesion.apellido = "SIN_APELL";
						}

						ProspectsEsales prospectsEsales = SqlEsales.getProspectByCuil(contexto, sesion.cuil, ProspectsEsales.listaDiscadorIndicador).tryGet();

						if (prospectsEsales != null && prospectsEsales.size() == 0) {
							SqlEsales.guardarProspect(contexto, sesion, ProspectsEsales.listaDiscadorIndicador).tryGet();
						}

					} else {
						SqlEsales.actualizarMotorIndicador(contexto, sesion.id, 0).tryGet();
					}
				}
			}
		}
	}
}