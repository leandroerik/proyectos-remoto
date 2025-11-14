package ar.com.hipotecario.canal.officebanking;

//import static ar.com.hipotecario.mobile.lib.Objeto.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.DiaBancario;
//import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EstadoUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

class OBTransferenciasTest {

	public static final ContextoOB CONTEXTO_OB = new ContextoOB("OB", Config.ambiente(), "1");

	@Test
	void validarDiaHabilNoDiferidaOK() {
		CONTEXTO_OB.parametros.set("fechaProgramacion", "2023-03-22");
		Fecha fecha = new Fecha("2023-03-22", "yyyy-MM-dd");
		Fecha fechaPosterior = new Fecha("2023-03-23", "yyyy-MM-dd");

		DiaBancario dia = ApiCatalogo.diaBancario(CONTEXTO_OB, fecha).get();

		assertEquals("1", dia.esDiaHabil);
		assertEquals(fechaPosterior, dia.diaHabilPosterior);

		Object respuesta = OBTransferencias.validarDiaHabil(CONTEXTO_OB);
		assertTrue(respuesta.toString().contains("\"diferida\": false"));
		assertTrue(respuesta.toString().contains("\"habilPosterior\": null"));
	}

	@Test
	void validarCBUAliasCbuAliasOK() {
		CONTEXTO_OB.parametros.set("cbu", "0440000430000004074761");

		Object respuesta = OBTransferencias.validarCBUAlias(CONTEXTO_OB);
		assertTrue(respuesta.toString().contains("\"cbu\": \"0440000430000004074761\""));
	}

	@Test
	void validarCBUAliasFormatoInvalido() {
		CONTEXTO_OB.parametros.set("cbu", "1234");

		Object respuesta = OBTransferencias.validarCBUAlias(CONTEXTO_OB);
		assertTrue(respuesta.toString().contains("CBU_ALIAS_FORMATO_INVALIDO"));
	}

	@Test
	void validarCBUAliasCvuNoPermitido() {
		CONTEXTO_OB.parametros.set("cbu", "0001234");

		Object respuesta = OBTransferencias.validarCBUAlias(CONTEXTO_OB);
		assertTrue(respuesta.toString().contains("CVU_NO_PERMITIDO"));
	}

	@Test
	void validarCBUAliasCbuAliasInvalido() {
		/*
		 * CONTEXTO_OB.parametros.set("cbu", "0440000430000004074777");
		 * 
		 * ServicioBeneficiarioOB servicioBeneficiario = new
		 * ServicioBeneficiarioOB(CONTEXTO_OB); InfoCuentaDTO info =
		 * servicioBeneficiario.infoCBUAlias(CONTEXTO_OB, "0440000430000004074777",
		 * null);
		 * 
		 * assertTrue(empty(info)); Object respuesta =
		 * OBTransferencias.validarCBUAlias(CONTEXTO_OB);
		 * assertTrue(respuesta.toString().contains("CBU_ALIAS_INVALIDO"));
		 */
	}

	@Test
	void validarCBUAliasCbuAliasCuentaInactiva() {

		/*
		 * InfoCuentaDTO infoFixture = new InfoCuentaDTO(); infoFixture.numero =
		 * "300000000407476";
		 * 
		 * CuentaCoelsa cuentaFixture = new CuentaCoelsa(); cuentaFixture.cbu =
		 * "0440000430000004074761"; cuentaFixture.nombreTitular = "Romanth Bostwick";
		 * cuentaFixture.cuit = "30712101136"; cuentaFixture.nroBco = "044";
		 * cuentaFixture.ctaActiva = false; cuentaFixture.nuevoAlias = "pepino.banco.1";
		 * cuentaFixture.transaccion = "23007336"; cuentaFixture.tipoCuenta = "CC";
		 * cuentaFixture.tipoPersona = "J";
		 * 
		 * infoFixture.cuenta = cuentaFixture;
		 * 
		 * CONTEXTO_OB.parametros.set("cbu", "0440000430000004074761");
		 * 
		 * ServicioBeneficiarioOB servicioBeneficiario =
		 * mock(ServicioBeneficiarioOB.class);
		 * 
		 * when(servicioBeneficiario.infoCBUAlias(CONTEXTO_OB, "0440000430000004074761",
		 * null)).thenReturn(infoFixture);
		 * 
		 * assertFalse(cuentaFixture.ctaActiva); // Object respuesta =
		 * OBTransferencias.validarCBUAlias(contextoOB); //
		 * assertTrue(respuesta.toString().contains("CUENTA_INACTIVA"));
		 */
	}

	@Test
	void conceptosOK() {
		Objeto respuesta = (Objeto) OBTransferencias.conceptos(CONTEXTO_OB);
		assertNotNull(respuesta.get("conceptos"));
	}

	@Test
	void horarioCamaraOK() {
		DiaBancario dia = ApiCatalogo.diaBancario(CONTEXTO_OB, Fecha.hoy()).get();
		LocalTime horaInicio = LocalTime.of(8, 0, 0);
		LocalTime horaCierre = LocalTime.of(16, 0, 0);

		Object respuesta = OBTransferencias.horarioCamara(CONTEXTO_OB);

		if (dia.esDiaHabil.equals("1")) {
			if (LocalTime.now().isBefore(horaCierre) && LocalTime.now().isAfter(horaInicio)) {
				assertTrue(respuesta.toString().contains("\"diferida\": false"));
			} else
				assertTrue(respuesta.toString().contains("\"diferida\": true"));
		} else
			assertTrue(respuesta.toString().contains("\"diferida\": true"));

	}

	@Test
	void monedasOK() {
		Objeto respuesta = (Objeto) OBTransferencias.monedas(CONTEXTO_OB);
		assertNotNull(respuesta.get("monedas"));
	}

	@Test
	void estadosOK() {
		Objeto respuesta = (Objeto) OBTransferencias.estadosHistorialTrn(CONTEXTO_OB);
		assertNotNull(respuesta);
	}

	@Test
	void validarMontoOperacionInvalida() {
		CONTEXTO_OB.parametros.set("empresa", "30509300700");
		CONTEXTO_OB.parametros.set("cuenta", "300000000367653");
		CONTEXTO_OB.parametros.set("monto", "99999999999");
		CONTEXTO_OB.parametros.set("firmante", "20203839880");

		Object respuesta = OBTransferencias.validarMonto(CONTEXTO_OB);

		assertTrue(respuesta.toString().contains("OPERACION_INVALIDA"));
	}

	@Test
	void listarOperacionInvalida() {
		Object respuesta = OBTransferencias.listar(CONTEXTO_OB);
		assertTrue(respuesta.toString().contains("OPERACION_INVALIDA"));
	}

	@Test
	void listarOK() {
		SesionOB sesion = new SesionOB();
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

		sesion.empresaOB = empresaOB;
		sesion.usuarioOB = usuarioOB;
		sesion.rol = "2";
		sesion.idCobis = "4639221";

		ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
		contextoOB.sesion().usuarioOB = usuarioOB;
		contextoOB.sesion().empresaOB = empresaOB;

		Object respuesta = OBTransferencias.listar(contextoOB);
		assertNotNull(respuesta);
	}

	@Test
	void ultimasOK() {
		SesionOB sesion = new SesionOB();
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

		sesion.empresaOB = empresaOB;
		sesion.usuarioOB = usuarioOB;
		sesion.rol = "2";
		sesion.idCobis = "4639221";

		ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
		contextoOB.sesion().usuarioOB = usuarioOB;
		contextoOB.sesion().empresaOB = empresaOB;

		Objeto respuesta = (Objeto) OBTransferencias.ultimas(contextoOB);
		assertNotNull(respuesta);

		OBTransferencias transferencias = mock(OBTransferencias.class);
		verify(transferencias, times(1));
	}

	@Test
	void detalleOperacionInvalida() {
		CONTEXTO_OB.parametros.set("idTransferencia", 1);
		Object respuesta = OBTransferencias.detalle(CONTEXTO_OB);
		assertTrue(respuesta.toString().contains("OPERACION_INVALIDA"));
	}

	@Test
	void detalle() {
		SesionOB sesion = new SesionOB();
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

		sesion.empresaOB = empresaOB;
		sesion.usuarioOB = usuarioOB;
		sesion.rol = "2";
		sesion.idCobis = "4639221";

		ContextoOB contextoOB = new ContextoOB("OB", Config.ambiente(), "1");
		contextoOB.sesion().usuarioOB = usuarioOB;
		contextoOB.sesion().empresaOB = empresaOB;
		contextoOB.parametros.set("idTransferencia", 2);

		Objeto respuesta = (Objeto) OBTransferencias.detalle(contextoOB);
		assertNotNull(respuesta);
	}

}