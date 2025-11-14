package ar.com.hipotecario.backend.servicio.api.clientes;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.clientes.Cliente.JubiladoPago;
import ar.com.hipotecario.backend.servicio.api.clientes.Cliente.JubiladoEstadoBeneficiario;

// http://api-clientes-microservicios-homo.appd.bh.com.ar/swagger-ui.html
public class ApiClientes extends Api {
    /* ========== CONSTANTES ========== */

    public static String API = "clientes";

    /* ========== SERVICIOS ========== */

    public static Futuro<JubiladoPago> jubiladoFechaProximoPago(Contexto contexto, String cuil) {
        return futuro(() -> Cliente.getJubiladoFechaProxPago(contexto, cuil));
    }

    public static Futuro<JubiladoEstadoBeneficiario> jubiladoEstadoBeneficiario(Contexto contexto, String cuil) {
        return futuro(() -> Cliente.getJubiladoEstadoBeneficiario(contexto, cuil));
    }
}
