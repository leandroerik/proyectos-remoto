package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuariosEmpresasActivoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class UsuariosEmpresasActivoOBRepositorio extends RepositorioGenericoImpl<UsuariosEmpresasActivoOB>{

	public UsuariosEmpresasActivoOB findByUsuarioEmpresaActivo(UsuarioOB usuario, EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<UsuariosEmpresasActivoOB> typedQuery = em.createNamedQuery("UsuariosEmpresasActivo", UsuariosEmpresasActivoOB.class);
			typedQuery.setParameter("usuario", usuario);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}
}
