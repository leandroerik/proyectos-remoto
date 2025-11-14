package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.DebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.EstadosDebinLoteOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class DebinLoteOBRepositorio extends RepositorioGenericoImpl<DebinLoteOB> {

    public List<DebinLoteOB> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, EstadosDebinLoteOB estado) {
        EntityManager em = emf.createEntityManager();

        LocalDateTime localFechaDesde = null;
        LocalDateTime localFechaHasta = null;

        if (fechaDesde != null && fechaHasta != null) {
            localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
            localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
        }
        try{
            TypedQuery<DebinLoteOB> typedQuery = em.createNamedQuery("DebinLoteOB.filtrarMovimientosHistorial", DebinLoteOB.class);
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

    public List<DebinLoteOB> existeArchivo(String nombreArchivo) {
        return findByField("nombreArchivo", nombreArchivo);
    }

    public List<DebinLoteOB> buscarPorFechaCreacion(LocalDate fechaCreacion){
        EntityManager em = emf.createEntityManager();
        TypedQuery<DebinLoteOB> query = em.createNamedQuery("DebinLoteOB.buscarPorFechaCreacion", DebinLoteOB.class);
        query.setParameter("fechaInicio", fechaCreacion.atStartOfDay());
        query.setParameter("fechaFin", fechaCreacion.plusDays(1).atStartOfDay());
        return query.getResultList();
    }
}
