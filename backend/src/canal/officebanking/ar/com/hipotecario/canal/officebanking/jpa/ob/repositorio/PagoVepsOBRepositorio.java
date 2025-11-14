package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class PagoVepsOBRepositorio extends RepositorioGenericoImpl<PagosVepOB> {

	public List<PagosVepOB> buscarPorNroVep(String numeroVep) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<PagosVepOB> typedQuery = em.createNamedQuery("PagosVepOB.buscarPorNroVep", PagosVepOB.class);
			typedQuery.setParameter("numeroVep", numeroVep);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<PagosVepOB> buscarPorEmpresaYFiltros(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, EstadoPagoOB estado, String numeroVep) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		try {
			TypedQuery<PagosVepOB> typedQuery = em.createNamedQuery("PagosVepOB.buscarPorEmpresaYFiltros", PagosVepOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("estado", estado.id);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			typedQuery.setParameter("numeroVep", numeroVep);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<PagosVepOB> buscarPorEmpresaYEnte(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, String ente) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		try {
			TypedQuery<PagosVepOB> typedQuery = em.createNamedQuery("PagosVepOB.buscarPorEmpresaYEnte", PagosVepOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			typedQuery.setParameter("ente", ente);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

    public List<PagosVepOB> buscarPorEstado(EstadoPagoOB estado) {
    	EntityManager em = emf.createEntityManager();

    	try {
    		TypedQuery<PagosVepOB> typedQuery = em.createNamedQuery("PagosVepOB.buscarPorEstado", PagosVepOB.class);
    		typedQuery.setParameter("estado", estado);
    		return typedQuery.getResultList();
    	} finally {
    		em.close();
    	}
	}
}
