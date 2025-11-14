package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class BandejaAccionesOBRepositorio extends RepositorioGenericoImpl<BandejaAccionesOB> {

	public List<BandejaAccionesOB> buscarPorIdEmpresaUsuarioYAccion(BandejaOB bandeja, EmpresaUsuarioOB empresaUsuario, Integer accionId) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<BandejaAccionesOB> typedQuery = em.createNamedQuery("BandejaAccionesOB.buscarPorIdEmpresaUsuarioYAccion", BandejaAccionesOB.class);
			typedQuery.setParameter("idEmpresaUsuario", empresaUsuario.id);
			typedQuery.setParameter("idBandeja", bandeja.id);
			typedQuery.setParameter("accion", accionId);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<BandejaAccionesOB> buscarPorIdBandejaYAccion(BandejaOB bandeja, Integer accionId) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<BandejaAccionesOB> typedQuery = em.createNamedQuery("BandejaAccionesOB.buscarPorIdBandejaYAccion", BandejaAccionesOB.class);
			typedQuery.setParameter("idBandeja", bandeja.id);
			typedQuery.setParameter("accion", accionId);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<BandejaAccionesOB> buscarPorBandeja(BandejaOB bandeja) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<BandejaAccionesOB> typedQuery = em.createNamedQuery("BandejaAccionesOB.buscarPorBandeja", BandejaAccionesOB.class);
			typedQuery.setParameter("idBandeja", bandeja.id);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}