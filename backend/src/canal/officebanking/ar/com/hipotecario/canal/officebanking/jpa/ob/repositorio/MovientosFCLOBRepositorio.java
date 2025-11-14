package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.FondoCeseLaboralOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class MovientosFCLOBRepositorio extends RepositorioGenericoImpl<FondoCeseLaboralOB>{
	
	
	public List<FondoCeseLaboralOB> buscarByConvenio(int convenio) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<FondoCeseLaboralOB> typedQuery = em.createNamedQuery("MovimeintosFCL.buscarByConvenio", FondoCeseLaboralOB.class);
			typedQuery.setParameter("convenio", convenio);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}
