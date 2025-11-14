package ar.com.hipotecario.canal.officebanking.jpa;

import java.time.LocalDate;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.OrdenPagoFechaEjecucionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.OrdenPagoFechaEjecucionOBRepositorio;

public class ServicioOrdenPagoFechaEjecucionOB extends ServicioOB {

	private OrdenPagoFechaEjecucionOBRepositorio repo;

	public ServicioOrdenPagoFechaEjecucionOB(ContextoOB contexto) {
		super(contexto);
		repo = new OrdenPagoFechaEjecucionOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<OrdenPagoFechaEjecucionOB> findById(Contexto contexto, Integer idOperacion) {
		return futuro(() -> repo.findByFieldUnique("idOperacion", idOperacion));
	}
	
	public Futuro<OrdenPagoFechaEjecucionOB> crear(String nombreArchivo, LocalDate fechaEjecucion, Integer idOperacion) {
		OrdenPagoFechaEjecucionOB ordenPago = new OrdenPagoFechaEjecucionOB();
		ordenPago.nombreArchivo = nombreArchivo;
		ordenPago.fechaEjecucion = fechaEjecucion;
		ordenPago.idOperacion = idOperacion;
		return futuro(() -> repo.create(ordenPago));
	}
}
