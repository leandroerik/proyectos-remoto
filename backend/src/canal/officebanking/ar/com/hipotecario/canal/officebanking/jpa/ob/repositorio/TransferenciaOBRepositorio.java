package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.dto.PaginaTransferenciaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.EstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class TransferenciaOBRepositorio extends RepositorioGenericoImpl<TransferenciaOB> {

	public PaginaTransferenciaDTO find(EmpresaOB empresa, int numeroPagina, int registrosPorPagina, String beneficiario, Integer idEstado, Fecha fechaDesde, Fecha fechaHasta) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		PaginaTransferenciaDTO result = new PaginaTransferenciaDTO();
		try {
			TypedQuery<TransferenciaOB> typedQuery = em.createNamedQuery("TransferenciaOB.find", TransferenciaOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("beneficiario", beneficiario == null ? "" : beneficiario);
			typedQuery.setParameter("idEstado", idEstado == null ? 0 : idEstado);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);

			int pagina = numeroPagina - 1;

			List<TransferenciaOB> resultList = typedQuery.getResultList();
			result.cantidad = resultList.size();

			typedQuery.setFirstResult(pagina * registrosPorPagina);
			typedQuery.setMaxResults(registrosPorPagina);

			result.transferencias = typedQuery.getResultList();

			result.numeroPagina = numeroPagina;
			result.registroPorPagina = registrosPorPagina;

			return result;

		} finally {
			em.close();
		}
	}

	public List<TransferenciaOB> buscarPorDosEstadosEntreFechaAplicacion(EmpresaOB empresa, EstadoTRNOB estado1, EstadoTRNOB estado2, Fecha fechaDesde, Fecha fechaHasta) {
		EntityManager em = emf.createEntityManager();

		LocalDate localFechaDesde;
		LocalDate localFechaHasta;
		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} else {
			localFechaDesde = LocalDate.now().minusDays(90);
			localFechaHasta = LocalDate.now();
		}

		try {
			TypedQuery<TransferenciaOB> typedQuery = em.createNamedQuery("TransferenciaOB.findByTwoStatesBetweenApplicationDate", TransferenciaOB.class);
			typedQuery.setParameter("emp_codigo", empresa);
			typedQuery.setParameter("idEstado1", estado1.id);
			typedQuery.setParameter("idEstado2", estado2.id);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<TransferenciaOB> buscarPorEstadoYFechaDeAplicacion(EstadoTRNOB estado, LocalDate fechaAplicacion) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<TransferenciaOB> typedQuery = em.createNamedQuery("TransferenciaOB.findByStateAndApplicationDate", TransferenciaOB.class);
			typedQuery.setParameter("idEstado", estado.id == null ? 0 : estado.id);
			typedQuery.setParameter("fechaAplicacion", fechaAplicacion);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<TransferenciaOB> buscarSinFirmaPorVencer(int estadoEnBandeja, LocalDate fechaHoy) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<TransferenciaOB> typedQuery = em.createNamedQuery("TransferenciaOB.buscarSinFirmaPorVencer", TransferenciaOB.class);
			typedQuery.setParameter("estadoEnBandeja", estadoEnBandeja);
			typedQuery.setParameter("fechaHoy", fechaHoy);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<TransferenciaOB> existsTransferenciaByEmpresaAndCbu(EmpresaOB empresa, String cbu) {
		EntityManager em = emf.createEntityManager();
		TypedQuery<TransferenciaOB> query = em.createNamedQuery("TransferenciaOB.findByEmpresaAndCbu", TransferenciaOB.class);
		query.setParameter("empresa", empresa);
		query.setParameter("cbu", cbu);
		return query.getResultList();
	}

	public List<TransferenciaOB> findTransfersForTodayByEmpresaAndEstadoRechazado(int empCodigo) {
		EntityManager em = emf.createEntityManager();
		return em.createNamedQuery("TransferenciaOB.findTodayByEmpresaAndEstadoBandejaNotEqual", TransferenciaOB.class)
				.setParameter("empCodigo", empCodigo)
				.getResultList();
	}

}