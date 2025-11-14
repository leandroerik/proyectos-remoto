package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.BandejaAccionesOBRepositorio;

public class ServicioBandejaAccionesOB extends ServicioOB {

	private BandejaAccionesOBRepositorio repo;

	public ServicioBandejaAccionesOB(ContextoOB contexto) {
		super(contexto);
		repo = new BandejaAccionesOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<BandejaAccionesOB>> buscarPorIdEmpresaUsuarioYAccion(BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario, Integer accionId) {
		return futuro(() -> repo.buscarPorIdEmpresaUsuarioYAccion(bandeja, empresaUsuario, accionId));
	}
	
	public Futuro<List<BandejaAccionesOB>> buscarPorIdBandejaYAccion(BandejaOB bandeja, Integer accionId) {
		return futuro(() -> repo.buscarPorIdBandejaYAccion(bandeja, accionId));
	}

	public Futuro<List<BandejaAccionesOB>> buscarPorBandeja(BandejaOB bandeja) {
		return futuro(() -> repo.buscarPorBandeja(bandeja));
	}

	public Futuro<BandejaAccionesOB> crear(BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario, AccionesOB accion, EstadoBandejaOB estadoInicialBandeja, EstadoBandejaOB estadoFinalBandeja) {
		BandejaAccionesOB bandejaAccion = new BandejaAccionesOB();
		bandejaAccion.empresaUsuario = empresaUsuario;
		bandejaAccion.fechaCreacion = LocalDateTime.now();
		bandejaAccion.accion = accion;
		bandejaAccion.bandeja = bandeja;
		bandejaAccion.estadoInicial = estadoInicialBandeja;
		bandejaAccion.estadoFinal = estadoFinalBandeja;
		return futuro(() -> repo.create(bandejaAccion));
	}
}
