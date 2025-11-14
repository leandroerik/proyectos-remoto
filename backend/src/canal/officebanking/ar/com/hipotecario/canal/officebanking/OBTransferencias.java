package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.CVU.ApiCVU;
import ar.com.hipotecario.backend.servicio.api.CVU.getCVU;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos.Banco;
import ar.com.hipotecario.backend.servicio.api.catalogo.DiaBancario;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.EstadoCuenta;
import ar.com.hipotecario.backend.servicio.api.debin.AutorizarCredin;
import ar.com.hipotecario.backend.servicio.api.debin.ApiDebin;
import ar.com.hipotecario.backend.servicio.api.debin.ConsultarDebin;
import ar.com.hipotecario.backend.servicio.api.firmas.TipoFirma;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.BancosInterbanking;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.PeticionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoTRNOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMigracionTransmit;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.PaginaTransferenciaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.BE.BETransferencia;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.PermisoOperadorOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.*;

public class OBTransferencias extends ModuloOB {

	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	private static ServicioParametroOB servicioParametro = new ServicioParametroOB(contexto);
	private static final List<String> habilitaBandejaFirmas = servicioParametro.split("transferencia.bandeja.firma").get();
	private static final BigDecimal MONTO_MAXIMO_NUEVO_BENEFICIARIO_PESOS = BigDecimal.valueOf(5000000);
	private static final BigDecimal MONTO_MAXIMO_NUEVO_BENEFICIARIO_DOLARES = BigDecimal.valueOf(5000);
	private static final BigDecimal MONTO_MAXIMO_SEGURAS_PESOS = BigDecimal.valueOf(9000000000000000000l);
	private static final BigDecimal MONTO_MAXIMO_SEGURAS_DOLARES = BigDecimal.valueOf(9000000000000000000l);
	public static final Set<String> ESTADOS_RECHAZADOS = new HashSet<>(Arrays.asList(
			"RECHAZO DE CLIENTE",
			"SIN GARANTIA",
			"ERROR DATOS",
			"ERROR DEBITO",
			"ERROR",
			"VENCIDO",
			"VENCIDA",
			"ERROR CREDITO",
			"ERROR AVISO RED",
			"NOTIFICACION",
			"ERROR GARANTIA",
			"GARANTIAS REVERSADAS",
			"ERROR ACREDITACION"
	));

	public static Object validarCBUAlias(ContextoOB contexto) {
		LogOB.evento(contexto, "validarCBUAlias", "INICIO");
		String cbuAlias = contexto.parametros.string("cbu");

		if (!cbuValido(cbuAlias) && !aliasValido(cbuAlias)) {
			LogOB.evento(contexto, "validarCBUAlias", "CBU_ALIAS_FORMATO_INVALIDO");
			return respuesta("CBU_ALIAS_FORMATO_INVALIDO");
		}

		ServicioBeneficiarioOB servicioBeneficiario = new ServicioBeneficiarioOB(contexto);
		InfoCuentaDTO info = servicioBeneficiario.infoCBUAlias(contexto, cbuAlias);
		if (empty(info)) {
			LogOB.evento(contexto, "validarCBUAlias", "CBU_ALIAS_INVALIDO ERROR info");
			return respuesta("CBU_ALIAS_INVALIDO");
		}

		if (!info.cuenta.ctaActiva) {
			LogOB.evento(contexto, "validarCBUAlias", "CUENTA_INACTIVA");
			return respuesta("CUENTA_INACTIVA");
		}
		boolean esCVU = cbuValido(cbuAlias) && cbuAlias.startsWith("000");
		if (esCVU) {
			getCVU cvu = ApiCVU.getCVU(contexto,cbuAlias).get();
			if (!cvu.cvu.estado.descripcion.equals("CVU ACTIVO")){
				return respuesta("CUENTA_INACTIVA");
			}
		}

		Objeto datos = new Objeto();
		datos.set("nombre", info.cuenta.nombreTitular);
		datos.set("cbu", info.cuenta.cbu);
		datos.set("alias", info.cuenta.nuevoAlias);
		datos.set("cuit", info.cuenta.cuit);

		Banco datosBanco = ApiCatalogo.bancos(contexto, info.cuenta.nroBco).get();
		datos.set("bancoDestino", datosBanco.Descripcion);

		if(info.monedas != null) {
		Objeto lstMonedas = new Objeto();
		if (esCVU) info.monedas = info.monedas.stream().filter(monedaOB -> monedaOB.codigoCobis.equals("ARS")).toList();
			for (MonedaOB moneda : info.monedas) {
				Objeto mon = new Objeto();
				mon.set("codigo", moneda.codigoCobis);
				mon.set("simbolo", moneda.simbolo);
				mon.set("descripcion", moneda.descripcion);
				lstMonedas.add(mon);
			}
			datos.set("monedas", lstMonedas);
			datos.set("esCVU",esCVU);
		}

		// MAXI MIRAR TIPO CUNETA 2
		TipoCuentaOB tipoCuenta = info.cuenta.tipoCuenta.equals("CTE") || info.cuenta.tipoCuenta.equals("CC") ? TipoCuentaOB.CC : TipoCuentaOB.CA;

		Objeto tcv = new Objeto();
		tcv.set("id", tipoCuenta.ordinal());
		tcv.set("descripcionCorta", tipoCuenta.name());
		tcv.set("descripcionLarga", tipoCuenta.getDescripcionLarga());
		datos.set("tipoCuenta", tcv);

		return respuesta("datos", datos);
	}

	public static Object conceptos(ContextoOB contexto) {

		ServicioConceptoOB servicio = new ServicioConceptoOB(contexto);
		List<ConceptoOB> lstConcepto = servicio.findAll().tryGet();

		Objeto respuesta = respuesta("0");
		for (ConceptoOB c : lstConcepto) {
			Objeto concepto = respuesta.add("conceptos");
			concepto.set("id", c.id);
			concepto.set("concepto", c.descripcion);
		}

		return respuesta;
	}

	public static Object horarioCamara(ContextoOB contexto) {
		ServicioCamaraHorarioOB servicio = new ServicioCamaraHorarioOB(contexto);
		HorarioCamaraOB interbanking = servicio.find(1).get();

		boolean camaraAbierta = validarHorario(interbanking);
		DiaBancario dia = ApiCatalogo.diaBancario(contexto, Fecha.hoy()).get();
		boolean diferida = dia.esDiaHabil() ? esDiferida(interbanking) : true;
		LogOB.evento(contexto, "horarioCamara", "camaraAbierta: "+String.valueOf(camaraAbierta));
		LogOB.evento(contexto, "horarioCamara", "diferida: " + String.valueOf(diferida));
		Objeto datos = new Objeto();
		datos.set("camara", interbanking.tipoCamara.descripcion);
		datos.set("camaraAbierta", true);
		datos.set("diferida", false);
		datos.set("habilPosterior", diferida ? dia.diaHabilPosterior : null);
		datos.set("hora_inicio", interbanking.horaInicio.toString());
		datos.set("hora_limite", interbanking.horaLimite.toString());
		LogOB.evento(contexto, "horarioCamara", "consulta ok");
		return respuesta("datos", datos);
	}

	public static Object monedas(ContextoOB contexto) {
		ServicioMonedaOB servicio = new ServicioMonedaOB(contexto);
		List<MonedaOB> lstMonedas = servicio.findAll().get();

		Objeto respuesta = respuesta("0");
		for (MonedaOB moneda : lstMonedas) {
			Objeto mon = respuesta.add("monedas");
			mon.set("codigo", moneda.codigoCobis);
			mon.set("simbolo", moneda.simbolo);
			mon.set("descripcion", moneda.descripcion);
		}

		return respuesta;
	}
	public static Boolean esCVU(String parametro) {
		return parametro.matches("[0-9]*") && parametro.length() == 22 && parametro.startsWith("000");
	}

	public static Object cargarTransferencia(ContextoOB contexto) {
		Object respuestaFinal = null;
		LogOB.evento(contexto, "cargarTransferencia","CARGA TRANSFEREICNIA...");
		LogOB.evento(contexto, "cargarTransferencia","habilitadas: "+ habilitaBandejaFirmas.get(0).equals("0"));
		if (habilitaBandejaFirmas.get(0).equals("0")) {
			respuestaFinal = enviar(contexto);
		} else if (habilitaBandejaFirmas.get(0).equals("1")) {

			String cuentaDebito = contexto.parametros.string("cuentaDebito");
			String cbuCredito = contexto.parametros.string("cbu");
			String cuitCredito = contexto.parametros.string("cuit", null);
			Integer moneda = contexto.parametros.integer("moneda");
			BigDecimal monto = contexto.parametros.bigDecimal("monto");
			String referencia = contexto.parametros.string("referencia", null);
			String email = contexto.parametros.string("email", null);
			Boolean cuentaSueldo = contexto.parametros.bool("cuentaSueldo", false);
			Boolean altaBeneficiario = contexto.parametros.bool("altaBeneficiario", false);
			Integer concepto = contexto.parametros.integer("concepto");
			SesionOB sesion = contexto.sesion();

			if (monto.signum() != 1) {
				LogOB.evento(contexto, "cargarTransferencia","MONTO_INVALIDO");
				return respuesta("MONTO_INVALIDO");
			}

			Fecha fechaProgramacion = contexto.parametros.fecha("fechaProgramacion", "yyyy-MM-dd", null);
			if (!empty(fechaProgramacion) && !fechaProgramacion.esPosterior(Fecha.hoy())) {
				LogOB.evento(contexto, "cargarTransferencia","FECHA_PROGRAMACION_INVALIDA");
				return ("FECHA_PROGRAMACION_INVALIDA");
			}



			if (!cbuValido(cbuCredito)) {
				LogOB.evento(contexto, "cargarTransferencia","CBU_INVALIDO");
				return respuesta("CBU_INVALIDO");
			}

			boolean esCvu = esCVU(cbuCredito);
			if (fechaProgramacion.esPosterior(Fecha.hoy())&&esCvu){
				return respuesta("ERROR","ERROR","CVU_PROGRAMADA");
			}
			if (cuentaSueldo && esCvu){
				return respuesta("ERROR","ERROR","CVU_SUELDO");
			}
			EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
			if (!puedeTransferir(contexto, empresaUsuario)) {
				LogOB.evento(contexto, "cargarTransferencia","OPERACION_INVALIDA !puedeTransferir(contexto, empresaUsuario)");
				return respuesta("OPERACION_INVALIDA");
			}

			ServicioPermisoOB servicioPermisoOB = new ServicioPermisoOB(contexto);
			if (!sesion.esOperadorInicial()) {
				ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
				PermisoOperadorOB permiso = servicioPermisoOperadorOB.buscarPermiso(empresaUsuario, servicioPermisoOB.find(4).get()).tryGet();
				if (empty(permiso)) {
					LogOB.evento(contexto, "cargarTransferencia","OPERACION_INVALIDA !sesion.esOperadorInicial()");
					return respuesta("OPERACION_INVALIDA");
				}
			}

			LocalDate fechaAplicacion;
			if (empty(fechaProgramacion)) {
				fechaAplicacion = LocalDate.now();

			} else {
				DiaBancario dia = ApiCatalogo.diaBancario(contexto, fechaProgramacion).get();
				if (dia.esDiaHabil()) {
					fechaAplicacion = fechaProgramacion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				} else {
					fechaAplicacion = dia.diaHabilPosterior.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				}
			}

			Objeto cuenta = OBCuentas.cuenta(contexto, cuentaDebito);
			if (empty(cuenta)) {
				LogOB.evento(contexto, "cargarTransferencia","CUENTA_DEBITO_INVALIDA");
				return respuesta("CUENTA_DEBITO_INVALIDA");
			}
			String cbuDebito = cuenta.get("cbu").toString();
			String tipoCuentaDebito = cuenta.get("tipoProducto").toString();

			ServicioBeneficiarioOB servicioInfo = new ServicioBeneficiarioOB(contexto);

			InfoCuentaDTO info = servicioInfo.infoCBUAlias(contexto, cbuCredito);
			if (empty(info.cuenta) || cbuCredito.equals(cbuDebito)) {
				LogOB.evento(contexto, "cargarTransferencia","CBU_INVALIDO");
				return respuesta("CBU_INVALIDO");
			}

			// MAXI Agregar
			boolean validadoXRUC = false;
			int camara = 0;
			if (!esCvu){
				EstadoCuenta respuesta = ApiCuentas.estadoCuenta(contexto, cbuCredito, info.cuenta.cuit).tryGet();
				if (!empty(respuesta)) {
					if (cbuCredito.startsWith("044")) {
						camara = 3;
						validadoXRUC = true;
					} else if (BancosInterbanking.get(contexto, "OB", cbuCredito.substring(0, 3)) != null) {
						camara = 1;
						validadoXRUC = true;
					} else {
						camara = 2;// coelsa
					}
				}else {
					if (!empty(info.cuenta)) {
						camara = 2;// coelsa
					}
				}

				if (camara == 0) {
					LogOB.evento(contexto, "cargarTransferencia","ERROR_XRUCC");
					return respuesta("ERROR_XRUCC");
				}

				validadoXRUC = !empty(respuesta) && respuesta.CodigoRespuesta.equals("CBU_OK");
				LogOB.evento(contexto, "cargarTransferencia","camara: " + camara);
			}else {
				 camara = 99; //API
			}
			info.cuenta.transaccion = String.valueOf(camara);



			try {
				ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
				ServicioEstadoTRNOB servicioEstadoTRNOB = new ServicioEstadoTRNOB(contexto);

				EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
				EstadoTRNOB estadoEnBandeja = servicioEstadoTRNOB.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get();

				ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
				TipoProductoFirmaOB tipoProductoFirmaOB = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()).get();

				ServicioTransferenciaOB servicio = new ServicioTransferenciaOB(contexto);
				TransferenciaOB transferenciaOB = servicio.enviarTransferencia(contexto, info, cuentaDebito, cbuDebito, tipoCuentaDebito, moneda, cbuCredito, fechaAplicacion, monto, concepto, referencia, email, cuentaSueldo, validadoXRUC, altaBeneficiario, estadoEnBandeja, tipoProductoFirmaOB, estadoInicialBandeja,esCvu).get();
				ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
				BandejaOB bandeja = servicioBandeja.find(transferenciaOB.id).get();

				ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
				AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

				ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
				ServicioHistorialTrnOB servicioHistorialTrn = new ServicioHistorialTrnOB(contexto);

				servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
				servicioHistorialTrn.cambiaEstado(transferenciaOB, accionCrear, empresaUsuario, estadoEnBandeja, estadoEnBandeja);

				contexto.parametros.set("idTransferencia", transferenciaOB.id);
				Objeto detalle = (Objeto) detalle(contexto);
				LogOB.evento(contexto, "cargarTransferencia","cargo la transferencia en bd id: " + transferenciaOB.id);
				respuestaFinal = respuesta("datos", detalle.get("datos"));

			} catch (RuntimeException rte) {
				LogOB.evento(contexto, "cargarTransferencia",rte.getMessage());
				return respuesta(rte.getMessage());
			}
		}
		LogOB.evento(contexto, "cargarTransferencia","FIN CARGA");
		return respuestaFinal;
	}

	public static Object enviar(ContextoOB contexto) {
		String cuentaDebito = contexto.parametros.string("cuentaDebito");
		String cbuCredito = contexto.parametros.string("cbu");
		String cuitCredito = contexto.parametros.string("cuit", null);
		String referencia = contexto.parametros.string("referencia", null);
		String email = contexto.parametros.string("email", null);
		Boolean cuentaSueldo = contexto.parametros.bool("cuentaSueldo", false);
		Boolean altaBeneficiario = contexto.parametros.bool("altaBeneficiario", false);

		Integer moneda = contexto.parametros.integer("moneda");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		Integer concepto = contexto.parametros.integer("concepto");

		SesionOB sesion = contexto.sesion();
		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (sesion.token.fechaValidacion.isNull()) {
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();

		Fecha fechaProgramacion = contexto.parametros.fecha("fechaProgramacion", "yyyy-MM-dd", null);
		if (!empty(fechaProgramacion) && !fechaProgramacion.esPosterior(Fecha.hoy())) {
			return ("FECHA_PROGRAMACION_INVALIDA");
		}

		if (monto.signum() != 1) {
			return respuesta("MONTO_INVALIDO");
		}

		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
		if (!puedeTransferir(contexto, empresaUsuario)) {
			return respuesta("OPERACION_INVALIDA");
		}

		if (monto.signum() != 1) {
			return respuesta("MONTO_INVALIDO");
		}

		if (!cbuValido(cbuCredito)) {
			return respuesta("CBU_INVALIDO");
		}

		boolean esCvu = esCVU(cbuCredito);

		ServicioPermisoOB servicioPermisoOB = new ServicioPermisoOB(contexto);
		if (!sesion.esOperadorInicial()) {
			ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
			PermisoOperadorOB permiso = servicioPermisoOperadorOB.buscarPermiso(empresaUsuario, servicioPermisoOB.find(4).get()).tryGet();
			if (empty(permiso)) {
				return respuesta("OPERACION_INVALIDA");
			}
		}

		Objeto cuenta = OBCuentas.cuenta(contexto, cuentaDebito);
		if (empty(cuenta)) {
			return respuesta("CUENTA_DEBITO_INVALIDA");
		}
		String cbuDebito = cuenta.get("cbu").toString();
		String tipoCuentaDebito = cuenta.get("tipoProducto").toString();

		ServicioBeneficiarioOB servicioInfo = new ServicioBeneficiarioOB(contexto);

		InfoCuentaDTO info = servicioInfo.infoCBUAlias(contexto, cbuCredito);
		if (empty(info.cuenta) || cbuCredito.equals(cbuDebito)) {
			return respuesta("CBU_INVALIDO");
		}

		Optional<MonedaOB> mon = info.monedas.stream().filter(m -> m.id.intValue() == moneda.intValue()).findFirst();
		if (!mon.isPresent()) {
			return respuesta("MONEDA_INVALIDA");
		}

		// MAXI Agregar
		boolean validadoXRUC = false;
		int camara = 0;
		if (!esCvu){
			EstadoCuenta respuesta = ApiCuentas.estadoCuenta(contexto, cbuCredito, info.cuenta.cuit).tryGet();
			if (!empty(respuesta)) {
				if (cbuCredito.startsWith("044")) {
					camara = 3;
					validadoXRUC = true;
				} else if (BancosInterbanking.get(contexto, "OB", cbuCredito.substring(0, 3)) != null) {
					camara = 1;
					validadoXRUC = true;
				} else {
					camara = 2;// coelsa
				}
			}
			if (camara == 0) {
				return respuesta("ERROR_XRUCC");
			}
			info.cuenta.transaccion = String.valueOf(camara);
			validadoXRUC = respuesta.CodigoRespuesta.equals("CBU_OK") ? true : false;
		}




		boolean vaPorAPI = esCvu;
		LocalDate fechaAplicacion;
		if (empty(fechaProgramacion)) {
			if (!esCvu){
				Objeto horario = (Objeto) horarioCamara(contexto);
				vaPorAPI = (boolean) horario.get("datos.diferida");
			}

			fechaAplicacion = LocalDate.now();
		} else {
			DiaBancario dia = ApiCatalogo.diaBancario(contexto, fechaProgramacion).get();
			if (dia.esDiaHabil()) {
				fechaAplicacion = fechaProgramacion.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			} else {
				fechaAplicacion = dia.diaHabilPosterior.FechaDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
		}

		try {
			FirmaOB firma = FirmaOB.tipoFirma(contexto, sesion.empresaOB.cuit.toString(), cuentaDebito, monto.toString(), sesion.usuarioOB.cuil.toString(), null, String.valueOf(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()));
			if (firma.tipoFirma == TipoFirma.FIRMA_INDISTINTA) {
				ServicioTransferenciaOB servicio = new ServicioTransferenciaOB(contexto);
				ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
				ServicioEstadoTRNOB servicioEstadoTRNOB = new ServicioEstadoTRNOB(contexto);
				ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
				TipoProductoFirmaOB tipoProductoTransferencias = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()).get();

				EstadoBandejaOB estadoBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get();
				EstadoTRNOB estadoTransferencia;
				if (!fechaAplicacion.isEqual(LocalDate.now()))
					estadoTransferencia = servicioEstadoTRNOB.find(EnumEstadoTRNOB.PROGRAMADA.getCodigo()).get();
				else
					estadoTransferencia = servicioEstadoTRNOB.find(EnumEstadoTRNOB.A_PROCESAR.getCodigo()).get();
				TransferenciaOB transferenciaOB = servicio.enviarTransferencia(contexto, info, cuentaDebito, cbuDebito, tipoCuentaDebito, moneda, cbuCredito, fechaAplicacion, monto, concepto, referencia, email, cuentaSueldo, validadoXRUC, altaBeneficiario, estadoTransferencia, tipoProductoTransferencias, estadoBandeja,esCvu).get();
				contexto.parametros.set("idTransferencia", transferenciaOB.id);
				Objeto detalle = (Objeto) OBTransferencias.detalle(contexto);

				ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
				AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();

				ServicioHistorialTrnOB servicioHistorialTrn = new ServicioHistorialTrnOB(contexto);

				servicioHistorialTrn.cambiaEstado(transferenciaOB, accionCrear, empresaUsuario, servicioEstadoTRNOB.find(EnumEstadoTRNOB.EN_BANDEJA.getCodigo()).get(), estadoTransferencia);

				return respuesta("datos", detalle.get("datos"));
			}
		} catch (RuntimeException rte) {
			return respuesta(rte.getMessage());
		}

		return respuesta("SIN_FIRMA_INDISTINTA");

	}

	public static Object detalle(ContextoOB contexto) {
		Integer idTransferencia = contexto.parametros.integer("idTransferencia");
		SesionOB sesion = contexto.sesion();

		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
//		if (!puedeTransferir(contexto, empresaUsuario)) {
//			return respuesta("OPERACION_INVALIDA");
//		}

		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		ServicioTransferenciaOB servicio = new ServicioTransferenciaOB(contexto);
		TransferenciaOB transferencia = servicio.find(idTransferencia).get();
		if (empty(transferencia) || !transferencia.emp_codigo.idCobis.equals(sesion.empresaOB.idCobis)) {
			return respuesta("DATOS_INVALIDOS");
		}

		Objeto trn = new Objeto();
		trn.set("id", transferencia.id);
		trn.set("tipo", transferencia.tipo.descripcion);
		trn.set("moneda", transferencia.moneda.simbolo);
		trn.set("monto", transferencia.monto);
		trn.set("fechaCreacion", transferencia.fechaCreacion.toString());
		trn.set("fechaAplicacion", transferencia.fechaAplicacion.toString());
		if (transferencia.fechaEjecucion != null) {
			trn.set("fechaEjecucion", transferencia.fechaEjecucion.toString());
		}
		trn.set("concepto", transferencia.concepto.descripcion);
		trn.set("creadaPor", transferencia.usuario.nombre + " " + transferencia.usuario.apellido);
		trn.set("comentario", transferencia.comentario != null ? transferencia.comentario : "");

		Objeto estado = new Objeto();

		if (transferencia.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
			if (transferencia instanceof TransferenciaCredinOB){
				if (transferencia.estado.id==EnumEstadoTRNOB.EN_PROCESO.getCodigo()||(transferencia.estado.id==EnumEstadoTRNOB.EXITO.getCodigo() && transferencia.fechaEjecucion.plusDays(10).isAfter(LocalDateTime.now()))){
					LogOB.evento(contexto,"actualizacion estado TRN CREDIN id: "+transferencia.id,"INICIO");
					TransferenciaCredinOB transferenciaCredinOB = (TransferenciaCredinOB) transferencia;
					LogOB.evento(contexto,"actualizacion estado TRN CREDIN idDebin: "+transferencia.id,transferenciaCredinOB.idDebin);
					ConsultarDebin.ConsultaDebinResponse detalle = ApiDebin.consultarDebin(contexto, transferenciaCredinOB.idDebin).tryGet();
					LogOB.evento(contexto,"actualizacion estado TRN EstadoDebin: "+detalle.estado.codigo);
					if (detalle.estado.codigo.equalsIgnoreCase("ACREDITADO")){
						transferencia.estado = new ServicioEstadoTRNOB(contexto).find(EnumEstadoTRNOB.EXITO.getCodigo()).get();
						servicio.update(transferenciaCredinOB);
					}else if  (ESTADOS_RECHAZADOS.contains(detalle.estado.codigo.toUpperCase())) {
						transferencia.estado = new ServicioEstadoTRNOB(contexto).find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
						servicio.update(transferenciaCredinOB);
					}
				}
			}
			EstadoTRNOB trnEstado = transferencia.estado;
			estado.set("id", trnEstado.id);
			estado.set("descripcionCorta", trnEstado.descripcion);
			trn.set("estado", estado);
		} else {
			estado.set("id", transferencia.estadoBandeja.id);
			estado.set("descripcionCorta", transferencia.estadoBandeja.descripcion);
			trn.set("estado", estado);
		}

		Objeto debito = new Objeto();
		debito.set("cbu", transferencia.debito.cbu);
		TipoCuentaOB tipoCuentaDebito = transferencia.debito.tipoCuenta;
		Objeto tcd = new Objeto();
		tcd.set("id", tipoCuentaDebito.ordinal());
		tcd.set("descripcionCorta", tipoCuentaDebito.name());
		tcd.set("descripcionLarga", tipoCuentaDebito.getDescripcionLarga());
		debito.set("tipoCuenta", tcd);
		debito.set("numeroCuenta", transferencia.debito.nroCuenta);
		debito.set("razonSocial", transferencia.debito.descripcion);
		debito.set("cuit", transferencia.debito.cuit);
		trn.set("debito", debito);

		Objeto credito = new Objeto();
		credito.set("cbu", transferencia.credito.cbu);
		credito.set("esCVU",OBTransferencias.esCVU(transferencia.credito.cbu));
		credito.set("banco", transferencia.credito.banco.denominacion);
		TipoCuentaOB tipoCuentaCredito = transferencia.credito.tipoCuenta;
		Objeto tcc = new Objeto();
		tcc.set("id", tipoCuentaCredito.ordinal());
		tcc.set("descripcionCorta", tipoCuentaCredito.name());
		tcc.set("descripcionLarga", tipoCuentaCredito.getDescripcionLarga());
		credito.set("tipoCuenta", tcc);
		credito.set("numeroCuenta", transferencia.credito.nroCuenta);
		credito.set("cuit", transferencia.credito.cuit);
		credito.set("titular", transferencia.credito.titular);
		credito.set("email", transferencia.credito.email);
		credito.set("comentario", transferencia.credito.comentario);

		trn.set("credito", credito);
		
		BandejaOB bandeja = servicioBandeja.find(transferencia.id).get();
		trn.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));		

		return respuesta("datos", trn);
	}

	public static Object ultimas(ContextoOB contexto) {
		contexto.parametros.set("numeroPagina", 1);
		contexto.parametros.set("registrosPorPagina", 4);
		return OBTransferencias.listar(contexto);
	}

	public static boolean tieneFondos(ContextoOB contexto, BigDecimal monto,String cuentaOrigen){
		BigDecimal saldoGirar = (BigDecimal) OBCuentas.cuenta(contexto,cuentaOrigen).get("saldoGirar");
		return monto.compareTo(saldoGirar)<1;
	}

	public static Object listar(ContextoOB contexto) {
		LogOB.evento(contexto, "listar","listar transferencias");
		Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);
		Integer registrosPorPagina = contexto.parametros.integer("registrosPorPagina", 10);
		String beneficiario = contexto.parametros.string("beneficiario", null);
		Integer idEstado = contexto.parametros.integer("idEstado", null);
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);

		SesionOB sesion = contexto.sesion();

		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
		if (!puedeTransferir(contexto, empresaUsuario)) {
			LogOB.evento(contexto, "listar","OPERACION_INVALIDA");
			return respuesta("OPERACION_INVALIDA");
		}

		ServicioTransferenciaOB servicio = new ServicioTransferenciaOB(contexto);
		PaginaTransferenciaDTO pagina = servicio.find(sesion.empresaOB, numeroPagina, registrosPorPagina, beneficiario, idEstado, fechaDesde, fechaHasta).get();
		LogOB.evento(contexto, "listar","cantidad de transferencias: " + String.valueOf(pagina.cantidad));
		Objeto respuesta = respuesta("0");
		respuesta.set("numeroPagina", pagina.numeroPagina);
		respuesta.set("registrosPorPagina", pagina.registroPorPagina);
		respuesta.set("cantidadRegistros", pagina.cantidad);
		respuesta.set("montoLimite", MONTO_MAXIMO_NUEVO_BENEFICIARIO_PESOS.toString());

		Objeto datos = respuesta.add("datos");

		for (TransferenciaOB transferencia : pagina.transferencias) {
			Objeto trn = new Objeto();
			trn.set("id", transferencia.id);
			trn.set("moneda", transferencia.moneda.simbolo);
			trn.set("monto", transferencia.monto);
			trn.set("fechaCreacion", transferencia.fechaCreacion.toString());
			trn.set("fechaAplicacion", transferencia.fechaAplicacion.toString());

			Objeto estado = new Objeto();

			if (transferencia.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {

				if (transferencia instanceof TransferenciaCredinOB){
					if (transferencia.estado.id==EnumEstadoTRNOB.EN_PROCESO.getCodigo()||(transferencia.estado.id==EnumEstadoTRNOB.EXITO.getCodigo() && transferencia.fechaEjecucion.plusDays(10).isAfter(LocalDateTime.now()))){
						LogOB.evento(contexto,"actualizacion estado TRN CREDIN id: "+transferencia.id,"INICIO");
						TransferenciaCredinOB transferenciaCredinOB = (TransferenciaCredinOB) transferencia;
						LogOB.evento(contexto,"actualizacion estado TRN CREDIN idDebin: "+transferencia.id,transferenciaCredinOB.idDebin);
						ConsultarDebin.ConsultaDebinResponse detalle = ApiDebin.consultarDebin(contexto, transferenciaCredinOB.idDebin).tryGet();
						LogOB.evento(contexto,"actualizacion estado TRN EstadoDebin: "+detalle.estado.codigo);
						if (detalle.estado.codigo.equalsIgnoreCase("ACREDITADO")){
							transferencia.estado = new ServicioEstadoTRNOB(contexto).find(EnumEstadoTRNOB.EXITO.getCodigo()).get();
							servicio.update(transferenciaCredinOB);
						}else if  (ESTADOS_RECHAZADOS.contains(detalle.estado.codigo.toUpperCase())) {
							transferencia.estado = new ServicioEstadoTRNOB(contexto).find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
							servicio.update(transferenciaCredinOB);
						}

					}

				}
				EstadoTRNOB trnEstado = transferencia.estado;
				estado.set("id", trnEstado.id);
				estado.set("descripcionCorta", trnEstado.descripcion);
				trn.set("estado", estado);

			} else {
				validarFechaAplicacionTransferencia(contexto,transferencia);
				estado.set("id", transferencia.estadoBandeja.id);
				estado.set("descripcionCorta", transferencia.estadoBandeja.descripcion);
				trn.set("estado", estado);
			}

			Objeto credito = new Objeto();
			CreditoTranfOB trnCredito = transferencia.credito;
			credito.set("cbu", trnCredito.cbu);
			credito.set("banco", trnCredito.banco.denominacion);
			credito.set("titular", !trnCredito.titular.isBlank() ? trnCredito.titular : trnCredito.cbu);
			trn.set("credito", credito);

			datos.add(trn);
		}
		LogOB.evento(contexto, "listar","retorna lista");
		return respuesta;
	}

	public static Object estadosHistorialTrn(ContextoOB contexto) {
		ServicioEstadoTRNOB servicio = new ServicioEstadoTRNOB(contexto);
		List<EstadoTRNOB> estados = servicio.findAll().get();

		Objeto datos = new Objeto();
		for (EstadoTRNOB estado : estados) {
			if (estado.id != EnumEstadoTRNOB.EN_BANDEJA.getCodigo()) {
				Objeto est = new Objeto();
				est.set("id", estado.id);
				est.set("descripcion", estado.descripcion);
				datos.add(est);
			}
		}

		return respuesta("datos", datos);
	}

	public static Object validarMonto(ContextoOB contexto) {
		String empresa = contexto.parametros.string("empresa");
		String cuenta = contexto.parametros.string("cuenta");
		String monto = contexto.parametros.string("monto");
		String firmante = contexto.parametros.string("firmante");
		Objeto datos = new Objeto();

		SesionOB sesion = contexto.sesion();

		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
		if (!puedeTransferir(contexto, empresaUsuario)) {
			return respuesta("OPERACION_INVALIDA");
		}

		boolean montoPermitido = true;
		try {
			OBFirmas.tipoFirma(contexto, empresa, cuenta, monto, firmante, null, "4");
		} catch (ApiException ae) {
			if (ae.codigoError.equals("EXCEDE_EL_MONTO")) {
				montoPermitido = false;
			}
		}
		datos.set("monto-permitido", montoPermitido);
		return respuesta("datos", datos);
	}

	public static void registrarEnCore(ContextoOB contexto, List<TransferenciaOB> transferencias) {
		LogOB.evento(contexto, "registrarEnCore", "INICIO");
		ServicioEstadoTRNOB servicioEstadoTransferenciasOB = new ServicioEstadoTRNOB(contexto);
		EstadoTRNOB estadoEnProceso = servicioEstadoTransferenciasOB.find(EnumEstadoTRNOB.EN_PROCESO.getCodigo()).get();
		ServicioHistorialBatchTrnOB servicioHistorialBatchTrnOB = new ServicioHistorialBatchTrnOB(contexto);

		ServicioTransferenciaOB servicioTransferenciaOB = new ServicioTransferenciaOB(contexto);
		transferencias.forEach(t -> {
			try {
				PeticionesOB.PeticionOB peticionOB = new PeticionesOB.PeticionOB(t);
				PeticionesOB.post(contexto, peticionOB);
				LogOB.evento(contexto, "registrarEnCore", "REGISTRADO EN ATE");
				EstadoTRNOB estadoAnterior = t.estado;
				t.estado = estadoEnProceso;
				servicioTransferenciaOB.update(t);
				servicioHistorialBatchTrnOB.crear(t, estadoAnterior, estadoEnProceso).get();
				LogOB.evento(contexto, "registrarEnCore", "idTransferencia: "+t.id);
				LogOB.evento(contexto, "registrarEnCore", "fechaAplicacion: "+t.fechaAplicacion);
				LogOB.evento(contexto, "registrarEnCore", "fechaEjecucion: "+t.fechaEjecucion);
			} catch (Exception e) {
				LogOB.evento(contexto, "CAMBIO_ESTADO_TRANSFERENCIA", new Objeto().set("idTransferencia", t.id).set("error", e.getMessage()));
			}
		});
	}

	public static Object validarDiaHabil(ContextoOB contexto) {
		Fecha fechaProgramacion = contexto.parametros.fecha("fechaProgramacion", "yyyy-MM-dd");

		DiaBancario dia = ApiCatalogo.diaBancario(contexto, fechaProgramacion).get();
		boolean diferida = dia.esDiaHabil() ? false : true;

		Objeto datos = new Objeto();
		datos.set("diferida", diferida);
		datos.set("habilPosterior", diferida ? dia.diaHabilPosterior : null);

		return respuesta("datos", datos);
	}

	private static Boolean puedeTransferir(ContextoOB contexto, EmpresaUsuarioOB empresaUsuario) {
		ServicioPermisoOB servicioPermisoOB = new ServicioPermisoOB(contexto);
		if (!contexto.sesion().esOperadorInicial()) {
			ServicioPermisoOperadorOB servicioPermisoOperadorOB = new ServicioPermisoOperadorOB(contexto);
			PermisoOperadorOB permiso = servicioPermisoOperadorOB.buscarPermiso(empresaUsuario, servicioPermisoOB.find(4).get()).tryGet();
			if (empty(permiso)) {
				return false;
			}
		}
		return true;
	}

	public static Objeto puedeAvanzarTransferencia(ContextoOB contexto){
		String cbu = contexto.parametros.string("cbu");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		String cuentaOrigen = contexto.parametros.string("cuentaOrigen",null);
		Objeto respuesta = new Objeto();
		boolean puedeAvanzar = true;
		boolean esTransferenciaSegura = true;
		ServicioTransferenciaOB servicioTransferenciaOB = new ServicioTransferenciaOB(contexto);
		ServicioBeneficiarioOB servicioBeneficiario = new ServicioBeneficiarioOB(contexto);
			InfoCuentaDTO info = servicioBeneficiario.infoCBUAlias(contexto, cbu);
			String cuit = info.cuenta.cuit;
		if (OBTransferencias.esCVU(cbu)) info.monedas = info.monedas.stream().filter(monedaOB -> monedaOB.codigoCobis.equals("ARS")).toList();
			boolean esACuentaPropia = cuit.equals(contexto.sesion().empresaOB.cuit.toString());
			if (!esACuentaPropia){
				boolean esUnipersonal = OBBeneficiarios.esUnipersonal(cuit);
				if (esUnipersonal){
					boolean transferenciasOB = transferenciasDesdeOB(contexto, cbu);
					boolean transferenciasBE=false;
					if (!transferenciasOB) transferenciasBE = transferenciasDesdeBE(contexto,cbu);
					boolean esNuevoBeneficiario = false;
					if (!transferenciasOB&&!transferenciasBE) esNuevoBeneficiario = OBBeneficiarios.esNuevoBeneficiario(contexto, contexto.sesion().empresaOB, cbu);
					esTransferenciaSegura = transferenciasOB||transferenciasBE||!esNuevoBeneficiario;
				}
				boolean cuentaEnPesos = info.monedas.get(0).id == 80;
				if (!esTransferenciaSegura){
					BigDecimal limite = cuentaEnPesos ?MONTO_MAXIMO_NUEVO_BENEFICIARIO_PESOS:MONTO_MAXIMO_NUEVO_BENEFICIARIO_DOLARES;
					List<TransferenciaOB> transferenciasNoRechazadasHoy = servicioTransferenciaOB.findTransfersForTodayByEmpresaAndEstadoRechazado(contexto.sesion().empresaOB.emp_codigo).get();
					Double volumenActual = 0.0;
					for (TransferenciaOB transferencia : transferenciasNoRechazadasHoy) {
						boolean esUniper = OBBeneficiarios.esUnipersonal(transferencia.credito.cuit);
						boolean transfDesdeOB = !transferenciasDesdeOB(contexto, transferencia.credito.cbu);
						boolean transfDesdeBE = !transferenciasDesdeBE(contexto, transferencia.credito.cbu);
						if (esUniper && transfDesdeOB && transfDesdeBE){
							volumenActual+=Double.valueOf(transferencia.monto.toString());
						}
					}
					puedeAvanzar = (volumenActual+Double.valueOf(monto.toString()))<=Double.valueOf(limite.toString());
					respuesta.set("montoLimite", cuentaEnPesos ?MONTO_MAXIMO_NUEVO_BENEFICIARIO_PESOS.toString():MONTO_MAXIMO_NUEVO_BENEFICIARIO_DOLARES.toString());
				}
				else {
					if (cuentaEnPesos){
						puedeAvanzar = Double.valueOf(monto.toString())<=Double.valueOf(MONTO_MAXIMO_SEGURAS_PESOS.toString());
						respuesta.set("montoLimite",MONTO_MAXIMO_SEGURAS_PESOS.toString());
					}else {
						puedeAvanzar = Double.valueOf(monto.toString())<=Double.valueOf(MONTO_MAXIMO_SEGURAS_DOLARES.toString());
						respuesta.set("montoLimite",MONTO_MAXIMO_SEGURAS_DOLARES.toString());
					}

				}
				respuesta.set("moneda",info.monedas.get(0).simbolo);
			}

		respuesta.set("puedeAvanzar",puedeAvanzar);
		respuesta.set("tieneFondos",!OBTransferencias.esCVU(cbu)?true:OBTransferencias.tieneFondos(contexto,monto,cuentaOrigen));

		return respuesta("0","DATOS", respuesta);
		}

	private static boolean transferenciasDesdeBE(ContextoOB contexto,String cbu){
		ServicioTransferenciaBE servicioTransferenciaBE = new ServicioTransferenciaBE(contexto);
		List<BETransferencia> transferenciasBE = servicioTransferenciaBE.existsTransferenciaByCuitEmpresaAndCbuCredito(BigDecimal.valueOf(contexto.sesion().empresaOB.cuit),cbu).get();
		transferenciasBE = transferenciasBE.stream().filter(transferencia->transferencia.fechaCreacion.plusDays(1).isBefore(LocalDateTime.now())).collect(Collectors.toList());
		return transferenciasBE.size()!=0;
	}
	private static boolean transferenciasDesdeOB(ContextoOB contexto, String cbu) {
		ServicioTransferenciaOB servicioTransferenciaOB = new ServicioTransferenciaOB(contexto);
		List<TransferenciaOB> transferenciasOB = servicioTransferenciaOB.yaSeTransfirioACBUOB(contexto.sesion().empresaOB, cbu).get();
		transferenciasOB = transferenciasOB.stream().filter(transferencia->transferencia.fechaCreacion.plusDays(1).isBefore(LocalDateTime.now())).collect(Collectors.toList());
		return transferenciasOB.size()!=0;
	}

	public static boolean esDiferida(HorarioCamaraOB tipoCamara) {
		return LocalTime.now().isAfter(tipoCamara.horaLimite.toLocalTime());
	}

	public static boolean camaraCerrada(HorarioCamaraOB camara){
		return LocalTime.now().isBefore(camara.horaInicio.toLocalTime())||LocalTime.now().isAfter(camara.horaLimite.toLocalTime());
	}

	private static boolean validarHorario(HorarioCamaraOB tipoCamara) {
		LocalTime hora = LocalTime.now();

		// valido que sea antes de la hora de cierre y despues de la hora de inicio
		// indicando la camara
		boolean camaraAbierta = false;
		if (tipoCamara.id == 1) {
			camaraAbierta = hora.isAfter(tipoCamara.horaInicio.toLocalTime());
			if (camaraAbierta) {
				camaraAbierta = hora.isBefore(tipoCamara.horaLimite.toLocalTime());
			}
			return camaraAbierta;

		}
		return camaraAbierta;
	}

	public static Objeto limiteTransferenciaNuevoUniPersonal(ContextoOB contexto){
		return respuesta("0","datos", MONTO_MAXIMO_NUEVO_BENEFICIARIO_PESOS.toString());
	}

	public static Objeto anularTransferenciaProgramada(ContextoOB contexto,ServicioTransferenciaOB servicioTransferenciaOB){
		int idTransferencia = contexto.parametros.integer("idTransferencia");
		TransferenciaOB transferencia = servicioTransferenciaOB.find(idTransferencia).get();
		if (!transferencia.estado.descripcion.equalsIgnoreCase("PROGRAMADA")||!transferencia.empresa.idCobis.equals(contexto.sesion().empresaOB.idCobis)) return respuesta("ERROR","DATOS","Estado de TRN incorrecto");
		try{
			OBFirmas.rechazarTransferenciasSinFirma(contexto,List.of(transferencia));
		} catch (Exception e){
			return respuesta("ERROR","DATOS","Error al anular transferencia");
		}
		return respuesta("0","DATOS","Transferencia anulada correctamente");
	}

	public static void validarFechaAplicacionTransferencia(ContextoOB contexto,TransferenciaOB transferencia){
		if (transferencia.fechaAplicacion.isBefore(LocalDate.now())&&(transferencia.estadoBandeja.id==EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()||transferencia.estadoBandeja.id==EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo())) rechazarTransferenciasNoFirmadas(transferencia,contexto);
	}

	private static void rechazarTransferenciasNoFirmadas(TransferenciaOB transferencia,ContextoOB contexto)
	{
		transferencia.estado = new ServicioEstadoTRNOB(contexto).find(EnumEstadoTRNOB.RECHAZADO.getCodigo()).get();
		transferencia.estadoBandeja = new ServicioEstadoBandejaOB(contexto).find(EnumEstadoBandejaOB.RECHAZADO_EN_FIRMA.getCodigo()).get();
		new ServicioTransferenciaOB(contexto).update(transferencia).get();
	}

	public static Objeto pruebaCredin(ContextoOB contexto){
		String destinatarioCuentaCvu = contexto.parametros.string("destinatarioCuentaCvu");
		String destinatarioCuentaBanco = contexto.parametros.string("destinatarioCuentaBanco");
		String destinatarioIdTributario = contexto.parametros.string("destinatarioIdTributario");
		String destinatarioNombreCompleto = contexto.parametros.string("destinatarioNombreCompleto");

		String detalleConcepto = contexto.parametros.string("detalleConcepto");
		String detalleImporte = contexto.parametros.string("detalleImporte");
		String detalleMonedaId = contexto.parametros.string("detalleMonedaId");
		String detalleMonedaSigno = contexto.parametros.string("detalleMonedaSigno");
		String detalleMonedaDescripcion = contexto.parametros.string("detalleMonedaDescripcion");

		String originanteCuentaCbu = contexto.parametros.string("originanteCuentaCbu");
		String originanteCuentaNumero = contexto.parametros.string("originanteCuentaNumero");
		String originanteCuentaSucursalId = contexto.parametros.string("originanteCuentaSucursalId");
		String originanteCuentaTipo = contexto.parametros.string("originanteCuentaTipo");
		String originanteIdTributario = contexto.parametros.string("originanteIdTributario");
		String originanteMail = contexto.parametros.string("originanteMail");
		String originanteNombreCompleto = contexto.parametros.string("originanteNombreCompleto");
		AutorizarCredin respuesta = AutorizarCredin.post(
				contexto,
				destinatarioCuentaCvu,
				destinatarioCuentaBanco,
				destinatarioIdTributario,
				destinatarioNombreCompleto,
				detalleConcepto,
				detalleImporte,
				detalleMonedaId,
				detalleMonedaSigno,
				detalleMonedaDescripcion,
				originanteCuentaCbu,
				originanteCuentaNumero,
				originanteCuentaSucursalId,
				originanteCuentaTipo,
				originanteIdTributario,
				originanteMail,
				originanteNombreCompleto,
				123456l
		);
		System.out.println("respuesta.toString() = " + respuesta.toString());
		return respuesta("0");

	}

}