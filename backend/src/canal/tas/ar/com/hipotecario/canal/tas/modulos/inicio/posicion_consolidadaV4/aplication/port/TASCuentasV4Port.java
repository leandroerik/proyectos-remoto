package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public interface TASCuentasV4Port {
    Objeto getDatosCuentas(ContextoTAS contexto, String idCliente, boolean acuerdo, boolean firmaConjunta, boolean consultaAlias,boolean firmantes, String tipoEstado);
}
