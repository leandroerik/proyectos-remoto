package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public interface TASTarjetasDebitoV4Port {
    Objeto getDatosTD(ContextoTAS contextoTAS, String idCliente, String tipoEstado);

}
