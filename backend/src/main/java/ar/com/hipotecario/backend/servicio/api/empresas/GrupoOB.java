package ar.com.hipotecario.backend.servicio.api.empresas;

import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class GrupoOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<PosibleFirmante> firmantes;

	public static class PosibleFirmante extends ApiObjeto {
		public String cedruc;
		public String firmante;
	}

}
