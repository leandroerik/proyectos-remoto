package ar.com.hipotecario.canal.officebanking;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EstadoUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

class OBFirmasTest {

	@Test
	void habilitaBandejaTrue() {
		// SesionOB sesion = new SesionOB();
		EmpresaOB empresaOB = new EmpresaOB();
		UsuarioOB usuarioOB = new UsuarioOB();
		EstadoUsuarioOB estadoUsuarioOB = new EstadoUsuarioOB();

		estadoUsuarioOB.codigo = 1;

		usuarioOB.numeroDocumento = 30124004L;
		usuarioOB.codigo = 38;
		usuarioOB.cuil = 20301240040L;
		usuarioOB.idCobis = "4639221";
		usuarioOB.estado = estadoUsuarioOB;

		empresaOB.emp_codigo = 27;
		empresaOB.cuit = 30612929455L;
		empresaOB.idCobis = "160";
		empresaOB.razonSocial = "LIBERTAD S A";

		// sesion.empresaOB = empresaOB;
		// sesion.usuarioOB = usuarioOB;
		// sesion.rol = "2";
		// sesion.idCobis = "4639221";

		ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
		contextoOB.sesion().usuarioOB = usuarioOB;
		contextoOB.sesion().empresaOB = empresaOB;
		contextoOB.parametros.set("empresa", 30612929455L);
		contextoOB.parametros.set("funcionalidadOB", 4);
		contextoOB.parametros.set("firmante", 20301240040L);

		Object respuesta = OBFirmas.habilitaBandeja(contextoOB);
		assertTrue(respuesta.toString().contains("\"habilitaBandeja\": true"));
	}

}