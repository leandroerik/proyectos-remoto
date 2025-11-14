package ar.com.hipotecario.backend.servicio.api.firmas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Util;

// http://cmpfirmas-api-core-desa.appd.bh.com.ar/index.html
public class ApiFirmas {

	/* ========== Esquemas Controller ========== */

	// GET /esquemas/{cedruc}/firmantes
	public static Futuro<FirmanteOB> firmanteOB(Contexto contexto, String cedruc, String cuenta, String firmante, String monto, String funcOB) {
		return Util.futuro(() -> FirmanteOB.get(contexto, cedruc, cuenta, firmante, monto, funcOB));
	}

}