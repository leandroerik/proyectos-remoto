package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOperadorOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;

public class PermisoOperadorOBRepositorio extends RepositorioGenericoImpl<PermisoOperadorOB> {

	public PermisoOperadorOB buscarPermiso(EmpresaUsuarioOB empresaUsuario, PermisoOB permiso) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<PermisoOperadorOB> typedQuery = em.createNamedQuery("PermisoOperadorOB.findByEmpresaPermiso", PermisoOperadorOB.class);
			typedQuery.setParameter("empresaUsuario", empresaUsuario);
			typedQuery.setParameter("permiso", permiso);
			PermisoOperadorOB result = typedQuery.getSingleResult();
			return result;
		} finally {
			em.close();
		}
	}
		
	public int eliminarPermiso(Integer idPermisoOperador) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query query = em.createNamedQuery("PermisoOperadorOB.deletePermiso");
            query.setParameter("idPermisoOperador", idPermisoOperador);
            int result = query.executeUpdate();
            tx.commit();
            return result;
        } finally {
            em.close();
        }
    }

}