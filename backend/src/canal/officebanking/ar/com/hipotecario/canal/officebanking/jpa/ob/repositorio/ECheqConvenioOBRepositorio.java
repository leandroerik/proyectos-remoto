package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.RepositorioGenericoImpl;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqConvenioOB;

public class ECheqConvenioOBRepositorio extends RepositorioGenericoImpl<EcheqConvenioOB> {
    @Override
    public List<EcheqConvenioOB> findByField(String field, Object value) {
        return super.findByField(field, value);
    }

//    public void delete(EcheqConvenioOB echeqConvenioOB) {
//        entityManager.remove(entityManager.contains(echeqConvenioOB) ? echeqConvenioOB : entityManager.merge(echeqConvenioOB));
//    }
    
}

