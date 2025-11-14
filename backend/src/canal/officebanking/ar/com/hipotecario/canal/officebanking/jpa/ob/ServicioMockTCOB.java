package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.mocks.MockTCOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.MockTCOBRepositorio;

public class ServicioMockTCOB extends  ServicioOB{
    private final MockTCOBRepositorio repo;

    public ServicioMockTCOB(ContextoOB contexto) {
        super(contexto);
        repo = new MockTCOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }


    public MockTCOB obtenerRespuestaPorIdPrisma(String idPrisma) {
        return repo.findByIdPrisma(idPrisma);
    }
    public MockTCOB obtenerRespuestaStopDebit(String cuenta) {
        return repo.findStopDebitByCuenta(cuenta);
    }
    public MockTCOB obtenerVencimientoPorIdPrismaYCuenta(String idPrisma, String cuenta) {
        return repo.findVencimientoTarjetasByIdPrismaYCuenta(idPrisma, cuenta);
    }

    public MockTCOB obtenerListadoTarjetasPorIdPrismaYCuenta(String idPrisma, String cuenta) {
        return repo.findListadoTarjetasByIdPrismaYCuenta(idPrisma, cuenta);
    }

    public MockTCOB obtenerTransaccionesPorIdPrismaYRequest(String idPrisma, String requestBody) {
        return repo.findTransaccionesByIdPrismaYRequest(idPrisma, requestBody);
    }

    public MockTCOB obtenerListadoPoridcliente(String idcliente) {
        return repo.obtenerListadoPoridcliente(idcliente);
    }
}

