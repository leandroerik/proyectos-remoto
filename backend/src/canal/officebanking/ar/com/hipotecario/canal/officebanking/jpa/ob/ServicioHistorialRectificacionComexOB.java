package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.HistorialRectificacionComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.HistorialRectificacionComexOBRepositorio;

public class ServicioHistorialRectificacionComexOB extends ServicioOB {
    private HistorialRectificacionComexOBRepositorio repo;

    public ServicioHistorialRectificacionComexOB(ContextoOB contexto){
        super(contexto);
        repo = new HistorialRectificacionComexOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<HistorialRectificacionComexOB> crear(Long idAnterior, Long idNuevo, Character rectificacion){
        return futuro(()->repo.create(new HistorialRectificacionComexOB(idAnterior,idNuevo,rectificacion)));
    }




}

