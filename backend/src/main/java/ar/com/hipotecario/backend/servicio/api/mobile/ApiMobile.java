package ar.com.hipotecario.backend.servicio.api.mobile;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiMobile extends Api {

	public static Futuro<SoftToken> crear(Contexto contexto, Boolean onboarding, String idCobis, String idDispositivo) {
		return futuro(() -> SoftToken.post(contexto, onboarding, idCobis, idDispositivo));
	}

}
