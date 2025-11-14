package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class CobranzaIntegralOBRepositorio extends RepositorioGenericoImpl<CobranzaIntegralOB> {
    private static final int CANTIDAD_RESULTADOS_PREVISUALIZACION = 5;
    public List<CobranzaIntegralOB> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, EstadosCobranzaIntegralOB estado) {
        EntityManager em = emf.createEntityManager();

        LocalDateTime localFechaDesde = null;
        LocalDateTime localFechaHasta = null;

        if (fechaDesde != null && fechaHasta != null) {
            localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
            localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
        }

        try {
            TypedQuery<CobranzaIntegralOB> typedQuery = em.createNamedQuery("CobranzaIntegralOB.filtrarMovimientosHistorial", CobranzaIntegralOB.class);
            typedQuery.setParameter("empCodigo", empresa);
            typedQuery.setParameter("fechaDesde", localFechaDesde);
            typedQuery.setParameter("fechaHasta", localFechaHasta);
            typedQuery.setParameter("convenio", convenio);
            typedQuery.setParameter("estado", estado);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }
    public List<CobranzaIntegralOB> buscarPorEstado(EstadosCobranzaIntegralOB estado) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<CobranzaIntegralOB> typedQuery = em.createNamedQuery("CobranzaIntegralOB.buscarPorEstado", CobranzaIntegralOB.class);
            typedQuery.setParameter("estado", estado);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public List<CobranzaIntegralOB> existeArchivo(String nombreArchivo) {
        return findByField("nombreArchivo", nombreArchivo);
    }

    public List<CobranzaIntegralOB> buscarPorFechaHora(LocalDateTime inicio, LocalDateTime fin, Integer empCodigo) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<CobranzaIntegralOB> query = em.createNamedQuery("CobranzaIntegralOB.buscarPorFechaHora", CobranzaIntegralOB.class);
            query.setParameter("fechaInicio", inicio);
            query.setParameter("fechaFin", fin);
            query.setParameter("empCodigo", empCodigo);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<CobranzaIntegralOB> buscarPorFechaCreacion(LocalDate fechaCreacion){
        EntityManager em = emf.createEntityManager();
        TypedQuery<CobranzaIntegralOB> query = em.createNamedQuery("CobranzaIntegralOB.buscarPorFechaCreacion", CobranzaIntegralOB.class);
        query.setParameter("fechaInicio", fechaCreacion.atStartOfDay());
        query.setParameter("fechaFin", fechaCreacion.plusDays(1).atStartOfDay());
        return query.getResultList();
    }
}
