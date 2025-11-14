package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.PagoDeServiciosOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class PagoDeServiciosOBRepositorio extends RepositorioGenericoImpl<PagoDeServiciosOB> {
    public List<PagoDeServiciosOB> buscarSinFirmaCambioDeDia(EstadoPagoOB estadoEnBandeja) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<PagoDeServiciosOB> typedQuery = em.createNamedQuery("PagoDeServiciosOB.buscarSinFirmaCambioDeDia", PagoDeServiciosOB.class);
            typedQuery.setParameter("estadoEnBandeja", estadoEnBandeja);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

	public List<PagoDeServiciosOB> buscarPorCpeYEstado(String ente, String codigoLink, int estado, EmpresaOB empresa) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<PagoDeServiciosOB> typedQuery = em.createNamedQuery("PagoDeServiciosOB.buscarPorCpeEstadoYEmpresa", PagoDeServiciosOB.class);
			typedQuery.setParameter("estado", estado);
			typedQuery.setParameter("ente", ente);
			typedQuery.setParameter("codigoLink", codigoLink);
			typedQuery.setParameter("empresa", empresa);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<PagoDeServiciosOB> buscarPorEmpresaYFiltros(EmpresaOB empresa, String ente, Fecha fechaDesde, Fecha fechaHasta, EstadoPagoOB estado, String codigoLink) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		try {
			TypedQuery<PagoDeServiciosOB> typedQuery = em.createNamedQuery("PagoDeServiciosOB.buscarPorEmpresaYFiltros", PagoDeServiciosOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("ente", ente);
			typedQuery.setParameter("estado", estado.id);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			typedQuery.setParameter("codigoLink", codigoLink);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
	
	public List<PagoDeServiciosOB> buscarPorEmpresaYEnte(EmpresaOB empresa, String ente, Fecha fechaDesde, Fecha fechaHasta) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		try {
			TypedQuery<PagoDeServiciosOB> typedQuery = em.createNamedQuery("PagoDeServiciosOB.buscarPorEmpresaYEnte", PagoDeServiciosOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("ente", ente);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}


	public List<PagoDeServiciosOB> buscarPorEmpresaYEstadoCodigoLink(EmpresaOB empresa, int estado, String codigoLink) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<PagoDeServiciosOB> typedQuery = em.createNamedQuery("PagoDeServiciosOB.buscarPorEmpresaYEstadoCodigoLink", PagoDeServiciosOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("estado", estado);
			typedQuery.setParameter("codigoLink", codigoLink);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
}
