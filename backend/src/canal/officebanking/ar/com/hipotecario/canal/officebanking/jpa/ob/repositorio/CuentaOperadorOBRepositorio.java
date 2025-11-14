package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.CuentaOperadorOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

public class CuentaOperadorOBRepositorio extends RepositorioGenericoImpl<CuentaOperadorOB> {

	public int eliminarCuenta(Integer idCuentaOperador) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Query query = em.createNamedQuery("CuentaOperadorOB.delete");
            query.setParameter("idCuentaOperador", idCuentaOperador);
            int result = query.executeUpdate();
            tx.commit();
            return result;
        } finally {
            em.close();
        }
    }

}