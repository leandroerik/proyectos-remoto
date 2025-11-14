package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.PagoDeHaberesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class PagoAProveedoresOBRepositorio extends RepositorioGenericoImpl<PagoAProveedoresOB> {
    public List<PagoAProveedoresOB> filtrarMovimientosHistorial(EmpresaOB empresa, Fecha fechaDesde, Fecha fechaHasta, Integer convenio, Integer subconvenio, String nroAdherente, EstadosPagosAProveedoresOB estado,boolean previsualizacion) {
        EntityManager emx = emf.createEntityManager();
        try{
        LocalDateTime localFechaDesde = null;
        LocalDateTime localFechaHasta = null;

        if (fechaDesde != null && fechaHasta != null) {
            localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
            localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
        }


            TypedQuery<PagoAProveedoresOB> typedQuery = emx.createNamedQuery("PagoAProveedoresOB.filtrarMovimientosHistorial", PagoAProveedoresOB.class);
            typedQuery.setParameter("empCodigo", empresa);
            typedQuery.setParameter("fechaDesde", localFechaDesde);
            typedQuery.setParameter("fechaHasta", localFechaHasta);
            typedQuery.setParameter("convenio", convenio);
            typedQuery.setParameter("subconvenio", subconvenio);
            typedQuery.setParameter("nroAdherente", nroAdherente);
            typedQuery.setParameter("estado", estado);
            if (previsualizacion) typedQuery.setMaxResults(5);
            Thread.sleep(1);
            List<PagoAProveedoresOB> resultList = typedQuery.getResultList();
            emx.close();
            return resultList;
        } catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
        finally {
            emx.close();
        }

    }

    public List<PagoAProveedoresOB> filtrarEstadoYBandeja(EstadosPagosAProveedoresOB estado, EstadoBandejaOB estadoBandeja, EmpresaOB empresa) {
            EntityManager entityManager = emf.createEntityManager();
            try{
            StringBuilder jpql = new StringBuilder("SELECT pap FROM PagoAProveedoresOB pap WHERE pap.estadoBandeja = :estadoBandeja AND pap.emp_codigo = :empresa");
            if (estado != null) {
                jpql.append(" AND pap.estado = :estado");
            }
            TypedQuery<PagoAProveedoresOB> query = entityManager.createQuery(jpql.toString(), PagoAProveedoresOB.class)
                    .setParameter("estadoBandeja", estadoBandeja)
                    .setParameter("empresa", empresa);
            if (estado != null) {
                query.setParameter("estado", estado);
            }
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
     
        List<PagoAProveedoresOB> resultList = query.getResultList();
        entityManager.close();
            return resultList;}
            finally {
                entityManager.close();
            }
    }

    public List<PagoAProveedoresOB> buscarPorEstado(EstadosPagosAProveedoresOB estado) {
        EntityManager em = emf.createEntityManager();

        try {
            TypedQuery<PagoAProveedoresOB> typedQuery = em.createNamedQuery("PagoAProveedoresOB.buscarPorEstado", PagoAProveedoresOB.class);
            typedQuery.setParameter("estado", estado);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public List<PagoAProveedoresOB> existeArchivo(String nombreArchivo) {
        return findByField("nombreArchivo", nombreArchivo);
    }

    public PagoAProveedoresOB buscarPorNroLoteYEmpresa(EmpresaOB empresa, String nroLote) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<PagoAProveedoresOB> typedQuery = em.createNamedQuery("PagoAProveedoresOB.buscarPorNroLoteYEmpresa", PagoAProveedoresOB.class);
            typedQuery.setParameter("empCodigo", empresa);
            typedQuery.setParameter("nroLote", nroLote);

            return typedQuery.getSingleResult();
        } finally {
            em.close();
        }
    }

}
