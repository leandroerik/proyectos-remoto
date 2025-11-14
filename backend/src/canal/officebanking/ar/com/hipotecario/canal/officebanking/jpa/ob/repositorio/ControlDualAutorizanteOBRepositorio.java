package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual.ControlDualAutorizanteOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ControlDualAutorizanteOBRepositorio extends RepositorioGenericoImpl<ControlDualAutorizanteOB>{

	public List<ControlDualAutorizanteOB> findAutorizantesActivos(EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<ControlDualAutorizanteOB> typedQuery = em.createNamedQuery("AutorizantesActivos", ControlDualAutorizanteOB.class);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
	
	public ControlDualAutorizanteOB findAutorizantePorEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<ControlDualAutorizanteOB> typedQuery = em.createNamedQuery("AutorizantePorEmpresa", ControlDualAutorizanteOB.class);
			typedQuery.setParameter("usuario", usuario);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}
}
