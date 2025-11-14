package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class PermisoOBRepositorio extends RepositorioGenericoImpl<PermisoOB> {

	public List<PermisoOB> permisos() {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<PermisoOB> typedQuery = em.createNamedQuery("PermisoOB.permisos", PermisoOB.class);
			List<PermisoOB> resultList = typedQuery.getResultList();
			return resultList;
		} finally {
			em.close();
		}
	}

	public List<PermisoOB> permisosSinAsignar(PermisoOB permiso, EmpresaUsuarioOB empresaUsuario) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<PermisoOB> typedQuery = em.createNamedQuery("PermisoOB.permisosSinAsignarAEmpresa", PermisoOB.class);
			typedQuery.setParameter("permiso", permiso);
			typedQuery.setParameter("empresaUsuario", empresaUsuario);
			List<PermisoOB> result = typedQuery.getResultList();
			return result;
		} finally {
			em.close();
		}
	}

	public List<PermisoOB> permisosAsignados(PermisoOB permiso, EmpresaUsuarioOB empresaUsuario) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<PermisoOB> typedQuery = em.createNamedQuery("PermisoOB.permisosAsignadoAEmpresa", PermisoOB.class);
			typedQuery.setParameter("permiso", permiso);
			typedQuery.setParameter("empresaUsuario", empresaUsuario);
			List<PermisoOB> result = typedQuery.getResultList();
			return result;
		} finally {
			em.close();
		}
	}
}