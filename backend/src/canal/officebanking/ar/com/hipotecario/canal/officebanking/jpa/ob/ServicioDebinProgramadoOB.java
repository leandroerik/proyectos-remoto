package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.ConceptoDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.DebinProgramadoOBRepositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServicioDebinProgramadoOB extends ServicioOB{


    private static DebinProgramadoOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
    ServicioEstadoDebinRecibidasOB servicioEstadoDebinRecibidasOB = new ServicioEstadoDebinRecibidasOB(contexto);

    public ServicioDebinProgramadoOB(ContextoOB contexto) {
        super(contexto);
        repo = new DebinProgramadoOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }
    public Futuro<DebinProgramadoOB> update(DebinProgramadoOB debin) {
        return futuro(() -> repo.update(debin));
    }
    public Futuro<DebinProgramadoOB> create(DebinProgramadoOB debin) {
        return futuro(() -> repo.create(debin));
    }

    public Futuro<DebinProgramadoOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }

    public Futuro<DebinProgramadoOB> findByIdDebin(String idDebin) {
        return futuro(() -> repo.findByIdDebin(idDebin));
    }

}
