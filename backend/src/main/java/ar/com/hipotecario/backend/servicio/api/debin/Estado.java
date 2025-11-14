package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Estado extends ApiObjeto {
    public String codigo;
    public String descripcion;
    
    public Estado(String codigo) {
		this.codigo = codigo;
	}
}
