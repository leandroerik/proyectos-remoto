package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PermisoOBRepositorio;

public class ServicioPermisosOB extends ServicioOB {

	private PermisoOBRepositorio repo;

	public ServicioPermisosOB(ContextoOB contexto) {
		super(contexto);
		repo = new PermisoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<PermisoOB> find(Integer id) {
		return futuro(() -> repo.find(id));
	}

	public Futuro<List<PermisoOB>> permisos() {
		return futuro(() -> repo.permisos());
	}

	public Futuro<List<PermisoOB>> permisosAsignados(PermisoOB permiso, EmpresaUsuarioOB empresa) {
		return futuro(() -> repo.permisosAsignados(permiso, empresa));
	}

	public Futuro<List<PermisoOB>> permisosSinAsignar(PermisoOB permiso, EmpresaUsuarioOB empresa) {
		return futuro(() -> repo.permisosSinAsignar(permiso, empresa));
	}

	public List<Objeto> seleccionarPermisos(List<PermisoOperadorOB> permisos) {
		Objeto result = new Objeto();
		List<PermisoOB> lstPermisos = repo.permisos().stream().filter(p -> p.opcional && p.codigo != 8).collect(Collectors.toList());
		for (PermisoOB permiso : lstPermisos) {
			Objeto op = seleccionar(permisos, permiso);
			Objeto lstSubPermisos = new Objeto();
			for (PermisoOB sp : permiso.subpermisos) {
				Objeto spermiso = seleccionar(permisos, sp);
				lstSubPermisos.add(spermiso);
			}
			if (permiso.subpermisos.size() > 0) {
				op.set("permisos", lstSubPermisos);
			}
			result.add(op);
		}
		return result.objetos();
	}

	public List<Objeto> seleccionarPermisos(List<PermisoOperadorOB> permisos, EmpresaUsuarioOB empresaOperador) {
		Objeto result = new Objeto();
		List<PermisoOperadorOB> permisosPadres = permisos.stream().filter(p -> p.permiso.padre == null).collect(Collectors.toList());

		for (PermisoOperadorOB permiso : permisosPadres) {
			Objeto op = seleccionar(permisos, permiso.permiso);

			List<PermisoOB> permisosAsignados = this.permisosAsignados(permiso.permiso, empresaOperador).get();
			Objeto lstSubPermisos = new Objeto();
			for (PermisoOB sp : permisosAsignados) {
				Objeto spermiso = seleccionar(permisos, sp);
				lstSubPermisos.add(spermiso);
			}
			if (permisosAsignados.size() > 0) {
				op.set("permisos", lstSubPermisos);
			}

			List<PermisoOB> permisosSinAsignar = this.permisosSinAsignar(permiso.permiso, empresaOperador).get();
			Objeto lstSubPermisosSinAsignar = new Objeto();
			for (PermisoOB sp : permisosSinAsignar) {
				Objeto spermiso = seleccionar(permisos, sp);
				lstSubPermisosSinAsignar.add(spermiso);
			}
			if (permisosSinAsignar.size() > 0) {
				op.set("permisosSinAsignar", lstSubPermisosSinAsignar);
			}

			result.add(op);
		}

		return result.objetos();
	}

	private Objeto seleccionar(List<PermisoOperadorOB> permisos, PermisoOB permiso) {
		Objeto op = new Objeto();
		op.set("codigo", permiso.codigo);
		op.set("nombre", permiso.nombre);
		if (permisos != null && permisos.size() > 0) {
			Optional<PermisoOperadorOB> existe = permisos.stream().filter(p -> p.permiso.codigo.equals(permiso.codigo)).findFirst();
			op.set("habilitado", existe.isPresent() ? true : false);
		} else {
			op.set("habilitado", false);
		}
		return op;
	}

}