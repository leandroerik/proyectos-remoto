package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.servicio.api.link.Pagos;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.EstadoPagosHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class PagoDeHaberesOBRepositorio extends RepositorioGenericoImpl<PagoDeHaberesOB> {

	public List<PagoDeHaberesOB> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, String producto, EstadoPagosHaberesOB estado) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		try {
			TypedQuery<PagoDeHaberesOB> typedQuery = em.createNamedQuery("PagoDeHaberesOB.filtrarMovimientosHistorial", PagoDeHaberesOB.class);
			typedQuery.setParameter("empCodigo", empresa);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			typedQuery.setParameter("convenio", convenio);
			typedQuery.setParameter("producto", producto);
			typedQuery.setParameter("estado", estado);

			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<PagoDeHaberesOB> buscarAcreditacionesSinFirmaAFechaArchivo(EstadoPagosHaberesOB estadoEnBandeja) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<PagoDeHaberesOB> typedQuery = em.createNamedQuery("PagoDeHaberesOB.buscarAcreditacionesSinFirmaAFechaArchivo", PagoDeHaberesOB.class);
			typedQuery.setParameter("estado", estadoEnBandeja);
			typedQuery.setParameter("fechaHoy", LocalDate.now());
			typedQuery.setParameter("tipoProducto", "Plan Sueldo");

			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<PagoDeHaberesOB> buscarArchivo(String archivo, Integer emp_codigo) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<PagoDeHaberesOB> typedQuery = em.createNamedQuery("PagoDeHaberesOB.buscarArchivo", PagoDeHaberesOB.class);
			typedQuery.setParameter("archivo", archivo);
			typedQuery.setParameter("estado", EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo());
			typedQuery.setParameter("empresa", emp_codigo);

			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public List<PagoDeHaberesOB> buscarArchivoContains(String archivo, Integer emp_codigo) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<PagoDeHaberesOB> typedQuery = em.createNamedQuery("PagoDeHaberesOB.buscarArchivoContains", PagoDeHaberesOB.class);
			typedQuery.setParameter("archivo", archivo);
			typedQuery.setParameter("estado", EnumEstadoPagosHaberesOB.RECHAZADO.getCodigo());
			typedQuery.setParameter("empresa", emp_codigo);

			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}

	public PagoDeHaberesOB buscarByNombre(String nombreArchivo) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<PagoDeHaberesOB> typedQuery = em.createNamedQuery("PagoDeHaberesOB.buscarByNombre", PagoDeHaberesOB.class);
			typedQuery.setParameter("archivo", nombreArchivo);
			typedQuery.setMaxResults(1);
			return typedQuery.getSingleResult();
		} finally {
			em.close();
		}
	}
}
