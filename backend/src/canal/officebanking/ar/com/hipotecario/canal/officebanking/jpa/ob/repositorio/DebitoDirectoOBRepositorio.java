package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.EstadosDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class DebitoDirectoOBRepositorio extends RepositorioGenericoImpl<DebitoDirectoOB> {
    private static final int CANTIDAD_RESULTADOS_PREVISUALIZACION = 5;
    public List<DebitoDirectoOB> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, EstadosDebitoDirectoOB estado,boolean previsualizacion) {
        EntityManager em = emf.createEntityManager();

        LocalDateTime localFechaDesde = null;
        LocalDateTime localFechaHasta = null;

        if (fechaDesde != null && fechaHasta != null) {
            localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
            localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
        }

        try {
            TypedQuery<DebitoDirectoOB> typedQuery = em.createNamedQuery("DebitoDirectoOB.filtrarMovimientosHistorial", DebitoDirectoOB.class);
            typedQuery.setParameter("empCodigo", empresa);
            typedQuery.setParameter("fechaDesde", localFechaDesde);
            typedQuery.setParameter("fechaHasta", localFechaHasta);
            typedQuery.setParameter("convenio", convenio);
            typedQuery.setParameter("estado", estado);
            if (previsualizacion){
                typedQuery.setMaxResults(CANTIDAD_RESULTADOS_PREVISUALIZACION);
            }
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }
    public List<DebitoDirectoOB> filtrarEstadoYBandeja(EstadosDebitoDirectoOB estado, EstadoBandejaOB estadoBandeja,EmpresaOB empresa){
        EntityManager entityManager = emf.createEntityManager();
        return entityManager.createQuery("SELECT dd FROM DebitoDirectoOB dd WHERE dd.estado = ?1 AND dd.estadoBandeja = ?2 AND dd.emp_codigo = ?3")
                .setParameter(1,estado)
                .setParameter(2,estadoBandeja)
                .setParameter(3,empresa)
                .getResultList();
    }
   
    public List<DebitoDirectoOB> buscarPorEstado(EstadosDebitoDirectoOB estado) {
		EntityManager em = emf.createEntityManager();

		try {
			TypedQuery<DebitoDirectoOB> typedQuery = em.createNamedQuery("DebitoDirectoOB.buscarPorEstado", DebitoDirectoOB.class);
			typedQuery.setParameter("estado", estado);
			return typedQuery.getResultList();
		} finally {
			em.close();
		}
    }

    public DebitoDirectoOB buscarPorNroLoteYEmpresa(EmpresaOB empresa, String nroLote) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<DebitoDirectoOB> typedQuery = em.createNamedQuery("DebitoDirectoOB.buscarPorNroLoteYEmpresa", DebitoDirectoOB.class);
            typedQuery.setParameter("empCodigo", empresa);
            typedQuery.setParameter("nroLote", nroLote);

            return typedQuery.getSingleResult();
        } finally {
            em.close();
        }
    }
    public List<DebitoDirectoOB> existeArchivo(String nombreArchivo){
        return findByField("nombreArchivo",nombreArchivo);
    }

    public List<DebitoDirectoOB> buscarPorFechaCreacion(LocalDate fechaCreacion){
        EntityManager em = emf.createEntityManager();
        TypedQuery<DebitoDirectoOB> query = em.createNamedQuery("DebitoDirectoOB.buscarPorFechaCreacion", DebitoDirectoOB.class);
        query.setParameter("fechaInicio", fechaCreacion.atStartOfDay());
        query.setParameter("fechaFin", fechaCreacion.plusDays(1).atStartOfDay());
        return query.getResultList();
    }


}
