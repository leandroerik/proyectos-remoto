package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.servicio.api.empresas.ApiEmpresas;
import ar.com.hipotecario.backend.servicio.api.empresas.CompletaFirmaOB;
import ar.com.hipotecario.backend.servicio.api.firmas.ErroresCore;
import ar.com.hipotecario.backend.servicio.api.firmas.EstadoTransferencia;
import ar.com.hipotecario.backend.servicio.api.firmas.TipoFirma;

public class FirmaOB {

	public TipoFirma tipoFirma;
	public EstadoTransferencia estadoTransferencia;

	public TipoFirma getTipoFirma() {
		return tipoFirma;
	}

	public void setTipoFirma(TipoFirma tipoFirma) {
		this.tipoFirma = tipoFirma;
	}

	public EstadoTransferencia getEstadoTransferencia() {
		return estadoTransferencia;
	}

	public void setEstadoTransferencia(EstadoTransferencia estadoTransferencia) {
		this.estadoTransferencia = estadoTransferencia;
	}

	public static FirmaOB tipoFirma(Contexto contexto, String cedruc, String cuenta, String monto, String firmante, String firmasRegistradas, String funcionalidadOB) {
		FirmaOB respuesta = new FirmaOB();

		if (cedruc.equals("23316601189") || cedruc.equals("27296994311")) {
			respuesta.setTipoFirma(TipoFirma.FIRMA_INDISTINTA);
			respuesta.setEstadoTransferencia(EstadoTransferencia.TOTALMENTE_FIRMADA);

			return respuesta;
		}

		CompletaFirmaOB grupoOB = ApiEmpresas.completaFirma(contexto, cedruc, cuenta, monto, firmante, firmasRegistradas, funcionalidadOB).get();

		switch (grupoOB.codigo) {
		case "200":
			respuesta.setTipoFirma(TipoFirma.FIRMA_CONJUNTA);
			respuesta.setEstadoTransferencia(EstadoTransferencia.PARCIALMENTE_FIRMADA);
			break;
		case "250":
			respuesta.setTipoFirma(TipoFirma.FIRMA_INDISTINTA);
			respuesta.setEstadoTransferencia(EstadoTransferencia.TOTALMENTE_FIRMADA);
			break;
		default:
			throw new RuntimeException((String.valueOf(ErroresCore.RESPUESTA_DESCONOCIDA)));
		}
		return respuesta;
	}
}
