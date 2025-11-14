package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.mocks.MockTCOB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Optional;

public class MockTCOBRepositorio extends RepositorioGenericoImpl<MockTCOB> {
    public MockTCOB findByIdPrisma(String idPrisma) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> typedQuery = em.createNamedQuery("MockTCOB.findByIdPrisma", MockTCOB.class);
            typedQuery.setParameter("idPrisma", idPrisma);
            return typedQuery.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
    public MockTCOB findStopDebitByCuenta(String numeroCuenta) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> typedQuery = em.createNamedQuery("MockTCOB.findStopDebitByCuenta", MockTCOB.class);
            typedQuery.setParameter("numeroCuenta", numeroCuenta);
            return typedQuery.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public MockTCOB findByIdPrismaYCuenta(String idPrisma, String cuenta) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> query = em.createNamedQuery("MockTCOB.findByIdPrismaYCuenta", MockTCOB.class);
            query.setParameter("idPrisma", idPrisma);
            query.setParameter("cuenta", cuenta);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public MockTCOB findListadoTarjetasByIdPrismaYCuenta(String idPrisma, String cuenta) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> query = em.createNamedQuery("MockTCOB.findListadoTarjetasByIdPrismaYCuenta", MockTCOB.class);
            query.setParameter("idPrisma", idPrisma);
            query.setParameter("cuenta", cuenta);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
    public MockTCOB findVencimientoTarjetasByIdPrismaYCuenta(String idPrisma, String cuenta) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> query = em.createNamedQuery("MockTCOB.findVencimientoTarjetasByIdPrismaYCuenta", MockTCOB.class);
            query.setParameter("idPrisma", idPrisma);
            query.setParameter("cuenta", cuenta);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
    public MockTCOB findTransaccionesByIdPrismaYRequest(String idPrisma, String requestBody) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> query = em.createNamedQuery("MockTCOB.findTransaccionesByIdPrismaYRequest", MockTCOB.class);
            query.setParameter("idPrisma", idPrisma);
            query.setParameter("requestBody", requestBody);
            return query.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public MockTCOB obtenerListadoPoridcliente(String idCliente) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<MockTCOB> typedQuery = em.createNamedQuery("MockTCOB.findByIdCliente", MockTCOB.class);
            typedQuery.setParameter("idCliente", idCliente);
            return typedQuery.getResultStream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }
}
