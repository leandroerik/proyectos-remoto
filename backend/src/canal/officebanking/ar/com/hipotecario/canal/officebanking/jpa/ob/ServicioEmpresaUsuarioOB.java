package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EmpresaUsuarioOBRepositorio;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.EmpresaUsuarioOBRepositorioLite;

public class ServicioEmpresaUsuarioOB extends ServicioOB {

	private EmpresaUsuarioOBRepositorio repo;
	private EmpresaUsuarioOBRepositorioLite repoLite;

	public ServicioEmpresaUsuarioOB(ContextoOB contexto) {
		super(contexto);
		repo = new EmpresaUsuarioOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
		repoLite = new EmpresaUsuarioOBRepositorioLite();
		repoLite.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<EmpresaUsuarioOB>> findAll() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<List<EmpresaUsuarioOB>> findByUsuario(UsuarioOB usuario) {
		return futuro(() -> repo.findByField("usuario", usuario));
	}

	public Futuro<List<EmpresaUsuarioOB>> findByEmpresa(EmpresaOB empresaOB) {
		return futuro(() -> repo.findByField("empresa", empresaOB));
	}

	public Futuro<EmpresaUsuarioOB> findByUsuarioEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
		return futuro(() -> repo.findByUsuarioEmpresa(usuario, empresa));
	}
	public Futuro<EmpresaUsuarioOBLite> findByUsuarioEmpresaLite(UsuarioOB usuario, EmpresaOB empresa) {
		return futuro(() -> repoLite.findByUsuarioEmpresa(usuario, empresa));
	}
	
	public Futuro<List<EmpresaUsuarioOB>> findUsuariosByEmpresa(EmpresaOB empresa) {
		return futuro(() -> repo.findUsuariosByEmpresa(empresa));
	}
	public Futuro<List<EmpresaUsuarioOBLite>> findUsuariosByEmpresaLite(EmpresaOB empresa) {
		return futuro(() -> repoLite.findUsuariosByEmpresa(empresa));
	}

	public Futuro<List<EmpresaUsuarioOB>> findUsuariosByIdCobisEmpresa(String idCobisEmpresa) {
		return futuro(() -> repo.findUsuariosByIdCobisEmpresa(idCobisEmpresa));
	}

	public Futuro<EmpresaUsuarioOB> create(EmpresaUsuarioOB empresaUsuario) {
		return futuro(() -> repo.create(empresaUsuario));
	}

	public Futuro<EmpresaUsuarioOB> update(EmpresaUsuarioOB empresaUsuario) {
		return futuro(() -> repo.update(empresaUsuario));
	}
	public Futuro<EmpresaUsuarioOBLite> updateLite(EmpresaUsuarioOBLite empresaUsuario) {
		return futuro(() -> repoLite.update(empresaUsuario));
	}

	public Futuro<EmpresaUsuarioOB> altaOperador(ContextoOB contexto, UsuarioOB usuarioOB, EmpresaOB empresa, String cuentas, String permisos) {
		ServicioRolOB servicioRol = new ServicioRolOB(contexto);
		ServicioPermisosOB servicio = new ServicioPermisosOB(contexto);
				
		EmpresaUsuarioOB empresaUsuario = new EmpresaUsuarioOB();
		empresaUsuario.usuario = usuarioOB;
		empresaUsuario.empresa = empresa;
		empresaUsuario.rol = servicioRol.find(2).tryGet();
		
		Integer[] nuevosPermisos = new Integer[0];
		if (!empty(permisos)) {
			nuevosPermisos = Stream.of(permisos.replaceAll("\\s", "").split(",")).map(Integer::valueOf).toArray(Integer[]::new);
		}

		for (Integer codigo : nuevosPermisos) {
			PermisoOperadorOB permisoOperador = new PermisoOperadorOB();
			permisoOperador.empresaUsuario = empresaUsuario;
			permisoOperador.permiso = servicio.find(codigo).tryGet();
			empresaUsuario.permisos.add(permisoOperador);
		}

		if (!empty(cuentas)) {
			for (String cuenta : cuentas.split(",")) {
				CuentaOperadorOB cuentaOperador = new CuentaOperadorOB();
				cuentaOperador.empresaUsuario = empresaUsuario;
				cuentaOperador.numeroCuenta = cuenta;
				empresaUsuario.cuentas.add(cuentaOperador);
			}
		}		
		
		return futuro(() -> repo.update(empresaUsuario));
	}
	
	public Futuro<EmpresaUsuarioOB> altaOperadorExistente(ContextoOB contexto, UsuarioOB usuarioOB, EmpresaOB empresa, EmpresaUsuarioOB empresaUsuario, String cuentas, String permisos) {
		ServicioPermisosOB servicio = new ServicioPermisosOB(contexto);
		
		Integer[] nuevosPermisos = new Integer[0];
		if (!empty(permisos)) {
			nuevosPermisos = Stream.of(permisos.replaceAll("\\s", "").split(",")).map(Integer::valueOf).toArray(Integer[]::new);
		}

		for (Integer codigo : nuevosPermisos) {
			PermisoOperadorOB permisoOperador = new PermisoOperadorOB();
			permisoOperador.empresaUsuario = empresaUsuario;
			permisoOperador.permiso = servicio.find(codigo).tryGet();
			empresaUsuario.permisos.add(permisoOperador);
		}

		if (!empty(cuentas)) {
			for (String cuenta : cuentas.split(",")) {
				CuentaOperadorOB cuentaOperador = new CuentaOperadorOB();
				cuentaOperador.empresaUsuario = empresaUsuario;
				cuentaOperador.numeroCuenta = cuenta;
				empresaUsuario.cuentas.add(cuentaOperador);
			}
		}		
		
		return futuro(() -> repo.update(empresaUsuario));
	}

	public Futuro<EmpresaUsuarioOB> vinculacionOperadorAdministradorEmpresa(ContextoOB contexto, UsuarioOB usuarioOB, EmpresaOB empresa, String cuentas, String permisos) {
		ServicioRolOB servicioRol = new ServicioRolOB(contexto);

		EmpresaUsuarioOB empresaUsuario = new EmpresaUsuarioOB();
		empresaUsuario.usuario = usuarioOB;
		empresaUsuario.empresa = empresa;
		empresaUsuario.rol = servicioRol.find(1).tryGet();

		return futuro(() -> repo.update(empresaUsuario));
	}

	public Futuro<List<PermisoOperadorOB>> editarPermisosOperador(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario, String permisos) {
		List<Integer> nuevosPermisos = new ArrayList<Integer>();
		if (!empty(permisos)) {
			nuevosPermisos = Stream.of(permisos.replaceAll("\\s", "").split(",")).map(Integer::valueOf).collect(Collectors.toList());
		}

		List<PermisoOperadorOB> permisosElimiandos = new ArrayList<PermisoOperadorOB>();
		for (PermisoOperadorOB permisoActual : empresaUsuario.permisos) {
			if (!nuevosPermisos.contains(permisoActual.permiso.codigo)) {
				permisosElimiandos.add(permisoActual);
			}
		}
		empresaUsuario.permisos.removeAll(permisosElimiandos);

		ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
		for (PermisoOperadorOB permiso : permisosElimiandos) {
			servicioPermisoOperadorOB.eliminarPermiso(permiso.id).tryGet();
		}

		ServicioPermisosOB servicioPermisos = new ServicioPermisosOB(contexto);
		ServicioPermisoOperadorOB servicioPermisoOperador = new ServicioPermisoOperadorOB(contexto);

		for (Integer codigo : nuevosPermisos) {
			PermisoOB nuevoPermiso = servicioPermisos.find(codigo).tryGet();
			if (!empty(nuevoPermiso)) {
				PermisoOperadorOB permisoOperador = new PermisoOperadorOB();
				permisoOperador.empresaUsuario = empresaUsuario;
				permisoOperador.permiso = nuevoPermiso;
				List<PermisoOperadorOB> permisosAnteriores = servicioPermisoOperador.findByEmpresaUsuario(empresaUsuario).get();
				Optional<PermisoOperadorOB> tienePermiso = permisosAnteriores.stream().filter(po -> po.permiso.codigo.intValue() == nuevoPermiso.codigo.intValue()).findFirst();
				if (!tienePermiso.isPresent()) {
					empresaUsuario.permisos.add(permisoOperador);
				}
			}
		}

		return futuro(() -> repo.update(empresaUsuario).permisos);
	}

	public Futuro<List<PermisoOperadorOB>> eliminarPermisosOperador(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario) {
		List<PermisoOperadorOB> permisosElimiandos = new ArrayList<PermisoOperadorOB>();
		for (PermisoOperadorOB permisoActual : empresaUsuario.permisos) {
			permisosElimiandos.add(permisoActual);
		}
		empresaUsuario.permisos.removeAll(permisosElimiandos);

		ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
		for (PermisoOperadorOB permiso : permisosElimiandos) {
			servicioPermisoOperadorOB.eliminarPermiso(permiso.id).tryGet();
		}

		return futuro(() -> repo.update(empresaUsuario).permisos);
	}
	
	public Futuro<List<CuentaOperadorOB>> editarCuentasOperador(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario, String cuentas) {
		List<String> nuevasCuentas = new ArrayList<String>();
		if (!empty(cuentas)) {
			nuevasCuentas = Stream.of(cuentas.replaceAll("\\s", "").split(",")).map(String::valueOf).collect(Collectors.toList());
		}

		List<CuentaOperadorOB> cuentasEliminadas = new ArrayList<CuentaOperadorOB>();
		for (CuentaOperadorOB cuentaActual : empresaUsuario.cuentas) {
			if (!nuevasCuentas.contains(cuentaActual.numeroCuenta)) {
				cuentasEliminadas.add(cuentaActual);
			}
		}
		empresaUsuario.cuentas.removeAll(cuentasEliminadas);

		ServicioCuentaOperadorOB servicioCuentaOperadorOB = new ServicioCuentaOperadorOB(contexto);
		for (CuentaOperadorOB cuenta : cuentasEliminadas) {
			servicioCuentaOperadorOB.eliminarCuenta(cuenta.id);
		}

		ServicioCuentaOperadorOB servicioCuentaOperador = new ServicioCuentaOperadorOB(contexto);
		for (String numeroCuenta : nuevasCuentas) {
			CuentaOperadorOB permisoOperador = new CuentaOperadorOB();
			permisoOperador.empresaUsuario = empresaUsuario;
			permisoOperador.numeroCuenta = numeroCuenta;
			List<CuentaOperadorOB> cuentasAnteriores = servicioCuentaOperador.findByEmpresaUsuario(empresaUsuario).get();
			Optional<CuentaOperadorOB> tieneCuenta = cuentasAnteriores.stream().filter(po -> po.numeroCuenta.equals(numeroCuenta)).findFirst();
			if (!tieneCuenta.isPresent() && !permisoOperador.numeroCuenta.equals("null")) {
				empresaUsuario.cuentas.add(permisoOperador);
			}
		}

		return futuro(() -> repo.update(empresaUsuario).cuentas);
	}

	public Futuro<List<CuentaOperadorOB>> eliminarCuentasOperador(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario) {
		List<CuentaOperadorOB> cuentasEliminadas = new ArrayList<CuentaOperadorOB>();
		for (CuentaOperadorOB cuentaActual : empresaUsuario.cuentas) {
			cuentasEliminadas.add(cuentaActual);			
		}
		empresaUsuario.cuentas.removeAll(cuentasEliminadas);

		ServicioCuentaOperadorOB servicioCuentaOperadorOB = new ServicioCuentaOperadorOB(contexto);
		for (CuentaOperadorOB cuenta : cuentasEliminadas) {
			servicioCuentaOperadorOB.eliminarCuenta(cuenta.id);
		}

		return futuro(() -> repo.update(empresaUsuario).cuentas);
	}

	public Futuro<List<EmpresaUsuarioOB>> findByRolCodigo(RolOB rol) {
		return futuro(() -> repo.findByField("rol", rol));
	}

	public Futuro<List<EmpresaUsuarioOB>> findByRolEstado(RolOB rol, EstadoUsuarioOB estado) {
		return futuro(() -> repo.findByUsuarioHabilitadoRol(rol, estado));
	}

	public Futuro<List<EmpresaUsuarioOB>> findByRolEmpresa(RolOB rol, EmpresaOB empresa, EstadoUsuarioOB estado) {
		return futuro(() -> repo.findByRolEmpresa(rol, empresa, estado));
	}

	public Futuro<Integer> updateRol(EmpresaOB empresa,int rol) {
		return futuro(() -> repo.updateRol(empresa,rol));
	}

}
