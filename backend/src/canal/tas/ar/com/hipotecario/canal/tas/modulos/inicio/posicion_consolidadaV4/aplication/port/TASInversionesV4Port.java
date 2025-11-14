package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public interface TASInversionesV4Port {

    Objeto getDatosInversiones(ContextoTAS contexto, String idCliente, String estado, boolean firmantes);
}
