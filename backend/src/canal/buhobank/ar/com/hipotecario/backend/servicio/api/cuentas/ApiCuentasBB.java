package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiCuentasBB extends Api {
    public static String API = "cuentas";

    public static Futuro<CuentasBB> get(Contexto contexto, String idCobis) {
        return futuro(() -> CuentasBB.get(contexto, idCobis));
    }
}
