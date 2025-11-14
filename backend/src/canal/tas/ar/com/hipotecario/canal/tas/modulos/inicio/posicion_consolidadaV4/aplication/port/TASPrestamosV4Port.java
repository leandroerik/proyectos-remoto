package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public interface TASPrestamosV4Port {

    Objeto getDatosPrestamo(ContextoTAS contextoTAS, String idCliente, String tipoEstado, boolean buscaNsp);
}
