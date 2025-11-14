package ar.com.hipotecario.canal.officebanking.cron;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron.CronJob;
import ar.com.hipotecario.backend.servicio.api.link.ApiLink;
import ar.com.hipotecario.backend.servicio.api.link.Pagos;
import ar.com.hipotecario.backend.servicio.api.link.PagosOB;
import ar.com.hipotecario.backend.servicio.api.link.TarjetaVirtual;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.OBFirmas;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.PagoDeServiciosOB;

public class CronOBRechazarPagoDeServiciosSinFirma extends CronJob {
	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final String CRON = CronOBRechazarPagoDeServiciosSinFirma.class.getSimpleName().toUpperCase();

	public void run() {
		boolean ejecutar = true;

		if (!ejecutar) {
			LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
			return;
		}

		LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

		List<PagoDeServiciosOB> pagosARechazar = new ServicioPagoDeServiciosOB(contexto).buscarSinFirmaCambioDeDia().get();

		if (pagosARechazar != null && !pagosARechazar.isEmpty()) {

			for (PagoDeServiciosOB pago : pagosARechazar) {
				TarjetaVirtual tarjetaVirtual = TarjetaVirtual.post(contexto, pago.empresa.cuit.toString());
				PagosOB pagosPendientes = ApiLink.pagosPendientesOB(contexto, tarjetaVirtual.nroTarjeta).get();

				try {
					boolean siguePendiente = false;

					if (pagosPendientes.stream().noneMatch(p -> p.ususarioLP.equals(pago.codigoLink))) {
						OBFirmas.rechazarPagoDeServiciosSinFirma(contexto, pago);
					} else {
						PagosOB.PagoOB pendiente = pagosPendientes.stream().filter(p -> p.ususarioLP.equals(pago.codigoLink)).findFirst().get();

						for (PagosOB.Vencimiento vencimiento : pendiente.vencimiento) {
							if (vencimiento.id.equals(pago.idDeuda)) {
								siguePendiente = true;
								break;
							}
						}
						if (!siguePendiente) {
							OBFirmas.rechazarPagoDeServiciosSinFirma(contexto, pago);
						}
					}
				} catch (Exception ignored) {

				}
			}
		}

		LogOB.evento(contexto, "FIN", LocalDateTime.now().toString(), CRON);
	}
}
