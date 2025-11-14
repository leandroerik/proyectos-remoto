package ar.com.hipotecario.canal.officebanking.util;

import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.ModuloOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.backend.base.Objeto;

public class ValidarCorreoYCelular extends ModuloOB {

	public static Objeto validarCorreoYCelular(ContextoOB contexto, String email, String telefonoMovil) {
		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
 
		if (email != null && !empty(servicioUsuario.findByEmail(email).tryGet())) {
			return respuesta("EMAIL_REGISTRADO");
		}
 
		if (telefonoMovil != null && !(servicioUsuario.findByCelular(telefonoMovil).tryGet().isEmpty())) {
			return respuesta("CELULAR_REGISTRADO");
		}
 
		return respuesta("0");
	}
}
