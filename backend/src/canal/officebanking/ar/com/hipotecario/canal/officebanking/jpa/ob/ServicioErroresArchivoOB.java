package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.ErroresArchivosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ErrorArchivoOBRepositorio;

public class ServicioErroresArchivoOB extends ServicioOB {
	private ErrorArchivoOBRepositorio repo;

	public ServicioErroresArchivoOB(ContextoOB contexto) {
		super(contexto);
		repo = new ErrorArchivoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<ErroresArchivosOB> crear(ErrorArchivoOB error, BandejaOB bandeja) {
		ErroresArchivosOB errores = new ErroresArchivosOB();
		errores.operacion = bandeja;
		errores.campo = error.getCampo();
		errores.titulo = error.getTitulo();
		errores.descripcion = error.getDescripcion();
		errores.linea = error.getLinea();

		return futuro(() -> repo.create(errores));
	}

	public Futuro<List<ErroresArchivosOB>> buscarPorIdOperacion(BandejaOB operacion) {
		return futuro(() -> repo.buscarPorIdOperacion(operacion));
	}
}
