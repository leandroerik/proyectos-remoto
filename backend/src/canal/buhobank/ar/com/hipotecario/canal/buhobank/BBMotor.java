package ar.com.hipotecario.canal.buhobank;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.servicio.api.ventas.ApiVentas;
import ar.com.hipotecario.backend.servicio.api.ventas.MotorScoringSimulacion;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados.CuentasSolicitadas;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados.CuentasSolicitadas.CuentaSolicitada;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados.RolesSolicitantes;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados.RolesSolicitantes.RolSolicitante;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados.TarjetasSolicitadas;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosSolicitados.TarjetasSolicitadas.TarjetaSolicitada;
import ar.com.hipotecario.canal.buhobank.scoring.Solicitantes;
import ar.com.hipotecario.canal.buhobank.scoring.Solicitantes.BuhoBank;
import ar.com.hipotecario.canal.buhobank.scoring.Solicitantes.DatosPersonales;
import ar.com.hipotecario.canal.buhobank.scoring.Solicitantes.Identidad;
import ar.com.hipotecario.canal.buhobank.scoring.Solicitantes.Solicitante;
import ar.com.hipotecario.canal.buhobank.scoring.Solicitantes.Telemail;
import ar.com.hipotecario.canal.buhobank.scoring.SolicitudMotor;
import ar.com.hipotecario.canal.buhobank.scoring.SolicitudMotor.Body;
import ar.com.hipotecario.canal.buhobank.scoring.SolicitudMotor.Header;
import ar.com.hipotecario.canal.buhobank.scoring.SolicitudMotor.Solicitud;

public class BBMotor extends Modulo {

	public static Solicitante crearSolicitante(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String idCobis = sesion.idCobis;

		Identidad identidad = new Identidad();
		identidad.idCliente = sesion.cobisPositivo() ? Long.parseLong(idCobis) : 0L;
		identidad.tipoDocumento = sesion.tipoDocumento();
		identidad.versionDNI = !empty(sesion.ejemplar) ? sesion.ejemplar : "A";
		identidad.nroDocumento = sesion.numeroDocumento;
		identidad.tipoIdTributaria = !empty(sesion.idTipoIDTributario) ? sesion.idTipoIDTributario : "08";
		identidad.nroIdTributaria = sesion.cuil;
		identidad.sexo = sesion.genero;
		identidad.nombres = sesion.nombre;
		identidad.apellido = sesion.apellido;

		DatosPersonales datosPersonales = new DatosPersonales();
		datosPersonales.fechaNacimiento = sesion.fechaNacimiento.string("yyyy-MM-dd", null);
		datosPersonales.tipoPersona = GeneralBB.TIPO_PERSONA_PARTICULAR;
		datosPersonales.montoAlquiler = GeneralBB.MONTO_ALQUILER;
		datosPersonales.unidoHecho = GeneralBB.UNIDO_HECHO_INICIAL;

		Telemail telemailParticular = new Telemail();
		telemailParticular.email = sesion.mail;
		telemailParticular.tieneMail = true;
		telemailParticular.telCelularPrefijo = sesion.codAreaCelular();
		telemailParticular.telCelularCaract = sesion.caracteristicaCelular();
		telemailParticular.telCelularNro = sesion.numeroCelular();

		BuhoBank buhoBank = new BuhoBank();
		buhoBank.ingresoNeto = GeneralBB.INGRESO_NETO;

		Solicitante solicitante = new Solicitante();
		solicitante.nroSolicitante = GeneralBB.NRO_SOLICITANTE;
		solicitante.identidad = identidad;
		solicitante.datosPersonales = datosPersonales;
		solicitante.telemailParticular = telemailParticular;
		solicitante.buhoBank = buhoBank;
		solicitante.solicitaValidarIdentidad = GeneralBB.SOLICITA_VALIDAR_IDENTIDAD;
		solicitante.esPlanSueldo = GeneralBB.ES_PLAN_SUELDO;
		solicitante.solicitaEvaluarMercadoAbierto = GeneralBB.SOLICITA_EVALUAR_MERCADO_ABIERTO;
		solicitante.esSujetoObligado = GeneralBB.ES_SUJETO_OBLIGADO;

		return solicitante;
	}

	public static Solicitantes obtenerSolicitantes(Solicitante solicitante) {
		List<Solicitante> solicitantesList = new ArrayList<>();
		solicitantesList.add(solicitante);

		Solicitantes solicitantes = new Solicitantes();
		solicitantes.solicitante = solicitantesList;

		return solicitantes;
	}

	public static RolesSolicitantes obtenerRolesSolicitantes() {
		RolSolicitante rolSolicitante = new RolSolicitante();
		rolSolicitante.nroSolicitante = GeneralBB.NRO_SOLICITANTE;
		rolSolicitante.rolSolicitante = GeneralBB.ROL_SOLICITANTE_TITULAR;

		List<RolSolicitante> rolesSolicitantesList = new ArrayList<>();
		rolesSolicitantesList.add(rolSolicitante);

		RolesSolicitantes rolesSolicitantes = new RolesSolicitantes();
		rolesSolicitantes.rolSolicitante = rolesSolicitantesList;

		return rolesSolicitantes;
	}

	public static TarjetaSolicitada obtenerTarjetaSolicitada(boolean esVirtual) {
		TarjetaSolicitada tarjetaSolicitada = new TarjetaSolicitada();
		tarjetaSolicitada.rolesSolicitantes = obtenerRolesSolicitantes();
		tarjetaSolicitada.tipoTarjeta = GeneralBB.TIPO_TARJETA_SOLICITADA;
		tarjetaSolicitada.nro = GeneralBB.NRO_TARJETA_SOLICITADA;
		tarjetaSolicitada.flagPaquete = GeneralBB.ES_PAQUETE;
		tarjetaSolicitada.solicitaPrimeraCompra = GeneralBB.SOLICITA_PRIMERA_COMPRA;
		tarjetaSolicitada.montoAlta = GeneralBB.MONTO_ALTA_TARJETA_SOLICITADA;
		tarjetaSolicitada.montoSolicitado = GeneralBB.MONTO_SOLICITADO_TARJETA_SOLICITADA;
		if(esVirtual){
			tarjetaSolicitada.esVirtual = true;
		}
		return tarjetaSolicitada;
	}

	public static CuentaSolicitada obtenerCuentaSolicitada() {
		CuentaSolicitada cuentaSolicitada = new CuentaSolicitada();
		cuentaSolicitada.rolesSolicitantes = obtenerRolesSolicitantes();
		cuentaSolicitada.tipoCuenta = GeneralBB.TIPO_CUENTA_SOLICITADA;
		cuentaSolicitada.nro = GeneralBB.NRO_CUENTA_SOLICITADA;
		cuentaSolicitada.flagPaquete = GeneralBB.ES_PAQUETE;
		cuentaSolicitada.montoAlta = GeneralBB.MONTO_ALTA_CUENTA_SOLICITADA;

		return cuentaSolicitada;
	}

	public static ProductosSolicitados obtenerProductosSolicitados(TarjetaSolicitada tarjeta, CuentaSolicitada cuenta) {
		List<TarjetaSolicitada> tarjetasSolicitadasList = new ArrayList<>();
		tarjetasSolicitadasList.add(tarjeta);

		TarjetasSolicitadas tarjetasSolicitadas = new TarjetasSolicitadas();
		tarjetasSolicitadas.tarjetaSolicitada = tarjetasSolicitadasList;

		List<CuentaSolicitada> cuentasSolicitadasList = new ArrayList<>();
		cuentasSolicitadasList.add(cuenta);

		CuentasSolicitadas cuentasSolicitadas = new CuentasSolicitadas();
		cuentasSolicitadas.cuentaSolicitada = cuentasSolicitadasList;

		ProductosSolicitados productosSolicitados = new ProductosSolicitados();
		productosSolicitados.cuentasSolicitadas = cuentasSolicitadas;
		productosSolicitados.tarjetasSolicitadas = tarjetasSolicitadas;

		return productosSolicitados;
	}

	public static MotorScoringSimulacion invocarMotor(ContextoBB contexto, BigInteger instancia, String idSolicitud) {
		SolicitudMotor solicitudMotor = new SolicitudMotor();
		Header header = new Header();
		header.handle = idSolicitud;
		solicitudMotor.header = header;

		Solicitud solicitud = new Solicitud();
		solicitud.idSolicitud = !Util.empty(idSolicitud) ? Long.parseLong(idSolicitud) : 0L;
		solicitud.nroInstancia = !Util.empty(instancia) ? instancia : GeneralBB.PRIMERA_LLAMADA;
		solicitud.canalVenta = contexto.canalVenta1();
		solicitud.subCanalVenta = contexto.subCanalVenta();
		solicitud.puntoVenta = GeneralBB.PUNTO_VENTA;
		solicitud.oficialVenta = GeneralBB.OFICIAL_VENTA;
		solicitud.oficialTramite = GeneralBB.OFICIAL_TRAMITE;
		solicitud.canalTramite = GeneralBB.CANAL_TRAMITE;
		solicitud.subCanalTramite = GeneralBB.SUB_CANAL_TRAMITE;
		solicitud.tipoInvocacion = GeneralBB.MOTOR_TIPO_INVOCACION;
		solicitud.flagSolicitaAprobacionEstandar = GeneralBB.SOLICITA_APROBACION_ESTANDAR;
		solicitud.flagSolicitaComprobarIngresos = GeneralBB.SOLICITA_COMPROBAR_INGRESOS;
		solicitud.flagSolicitaAprobacionCentralizada = GeneralBB.SOLICITA_APROBACION_CENTRALIZADA;
		solicitud.flagSolicitaExcepcionChequeoFinal = GeneralBB.SOLICITA_EXCEPCION_CHEQUEO_FINAL;
		solicitud.flagSimulacion = GeneralBB.ES_SIMULACION;
		solicitud.flagRutaConPactado = GeneralBB.RUTA_CON_PACTADO;

		Solicitante solicitante = crearSolicitante(contexto);
		solicitud.solicitantes = obtenerSolicitantes(solicitante);

		TarjetaSolicitada tarjetaSolicitada = obtenerTarjetaSolicitada(contexto.sesion().getParamTcOnline(contexto));
		CuentaSolicitada cuentaSolicitada = obtenerCuentaSolicitada();
		solicitud.productosSolicitados = obtenerProductosSolicitados(tarjetaSolicitada, cuentaSolicitada);

		Body body = new Body();
		body.solicitud = solicitud;
		solicitudMotor.body = body;

		return ApiVentas.getOfertaMotorScoring(contexto, idSolicitud, solicitudMotor, contexto.sesion().getParamTcOnline(contexto)).tryGet();
	}
}
