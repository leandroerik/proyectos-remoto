package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.exception.UnauthorizedException;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils;
import ar.com.hipotecario.backend.servicio.api.personas.Persona;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.UsuarioISVA;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.LogsBuhoBank.LogBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsalesBB2.SesionEsalesBB2;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesStandBy.SesionStandBy;
import ar.com.hipotecario.backend.util.EncryptableService;
import ar.com.hipotecario.backend.util.LoginLDAP;

public class BBSeguridad extends Modulo {

	public static Objeto crearSesion(ContextoBB contexto) {
		contexto.sesion().limpiarSesion();
		
		String cuil = contexto.parametros.string("cuil");
		String secret = contexto.parametros.string("secret", "");
		String tokenFirebase = contexto.parametros.string("tokenFirebase", null);
		String plataforma = contexto.parametros.string("plataforma", null);
		String sexo = contexto.parametros.string("sexo", null);
		String idDispositivo = contexto.parametros.string("idDispositivo", null);
		String urlQrSucursal = contexto.parametros.string("sucursalOnboarding", "");
		String latitud = contexto.parametros.string("latitud", null);
		String longitud = contexto.parametros.string("longitud", null);
		Boolean usuarioLibertad = contexto.parametros.bool("usuarioLibertad", false);
		String adjustAdid = contexto.parametros.string("adjustAdid", null);
		String adjustGpsAdid = contexto.parametros.string("adjustGpsAdid", null);

		String versionPlataforma = obtenerVersionPlataforma(secret);
		if (!empty(secret) && empty(versionPlataforma)) {
			LogBB.evento(contexto, "ERROR_APP_INVALIDA", contexto.parametros.toString(), cuil);
			return respuesta("APP_INVALIDA");
		}

		contexto.parametros.set("secret", "");
		
		SesionBB sesion = contexto.sesion();
		sesion.reintentos = 0;
		sesion.versionPlataforma = versionPlataforma;
		sesion.idCobis = obtenerIdCobis(contexto, cuil);
		sesion.cuil = cuil;
		sesion.numeroDocumento = cuil.substring(2, 10);
		sesion.fechaLogin = Fecha.ahora();
		sesion.token = sesion.uuid();
		sesion.estado = EstadosBB.SESION_CREADA;
		sesion.tokenFirebase = tokenFirebase;
		sesion.plataforma = plataforma;
		sesion.genero = sexo;
		sesion.usuarioVU = cuil;
		sesion.idDispositivo = idDispositivo;
		sesion.adjustAdid = adjustAdid;
		sesion.adjustGpsAdid = adjustGpsAdid;
		String flujo = sesion.inicializarFlujo(contexto, urlQrSucursal);
		if(GeneralBB.FLUJO_TCV.equals(flujo)){
			flujo = GeneralBB.FLUJO_ONBOARDING;
		}
		sesion.sucursalOnboarding = flujo + "|" + urlQrSucursal;
		sesion.latitud = stringToBigDecimal(latitud);
		sesion.longitud = stringToBigDecimal(longitud);
		sesion.cuilReferido = "";
		sesion.estado = EstadosBB.SESION_CREADA;
		sesion.tdFisica = GeneralBB.VISUALIZA_N;
		sesion.finalizarEnEjecucion = false;
		sesion.crearSesion();
		sesion.limpiezaCache();

		LogBB.evento(contexto, EstadosBB.SESION_CREADA, sesion.getFlujo());
		return respuesta("sesion", sesion.getSesion());
	}

	private static String obtenerIdCobis(ContextoBB contexto, String cuil) {
		Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
		if(persona == null || empty(persona.idCliente) || persona.idCliente.contains("-")){
			return "-1";
		}

		return persona.idCliente;
	}

	private static String obtenerVersionPlataforma(String secret) {

		if (empty(secret)) {
			return "";
		}

		if (GeneralBB.VERSION_PLATAFORMA_0_0_1.equals(secret)) {
			return GeneralBB.NUMERO_VERSION_PLATAFORMA_0_0_1;
		}

		if (GeneralBB.VERSION_PLATAFORMA_0_0_2.equals(secret)) {
			return GeneralBB.NUMERO_VERSION_PLATAFORMA_0_0_2;
		}

		if (GeneralBB.VERSION_PLATAFORMA_0_0_3.equals(secret)) {
			return GeneralBB.NUMERO_VERSION_PLATAFORMA_0_0_3;
		}

		return "";
	}

	public static BigDecimal stringToBigDecimal(String valor) {

		if (empty(valor)) {
			return null;
		}

		BigDecimal valorBigDecimal;

		try {
			valorBigDecimal = new BigDecimal(valor);
		} catch (Exception e) {
			return null;
		}

		return valorBigDecimal.setScale(5, RoundingMode.DOWN);
	}

	public static Object borrarSesion(ContextoBB contexto) {
		contexto.sesion().delete();
		return respuesta();
	}

	public static Objeto obtenerSesion(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();
		Boolean isOffline = contexto.modoOffline();

		Objeto respuesta = respuesta();

		respuesta.set("sesion", sesion.getSesion());
		respuesta.set("sesion.renaperPorNroTramite", validacionRenaperPorNroTramite(contexto));
		respuesta.set("sesion.intentos_vu", BBSeguridad.getIntentosVU(contexto));
		respuesta.set("offline", isOffline);
		return respuesta;
	}

	public static Boolean validacionRenaperPorNroTramite(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();

		if (!retomaSesionStandByEstado(contexto, sesion.cuil)) {
			return false;
		}

		if (!BBPersona.tieneSelfies(contexto, sesion.cuil)) {
			return false;
		}

		return getIntentosVU(contexto) >= 2;
	}

	static Integer getIntentosVU(ContextoBB contexto) {

		Integer intentos = 0;
		SesionBB sesion = contexto.sesion();

		if (empty(sesion.cuil)) {
			return null;
		}

		Fecha fechaDesde = Fecha.ahora().restarDias(GeneralBB.DIAS_RETOMAR_SESION);

		LogsBuhoBank registrosLog = SqlBuhoBank.obtenerRegistros(contexto, sesion.cuil, fechaDesde).tryGet();
		if (registrosLog == null || registrosLog.size() == 0) {
			return intentos;
		}

		for (LogBuhoBank registro : registrosLog) {

			if (registro.evento != null && (registro.evento.contains("user_retry_counter") || registro.evento.contains("VU_CODE_ANDROID"))) {
				intentos++;
				continue;
			}
		}

		return intentos;
	}

	public static Object retomarSesion(ContextoBB contexto) {
		contexto.sesion().limpiarSesion();
		
		String cuil = contexto.parametros.string("cuil");
		String urlQrSucursal = contexto.parametros.string("sucursalOnboarding", null);
		//String plataforma = contexto.parametros.string("plataforma", null);

		if(!"true".equals(contexto.config.string("bb_prendido_retomar_sesion", "true"))) {
			return respuesta("NO_RETOMA_SESION");
		}

		if (!retomaSesionStandByEstado(contexto, cuil)) {
			return respuesta(ErroresBB.DNI_O_IP_INVALIDOS);
		}

		LogsBuhoBank logsBuhoBank = obtenerAbandono(contexto, cuil);
		if (logsBuhoBank == null || logsBuhoBank.size() == 0 || empty(logsBuhoBank.get(0).datos)) {
			return respuesta(ErroresBB.NO_EXISTE_PERSONA);
		}

		SesionEsales sesionEsales = SesionesEsales.getPorToken(contexto, logsBuhoBank.get(0).datos);
		if (!sesionEsales.ip.equals(contexto.ip())) {
			return respuesta(ErroresBB.DNI_O_IP_INVALIDOS);
		}
		
		SesionEsalesBB2 sesionEsalesBB2 = SesionesEsalesBB2.getId(contexto, sesionEsales.id);
		String flujo = contexto.sesion().inicializarFlujo(contexto, urlQrSucursal);
		if(GeneralBB.FLUJO_TCV.equals(flujo)){
			flujo = GeneralBB.FLUJO_ONBOARDING;
		}
		if(!sesionEsalesBB2.sucursal_onboarding.contains(flujo)){
			return respuesta(ErroresBB.NO_SE_PUDO_RETOMAR_SESION);
		}

		if(empty(sesionEsalesBB2.email_otp_validado) || !sesionEsalesBB2.email_otp_validado) {
			return respuesta("NO_RETOMA_SESION");
		}

		if(empty(sesionEsalesBB2.telefono_otp_validado) || !sesionEsalesBB2.telefono_otp_validado) {
			return respuesta("NO_RETOMA_SESION");
		}

		SesionBB sesionBB = SqlEsales.retomarSesionAbandono(contexto, sesionEsales, sesionEsalesBB2).tryGet();
		if (sesionBB == null) {
			LogBB.evento(contexto, ErroresBB.NO_SE_PUDO_RETOMAR_SESION, "En sesión esales " + sesionEsales.token, cuil);
			return respuesta(ErroresBB.NO_SE_PUDO_RETOMAR_SESION);
		}

		LogBB.evento(contexto, EstadosBB.SESION_RETOMADA, sesionEsales.token, cuil);
		return respuesta("sesion", contexto.sesion().getSesion());
	}

	static boolean retomaSesionStandByEstado(ContextoBB contexto, String cuil) {
		SesionStandBy sesionStandBy = SqlEsales.sesionStandBy(contexto, cuil).tryGet();
		if (sesionStandBy == null) {
			return true;
		}

		return SesionesStandBy.FLUJO_VU_OK.equals(sesionStandBy.estado) || SesionesStandBy.CONTROL_OK.equals(sesionStandBy.estado);
	}

	static LogsBuhoBank obtenerAbandono(ContextoBB contexto, String cuil) {
		String estado = EstadosBB.BB_VALIDAR_DATOS_PERSONALES_OK;
		Fecha fechaDesde = Fecha.hoy().restarDias(GeneralBB.DIAS_RETOMAR_SESION);
		return SqlBuhoBank.captarAbandono(contexto, cuil, estado, fechaDesde).tryGet();
	}

	public static Objeto generarCredenciales(ContextoBB contexto) {
		String usuario = contexto.parametros.string("usuario");
		String clave = contexto.parametros.string("clave");
		Objeto respuesta = new Objeto();

		if (usuario.length() < 8) {
			LogBB.error(contexto, "ERROR_GENERACION_CLAVE", "USUARIO_FORMATO_INVALIDO");
			respuesta.set("estado", "USUARIO_FORMATO_INVALIDO");
			return respuesta;
		}

		if (clave.length() != 4) {
			LogBB.error(contexto, "ERROR_GENERACION_CLAVE", "CLAVE_FORMATO_INVALIDO");
			respuesta.set("estado", "CLAVE_FORMATO_INVALIDO");
			return respuesta;
		}

		if (!EstadosBB.FINALIZAR_OK.equals(contexto.sesion().estado)) {
			LogBB.error(contexto, "ERROR_GENERACION_CLAVE", "sesion sin finalizar");
			return respuesta("ERROR");
		}

		Boolean claveActiva = tieneClaveActiva(contexto, contexto.sesion().cuil);
		if(claveActiva == null || claveActiva){
			LogBB.error(contexto, "ERROR_GENERACION_CLAVE", "tiene clave activa");
			return respuesta("ERROR");
		}

		String idCobis = contexto.sesion().getCobis(contexto);
		if (idCobis.contains("-")) {
			LogBB.error(contexto, "ERROR_GENERACION_CLAVE", idCobis);
			respuesta.set("estado", "PERSONA_NO_ENCONTRADA");
			return respuesta;
		}

		ApiObjeto resUsuario = ApiSeguridad.crearClave(contexto, idCobis, usuario, null).get();
		ApiObjeto resClave = ApiSeguridad.crearClave(contexto, idCobis, clave, "numerica").get();
		if (resClave == null || resUsuario == null) {
			LogBB.error(contexto, "ERROR_GENERACION_CLAVE", idCobis);
			respuesta.set("estado", "ERROR_GENERACION_CLAVE");
			return respuesta;
		}

		LogBB.evento(contexto, "GENERAR_CLAVE_OK", idCobis);

		respuesta.set("estado", "0");
		return respuesta;
	}

	public static Boolean tieneClaveActivaBool(ContextoBB contexto, String cuil) {

		Boolean claveActiva = BBSeguridad.tieneClaveActiva(contexto, cuil);
		if (claveActiva == null || claveActiva) {
			return true;
		}

		return false;
	}

	public static Boolean tieneClaveActiva(ContextoBB contexto, String cuil) {
		String idCobis = contexto.sesion().getCobis(contexto);
		if (idCobis.contains("-")) {
			return null;
		}

		UsuarioISVA usuario = ApiSeguridad.usuarioISVA(contexto, idCobis).tryGet();
		if (usuario == null) {
			return null;
		}

		if (usuario.tieneClaveNumerica) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date vencimiento = dateFormat.parse(usuario.fechaVencimientoClaveBuho);
				Date fechaActual = new Date();

				if (fechaActual.before(vencimiento)) {
					return true;
				}
			} catch (ParseException ex) {
				return null;
			}
		}

		return false;
	}

	public static Objeto claveActiva(ContextoBB contexto, Boolean logPrendido) {

		if (logPrendido) {
			LogBB.evento(contexto, "REQUEST_CLAVE_ACTIVA");
		}

		Boolean claveActiva = tieneClaveActiva(contexto, contexto.sesion().cuil);

		if (claveActiva == null) {
			if (logPrendido) {
				LogBB.error(contexto, "ERROR_CLAVE_ACTIVA", "USUARIO_NO_ENCONTRADO");
			}
			return respuesta("USUARIO_NO_ENCONTRADO");
		}

		if (claveActiva) {
			if (logPrendido) {
				LogBB.evento(contexto, "TIENE_CLAVE_ACTIVA");
			}
			return respuesta("TIENE_CLAVE_ACTIVA");
		}

		if (logPrendido) {
			LogBB.evento(contexto, "SIN_CLAVE_ACTIVA_OK");
		}
		return respuesta();
	}

	public static Objeto login(ContextoBB contexto) {

		String numeroDocumento = contexto.parametros.string("numeroDocumento");
		String usuario = contexto.parametros.string("usuario");
		String clave = contexto.parametros.string("clave");
		Objeto respuesta = new Objeto();

		if (usuario.length() < 8) {
			respuesta.set("estado", "USUARIO_FORMATO_INVALIDO");
			return respuesta;
		}

		if (clave.length() != 4) {
			respuesta.set("estado", "CLAVE_FORMATO_INVALIDO");
			return respuesta;
		}

		if (numeroDocumento.length() == 7) {
			numeroDocumento = "0" + numeroDocumento;
		}

		if (numeroDocumento.length() != 8) {
			respuesta.set("estado", "DOCUMENTO_FORMATO_INVALIDO");
			return respuesta;
		}

		Cuils resCuils = ApiPersonas.cuils(contexto, numeroDocumento).tryGet();

		if (resCuils.size() != 1) {
			respuesta.set("estado", "PERSONA_NO_ENCONTRADA");
			return respuesta;
		}

		for (Integer i = 0; i < resCuils.size(); i++) {
			String cuil = resCuils.get(i).getCuil();
			Persona persona = ApiPersonas.persona(contexto, cuil, false).tryGet();
			if (persona != null) {
				if (!persona.idCliente.contains("-")) {

					Boolean res = ApiSeguridad.loginHB(contexto, persona.idCliente, usuario, clave).get();
					if (res != null) {
						respuesta.set("estado", "0");
						return respuesta;
					}
				}
			}
		}

		respuesta.set("estado", "USUARIO_INVALIDO");
		return respuesta;
	}

	public static void main(String[] args) {
		// Generador de nuevo codigo secret para app
		String factor = EncryptableService.factorRandom();
		String stringForToken = "dsahui_" + "secret" + "_" + new Date().getTime() + "_" + factor + "_uhfuihew";
		String secretGenerado = EncryptableService.stringEncryptor().encrypt(stringForToken);
		System.out.println("secret: " + secretGenerado);
		System.out.println("");
	}

	public static Object configLogin(ContextoBB contexto) {
		String usuario = contexto.parametros.string("usuario");
		String clave = contexto.parametros.string("clave");

		Objeto login = LoginLDAP.loginLdpaBB(contexto, usuario, clave);
		Boolean autenticado = (Boolean) login.get("autenticado");
		if (autenticado) {
			String nombre = (String) login.get("nombre");
			String rol = (String) login.get("permisos");

			Objeto respuesta = respuesta();
			respuesta.set("autenticado", login.get("autenticado"));
			respuesta.set("nombre", login.get("nombre"));
			respuesta.set("usuario", login.get("usuario"));
			respuesta.set("permisos", login.get("permisos"));
			respuesta.set("token", JwtBB.create(usuario, nombre, rol));
			return respuesta;
		}
		
		return respuesta("Usuario y/o clave incorrecta");
	}

	public static String getUsuarioJWT(ContextoBB contexto) {

		String tokenJwt = contexto.requestHeader("Token-JWT");
		Objeto valueJwt = tokenJwt == null ? null : JwtBB.getValue(tokenJwt);
		return valueJwt == null ? null : valueJwt.string("value");
	}

	public static Objeto getValueJWT(ContextoBB contexto) {

		String tokenJwt = contexto.requestHeader("Token-JWT");
		return tokenJwt == null ? null : JwtBB.getValue(tokenJwt);
	}

	public static boolean tieneNacionalidad(ContextoBB contexto) {

		SesionBB sesion = contexto.sesion();

		if (!empty(sesion.idNacionalidad) && !empty(sesion.nacionalidad) && !empty(sesion.idPaisNacimiento) && !empty(sesion.paisNacimiento)) {
			return true;
		}

		return false;
	}

    public static Objeto crearSesionV2(ContextoBB contexto) {
		UnauthorizedException.ifNot(contexto.requestHeader("Authorization"));
		contexto.sesion().limpiarSesion();

		String tokenFirebase = contexto.parametros.string("tokenFirebase", null);
		String plataforma = contexto.parametros.string("plataforma");
		String version = contexto.parametros.string("version");

		String cuil = contexto.parametros.string("cuil");
		String nombres = contexto.parametros.string("nombres");
		String apellidos = contexto.parametros.string("apellidos");
		String sexo = contexto.parametros.string("sexo");
		Fecha fechaNacimiento = contexto.parametros.fecha("fechaNacimiento", "dd/MM/yyyy");

		if(cuil.length() != 11 || (!sexo.equals("M") && !sexo.equals("F"))){
			return respuesta("PARAMETROS_INCORRECTOS");
		}

		if(BBPersona.esClienteBool(contexto, cuil)){
			return respuesta("ES_CLIENTE");
		}

		SesionBB sesion = contexto.sesion();
		sesion.reintentos = 0;
		sesion.versionPlataforma = version;
		sesion.plataforma = plataforma;
		sesion.token = sesion.uuid();
		sesion.tokenFirebase = tokenFirebase;

		sesion.nombre = nombres;
		sesion.apellido = apellidos;
		sesion.genero = sexo;
		sesion.fechaNacimiento = fechaNacimiento;
		sesion.cuil = cuil;
		sesion.numeroDocumento = cuil.substring(2, 10);

		sesion.fechaLogin = Fecha.ahora();
		sesion.estado = EstadosBB.SESION_CREADA;
		sesion.sucursalOnboarding = GeneralBB.FLUJO_TCV + "|";
		sesion.finalizarEnEjecucion = false;
		sesion.idEstadoCivil = "S";
		sesion.idSituacionLaboral = "1";
		sesion.idSucursal = null;
		sesion.formaEntrega = GeneralBB.ENTREGA_DOMICILIO;
		sesion.crearSesion();
		sesion.limpiezaCache();

		LogBB.evento(contexto, EstadosBB.SESION_CREADA, sesion.getFlujo());
		return respuesta();
    }
}
