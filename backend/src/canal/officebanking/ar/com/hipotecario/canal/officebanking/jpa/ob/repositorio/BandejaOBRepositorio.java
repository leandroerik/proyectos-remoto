package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;
import java.time.LocalDateTime;
		import java.time.ZoneId;
		import java.util.List;

		import ar.com.hipotecario.backend.base.Fecha;
		import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
		import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
		import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
		import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
		import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
		import jakarta.persistence.EntityManager;
		import jakarta.persistence.TypedQuery;

		public class BandejaOBRepositorio extends RepositorioGenericoImpl<BandejaOB> {

		    protected EntityManager entityManager;

		    public void setEntityManager(EntityManager entityManager) {
		        this.entityManager = entityManager;
		    }

		    public List<BandejaOB> buscarPendientesDeFirma(EmpresaOB empresa, EstadoBandejaOB estado1, EstadoBandejaOB estado2, Fecha fechaDesde, Fecha fechaHasta, MonedaOB moneda, Integer codProducto, String cuenta, String tipoSolicitud) {
		        EntityManager em = emf.createEntityManager();

		        LocalDateTime localFechaDesde;
		        LocalDateTime localFechaHasta;

		        if (fechaDesde != null && fechaHasta != null) {
		            localFechaDesde = fechaDesde.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
		            localFechaHasta = fechaHasta.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		        } else {
		            localFechaDesde = Fecha.ahora().FechaDate().toInstant().atZone(ZoneId.systemDefault()).minusDays(90).toLocalDate().atStartOfDay();
		            localFechaHasta = Fecha.ahora().FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
		        }

		        try {
		            TypedQuery<BandejaOB> typedQuery = em.createNamedQuery("BandejaOB.buscarPendientesDeFirma", BandejaOB.class);
		            typedQuery.setParameter("emp_codigo", empresa);
		            typedQuery.setParameter("idEstado1", estado1.id);
		            typedQuery.setParameter("idEstado2", estado2.id);
		            typedQuery.setParameter("fechaDesde", localFechaDesde);
		            typedQuery.setParameter("fechaHasta", localFechaHasta);
		            typedQuery.setParameter("moneda", moneda);
		            typedQuery.setParameter("codProducto", codProducto);
		            typedQuery.setParameter("cuenta", cuenta);
		            typedQuery.setParameter("tipoSolicitud", tipoSolicitud);
		            return typedQuery.getResultList();
		        } finally {
		            em.close();
		        }
		    }

		}