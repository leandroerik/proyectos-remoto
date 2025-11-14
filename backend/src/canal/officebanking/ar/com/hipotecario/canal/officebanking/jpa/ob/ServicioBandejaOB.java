package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.BandejaOBRepositorio;

public class ServicioBandejaOB extends ServicioOB {

	private BandejaOBRepositorio repo;

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static final ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);

	public ServicioBandejaOB(ContextoOB contexto) {
		super(contexto);
		repo = new BandejaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<BandejaOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<List<BandejaOB>> buscarPendientesDeFirma(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, MonedaOB moneda, Integer codProducto, String cuenta, String tipoSolicitud) {
		EstadoBandejaOB estadoPendienteDeFirma = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		EstadoBandejaOB estadoParcialmenteFirmada = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo()).get();

		return futuro(() -> repo.buscarPendientesDeFirma(empresa, estadoPendienteDeFirma, estadoParcialmenteFirmada, fechaDesde, fechaHasta, moneda, codProducto, cuenta, tipoSolicitud));
	}

	public Futuro<BandejaOB> update(BandejaOB bandejaOB) {
		bandejaOB.fechaUltActulizacion = LocalDateTime.now();
		return futuro(() -> repo.update(bandejaOB));
	}

}
