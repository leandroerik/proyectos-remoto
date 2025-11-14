package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoSolicitudInversionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.SolicitudPerfilInversorOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class PerfilInversorOBRepositorio extends RepositorioGenericoImpl<SolicitudPerfilInversorOB> {
	public List<SolicitudPerfilInversorOB> buscarPorEmpresaYEstado(EmpresaOB empresa, EstadoSolicitudInversionOB estadoPendiente) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<SolicitudPerfilInversorOB> typedQuery = em.createNamedQuery("SolicitudPerfilInversorOB.buscarPorEmpresaYEstado", SolicitudPerfilInversorOB.class);
			typedQuery.setParameter("idEmpresa", empresa);
			typedQuery.setParameter("idEstadoPendiente", estadoPendiente.id);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}
