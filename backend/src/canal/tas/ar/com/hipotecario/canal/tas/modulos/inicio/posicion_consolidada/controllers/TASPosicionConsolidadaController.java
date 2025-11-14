package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.controllers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.servicios.TASRestPosicionConsolidada;

import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

public class TASPosicionConsolidadaController {

	public static Objeto getPosicionConsolidada(ContextoTAS contexto) {
		try {
			String idCliente = contexto.parametros.string("idCliente");
			if (idCliente.isEmpty()) return RespuestaTAS.sinParametros(contexto, "parametro no ingresado");
			TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
			if (clienteSesion == null)
				return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
			if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
				return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

			return TASRestPosicionConsolidada.getPosicionConsolidada(contexto, idCliente);
		} catch (Exception e) {
			Pattern patron = Pattern.compile("\\[([^\\]]+)\\]");
			Matcher matcher = patron.matcher(e.getMessage());
			if (matcher.find())
				return RespuestaTAS.sinParametros(contexto, "Parametro no encontrado: " + matcher.group(1));

			return RespuestaTAS.error(contexto,
					"TASPosicionConsolidadaController - Metodo getPosicionConsolidada()", e);
		}

	}
}
