package ar.com.hipotecario.mobile.api;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class MBOrquestado {

	/* ========== SESION ========== */
	public static void crearSesion(ContextoMB contexto, String idCobis, String canal) {
		contexto.sesion().setIdCobis(idCobis);
		contexto.sesion().setIdCobisReal(idCobis);
		contexto.sesion().setUsuarioLogueado(true);
		// contexto.sesion().setIp(contexto.ip());
		// contexto.sesion().setCanal(canal);
		// contexto.sesion().save();
	}

	public static void eliminarSesion(ContextoMB contexto) {
		contexto.sesion().setIdCobis(null);
		contexto.sesion().setIdCobisReal(null);
		contexto.sesion().setUsuarioLogueado(null);
//		contexto.sesion().setIp(null);
//		contexto.sesion().setCanal(null);
		// contexto.sesion().delete();
	}

	public static Object xValidarSoftToken(ContextoMB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		String canal = contexto.parametros.string("canal");
		if (Objeto.anyEmpty(idCobis, canal)) {
			return RespuestaMB.parametrosIncorrectos();
		}
		try {
			crearSesion(contexto, idCobis, canal);
			return new MBSoftToken().validarSoftToken(contexto);
		} finally {
			eliminarSesion(contexto);
		}
	}
}
