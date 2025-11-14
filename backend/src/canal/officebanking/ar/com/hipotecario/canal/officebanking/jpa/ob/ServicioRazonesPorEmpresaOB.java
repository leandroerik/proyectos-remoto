package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.RazonesPorEmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.RazonesPorEmpresaOBRepositorio;

import java.util.List;

public class ServicioRazonesPorEmpresaOB extends ServicioOB {
    private static RazonesPorEmpresaOBRepositorio repo;

    public ServicioRazonesPorEmpresaOB(ContextoOB contexto) {
        super(contexto);
        repo = new RazonesPorEmpresaOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<RazonesPorEmpresaOB> create(EmpresaOB empresa, String cuit, String razonSocial, String email) {
        RazonesPorEmpresaOB razonesPorEmpresaOB = new RazonesPorEmpresaOB();
        razonesPorEmpresaOB.cuit = cuit;
        razonesPorEmpresaOB.razonSocial = razonSocial;
        razonesPorEmpresaOB.email = email;
        razonesPorEmpresaOB.emp_codigo = empresa;

        return futuro(() -> repo.create(razonesPorEmpresaOB));
    }

    public Futuro<List<RazonesPorEmpresaOB>> findByEmpresa(EmpresaOB empresaOB) {
        return futuro(() -> repo.findByField("emp_codigo", empresaOB));
    }
}
