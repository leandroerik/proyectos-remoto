package ar.com.hipotecario.backend.servicio.api.tarjetasdebito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiTarjetasDebitoBB extends Api {
    public static String API = "tarjetasdebito";

    public static Futuro<TarjetasDebitoBB> get(Contexto contexto, String idCobis) {
        return futuro(() -> TarjetasDebitoBB.get(contexto, idCobis));
    }
}