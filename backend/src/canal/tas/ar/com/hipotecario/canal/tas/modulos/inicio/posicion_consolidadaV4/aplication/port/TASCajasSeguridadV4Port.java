package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public interface TASCajasSeguridadV4Port {
    Objeto getDatosCajas(ContextoTAS contexto, String idCliente, boolean cancelados);

}
