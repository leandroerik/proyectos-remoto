package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TerminosYCondicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TerminosYCondicionesOBRepositorio;

public class ServicioTerminosYCondicionesOB extends ServicioOB {
    private static TerminosYCondicionesOBRepositorio repo;

    public ServicioTerminosYCondicionesOB(ContextoOB contexto) {
        super(contexto);
        repo = new TerminosYCondicionesOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<TerminosYCondicionesOB> crear(ContextoOB contexto, TerminosYCondicionesOB terminosCondiciones) {
        return futuro(() -> repo.create(terminosCondiciones));
    }

    public Futuro<Boolean> buscarPorEmpresaYCuenta(EmpresaOB empresa, String numeroCuenta) {
        return futuro(() -> repo.buscarPorEmpresaYCuenta(empresa, numeroCuenta));
    }
}
