package ar.com.hipotecario.canal.homebanking.negocio;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;

public enum TipoNotificacion {
	SIMPLE(0, ConfigHB.string("doppler_documentacion")), CUENTA_DOLARES(1, "doppler_documentacion_cuenta_dolares"), SOS(2, "doppler_SOS"), Inversion(3, "doppler_Inversion");

	public final int value;
	public final String template;

	private TipoNotificacion(int value, String template) {
		this.value = value;
		this.template = template;
	}
}
