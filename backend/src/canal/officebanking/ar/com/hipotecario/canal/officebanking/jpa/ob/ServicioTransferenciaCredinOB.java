package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaCredinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TransferenciaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TransferenciaCredinOBRepositorio;
import jakarta.persistence.EntityManager;

public class ServicioTransferenciaCredinOB extends ServicioOB{
    private static TransferenciaCredinOBRepositorio repo;
    private ContextoOB contexto;

    public ServicioTransferenciaCredinOB(ContextoOB contexto){
        super(contexto);
        repo = new TransferenciaCredinOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
        this.contexto = contexto;
    }

    public Futuro<TransferenciaCredinOB>  create(TransferenciaCredinOB transferencia){
        return futuro(()-> repo.create(transferencia));
    }
    public Futuro<TransferenciaCredinOB> find (int id){
        return futuro(()-> repo.find(id));
    }
    public TransferenciaCredinOB crearDesdeTransferenciaOB(Integer idTransferencia){
         repo.cargarDesdeTransferencia(idTransferencia);
         return this.find(idTransferencia).get();
    }
}
