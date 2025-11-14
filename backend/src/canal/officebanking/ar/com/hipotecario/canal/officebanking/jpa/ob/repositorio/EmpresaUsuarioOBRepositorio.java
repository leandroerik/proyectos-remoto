package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EstadoUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.RolOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class EmpresaUsuarioOBRepositorio extends RepositorioGenericoImpl<EmpresaUsuarioOB> {

	public List<EmpresaUsuarioOB> find(UsuarioOB usuario) {
		List<EmpresaUsuarioOB> usuariosEmpresas = this.findByField("usuario", usuario);
		return usuariosEmpresas;
	}

	public EmpresaUsuarioOB findByUsuarioEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOB> typedQuery = em.createNamedQuery("EmpresaUsuario", EmpresaUsuarioOB.class);
			typedQuery.setParameter("usuario", usuario);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}
	
	public List<EmpresaUsuarioOB> findUsuariosByEmpresa(EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOB> typedQuery = em.createNamedQuery("UsuariosEmpresa", EmpresaUsuarioOB.class);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<EmpresaUsuarioOB> findUsuariosByIdCobisEmpresa(String idCobisEmpresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOB> typedQuery = em.createNamedQuery("UsuariosPorIdCobisEmpresa", EmpresaUsuarioOB.class);
			typedQuery.setParameter("idCobisEmpresa", idCobisEmpresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<EmpresaUsuarioOB> findByRolEmpresa(RolOB rol, EmpresaOB empresa, EstadoUsuarioOB estado) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<EmpresaUsuarioOB> typedQuery = em.createNamedQuery("EmpresaRol", EmpresaUsuarioOB.class);
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