package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ClaveUsuarioOBRepositorio extends RepositorioGenericoImpl<ClaveUsuarioOB> {

	public List<ClaveUsuarioOB> findByUsuario(UsuarioOB usuario) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<ClaveUsuarioOB> typedQuery = em.createNamedQuery("UltimaClave", ClaveUsuarioOB.class);
			typedQuery.setParameter("usuario", usuario);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}