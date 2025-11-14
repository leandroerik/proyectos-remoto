package ar.com.hipotecario.canal.rewards.modulos;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;

public class RWAplicacion {
    
    public static Objeto health(ContextoRewards contexto) {
        Objeto objeto = new Objeto();
        objeto.set("status", "UP");
        return objeto;
    }
}
