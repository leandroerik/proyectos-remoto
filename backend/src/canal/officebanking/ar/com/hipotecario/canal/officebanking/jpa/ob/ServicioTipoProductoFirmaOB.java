package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TipoProductoFirmaOBRepositorio;

public class ServicioTipoProductoFirmaOB extends ServicioOB {

	private TipoProductoFirmaOBRepositorio repo;

	public ServicioTipoProductoFirmaOB(ContextoOB contexto) {
		super(contexto);
		repo = new TipoProductoFirmaOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<TipoProductoFirmaOB>> findByActivo() {
		return futuro(() -> repo.findByActivo());
	}

	public Futuro<TipoProductoFirmaOB> findByCodigo(Integer codigo) {
		return futuro(() -> repo.findByCodigo(codigo));
	}
}
