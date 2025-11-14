package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaCredinOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class TransferenciaCredinOBRepositorio extends RepositorioGenericoImpl<TransferenciaCredinOB> {

    public void cargarDesdeTransferencia(int id){
        EntityManager em = emf.createEntityManager();

        try{
            em.getTransaction().begin();
            Query query = em.createNamedQuery("TransferenciaCredinOB.insertDesdeTransferencia");
            query.setParameter("id",id);
            query.executeUpdate();
            em.getTransaction().commit();
        }catch (Exception e){
            em.getTransaction().rollback();
            throw e;
        }finally {
            em.close();
        }
    }
}
