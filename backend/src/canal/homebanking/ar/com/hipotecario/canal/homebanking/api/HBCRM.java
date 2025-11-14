package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;

public class HBCRM {

	/* ========== SESION ========== */
	public static void crearSesion(ContextoHB contexto, String idCobis, String canal) {
		contexto.sesion.idCobis = (idCobis);
		contexto.sesion.idCobisReal = (idCobis);
		contexto.sesion.usuarioLogueado = (true);
		contexto.sesion.ip = (contexto.ip());
		contexto.sesion.canal = canal;
		contexto.sesion.save();
	}

	public static void eliminarSesion(ContextoHB contexto) {
		contexto.eliminarSesion(true, contexto.idCobis());
		contexto.sesion.idCobis = null;
		contexto.sesion.idCobisReal = null;
		contexto.sesion.usuarioLogueado = null;
		contexto.sesion.ip = null;
		contexto.sesion.canal = null;
		contexto.sesion.save();
	}

	/* ========== ENDPOINTS ========== */
	public static Object consulta(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBDebinRecurrente.consulta(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object alta(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBDebinRecurrente.alta(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object modificacion(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBDebinRecurrente.modificacion(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object baja(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBDebinRecurrente.baja(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object terminosCondiciones(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBDebinRecurrente.terminosCondiciones(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object consultaAumentoMontoMinimo(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBDebinRecurrente.consultaAumentoMontoMinimo(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object cuentaTercero(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			Respuesta respuesta = HBTransferencia.cuentaTerceroCoelsa(contexto);
			return respuesta;
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object montoMinimo(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			BigDecimal montoMinimo = HBDebinRecurrente.montoMinimo(contexto);
			Respuesta respuesta = new Respuesta();
			respuesta.set("montoMinimo", montoMinimo);
			return respuesta;
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object montoMinimoV2(ContextoHB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			BigDecimal montoMinimo = HBDebinRecurrente.montoMinimoV2(contexto);
			Respuesta respuesta = new Respuesta();
			respuesta.set("montoMinimo", montoMinimo);
			return respuesta;
		} finally {
			eliminarSesion(contexto);
		}
	}

	public static Object servicioCRMinsertGestion(ContextoHB contexto) {
        String idCobis = contexto.parametros.string("idCobis");
        String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return Respuesta.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return HBSucursalVirtual.servicioCRMinsertGestion(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}
}
