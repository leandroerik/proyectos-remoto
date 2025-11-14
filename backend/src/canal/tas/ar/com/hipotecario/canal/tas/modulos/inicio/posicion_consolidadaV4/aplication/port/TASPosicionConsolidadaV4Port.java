package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public interface TASPosicionConsolidadaV4Port {
    Objeto getPosicionConsolidadaV4(ContextoTAS contexto, String idCliente);
    Objeto getConsolidadaTCV4(ContextoTAS contexto, String idCliente);
}
