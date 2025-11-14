package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.HistorialTrnOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class HistorialTrnOBRepositorio extends RepositorioGenericoImpl<HistorialTrnOB> {

	public List<HistorialTrnOB> buscar(TransferenciaOB transferencia) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<HistorialTrnOB> typedQuery = em.createNamedQuery("HistorialTrnOB.buscarPorTrn", HistorialTrnOB.class);
			typedQuery.setParameter("idTransferencia", transferencia.id);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<HistorialTrnOB> buscar(TransferenciaOB transferencia, EmpresaUsuarioOB empresaUsuario) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<HistorialTrnOB> typedQuery = em.createNamedQuery("HistorialTrnOB.buscarPorTrnYUsuario", HistorialTrnOB.class);
			typedQuery.setParameter("idTransferencia", transferencia.id);
			typedQuery.setParameter("idEmpresaUsuario", empresaUsuario.id);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}
