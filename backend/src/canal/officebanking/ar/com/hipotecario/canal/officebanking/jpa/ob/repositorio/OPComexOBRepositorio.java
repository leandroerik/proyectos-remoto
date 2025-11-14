package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.EstadoOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.OrdenPagoComexOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class OPComexOBRepositorio  extends RepositorioGenericoImpl<OrdenPagoComexOB> {
	 private static final int CANTIDAD_RESULTADOS_PREVISUALIZACION = 5;
	    
	    public List<OrdenPagoComexOB> buscarPorEstado(EstadoOPComexOB estado) {
			EntityManager em = emf.createEntityManager();

			try {
				TypedQuery<OrdenPagoComexOB> typedQuery = em.createNamedQuery("OrdenPagoComexOB.buscarPorEstado", OrdenPagoComexOB.class);
				typedQuery.setParameter("estado", estado);
				return typedQuery.getResultList();
			} finally {
				em.close();
			}
	    }

		public OrdenPagoComexOB buscarPorTRR(String numeroTRR) {
			EntityManager em = emf.createEntityManager();

			try {
				TypedQuery<OrdenPagoComexOB> typedQuery = em.createNamedQuery("OrdenPagoComexOB.buscarPorTRR", OrdenPagoComexOB.class);
				typedQuery.setParameter("numeroTRR", numeroTRR);
				typedQuery.setMaxResults(1);
				return typedQuery.getSingleResult();
			} finally {
				em.close();
			}
		}

	    public List<OrdenPagoComexOB> filtrarOrdenesPagosHistorial(EmpresaOB empresa, String cuenta, Fecha fechaDesde, Fecha fechaHasta, boolean previsualizacion) {
	        EntityManager em = emf.createEntityManager();

	        LocalDateTime localFechaDesde = null;
	        LocalDateTime localFechaHasta = null;

	        if (fechaDesde != null && fechaHasta != null) {
	            localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
	            localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
	        }

	        try {
	            TypedQuery<OrdenPagoComexOB> typedQuery = em.createNamedQuery("OrdenPagoComexOB.filtrarOrdenesPagosHistorial", OrdenPagoComexOB.class);
	            typedQuery.setParameter("empresa", empresa);
	            typedQuery.setParameter("fechaDesde", localFechaDesde);
	            typedQuery.setParameter("fechaHasta", localFechaHasta);
	            typedQuery.setParameter("cuenta", cuenta);
	            if (previsualizacion){
	                typedQuery.setMaxResults(CANTIDAD_RESULTADOS_PREVISUALIZACION);
	            }
	            return typedQuery.getResultList();
	        } finally {
	            em.close();
	        }
	    }

	    public List<OrdenPagoComexOB> listarCuentas(EmpresaOB empresa) {
	        EntityManager em = emf.createEntityManager();

	        try {
	            TypedQuery<OrdenPagoComexOB> typedQuery = em.createNamedQuery("OrdenPagoComexOB.listarCuentas", OrdenPagoComexOB.class);
	            typedQuery.setParameter("empresa", empresa);
	            return typedQuery.getResultList();
	        } finally {
	            em.close();
	        }
	    }
}
