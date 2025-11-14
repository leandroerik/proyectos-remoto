package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TarjetaVirtualOBRepositorio;

public class ServicioTarjetaVirtualOB extends ServicioOB {

	private static TarjetaVirtualOBRepositorio repo;
	
	public ServicioTarjetaVirtualOB(ContextoOB contexto) {
		super(contexto);
		repo = new TarjetaVirtualOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}
	
	public Futuro<TarjetaVirtualOB> create(TarjetaVirtualOB tarjetaVirtual) {
		return futuro(() -> repo.create(tarjetaVirtual));
	}
	
	public Futuro<List<TarjetaVirtualOB>> buscarPorEmpresa(EmpresaOB empresaOB) {
		return futuro(() -> repo.buscarPorEmpresa(empresaOB));
	}

	public Futuro<List<TarjetaVirtualOB>> buscarPorEmpresaAndId(EmpresaOB empresaOB, Integer id) {
		return futuro(() -> repo.buscarPorEmpresaAndId(empresaOB, id));
	}

	public Futuro<String> getIndex() {
		return futuro(() -> repo.getIndex());
	}
}
