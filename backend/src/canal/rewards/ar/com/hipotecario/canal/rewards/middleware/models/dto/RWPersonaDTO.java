package ar.com.hipotecario.canal.rewards.middleware.models.dto;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWCliente;

public class RWPersonaDTO extends Objeto {
    private RWCliente datosCliente;

    public RWCliente getDatosCliente() {
        return datosCliente;
    }

    public void setDatosCliente(RWCliente datosCliente) {
        this.datosCliente = datosCliente;
    }

}
