package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc.EmpresaTycProducto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc.TycProducto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

public class EmpresaTycProductoOBRepositorio extends RepositorioGenericoImpl<EmpresaTycProducto> {
  
	public TycProducto findTycByVersioProductoEmpresa(int version, int productoId, int empresaCodigo) {
		 EntityManager em = emf.createEntityManager();
		    TycProducto tycProducto = null;
		    try {    
		        tycProducto = em.createNamedQuery("TycProducto.obtenerPorEmpresaVersionYProducto", TycProducto.class)
		                .setParameter("version", version)
		                .setParameter("productoId", productoId)
		                .setMaxResults(1)
		                .getSingleResult();
		    
		        // Verificar si existe una relación entre OB_Empresa_Tyc_Producto y OB_Tyc_Producto en la base de datos
		        if(tycProducto != null) {
		            EmpresaTycProducto empresaTycProducto = em.createNamedQuery("EmpresaTycProducto.obtenerPorEmpresaYProducto", EmpresaTycProducto.class)
		                    .setParameter("productoId", tycProducto.getId())
		                    .setParameter("empresaCodigo", empresaCodigo)
		                    .setMaxResults(1)
		                    .getSingleResult();
		            
		            if (empresaTycProducto != null) {
		                tycProducto.setRelacionEmpresa(true);
		            }
		        }
		    } catch(NoResultException e) {
		        return tycProducto;
		    } finally {
		        if (em != null && em.isOpen()) {
		            em.close();
		        }
		    }
		    
		    return tycProducto;
	}
	
	public void insertarEmpresaTycProducto(int empCodigo, int idTycProducto) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = null;
        
        try {
            tx = em.getTransaction();
            tx.begin();

            EmpresaTycProducto empresaTycProducto = new EmpresaTycProducto();
            empresaTycProducto.setEmpCodigo(empCodigo);
            empresaTycProducto.setIdTycProducto(idTycProducto);

            em.persist(empresaTycProducto);
            tx.commit();
        } catch(Exception e) {
            if(tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if(em != null && em.isOpen()) {
                em.close();
            }
        }
    }
	
	public EmpresaTycProducto findEmpresaYProductoById(int empCodigo,int idProductoTyc) {
		EntityManager em = emf.createEntityManager(); 
		EmpresaTycProducto empresaTycProducto = null;
	try {
		empresaTycProducto = em.createNamedQuery("EmpresaTycProducto.obtenerPorEmpresaYProducto", EmpresaTycProducto.class)
                 .setParameter("productoId", idProductoTyc)
                 .setParameter("empresaCodigo", empCodigo)
                 .setMaxResults(1)
                 .getSingleResult();
         
	} catch(NoResultException e) {
        return empresaTycProducto;
    } finally {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    
    return empresaTycProducto;
	}
}
  
