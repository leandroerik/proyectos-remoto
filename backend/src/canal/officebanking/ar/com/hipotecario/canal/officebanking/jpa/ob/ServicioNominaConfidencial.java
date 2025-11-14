package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.NominaConfidencialOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.NominaConfidencialOBRepositorio;

import java.time.LocalDateTime;
import java.util.List;

public class ServicioNominaConfidencial extends ServicioOB {
    private NominaConfidencialOBRepositorio repo;

    public ServicioNominaConfidencial(ContextoOB contexto) {
        super(contexto);
        repo = new NominaConfidencialOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<NominaConfidencialOB> findByUsuarioEmpresa(UsuarioOB usuario, EmpresaOB empresa) {
        return futuro(() -> repo.findByUsuarioEmpresa(usuario, empresa));
    }

    public Futuro<List<NominaConfidencialOB>> findByEmpresa(EmpresaOB empresa) {
        return futuro(() -> repo.findByEmpresa(empresa));
    }

    public Futuro<Integer> eliminarUsuario(Integer emp_codigo) {
        return futuro(() -> repo.eliminarUsuario(emp_codigo));
    }

    public Futuro<NominaConfidencialOB> crearUsuario(NominaConfidencialOB nuevoUsuario) {
        return futuro(() -> repo.create(nuevoUsuario));
    }

    public Futuro<NominaConfidencialOB> update(NominaConfidencialOB nominaConfidencialOB) {
        nominaConfidencialOB.fechaModificacion = LocalDateTime.now();
        return futuro(() -> repo.update(nominaConfidencialOB));
    }

}
