package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class EmpresaUsuarioOBRepositorioLite extends RepositorioGenericoImpl<EmpresaUsuarioOBLite> {

	public List<EmpresaUsuarioOBLite> find(UsuarioOB usuario) {
		List<EmpresaUsuarioOBLite> usuariosEmpresas = this.findByField("usuario", usuario);
		return usuariosEmpresas;
	}

	public EmpresaUsuarioOBLite findByUsuarioEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOBLite> typedQuery = em.createNamedQuery("EmpresaUsuarioLite", EmpresaUsuarioOBLite.class);
			typedQuery.setParameter("usuario", usuario);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}
	
	public List<EmpresaUsuarioOBLite> findUsuariosByEmpresa(EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOBLite> typedQuery = em.createNamedQuery("UsuariosEmpresaLite", EmpresaUsuarioOBLite.class);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<EmpresaUsuarioOBLite> findUsuariosByIdCobisEmpresa(String idCobisEmpresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOBLite> typedQuery = em.createNamedQuery("UsuariosPorIdCobisEmpresa", EmpresaUsuarioOBLite.class);
			typedQuery.setParameter("idCobisEmpresa", idCobisEmpresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<EmpresaUsuarioOBLite> findByRolEmpresa(RolOB rol, EmpresaOB empresa, EstadoUsuarioOB estado) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOBLite> typedQuery = em.createNamedQuery("EmpresaRol", EmpresaUsuarioOBLite.class);
			typedQuery.setParameter("rol", rol);
			typedQuery.setParameter("empresa", empresa);
			typedQuery.setParameter("estado", estado);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public int updateRol(EmpresaOB empresa, int rol) {
		EntityManager em = emf.createEntityManager();
		try {

			Query query = em.createNamedQuery("updateRol");
			query.setParameter("empresa", empresa);
			query.setParameter("rol", rol);


			em.getTransaction().begin();
			int updatedCount = query.executeUpdate();
			em.getTransaction().commit();

			return updatedCount;
		} catch (Exception e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			System.out.println("Error updating rol: " + e.getMessage());
			return 0;
		} finally {
			em.close();
		}
	}

	public List<EmpresaUsuarioOB> findByUsuarioHabilitadoRol(RolOB rol, EstadoUsuarioOB estado) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOB> typedQuery = em.createNamedQuery("EmpresaHabilitadosRol", EmpresaUsuarioOB.class);
			typedQuery.setParameter("rol", rol);
			typedQuery.setParameter("estado", estado);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}