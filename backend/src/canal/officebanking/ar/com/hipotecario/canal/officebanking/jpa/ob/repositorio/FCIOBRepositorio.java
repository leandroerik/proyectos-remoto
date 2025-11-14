package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.FondosComunesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class FCIOBRepositorio extends RepositorioGenericoImpl<FondosComunesOB> {

	public List<FondosComunesOB> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, String cuenta, String tipoSolicitud, MonedaOB moneda) {
		EntityManager em = emf.createEntityManager();

		LocalDateTime localFechaDesde = null;
		LocalDateTime localFechaHasta = null;

		if (fechaDesde != null && fechaHasta != null) {
			localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
			localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		}

		try {
			TypedQuery<FondosComunesOB> typedQuery = em.createNamedQuery("FondosComunesOB.filtrarMovimientosHistorial", FondosComunesOB.class);
			typedQuery.setParameter("empCodigo", empresa);
			typedQuery.setParameter("fechaDesde", localFechaDesde);
			typedQuery.setParameter("fechaHasta", localFechaHasta);
			typedQuery.setParameter("tipoSolicitud", tipoSolicitud);
			typedQuery.setParameter("idCuotapartista", cuenta);
			typedQuery.setParameter("moneda", moneda);

			return typedQuery.getResultList();
		} finally {
			em.close();
		}

	}

	public List<FondosComunesOB> buscarSinFirmaCompletaPorVencer(int estadoPendiente) {
		EntityManager em = emf.createEntityManager();
		try {
			TypedQuery<FondosComunesOB> typedQuery = em.createNamedQuery("FondosComunesOB.buscarSinFirmaCompletaPorVencer", FondosComunesOB.class);
			typedQuery.setParameter("estadoPendiente", estadoPendiente);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
	}
	public FondosComunesOB buscarPorIdSolicitud(String idSolicitud) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<FondosComunesOB> typedQuery = em.createNamedQuery("FondosComunesOB.buscarIdSolicitud", FondosComunesOB.class);
            typedQuery.setParameter("idSolicitud", idSolicitud);
            return typedQuery.getSingleResult();
        } finally {
            em.close();
        }
    }

}
