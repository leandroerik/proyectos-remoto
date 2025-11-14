package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ReferenciaPagoServicioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ReferenciaPagoDeServiciosOBRepositorio;

import java.time.LocalDateTime;
import java.util.List;

public class ServicioReferenciaPagoServiciosOB extends ServicioOB{

    private static ReferenciaPagoDeServiciosOBRepositorio repo;

    public ServicioReferenciaPagoServiciosOB(ContextoOB contexto){
        super(contexto);
        repo = new ReferenciaPagoDeServiciosOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<ReferenciaPagoServicioOB> crear(EmpresaOB empresa, String ente, String codigoLink,String referencia){
        ReferenciaPagoServicioOB referenciaPago = new ReferenciaPagoServicioOB();
        referenciaPago.empresa = empresa;
        referenciaPago.ente = ente;
        referenciaPago.codigoLink = codigoLink;
        referenciaPago.referencia = referencia;
        referenciaPago.fechaCreacion = LocalDateTime.now();
        referenciaPago.fechaModificacion = referenciaPago.fechaCreacion;

        return futuro(()->repo.create(referenciaPago));
    }

    public Futuro<List<ReferenciaPagoServicioOB>> buscarPorEmpresa(EmpresaOB empresa){
        return futuro(()->repo.findByField("empresa",empresa));
    }

    public Futuro<ReferenciaPagoServicioOB> buscar(EmpresaOB empresa,String ente,String codigoLink){
        return futuro(()->repo.findByFields(empresa,ente,codigoLink));
    }

    public Futuro<ReferenciaPagoServicioOB> actualizar(ReferenciaPagoServicioOB referencia){
        return futuro(()->repo.update(referencia));
    }
}
