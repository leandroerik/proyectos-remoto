package ar.com.hipotecario.backend.servicio.api.CVU;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import static ar.com.hipotecario.backend.base.Util.futuro;

public class ApiCVU {

    public static Futuro<getCVU> getCVU(ContextoOB contexto, String cvu){
        return futuro(()-> getCVU.get(contexto,cvu));
    }

    public static Futuro<AliasCVU> getAliasCVU(ContextoOB contexto, String cvu){
        return futuro(()->AliasCVU.get(contexto,cvu));
    }
}
