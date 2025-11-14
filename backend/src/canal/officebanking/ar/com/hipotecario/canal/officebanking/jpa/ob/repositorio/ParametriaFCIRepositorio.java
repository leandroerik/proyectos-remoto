package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.ParametriaFciOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class ParametriaFCIRepositorio extends RepositorioGenericoImpl<ParametriaFciOB> {

	public ParametriaFciOB buscarPorFondo(Integer idFondo, String tipoVcpDescripcion, String tipoSolicitud) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<ParametriaFciOB> typedQuery = em.createNamedQuery("ParametriaFciOB.buscarPorFondo", ParametriaFciOB.class);
			typedQuery.setParameter("idFondo", idFondo);
			typedQuery.setParameter("tipoVcpDescripcion", tipoVcpDescripcion);
			typedQuery.setParameter("tipoSolicitud", tipoSolicitud);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}

	public ParametriaFciOB buscarPorFondoYOperacion(Integer idFondo, String tipoVcpDescripcion, String tipoSolicitud) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<ParametriaFciOB> typedQuery = em.createNamedQuery("ParametriaFciOB.buscarPorFondo", ParametriaFciOB.class);
			typedQuery.setParameter("idFondo", idFondo);
			typedQuery.setParameter("tipoVcpDescripcion", tipoVcpDescripcion);
			typedQuery.setParameter("tipoSolicitud", tipoSolicitud);
			typedQuery.setMaxResults(1);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}

}